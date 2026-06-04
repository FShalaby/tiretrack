package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.aem.tiretrack.dto.WorkShiftRequest;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.WorkShift;
import com.aem.tiretrack.repository.UserRepository;
import com.aem.tiretrack.repository.WorkShiftRepository;

@Service
public class WorkShiftService {

    private final WorkShiftRepository workShiftRepository;
    private final UserRepository userRepository;
    private final ShopContextService shopContextService;

    public WorkShiftService(WorkShiftRepository workShiftRepository, UserRepository userRepository, ShopContextService shopContextService) {
        this.workShiftRepository = workShiftRepository;
        this.userRepository = userRepository;
        this.shopContextService = shopContextService;
    }

    public List<WorkShift> getAllShifts()
    {
        if (shopContextService.isSuperAdmin()) {
            return workShiftRepository.findAll();
        }

        return workShiftRepository.findByEmployee_Shop_Id(shopContextService.requireShopForAdminOrEmployee().getId());
    }

    public WorkShift getShiftById(long id)
    {
       WorkShift shift = workShiftRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Shift not found with id: " + id));
       ensureShiftAccess(shift);
       return shift;
    }

    public List<WorkShift> getShiftsByEmployeeId(long employeeId)
    {
        ensureEmployeeAccess(employeeById(employeeId));
        return workShiftRepository.findByEmployee_Id(employeeId);
    }

    public List<WorkShift> getShiftsByEmployeeIdAndDateRange(long employeeId, String start, String end)
    {
        ensureEmployeeAccess(employeeById(employeeId));
        return workShiftRepository.findByEmployee_IdAndShiftDateBetween(employeeId, java.time.LocalDate.parse(start), java.time.LocalDate.parse(end));
    }

    public WorkShift createShift(WorkShiftRequest request)
    {
        WorkShift shift = new WorkShift();
        User user = employeeById(request.getEmployeeId());
        if(user.getRole() != UserRole.EMPLOYEE)
        {
            throw new IllegalArgumentException("User with id: " + request.getEmployeeId() + " is not an employee");
        }
        ensureEmployeeAccess(user);
    
        shift.setEmployee(user);
        shift.setShiftDate(request.getShiftDate());
        shift.setClockIn(request.getClockIn());
        shift.setClockOut(request.getClockOut());
        shift.setBreakMinutes(request.getBreakMinutes());
        shift.setNotes(request.getNotes());
        shift.setWorkedHours(calculateWorkedHours(shift));

        return workShiftRepository.save(shift);
    }

    public void deleteShift(long id)
    {
        WorkShift shift = getShiftById(id);
        workShiftRepository.delete(shift);
    }

    public WorkShift updateShift(long id, WorkShiftRequest request)
    {
        WorkShift shift = getShiftById(id);
        User user = employeeById(request.getEmployeeId());
        if(user.getRole() != UserRole.EMPLOYEE)
            {
                throw new IllegalArgumentException("User with id: " + request.getEmployeeId() + " is not an employee");
            }
            ensureEmployeeAccess(user);
            shift.setEmployee(user);
            shift.setShiftDate(request.getShiftDate());
            shift.setClockIn(request.getClockIn());
            shift.setClockOut(request.getClockOut());
            shift.setBreakMinutes(request.getBreakMinutes());
            shift.setNotes(request.getNotes());
            shift.setWorkedHours(calculateWorkedHours(shift));
            return workShiftRepository.save(shift);
        }
    
            
    private BigDecimal calculateWorkedHours(WorkShift shift) {
    if (shift.getClockIn() == null || shift.getClockOut() == null) {
        throw new IllegalArgumentException("Clock in and clock out are required");
    }

    if (shift.getClockOut().isBefore(shift.getClockIn())) {
        throw new IllegalArgumentException("Clock out cannot be before clock in");
    }

    int breakMinutes = shift.getBreakMinutes() == null ? 0 : shift.getBreakMinutes();

    if (breakMinutes < 0) {
        throw new IllegalArgumentException("Break minutes cannot be negative");
    }

    long totalMinutes = Duration.between(shift.getClockIn(),shift.getClockOut()).toMinutes() - breakMinutes;

    if (totalMinutes < 0) {
        throw new IllegalArgumentException("Worked hours cannot be negative");
    }

    return BigDecimal.valueOf(totalMinutes)
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
}

    private User employeeById(long employeeId) {
        return userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
    }

    private void ensureShiftAccess(WorkShift shift) {
        ensureEmployeeAccess(shift.getEmployee());
    }

    private void ensureEmployeeAccess(User employee) {
        if (!shopContextService.canAccessTenantUser(employee)) {
            throw new AccessDeniedException("You do not have permission to access this employee.");
        }
    }
}
