package com.aem.tiretrack.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.PayrollShiftSlotRequest;
import com.aem.tiretrack.dto.PayrollShiftSlotResponse;
import com.aem.tiretrack.dto.PayrollShiftSlotResponse.SignupSummary;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.PayrollPeriod;
import com.aem.tiretrack.model.PayrollShiftSignup;
import com.aem.tiretrack.model.PayrollShiftSlot;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.repository.PayrollPeriodRepository;
import com.aem.tiretrack.repository.PayrollShiftSignupRepository;
import com.aem.tiretrack.repository.PayrollShiftSlotRepository;
import com.aem.tiretrack.repository.UserRepository;

@Service
public class PayrollScheduleService {
    private final PayrollShiftSlotRepository slotRepository;
    private final PayrollShiftSignupRepository signupRepository;
    private final PayrollPeriodRepository periodRepository;
    private final UserRepository userRepository;
    private final ShopContextService shopContextService;

    public PayrollScheduleService(PayrollShiftSlotRepository slotRepository, PayrollShiftSignupRepository signupRepository, PayrollPeriodRepository periodRepository, UserRepository userRepository, ShopContextService shopContextService) {
        this.slotRepository = slotRepository;
        this.signupRepository = signupRepository;
        this.periodRepository = periodRepository;
        this.userRepository = userRepository;
        this.shopContextService = shopContextService;
    }

    public List<PayrollShiftSlotResponse> getSlots(Long periodId) {
        User currentUser = currentUser();
        Long shopId = shopContextService.isSuperAdmin() ? null : shopContextService.requireShopForAdminOrEmployee().getId();
        List<PayrollShiftSlot> slots;
        if (shopId == null) {
            slots = periodId == null
                    ? slotRepository.findAllByOrderByShiftDateAscStartTimeAsc()
                    : slotRepository.findByPayrollPeriod_IdOrderByShiftDateAscStartTimeAsc(periodId);
        } else {
            slots = periodId == null
                    ? slotRepository.findByPayrollPeriod_Shop_IdOrderByShiftDateAscStartTimeAsc(shopId)
                    : slotRepository.findByPayrollPeriod_Shop_IdAndPayrollPeriod_IdOrderByShiftDateAscStartTimeAsc(shopId, periodId);
        }

        return slots.stream()
                .map(slot -> toResponse(slot, currentUser))
                .toList();
    }

    public PayrollShiftSlotResponse createSlot(PayrollShiftSlotRequest request) {
        validateSlotRequest(request);
        PayrollPeriod period = periodRepository.findById(request.getPayrollPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("Payroll period not found with id: " + request.getPayrollPeriodId()));
        ensurePeriodAccess(period);

        if (request.getShiftDate().isBefore(period.getStartDate()) || request.getShiftDate().isAfter(period.getEndDate())) {
            throw new IllegalArgumentException("Shift date must be inside the payroll period");
        }

        PayrollShiftSlot slot = new PayrollShiftSlot();
        slot.setPayrollPeriod(period);
        slot.setShiftDate(request.getShiftDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setRequiredEmployees(request.getRequiredEmployees());
        slot.setNotes(request.getNotes());

        return toResponse(slotRepository.save(slot), currentUser());
    }

    @Transactional
    public void deleteSlot(long slotId) {
        PayrollShiftSlot slot = getSlot(slotId);
        signupRepository.deleteBySlot_Id(slotId);
        slotRepository.delete(slot);
    }

    @Transactional
    public PayrollShiftSlotResponse signup(long slotId) {
        User employee = currentUser();
        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("Only employees can sign up for shifts");
        }

        return assignEmployee(slotId, employee.getId());
    }

    @Transactional
    public PayrollShiftSlotResponse cancelSignup(long slotId) {
        User employee = currentUser();
        PayrollShiftSlot slot = getSlot(slotId);
        PayrollShiftSignup signup = signupRepository.findBySlot_IdAndEmployee_Id(slotId, employee.getId())
                .orElseThrow(() -> new IllegalArgumentException("Shift signup not found"));
        signupRepository.delete(signup);
        return toResponse(slot, employee);
    }

