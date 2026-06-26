const jsonHeaders = {
  "Content-Type": "application/json"
};
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

function apiUrl(path) {
  if (/^https?:\/\//i.test(path)) {
    return path;
  }

  return `${apiBaseUrl}${path.startsWith("/") ? path : `/${path}`}`;
}

function getAuthToken() {
  return localStorage.getItem("tiretrack-token");
}

function getRefreshToken() {
  return localStorage.getItem("tiretrack-refresh-token");
}

function setTokens(token, refreshToken) {
  localStorage.setItem("tiretrack-token", token);
  localStorage.setItem("tiretrack-refresh-token", refreshToken);
}

function clearTokens() {
  localStorage.removeItem("tiretrack-token");
  localStorage.removeItem("tiretrack-refresh-token");
  localStorage.removeItem("tiretrack-auth");
}

function persistAuth(authData) {
  localStorage.setItem("tiretrack-auth", JSON.stringify(authData));
  setTokens(authData.token, authData.refreshToken);
  window.dispatchEvent(new CustomEvent("tiretrack-auth-updated", { detail: authData }));
}

function clearAuthSession() {
  clearTokens();
  window.dispatchEvent(new Event("tiretrack-auth-cleared"));
}

function safeErrorDetail(value) {
  if (value == null) {
    return "";
  }

  if (typeof value === "string") {
    return value;
  }

  if (Array.isArray(value)) {
    return value.map(safeErrorDetail).filter(Boolean).join(", ");
  }

  if (typeof value === "object") {
    return Object.values(value).map(safeErrorDetail).filter(Boolean).join(", ");
  }

  return String(value);
}

function cleanApiErrorMessage(status, bodyText) {
  let parsedMessage = null;

  try {
    parsedMessage = bodyText ? JSON.parse(bodyText) : null;
  } catch {
    parsedMessage = null;
  }

  const details = parsedMessage?.message
    || safeErrorDetail(parsedMessage?.validationErrors)
    || safeErrorDetail(parsedMessage)
    || bodyText
    || "";
  const cleanDetails = String(details)
    .replace(/^\/api\/[^\s:]+:\s*/i, "")
    .replace(/\s+/g, " ")
    .trim();
  const lowerDetails = cleanDetails.toLowerCase();

  if (lowerDetails.includes("shop account is currently inactive")) {
    return "Your shop account is currently inactive. Please contact support.";
  }

  if (status === 401) {
    return cleanDetails && !/exception|stack trace|\/api\//i.test(cleanDetails)
      ? cleanDetails
      : "Your session expired. Please log in again.";
  }

  if (status === 403) {
    return "You do not have permission to perform this action.";
  }

  if (status === 404) {
    return cleanDetails || "We could not find that record.";
  }

  if (status === 409) {
    return cleanDetails || "A record with this value already exists.";
  }

  if (status >= 500) {
    return "Something went wrong. Please try again or contact support.";
  }

  return cleanDetails || `Request failed with status ${status}.`;
}

async function refreshAuthToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    return false;
  }

  try {
    const data = await request("/api/auth/refresh", {
      method: "POST",
      headers: jsonHeaders,
      body: JSON.stringify({ refreshToken })
    }, true);

    persistAuth(data);
    return true;
  } catch {
    clearAuthSession();
    return false;
  }
}

async function request(path, options = {}, skipAuth = false) {
  let response;

  const token = getAuthToken();
  const isFormData = options.body instanceof FormData;
  const headers = {
    ...options.headers,
    ...(isFormData ? {} : jsonHeaders)
  };

  if (!skipAuth && token) {
    headers.Authorization = `Bearer ${token}`;
  }

  try {
    response = await fetch(apiUrl(path), {
      ...options,
      headers
    });
  } catch {
    throw new Error("Could not reach the backend. Make sure TireTrack is running and try again.");
  }

  if (response.status === 401 && !skipAuth) {
    const refreshed = await refreshAuthToken();
    if (refreshed) {
      const retryToken = getAuthToken();
      response = await fetch(apiUrl(path), {
        ...options,
        headers: {
          ...headers,
          Authorization: `Bearer ${retryToken}`
        }
      });
    } else {
      clearAuthSession();
    }
  }

  if (!response.ok) {
    const message = await response.text();
    throw new Error(cleanApiErrorMessage(response.status, message));
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

export function login(credentials) {
  return request("/api/auth/login", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(credentials)
  }, true);
}

export function register(account) {
  return request("/api/auth/register", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(account)
  }, true);
}

