package com.aem.tiretrack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import com.aem.tiretrack.dto.CompanySettingsRequest;
import com.aem.tiretrack.dto.DashboardSummary;
import com.aem.tiretrack.dto.InvoiceStatusUpdateRequest;
import com.aem.tiretrack.dto.PayrollGenerationResponse;
import com.aem.tiretrack.dto.customer.CustomerAppointmentRequest;
import com.aem.tiretrack.dto.customer.CustomerPortalResponse;
import com.aem.tiretrack.enums.AbsenceDecision;
import com.aem.tiretrack.enums.AppointmentStatus;
import com.aem.tiretrack.enums.AttendanceStatus;
import com.aem.tiretrack.enums.Condition;
import com.aem.tiretrack.enums.EstimateStatus;
import com.aem.tiretrack.enums.ExpenseCategory;
import com.aem.tiretrack.enums.InvoiceItemType;
import com.aem.tiretrack.enums.PayrollStatus;
import com.aem.tiretrack.enums.ServiceType;
import com.aem.tiretrack.enums.ShopLocationType;
import com.aem.tiretrack.enums.UserRole;
import com.aem.tiretrack.enums.WorkOrderStatus;
import com.aem.tiretrack.model.AppNotification;
import com.aem.tiretrack.model.Appointment;
import com.aem.tiretrack.model.CompanySettings;
import com.aem.tiretrack.model.CustomerVehicle;
import com.aem.tiretrack.model.EmployeeAttendance;
import com.aem.tiretrack.model.Estimate;
import com.aem.tiretrack.model.Expense;
import com.aem.tiretrack.model.Invoice;
import com.aem.tiretrack.model.InvoiceItem;
import com.aem.tiretrack.model.PayrollPeriod;
import com.aem.tiretrack.model.PayrollRecord;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.model.TireRequest;
import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.WorkOrder;
import com.aem.tiretrack.repository.AppNotificationRepository;
import com.aem.tiretrack.repository.AppointmentRepository;
import com.aem.tiretrack.repository.CompanySettingsRepository;
import com.aem.tiretrack.repository.CustomerVehicleRepository;
import com.aem.tiretrack.repository.EmployeeAttendanceRepository;
import com.aem.tiretrack.repository.EstimateRepository;
import com.aem.tiretrack.repository.ExpenseRepository;
import com.aem.tiretrack.repository.InvoiceRepository;
import com.aem.tiretrack.repository.JournalEntryRepository;
import com.aem.tiretrack.repository.PayrollPeriodRepository;
import com.aem.tiretrack.repository.PayrollRecordRepository;
import com.aem.tiretrack.repository.ShopRepository;
import com.aem.tiretrack.repository.ShopLocationRepository;
import com.aem.tiretrack.repository.TireRepository;
import com.aem.tiretrack.repository.TireRequestRepository;
import com.aem.tiretrack.repository.UserRepository;
import com.aem.tiretrack.repository.WorkOrderRepository;
import com.aem.tiretrack.security.JwtService;
import com.aem.tiretrack.service.AccountingService;
import com.aem.tiretrack.service.AppointmentService;
import com.aem.tiretrack.service.AttendanceService;
import com.aem.tiretrack.service.CompanySettingsService;
import com.aem.tiretrack.service.CustomerPortalService;
import com.aem.tiretrack.service.DashboardService;
import com.aem.tiretrack.service.EstimateService;
import com.aem.tiretrack.service.InvoiceService;
import com.aem.tiretrack.service.NotificationService;
import com.aem.tiretrack.service.PdfService;
import com.aem.tiretrack.service.PayrollService;
import com.aem.tiretrack.service.TireService;
import com.aem.tiretrack.service.WorkOrderService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ProductionReadinessIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;

    @Autowired private ShopRepository shopRepository;
    @Autowired private ShopLocationRepository shopLocationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TireRepository tireRepository;
    @Autowired private TireRequestRepository tireRequestRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private CompanySettingsRepository companySettingsRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private EstimateRepository estimateRepository;
    @Autowired private WorkOrderRepository workOrderRepository;
    @Autowired private CustomerVehicleRepository vehicleRepository;
    @Autowired private EmployeeAttendanceRepository attendanceRepository;
    @Autowired private PayrollPeriodRepository payrollPeriodRepository;
    @Autowired private PayrollRecordRepository payrollRecordRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private AppNotificationRepository notificationRepository;
    @Autowired private JournalEntryRepository journalEntryRepository;

    @Autowired private TireService tireService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private InvoiceService invoiceService;
    @Autowired private EstimateService estimateService;
    @Autowired private WorkOrderService workOrderService;
    @Autowired private AttendanceService attendanceService;
    @Autowired private PayrollService payrollService;
    @Autowired private AccountingService accountingService;
    @Autowired private CompanySettingsService companySettingsService;
    @Autowired private CustomerPortalService customerPortalService;
    @Autowired private DashboardService dashboardService;
    @Autowired private NotificationService notificationService;
    @Autowired private PdfService pdfService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminListAndDirectIdAccessAreShopIsolated() {
        TenantFixture fixture = tenantFixture();

        signIn(fixture.adminA);

        assertThat(tireService.getAllTires()).extracting(Tire::getId).contains(fixture.tireA.getId()).doesNotContain(fixture.tireB.getId());
        assertDenied(() -> tireService.getTireById(fixture.tireB.getId()));

        assertThat(appointmentService.getAllAppointments()).extracting(Appointment::getId).contains(fixture.appointmentA.getId()).doesNotContain(fixture.appointmentB.getId());
        assertDenied(() -> appointmentService.getAppointmentById(fixture.appointmentB.getId()));

        assertThat(invoiceService.getAllInvoices()).extracting(Invoice::getId).contains(fixture.invoiceA.getId()).doesNotContain(fixture.invoiceB.getId());
        assertDenied(() -> invoiceService.getInvoiceById(fixture.invoiceB.getId()));

        assertThat(estimateService.getAllEstimates()).extracting(Estimate::getId).contains(fixture.estimateA.getId()).doesNotContain(fixture.estimateB.getId());
        assertDenied(() -> estimateService.getEstimateById(fixture.estimateB.getId()));

        assertThat(workOrderService.getAllWorkOrders()).extracting(WorkOrder::getId).contains(fixture.workOrderA.getId()).doesNotContain(fixture.workOrderB.getId());
        assertDenied(() -> workOrderService.getWorkOrderById(fixture.workOrderB.getId()));

        assertThat(attendanceService.getEmployeeAttendanceRange(fixture.employeeA.getId(), LocalDate.now(), LocalDate.now()))
                .extracting(EmployeeAttendance::getId)
                .contains(fixture.attendanceA.getId());
        assertDenied(() -> attendanceService.getEmployeeAttendanceRange(fixture.employeeB.getId(), LocalDate.now(), LocalDate.now()));

        assertThat(payrollService.getAllPeriods()).extracting(PayrollPeriod::getId).contains(fixture.payrollPeriodA.getId()).doesNotContain(fixture.payrollPeriodB.getId());
        assertDenied(() -> payrollService.getPeriodById(fixture.payrollPeriodB.getId()));
        assertDenied(() -> payrollService.getRecordById(fixture.payrollRecordB.getId()));

        assertThat(accountingService.getExpenses()).extracting(Expense::getId).contains(fixture.expenseA.getId()).doesNotContain(fixture.expenseB.getId());

        assertThat(notificationService.currentNotifications()).extracting(AppNotification::getId).contains(fixture.notificationA.getId()).doesNotContain(fixture.notificationB.getId());

        assertThat(customerPortalService.adminSummaries())
                .extracting(summary -> summary.getId())
                .contains(fixture.customerA.getId())
                .doesNotContain(fixture.customerB.getId());

        signIn(fixture.customerA);
        CustomerPortalResponse portal = customerPortalService.portal();
        assertThat(portal.getVehicles()).extracting(vehicle -> vehicle.getId()).contains(fixture.vehicleA.getId()).doesNotContain(fixture.vehicleB.getId());
        assertThat(portal.getInvoices()).extracting(invoice -> invoice.getId()).contains(fixture.invoiceA.getId()).doesNotContain(fixture.invoiceB.getId());
        assertThat(portal.getAppointments()).extracting(appointment -> appointment.getId()).contains(fixture.appointmentA.getId()).doesNotContain(fixture.appointmentB.getId());
        assertThatThrownBy(() -> customerPortalService.payInvoice(fixture.invoiceB.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invoice not found");
        assertThatThrownBy(() -> customerPortalService.deleteVehicle(fixture.vehicleB.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    void superAdminCanSeePlatformWideOperationalData() {
        TenantFixture fixture = tenantFixture();
        signIn(fixture.superAdmin);

        assertThat(tireService.getAllTires()).extracting(Tire::getId).contains(fixture.tireA.getId(), fixture.tireB.getId());
        assertThat(invoiceService.getAllInvoices()).extracting(Invoice::getId).contains(fixture.invoiceA.getId(), fixture.invoiceB.getId());
        assertThat(payrollService.getAllPeriods()).extracting(PayrollPeriod::getId).contains(fixture.payrollPeriodA.getId(), fixture.payrollPeriodB.getId());
        assertThat(accountingService.getExpenses()).extracting(Expense::getId).contains(fixture.expenseA.getId(), fixture.expenseB.getId());
    }

    @Test
    void httpWrongShopRequestsReturnForbiddenAndCustomerPortalHidesResources() throws Exception {
        TenantFixture fixture = tenantFixture();
        String adminToken = bearer(fixture.adminA);

        mockMvc.perform(get("/api/tires/{id}", fixture.tireB.getId()).header("Authorization", adminToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/appointments/{id}", fixture.appointmentB.getId()).header("Authorization", adminToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/invoices/{id}", fixture.invoiceB.getId()).header("Authorization", adminToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/estimates/{id}", fixture.estimateB.getId()).header("Authorization", adminToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/work-orders/{id}", fixture.workOrderB.getId()).header("Authorization", adminToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/payroll/periods/{id}", fixture.payrollPeriodB.getId()).header("Authorization", adminToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/payroll/records/{id}/approve", fixture.payrollRecordB.getId()).header("Authorization", adminToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/attendance/employee/{employeeId}/range", fixture.employeeB.getId())
                        .param("start", LocalDate.now().toString())
                        .param("end", LocalDate.now().toString())
                        .header("Authorization", adminToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/accounting/expenses/{id}/pay", fixture.expenseB.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/settings")
                        .param("shopId", fixture.shopB.getId().toString())
                        .header("Authorization", adminToken))
                .andExpect(status().isForbidden());
        String customerJson = mockMvc.perform(get("/api/customers").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(customerJson).contains(fixture.customerA.getEmail()).doesNotContain(fixture.customerB.getEmail());
        mockMvc.perform(post("/api/customers/{id}/notices", fixture.customerB.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Notice\",\"message\":\"Wrong shop\"}"))
                .andExpect(status().isForbidden());

        String notificationJson = mockMvc.perform(get("/api/notifications").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(notificationJson).contains("Notice A").doesNotContain("Notice B");
        mockMvc.perform(put("/api/notifications/{id}/read", fixture.notificationB.getId()).header("Authorization", adminToken))
                .andExpect(status().isNotFound());

        String customerToken = bearer(fixture.customerA);
        mockMvc.perform(delete("/api/customer/vehicles/{id}", fixture.vehicleB.getId()).header("Authorization", customerToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/customer/invoices/{id}/pay", fixture.invoiceB.getId()).header("Authorization", customerToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/tires/{id}", fixture.tireB.getId()).header("Authorization", bearer(fixture.superAdmin)))
                .andExpect(status().isOk());
    }

    @Test
    void shopAdminLocationFiltersAndInvoiceDeductionStayWithinSelectedLocation() throws Exception {
        Shop shopA = shopRepository.saveAndFlush(shop("Location Shop A"));
        Shop shopB = shopRepository.saveAndFlush(shop("Location Shop B"));
        ShopLocation storeA = shopLocationRepository.saveAndFlush(shopLocation(shopA, "Mississauga Store", ShopLocationType.STORE, true));
        ShopLocation warehouseA = shopLocationRepository.saveAndFlush(shopLocation(shopA, "Brampton Warehouse", ShopLocationType.WAREHOUSE, false));
        ShopLocation storeB = shopLocationRepository.saveAndFlush(shopLocation(shopB, "Other Shop Store", ShopLocationType.STORE, true));
        User adminA = userRepository.saveAndFlush(user("Location Admin A", "loc-admin-a-" + System.nanoTime() + "@test.com", UserRole.ADMIN, shopA));

        Tire tireStore = tire("Store Tire", shopA);
        tireStore.setQuantity(5);
        tireStore.setShopLocation(storeA);
        tireStore = tireRepository.saveAndFlush(tireStore);

        Tire tireWarehouse = tire("Warehouse Tire", shopA);
        tireWarehouse.setQuantity(7);
        tireWarehouse.setShopLocation(warehouseA);
        tireWarehouse = tireRepository.saveAndFlush(tireWarehouse);

        Tire tireOtherShop = tire("Other Shop Tire", shopB);
        tireOtherShop.setShopLocation(storeB);
        tireOtherShop = tireRepository.saveAndFlush(tireOtherShop);

        User customerA = userRepository.saveAndFlush(user("Location Customer", "loc-customer-" + System.nanoTime() + "@test.com", UserRole.CUSTOMER, shopA));
        Appointment storeAppointment = appointment("Location Appointment", customerA, shopA);
        LocalDate appointmentDate = LocalDate.now().plusDays(10);
        storeAppointment.setAppointmentDate(appointmentDate.atTime(9, 0));
        storeAppointment.setShopLocation(storeA);
        appointmentRepository.saveAndFlush(storeAppointment);

        signIn(adminA);

        assertThat(tireService.getAllTires()).extracting(Tire::getId)
                .contains(tireStore.getId(), tireWarehouse.getId())
                .doesNotContain(tireOtherShop.getId());

        String storeInventoryJson = mockMvc.perform(get("/api/tires")
                        .param("locationId", storeA.getId().toString())
                        .header("Authorization", bearer(adminA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(storeInventoryJson).contains("Store Tire").doesNotContain("Warehouse Tire").doesNotContain("Other Shop Tire");

        signIn(adminA);
        DashboardSummary allLocations = dashboardService.getDashboardSummary();
        DashboardSummary storeDashboard = dashboardService.getDashboardSummary(storeA.getId());
        assertThat(allLocations.getTotalTiresInStock()).isEqualTo(12);
        assertThat(storeDashboard.getTotalTiresInStock()).isEqualTo(5);
        assertThat(allLocations.getLocationBreakdowns())
                .filteredOn(breakdown -> storeA.getId().equals(breakdown.getLocationId()))
                .first()
                .extracting(breakdown -> breakdown.getInventoryQuantity())
                .isEqualTo(5);

        assertThat(appointmentService.getAvailableSlots(appointmentDate, storeA.getId())).doesNotContain("09:00");
        assertThat(appointmentService.getAvailableSlots(appointmentDate, warehouseA.getId())).contains("09:00");

        Invoice wrongLocationInvoice = invoice("Wrong Location Invoice", customerA, shopA);
        wrongLocationInvoice.setShopLocation(storeA);
        InvoiceItem wrongLocationItem = invoiceItem(InvoiceItemType.TIRE, "Warehouse Tire", 1, "100.00");
        wrongLocationItem.setTireId(tireWarehouse.getId());
        wrongLocationInvoice.addItem(wrongLocationItem);
        assertThatThrownBy(() -> invoiceService.saveInvoice(wrongLocationInvoice))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("another location");

        Invoice storeInvoice = invoice("Store Location Invoice", customerA, shopA);
        storeInvoice.setShopLocation(storeA);
        InvoiceItem storeItem = invoiceItem(InvoiceItemType.TIRE, "Store Tire", 2, "100.00");
        storeItem.setTireId(tireStore.getId());
        storeInvoice.addItem(storeItem);
        invoiceService.saveInvoice(storeInvoice);

        assertThat(tireRepository.findById(tireStore.getId()).orElseThrow().getQuantity()).isEqualTo(3);
        assertThat(tireRepository.findById(tireWarehouse.getId()).orElseThrow().getQuantity()).isEqualTo(7);

        String publicLocationsJson = mockMvc.perform(get("/api/public/shops/{shopId}/locations", shopA.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(publicLocationsJson).contains("Mississauga Store").doesNotContain("Brampton Warehouse");
    }

    @Test
    void ownerSeesAllShopLocationsWhileLocationAdminIsScopedToOneLocation() throws Exception {
        Shop shop = shopRepository.saveAndFlush(shop("Owner Scope Shop"));
        ShopLocation store = shopLocationRepository.saveAndFlush(shopLocation(shop, "Owner Store", ShopLocationType.STORE, true));
        ShopLocation warehouse = shopLocationRepository.saveAndFlush(shopLocation(shop, "Owner Warehouse", ShopLocationType.WAREHOUSE, false));

        User owner = userRepository.saveAndFlush(user("Shop Owner", "shop-owner-" + System.nanoTime() + "@test.com", UserRole.OWNER, shop));
        User locationAdmin = user("Store Admin", "store-admin-" + System.nanoTime() + "@test.com", UserRole.ADMIN, shop);
        locationAdmin.setShopLocation(store);
        locationAdmin = userRepository.saveAndFlush(locationAdmin);

        Tire storeTire = tire("Owner Store Tire", shop);
        storeTire.setQuantity(5);
        storeTire.setShopLocation(store);
        storeTire = tireRepository.saveAndFlush(storeTire);

        Tire warehouseTire = tire("Owner Warehouse Tire", shop);
        warehouseTire.setQuantity(7);
        warehouseTire.setShopLocation(warehouse);
        warehouseTire = tireRepository.saveAndFlush(warehouseTire);

        signIn(owner);
        assertThat(tireService.getAllTires()).extracting(Tire::getId)
                .contains(storeTire.getId(), warehouseTire.getId());
        assertThat(dashboardService.getDashboardSummary().getTotalTiresInStock()).isEqualTo(12);
        assertThat(dashboardService.getDashboardSummary(warehouse.getId()).getTotalTiresInStock()).isEqualTo(7);

        String ownerLocationsJson = mockMvc.perform(get("/api/shop-locations/shop/{shopId}/active", shop.getId())
                        .header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(ownerLocationsJson).contains("Owner Store", "Owner Warehouse");

        signIn(locationAdmin);
        assertThat(tireService.getAllTires()).extracting(Tire::getId)
                .contains(storeTire.getId())
                .doesNotContain(warehouseTire.getId());
        assertThat(dashboardService.getDashboardSummary().getTotalTiresInStock()).isEqualTo(5);
        assertDenied(() -> dashboardService.getDashboardSummary(warehouse.getId()));

        String adminLocationsJson = mockMvc.perform(get("/api/shop-locations/shop/{shopId}/active", shop.getId())
                        .header("Authorization", bearer(locationAdmin)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(adminLocationsJson).contains("Owner Store").doesNotContain("Owner Warehouse");

        mockMvc.perform(get("/api/tires")
                        .param("locationId", warehouse.getId().toString())
                        .header("Authorization", bearer(locationAdmin)))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerRegistrationRejectsMismatchedPasswords() throws Exception {
        String email = "signup-password-mismatch-" + System.nanoTime() + "@test.com";
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Password Mismatch",
                                  "email": "%s",
                                  "phone": "437%s",
                                  "password": "ValidPass1!",
                                  "confirmPassword": "DifferentPass1!"
                                }
                                """.formatted(email, Math.abs(email.hashCode()))))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("Passwords do not match");
    }

    @Test
    void customerRegistrationRequiresAndPersistsPublicShopLocationWhenConfigured() throws Exception {
        Shop shop = shopRepository.saveAndFlush(shop("Public Signup Shop"));
        ShopLocation store = shopLocationRepository.saveAndFlush(shopLocation(shop, "Public Signup Store", ShopLocationType.STORE, true));
        shopLocationRepository.saveAndFlush(shopLocation(shop, "Public Signup Warehouse", ShopLocationType.WAREHOUSE, false));

        String missingSelectionEmail = "signup-missing-" + System.nanoTime() + "@test.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Missing Selection",
                                  "email": "%s",
                                  "phone": "416%s",
                                  "password": "ValidPass1!",
                                  "confirmPassword": "ValidPass1!"
                                }
                                """.formatted(missingSelectionEmail, Math.abs(missingSelectionEmail.hashCode()))))
                .andExpect(status().isBadRequest());

        String email = "signup-linked-" + System.nanoTime() + "@test.com";
        String registerJson = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Linked Customer",
                                  "email": "%s",
                                  "phone": "647%s",
                                  "password": "ValidPass1!",
                                  "confirmPassword": "ValidPass1!",
                                  "shopId": %d,
                                  "locationId": %d
                                }
                                """.formatted(email, Math.abs(email.hashCode()), shop.getId(), store.getId())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(registerJson).contains("\"shopId\":" + shop.getId(), "\"locationId\":" + store.getId());

        User customer = userRepository.findByEmail(email).orElseThrow();
        assertThat(customer.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(customer.getShop().getId()).isEqualTo(shop.getId());
        assertThat(customer.getShopLocation().getId()).isEqualTo(store.getId());
    }

    @Test
    void partialPaymentFullPaymentAndAccountingEntriesUseActualCashReceived() {
        TenantFixture fixture = tenantFixture();
        signIn(fixture.adminA);

        Invoice invoice = invoice("Payment Customer", fixture.customerA, fixture.shopA);
        invoice.addItem(invoiceItem(InvoiceItemType.SERVICE, "Service", 1, "1000.00"));
        Invoice savedInvoice = invoiceService.saveInvoice(invoice);

        InvoiceStatusUpdateRequest partial = new InvoiceStatusUpdateRequest();
        partial.setStatus("PARTIALLY_PAID");
        partial.setAmountPaid(new BigDecimal("300.00"));
        partial.setDueDate(LocalDate.now().plusDays(10));
        partial.setPaymentMethod("Cash");

        Invoice partiallyPaid = invoiceService.updateInvoiceStatus(savedInvoice.getId(), partial);
        assertThat(partiallyPaid.getStatus()).isEqualTo("PARTIALLY_PAID");
        assertThat(partiallyPaid.getAmountPaid()).isEqualByComparingTo("300.00");
        assertThat(partiallyPaid.getBalanceDue()).isEqualByComparingTo("700.00");
        assertThat(partiallyPaid.getDueDate()).isEqualTo(LocalDate.now().plusDays(10));
        assertThat(paymentEntryCount(partiallyPaid)).isEqualTo(1);

        InvoiceStatusUpdateRequest paid = new InvoiceStatusUpdateRequest();
        paid.setStatus("PAID");
        paid.setPaymentMethod("Cash");

        Invoice fullyPaid = invoiceService.updateInvoiceStatus(savedInvoice.getId(), paid);
        assertThat(fullyPaid.getStatus()).isEqualTo("PAID");
        assertThat(fullyPaid.getAmountPaid()).isEqualByComparingTo("1000.00");
        assertThat(fullyPaid.getBalanceDue()).isEqualByComparingTo("0.00");
        assertThat(paymentEntryCount(fullyPaid)).isEqualTo(2);

        invoiceService.updateInvoiceStatus(savedInvoice.getId(), paid);
        assertThat(paymentEntryCount(fullyPaid)).isEqualTo(2);
        assertThat(accountingService.findDuplicateInvoicePaymentEntries()).isEmpty();
    }

    @Test
    void payrollRequiresApprovalAndSyncsAccountingOnlyOnce() {
        TenantFixture fixture = tenantFixture();
        signIn(fixture.adminA);

        PayrollPeriod period = payrollPeriod(fixture.shopA, LocalDate.now(), LocalDate.now());
        payrollPeriodRepository.saveAndFlush(period);

        PayrollGenerationResponse generated = payrollService.generatePayrollForPeriod(period.getId());
        assertThat(generated.getRecords()).hasSize(1);
        Long recordId = generated.getRecords().get(0).getId();

        assertThatThrownBy(() -> payrollService.payRecord(recordId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only approved payroll records can be paid");

        PayrollRecord approved = payrollService.approveRecord(recordId);
        assertThat(approved.getStatus()).isEqualTo(PayrollStatus.APPROVED);

        PayrollRecord paid = payrollService.payRecord(recordId);
        assertThat(paid.getStatus()).isEqualTo(PayrollStatus.PAID);
        assertThat(paid.isAccountingSynced()).isTrue();
        assertThat(paid.getAccountingEntryId()).isNotNull();
        assertThat(expenseRepository.findById(paid.getAccountingEntryId())).isPresent();
        assertThat(payrollExpenseCount(fixture.shopA)).isEqualTo(1);

        assertThatThrownBy(() -> payrollService.payRecord(recordId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payroll record is already paid");
        assertThat(payrollExpenseCount(fixture.shopA)).isEqualTo(1);
    }

    @Test
    void companySettingsAreIsolatedPerShopAndInspectableBySuperAdmin() {
        TenantFixture fixture = tenantFixture();

        signIn(fixture.adminA);
        CompanySettings shopASettings = companySettingsService.saveSettings(settingsRequest("Shop A Public Name"));
        assertThat(shopASettings.getShop().getId()).isEqualTo(fixture.shopA.getId());

        signIn(fixture.adminB);
        CompanySettings shopBSettings = companySettingsService.getSettings();
        assertThat(shopBSettings.getShop().getId()).isEqualTo(fixture.shopB.getId());
        assertThat(shopBSettings.getShopName()).isNotEqualTo("Shop A Public Name");

        signIn(fixture.superAdmin);
        assertThat(companySettingsService.getSettings(fixture.shopA.getId()).getShopName()).isEqualTo("Shop A Public Name");
        assertThat(companySettingsService.getSettings(fixture.shopB.getId()).getShopName()).isEqualTo(shopBSettings.getShopName());
    }

    @Test
    void companySettingsSaveUpdatesExistingRowAndAllowsUploadedLogoData() {
        Shop shop = shopRepository.saveAndFlush(shop("Settings Save Shop"));
        User admin = userRepository.saveAndFlush(user("Settings Admin", "settings-admin-" + System.nanoTime() + "@test.com", UserRole.ADMIN, shop));

        CompanySettings first = new CompanySettings();
        first.setShop(shop);
        first.setShopName("Old Name");
        first.setTaxRate(new BigDecimal("13.00"));
        first.setInvoiceTerms("Old terms");
        first = companySettingsRepository.saveAndFlush(first);

        CompanySettings duplicate = new CompanySettings();
        duplicate.setShop(shop);
        duplicate.setShopName("Duplicate Name");
        duplicate.setTaxRate(new BigDecimal("13.00"));
        duplicate.setInvoiceTerms("Duplicate terms");
        duplicate = companySettingsRepository.saveAndFlush(duplicate);

        signIn(admin);

        CompanySettingsRequest request = settingsRequest("Updated Shop Name");
        request.setLogoUrl("data:image/png;base64," + "A".repeat(90_000));
        CompanySettings saved = companySettingsService.saveSettings(request);

        assertThat(saved.getId()).isEqualTo(first.getId());
        assertThat(saved.getShopName()).isEqualTo("Updated Shop Name");
        assertThat(saved.getLogoUrl()).startsWith("data:image/png;base64,");
        assertThat(companySettingsRepository.findById(duplicate.getId()).orElseThrow().getShopName()).isEqualTo("Duplicate Name");

        CompanySettingsRequest secondRequest = settingsRequest("Updated Again");
        secondRequest.setLogoUrl("data:image/png;base64," + "B".repeat(90_000));
        CompanySettings secondSave = companySettingsService.saveSettings(secondRequest);

        assertThat(secondSave.getId()).isEqualTo(first.getId());
        assertThat(secondSave.getShopName()).isEqualTo("Updated Again");
        assertThat(secondSave.getLogoUrl()).contains("BBBB");

        Invoice invoice = invoiceRepository.saveAndFlush(invoice("Settings Invoice", admin, shop));
        String pdfText = new String(pdfService.invoicePdf(invoice.getId()));
        assertThat(pdfText).contains("Updated Again");
    }

    @Test
    void customerPortalCanBookWithAdminCreatedVehicleAfterShopLinking() {
        Shop shop = shopRepository.saveAndFlush(shop("Portal Vehicle Shop"));
        ShopLocation store = shopLocationRepository.saveAndFlush(shopLocation(shop, "Portal Store", ShopLocationType.STORE, true));
        User customer = userRepository.saveAndFlush(user("Portal Customer", "portal-customer-" + System.nanoTime() + "@test.com", UserRole.CUSTOMER, shop));
        customer.setShopLocation(store);
        customer = userRepository.saveAndFlush(customer);

        CustomerVehicle adminCreatedVehicle = vehicle(customer, "Admin Created Vehicle");
        adminCreatedVehicle.setShop(null);
        adminCreatedVehicle.setShopLocation(null);
        adminCreatedVehicle = vehicleRepository.saveAndFlush(adminCreatedVehicle);

        signIn(customer);

        CustomerPortalResponse portal = customerPortalService.portal();
        assertThat(portal.getVehicles()).extracting(vehicle -> vehicle.getId()).contains(adminCreatedVehicle.getId());

        CustomerAppointmentRequest request = new CustomerAppointmentRequest();
        request.setVehicleId(adminCreatedVehicle.getId());
        request.setLocationId(store.getId());
        request.setAppointmentDate(LocalDate.now().plusDays(2));
        request.setAppointmentTime(java.time.LocalTime.of(10, 0));
        request.setServiceType(ServiceType.INSTALLATION);

        Appointment appointment = customerPortalService.bookAppointment(request);

        assertThat(appointment.getCustomerId()).isEqualTo(customer.getId());
        assertThat(appointment.getVehicle()).contains("Toyota", "Camry");
        assertThat(appointment.getShop().getId()).isEqualTo(shop.getId());
        assertThat(appointment.getShopLocation().getId()).isEqualTo(store.getId());
        CustomerVehicle claimedVehicle = vehicleRepository.findById(adminCreatedVehicle.getId()).orElseThrow();
        assertThat(claimedVehicle.getShop().getId()).isEqualTo(shop.getId());
    }

    @Test
    void customerPortalInstallationWithAvailableVehicleTireConfirmsAppointment() {
        Shop shop = shopRepository.saveAndFlush(shop("Portal Tire Stock Shop"));
        ShopLocation store = shopLocationRepository.saveAndFlush(shopLocation(shop, "Portal Tire Store", ShopLocationType.STORE, true));
        User customer = userRepository.saveAndFlush(user("Portal Tire Customer", "portal-tire-customer-" + System.nanoTime() + "@test.com", UserRole.CUSTOMER, shop));
        customer.setShopLocation(store);
        customer = userRepository.saveAndFlush(customer);

        CustomerVehicle vehicle = vehicle(customer, "Stock Vehicle");
        vehicle.setShop(shop);
        vehicle.setShopLocation(store);
        vehicle = vehicleRepository.saveAndFlush(vehicle);

        Tire tire = tire("Available Portal Tire", shop);
        tire.setWidth(225);
        tire.setAspectRatio(45);
        tire.setRimSize(18);
        tire.setQuantity(4);
        tire.setShopLocation(store);
        tireRepository.saveAndFlush(tire);

        signIn(customer);

        CustomerAppointmentRequest request = new CustomerAppointmentRequest();
        request.setVehicleId(vehicle.getId());
        request.setLocationId(store.getId());
        request.setAppointmentDate(LocalDate.now().plusDays(5));
        request.setAppointmentTime(java.time.LocalTime.of(11, 0));
        request.setServiceType(ServiceType.INSTALLATION);

        Appointment appointment = customerPortalService.bookAppointment(request);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
        assertThat(tireRequestRepository.findByCustomer_IdOrderByCreatedAtDesc(customer.getId())).isEmpty();
    }

    @Test
    void customerPortalOutOfStockInstallationCreatesPendingTireRequest() {
        Shop shop = shopRepository.saveAndFlush(shop("Portal Tire Request Shop"));
        ShopLocation store = shopLocationRepository.saveAndFlush(shopLocation(shop, "Portal Request Store", ShopLocationType.STORE, true));
        User customer = userRepository.saveAndFlush(user("Portal Request Customer", "portal-request-customer-" + System.nanoTime() + "@test.com", UserRole.CUSTOMER, shop));
        customer.setShopLocation(store);
        customer = userRepository.saveAndFlush(customer);

        CustomerVehicle vehicle = vehicle(customer, "Out Of Stock Vehicle");
        vehicle.setShop(shop);
        vehicle.setShopLocation(store);
        vehicle = vehicleRepository.saveAndFlush(vehicle);

        signIn(customer);

        CustomerAppointmentRequest request = new CustomerAppointmentRequest();
        request.setVehicleId(vehicle.getId());
        request.setLocationId(store.getId());
        request.setAppointmentDate(LocalDate.now().plusDays(6));
        request.setAppointmentTime(java.time.LocalTime.of(12, 0));
        request.setServiceType(ServiceType.INSTALLATION);
        request.setNotes("Customer needs the shop to source tires.");

        Appointment appointment = customerPortalService.bookAppointment(request);
        List<TireRequest> requests = tireRequestRepository.findByCustomer_IdOrderByCreatedAtDesc(customer.getId());
        CustomerPortalResponse portal = customerPortalService.portal();

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING_TIRE_AVAILABILITY);
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getRequestedSize()).isEqualTo("225/45/18");
        assertThat(requests.get(0).getAppointmentId()).isEqualTo(appointment.getId());
        assertThat(portal.getTireRequests()).extracting(tireRequest -> tireRequest.getId()).contains(requests.get(0).getId());
    }

    private TenantFixture tenantFixture() {
        Shop shopA = shopRepository.saveAndFlush(shop("Shop A"));
        Shop shopB = shopRepository.saveAndFlush(shop("Shop B"));

        User superAdmin = userRepository.saveAndFlush(user("Owner", "owner-" + System.nanoTime() + "@test.com", UserRole.SUPER_ADMIN, null));
        User adminA = userRepository.saveAndFlush(user("Admin A", "admin-a-" + System.nanoTime() + "@test.com", UserRole.ADMIN, shopA));
        User adminB = userRepository.saveAndFlush(user("Admin B", "admin-b-" + System.nanoTime() + "@test.com", UserRole.ADMIN, shopB));
        User employeeA = userRepository.saveAndFlush(employee("Employee A", "employee-a-" + System.nanoTime() + "@test.com", shopA));
        User employeeB = userRepository.saveAndFlush(employee("Employee B", "employee-b-" + System.nanoTime() + "@test.com", shopB));
        User customerA = userRepository.saveAndFlush(user("Customer A", "customer-a-" + System.nanoTime() + "@test.com", UserRole.CUSTOMER, shopA));
        User customerB = userRepository.saveAndFlush(user("Customer B", "customer-b-" + System.nanoTime() + "@test.com", UserRole.CUSTOMER, shopB));

        Tire tireA = tireRepository.saveAndFlush(tire("Brand A", shopA));
        Tire tireB = tireRepository.saveAndFlush(tire("Brand B", shopB));
        CustomerVehicle vehicleA = vehicleRepository.saveAndFlush(vehicle(customerA, "Vehicle A"));
        CustomerVehicle vehicleB = vehicleRepository.saveAndFlush(vehicle(customerB, "Vehicle B"));
        Appointment appointmentA = appointmentRepository.saveAndFlush(appointment("Appointment A", customerA, shopA));
        Appointment appointmentB = appointmentRepository.saveAndFlush(appointment("Appointment B", customerB, shopB));
        Invoice invoiceA = invoiceRepository.saveAndFlush(invoice("Invoice A", customerA, shopA));
        Invoice invoiceB = invoiceRepository.saveAndFlush(invoice("Invoice B", customerB, shopB));
        Estimate estimateA = estimateRepository.saveAndFlush(estimate("Estimate A", customerA, shopA));
        Estimate estimateB = estimateRepository.saveAndFlush(estimate("Estimate B", customerB, shopB));
        WorkOrder workOrderA = workOrderRepository.saveAndFlush(workOrder("Work A", customerA, employeeA, shopA));
        WorkOrder workOrderB = workOrderRepository.saveAndFlush(workOrder("Work B", customerB, employeeB, shopB));
        EmployeeAttendance attendanceA = attendanceRepository.saveAndFlush(attendance(employeeA, shopA, new BigDecimal("8.00")));
        EmployeeAttendance attendanceB = attendanceRepository.saveAndFlush(attendance(employeeB, shopB, new BigDecimal("8.00")));
        PayrollPeriod payrollPeriodA = payrollPeriodRepository.saveAndFlush(payrollPeriod(shopA, LocalDate.now().minusDays(7), LocalDate.now().minusDays(1)));
        PayrollPeriod payrollPeriodB = payrollPeriodRepository.saveAndFlush(payrollPeriod(shopB, LocalDate.now().minusDays(7), LocalDate.now().minusDays(1)));
        PayrollRecord payrollRecordA = payrollRecordRepository.saveAndFlush(payrollRecord(employeeA, payrollPeriodA));
        PayrollRecord payrollRecordB = payrollRecordRepository.saveAndFlush(payrollRecord(employeeB, payrollPeriodB));
        Expense expenseA = expenseRepository.saveAndFlush(expense("Expense A", shopA));
        Expense expenseB = expenseRepository.saveAndFlush(expense("Expense B", shopB));
        AppNotification notificationA = notificationRepository.saveAndFlush(notification("Notice A", UserRole.ADMIN, shopA));
        AppNotification notificationB = notificationRepository.saveAndFlush(notification("Notice B", UserRole.ADMIN, shopB));

        return new TenantFixture(shopA, shopB, superAdmin, adminA, adminB, employeeA, employeeB, customerA, customerB,
                tireA, tireB, vehicleA, vehicleB, appointmentA, appointmentB, invoiceA, invoiceB, estimateA, estimateB, workOrderA, workOrderB,
                attendanceA, attendanceB, payrollPeriodA, payrollPeriodB, payrollRecordA, payrollRecordB, expenseA, expenseB,
                notificationA, notificationB);
    }

    private void signIn(User user) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                "test",
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user.getEmail());
    }

    private void assertDenied(ThrowingRunnable runnable) {
        assertThatThrownBy(runnable::run)
                .isInstanceOfAny(AccessDeniedException.class, RuntimeException.class)
                .hasMessageContaining("permission");
    }

    private long paymentEntryCount(Invoice invoice) {
        return journalEntryRepository.countByReferenceTypeAndReferenceIdAndSourceStartingWith(
                "Invoice",
                invoice.getId(),
                "INVOICE_PAYMENT");
    }

    private long payrollExpenseCount(Shop shop) {
        return expenseRepository.findAll().stream()
                .filter(expense -> expense.getCategoryKey() == ExpenseCategory.PAYROLL)
                .filter(expense -> expense.getShop() != null && expense.getShop().getId().equals(shop.getId()))
                .count();
    }

    private Shop shop(String name) {
        Shop shop = new Shop();
        shop.setName(name);
        shop.setActive(true);
        return shop;
    }

    private ShopLocation shopLocation(Shop shop, String name, ShopLocationType type, boolean customerFacing) {
        ShopLocation location = new ShopLocation();
        location.setShop(shop);
        location.setName(name);
        location.setType(type);
        location.setAddress(name + " address");
        location.setCity("Toronto");
        location.setProvince("ON");
        location.setPostalCode("M1M 1M1");
        location.setCustomerFacing(customerFacing);
        location.setActive(true);
        return location;
    }

    private User user(String name, String email, UserRole role, Shop shop) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPhone("555-" + Math.abs(email.hashCode()));
        user.setPasswordHash("$2a$10$productionreadinesstesthashonly000000000000000000000");
        user.setRole(role);
        user.setActive(true);
        user.setShop(shop);
        return user;
    }

    private User employee(String name, String email, Shop shop) {
        User user = user(name, email, UserRole.EMPLOYEE, shop);
        user.setPayrollEnabled(true);
        user.setHourlyRate(new BigDecimal("25.00"));
        return user;
    }

    private Tire tire(String brand, Shop shop) {
        Tire tire = new Tire();
        tire.setBrand(brand);
        tire.setModel("Pilot");
        tire.setWidth(245);
        tire.setAspectRatio(40);
        tire.setRimSize(18);
        tire.setCondition(Condition.NEW);
        tire.setSeason("All season");
        tire.setQuantity(4);
        tire.setPrice(new BigDecimal("100.00"));
        tire.setLocation("Rack");
        tire.setShop(shop);
        return tire;
    }

    private Appointment appointment(String name, User customer, Shop shop) {
        Appointment appointment = new Appointment();
        appointment.setCustomerId(customer.getId());
        appointment.setCustomerName(name);
        appointment.setPhone(customer.getPhone());
        appointment.setEmail(customer.getEmail());
        appointment.setVehicle("Test vehicle");
        appointment.setAppointmentDate(LocalDateTime.now().plusDays(3));
        appointment.setServiceType(ServiceType.INSTALLATION);
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setShop(shop);
        return appointment;
    }

    private CustomerVehicle vehicle(User customer, String nickname) {
        CustomerVehicle vehicle = new CustomerVehicle();
        vehicle.setCustomer(customer);
        vehicle.setNickname(nickname);
        vehicle.setYear("2024");
        vehicle.setMake("Toyota");
        vehicle.setModel("Camry");
        vehicle.setTireSize("225/45R18");
        return vehicle;
    }

    private Invoice invoice(String name, User customer, Shop shop) {
        Invoice invoice = new Invoice();
        invoice.setCustomerId(customer.getId());
        invoice.setCustomerName(name);
        invoice.setPhone(customer.getPhone());
        invoice.setVehicle("Test vehicle");
        invoice.setTaxRate(BigDecimal.ZERO);
        invoice.setStatus("UNPAID");
        invoice.setPaymentMethod("Cash");
        invoice.setShop(shop);
        return invoice;
    }

    private InvoiceItem invoiceItem(InvoiceItemType type, String name, int quantity, String unitPrice) {
        InvoiceItem item = new InvoiceItem();
        item.setItemType(type);
        item.setItemName(name);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        item.setTotalPrice(new BigDecimal(unitPrice).multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    private Estimate estimate(String name, User customer, Shop shop) {
        Estimate estimate = new Estimate();
        estimate.setCustomer(customer);
        estimate.setCustomerName(name);
        estimate.setPhone(customer.getPhone());
        estimate.setEmail(customer.getEmail());
        estimate.setVehicle("Test vehicle");
        estimate.setEstimateNumber("EST-" + Math.abs(name.hashCode()) + "-" + System.nanoTime());
        estimate.setStatus(EstimateStatus.DRAFT);
        estimate.setShop(shop);
        return estimate;
    }

    private WorkOrder workOrder(String name, User customer, User employee, Shop shop) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setCustomer(customer);
        workOrder.setAssignedEmployee(employee);
        workOrder.setCustomerName(name);
        workOrder.setPhone(customer.getPhone());
        workOrder.setEmail(customer.getEmail());
        workOrder.setVehicle("Test vehicle");
        workOrder.setServiceType(ServiceType.INSTALLATION);
        workOrder.setStatus(WorkOrderStatus.PENDING);
        workOrder.setShop(shop);
        return workOrder;
    }

    private EmployeeAttendance attendance(User employee, Shop shop, BigDecimal workedHours) {
        EmployeeAttendance attendance = new EmployeeAttendance();
        attendance.setEmployee(employee);
        attendance.setShop(shop);
        attendance.setWorkDate(LocalDate.now());
        attendance.setClockIn(LocalDateTime.now().minusHours(8));
        attendance.setClockOut(LocalDateTime.now());
        attendance.setWorkedHours(workedHours);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setAbsenceDecision(AbsenceDecision.UNRESOLVED);
        return attendance;
    }

    private PayrollPeriod payrollPeriod(Shop shop, LocalDate start, LocalDate end) {
        PayrollPeriod period = new PayrollPeriod();
        period.setShop(shop);
        period.setStartDate(start);
        period.setEndDate(end);
        period.setStatus(PayrollStatus.PENDING);
        return period;
    }

    private PayrollRecord payrollRecord(User employee, PayrollPeriod period) {
        PayrollRecord record = new PayrollRecord();
        record.setEmployee(employee);
        record.setPayrollPeriod(period);
        record.setRegularHours(new BigDecimal("8.00"));
        record.setOvertimeHours(BigDecimal.ZERO);
        record.setHourlyRate(new BigDecimal("25.00"));
        record.setGrossPay(new BigDecimal("200.00"));
        record.setNetPay(new BigDecimal("200.00"));
        record.setStatus(PayrollStatus.PENDING);
        return record;
    }

    private Expense expense(String vendor, Shop shop) {
        Expense expense = new Expense();
        expense.setVendor(vendor);
        expense.setSubtotal(new BigDecimal("10.00"));
        expense.setTaxAmount(BigDecimal.ZERO);
        expense.setTotal(new BigDecimal("10.00"));
        expense.setStatus("UNPAID");
        expense.setShop(shop);
        return expense;
    }

    private AppNotification notification(String title, UserRole role, Shop shop) {
        AppNotification notification = new AppNotification();
        notification.setRecipientRole(role);
        notification.setTitle(title);
        notification.setMessage(title);
        notification.setShop(shop);
        return notification;
    }

    private CompanySettingsRequest settingsRequest(String shopName) {
        CompanySettingsRequest request = new CompanySettingsRequest();
        request.setShopName(shopName);
        request.setTaxRate(new BigDecimal("13.00"));
        request.setInvoiceTerms("Due on receipt");
        return request;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record TenantFixture(
            Shop shopA,
            Shop shopB,
            User superAdmin,
            User adminA,
            User adminB,
            User employeeA,
            User employeeB,
            User customerA,
            User customerB,
            Tire tireA,
            Tire tireB,
            CustomerVehicle vehicleA,
            CustomerVehicle vehicleB,
            Appointment appointmentA,
            Appointment appointmentB,
            Invoice invoiceA,
            Invoice invoiceB,
            Estimate estimateA,
            Estimate estimateB,
            WorkOrder workOrderA,
            WorkOrder workOrderB,
            EmployeeAttendance attendanceA,
            EmployeeAttendance attendanceB,
            PayrollPeriod payrollPeriodA,
            PayrollPeriod payrollPeriodB,
            PayrollRecord payrollRecordA,
            PayrollRecord payrollRecordB,
            Expense expenseA,
            Expense expenseB,
            AppNotification notificationA,
            AppNotification notificationB) {}
}
