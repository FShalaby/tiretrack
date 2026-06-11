package com.aem.tiretrack.dto.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.CustomerVehicle;

public class CustomerSummary {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Long shopId;
    private String shopName;
    private Long locationId;
    private String locationName;
    private long vehicleCount;
    private long appointmentCount;
    private long invoiceCount;
    private BigDecimal totalSpent;
    private BigDecimal outstandingBalance;
    private Long nextUnpaidInvoiceId;
    private LocalDate nextPaymentDueDate;
    private boolean hasOverdueBalance;
    private boolean hasBalanceDueSoon;
    private Long nextAppointmentId;
    private LocalDateTime nextAppointmentDate;
    private String nextAppointmentVehicle;
    private boolean hasUpcomingAppointment;
    private List<CustomerVehicleResponse> vehicles;
    private List<CustomerInvoiceSummary> unpaidInvoices;

    public CustomerSummary(User user, long vehicleCount, long appointmentCount, long invoiceCount, BigDecimal totalSpent, BigDecimal outstandingBalance, Long nextUnpaidInvoiceId, LocalDate nextPaymentDueDate, boolean hasOverdueBalance, boolean hasBalanceDueSoon, Long nextAppointmentId, LocalDateTime nextAppointmentDate, String nextAppointmentVehicle, boolean hasUpcomingAppointment, List<CustomerVehicle> vehicles, List<CustomerInvoiceSummary> unpaidInvoices) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.shopId = user.getShop() == null ? null : user.getShop().getId();
        this.shopName = user.getShop() == null ? null : user.getShop().getName();
        this.locationId = user.getLocationId();
        this.locationName = user.getLocationName();
        this.vehicleCount = vehicleCount;
        this.appointmentCount = appointmentCount;
        this.invoiceCount = invoiceCount;
        this.totalSpent = totalSpent;
        this.outstandingBalance = outstandingBalance;
        this.nextUnpaidInvoiceId = nextUnpaidInvoiceId;
        this.nextPaymentDueDate = nextPaymentDueDate;
        this.hasOverdueBalance = hasOverdueBalance;
        this.hasBalanceDueSoon = hasBalanceDueSoon;
        this.nextAppointmentId = nextAppointmentId;
        this.nextAppointmentDate = nextAppointmentDate;
        this.nextAppointmentVehicle = nextAppointmentVehicle;
        this.hasUpcomingAppointment = hasUpcomingAppointment;
        this.vehicles = vehicles.stream().map(CustomerVehicleResponse::new).toList();
        this.unpaidInvoices = unpaidInvoices;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public long getVehicleCount() { return vehicleCount; }
    public long getAppointmentCount() { return appointmentCount; }
    public long getInvoiceCount() { return invoiceCount; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public Long getNextUnpaidInvoiceId() { return nextUnpaidInvoiceId; }
    public LocalDate getNextPaymentDueDate() { return nextPaymentDueDate; }
    public boolean isHasOverdueBalance() { return hasOverdueBalance; }
    public boolean isHasBalanceDueSoon() { return hasBalanceDueSoon; }
    public Long getNextAppointmentId() { return nextAppointmentId; }
    public LocalDateTime getNextAppointmentDate() { return nextAppointmentDate; }
    public String getNextAppointmentVehicle() { return nextAppointmentVehicle; }
    public boolean isHasUpcomingAppointment() { return hasUpcomingAppointment; }
    public List<CustomerVehicleResponse> getVehicles() { return vehicles; }
    public List<CustomerInvoiceSummary> getUnpaidInvoices() { return unpaidInvoices; }
}
