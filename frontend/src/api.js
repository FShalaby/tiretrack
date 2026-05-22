const jsonHeaders = {
  "Content-Type": "application/json"
};

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
  const headers = {
    ...options.headers,
    ...jsonHeaders
  };

  if (!skipAuth && token) {
    headers.Authorization = `Bearer ${token}`;
  }

  try {
    response = await fetch(path, {
      ...options,
      headers
    });
  } catch {
    throw new Error(`Could not reach the backend for ${path}. Make sure Spring Boot is running on port 8080.`);
  }

  if (response.status === 401 && !skipAuth) {
    const refreshed = await refreshAuthToken();
    if (refreshed) {
      const retryToken = getAuthToken();
      response = await fetch(path, {
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
    let parsedMessage = null;

    try {
      parsedMessage = JSON.parse(message);
    } catch {
      parsedMessage = null;
    }

    const details = parsedMessage?.message || (parsedMessage ? Object.values(parsedMessage).join(", ") : message);
    throw new Error(details ? `${path}: ${details}` : `${path}: request failed with status ${response.status}`);
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

export function logout() {
  clearAuthSession();
}

export function getAvailableSlots(date) {
  return request(`/api/public/available-slots?date=${encodeURIComponent(date)}`, {}, true);
}

export function createPublicBooking(booking) {
  return request("/api/public/bookings", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify(booking)
  }, true);
}

export function getDashboard() {
  return request("/api/dashboard");
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

export function getSalesData(days = 14) {
  return request(`/api/dashboard/sales?days=${encodeURIComponent(days)}`);
}

export function getTires() {
  return request("/api/tires");
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

export function getAppointments() {
  return request("/api/appointments");
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

export function getInvoices() {
  return request("/api/invoices");
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
  return request(`/api/invoices/${id}/status`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify({ status })
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

export function payCustomerInvoice(id) {
  return request(`/api/customer/invoices/${id}/pay`, {
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

export function getAccountingReport(start, end) {
  const params = new URLSearchParams();
  if (start) params.set("start", start);
  if (end) params.set("end", end);
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

export function getPayrollPeriods() {
  return request("/api/payroll/periods");
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

export function cancelPayrollRecord(id) {
  return request(`/api/payroll/records/${id}/cancel`, {
    method: "POST"
  });
}

export function getPayrollEmployees() {
  return request("/api/payroll/employees");
}

export function updateEmployeePayrollSettings(employeeId, settings) {
  return request(`/api/payroll/employees/${employeeId}/settings`, {
    method: "PUT",
    headers: jsonHeaders,
    body: JSON.stringify(settings)
  });
}

export function getWorkShifts() {
  return request("/api/shifts");
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
