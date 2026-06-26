package com.aem.tiretrack.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aem.tiretrack.dto.WorkOrderRequest;
import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.InvoiceItemType;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.enums.WorkOrderStatus;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.InvoiceItem;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.WorkOrder;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.TireRepository;
import com.aem.tiretrack.repository.UserRepository;
import com.aem.tiretrack.repository.WorkOrderRepository;

@Service
public class WorkOrderService {
    private final WorkOrderRepository workOrderRepository;
    private final AppointmentRepository appointmentRepository;
    private final TireRepository tireRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final ShopContextService shopContextService;

    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            AppointmentRepository appointmentRepository,
            TireRepository tireRepository,
            UserRepository userRepository,
            InvoiceService invoiceService,
            ShopContextService shopContextService) {
        this.workOrderRepository = workOrderRepository;
        this.appointmentRepository = appointmentRepository;
        this.tireRepository = tireRepository;
        this.userRepository = userRepository;
        this.invoiceService = invoiceService;
        this.shopContextService = shopContextService;
    }

    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll().stream()
                .filter(this::canAccessWorkOrder)
                .toList();
    }

    public WorkOrder getWorkOrderById(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found with id: " + id));
        ensureWorkOrderAccess(workOrder);
        return workOrder;
    }

    @Transactional
    public WorkOrder createWorkOrder(WorkOrderRequest request) {
        WorkOrder workOrder = new WorkOrder();
        applyRequest(workOrder, request, true);
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder createFromAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + appointmentId));
        ensureAppointmentAccess(appointment);

        workOrderRepository.findByAppointment_Id(appointmentId)
                .filter(this::canAccessWorkOrder)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("This appointment already has a work order.");
                });

        WorkOrder workOrder = new WorkOrder();
        populateFromAppointment(workOrder, appointment);
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder updateWorkOrder(Long id, WorkOrderRequest request) {
        WorkOrder workOrder = getWorkOrderById(id);
        if (workOrder.getStatus() == WorkOrderStatus.COMPLETED || workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Completed or cancelled work orders cannot be edited.");
        }
        applyRequest(workOrder, request, false);
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder startWorkOrder(Long id) {
        WorkOrder workOrder = getWorkOrderById(id);
        requireNotCancelled(workOrder);
        if (workOrder.getStatus() == WorkOrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Completed work orders cannot be restarted.");
        }
        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        if (workOrder.getStartedAt() == null) {
            workOrder.setStartedAt(LocalDateTime.now());
        }
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder markVehicleReady(Long id) {
        WorkOrder workOrder = getWorkOrderById(id);
        requireNotCancelled(workOrder);
        if (workOrder.getStatus() == WorkOrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Completed work orders are already finished.");
        }
        if (workOrder.getStartedAt() == null) {
            workOrder.setStartedAt(LocalDateTime.now());
        }
        workOrder.setStatus(WorkOrderStatus.VEHICLE_READY);
        workOrder.setVehicleReadyAt(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder completeWorkOrder(Long id) {
        WorkOrder workOrder = getWorkOrderById(id);
        requireNotCancelled(workOrder);
        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        if (workOrder.getCompletedAt() == null) {
            workOrder.setCompletedAt(LocalDateTime.now());
        }
        markLinkedAppointmentCompleted(workOrder);
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder cancelWorkOrder(Long id) {
        WorkOrder workOrder = getWorkOrderById(id);
        if (workOrder.getInvoiceId() != null) {
            throw new IllegalArgumentException("Invoiced work orders cannot be cancelled.");
        }
        workOrder.setStatus(WorkOrderStatus.CANCELLED);
        return workOrderRepository.save(workOrder);
    }

    public Invoice previewInvoice(Long id) {
        WorkOrder workOrder = getWorkOrderById(id);
        validateCanConvertToInvoice(workOrder);
        return buildInvoiceDraft(workOrder);
    }

    @Transactional
    public Invoice convertToInvoice(Long id) {
        WorkOrder workOrder = getWorkOrderById(id);
        validateCanConvertToInvoice(workOrder);

        Invoice savedInvoice = invoiceService.saveInvoice(buildInvoiceDraft(workOrder));
        workOrder.setInvoiceId(savedInvoice.getId());
        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        if (workOrder.getCompletedAt() == null) {
            workOrder.setCompletedAt(LocalDateTime.now());
        }
        markLinkedAppointmentCompleted(workOrder);
        workOrderRepository.save(workOrder);
        return savedInvoice;
    }

    @Transactional
    public WorkOrder linkInvoice(Long id, Long invoiceId) {
        WorkOrder workOrder = getWorkOrderById(id);
        validateCanConvertToInvoice(workOrder);

        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        if (workOrder.getShop() != null
                && invoice.getShop() != null
                && !workOrder.getShop().getId().equals(invoice.getShop().getId())) {
            throw new AccessDeniedException("Invoice does not belong to this work order's shop.");
        }

        workOrder.setInvoiceId(invoice.getId());
        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        if (workOrder.getCompletedAt() == null) {
            workOrder.setCompletedAt(LocalDateTime.now());
        }
        markLinkedAppointmentCompleted(workOrder);
        return workOrderRepository.save(workOrder);
    }

    private void validateCanConvertToInvoice(WorkOrder workOrder) {
        if (workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled work orders cannot be invoiced.");
        }
        if (workOrder.getInvoiceId() != null) {
            throw new IllegalArgumentException("This work order has already been converted to invoice #" + workOrder.getInvoiceId());
        }
    }

    private Invoice buildInvoiceDraft(WorkOrder workOrder) {
        Invoice invoice = new Invoice();
        invoice.setCustomerId(workOrder.getCustomer() == null ? null : workOrder.getCustomer().getId());
        invoice.setCustomerName(workOrder.getCustomerName());
        invoice.setPhone(workOrder.getPhone());
        invoice.setVehicle(workOrder.getVehicle());
        invoice.setStatus("UNPAID");
        invoice.setPaymentMethod("Manual");
        invoice.setShop(workOrder.getShop());
        invoice.setShopLocation(workOrder.getShopLocation());

        if (workOrder.getAppointment() != null) {
            invoice.setAppointmentId(workOrder.getAppointment().getId());
            addAppointmentTireItems(invoice, workOrder.getAppointment());
        }

        if (invoice.getItems().isEmpty()) {
            InvoiceItem item = new InvoiceItem();
            item.setItemType(InvoiceItemType.SERVICE);
            item.setItemName(label(workOrder.getServiceType()) + " service");
            item.setQuantity(1);
            item.setUnitPrice(BigDecimal.ZERO);
            invoice.addItem(item);
        }

        return invoice;
    }

    private void applyRequest(WorkOrder workOrder, WorkOrderRequest request, boolean creating) {
        if (request == null) {
            throw new IllegalArgumentException("Work order details are required.");
        }

        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + request.getAppointmentId()));
            ensureAppointmentAccess(appointment);
            workOrder.setAppointment(appointment);
            if (creating) {
                populateFromAppointment(workOrder, appointment);
            }
        }

        if (request.getCustomerId() != null) {
            User customer = userRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + request.getCustomerId()));
            if (customer.getRole() != UserRole.CUSTOMER) {
                throw new IllegalArgumentException("Selected user is not a customer.");
            }
            ensureUserAccess(customer);
            workOrder.setCustomer(customer);
            workOrder.setCustomerName(customer.getFullName());
            workOrder.setPhone(customer.getPhone());
            workOrder.setEmail(customer.getEmail());
        }

        if (request.getAssignedEmployeeId() != null) {
            User employee = userRepository.findById(request.getAssignedEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + request.getAssignedEmployeeId()));
            if (employee.getRole() != UserRole.EMPLOYEE) {
                throw new IllegalArgumentException("Assigned user is not an employee.");
            }
            ensureUserAccess(employee);
            workOrder.setAssignedEmployee(employee);
        } else {
            workOrder.setAssignedEmployee(null);
        }

        if (request.getCustomerName() != null && !request.getCustomerName().isBlank()) {
            workOrder.setCustomerName(request.getCustomerName().trim());
        }
        if (request.getPhone() != null) {
            workOrder.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            workOrder.setEmail(request.getEmail());
        }
        if (request.getVehicle() != null) {
            workOrder.setVehicle(request.getVehicle());
        }
        if (request.getServiceType() != null) {
            workOrder.setServiceType(request.getServiceType());
        }
        workOrder.setNotes(request.getNotes());

        if (request.getLocationId() != null) {
            ShopLocation requestedLocation = shopContextService.resolveAccessibleLocation(
                    request.getLocationId(),
                    workOrder.getShop(),
                    true).orElse(null);
            if (requestedLocation != null) {
                workOrder.setShopLocation(requestedLocation);
                if (workOrder.getShop() == null) {
                    workOrder.setShop(requestedLocation.getShop());
                }
            }
        }

        if (workOrder.getShop() == null) {
            shopContextService.getCurrentTenantShop().ifPresent(workOrder::setShop);
        }
        if (workOrder.getShopLocation() == null) {
            shopContextService.getCurrentTenantLocation()
                    .filter(location -> workOrder.getShop() == null
                            || (location.getShop() != null && location.getShop().getId().equals(workOrder.getShop().getId())))
                    .ifPresent(workOrder::setShopLocation);
        }
        validateRequired(workOrder);
    }

    private void populateFromAppointment(WorkOrder workOrder, Appointment appointment) {
        workOrder.setAppointment(appointment);
        workOrder.setShop(appointment.getShop());
        workOrder.setShopLocation(appointment.getShopLocation());
        workOrder.setCustomerName(appointment.getCustomerName());
        workOrder.setPhone(appointment.getPhone());
        workOrder.setEmail(appointment.getEmail());
        workOrder.setVehicle(appointment.getVehicle());
        workOrder.setServiceType(appointment.getServiceType());
        workOrder.setNotes(appointment.getNotes());

        if (appointment.getCustomerId() != null) {
            userRepository.findById(appointment.getCustomerId()).ifPresent(workOrder::setCustomer);
        }
    }

    private void addAppointmentTireItems(Invoice invoice, Appointment appointment) {
        addTireItem(invoice, appointment.getFrontTireId(), appointment.getFrontQuantity());
        if (appointment.getRearTireId() != null && !appointment.getRearTireId().equals(appointment.getFrontTireId())) {
            addTireItem(invoice, appointment.getRearTireId(), appointment.getRearQuantity());
        }
    }

    private void addTireItem(Invoice invoice, Long tireId, int quantity) {
        if (tireId == null || quantity <= 0) {
            return;
        }
        Tire tire = tireRepository.findById(tireId)
                .orElseThrow(() -> new IllegalArgumentException("Tire not found with id: " + tireId));
        if (!shopContextService.canAccessTenantResource(tire.getShop(), tire.getShopLocation())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }
        InvoiceItem item = new InvoiceItem();
        item.setItemType(InvoiceItemType.TIRE);
        item.setTireId(tire.getId());
        item.setItemName(tire.getBrand() + " " + tire.getTireSize());
        item.setQuantity(quantity);
        item.setUnitPrice(tire.getPrice());
        invoice.addItem(item);
    }

    private void validateRequired(WorkOrder workOrder) {
        if (workOrder.getCustomerName() == null || workOrder.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (workOrder.getServiceType() == null) {
            throw new IllegalArgumentException("Service type is required.");
        }
    }

    private void requireNotCancelled(WorkOrder workOrder) {
        if (workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled work orders cannot be changed.");
        }
    }

    private void ensureWorkOrderAccess(WorkOrder workOrder) {
        if (!canAccessWorkOrder(workOrder)) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }
    }

    private boolean canAccessWorkOrder(WorkOrder workOrder) {
        return shopContextService.canAccessTenantResource(workOrder.getShop(), workOrder.getShopLocation());
    }

    private void ensureAppointmentAccess(Appointment appointment) {
        if (!shopContextService.canAccessTenantResource(appointment.getShop(), appointment.getShopLocation())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }
    }

    private void ensureUserAccess(User user) {
        if (!shopContextService.canAccessTenantUser(user)) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }
    }

    private void markLinkedAppointmentCompleted(WorkOrder workOrder) {
        Appointment appointment = workOrder.getAppointment();

        if (appointment == null) {
            return;
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
    }

    private String label(Object value) {
        return String.valueOf(value == null ? "Service" : value)
                .replace("_", " ")
                .toLowerCase();
    }
}