    @Transactional
    public PayrollShiftSlotResponse assignEmployee(long slotId, long employeeId) {
        PayrollShiftSlot slot = getSlot(slotId);
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("User is not an employee");
        }
        ensureEmployeeCanUseSlot(employee, slot);

        if (signupRepository.existsBySlot_IdAndEmployee_Id(slotId, employeeId)) {
            return toResponse(slot, currentUser());
        }

        if (signupRepository.countBySlot_Id(slotId) >= slot.getRequiredEmployees()) {
            throw new IllegalArgumentException("This shift is full");
        }

        PayrollShiftSignup signup = new PayrollShiftSignup();
        signup.setSlot(slot);
        signup.setEmployee(employee);
        signupRepository.save(signup);

        return toResponse(slot, currentUser());
    }

    public PayrollShiftSlotResponse removeSignup(long slotId, long signupId) {
        PayrollShiftSignup signup = signupRepository.findById(signupId)
                .orElseThrow(() -> new IllegalArgumentException("Shift signup not found"));

        if (!signup.getSlot().getId().equals(slotId)) {
            throw new IllegalArgumentException("Shift signup does not belong to this slot");
        }
        ensurePeriodAccess(signup.getSlot().getPayrollPeriod());
        ensureEmployeeCanUseSlot(signup.getEmployee(), signup.getSlot());

        signupRepository.delete(signup);
        return toResponse(getSlot(slotId), currentUser());
    }

    private PayrollShiftSlot getSlot(long slotId) {
        PayrollShiftSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Shift slot not found with id: " + slotId));
        ensurePeriodAccess(slot.getPayrollPeriod());
        return slot;
    }

    private PayrollShiftSlotResponse toResponse(PayrollShiftSlot slot, User currentUser) {
        List<PayrollShiftSignup> signups = signupRepository.findBySlot_Id(slot.getId()).stream()
                .sorted(Comparator.comparing(PayrollShiftSignup::getCreatedAt))
                .toList();
        boolean signedUp = currentUser != null && signups.stream()
                .anyMatch(signup -> signup.getEmployee().getId().equals(currentUser.getId()));
        List<SignupSummary> summaries = signups.stream()
                .map(signup -> new SignupSummary(
                        signup.getId(),
                        signup.getEmployee().getId(),
                        signup.getEmployee().getFullName(),
                        signup.getEmployee().getEmail(),
                        signup.getCreatedAt()))
                .toList();

        return new PayrollShiftSlotResponse(
                slot.getId(),
                slot.getPayrollPeriod().getId(),
                slot.getShiftDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getRequiredEmployees(),
                signups.size(),
                slot.getNotes(),
                slot.getCreatedAt(),
                signedUp,
                summaries);
    }

    private void validateSlotRequest(PayrollShiftSlotRequest request) {
        if (request.getPayrollPeriodId() == null) {
            throw new IllegalArgumentException("Payroll period is required");
        }

        if (request.getShiftDate() == null || request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Shift date, start time, and end time are required");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("Shift end time must be after start time");
        }

        if (request.getRequiredEmployees() == null || request.getRequiredEmployees() < 1) {
            throw new IllegalArgumentException("Required employees must be at least 1");
        }
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void ensurePeriodAccess(PayrollPeriod period) {
        if (!shopContextService.canAccessTenantResource(period.getShop(), period.getShopLocation())) {
            throw new IllegalArgumentException("Payroll period not found with id: " + period.getId());
        }
    }

    private void ensureEmployeeCanUseSlot(User employee, PayrollShiftSlot slot) {
        Long slotShopId = slot.getPayrollPeriod().getShopId();
        if (slotShopId == null) {
            if (shopContextService.isSuperAdmin()) {
                return;
            }
            throw new IllegalArgumentException("Shift slot is not assigned to a shop");
        }

        if (employee.getShop() == null || !slotShopId.equals(employee.getShop().getId())) {
            throw new IllegalArgumentException("Employee does not belong to this shop");
        }
    }
}