export function refreshToken(requestBody) {
  return request("/api/auth/refresh", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(requestBody)
  }, true);
}

export function getCurrentUser() {
  return request("/api/auth/me");
}

export function logout() {
  clearAuthSession();
}

export function getAvailableSlots(date, locationId) {
  const params = new URLSearchParams({ date });
  if (locationId) params.set("locationId", locationId);
  return request(`/api/public/available-slots?${params.toString()}`, {}, true);
}

export function createPublicBooking(booking) {
  return request("/api/public/bookings", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(booking)
  }, true);
}

export function getPublicShops() {
  return request("/api/public/shops", {}, true);
}

export function getPublicShopLocations(shopId) {
  return request(`/api/public/shops/${encodeURIComponent(shopId)}/locations`, {}, true);
}

export function getDashboard(locationId) {
  const query = locationId ? `?locationId=${encodeURIComponent(locationId)}` : "";
  return request(`/api/dashboard${query}`);
}

export function getSettings() {
  return request("/api/settings");
}

export function getAuditLogs() {
  return request("/api/audit-logs");
}

export function getNotifications() {
  return request("/api/notifications");
}

export function createNotification(notification) {
  return request("/api/notifications", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(notification)
  });
}

export function markNotificationRead(id) {
  return request(`/api/notifications/${id}/read`, {
    method: "PUT"
  });
}

export function markAllNotificationsRead() {
  return request("/api/notifications/read-all", {
    method: "PUT"
  });
}

export function updateSettings(settings) {
  return request("/api/settings", {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(settings)
  });
}

export function getSalesData(days = 14, locationId) {
  const params = new URLSearchParams({ days });
  if (locationId) params.set("locationId", locationId);
  return request(`/api/dashboard/sales?${params.toString()}`);
}

export function getTires(locationId) {
  const query = locationId ? `?locationId=${encodeURIComponent(locationId)}` : "";
  return request(`/api/tires${query}`);
}

export function searchTiresByBrand(brand) {
  return request(`/api/tires/search/brand?brand=${encodeURIComponent(brand)}`);
}

export function searchTiresBySize({ width, aspectRatio, rimSize }) {
  const params = new URLSearchParams({ width, aspectRatio, rimSize });
  return request(`/api/tires/search/size?${params.toString()}`);
}

export function searchTiresByCondition(condition) {
  return request(`/api/tires/search/condition?condition=${encodeURIComponent(condition)}`);
}

export function searchTiresBySeason(season) {
  return request(`/api/tires/search/season?season=${encodeURIComponent(season)}`);
}

export function searchTiresByLocation(location) {
  return request(`/api/tires/search/location?location=${encodeURIComponent(location)}`);
}

export function getLowStockTires(threshold) {
  return request(`/api/tires/low-stock?threshold=${encodeURIComponent(threshold)}`);
}

export function createTire(tire) {
  return request("/api/tires", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(tire)
  });
}

export function importTiresCsv(file) {
  const formData = new FormData();
  formData.append("file", file);

  return request("/api/tires/import", {
    method: "POST",
    body: formData
  });
}

export function deleteTire(id) {
  return request(`/api/tires/${id}`, {
    method: "DELETE"
  });
}

export function updateTire(id, tire) {
  return request(`/api/tires/${id}`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(tire)
  });
}

export function getAppointments(locationId) {
  const query = locationId ? `?locationId=${encodeURIComponent(locationId)}` : "";
  return request(`/api/appointments${query}`);
}

export function createAppointment(appointment) {
  return request("/api/appointments", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(appointment)
  });
}

export function updateAppointment(id, appointment) {
  return request(`/api/appointments/${id}`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(appointment)
  });
}

export function deleteAppointment(id) {
  return request(`/api/appointments/${id}`, {
    method: "DELETE"
  });
}

