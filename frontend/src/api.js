const jsonHeaders = {
  "Content-Type": "application/json"
};

async function request(path, options = {}) {
  const response = await fetch(path, options);

  if (!response.ok) {
    const message = await response.text();
    let parsedMessage = null;

    try {
      parsedMessage = JSON.parse(message);
    } catch {
      parsedMessage = null;
    }

    throw new Error(parsedMessage?.message || (parsedMessage ? Object.values(parsedMessage).join(", ") : message) || `Request failed with status ${response.status}`);
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
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