export function getAppointmentTireAvailability(vehicleId, locationId, serviceType = "INSTALLATION") {
  const params = new URLSearchParams({ vehicleId, serviceType });
  if (locationId) params.set("locationId", locationId);
  return request(`/api/appointments/tire-availability?${params.toString()}`);
}

export function getTireRequests() {
  return request("/api/tire-requests");
}

export function createTireRequest(tireRequest) {
  return request("/api/tire-requests", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(tireRequest)
  });
}

export function updateTireRequestStatus(id, status, adminResponse = "") {
  return request(`/api/tire-requests/${encodeURIComponent(id)}/status`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify({ status, adminResponse })
  });
}

export function confirmTireRequestAppointment(id) {
  return request(`/api/tire-requests/${encodeURIComponent(id)}/confirm-appointment`, {
    method: "POST"
  });
}

export function getWorkOrders(locationId) {
  const query = locationId ? `?locationId=${encodeURIComponent(locationId)}` : "";
  return request(`/api/work-orders${query}`);
}

export function createWorkOrder(workOrder) {
  return request("/api/work-orders", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(workOrder)
  });
}

export function createWorkOrderFromAppointment(appointmentId) {
  return request(`/api/work-orders/from-appointment/${encodeURIComponent(appointmentId)}`, {
    method: "POST"
  });
}

export function updateWorkOrder(id, workOrder) {
  return request(`/api/work-orders/${encodeURIComponent(id)}`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(workOrder)
  });
}

export function startWorkOrder(id) {
  return request(`/api/work-orders/${encodeURIComponent(id)}/start`, {
    method: "POST"
  });
}

export function markWorkOrderVehicleReady(id) {
  return request(`/api/work-orders/${encodeURIComponent(id)}/vehicle-ready`, {
    method: "POST"
  });
}

export function completeWorkOrder(id) {
  return request(`/api/work-orders/${encodeURIComponent(id)}/complete`, {
    method: "POST"
  });
}

export function cancelWorkOrder(id) {
  return request(`/api/work-orders/${encodeURIComponent(id)}/cancel`, {
    method: "POST"
  });
}

export function convertWorkOrderToInvoice(id) {
  return request(`/api/work-orders/${encodeURIComponent(id)}/convert-to-invoice`, {
    method: "POST"
  });
}

export function linkWorkOrderInvoice(workOrderId, invoiceId) {
  return request(`/api/work-orders/${encodeURIComponent(workOrderId)}/link-invoice/${encodeURIComponent(invoiceId)}`, {
    method: "POST"
  });
}

export function previewWorkOrderInvoice(id) {
  return request(`/api/work-orders/${encodeURIComponent(id)}/invoice-preview`);
}

export function getInvoices(locationId) {
  const query = locationId ? `?locationId=${encodeURIComponent(locationId)}` : "";
  return request(`/api/invoices${query}`);
}

export function getInvoice(id) {
  return request(`/api/invoices/${id}`);
}

export function createInvoice(invoice) {
  return request("/api/invoices", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(invoice)
  });
}

export function deleteInvoice(id) {
  return request(`/api/invoices/${id}`, {
    method: "DELETE"
  });
}

export function updateInvoiceStatus(id, status) {
  const payload = typeof status === "object" && status !== null ? status : { status };
  return request(`/api/invoices/${id}/status`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(payload)
  });
}

export function getEstimates(locationId) {
  const query = locationId ? `?locationId=${encodeURIComponent(locationId)}` : "";
  return request(`/api/estimates${query}`);
}

export function createEstimate(estimate) {
  return request("/api/estimates", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(estimate)
  });
}

export function updateEstimate(id, estimate) {
  return request(`/api/estimates/${encodeURIComponent(id)}`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(estimate)
  });
}

export function approveEstimate(id) {
  return request(`/api/estimates/${encodeURIComponent(id)}/approve`, {
    method: "POST"
  });
}

export function declineEstimate(id) {
  return request(`/api/estimates/${encodeURIComponent(id)}/decline`, {
    method: "POST"
  });
}

export function cancelEstimate(id) {
  return request(`/api/estimates/${encodeURIComponent(id)}/cancel`, {
    method: "POST"
  });
}

export function sendEstimate(id) {
  return request(`/api/estimates/${encodeURIComponent(id)}/send`, {
    method: "POST"
  });
}

export function convertEstimateToInvoice(id, invoice) {
  return request(`/api/estimates/${encodeURIComponent(id)}/convert-to-invoice`, {
    method: "POST",
    ...(invoice ? {
      headers: jsonHeaders,
      body: JSON.stringify(invoice)
    } : {})
  });
}

export function getCustomerPortal() {
  return request("/api/customer/portal");
}

export function createCustomerVehicle(vehicle) {
  return request("/api/customer/vehicles", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(vehicle)
  });
}

export function deleteCustomerVehicle(id) {
  return request(`/api/customer/vehicles/${id}`, {
    method: "DELETE"
  });
}

export function createCustomerAppointment(appointment) {
  return request("/api/customer/appointments", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(appointment)
  });
}

export function getCustomerTireAvailability(vehicleId, locationId, serviceType = "INSTALLATION") {
  const params = new URLSearchParams({ vehicleId, serviceType });
  if (locationId) params.set("locationId", locationId);
  return request(`/api/customer/tire-availability?${params.toString()}`);
}

export function getCustomerTireRequests() {
  return request("/api/customer/tire-requests");
}

export function payCustomerInvoice(id) {
  return request(`/api/customer/invoices/${id}/pay`, {
    method: "POST"
  });
}

export function approveCustomerEstimate(id) {
  return request(`/api/customer/estimates/${id}/approve`, {
    method: "POST"
  });
}

export function markCustomerNotificationRead(id) {
  return request(`/api/customer/notifications/${id}/read`, {
    method: "PUT"
  });
}

export function getCustomers() {
  return request("/api/customers");
}

export function sendCustomerNotice(id, notice) {
  return request(`/api/customers/${id}/notices`, {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(notice)
  });
}

export function getAccountingReport(start, end, locationId) {
  const params = new URLSearchParams();
  if (start) params.set("start", start);
  if (end) params.set("end", end);
  if (locationId) params.set("locationId", locationId);
  return request(`/api/accounting/reports${params.toString() ? `?${params.toString()}` : ""}`);
}

export function getAccountingAccounts() {
  return request("/api/accounting/accounts");
}

export function createAccountingAccount(account) {
  return request("/api/accounting/accounts", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(account)
  });
}

export function createExpense(expense) {
  return request("/api/accounting/expenses", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(expense)
  });
}

export function payExpense(id, payment) {
  return request(`/api/accounting/expenses/${id}/pay`, {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(payment || {})
  });
}

export function getVendors() {
  return request("/api/accounting/vendors");
}

export function createVendor(vendor) {
  return request("/api/accounting/vendors", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(vendor)
  });
}

export function getPayrollPeriods(locationId) {
  const query = locationId ? `?locationId=${encodeURIComponent(locationId)}` : "";
  return request(`/api/payroll/periods${query}`);
}

export function createPayrollPeriod(period) {
  return request("/api/payroll/periods", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(period)
  });
}

export function updatePayrollPeriod(id, period) {
  return request(`/api/payroll/periods/${id}`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(period)
  });
}

export function deletePayrollPeriod(id) {
  return request(`/api/payroll/periods/${id}`, {
    method: "DELETE"
  });
}

export function generatePayroll(id) {
  return request(`/api/payroll/periods/${id}/generate`, {
    method: "POST"
  });
}

export function getPayrollRecordsForPeriod(id) {
  return request(`/api/payroll/periods/${id}/records`);
}

export function getPayrollRecordsForEmployee(employeeId) {
  return request(`/api/payroll/employees/${employeeId}/records`);
}

export function approvePayrollRecord(id) {
  return request(`/api/payroll/records/${id}/approve`, {
    method: "POST"
  });
}

export function payPayrollRecord(id) {
  return request(`/api/payroll/records/${id}/pay`, {
    method: "POST"
  });
}

export function updatePayrollRecordNotes(id, notes) {
  return request(`/api/payroll/records/${id}/notes`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify({ notes })
  });
}

export function addPayrollAdjustment(id, adjustment) {
  return request(`/api/payroll/records/${id}/adjustments`, {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(adjustment)
  });
}

export function deletePayrollAdjustment(recordId, adjustmentId) {
  return request(`/api/payroll/records/${recordId}/adjustments/${adjustmentId}`, {
    method: "DELETE"
  });
}

export function cancelPayrollRecord(id) {
  return request(`/api/payroll/records/${id}/cancel`, {
    method: "POST"
  });
}

export function getPayrollEmployees(locationId) {
  const query = locationId ? `?locationId=${encodeURIComponent(locationId)}` : "";
  return request(`/api/payroll/employees${query}`);
}

export function updateEmployeePayrollSettings(employeeId, settings) {
  return request(`/api/payroll/employees/${employeeId}/settings`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(settings)
  });
}

export function getPayrollLoans() {
  return request("/api/payroll/loans");
}

export function getEmployeeLoans(employeeId) {
  return request(`/api/payroll/employees/${employeeId}/loans`);
}

export function createEmployeeLoan(loan) {
  return request("/api/payroll/loans", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(loan)
  });
}

export function cancelEmployeeLoan(id) {
  return request(`/api/payroll/loans/${id}/cancel`, {
    method: "POST"
  });
}

export function getWorkShifts() {
  return request("/api/shifts");
}

export function getPlatformShops() {
  return request("/api/platform/shops");
}

export function getPlatformUsers() {
  return request("/api/platform/users");
}

export function getPlatformBillingSummary() {
  return request("/api/platform/billing/summary");
}

export function getPlatformBillingSubscriptions() {
  return request("/api/platform/billing/subscriptions");
}

export function getShopSubscription(shopId) {
  return request(`/api/platform/billing/shops/${encodeURIComponent(shopId)}/subscription`);
}

export function updateShopSubscription(shopId, subscription) {
  return request(`/api/platform/billing/shops/${encodeURIComponent(shopId)}/subscription`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(subscription)
  });
}

export function startShopTrial(shopId) {
  return request(`/api/platform/billing/shops/${encodeURIComponent(shopId)}/trial`, {
    method: "POST"
  });
}

export function getShopPayments(shopId) {
  return request(`/api/platform/billing/shops/${encodeURIComponent(shopId)}/payments`);
}

export function recordShopPayment(shopId, payment) {
  return request(`/api/platform/billing/shops/${encodeURIComponent(shopId)}/payments`, {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(payment)
  });
}

export function updateShopPaymentStatus(paymentId, paymentStatus) {
  return request(`/api/platform/billing/payments/${encodeURIComponent(paymentId)}/status`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(paymentStatus)
  });
}

export function cancelShopSubscription(shopId) {
  return request(`/api/platform/billing/shops/${encodeURIComponent(shopId)}/cancel`, {
    method: "POST"
  });
}

export function markShopReadOnly(shopId) {
  return request(`/api/platform/billing/shops/${encodeURIComponent(shopId)}/read-only`, {
    method: "POST"
  });
}

export function reactivateShopSubscription(shopId) {
  return request(`/api/platform/billing/shops/${encodeURIComponent(shopId)}/reactivate`, {
    method: "POST"
  });
}

export function createPlatformUser(user) {
  return request("/api/platform/users", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(user)
  });
}

export function activatePlatformUser(id) {
  return request(`/api/platform/users/${encodeURIComponent(id)}/activate`, {
    method: "POST"
  });
}

export function deactivatePlatformUser(id) {
  return request(`/api/platform/users/${encodeURIComponent(id)}/deactivate`, {
    method: "POST"
  });
}

export function getPlatformLinks() {
  return request("/api/platform/links");
}

export function assignAdminToShop(userId, shopId) {
  return request(`/api/platform/users/${encodeURIComponent(userId)}/assign-shop/${encodeURIComponent(shopId)}`, {
    method: "PUT"
  });
}

export function assignAdminToLocation(userId, locationId) {
  return request(`/api/platform/users/${encodeURIComponent(userId)}/assign-location/${encodeURIComponent(locationId)}`, {
    method: "PUT"
  });
}

export function assignPlatformRecord(type, id, assignment) {
  return request(`/api/platform/links/${encodeURIComponent(type)}/${encodeURIComponent(id)}`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(assignment)
  });
}

export function assignLegacyDataToShop(shopId) {
  return request(`/api/platform/shops/${encodeURIComponent(shopId)}/assign-legacy-data`, {
    method: "POST"
  });
}

export function createPlatformShop(shop) {
  return request("/api/platform/shops", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(shop)
  });
}

export function updatePlatformShop(id, shop) {
  return request(`/api/platform/shops/${id}`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(shop)
  });
}

export function activatePlatformShop(id) {
  return request(`/api/platform/shops/${id}/activate`, {
    method: "POST"
  });
}

export function deactivatePlatformShop(id) {
  return request(`/api/platform/shops/${id}/deactivate`, {
    method: "POST"
  });
}

export function deletePlatformShop(id) {
  return request(`/api/platform/shops/${encodeURIComponent(id)}`, {
    method: "DELETE"
  });
}

export function getShopLocations(shopId) {
  return request(`/api/shop-locations/shop/${encodeURIComponent(shopId)}`);
}

export function getActiveShopLocations(shopId) {
  return request(`/api/shop-locations/shop/${encodeURIComponent(shopId)}/active`);
}

export function createShopLocation(location) {
  return request("/api/shop-locations", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(location)
  });
}

export function clockIn(locationId) {
  const query = locationId ? `?locationId=${encodeURIComponent(locationId)}` : "";
  return request(`/api/attendance/clock-in${query}`, {
    method: "POST"
  });
}

export function clockOut() {
  return request("/api/attendance/clock-out", {
    method: "POST"
  });
}

export function getMyTodayAttendance() {
  return request("/api/attendance/me/today");
}

export function getMyAttendanceRange(start, end) {
  const params = new URLSearchParams({ start, end });
  return request(`/api/attendance/me/range?${params.toString()}`);
}

export function getAttendanceEmployees() {
  return request("/api/attendance/employees");
}

export function getAttendanceByDate(date, locationId) {
  const params = new URLSearchParams();
  if (date) params.set("date", date);
  if (locationId) params.set("locationId", locationId);
  return request(`/api/attendance/day${params.toString() ? `?${params.toString()}` : ""}`);
}

export function getEmployeeAttendanceRange(employeeId, start, end, locationId) {
  const params = new URLSearchParams({ start, end });
  if (locationId) params.set("locationId", locationId);
  return request(`/api/attendance/employee/${encodeURIComponent(employeeId)}/range?${params.toString()}`);
}

export function markEmployeeAbsent(employeeId, date) {
  return request(`/api/attendance/employee/${encodeURIComponent(employeeId)}/absent?date=${encodeURIComponent(date)}`, {
    method: "POST"
  });
}

export function resolveAbsence(attendanceId, decision, notes) {
  return request(`/api/attendance/${encodeURIComponent(attendanceId)}/resolve-absence`, {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify({ decision, notes })
  });
}

export function getUnresolvedAbsences() {
  return request("/api/attendance/absences/unresolved");
}

export function createWorkShift(shift) {
  return request("/api/shifts", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(shift)
  });
}

export function deleteWorkShift(id) {
  return request(`/api/shifts/${id}`, {
    method: "DELETE"
  });
}

export function getPayrollShiftSlots(periodId) {
  const query = periodId ? `?periodId=${encodeURIComponent(periodId)}` : "";
  return request(`/api/payroll/shift-slots${query}`);
}

export function createPayrollShiftSlot(slot) {
  return request("/api/payroll/shift-slots", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(slot)
  });
}

export function deletePayrollShiftSlot(id) {
  return request(`/api/payroll/shift-slots/${id}`, {
    method: "DELETE"
  });
}

export function signupForPayrollShiftSlot(id) {
  return request(`/api/payroll/shift-slots/${id}/signup`, {
    method: "POST"
  });
}

export function cancelPayrollShiftSignup(id) {
  return request(`/api/payroll/shift-slots/${id}/signup`, {
    method: "DELETE"
  });
}

export function assignEmployeeToPayrollShiftSlot(slotId, employeeId) {
  return request(`/api/payroll/shift-slots/${slotId}/employees/${employeeId}`, {
    method: "POST"
  });
}

export function removePayrollShiftSignup(slotId, signupId) {
  return request(`/api/payroll/shift-slots/${slotId}/signups/${signupId}`, {
    method: "DELETE"
  });
}
