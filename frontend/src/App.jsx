import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { motion } from "framer-motion";
import {
  AlertTriangle,
  Bell,
  BriefcaseBusiness,
  CalendarDays,
  ChevronDown,
  CheckCircle2,
  CircleDollarSign,
  Clock3,
  ClipboardList,
  Download,
  Disc3,
  FileText,
  Gauge,
  LogIn,
  MapPin,
  Package,
  RefreshCw,
  Search,
  Settings as SettingsIcon,
  Moon,
  Sun,
  Upload,
  UserCircle,
  ShieldCheck
} from "lucide-react";
import {
  Area,
  AreaChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from "recharts";
import {
  createAccountingAccount,
  createAppointment,
  createCustomerAppointment,
  createCustomerVehicle,
  createExpense,
  createInvoice,
  createPlatformShop,
  createPlatformUser,
  createShopLocation,
  createPayrollPeriod,
  createPayrollShiftSlot,
  createEmployeeLoan,
  createNotification,
  createPublicBooking,
  createTire,
  createVendor,
  createWorkShift,
  deleteAppointment,
  deleteCustomerVehicle,
  deleteInvoice,
  deletePayrollPeriod,
  deletePayrollShiftSlot,
  deleteTire,
  deleteWorkShift,
  confirmTireRequestAppointment,
  approveCustomerEstimate,
  approvePayrollRecord,
  addPayrollAdjustment,
  approveEstimate,
  assignAdminToLocation,
  assignAdminToShop,
  assignLegacyDataToShop,
  assignPlatformRecord,
  cancelEstimate,
  cancelPayrollRecord,
  cancelEmployeeLoan,
  cancelWorkOrder,
  clockIn,
  clockOut,
  completeWorkOrder,
  convertEstimateToInvoice,
  convertWorkOrderToInvoice,
  generatePayroll,
  getAppointments,
  getAppointmentTireAvailability,
  getAccountingAccounts,
  getAccountingReport,
  getAuditLogs,
  getAttendanceByDate,
  getAttendanceEmployees,
  getDashboard,
  getEstimates,
  getEmployeeAttendanceRange,
  getInvoice,
  getInvoices,
  getNotifications,
  getLowStockTires,
  getMyAttendanceRange,
  getMyTodayAttendance,
  getPlatformLinks,
  getPlatformShops,
  getPlatformUsers,
  getUnresolvedAbsences,
  getSalesData,
  getSettings,
  getActiveShopLocations,
  getTires,
  getTireRequests,
  getAvailableSlots,
  getCurrentUser,
  getCustomerPortal,
  getCustomerTireAvailability,
  getCustomers,
  getVendors,
  getPayrollEmployees,
  getPayrollLoans,
  getPayrollPeriods,
  getPayrollRecordsForEmployee,
  getPayrollRecordsForPeriod,
  getPayrollShiftSlots,
  getPublicShopLocations,
  getPublicShops,
  getWorkOrders,
  getWorkShifts,
  importTiresCsv,
  login as loginApi,
  logout as logoutApi,
  markEmployeeAbsent,
  markCustomerNotificationRead,
  markAllNotificationsRead,
  markNotificationRead,
  payExpense as payExpenseApi,
  payCustomerInvoice,
  payPayrollRecord,
  previewWorkOrderInvoice,
  register as registerApi,
  refreshToken as refreshTokenApi,
  searchTiresByBrand,
  searchTiresByCondition,
  searchTiresByLocation,
  searchTiresBySeason,
  searchTiresBySize,
  sendCustomerNotice,
  sendEstimate,
  createEstimate,
  createWorkOrder,
  createWorkOrderFromAppointment,
  resolveAbsence,
  activatePlatformShop,
  activatePlatformUser,
  deactivatePlatformShop,
  deactivatePlatformUser,
  updateAppointment,
  updateInvoiceStatus,
  updateEstimate,
  updateEmployeePayrollSettings,
  updatePayrollRecordNotes,
  updatePayrollPeriod,
  updatePlatformShop,
  updateSettings,
  updateTire,
  updateTireRequestStatus,
  updateWorkOrder,
  markWorkOrderVehicleReady,
  startWorkOrder,
  signupForPayrollShiftSlot,
  cancelPayrollShiftSignup,
  deletePayrollAdjustment,
  assignEmployeeToPayrollShiftSlot,
  removePayrollShiftSignup
} from "./api";

const tabs = ["Platform", "Employee Portal", "Dashboard", "Tires", "Appointments", "Work Orders", "Estimates", "Invoices", "Customers", "Accounting", "Attendance", "Payroll", "My Payroll", "Audit Logs", "Settings"];
const employeeHiddenTabs = ["Dashboard", "Customers", "Accounting", "Audit Logs", "Settings"];
const tabIcons = {
  Dashboard: Gauge,
  Platform: ShieldCheck,
  "Employee Portal": Clock3,
  Tires: Disc3,
  Appointments: CalendarDays,
  "Work Orders": ClipboardList,
  Estimates: FileText,
  Invoices: FileText,
  Customers: UserCircle,
  Accounting: CircleDollarSign,
  Attendance: Clock3,
  Payroll: BriefcaseBusiness,
  "My Payroll": CircleDollarSign,
  "Audit Logs": ClipboardList,
  Settings: SettingsIcon
};
const statusClassMap = {
  BOOKED: "blue",
  PENDING_TIRE_AVAILABILITY: "yellow",
  COMPLETED: "green",
  CANCELLED: "red",
  PAID: "green",
  UNPAID: "yellow",
  PARTIAL: "yellow",
  PARTIALLY_PAID: "yellow",
  OVERDUE: "red",
  PENDING: "yellow",
  IN_PROGRESS: "blue",
  VEHICLE_READY: "green",
  APPROVED: "blue",
  DRAFT: "gray",
  SENT: "blue",
  DECLINED: "red",
  CONVERTED: "green",
  EXPIRED: "red",
  VOID: "gray",
  DUE_SOON: "yellow",
  SOURCING: "blue",
  AVAILABLE: "green",
  FULFILLED: "green",
  UNAVAILABLE: "red",
  IN_STOCK: "green",
  LOW_STOCK: "yellow",
  OUT_OF_STOCK: "red",
  AVAILABLE_AT_OTHER_LOCATION: "yellow",
  REMINDER: "blue",
  NEW: "green",
  USED: "yellow",
  ACTIVE: "green",
  PAID_OFF: "blue"
};
const chartColors = ["#18d3b2", "#7c8cff", "#ef4444", "#f59e0b"];
const appointmentTimes = buildAppointmentTimes();
const dashboardWidgetCatalog = [
  { id: "keyMetrics", label: "Key Metrics", icon: Gauge },
  { id: "today", label: "Today", icon: CalendarDays },
  { id: "revenue", label: "Revenue Trend", icon: CircleDollarSign },
  { id: "workOrders", label: "Work Orders", icon: ClipboardList },
  { id: "inventoryMovement", label: "Inventory Movement", icon: Package },
  { id: "followUps", label: "Customer Follow Ups", icon: Bell },
  { id: "latestInvoices", label: "Latest Invoices", icon: FileText },
  { id: "locationBreakdown", label: "Location Breakdown", icon: ShieldCheck },
  { id: "conditionMix", label: "Tire Condition Mix", icon: ShieldCheck },
  { id: "topInventory", label: "Top Inventory", icon: Package },
  { id: "lowStock", label: "Low Stock", icon: AlertTriangle },
  { id: "inventoryHealth", label: "Inventory Health", icon: Gauge },
  { id: "activity", label: "Recent Actions", icon: Bell }
];
const defaultDashboardWidgetIds = ["keyMetrics", "today", "revenue", "workOrders", "inventoryMovement", "followUps"];
const accountingTabs = ["Dashboard", "Expenses", "Vendors", "Accounts", "Reports", "Ledger"];
const accountingTabMeta = {
  Dashboard: {
    description: "High-level financial snapshot with the reports most owners check first.",
    eyebrow: "Overview",
    nav: "Snapshot",
    statLabel: "Net income",
    statValue: ({ report }) => money(report?.netIncome)
  },
  Expenses: {
    description: "Post vendor costs, track due dates, and keep payable status visible.",
    eyebrow: "Spend control",
    nav: "Record costs",
    statLabel: "Recent expenses",
    statValue: ({ recentExpenses }) => recentExpenses.length
  },
  Vendors: {
    description: "Manage suppliers and quickly understand current-period vendor spending.",
    eyebrow: "Supplier book",
    nav: "Suppliers",
    statLabel: "Vendors",
    statValue: ({ vendors }) => vendors.length
  },
  Accounts: {
    description: "Maintain the chart of accounts that powers expenses, reports, and ledger posting.",
    eyebrow: "Setup",
    nav: "Chart",
    statLabel: "Accounts",
    statValue: ({ accounts }) => accounts.length
  },
  Reports: {
    description: "Review core statements without mixing them into day-to-day entry screens.",
    eyebrow: "Statements",
    nav: "P&L / balance",
    statLabel: "Revenue",
    statValue: ({ report }) => money(report?.revenue)
  },
  Ledger: {
    description: "Audit double-entry postings and verify debits, credits, and account balances.",
    eyebrow: "Audit trail",
    nav: "Journal",
    statLabel: "Entries",
    statValue: ({ recentJournalEntries }) => recentJournalEntries.length
  }
};
const expenseCategoryOptions = [
  "INVENTORY",
  "SUPPLIES",
  "RENT",
  "UTILITIES",
  "PAYROLL",
  "EQUIPMENT",
  "MARKETING",
  "INSURANCE",
  "PROFESSIONAL_SERVICES",
  "REPAIRS_MAINTENANCE",
  "BANK_FEES",
  "OTHER"
];
const vendorCategoryOptions = [
  "TIRE_SUPPLIER",
  "PARTS_SUPPLIER",
  "EQUIPMENT",
  "UTILITIES",
  "RENT",
  "INSURANCE",
  "PROFESSIONAL_SERVICES",
  "MARKETING",
  "GENERAL",
  "OTHER"
];
const accountingPaymentOptions = ["CASH", "DEBIT", "CREDIT_CARD", "BANK_TRANSFER", "CHEQUE", "OTHER"];
const tooltipStyle = {
  background: "var(--tooltip-bg)",
  border: "1px solid var(--tooltip-border)",
  borderRadius: 10,
  color: "var(--tooltip-text)"
};

const SERVICE_TYPE_OPTIONS = ["INSTALLATION", "RE_AND_RE", "BOLT_ON", "BALANCING", "ROTATION", "REPAIR"];

const emptyTire = {
  brand: "",
  model: "",
  width: 205,
  aspectRatio: 55,
  rimSize: 16,
  season: "All Season",
  condition: "NEW",
  quantity: 4,
  price: "0.00",
  location: "",
  shopId: "",
  locationId: ""
};

const emptyTireFilters = {
  type: "brand",
  query: "",
  condition: "NEW",
  width: "",
  aspectRatio: "",
  rimSize: "",
  threshold: "4"
};

const emptyAppointment = {
  customerId: "",
  customerVehicleId: "",
  customerName: "",
  email: "",
  phone: "",
  vehicle: "",
  tireSetup: "regular",
  tireSize: "",
  frontTireId: "",
  frontQuantity: 4,
  frontTireSize: "",
  rearTireId: "",
  rearQuantity: 0,
  rearTireSize: "",
  appointmentDate: "",
  locationId: "",
  serviceType: "INSTALLATION",
  notes: "",
  overrideTireAvailability: false,
  tireAvailabilityOverrideReason: "",
  reminderStatus: "NOT_SET",
  reminderAt: "",
  confirmationStatus: "PENDING",
  cancelReason: "",
  status: "BOOKED"
};

const emptyExpense = {
  vendor: "",
  vendorId: "",
  expenseAccountId: "",
  category: "Supplies",
  categoryKey: "SUPPLIES",
  customCategory: "",
  expenseDate: todayDateKey(),
  dueDate: "",
  subtotal: "0.00",
  taxAmount: "0.00",
  total: "0.00",
  status: "PAID",
  paymentMethod: "Cash",
  paymentMethodKey: "CASH",
  customPaymentMethod: "",
  notes: "",
  locationId: ""
};

const emptyAccount = {
  code: "",
  name: "",
  type: "EXPENSE",
  active: true,
  systemAccount: false
};

const emptyVendor = {
  name: "",
  email: "",
  phone: "",
  category: "General",
  categoryKey: "GENERAL",
  customCategory: "",
  notes: ""
};

const defaultAccountingRange = {
  mode: "lifetime",
  start: "",
  end: ""
};

const emptyInvoice = {
  companyName: "Your Shop Name",
  customerName: "",
  phone: "",
  vehicle: "",
  paymentMethod: "Cash",
  status: "UNPAID",
  amountPaid: "",
  dueDate: "",
  appointmentId: "",
  estimateId: "",
  locationId: "",
  items: [makeInvoiceItem()]
};

const emptyWorkOrder = {
  appointmentId: "",
  customerId: "",
  customerName: "",
  email: "",
  phone: "",
  vehicle: "",
  assignedEmployeeId: "",
  locationId: "",
  serviceType: "INSTALLATION",
  notes: ""
};

const emptyEstimate = {
  customerId: "",
  customerName: "",
  email: "",
  phone: "",
  vehicle: "",
  locationId: "",
  taxRate: "13",
  notes: "",
  validUntil: "",
  items: [makeInvoiceItem()]
};

const defaultCompanySettings = {
  shopName: "Your Shop Name",
  logoUrl: "",
  phone: "",
  address: "",
  taxRate: "13",
  invoiceTerms: "Payment is due upon receipt. Thank you for your business."
};

function money(value) {
  return Number(value || 0).toLocaleString(undefined, {
    style: "currency",
    currency: "CAD"
  });
}

function formatCanadianPhoneInput(value) {
  const rawDigits = String(value || "").replace(/\D/g, "");
  const digits = rawDigits.length > 10 && rawDigits.startsWith("1") ? rawDigits.slice(1) : rawDigits;
  const trimmed = digits.slice(0, 10);

  if (trimmed.length <= 3) {
    return trimmed;
  }

  if (trimmed.length <= 6) {
    return `${trimmed.slice(0, 3)}-${trimmed.slice(3)}`;
  }

  return `${trimmed.slice(0, 3)}-${trimmed.slice(3, 6)}-${trimmed.slice(6)}`;
}

function formatTireSizeInput(value) {
  const digits = String(value || "").replace(/\D/g, "").slice(0, 7);

  if (digits.length <= 3) {
    return digits;
  }

  if (digits.length <= 5) {
    return `${digits.slice(0, 3)}/${digits.slice(3)}`;
  }

  return `${digits.slice(0, 3)}/${digits.slice(3, 5)}/${digits.slice(5)}`;
}

function publicStoreLocations(locations) {
  return (locations || []).filter((location) => String(location.type || "").toUpperCase() === "STORE");
}

function playNotificationChime() {
  const AudioContext = window.AudioContext || window.webkitAudioContext;
  if (!AudioContext) {
    return;
  }

  try {
    const context = new AudioContext();
    const gain = context.createGain();
    gain.gain.setValueAtTime(0.0001, context.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.08, context.currentTime + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.0001, context.currentTime + 0.45);
    gain.connect(context.destination);

    [660, 880].forEach((frequency, index) => {
      const oscillator = context.createOscillator();
      oscillator.type = "sine";
      oscillator.frequency.setValueAtTime(frequency, context.currentTime + index * 0.11);
      oscillator.connect(gain);
      oscillator.start(context.currentTime + index * 0.11);
      oscillator.stop(context.currentTime + 0.32 + index * 0.11);
    });

    window.setTimeout(() => context.close().catch(() => null), 700);
  } catch {
    // Browsers can block sound until the user has interacted with the page.
  }
}

function invoiceStatusKey(status) {
  const normalized = String(status || "UNPAID").toUpperCase();
  return normalized === "PARTIAL" ? "PARTIALLY_PAID" : normalized;
}

function invoiceNumber(invoice) {
  const value = invoice?.invoiceNumber || invoice?.id;
  return value ? String(value).startsWith("INV-") ? String(value) : `INV-${String(value).padStart(6, "0")}` : "-";
}

function statusLabel(value) {
  const normalized = String(value || "").toUpperCase();
  const labels = {
    PARTIALLY_PAID: "Partially paid",
    IN_PROGRESS: "In progress",
    VEHICLE_READY: "Vehicle ready",
    PAID_OFF: "Paid off",
    DUE_SOON: "Due soon",
    CONVERTED: "Invoiced",
    NOT_SET: "Not set",
    PENDING_TIRE_AVAILABILITY: "Waiting for tire availability",
    AVAILABLE_AT_OTHER_LOCATION: "Available at another location",
    OUT_OF_STOCK: "Out of stock",
    IN_STOCK: "In stock",
    LOW_STOCK: "Low stock"
  };

  return labels[normalized] || normalized.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function appointmentStatusLabel(value) {
  return statusLabel(value || "BOOKED");
}

function tireRequestStatusLabel(value) {
  return statusLabel(value || "PENDING");
}

function tireAvailabilityTone(status) {
  switch (String(status || "").toUpperCase()) {
    case "IN_STOCK":
      return "green";
    case "LOW_STOCK":
    case "AVAILABLE_AT_OTHER_LOCATION":
      return "yellow";
    case "OUT_OF_STOCK":
      return "red";
    default:
      return "gray";
  }
}

function serviceTypeLabel(value) {
  const labels = {
    INSTALLATION: "Installation",
    RE_AND_RE: "Re & Re (Remove & Replace)",
    BOLT_ON: "Bolt On",
    BALANCING: "Balancing",
    ROTATION: "Rotation",
    REPAIR: "Repair"
  };

  return labels[String(value || "").toUpperCase()] || statusLabel(value || "Service");
}

function usesInventoryTires(serviceType) {
  return String(serviceType || "").toUpperCase() === "INSTALLATION";
}

function ownTireServiceMessage(serviceType) {
  switch (String(serviceType || "").toUpperCase()) {
    case "RE_AND_RE":
      return "Customer is bringing loose tires. The shop removes the old tires from the rims and installs the replacement tires.";
    case "BOLT_ON":
      return "Customer is bringing a mounted set already on rims. The shop swaps the wheel assemblies onto the vehicle.";
    default:
      return "";
  }
}

function needsTireSourcing(availability) {
  return Boolean(availability?.tireServiceRequired && !availability?.canConfirmAppointment);
}

function appointmentSubmitLabel(editingId, form, availability) {
  if (editingId) {
    return "Save Changes";
  }

  if (needsTireSourcing(availability)) {
    return form?.overrideTireAvailability ? "Book With Override" : "Create Sourcing Request";
  }

  return "Book Appointment";
}

function customerBookingSubmitLabel(availability) {
  return needsTireSourcing(availability) ? "Request Tire Sourcing" : "Book Appointment";
}

function tireRequestCustomerMessage(status) {
  switch (String(status || "PENDING").toUpperCase()) {
    case "SOURCING":
      return "The shop is sourcing your tire.";
    case "AVAILABLE":
      return "Your requested tire is available.";
    case "UNAVAILABLE":
      return "The shop could not source this tire at this time.";
    case "FULFILLED":
      return "Your tire request has been fulfilled.";
    case "DECLINED":
      return "The shop declined this tire request.";
    case "CANCELLED":
      return "This tire request was cancelled.";
    case "PENDING":
    default:
      return "Waiting for shop review.";
  }
}

function invoiceCollectedAmount(invoice) {
  const amountPaid = Number(invoice?.amountPaid || 0);

  if (amountPaid > 0) {
    return amountPaid;
  }

  return invoiceStatusKey(invoice?.status) === "PAID" ? Number(invoice?.total || 0) : 0;
}

function invoiceBalanceAmount(invoice) {
  const explicitBalance = Number(invoice?.balanceDue || 0);

  if (explicitBalance > 0) {
    return explicitBalance;
  }

  if (["PAID", "VOID"].includes(invoiceStatusKey(invoice?.status))) {
    return 0;
  }

  return Math.max(Number(invoice?.total || 0) - invoiceCollectedAmount(invoice), 0);
}

function dateTime(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString();
}

function toDateTimeLocalValue(value) {
  return value ? String(value).slice(0, 16) : "";
}

function reminderStatusLabel(value) {
  switch (String(value || "NOT_SET").toUpperCase()) {
    case "SCHEDULED":
      return "Reminder scheduled";
    case "SENT":
      return "Reminder sent";
    case "NOT_SET":
    default:
      return "No reminder";
  }
}

function confirmationStatusLabel(value) {
  switch (String(value || "PENDING").toUpperCase()) {
    case "CONFIRMED":
      return "Confirmed";
    case "NO_SHOW":
      return "No-show";
    case "PENDING":
    default:
      return "Awaiting confirmation";
  }
}

function splitAppointmentDate(value) {
  const [date = "", rawTime = ""] = (value || "").split("T");
  const time = rawTime.slice(0, 5);

  return { date, time };
}

function joinAppointmentDate(date, time) {
  if (!date && !time) {
    return "";
  }

  return `${date}T${time}`;
}

function buildAppointmentTimes() {
  const slots = [];
  for (let minutes = 9 * 60; minutes <= (16 * 60) + 30; minutes += 30) {
    const hour = String(Math.floor(minutes / 60)).padStart(2, "0");
    const minute = String(minutes % 60).padStart(2, "0");
    slots.push(`${hour}:${minute}`);
  }
  return slots;
}

function todayDateKey() {
  return toDateKey(new Date());
}

function locationScopeText(summary) {
  if (!summary) {
    return "No records";
  }

  return `${summary.tireUnits} tires - ${summary.todayAppointments} today - ${summary.openJobs} open jobs`;
}

function buildLocationScopeSummaries(locations, tires, appointments, workOrders) {
  const activeLocations = (locations || []).filter((location) => location.active !== false);

  function summarize(locationId) {
    const scopedTires = filterRecordsByLocation(tires, locationId);
    const scopedAppointments = filterRecordsByLocation(appointments, locationId);
    const scopedWorkOrders = filterRecordsByLocation(workOrders, locationId);

    return {
      tireUnits: scopedTires.reduce((total, tire) => total + Number(tire.quantity || 0), 0),
      todayAppointments: scopedAppointments.filter((appointment) =>
        appointmentDateKey(appointment.appointmentDate) === todayDateKey()
        && appointment.status !== "CANCELLED"
      ).length,
      openJobs: scopedWorkOrders.filter((workOrder) =>
        ["PENDING", "IN_PROGRESS", "VEHICLE_READY"].includes(String(workOrder.status || "").toUpperCase())
      ).length
    };
  }

  return [
    { id: "", name: "All Locations", type: "Shop-wide", ...summarize("") },
    ...activeLocations.map((location) => ({
      id: String(location.id),
      name: location.name || "Location",
      type: location.type || "Location",
      ...summarize(location.id)
    }))
  ];
}

function appointmentDateKey(value) {
  return splitAppointmentDate(value).date;
}

function appointmentTimeKey(value) {
  return splitAppointmentDate(value).time;
}

function isBookableAppointment(appointment) {
  return !appointment.status || appointment.status === "BOOKED";
}

function customerForAppointment(appointment, customers) {
  return customers.find((customer) =>
    (appointment.customerId && Number(customer.id) === Number(appointment.customerId))
    || (appointment.phone && customer.phone === appointment.phone)
    || (appointment.email && customer.email === appointment.email)
  );
}

function matchingCustomersForForm(customers, form, limit = 5) {
  const needles = [form.customerName, form.email, form.phone]
    .map((value) => String(value || "").trim().toLowerCase())
    .filter(Boolean);

  if (!needles.length || form.customerId) {
    return [];
  }

  return customers
    .filter((customer) => {
      const haystack = [
        customer.fullName,
        customer.email,
        customer.phone
      ].filter(Boolean).join(" ").toLowerCase();

      return needles.some((needle) => haystack.includes(needle));
    })
    .slice(0, limit);
}

function compactDate(value) {
  return value ? new Date(value).toLocaleDateString(undefined, { month: "short", day: "numeric" }) : "";
}

function loadCompanySettings() {
  try {
    return { ...defaultCompanySettings, ...JSON.parse(localStorage.getItem("tiretrack-company-settings") || "{}") };
  } catch {
    return defaultCompanySettings;
  }
}

function loadThemeMode() {
  try {
    return localStorage.getItem("tiretrack-theme") || "dark";
  } catch {
    return "dark";
  }
}

function dashboardWidgetStorageKey(auth) {
  return `tiretrack-dashboard-widgets-${auth?.id || auth?.email || "default"}`;
}

function validDashboardWidgetIds(ids) {
  const validIds = new Set(dashboardWidgetCatalog.map((widget) => widget.id));
  return (ids || []).filter((id) => validIds.has(id));
}

function loadDashboardWidgets(storageKey) {
  try {
    const storedWidgets = JSON.parse(localStorage.getItem(storageKey) || "null");
    if (Array.isArray(storedWidgets)) {
      return validDashboardWidgetIds(storedWidgets);
    }
  } catch {
    // Ignore invalid local preferences and fall back to the default workspace.
  }

  return defaultDashboardWidgetIds;
}

function makeInvoiceForm(settings) {
  return {
    ...emptyInvoice,
    companyName: settings?.shopName || defaultCompanySettings.shopName
  };
}

function loadStoredAuth() {
  try {
    return JSON.parse(localStorage.getItem("tiretrack-auth") || "null");
  } catch {
    return null;
  }
}

function persistStoredAuth(authData) {
  localStorage.setItem("tiretrack-auth", JSON.stringify(authData));
  localStorage.setItem("tiretrack-token", authData.token);
  localStorage.setItem("tiretrack-refresh-token", authData.refreshToken);
}

function clearStoredAuth() {
  localStorage.removeItem("tiretrack-auth");
  localStorage.removeItem("tiretrack-token");
  localStorage.removeItem("tiretrack-refresh-token");
}

function scrollPageToTop() {
  window.requestAnimationFrame(() => {
    document.querySelector(".content")?.scrollTo?.({ top: 0, behavior: "smooth" });
    window.scrollTo({ top: 0, behavior: "smooth" });
  });
}

function scrollToSelector(selector) {
  window.requestAnimationFrame(() => {
    const target = document.querySelector(selector);
    target?.scrollIntoView?.({ behavior: "smooth", block: "start" });
  });
}

function defaultTabForRole(role) {
  if (role === "SUPER_ADMIN") {
    return "Platform";
  }

  if (role === "CUSTOMER") {
    return "Portal";
  }

  return role === "EMPLOYEE" ? "Employee Portal" : "Dashboard";
}

function canAccessTab(role, tab) {
  if (role === "SUPER_ADMIN") {
    return tab === "Platform";
  }

  if (tab === "Platform") {
    return false;
  }

  if (tab === "Employee Portal") {
    return role === "EMPLOYEE";
  }

  if (role === "CUSTOMER") {
    return tab === "Portal";
  }

  if (tab === "Attendance") {
    return isShopManagerRole(role);
  }

  if (tab === "Payroll") {
    return isShopManagerRole(role);
  }

  if (tab === "My Payroll") {
    return role === "EMPLOYEE";
  }

  return role !== "EMPLOYEE" || !employeeHiddenTabs.includes(tab);
}

function isShopManagerRole(role) {
  return role === "OWNER" || role === "ADMIN";
}

function isShopOwnerAccount(auth) {
  return auth?.role === "OWNER" || Boolean(auth?.shopOwner);
}

function canUseMultiLocationScope(auth) {
  return isShopOwnerAccount(auth) && Boolean(auth?.multiLocationAllowed);
}

function requiresShopAssignment(role) {
  return role === "OWNER" || role === "ADMIN" || role === "EMPLOYEE";
}

function downloadTextFile(filename, content, type = "text/plain") {
  const blob = new Blob([content], { type });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}

function accountingOptionLabel(value) {
  return String(value || "")
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function resolveAccountingLabel(key, customValue) {
  if (key === "OTHER" && customValue?.trim()) {
    return customValue.trim();
  }

  return accountingOptionLabel(key);
}

function csvEscape(value) {
  const text = String(value ?? "");

  return /[",\n]/.test(text) ? `"${text.replaceAll("\"", "\"\"")}"` : text;
}

function htmlEscape(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#39;");
}

function toCsv(headers, rows) {
  return [headers, ...rows].map((row) => row.map(csvEscape).join(",")).join("\n");
}

function getRangeStart(range, customStart) {
  const date = new Date();

  if (range === "today") {
    return todayDateKey();
  }

  if (range === "week") {
    date.setDate(date.getDate() - 6);
    return toDateKey(date);
  }

  if (range === "custom") {
    return customStart || todayDateKey();
  }

  date.setDate(date.getDate() - 29);
  return toDateKey(date);
}

function accountingDatesForRange(range = defaultAccountingRange) {
  const mode = range.mode || "lifetime";

  if (mode === "today") {
    const today = todayDateKey();
    return { start: today, end: today };
  }

  if (mode === "month") {
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth(), 1);
    return { start: toDateKey(start), end: todayDateKey() };
  }

  if (mode === "custom") {
    return {
      start: range.start || todayDateKey(),
      end: range.end || range.start || todayDateKey()
    };
  }

  return { start: null, end: null };
}

function accountingRangeLabel(range = defaultAccountingRange) {
  const mode = range.mode || "lifetime";

  if (mode === "today") {
    return "Today";
  }

  if (mode === "month") {
    return "This month";
  }

  if (mode === "custom") {
    const dates = accountingDatesForRange(range);
    return `${dates.start} to ${dates.end}`;
  }

  return "Lifetime";
}

function buildTireSize(form) {
  if (!usesInventoryTires(form.serviceType)) {
    return "";
  }

  if (form.tireSetup === "staggered") {
    const front = form.frontTireSize.trim();
    const rear = form.rearTireSize.trim();

    if (front && rear) {
      return `Front: ${front} / Rear: ${rear}`;
    }

    return front || rear || "";
  }

  return form.tireSize.trim();
}

function parseTireSetup(tireSize) {
  const staggeredMatch = /^Front:\s*(.*?)\s*\/\s*Rear:\s*(.*)$/i.exec(tireSize || "");

  if (!staggeredMatch) {
    return {
      tireSetup: "regular",
      tireSize: formatTireSizeInput(tireSize),
      frontTireSize: "",
      rearTireSize: ""
    };
  }

  return {
    tireSetup: "staggered",
    tireSize: "",
    frontTireSize: formatTireSizeInput(staggeredMatch[1]),
    rearTireSize: formatTireSizeInput(staggeredMatch[2])
  };
}

function App() {
  const isPublicBooking = window.location.pathname.startsWith("/booking");
  const isLoginRoute = window.location.pathname.startsWith("/login");
  const isCustomerSignupRoute = window.location.pathname.startsWith("/customer/signup");
  const [auth, setAuth] = useState(loadStoredAuth);
  const [authLoading, setAuthLoading] = useState(true);
  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [loginError, setLoginError] = useState("");
  const [loginSubmitting, setLoginSubmitting] = useState(false);
  const [signupForm, setSignupForm] = useState({ fullName: "", email: "", phone: "", password: "", confirmPassword: "", shopId: "", locationId: "" });
  const [signupError, setSignupError] = useState("");
  const [signupSubmitting, setSignupSubmitting] = useState(false);
  const [themeMode, setThemeMode] = useState(loadThemeMode);
  const [activeTab, setActiveTab] = useState(() => defaultTabForRole(loadStoredAuth()?.role));
  const [activeAccountingTab, setActiveAccountingTab] = useState("Dashboard");
  const [accountingRange, setAccountingRange] = useState(defaultAccountingRange);
  const [accountingLoading, setAccountingLoading] = useState(false);
  const [globalQuery, setGlobalQuery] = useState("");
  const [showNotifications, setShowNotifications] = useState(false);
  const [appNotifications, setAppNotifications] = useState([]);
  const previousAppUnreadCountRef = useRef(null);
  const [highlightedRow, setHighlightedRow] = useState(null);
  const [activityLog, setActivityLog] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("tiretrack-activity-log") || "[]");
    } catch {
      return [];
    }
  });
  const [companySettings, setCompanySettings] = useState(loadCompanySettings);
  const [dashboard, setDashboard] = useState(null);
  const [tires, setTires] = useState([]);
  const [inventoryTires, setInventoryTires] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [tireRequests, setTireRequests] = useState([]);
  const [workOrders, setWorkOrders] = useState([]);
  const [estimates, setEstimates] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [salesData, setSalesData] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [customerPortal, setCustomerPortal] = useState(null);
  const [accountingReport, setAccountingReport] = useState(null);
  const [accountingAccounts, setAccountingAccounts] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [platformShops, setPlatformShops] = useState([]);
  const [platformUsers, setPlatformUsers] = useState([]);
  const [platformLinkRecords, setPlatformLinkRecords] = useState([]);
  const [platformLinkLocations, setPlatformLinkLocations] = useState([]);
  const [shopLocations, setShopLocations] = useState([]);
  const [selectedLocationId, setSelectedLocationId] = useState("");
  const [expenseForm, setExpenseForm] = useState(emptyExpense);
  const [accountForm, setAccountForm] = useState(emptyAccount);
  const [vendorForm, setVendorForm] = useState(emptyVendor);
  const [accountingMessage, setAccountingMessage] = useState("");
  const [tireForm, setTireForm] = useState(emptyTire);
  const [tireFilters, setTireFilters] = useState(emptyTireFilters);
  const [appointmentForm, setAppointmentForm] = useState(emptyAppointment);
  const [editingAppointmentId, setEditingAppointmentId] = useState(null);
  const [invoiceForm, setInvoiceForm] = useState(() => makeInvoiceForm(loadCompanySettings()));
  const [generatedInvoice, setGeneratedInvoice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  function scrollToFeedback() {
    scrollPageToTop();
  }

  function notifySuccess(message) {
    setError("");
    setSuccessMessage(message);
    recordNotification("Change saved", message, activeTab, "SUCCESS");
    scrollToFeedback();
  }

  async function loadAccountingReportForRange(locationId = selectedLocationId, range = accountingRange) {
    const dates = accountingDatesForRange(range);
    return getAccountingReport(dates.start, dates.end, locationId);
  }

  function toggleThemeMode() {
    setThemeMode((current) => current === "light" ? "dark" : "light");
  }

  async function loadData(currentAuth = auth) {
    setLoading(true);
    setError("");

    try {
      let workingAuth = currentAuth;

      if (currentAuth?.token) {
        const currentProfile = await getCurrentUser().catch(() => null);

        if (currentProfile) {
          workingAuth = {
            ...currentAuth,
            id: currentProfile.id,
            fullName: currentProfile.fullName,
            email: currentProfile.email,
            role: currentProfile.role,
            shopId: currentProfile.shopId,
            shopName: currentProfile.shopName,
            subscriptionPlan: currentProfile.subscriptionPlan,
            multiLocationAllowed: Boolean(currentProfile.multiLocationAllowed),
            shopOwner: Boolean(currentProfile.shopOwner),
            locationId: currentProfile.locationId,
            locationName: currentProfile.locationName,
            accessibleLocationIds: currentProfile.accessibleLocationIds || currentAuth.accessibleLocationIds || [],
            permissions: currentProfile.permissions || currentAuth.permissions || []
          };
          setAuth(workingAuth);
          persistStoredAuth(workingAuth);
        }
      }

      if (workingAuth?.role === "CUSTOMER") {
        const portal = await getCustomerPortal();
        setCustomerPortal(portal);
        setLoading(false);
        return;
      }

      if (requiresShopAssignment(workingAuth?.role) && !workingAuth?.shopId) {
        setDashboard(null);
        setTires([]);
        setInventoryTires([]);
        setAppointments([]);
        setTireRequests([]);
        setWorkOrders([]);
        setEstimates([]);
        setInvoices([]);
        setSalesData([]);
        setCustomers([]);
        setAccountingReport(null);
        setAccountingAccounts([]);
        setVendors([]);
        setShopLocations([]);
        setSelectedLocationId("");
        setAppNotifications([]);
        setActivityLog([]);
        setError("No shop assigned to this user. Contact Monarch Solutions.");
        setLoading(false);
        return;
      }

      if (workingAuth?.role === "SUPER_ADMIN") {
        const [shops, users, linkOverview] = await Promise.all([
          getPlatformShops(),
          getPlatformUsers(),
          getPlatformLinks()
        ]);
        setPlatformShops(shops || []);
        setPlatformUsers(users || []);
        setPlatformLinkRecords(linkOverview?.records || []);
        setPlatformLinkLocations(linkOverview?.locations || []);
        setDashboard(null);
        setTires([]);
        setInventoryTires([]);
        setAppointments([]);
        setTireRequests([]);
        setWorkOrders([]);
        setEstimates([]);
        setInvoices([]);
        setSalesData([]);
        setCustomers([]);
        setAccountingReport(null);
        setAccountingAccounts([]);
        setVendors([]);
        setShopLocations([]);
        setSelectedLocationId("");
        setAppNotifications([]);
        setActivityLog([]);
        setCompanySettings(loadCompanySettings());
        return;
      }

      if (isShopManagerRole(workingAuth?.role)) {
        const multiLocationAllowed = canUseMultiLocationScope(workingAuth);
        const locationViewId = workingAuth?.locationId ? String(workingAuth.locationId) : multiLocationAllowed ? selectedLocationId : "";
        const [summary, tireList, appointmentList, tireRequestList, workOrderList, estimateList, invoiceList, salesList, savedSettings, auditLogs, customerList, accounting, accounts, vendorList, notifications, locations] = await Promise.all([
          getDashboard(locationViewId),
          getTires(),
          getAppointments(),
          getTireRequests().catch(() => []),
          getWorkOrders().catch(() => []),
          getEstimates().catch(() => []),
          getInvoices(),
          getSalesData(14, locationViewId),
          getSettings().catch(() => loadCompanySettings()),
          getAuditLogs().catch(() => []),
          getCustomers().catch(() => []),
          loadAccountingReportForRange(locationViewId, accountingRange).catch(() => null),
          getAccountingAccounts().catch(() => []),
          getVendors().catch(() => []),
          getNotifications().catch(() => []),
          multiLocationAllowed && workingAuth?.shopId ? getActiveShopLocations(workingAuth.shopId).catch(() => []) : Promise.resolve([])
        ]);

        const mergedSettings = { ...defaultCompanySettings, ...(savedSettings || {}) };
        const nextLocationId = workingAuth?.locationId
          ? String(workingAuth.locationId)
          : multiLocationAllowed && locationViewId && (locations || []).some((location) => String(location.id) === String(locationViewId))
            ? String(locationViewId)
            : "";
        setDashboard(summary);
        setTires(tireList || []);
        setInventoryTires(filterRecordsByLocation(tireList || [], nextLocationId));
        setAppointments(appointmentList || []);
        setTireRequests(tireRequestList || []);
        setWorkOrders(workOrderList || []);
        setEstimates(estimateList || []);
        setInvoices(invoiceList || []);
        setSalesData(salesList || []);
        setCustomers(customerList || []);
        setAccountingReport(accounting);
        setAccountingAccounts(accounts || []);
        setVendors(vendorList || []);
        setShopLocations(multiLocationAllowed ? locations || [] : []);
        setSelectedLocationId(nextLocationId);
        setAppNotifications(notifications || []);
        setActivityLog((auditLogs || []).map((log) => ({
          id: log.id,
          action: log.action,
          entityType: log.entityType,
          entityId: log.entityId,
          label: log.message || log.action,
          performedBy: log.performedBy,
          tab: log.entityType === "Tire" ? "Tires" : `${log.entityType || "Dashboard"}s`,
          createdAt: log.createdAt
        })));
        setCompanySettings(mergedSettings);
        localStorage.setItem("tiretrack-company-settings", JSON.stringify(mergedSettings));
      } else {
        const [tireList, appointmentList, tireRequestList, workOrderList, estimateList, invoiceList] = await Promise.all([
          getTires(),
          getAppointments(),
          getTireRequests().catch(() => []),
          getWorkOrders().catch(() => []),
          getEstimates().catch(() => []),
          getInvoices()
        ]);

        setDashboard(null);
        setTires(tireList || []);
        setInventoryTires(tireList || []);
        setAppointments(appointmentList || []);
        setTireRequests(tireRequestList || []);
        setWorkOrders(workOrderList || []);
        setEstimates(estimateList || []);
        setInvoices(invoiceList || []);
        setSalesData([]);
        setCustomers([]);
        setAccountingReport(null);
        setAccountingAccounts([]);
        setVendors([]);
        setShopLocations([]);
        setSelectedLocationId("");
        setAppNotifications([]);
        setCompanySettings(loadCompanySettings());
      }
    } catch (err) {
      setError(err.message);
      scrollToFeedback();
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    document.documentElement.dataset.theme = themeMode;
    localStorage.setItem("tiretrack-theme", themeMode);
  }, [themeMode]);

  useEffect(() => {
    async function initializeAuth() {
      const existingAuth = loadStoredAuth();
      if (existingAuth?.token) {
        setAuth(existingAuth);
        setActiveTab(defaultTabForRole(existingAuth.role));
        await loadData(existingAuth);
      } else {
        const refreshToken = localStorage.getItem("tiretrack-refresh-token");
        if (refreshToken) {
          try {
            const refreshed = await refreshTokenApi({ refreshToken });
            persistStoredAuth(refreshed);
            setAuth(refreshed);
            setActiveTab(defaultTabForRole(refreshed.role));
            await loadData(refreshed);
          } catch {
            clearStoredAuth();
          }
        }
      }
      setAuthLoading(false);
    }

    initializeAuth();
  }, []);

  useEffect(() => {
    function handleAuthUpdated(event) {
      const nextAuth = event.detail || loadStoredAuth();
      setAuth(nextAuth);
      setActiveTab((currentTab) => canAccessTab(nextAuth?.role, currentTab) ? currentTab : defaultTabForRole(nextAuth?.role));
    }

    function handleAuthCleared() {
      clearStoredAuth();
      setAuth(null);
      setActiveTab("Dashboard");
    }

    window.addEventListener("tiretrack-auth-updated", handleAuthUpdated);
    window.addEventListener("tiretrack-auth-cleared", handleAuthCleared);

    return () => {
      window.removeEventListener("tiretrack-auth-updated", handleAuthUpdated);
      window.removeEventListener("tiretrack-auth-cleared", handleAuthCleared);
    };
  }, []);

  useEffect(() => {
    if (auth && !canAccessTab(auth.role, activeTab)) {
      setActiveTab(defaultTabForRole(auth.role));
    }
  }, [activeTab, auth]);

  useEffect(() => {
    setError("");
    setSuccessMessage("");
    setAccountingMessage("");
  }, [activeTab, activeAccountingTab]);

  useEffect(() => {
    if (error || successMessage || accountingMessage) {
      scrollToFeedback();
    }
  }, [accountingMessage, error, successMessage]);

  async function handleLogin(event) {
    event.preventDefault();
    setLoginError("");
    setLoginSubmitting(true);

    try {
      const response = await loginApi(loginForm);
      persistStoredAuth(response);
      setAuth(response);
      setActiveTab(defaultTabForRole(response.role));
      if (window.location.pathname.startsWith("/login")) {
        window.location.href = "/";
        return;
      }
      await loadData(response);
    } catch (err) {
      setLoginError(err.message || "Invalid login credentials");
    } finally {
      setLoginSubmitting(false);
    }
  }

  async function handleCustomerSignup(event) {
    event.preventDefault();
    setSignupError("");

    if (signupForm.password !== signupForm.confirmPassword) {
      setSignupError("Passwords do not match.");
      return;
    }

    setSignupSubmitting(true);

    try {
      const response = await registerApi({
        ...signupForm,
        role: "CUSTOMER",
        shopId: signupForm.shopId ? Number(signupForm.shopId) : null,
        locationId: signupForm.locationId ? Number(signupForm.locationId) : null
      });
      persistStoredAuth(response);
      setAuth(response);
      setActiveTab("Portal");
      window.location.href = "/";
    } catch (err) {
      setSignupError(err.message || "Could not create customer account");
    } finally {
      setSignupSubmitting(false);
    }
  }

  function handleLogout() {
    logoutApi();
    clearStoredAuth();
    setAuth(null);
    setActiveTab("Dashboard");
  }

  const refreshCustomerPortal = useCallback(async () => {
    const portal = await getCustomerPortal();
    setCustomerPortal(portal);
  }, []);

  async function refreshCurrentData() {
    if (!auth) {
      return;
    }

    setIsRefreshing(true);
    setError("");
    setSuccessMessage("");

    try {
      if (auth.role === "CUSTOMER") {
        await refreshCustomerPortal();
      } else {
        await loadData(auth);
      }
      setSuccessMessage("Data refreshed.");
    } catch (err) {
      setError(err.message || "Could not refresh data.");
      scrollToFeedback();
    } finally {
      setIsRefreshing(false);
    }
  }

  async function saveCustomerVehicle(vehicle) {
    await createCustomerVehicle(vehicle);
    scrollToFeedback();
    await refreshCustomerPortal();
  }

  async function removeCustomerVehicle(id) {
    await deleteCustomerVehicle(id);
    await refreshCustomerPortal();
  }

  async function bookCustomerAppointment(appointment) {
    const savedAppointment = await createCustomerAppointment(appointment);
    scrollToFeedback();
    await refreshCustomerPortal();
    return savedAppointment;
  }

  async function payInvoiceFromPortal(id) {
    await payCustomerInvoice(id);
    scrollToFeedback();
    await refreshCustomerPortal();
  }

  async function approveEstimateFromPortal(id) {
    await approveCustomerEstimate(id);
    scrollToFeedback();
    await refreshCustomerPortal();
    await loadData(auth);
  }

  async function markPortalNoticeRead(id) {
    await markCustomerNotificationRead(id);
    await refreshCustomerPortal();
  }

  async function sendNoticeToCustomer(id, notice) {
    await sendCustomerNotice(id, notice);
    await recordNotification("Notice sent", notice.title || "Customer notice sent.", "Customers", "SUCCESS");
    const customerList = await getCustomers().catch(() => customers);
    setCustomers(customerList || []);
  }

  async function refreshAccounting() {
    const [accounting, accounts, vendorList] = await Promise.all([
      loadAccountingReportForRange(selectedLocationId, accountingRange).catch(() => null),
      getAccountingAccounts().catch(() => []),
      getVendors().catch(() => [])
    ]);
    setAccountingReport(accounting);
    setAccountingAccounts(accounts || []);
    setVendors(vendorList || []);
  }

  async function changeLocationView(locationId) {
    const nextLocationId = auth?.locationId ? String(auth.locationId) : String(locationId || "");
    setSelectedLocationId(nextLocationId);
    setInventoryTires(filterRecordsByLocation(tires, nextLocationId));
    setAppointmentForm((current) => ({ ...current, locationId: nextLocationId }));
    setInvoiceForm((current) => ({ ...current, locationId: nextLocationId }));
    setExpenseForm((current) => ({ ...current, locationId: nextLocationId }));

    try {
      const [summary, salesList, accounting] = await Promise.all([
        getDashboard(nextLocationId),
        getSalesData(14, nextLocationId),
        loadAccountingReportForRange(nextLocationId, accountingRange).catch(() => null)
      ]);
      setDashboard(summary);
      setSalesData(salesList || []);
      setAccountingReport(accounting);
    } catch (err) {
      setError(err.message);
      scrollToFeedback();
    }
  }

  async function refreshNotifications() {
    const notifications = await getNotifications().catch(() => []);
    setAppNotifications(notifications || []);
  }

  async function recordNotification(title, message, targetTab = activeTab, type = "INFO") {
    await createNotification({ title, message, targetTab, type }).catch(() => null);
    await refreshNotifications();
  }

  async function openNotification(notification) {
    if (!notification.read) {
      await markNotificationRead(notification.id).catch(() => null);
      await refreshNotifications();
    }
    setActiveTab(notification.targetTab || "Dashboard");
    setShowNotifications(false);
  }

  async function readAllNotifications() {
    await markAllNotificationsRead().catch(() => null);
    await refreshNotifications();
  }

  async function submitExpense(event) {
    event.preventDefault();
    setAccountingMessage("");
    setSuccessMessage("");
    await createExpense({
      ...expenseForm,
      vendorId: expenseForm.vendorId || null,
      expenseAccountId: expenseForm.expenseAccountId || null,
      locationId: expenseForm.locationId ? Number(expenseForm.locationId) : selectedLocationId ? Number(selectedLocationId) : null,
      category: resolveAccountingLabel(expenseForm.categoryKey, expenseForm.customCategory),
      paymentMethod: resolveAccountingLabel(expenseForm.paymentMethodKey, expenseForm.customPaymentMethod),
      subtotal: Number(expenseForm.subtotal || 0),
      taxAmount: Number(expenseForm.taxAmount || 0),
      total: calculateExpenseTotal(expenseForm)
    });
    setExpenseForm(emptyExpense);
    setAccountingMessage("Expense recorded and posted to the ledger.");
    recordNotification("Expense posted", "Expense recorded and posted to the ledger.", "Accounting", "SUCCESS");
    scrollToFeedback();
    await refreshAccounting();
  }

  async function submitAccountingAccount(event) {
    event.preventDefault();
    setAccountingMessage("");
    setSuccessMessage("");
    await createAccountingAccount(accountForm);
    setAccountForm(emptyAccount);
    setAccountingMessage("Account added to the chart of accounts.");
    recordNotification("Account added", "Account added to the chart of accounts.", "Accounting", "SUCCESS");
    scrollToFeedback();
    await refreshAccounting();
  }

  async function submitVendor(event) {
    event.preventDefault();
    setAccountingMessage("");
    setSuccessMessage("");
    await createVendor({
      ...vendorForm,
      category: resolveAccountingLabel(vendorForm.categoryKey, vendorForm.customCategory)
    });
    setVendorForm(emptyVendor);
    setAccountingMessage("Vendor added and ready for expense posting.");
    recordNotification("Vendor added", "Vendor added and ready for expense posting.", "Accounting", "SUCCESS");
    scrollToFeedback();
    await refreshAccounting();
  }

  async function payAccountingExpense(expense, paymentMethodKey = "CASH") {
    setAccountingMessage("");
    setSuccessMessage("");
    await payExpenseApi(expense.id, { paymentMethodKey });
    setAccountingMessage(`Expense #${expense.id} marked paid.`);
    recordNotification("Expense paid", `Expense #${expense.id} marked paid.`, "Accounting", "SUCCESS");
    scrollToFeedback();
    await refreshAccounting();
  }

  async function changeAccountingRange(nextRange) {
    setAccountingRange(nextRange);
    setAccountingLoading(true);
    setAccountingMessage("");

    try {
      const accounting = await loadAccountingReportForRange(selectedLocationId, nextRange);
      setAccountingReport(accounting);
    } catch (err) {
      setError(err.message || "Accounting report could not be loaded.");
      scrollToFeedback();
    } finally {
      setAccountingLoading(false);
    }
  }

  async function jumpToResult(result) {
    setActiveTab(result.tab);
    setGlobalQuery("");
    setHighlightedRow(result.id);
    window.setTimeout(() => setHighlightedRow(null), 2200);

    if (result.tab === "Invoices") {
      try {
        await previewInvoice(invoices.find((invoice) => Number(invoice.id) === Number(result.entityId)) || { id: result.entityId });
      } catch (err) {
        setError(err.message || "Invoice preview could not be loaded.");
      }
    }
  }

  function logActivity(label, tab) {
    const nextLog = [normalizeAuditLog({
      id: Date.now(),
      label,
      performedBy: auth?.email || auth?.fullName || "current-user",
      tab,
      createdAt: new Date().toISOString()
    }), ...activityLog].slice(0, 20);
    setActivityLog(nextLog);
    localStorage.setItem("tiretrack-activity-log", JSON.stringify(nextLog));
  }

  async function saveCompanySettings(nextSettings) {
    await updateSettings(nextSettings);
    const verifiedSettings = await getSettings();
    const mergedSettings = { ...defaultCompanySettings, ...(verifiedSettings || {}) };

    if (String(mergedSettings.shopName || "") !== String(nextSettings.shopName || "")) {
      throw new Error("Settings were not verified in the database. Try saving again.");
    }

    setCompanySettings(mergedSettings);
    localStorage.setItem("tiretrack-company-settings", JSON.stringify(mergedSettings));
    setInvoiceForm((current) => ({
      ...current,
      companyName: mergedSettings.shopName || defaultCompanySettings.shopName
    }));
    scrollToFeedback();
  }

  const visibleTires = useMemo(() => filterRecordsByLocation(tires, selectedLocationId), [tires, selectedLocationId]);
  const visibleAppointments = useMemo(() => filterRecordsByLocation(appointments, selectedLocationId), [appointments, selectedLocationId]);
  const visibleTireRequests = useMemo(() => filterRecordsByLocation(tireRequests, selectedLocationId), [tireRequests, selectedLocationId]);
  const visibleWorkOrders = useMemo(() => filterRecordsByLocation(workOrders, selectedLocationId), [workOrders, selectedLocationId]);
  const visibleEstimates = useMemo(() => filterRecordsByLocation(estimates, selectedLocationId), [estimates, selectedLocationId]);
  const visibleInvoices = useMemo(() => filterRecordsByLocation(invoices, selectedLocationId), [invoices, selectedLocationId]);
  const visibleCustomers = useMemo(() => filterRecordsByLocation(customers, selectedLocationId), [customers, selectedLocationId]);
  const locationScopeSummaries = useMemo(
    () => buildLocationScopeSummaries(shopLocations, tires, appointments, workOrders),
    [shopLocations, tires, appointments, workOrders]
  );
  const lowStockTires = useMemo(
    () => visibleTires.filter((tire) => Number(tire.quantity) <= 5),
    [visibleTires]
  );
  const activeAppointments = useMemo(
    () => {
      const paidAppointmentIds = new Set(
        visibleInvoices
          .filter((invoice) => invoiceStatusKey(invoice.status) === "PAID" && invoice.appointmentId)
          .map((invoice) => Number(invoice.appointmentId))
      );

      return visibleAppointments.filter((appointment) =>
        appointment.status !== "COMPLETED"
        && appointment.status !== "CANCELLED"
        && !paidAppointmentIds.has(Number(appointment.id))
      );
    },
    [visibleAppointments, visibleInvoices]
  );
  const globalSearchResults = useMemo(() => {
    const query = globalQuery.trim().toLowerCase();

    if (!query) {
      return [];
    }

    const tireMatches = tires
      .filter((tire) => [
        tire.brand,
        tire.model,
        tire.season,
        tire.condition,
        tire.locationName,
        tire.location,
        `${tire.width}/${tire.aspectRatio}r${tire.rimSize}`
      ].join(" ").toLowerCase().includes(query))
      .slice(0, 4)
      .map((tire) => ({
        id: `tire-${tire.id}`,
        entityId: tire.id,
        label: `${tire.brand} ${tire.model || ""}`,
        meta: `${tire.width}/${tire.aspectRatio}R${tire.rimSize} · ${tire.quantity} in stock`,
        tab: "Tires"
      }));
    const appointmentMatches = appointments
      .filter((appointment) => {
        const linkedCustomer = customerForAppointment(appointment, customers);

        return [
          appointment.customerName,
          appointment.email,
          linkedCustomer?.email,
          appointment.phone,
          linkedCustomer?.phone,
          appointment.vehicle,
          appointment.serviceType,
          appointment.status
        ].join(" ").toLowerCase().includes(query);
      })
      .slice(0, 4)
      .map((appointment) => ({
        id: `appointment-${appointment.id}`,
        entityId: appointment.id,
        label: appointment.customerName,
        meta: `${appointment.serviceType} · ${compactDate(appointment.appointmentDate)}`,
        tab: "Appointments"
      }));
    const workOrderMatches = workOrders
      .filter((workOrder) => [
        workOrder.customerName,
        workOrder.phone,
        workOrder.email,
        workOrder.vehicle,
        workOrder.serviceType,
        workOrder.status,
        workOrder.assignedEmployeeName,
        workOrder.invoiceId
      ].join(" ").toLowerCase().includes(query))
      .slice(0, 4)
      .map((workOrder) => ({
        id: `work-order-${workOrder.id}`,
        entityId: workOrder.id,
        label: workOrder.customerName,
        meta: `${workOrder.serviceType || "Service"} Â· ${workOrder.status || "PENDING"}`,
        tab: "Work Orders"
      }));
    const estimateMatches = estimates
      .filter((estimate) => [
        estimate.estimateNumber,
        estimate.customerName,
        estimate.phone,
        estimate.email,
        estimate.vehicle,
        estimate.status
      ].join(" ").toLowerCase().includes(query))
      .slice(0, 4)
      .map((estimate) => ({
        id: `estimate-${estimate.id}`,
        entityId: estimate.id,
        label: estimate.estimateNumber || `Estimate #${estimate.id}`,
        meta: `${estimate.customerName} Â· ${money(estimate.total)}`,
        tab: "Estimates"
      }));
    const customerMatches = customers
      .filter((customer) => [
        customer.fullName,
        customer.email,
        customer.phone
      ].join(" ").toLowerCase().includes(query))
      .slice(0, 4)
      .map((customer) => ({
        id: `customer-${customer.id}`,
        entityId: customer.id,
        label: customer.fullName,
        meta: [customer.email, customer.phone].filter(Boolean).join(" - "),
        tab: "Customers"
      }));
    const invoiceMatches = invoices
      .filter((invoice) => [
        invoiceNumber(invoice),
        invoice.invoiceNumber,
        invoice.id,
        invoice.customerName,
        invoice.phone,
        invoice.vehicle,
        invoice.status,
        statusLabel(invoiceDisplayStatus(invoice)),
        invoice.paymentMethod
      ].join(" ").toLowerCase().includes(query))
      .slice(0, 4)
      .map((invoice) => ({
        id: `invoice-${invoice.id}`,
        entityId: invoice.id,
        label: invoiceNumber(invoice),
        meta: `${invoice.customerName} - ${money(invoice.total)} - ${statusLabel(invoiceDisplayStatus(invoice))}`,
        tab: "Invoices"
      }));

    return [...customerMatches, ...tireMatches, ...appointmentMatches, ...workOrderMatches, ...estimateMatches, ...invoiceMatches].slice(0, 8);
  }, [appointments, customers, estimates, globalQuery, invoices, tires, workOrders]);
  const visibleTabs = tabs.filter((tab) => canAccessTab(auth?.role, tab));

  const ignoredDerivedNotifications = useMemo(() => {
    const today = todayDateKey();
    const todaysAppointments = activeAppointments.filter((appointment) => appointmentDateKey(appointment.appointmentDate) === today);
    const urgentLowStock = lowStockTires.filter((tire) => Number(tire.quantity || 0) < 3);

    return [
      ...urgentLowStock.slice(0, 4).map((tire) => ({
        id: `stock-${tire.id}`,
        label: `${tire.brand} needs refill`,
        meta: `${tire.width}/${tire.aspectRatio}R${tire.rimSize} · ${tire.quantity} left`,
        tab: "Tires"
      })),
      ...todaysAppointments.slice(0, 4).map((appointment) => ({
        id: `today-${appointment.id}`,
        label: `${appointment.customerName} today`,
        meta: `${appointmentTimeKey(appointment.appointmentDate)} · ${appointment.serviceType}`,
        tab: "Appointments"
      }))
    ];
  }, [activeAppointments, lowStockTires]);
  const unreadNotifications = appNotifications.filter((notification) => !notification.read);

  useEffect(() => {
    if (!auth || auth.role === "CUSTOMER") {
      previousAppUnreadCountRef.current = null;
      return;
    }

    const unreadCount = unreadNotifications.length;
    if ((previousAppUnreadCountRef.current === null && unreadCount > 0)
        || (previousAppUnreadCountRef.current !== null && unreadCount > previousAppUnreadCountRef.current)) {
      playNotificationChime();
    }
    previousAppUnreadCountRef.current = unreadCount;
  }, [auth, unreadNotifications.length]);

  async function submitTire(event) {
    event.preventDefault();
    setError("");
    setSuccessMessage("");

    const tirePayload = {
      ...tireForm,
      width: Number(tireForm.width),
      aspectRatio: Number(tireForm.aspectRatio),
      rimSize: Number(tireForm.rimSize),
      quantity: Number(tireForm.quantity),
      price: Number(tireForm.price),
      shopId: tireForm.shopId ? Number(tireForm.shopId) : null,
      locationId: tireForm.locationId ? Number(tireForm.locationId) : null
    };
    const matchingTire = tires.find((tire) =>
      tire.brand.trim().toLowerCase() === tirePayload.brand.trim().toLowerCase()
      && Number(tire.width) === tirePayload.width
      && Number(tire.aspectRatio) === tirePayload.aspectRatio
      && Number(tire.rimSize) === tirePayload.rimSize
      && String(tire.condition || "") === String(tirePayload.condition || "")
    );

    try {
      if (matchingTire) {
        await updateTire(matchingTire.id, {
          ...matchingTire,
          ...tirePayload,
          quantity: Number(matchingTire.quantity || 0) + tirePayload.quantity
        });
      } else {
        await createTire(tirePayload);
      }

      setTireForm(emptyTire);
      logActivity(matchingTire ? `Refilled ${tirePayload.brand}` : `Added ${tirePayload.brand}`, "Tires");
      notifySuccess(matchingTire ? `${tirePayload.brand} inventory refilled.` : `${tirePayload.brand} added to inventory.`);
      await loadData();
    } catch (err) {
      setError(err.message);
      scrollToFeedback();
    }
  }

  async function uploadTireCsv(file) {
    setError("");
    setSuccessMessage("");

    try {
      const result = await importTiresCsv(file);
      await loadData();
      notifySuccess(`CSV imported: ${result.createdCount || 0} created, ${result.updatedCount || 0} refilled, ${result.skippedCount || 0} skipped.`);
      return result;
    } catch (err) {
      setError(err.message);
      scrollToFeedback();
      throw err;
    }
  }

  async function createInventoryLocation(location) {
    if (!auth?.shopId) {
      throw new Error("Your account is not assigned to a shop yet.");
    }

    if (!canUseMultiLocationScope(auth)) {
      throw new Error("Multi-location support requires a Premium plan and a shop owner account.");
    }

    await createShopLocation({
      ...location,
      shopId: auth.shopId,
      active: true
    });

    notifySuccess(`${location.name} location created.`);
    await loadData();
  }

  async function assignPlatformLink(record, assignment) {
    setError("");
    setSuccessMessage("");

    const updatedRecord = await assignPlatformRecord(record.type, record.id, assignment);
    setPlatformLinkRecords((current) => current.map((entry) =>
      entry.type === updatedRecord.type && Number(entry.id) === Number(updatedRecord.id) ? updatedRecord : entry
    ));
    notifySuccess(`${record.label} linked to ${updatedRecord.shopName || "Unassigned"}.`);
    return updatedRecord;
  }

  async function assignLegacyShopData(shopId) {
    setError("");
    setSuccessMessage("");

    const result = await assignLegacyDataToShop(shopId);
    await loadData(auth);
    notifySuccess(result?.message || "Legacy data linked to shop.");
    return result;
  }

  async function assignPlatformAdminShop(userId, shopId) {
    setError("");
    setSuccessMessage("");

    if (!shopId) {
      throw new Error("Choose a shop before assigning an admin.");
    }

    const updatedAdmin = await assignAdminToShop(userId, shopId);
    const updatedRecord = {
      type: "USER",
      id: updatedAdmin.id,
      label: updatedAdmin.fullName,
      detail: [updatedAdmin.email, updatedAdmin.phone].filter(Boolean).join(" / "),
      status: updatedAdmin.role,
      shopId: updatedAdmin.shopId,
      shopName: updatedAdmin.shopName,
      locationId: null,
      locationName: null
    };

    setPlatformLinkRecords((current) => current.map((entry) =>
      entry.type === "USER" && Number(entry.id) === Number(updatedAdmin.id) ? updatedRecord : entry
    ));
    await loadData(auth);
    notifySuccess(`${updatedAdmin.fullName} assigned to ${updatedAdmin.shopName}.`);
    return updatedAdmin;
  }

  async function assignPlatformAdminLocation(userId, locationId) {
    setError("");
    setSuccessMessage("");

    if (!locationId) {
      throw new Error("Choose a location before assigning a location admin.");
    }

    const updatedAdmin = await assignAdminToLocation(userId, locationId);
    const updatedRecord = {
      type: "USER",
      id: updatedAdmin.id,
      label: updatedAdmin.fullName,
      detail: [updatedAdmin.email, updatedAdmin.phone].filter(Boolean).join(" / "),
      status: updatedAdmin.role,
      shopId: updatedAdmin.shopId,
      shopName: updatedAdmin.shopName,
      locationId: updatedAdmin.locationId,
      locationName: updatedAdmin.locationName
    };

    setPlatformLinkRecords((current) => current.map((entry) =>
      entry.type === "USER" && Number(entry.id) === Number(updatedAdmin.id) ? updatedRecord : entry
    ));
    await loadData(auth);
    notifySuccess(`${updatedAdmin.fullName} assigned to ${updatedAdmin.locationName}.`);
    return updatedAdmin;
  }

  async function createPlatformLocation(location) {
    await createShopLocation({
      ...location,
      active: true
    });

    notifySuccess(`${location.name} location created.`);
    await loadData(auth);
  }

  async function createOwnerPlatformUser(user) {
    setError("");
    setSuccessMessage("");

    const createdUser = await createPlatformUser(user);
    notifySuccess(`${createdUser.fullName} created.`);
    await loadData(auth);
    return createdUser;
  }

  async function setPlatformUserActive(user, active) {
    setError("");
    setSuccessMessage("");

    const updatedUser = active
      ? await activatePlatformUser(user.id)
      : await deactivatePlatformUser(user.id);

    notifySuccess(`${updatedUser.fullName} ${active ? "activated" : "deactivated"}.`);
    await loadData(auth);
    return updatedUser;
  }

  async function applyTireFilters(event) {
    event.preventDefault();
    setError("");

    try {
      let results = [];

      if (tireFilters.type === "brand") {
        results = await searchTiresByBrand(tireFilters.query);
      } else if (tireFilters.type === "season") {
        results = await searchTiresBySeason(tireFilters.query);
      } else if (tireFilters.type === "location") {
        results = await searchTiresByLocation(tireFilters.query);
      } else if (tireFilters.type === "condition") {
        results = await searchTiresByCondition(tireFilters.condition);
      } else if (tireFilters.type === "size") {
        results = await searchTiresBySize({
          width: tireFilters.width,
          aspectRatio: tireFilters.aspectRatio,
          rimSize: tireFilters.rimSize
        });
      } else if (tireFilters.type === "low-stock") {
        results = await getLowStockTires(tireFilters.threshold);
      }

      setInventoryTires(results || []);
    } catch (err) {
      setError(err.message);
    }
  }

  function clearTireFilters() {
    setTireFilters(emptyTireFilters);
    setInventoryTires(tires);
  }

  async function submitAppointment(event) {
    event.preventDefault();
    setError("");
    setSuccessMessage("");

    if (appointmentForm.reminderStatus === "SCHEDULED" && !appointmentForm.reminderAt) {
      setError("Choose a reminder date and time, or set Reminder to No reminder.");
      scrollToFeedback();
      return;
    }

    const reminderAt = appointmentForm.reminderAt || null;
    const reminderStatus = reminderAt
      ? appointmentForm.reminderStatus === "SENT" ? "SENT" : "SCHEDULED"
      : "NOT_SET";

    const appointment = {
      customerId: appointmentForm.customerId ? Number(appointmentForm.customerId) : null,
      customerVehicleId: appointmentForm.customerVehicleId ? Number(appointmentForm.customerVehicleId) : null,
      customerName: appointmentForm.customerName,
      email: appointmentForm.email,
      phone: appointmentForm.phone,
      vehicle: appointmentForm.vehicle,
      tireSize: buildTireSize(appointmentForm),
      frontTireId: usesInventoryTires(appointmentForm.serviceType) && appointmentForm.frontTireId ? Number(appointmentForm.frontTireId) : null,
      frontQuantity: usesInventoryTires(appointmentForm.serviceType) ? Number(appointmentForm.frontQuantity || 0) : 0,
      rearTireId: usesInventoryTires(appointmentForm.serviceType) && appointmentForm.tireSetup === "staggered" && appointmentForm.rearTireId ? Number(appointmentForm.rearTireId) : null,
      rearQuantity: usesInventoryTires(appointmentForm.serviceType) && appointmentForm.tireSetup === "staggered" ? Number(appointmentForm.rearQuantity || 0) : 0,
      appointmentDate: appointmentForm.appointmentDate,
      locationId: appointmentForm.locationId ? Number(appointmentForm.locationId) : selectedLocationId ? Number(selectedLocationId) : null,
      serviceType: appointmentForm.serviceType,
      notes: appointmentForm.notes,
      overrideTireAvailability: Boolean(appointmentForm.overrideTireAvailability),
      tireAvailabilityOverrideReason: appointmentForm.tireAvailabilityOverrideReason || "",
      reminderStatus,
      reminderAt,
      confirmationStatus: appointmentForm.confirmationStatus,
      cancelReason: appointmentForm.cancelReason,
      status: appointmentForm.status
    };

    if (usesInventoryTires(appointmentForm.serviceType) && !appointment.frontTireId && !appointment.customerVehicleId && !appointment.tireSize) {
      setError("Select an inventory tire or saved customer vehicle for this installation appointment.");
      scrollToFeedback();
      return;
    }

    const selectedDate = appointmentDateKey(appointment.appointmentDate);
    const selectedTime = appointmentTimeKey(appointment.appointmentDate);

    if (!selectedDate || selectedDate < todayDateKey()) {
      setError("Choose today or a future date for this appointment.");
      scrollToFeedback();
      return;
    }

    if (!selectedTime) {
      setError("Choose an appointment time.");
      scrollToFeedback();
      return;
    }

    const matchingAppointment = appointments.find((existingAppointment) =>
      Number(existingAppointment.id) !== Number(editingAppointmentId)
      && isBookableAppointment(existingAppointment)
      && sameLocationValue(existingAppointment.locationId, appointment.locationId)
      && appointmentDateKey(existingAppointment.appointmentDate) === selectedDate
      && appointmentTimeKey(existingAppointment.appointmentDate) === selectedTime
    );

    if (matchingAppointment) {
      setError(`That time is already booked for ${matchingAppointment.customerName}. Pick another slot.`);
      scrollToFeedback();
      return;
    }

    try {
      if (editingAppointmentId) {
        await updateAppointment(editingAppointmentId, appointment);
      } else {
        await createAppointment(appointment);
      }

      setAppointmentForm(emptyAppointment);
      setEditingAppointmentId(null);
      logActivity(`${editingAppointmentId ? "Updated" : "Booked"} appointment for ${appointment.customerName}`, "Appointments");
      notifySuccess(`${editingAppointmentId ? "Appointment updated" : "Appointment booked"} for ${appointment.customerName}.`);
      await loadData();
    } catch (err) {
      setError(err.message);
      scrollToFeedback();
    }
  }

  function editAppointment(appointment) {
    const tireSetup = parseTireSetup(appointment.tireSize);

    setActiveTab("Appointments");
    setEditingAppointmentId(appointment.id);
    setAppointmentForm({
      customerName: appointment.customerName || "",
      customerId: appointment.customerId ? String(appointment.customerId) : "",
      customerVehicleId: "",
      email: appointment.email || "",
      phone: appointment.phone || "",
      vehicle: appointment.vehicle || "",
      ...tireSetup,
      frontTireId: appointment.frontTireId ? String(appointment.frontTireId) : "",
      frontQuantity: appointment.frontQuantity || 0,
      rearTireId: appointment.rearTireId ? String(appointment.rearTireId) : "",
      rearQuantity: appointment.rearQuantity || 0,
      appointmentDate: appointment.appointmentDate || "",
      locationId: appointment.locationId ? String(appointment.locationId) : selectedLocationId,
      serviceType: appointment.serviceType || "INSTALLATION",
      notes: appointment.notes || "",
      overrideTireAvailability: false,
      tireAvailabilityOverrideReason: "",
      reminderStatus: appointment.reminderStatus || "NOT_SET",
      reminderAt: toDateTimeLocalValue(appointment.reminderAt),
      confirmationStatus: appointment.confirmationStatus || "PENDING",
      cancelReason: appointment.cancelReason || "",
      status: appointment.status || "BOOKED"
    });
  }

  function cancelAppointmentEdit() {
    setEditingAppointmentId(null);
    setAppointmentForm(emptyAppointment);
  }

  async function submitInvoice(event) {
    event.preventDefault();
    setError("");
    setSuccessMessage("");

    const items = invoiceForm.items.map((item) => ({
      ...item,
      tireId: item.tireId ? Number(item.tireId) : null,
      itemName: item.itemName?.trim() || (item.itemType === "SERVICE" ? "Service" : ""),
      quantity: Number(item.quantity || 1),
      unitPrice: item.unitPrice === "" ? null : Number(item.unitPrice || 0)
    }));
    const invalidTireItem = items.find((item) => item.itemType === "TIRE" && !item.tireId);

    if (invalidTireItem) {
      setError("Select an inventory tire for every tire line on the invoice.");
      scrollToFeedback();
      return;
    }

    try {
      if (invoiceStatusKey(invoiceForm.status) === "PARTIALLY_PAID" && !invoiceForm.dueDate) {
        setError("Choose a due date for the remaining partial-payment balance.");
        scrollToFeedback();
        return;
      }

      const invoicePayload = {
        ...invoiceForm,
        companyName: undefined,
        estimateId: undefined,
        taxRate: Number(companySettings.taxRate || 13),
        amountPaid: invoiceForm.amountPaid === "" ? null : Number(invoiceForm.amountPaid || 0),
        dueDate: invoiceForm.dueDate || null,
        appointmentId: invoiceForm.appointmentId ? Number(invoiceForm.appointmentId) : null,
        locationId: invoiceForm.locationId ? Number(invoiceForm.locationId) : selectedLocationId ? Number(selectedLocationId) : null,
        items
      };
      const savedInvoice = invoiceForm.estimateId
        ? await convertEstimateToInvoice(invoiceForm.estimateId, invoicePayload)
        : await createInvoice(invoicePayload);
      const printableInvoice = savedInvoice?.id ? await getInvoice(savedInvoice.id) : savedInvoice;

      setGeneratedInvoice(printableInvoice);
      logActivity(`${invoiceForm.estimateId ? "Converted estimate into" : "Created"} invoice for ${invoiceForm.customerName}`, "Invoices");
      setInvoiceForm(makeInvoiceForm(companySettings));
      notifySuccess(`Invoice ${invoiceNumber(printableInvoice)} created for ${invoiceForm.customerName}.`);
      await loadData();
    } catch (err) {
      setError(err.message);
      scrollToFeedback();
    }
  }

  async function removeTire(id) {
    await deleteTire(id);
    notifySuccess("Tire deleted.");
    await loadData();
  }

  function refillTire(tire) {
    setActiveTab("Tires");
    setTireForm({
      brand: tire.brand || "",
      model: tire.model || "",
      width: tire.width,
      aspectRatio: tire.aspectRatio,
      rimSize: tire.rimSize,
      season: tire.season || "All Season",
      condition: tire.condition || "NEW",
      quantity: 4,
      price: tire.price ?? "0.00",
      location: tire.location || "",
      shopId: tire.shopId ? String(tire.shopId) : auth?.shopId ? String(auth.shopId) : "",
      locationId: tire.locationId ? String(tire.locationId) : ""
    });
    notifySuccess("Refill details loaded. Review quantity and save.");
  }

  async function removeAppointment(id) {
    await deleteAppointment(id);
    notifySuccess("Appointment deleted.");
    await loadData();
  }

  async function cancelAppointment(appointment) {
    await updateAppointment(appointment.id, {
      customerName: appointment.customerName,
      customerId: appointment.customerId || null,
      email: appointment.email || "",
      phone: appointment.phone,
      vehicle: appointment.vehicle,
      tireSize: appointment.tireSize || "",
      frontTireId: appointment.frontTireId || null,
      frontQuantity: appointment.frontQuantity || 0,
      rearTireId: appointment.rearTireId || null,
      rearQuantity: appointment.rearQuantity || 0,
      appointmentDate: appointment.appointmentDate,
      serviceType: appointment.serviceType,
      notes: appointment.notes || "",
      status: "CANCELLED"
    });
    notifySuccess(`Appointment cancelled for ${appointment.customerName}.`);
    await loadData();
  }

  async function changeTireRequestStatus(request, status, adminResponse = "") {
    try {
      await updateTireRequestStatus(request.id, status, adminResponse);
      notifySuccess(`Tire request marked ${tireRequestStatusLabel(status).toLowerCase()}.`);
      await loadData(auth);
    } catch (err) {
      setError(err.message || "Tire request could not be updated.");
      scrollToFeedback();
    }
  }

  async function confirmTireRequest(request) {
    try {
      await confirmTireRequestAppointment(request.id);
      notifySuccess("Appointment confirmed and tire request fulfilled.");
      await loadData(auth);
    } catch (err) {
      setError(err.message || "Appointment could not be confirmed from this tire request.");
      scrollToFeedback();
    }
  }

  async function removeInvoice(id) {
    await deleteInvoice(id);
    notifySuccess("Invoice deleted.");
    await loadData();
  }

  async function markInvoicePaid(invoice) {
    await updateInvoiceStatus(invoice.id, {
      status: "PAID",
      paymentMethod: invoice.paymentMethod || "Manual"
    });
    logActivity(`Marked invoice ${invoiceNumber(invoice)} paid`, "Invoices");
    notifySuccess(`Invoice ${invoiceNumber(invoice)} marked paid.`);
    await loadData();
  }

  async function previewInvoice(invoice) {
    const printableInvoice = invoice?.id ? await getInvoice(invoice.id) : invoice;
    setGeneratedInvoice(printableInvoice);
  }

  async function openGeneratedInvoice(invoice) {
    const printableInvoice = invoice?.id ? await getInvoice(invoice.id) : invoice;
    setGeneratedInvoice(printableInvoice);
    setActiveTab("Invoices");
  }

  async function updateInvoiceLifecycleStatus(invoice, status) {
    const payload = { status };

    if (invoiceStatusKey(status) === "PARTIALLY_PAID") {
      const defaultPaid = invoice.amountPaid || "";
      const paidInput = window.prompt("Amount paid so far", defaultPaid);

      if (paidInput === null) {
        return;
      }

      payload.amountPaid = Number(paidInput || 0);

      const dueInput = window.prompt("Remaining balance due date (YYYY-MM-DD)", invoice.dueDate || "");
      if (dueInput === null) {
        return;
      }

      if (!dueInput.trim()) {
        setError("Choose a due date for the remaining partial-payment balance.");
        scrollToFeedback();
        return;
      }

      payload.dueDate = dueInput.trim();
      payload.paymentMethod = invoice.paymentMethod || "Manual";
    }

    try {
      await updateInvoiceStatus(invoice.id, payload);
      logActivity(`Marked invoice ${invoiceNumber(invoice)} ${statusLabel(status)}`, "Invoices");
      notifySuccess(`Invoice ${invoiceNumber(invoice)} marked ${statusLabel(status)}.`);
      await loadData();
    } catch (err) {
      setError(err.message);
      scrollToFeedback();
    }
  }

  function startInvoiceFromAppointment(appointment) {
    const items = [];

    if (appointment.frontTireId) {
      items.push(invoiceItemFromTire(appointment.frontTireId, appointment.frontQuantity, tires));
    }

    if (appointment.rearTireId) {
      const existingItem = items.find((item) => String(item.tireId) === String(appointment.rearTireId));

      if (existingItem) {
        existingItem.quantity = Number(existingItem.quantity || 0) + Number(appointment.rearQuantity || 0);
      } else {
        items.push(invoiceItemFromTire(appointment.rearTireId, appointment.rearQuantity, tires));
      }
    }

    setGeneratedInvoice(null);
    const nextInvoiceForm = makeInvoiceForm(companySettings);
    setInvoiceForm({
      ...nextInvoiceForm,
      appointmentId: String(appointment.id),
      customerName: appointment.customerName || "",
      email: appointment.email || "",
      phone: appointment.phone || "",
      vehicle: appointment.vehicle || "",
      items: items.length ? items : nextInvoiceForm.items
    });
    setActiveTab("Invoices");
  }

  function startInvoiceFromEstimate(estimate) {
    const nextInvoiceForm = makeInvoiceForm(companySettings);
    setGeneratedInvoice(null);
    setInvoiceForm({
      ...nextInvoiceForm,
      estimateId: String(estimate.id),
      customerName: estimate.customerName || "",
      phone: estimate.phone || "",
      vehicle: estimate.vehicle || "",
      locationId: estimate.locationId ? String(estimate.locationId) : selectedLocationId,
      status: "UNPAID",
      paymentMethod: "Cash",
      items: (estimate.items || []).length ? estimate.items.map((item) => ({
        tireId: item.tireId ? String(item.tireId) : "",
        itemType: item.itemType || "SERVICE",
        itemName: item.itemName || "",
        quantity: item.quantity || 1,
        unitPrice: String(item.unitPrice ?? "0.00")
      })) : nextInvoiceForm.items
    });
    setActiveTab("Invoices");
    notifySuccess(`Estimate ${estimate.estimateNumber || `#${estimate.id}`} loaded into the invoice editor.`);
  }

  if (isPublicBooking && loadStoredAuth()?.role === "SUPER_ADMIN") {
    window.location.href = "/";
    return null;
  }

  if (isPublicBooking) {
    return <PublicBookingPage onToggleTheme={toggleThemeMode} themeMode={themeMode} />;
  }

  if (isCustomerSignupRoute) {
    return (
      <CustomerSignupScreen
        error={signupError}
        form={signupForm}
        isSubmitting={signupSubmitting}
        onToggleTheme={toggleThemeMode}
        onSubmit={handleCustomerSignup}
        setForm={setSignupForm}
        themeMode={themeMode}
      />
    );
  }

  if (isLoginRoute) {
    return (
      <LoginScreen
        onSubmit={handleLogin}
        loginForm={loginForm}
        setLoginForm={setLoginForm}
        error={loginError}
        isSubmitting={loginSubmitting}
        onToggleTheme={toggleThemeMode}
        themeMode={themeMode}
      />
    );
  }

  if (authLoading) {
    return (
      <main className="login-shell">
        <ThemeToggleButton className="theme-floating-toggle" onToggle={toggleThemeMode} themeMode={themeMode} />
        <motion.section
          animate={{ opacity: 1, y: 0 }}
          className="login-panel"
          initial={{ opacity: 0, y: 16 }}
          transition={{ duration: 0.4 }}
        >
          <p className="loading-state">Checking session...</p>
        </motion.section>
      </main>
    );
  }

  if (!auth) {
    return (
      <LoginScreen
        onSubmit={handleLogin}
        loginForm={loginForm}
        setLoginForm={setLoginForm}
        error={loginError}
        isSubmitting={loginSubmitting}
        onToggleTheme={toggleThemeMode}
        themeMode={themeMode}
      />
    );
  }

  if (auth.role === "CUSTOMER") {
    return (
      <CustomerPortalShell
        auth={auth}
        onBookAppointment={bookCustomerAppointment}
        onDeleteVehicle={removeCustomerVehicle}
        onLogout={handleLogout}
        onMarkNoticeRead={markPortalNoticeRead}
        onApproveEstimate={approveEstimateFromPortal}
        onPayInvoice={payInvoiceFromPortal}
        isRefreshing={isRefreshing}
        onRefresh={refreshCurrentData}
        onSaveVehicle={saveCustomerVehicle}
        onToggleTheme={toggleThemeMode}
        portal={customerPortal}
        themeMode={themeMode}
      />
    );
  }

  const tenantBlocked = requiresShopAssignment(auth?.role) && !auth?.shopId;

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <motion.div
          animate={{ opacity: 1, y: 0 }}
          className="brand-lockup"
          initial={{ opacity: 0, y: 10 }}
          transition={{ duration: 0.35 }}
        >
          <span className="brand-mark"><Disc3 size={22} /></span>
          <h1>TireTrack</h1>
          <p>Inventory, service, and sales</p>
        </motion.div>

        <nav className="tabs" aria-label="Main navigation">
          {visibleTabs.map((tab) => {
            const Icon = tabIcons[tab];
            const isAccounting = tab === "Accounting";

            return (
              <div className={isAccounting ? "nav-group" : ""} key={tab}>
                <motion.button
                  className={activeTab === tab ? "active" : ""}
                  onClick={() => setActiveTab(tab)}
                  whileHover={{ x: 3 }}
                  type="button"
                >
                  {Icon ? <Icon size={18} /> : null}
                  {tab}
                </motion.button>
                {isAccounting && activeTab === "Accounting" && (
                  <div className="sidebar-subnav" aria-label="Accounting sections">
                    {accountingTabs.map((section) => (
                      <button className={activeAccountingTab === section ? "active" : ""} key={section} onClick={() => setActiveAccountingTab(section)} type="button">
                        <span>{section}</span>
                        <small>{accountingTabMeta[section]?.nav}</small>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </nav>

        {auth?.role !== "SUPER_ADMIN" && (
          <button className="sidebar-booking-link with-icon" onClick={() => { window.location.href = "/booking"; }} type="button">
            <CalendarDays size={17} />
            Public Booking
          </button>
        )}
        <div className="monarch-footer">
          <strong>Powered by Monarch Solutions</strong>
          <a href="mailto:support@monarchsolutions.ca">support@monarchsolutions.ca</a>
        </div>
      </aside>

      <main className="content">
        <header className="topbar">
          <div>
            <span className="eyebrow">{activeTab}</span>
            <h2>{activeTab}</h2>
          </div>
          <div className="topbar-actions">
            <div className="global-search">
              <Search size={16} />
              <input
                aria-label="Search tires, appointments, invoices"
                onChange={(event) => setGlobalQuery(event.target.value)}
                placeholder="Search tires, customers, invoices"
                value={globalQuery}
              />
              {globalQuery.trim() && (
                <div className="global-results">
                  {globalSearchResults.length === 0 ? (
                    <span className="global-empty">No matches</span>
                  ) : (
                    globalSearchResults.map((result) => (
                      <button
                        key={result.id}
                        onClick={() => jumpToResult(result)}
                        type="button"
                      >
                        <strong>{result.label}</strong>
                        <small>{result.tab} · {result.meta}</small>
                      </button>
                    ))
                  )}
                </div>
              )}
            </div>
            <div className="notification-wrap">
              <button
                className={`icon-button notification-button${unreadNotifications.length > 0 ? " has-unread" : ""}`}
                aria-label="Notifications"
                onClick={() => {
                  if (unreadNotifications.length > 0) {
                    playNotificationChime();
                  }
                  setShowNotifications((current) => !current);
                }}
                type="button"
              >
                <Bell size={18} />
                {unreadNotifications.length > 0 && <span>{unreadNotifications.length}</span>}
              </button>
              {showNotifications && (
                <div className="notification-menu">
                  <div className="notification-menu-head">
                    <strong>Notifications</strong>
                    {unreadNotifications.length > 0 && (
                      <button onClick={readAllNotifications} type="button">Mark all read</button>
                    )}
                  </div>
                  {appNotifications.length === 0 ? (
                    <p>No notifications yet.</p>
                  ) : (
                    appNotifications.map((notification) => (
                      <button
                        className={notification.read ? "read" : "unread"}
                        key={notification.id}
                        onClick={() => openNotification(notification)}
                        type="button"
                      >
                        <span>{notification.title}</span>
                        <small>{notification.message}</small>
                      </button>
                    ))
                  )}
                </div>
              )}
            </div>
            <button className="ghost-button with-icon" disabled={isRefreshing || loading} onClick={refreshCurrentData} type="button">
              <RefreshCw size={16} />
              {isRefreshing ? "Refreshing..." : "Refresh"}
            </button>
            <ThemeToggleButton onToggle={toggleThemeMode} themeMode={themeMode} />
            {auth?.role !== "SUPER_ADMIN" && (
              <button className="ghost-button with-icon" onClick={() => { window.location.href = "/booking"; }} type="button">
                <CalendarDays size={16} />
                Public Booking
              </button>
            )}
            <button className="profile-chip with-icon" onClick={handleLogout} type="button" title="Log out">
              <UserCircle size={19} />
              <span className="profile-chip-text">
                <strong>{auth?.fullName || "Log out"}</strong>
                {auth?.role !== "CUSTOMER" && <small>{[auth?.shopName, auth?.locationName].filter(Boolean).join(" / ") || auth?.role || "Unassigned"}</small>}
              </span>
            </button>
          </div>
        </header>

        {successMessage && <div className="success-alert">{successMessage}</div>}
        {error && <div className="alert">{error}</div>}
        {loading ? <DashboardSkeleton /> : null}

        {!loading && tenantBlocked && <TenantAssignmentWarning auth={auth} />}

        {!tenantBlocked && !loading && canUseMultiLocationScope(auth) && (
          <LocationScopeBar
            auth={auth}
            locations={shopLocations}
            summaries={locationScopeSummaries}
            onChange={changeLocationView}
            value={selectedLocationId}
          />
        )}

        {!tenantBlocked && !loading && activeTab === "Platform" && auth?.role === "SUPER_ADMIN" && (
          <PlatformPage
            linkLocations={platformLinkLocations}
            linkRecords={platformLinkRecords}
            onAssignAdmin={assignPlatformAdminShop}
            onAssignLegacyData={assignLegacyShopData}
            onAssignLocation={assignPlatformAdminLocation}
            onAssignRecord={assignPlatformLink}
            onCreateLocation={createPlatformLocation}
            onCreateUser={createOwnerPlatformUser}
            onRefresh={() => loadData(auth)}
            onSetUserActive={setPlatformUserActive}
            setError={setError}
            shops={platformShops}
            users={platformUsers}
          />
        )}

        {!tenantBlocked && !loading && activeTab === "Employee Portal" && auth?.role === "EMPLOYEE" && (
          <EmployeePortal auth={auth} onOpenPayroll={() => setActiveTab("My Payroll")} />
        )}

        {!tenantBlocked && !loading && activeTab === "Dashboard" && (
          <Dashboard
            auth={auth}
            appointments={activeAppointments}
            customers={visibleCustomers}
            dashboard={dashboard}
            invoices={visibleInvoices}
            workOrders={visibleWorkOrders}
            tires={visibleTires}
            lowStockTires={lowStockTires}
            onCancelAppointment={cancelAppointment}
            onDeleteAppointment={removeAppointment}
            onEditAppointment={editAppointment}
            onInvoiceAppointment={startInvoiceFromAppointment}
            salesData={salesData}
            activityLog={activityLog}
            onJumpActivity={(tab) => setActiveTab(tab)}
          />
        )}

        {!tenantBlocked && !loading && activeTab === "Tires" && (
          <Tires
            auth={auth}
            filters={tireFilters}
            form={tireForm}
            onClearFilters={clearTireFilters}
            onChange={setTireForm}
            onDelete={removeTire}
            onFilterChange={setTireFilters}
            onFilterSubmit={applyTireFilters}
            onImportCsv={uploadTireCsv}
            onCreateLocation={createInventoryLocation}
            onRefill={refillTire}
            onSubmit={submitTire}
            highlightedRow={highlightedRow}
            shopLocations={shopLocations}
            tires={filterRecordsByLocation(inventoryTires, selectedLocationId)}
          />
        )}

        {!tenantBlocked && !loading && activeTab === "Appointments" && (
          <Appointments
            appointments={visibleAppointments}
            customers={visibleCustomers}
            editingId={editingAppointmentId}
            form={appointmentForm}
            onConfirmTireRequest={confirmTireRequest}
            onTireRequestStatusChange={changeTireRequestStatus}
            onChange={setAppointmentForm}
            onCancelEdit={cancelAppointmentEdit}
            onDelete={removeAppointment}
            onEdit={editAppointment}
            onSubmit={submitAppointment}
            highlightedRow={highlightedRow}
            selectedLocationId={selectedLocationId}
            shopLocations={shopLocations}
            tireRequests={visibleTireRequests}
            tires={visibleTires}
          />
        )}

        {!tenantBlocked && !loading && activeTab === "Work Orders" && (
          <WorkOrdersPage
            appointments={activeAppointments}
            customers={visibleCustomers}
            onInvoiceCreated={openGeneratedInvoice}
            onNotify={notifySuccess}
            onRefresh={() => loadData(auth)}
            selectedLocationId={selectedLocationId}
            setError={setError}
            shopLocations={shopLocations}
            workOrders={visibleWorkOrders}
          />
        )}

        {!tenantBlocked && !loading && activeTab === "Estimates" && (
          <EstimatesPage
            customers={visibleCustomers}
            estimates={visibleEstimates}
            onStartInvoice={startInvoiceFromEstimate}
            onNotify={notifySuccess}
            onRefresh={() => loadData(auth)}
            selectedLocationId={selectedLocationId}
            settings={companySettings}
            setError={setError}
            shopLocations={shopLocations}
            tires={visibleTires}
          />
        )}

        {!tenantBlocked && !loading && activeTab === "Invoices" && (
          <Invoices
            form={invoiceForm}
            generatedInvoice={generatedInvoice}
            settings={companySettings}
            invoices={visibleInvoices}
            onChange={setInvoiceForm}
            onDelete={removeInvoice}
            onMarkPaid={markInvoicePaid}
            onPreviewInvoice={previewInvoice}
            onUpdateStatus={updateInvoiceLifecycleStatus}
            onSubmit={submitInvoice}
            highlightedRow={highlightedRow}
            appointments={activeAppointments}
            selectedLocationId={selectedLocationId}
            shopLocations={shopLocations}
            tires={visibleTires}
          />
        )}

        {!tenantBlocked && !loading && activeTab === "Customers" && (
          <CustomersPage customers={visibleCustomers} onSendNotice={sendNoticeToCustomer} />
        )}

        {!tenantBlocked && !loading && activeTab === "Accounting" && (
          <AccountingPage
            accountForm={accountForm}
            accounts={accountingAccounts}
            activeAccountingTab={activeAccountingTab}
            expenseForm={expenseForm}
            message={accountingMessage}
            onAccountChange={setAccountForm}
            onExpenseChange={setExpenseForm}
            onSubmitAccount={submitAccountingAccount}
            onSubmitExpense={submitExpense}
            onSubmitVendor={submitVendor}
            onPayExpense={payAccountingExpense}
            onTabChange={setActiveAccountingTab}
            onRangeChange={changeAccountingRange}
            onVendorChange={setVendorForm}
            report={accountingReport}
            reportLoading={accountingLoading}
            reportRange={accountingRange}
            selectedLocationId={selectedLocationId}
            shopLocations={shopLocations}
            vendorForm={vendorForm}
            vendors={vendors}
          />
        )}

        {!tenantBlocked && !loading && activeTab === "Attendance" && isShopManagerRole(auth?.role) && (
          <AttendancePage auth={auth} selectedLocationId={selectedLocationId} />
        )}

        {!tenantBlocked && !loading && activeTab === "Payroll" && isShopManagerRole(auth?.role) && (
          <PayrollPage auth={auth} mode="admin" selectedLocationId={selectedLocationId} shopLocations={shopLocations} />
        )}

        {!tenantBlocked && !loading && activeTab === "My Payroll" && auth?.role === "EMPLOYEE" && (
          <PayrollPage auth={auth} mode="employee" />
        )}

        {!tenantBlocked && !loading && activeTab === "Audit Logs" && (
          <AuditLogsPage logs={activityLog} />
        )}

        {!tenantBlocked && !loading && activeTab === "Settings" && (
          <SettingsPage settings={companySettings} onSave={saveCompanySettings} />
        )}
      </main>
    </div>
  );
}

function TenantAssignmentWarning({ auth }) {
  return (
    <section className="panel tenant-warning">
      <div className="metric-icon">
        <ShieldCheck size={22} />
      </div>
      <div>
        <span className="eyebrow">Shop assignment required</span>
        <h3>No shop assigned</h3>
        <p>
          {auth?.fullName || "This user"} is signed in as {auth?.role || "a shop user"}, but the account is not linked to a shop yet.
          Contact Monarch Solutions or a SUPER_ADMIN to assign this user before using TireTrack operations.
        </p>
      </div>
    </section>
  );
}

function LocationScopeBar({ auth, locations, onChange, summaries = [], value }) {
  const [open, setOpen] = useState(false);
  const lockedLocation = Boolean(auth?.locationId);
  const activeLocations = locations.filter((location) => location.active !== false);
  const selectedId = lockedLocation ? String(auth.locationId) : String(value || "");
  const selectedLocation = activeLocations.find((location) => String(location.id) === selectedId);
  const selectedSummary = summaries.find((summary) => String(summary.id || "") === selectedId) || summaries[0];
  const options = summaries.filter((summary) => summary.id === "" || activeLocations.some((location) => String(location.id) === String(summary.id)));
  const canChooseLocation = !lockedLocation && activeLocations.length > 0;
  const selectedName = selectedId ? selectedLocation?.name || auth?.locationName || selectedSummary?.name || "Selected Location" : "All Locations";
  const selectedType = selectedId
    ? selectedLocation?.type || selectedSummary?.type || "Location"
    : activeLocations.length > 0 ? "Shop-wide view" : "No locations set up";

  function chooseLocation(locationId) {
    setOpen(false);
    onChange(locationId);
  }

  return (
    <section
      className={`location-scope-bar ${open ? "open" : ""} ${lockedLocation ? "locked" : ""}`}
      onBlur={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget)) {
          setOpen(false);
        }
      }}
    >
      <button
        aria-expanded={open}
        className="location-scope-trigger"
        disabled={!canChooseLocation}
        onClick={() => setOpen((current) => !current)}
        type="button"
      >
        <span className="location-scope-icon"><MapPin size={17} /></span>
        <span className="location-scope-copy">
          <span>Viewing</span>
          <strong>{selectedName}</strong>
        </span>
        <span className="location-scope-meta">
          <b>{selectedType}</b>
          <small>{locationScopeText(selectedSummary)}</small>
        </span>
        {canChooseLocation && <ChevronDown className="location-scope-chevron" size={17} />}
      </button>

      {canChooseLocation && open && (
        <div className="location-scope-menu" role="listbox">
          {options.map((option) => {
            const isSelected = String(option.id || "") === selectedId;

            return (
              <button
                className={isSelected ? "selected" : ""}
                key={option.id || "all-locations"}
                onClick={() => chooseLocation(option.id || "")}
                type="button"
              >
                <span>
                  <strong>{option.name}</strong>
                  <small>{option.type}</small>
                </span>
                <span>{locationScopeText(option)}</span>
                {isSelected && <CheckCircle2 size={16} />}
              </button>
            );
          })}
        </div>
      )}
    </section>
  );
}

function Dashboard({
  auth,
  appointments = [],
  customers = [],
  dashboard,
  invoices = [],
  workOrders = [],
  lowStockTires = [],
  onCancelAppointment,
  onDeleteAppointment,
  onEditAppointment,
  onInvoiceAppointment,
  activityLog = [],
  onJumpActivity,
  salesData = [],
  tires = []
}) {
  const [range, setRange] = useState("month");
  const [customRange, setCustomRange] = useState({ start: todayDateKey(), end: todayDateKey() });
  const [showWidgetPicker, setShowWidgetPicker] = useState(false);
  const widgetStorageKey = dashboardWidgetStorageKey(auth);
  const [selectedWidgetIds, setSelectedWidgetIds] = useState(null);
  const rangeStart = getRangeStart(range, customRange.start);
  const rangeEnd = range === "custom" ? customRange.end : todayDateKey();
  const activeWidgetIds = validDashboardWidgetIds(selectedWidgetIds ?? defaultDashboardWidgetIds);
  const filteredInvoices = invoices.filter((invoice) => {
    const date = appointmentDateKey(invoice.createdAt || invoice.invoiceDate);
    return date && date >= rangeStart && date <= rangeEnd;
  });
  const filteredSalesData = salesData.filter((day) => day.date >= rangeStart && day.date <= rangeEnd);
  const inventoryValue = tires.reduce(
    (total, tire) => total + Number(tire.quantity || 0) * Number(tire.price || 0),
    0
  );
  const totalUnits = tires.reduce((total, tire) => total + Number(tire.quantity || 0), 0);
  const averageUnits = tires.length ? Math.round(totalUnits / tires.length) : 0;
  const today = todayDateKey();
  const todayAppointments = appointments.filter((appointment) => appointmentDateKey(appointment.appointmentDate) === today);
  const todayInvoices = invoices.filter((invoice) => appointmentDateKey(invoice.createdAt || invoice.invoiceDate) === today);
  const rangeRevenue = filteredInvoices.reduce((total, invoice) => total + invoiceCollectedAmount(invoice), 0);
  const todayRevenue = todayInvoices.reduce((total, invoice) => total + invoiceCollectedAmount(invoice), 0);
  const urgentRestock = lowStockTires.filter((tire) => Number(tire.availableQuantity ?? tire.quantity ?? 0) < 3);
  const outstandingCustomerBalance = customers.reduce((total, customer) => total + Number(customer.outstandingBalance || 0), 0);
  const paymentAlertCustomers = customers.filter((customer) => Number(customer.outstandingBalance || 0) > 0);
  const appointmentReminderCustomers = customers.filter((customer) => customer.hasUpcomingAppointment);
  const followUpCustomers = [...paymentAlertCustomers, ...appointmentReminderCustomers]
    .filter((customer, index, list) => list.findIndex((entry) => entry.id === customer.id) === index)
    .sort((first, second) => followUpPriority(first) - followUpPriority(second));
  const latestInvoices = [...invoices]
    .sort((first, second) => new Date(second.createdAt || second.invoiceDate || 0) - new Date(first.createdAt || first.invoiceDate || 0))
    .slice(0, 5);
  const conditionTotals = ["NEW", "USED"].map((condition, index) => ({
    className: index === 0 ? "available" : "reserved",
    color: chartColors[index],
    label: condition === "NEW" ? "New tires" : "Used tires",
    value: tires
      .filter((tire) => String(tire.condition || "").toUpperCase() === condition)
      .reduce((total, tire) => total + Number(tire.quantity || 0), 0)
  })).filter((item) => item.value > 0);
  const inventoryBars = tires
    .slice()
    .sort((first, second) => Number(second.quantity || 0) - Number(first.quantity || 0))
    .slice(0, 6)
    .map((tire) => ({
      condition: tire.condition || "-",
      label: tire.brand || "Unknown",
      size: `${tire.width}/${tire.aspectRatio}R${tire.rimSize}`,
      units: Number(tire.quantity || 0)
    }));
  const workOrderStats = [
    { label: "Pending", value: dashboard?.pendingWorkOrders ?? workOrders.filter((workOrder) => workOrder.status === "PENDING").length, tone: "warning" },
    { label: "In progress", value: dashboard?.inProgressWorkOrders ?? workOrders.filter((workOrder) => workOrder.status === "IN_PROGRESS").length, tone: "info" },
    { label: "Vehicle ready", value: dashboard?.vehicleReadyWorkOrders ?? workOrders.filter((workOrder) => workOrder.status === "VEHICLE_READY").length, tone: "good" },
    { label: "Completed today", value: dashboard?.completedWorkOrdersToday ?? workOrders.filter((workOrder) => workOrder.status === "COMPLETED" && appointmentDateKey(workOrder.completedAt) === today).length, tone: "neutral" }
  ];
  const maxWorkOrderStat = Math.max(...workOrderStats.map((stat) => Number(stat.value || 0)), 1);
  const keyMetrics = [
    {
      detail: `${filteredInvoices.length} invoices in range`,
      icon: CircleDollarSign,
      label: "Collected",
      tone: "good",
      value: money(dashboard?.totalCollected ?? dashboard?.totalRevenue)
    },
    {
      detail: `${paymentAlertCustomers.length} customers with balances`,
      icon: AlertTriangle,
      label: "Outstanding",
      tone: Number(dashboard?.outstandingBalance || 0) > 0 ? "warning" : "neutral",
      value: money(dashboard?.outstandingBalance)
    },
    {
      detail: `${todayAppointments.length} scheduled today`,
      icon: CalendarDays,
      label: "Appointments",
      tone: "info",
      value: dashboard?.todayAppointments ?? todayAppointments.length
    },
    {
      detail: `${urgentRestock.length} urgent restocks`,
      icon: Package,
      label: "Inventory units",
      tone: urgentRestock.length ? "warning" : "good",
      value: dashboard?.totalTiresInStock ?? totalUnits
    },
    {
      detail: `${dashboard?.totalCustomers ?? customers.length} customers`,
      icon: UserCircle,
      label: "Customers",
      tone: "neutral",
      value: dashboard?.totalCustomers ?? customers.length
    },
    {
      detail: `${workOrderStats[2].value} ready for pickup`,
      icon: CheckCircle2,
      label: "Work orders",
      tone: workOrderStats[2].value ? "good" : "neutral",
      value: workOrders.length
    }
  ];

  useEffect(() => {
    setSelectedWidgetIds(loadDashboardWidgets(widgetStorageKey));
  }, [widgetStorageKey]);

  useEffect(() => {
    if (selectedWidgetIds !== null) {
      localStorage.setItem(widgetStorageKey, JSON.stringify(validDashboardWidgetIds(selectedWidgetIds)));
    }
  }, [selectedWidgetIds, widgetStorageKey]);

  function toggleWidget(id) {
    setSelectedWidgetIds((current) => {
      const currentIds = validDashboardWidgetIds(current ?? defaultDashboardWidgetIds);
      return currentIds.includes(id)
        ? currentIds.filter((widgetId) => widgetId !== id)
        : [...currentIds, id];
    });
  }

  function resetWidgets() {
    setSelectedWidgetIds(defaultDashboardWidgetIds);
  }

  function renderWidget(id) {
    switch (id) {
      case "keyMetrics":
        return (
          <section className="dashboard-kpi-strip dashboard-widget-wide" key={id}>
            {keyMetrics.map((metric) => (
              <DashboardMetricTile key={metric.label} metric={metric} />
            ))}
          </section>
        );
      case "today":
        return (
          <DashboardWidgetPanel eyebrow="Today" icon={CalendarDays} key={id} title="Daily Operations" wide>
            <div className="today-stats">
              <div><span>Appointments</span><strong>{todayAppointments.length}</strong></div>
              <div><span>Invoices</span><strong>{todayInvoices.length}</strong></div>
              <div><span>Collected</span><strong>{money(todayRevenue)}</strong></div>
              <div><span>Low stock actions</span><strong>{urgentRestock.length}</strong></div>
            </div>
            <div className="today-actions">
              {urgentRestock.slice(0, 3).map((tire) => (
                <span className="warning-chip danger" key={tire.id}>{tire.brand} {tire.width}/{tire.aspectRatio}R{tire.rimSize}</span>
              ))}
              {urgentRestock.length === 0 && <span className="warning-chip good">No urgent restocks</span>}
            </div>
          </DashboardWidgetPanel>
        );
      case "revenue":
        return <SalesChart className="dashboard-widget dashboard-widget-wide" key={id} salesData={filteredSalesData.length ? filteredSalesData : salesData} />;
      case "workOrders":
        return (
          <DashboardWidgetPanel eyebrow="Jobs" icon={ClipboardList} key={id} title="Work Order Flow">
            <div className="dashboard-progress-list">
              {workOrderStats.map((stat) => (
                <div className={`dashboard-progress-row ${stat.tone}`} key={stat.label}>
                  <div>
                    <span>{stat.label}</span>
                    <strong>{stat.value}</strong>
                  </div>
                  <div className="dashboard-progress-track">
                    <span style={{ width: `${Math.max((Number(stat.value || 0) / maxWorkOrderStat) * 100, stat.value ? 8 : 0)}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </DashboardWidgetPanel>
        );
      case "inventoryMovement":
        return <InventoryBars className="dashboard-widget" key={id} lowStockTires={lowStockTires} tires={tires} />;
      case "followUps":
        return (
          <DashboardWidgetPanel eyebrow="Customers" icon={Bell} key={id} title="Follow Ups">
            {followUpCustomers.length === 0 ? (
              <p className="empty-note">No customer follow ups.</p>
            ) : (
              <>
                <div className="follow-up-summary">
                  <div><span>Outstanding</span><strong>{money(outstandingCustomerBalance)}</strong></div>
                  <div><span>Payment alerts</span><strong>{paymentAlertCustomers.length}</strong></div>
                  <div><span>Bookings</span><strong>{appointmentReminderCustomers.length}</strong></div>
                </div>
                <div className="follow-up-list">
                  {followUpCustomers.slice(0, 3).map((customer) => (
                    <button className={`follow-up-card ${followUpTone(customer)}`} key={customer.id} onClick={() => onJumpActivity("Customers")} type="button">
                      <div>
                        <span>{followUpLabel(customer)}</span>
                        <strong>{customer.fullName}</strong>
                        <small>{followUpDetail(customer)}</small>
                      </div>
                      <b>{followUpAction(customer)}</b>
                    </button>
                  ))}
                </div>
              </>
            )}
          </DashboardWidgetPanel>
        );
      case "latestInvoices":
        return (
          <DashboardWidgetPanel eyebrow="Invoices" icon={FileText} key={id} title="Latest Invoices">
            <div className="dashboard-compact-list">
              {latestInvoices.length === 0 ? (
                <p className="empty-note">No invoices yet.</p>
              ) : (
                latestInvoices.map((invoice) => (
                  <div className="dashboard-list-row" key={invoice.id}>
                    <div>
                      <strong>{invoice.customerName}</strong>
                      <span>{dateTime(invoice.createdAt || invoice.invoiceDate)}</span>
                    </div>
                    <div>
                      <strong>{money(invoice.total)}</strong>
                      <StatusBadge value={invoiceDisplayStatus(invoice)} />
                    </div>
                  </div>
                ))
              )}
            </div>
          </DashboardWidgetPanel>
        );
      case "locationBreakdown":
        return (
          <DashboardWidgetPanel eyebrow="Locations" icon={ShieldCheck} key={id} title="Location Breakdown" wide>
            {(dashboard?.locationBreakdowns || []).length === 0 ? (
              <p className="empty-note">No location activity yet.</p>
            ) : (
              <div className="dashboard-location-list">
                {(dashboard.locationBreakdowns || []).map((location) => (
                  <article className="dashboard-location-row" key={`location-breakdown-${location.locationId}`}>
                    <div>
                      <strong>{location.locationName || "Location"}</strong>
                      <span>{location.locationType || "Location"}</span>
                    </div>
                    <div className="dashboard-location-metrics">
                      <span><b>{location.inventoryQuantity || 0}</b> units</span>
                      <span><b>{money(location.revenue)}</b> revenue</span>
                      <span><b>{location.appointmentCount || 0}</b> appointments</span>
                      <span><b>{money(location.expenses)}</b> expenses</span>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </DashboardWidgetPanel>
        );
      case "conditionMix":
        return (
          <DashboardWidgetPanel eyebrow="Inventory" icon={ShieldCheck} key={id} title="Tire Condition Mix">
            <DashboardDonut ariaLabel="Tire condition mix chart" centerLabel="units" segments={conditionTotals} />
          </DashboardWidgetPanel>
        );
      case "topInventory":
        return (
          <DashboardWidgetPanel eyebrow="Inventory" icon={Package} key={id} title="Top Inventory">
            <InventoryLeaderBars items={inventoryBars} />
          </DashboardWidgetPanel>
        );
      case "lowStock":
        return (
          <DashboardWidgetPanel eyebrow="Inventory" icon={AlertTriangle} key={id} title="Low Stock">
            <div className="dashboard-compact-list">
              {lowStockTires.length === 0 ? (
                <p className="empty-note">No low stock tires.</p>
              ) : (
                lowStockTires.slice(0, 8).map((tire) => (
                  <div className={`dashboard-list-row ${tireAvailableQuantity(tire) <= 5 ? "low-stock-pulse" : ""}`} key={tire.id}>
                    <div>
                      <strong>{tire.brand} {tire.model}</strong>
                      <span>{tire.width}/{tire.aspectRatio}R{tire.rimSize} · {displayTireLocation(tire)}</span>
                    </div>
                    <div>
                      <strong className={tireAvailableQuantity(tire) < 3 ? "urgent-stock-text" : ""}>
                        {tireAvailableQuantity(tire)}
                      </strong>
                      <span>qty</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </DashboardWidgetPanel>
        );
      case "inventoryHealth":
        return (
          <DashboardWidgetPanel eyebrow="Inventory" icon={Gauge} key={id} title="Inventory Health">
            <section className="dashboard-health-list">
              <div className="dashboard-health-row"><span>Total SKUs</span><strong>{tires.length}</strong></div>
              <div className="dashboard-health-row"><span>Total Units</span><strong>{totalUnits}</strong></div>
              <div className="dashboard-health-row"><span>Inventory Value</span><strong>{money(inventoryValue)}</strong></div>
              <div className="dashboard-health-row"><span>Average Units/SKU</span><strong>{averageUnits}</strong></div>
            </section>
          </DashboardWidgetPanel>
        );
      case "activity":
        return (
          <DashboardWidgetPanel eyebrow="Activity" icon={Bell} key={id} title="Recent Actions">
            {activityLog.length === 0 ? (
              <p className="empty-note">No activity yet.</p>
            ) : (
              <div className="activity-feed-list compact">
                {activityLog.slice(0, 6).map((activity) => (
                  <button key={activity.id} onClick={() => onJumpActivity(activity.tab)} type="button">
                    <strong>{activity.label}</strong>
                    <span>{dateTime(activity.createdAt)}</span>
                  </button>
                ))}
              </div>
            )}
          </DashboardWidgetPanel>
        );
      default:
        return null;
    }
  }

  return (
    <>
      <section className="dashboard-command-bar panel">
        <div>
          <span className="eyebrow">{auth?.shopName || "Dashboard"}</span>
          <h3>{range === "custom" ? `${rangeStart} to ${rangeEnd}` : `This ${range}`}</h3>
        </div>
        <div className="dashboard-command-actions">
          <div className="segmented-control">
            {["today", "week", "month", "custom"].map((option) => (
              <button className={range === option ? "active" : ""} key={option} onClick={() => setRange(option)} type="button">
                {option}
              </button>
            ))}
          </div>
          {range === "custom" && (
            <div className="range-inputs">
              <input type="date" value={customRange.start} onChange={(event) => setCustomRange({ ...customRange, start: event.target.value })} />
              <input type="date" value={customRange.end} onChange={(event) => setCustomRange({ ...customRange, end: event.target.value })} />
            </div>
          )}
          <strong className="dashboard-range-total">{money(rangeRevenue)} revenue</strong>
          <button className="ghost-button with-icon" onClick={() => setShowWidgetPicker((open) => !open)} type="button">
            <SettingsIcon size={16} />
            Widgets
          </button>
        </div>
      </section>

      {showWidgetPicker && (
        <section className="dashboard-widget-picker panel">
          <div>
            <span className="eyebrow">Visible widgets</span>
            <h3>Customize Dashboard</h3>
          </div>
          <div className="dashboard-widget-options">
            {dashboardWidgetCatalog.map((widget) => {
              const Icon = widget.icon;
              const checked = activeWidgetIds.includes(widget.id);

              return (
                <label className={`widget-toggle ${checked ? "active" : ""}`} key={widget.id}>
                  <input checked={checked} onChange={() => toggleWidget(widget.id)} type="checkbox" />
                  <Icon size={16} />
                  <span>{widget.label}</span>
                </label>
              );
            })}
          </div>
          <button className="ghost-button" onClick={resetWidgets} type="button">Reset</button>
        </section>
      )}

      {activeWidgetIds.length === 0 ? (
        <section className="dashboard-empty panel">
          <strong>No dashboard widgets selected.</strong>
          <button className="primary-button" onClick={() => setShowWidgetPicker(true)} type="button">Add Widgets</button>
        </section>
      ) : (
        <section className="dashboard-widget-grid">
          {activeWidgetIds.map((id) => renderWidget(id))}
        </section>
      )}

      <section className="dashboard-calendar-anchor">
        <div className="dashboard-calendar-title">
          <div>
            <span className="eyebrow">Fixed</span>
            <h3>Appointments Calendar</h3>
          </div>
        </div>
        <AppointmentCalendar
          appointments={appointments}
          onCancelAppointment={onCancelAppointment}
          onDeleteAppointment={onDeleteAppointment}
          onEditAppointment={onEditAppointment}
          onInvoiceAppointment={onInvoiceAppointment}
        />
      </section>
    </>
  );
}

function DashboardMetricTile({ metric }) {
  const Icon = metric.icon || Gauge;

  return (
    <motion.article
      animate={{ opacity: 1, y: 0 }}
      className={`dashboard-kpi ${metric.tone || "neutral"}`}
      initial={{ opacity: 0, y: 8 }}
      transition={{ duration: 0.28 }}
    >
      <span className="dashboard-kpi-icon"><Icon size={18} /></span>
      <div>
        <span>{metric.label}</span>
        <strong>{metric.value}</strong>
        <small>{metric.detail}</small>
      </div>
    </motion.article>
  );
}

function DashboardWidgetPanel({ children, className = "", eyebrow, icon: Icon = Gauge, title, wide = false }) {
  return (
    <section className={`dashboard-widget panel ${wide ? "dashboard-widget-wide" : ""} ${className}`}>
      <div className="dashboard-widget-head">
        <div>
          <span className="eyebrow">{eyebrow}</span>
          <h3>{title}</h3>
        </div>
        <span className="metric-icon small"><Icon size={17} /></span>
      </div>
      {children}
    </section>
  );
}

function InventoryBars({ className = "", lowStockTires, tires }) {
  const availableUnits = tires.reduce((total, tire) => total + Number(tire.availableQuantity ?? tire.quantity ?? 0), 0);
  const reservedUnits = tires.reduce((total, tire) => total + Number(tire.reservedQuantity || 0), 0);
  const lowStockCount = lowStockTires.length;
  const segments = [
    { label: "Available", value: availableUnits, className: "available", color: "#18d3b2" },
    { label: "Reserved", value: reservedUnits, className: "reserved", color: "#7c8cff" },
    { label: "Low stock", value: lowStockCount, className: "urgent", color: "#ef4444" }
  ];

  return (
    <section className={`analytics-panel panel ${className}`}>
      <div>
        <span className="eyebrow">Inventory</span>
        <h3>Stock Movement</h3>
      </div>
      <DashboardDonut
        ariaLabel="Inventory stock movement chart"
        centerLabel="units"
        segments={segments}
      />
    </section>
  );
}

function DashboardDonut({ ariaLabel, centerLabel, segments }) {
  const [hoveredSegment, setHoveredSegment] = useState(null);
  const total = Math.max(segments.reduce((sum, segment) => sum + Number(segment.value || 0), 0), 1);
  const activeSegment = hoveredSegment || { label: "Total", value: total };
  let offset = 25;

  if (segments.length === 0 || segments.every((segment) => Number(segment.value || 0) === 0)) {
    return <p className="empty-note">No chart data yet.</p>;
  }

  return (
    <div className="inventory-donut-layout">
      <div className="inventory-donut" aria-label={ariaLabel}>
        <svg viewBox="0 0 42 42" role="img">
          <circle className="donut-ring" cx="21" cy="21" r="15.9155" />
          {segments.map((segment) => {
            const length = (Number(segment.value || 0) / total) * 100;
            const dashArray = `${length} ${100 - length}`;
            const strokeDashoffset = offset;
            offset -= length;

            return (
              <circle
                className={`donut-segment ${segment.className}`}
                cx="21"
                cy="21"
                key={segment.label}
                onMouseEnter={() => setHoveredSegment(segment)}
                onMouseLeave={() => setHoveredSegment(null)}
                onFocus={() => setHoveredSegment(segment)}
                onBlur={() => setHoveredSegment(null)}
                r="15.9155"
                tabIndex="0"
                strokeDasharray={dashArray}
                strokeDashoffset={strokeDashoffset}
              />
            );
          })}
        </svg>
        <div className="donut-center">
          <strong>{activeSegment.value}</strong>
          <span>{hoveredSegment ? activeSegment.label : centerLabel}</span>
        </div>
      </div>
      <div className="donut-legend">
        {segments.map((segment) => (
          <div
            className={`donut-legend-row ${hoveredSegment?.label === segment.label ? "active" : ""}`}
            key={segment.label}
            onMouseEnter={() => setHoveredSegment(segment)}
            onMouseLeave={() => setHoveredSegment(null)}
          >
            <span style={{ background: segment.color }} />
            <div>
              <strong>{segment.label}</strong>
              <small>{segment.value}</small>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function InventoryLeaderBars({ items }) {
  const maxUnits = Math.max(...items.map((item) => Number(item.units || 0)), 1);

  if (items.length === 0) {
    return <p className="empty-note">No inventory yet.</p>;
  }

  return (
    <div className="leader-bars">
      {items.map((item, index) => (
        <div className="leader-bar-row" key={`${item.label}-${item.size}-${item.condition}`}>
          <div className="leader-bar-label">
            <span title={`${item.label} ${item.size}`}>
              <b>{index + 1}</b>
              {item.label}
            </span>
            <small>{item.size} · {item.condition}</small>
            <strong>{item.units}</strong>
          </div>
          <div className="leader-bar-track">
            <motion.div
              animate={{ width: `${Math.max((item.units / maxUnits) * 100, 4)}%` }}
              className="leader-bar-fill"
              initial={{ width: 0 }}
              transition={{ duration: 0.8, ease: "easeOut" }}
            />
          </div>
        </div>
      ))}
    </div>
  );
}

function followUpPriority(customer) {
  if (customer.hasOverdueBalance) {
    return 0;
  }

  if (Number(customer.outstandingBalance || 0) > 0) {
    return 1;
  }

  return 2;
}

function followUpTone(customer) {
  if (customer.hasOverdueBalance) {
    return "danger";
  }

  if (Number(customer.outstandingBalance || 0) > 0) {
    return "warning";
  }

  return "info";
}

function followUpLabel(customer) {
  if (customer.hasOverdueBalance) {
    return "Payment overdue";
  }

  if (customer.hasBalanceDueSoon) {
    return "Payment due soon";
  }

  if (Number(customer.outstandingBalance || 0) > 0) {
    return "Outstanding balance";
  }

  return "Appointment reminder";
}

function followUpDetail(customer) {
  if (Number(customer.outstandingBalance || 0) > 0) {
    return `${money(customer.outstandingBalance)}${customer.nextPaymentDueDate ? ` due ${customer.nextPaymentDueDate}` : " outstanding"}`;
  }

  return customer.nextAppointmentDate ? dateTime(customer.nextAppointmentDate) : "Upcoming appointment";
}

function followUpAction(customer) {
  if (Number(customer.outstandingBalance || 0) > 0) {
    return customer.hasOverdueBalance ? "Send payment notice" : "Review balance";
  }

  return "Send reminder";
}

function SalesChart({ className = "", salesData }) {
  const points = salesData
    .map((day) => ({ date: day.date, revenue: Number(day.revenue || 0) }))
    .sort((first, second) => new Date(first.date) - new Date(second.date));
  const totalRevenue = points.reduce((total, point) => total + point.revenue, 0);

  return (
    <section className={`analytics-panel panel ${className}`}>
      <div className="sales-chart-header">
        <div>
          <span className="eyebrow">Sales</span>
          <h3>Recent Revenue</h3>
        </div>
        <strong>{money(totalRevenue)}</strong>
      </div>
      <div className="sales-line-chart">
        {points.length === 0 ? (
          <p className="empty-note">No recent invoices.</p>
        ) : (
          <ResponsiveContainer height={252} width="100%">
            <AreaChart data={points} margin={{ bottom: 8, left: 4, right: 16, top: 14 }}>
              <defs>
                <linearGradient id="salesAreaGradient" x1="0" x2="0" y1="0" y2="1">
                  <stop offset="0%" stopColor="#18d3b2" stopOpacity={0.34} />
                  <stop offset="100%" stopColor="#18d3b2" stopOpacity={0.02} />
                </linearGradient>
              </defs>
              <CartesianGrid stroke="rgba(255,255,255,0.06)" vertical={false} />
              <XAxis dataKey="date" tickFormatter={formatShortDate} tick={{ fill: "#8f95a3", fontSize: 11 }} />
              <YAxis tickFormatter={(value) => `$${Number(value).toLocaleString()}`} tick={{ fill: "#8f95a3", fontSize: 11 }} width={70} />
              <Tooltip contentStyle={tooltipStyle} formatter={(value) => money(value)} labelFormatter={formatShortDate} />
              <Area
                animationDuration={1200}
                dataKey="revenue"
                fill="url(#salesAreaGradient)"
                stroke="#18d3b2"
                strokeWidth={4}
                type="monotone"
              />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </div>
    </section>
  );
}

function buildSalesLineChart(salesData) {
  const points = salesData
    .map((day) => ({
      date: day.date,
      revenue: Number(day.revenue || 0)
    }))
    .sort((first, second) => new Date(first.date) - new Date(second.date));
  const totalRevenue = points.reduce((total, point) => total + point.revenue, 0);
  const maxRevenue = Math.max(...points.map((point) => point.revenue), 1);
  const width = 560;
  const height = 176;
  const left = 48;
  const top = 24;
  const bottom = top + height;
  const plottedPoints = points.map((point, index) => {
    const x = points.length === 1 ? left + width / 2 : left + (index / (points.length - 1)) * width;
    const y = bottom - (point.revenue / maxRevenue) * height;

    return { ...point, x, y };
  });
  const linePath = plottedPoints.length
    ? plottedPoints.map((point, index) => `${index === 0 ? "M" : "L"}${point.x} ${point.y}`).join(" ")
    : "";
  const areaPath = plottedPoints.length
    ? `${linePath} L${plottedPoints[plottedPoints.length - 1].x} ${bottom} L${plottedPoints[0].x} ${bottom} Z`
    : "";

  return {
    areaPath,
    linePath,
    maxRevenue,
    points: plottedPoints,
    totalRevenue
  };
}

function AppointmentCalendar({
  appointments,
  onCancelAppointment,
  onDeleteAppointment,
  onEditAppointment,
  onInvoiceAppointment
}) {
  const today = new Date();
  const [selectedDate, setSelectedDate] = useState(toDateKey(today));
  const [visibleMonth, setVisibleMonth] = useState(new Date(today.getFullYear(), today.getMonth(), 1));
  const monthAppointments = groupAppointmentsByDate(appointments);
  const selectedAppointments = monthAppointments[selectedDate] || [];
  const days = getCalendarDays(visibleMonth);

  function changeMonth(offset) {
    setVisibleMonth(new Date(visibleMonth.getFullYear(), visibleMonth.getMonth() + offset, 1));
  }

  return (
    <section className="dashboard-calendar panel">
      <div className="calendar-header">
        <div>
          <span className="eyebrow">Schedule</span>
          <h3>{visibleMonth.toLocaleString(undefined, { month: "long", year: "numeric" })}</h3>
        </div>
        <div className="calendar-controls">
          <button className="ghost-button" onClick={() => changeMonth(-1)} type="button">
            Previous
          </button>
          <button className="ghost-button" onClick={() => changeMonth(1)} type="button">
            Next
          </button>
        </div>
      </div>

      <div className="calendar-layout">
        <div className="calendar-grid" aria-label="Appointment calendar">
          {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map((day) => (
            <div className="calendar-weekday" key={day}>{day}</div>
          ))}
          {days.map((day) => {
            const dateKey = toDateKey(day);
            const dayAppointments = monthAppointments[dateKey] || [];
            const isCurrentMonth = day.getMonth() === visibleMonth.getMonth();
            const isSelected = dateKey === selectedDate;
            const isToday = dateKey === toDateKey(today);

            return (
              <button
                className={[
                  "calendar-day",
                  isCurrentMonth ? "" : "muted",
                  isSelected ? "selected" : "",
                  isToday ? "today" : ""
                ].filter(Boolean).join(" ")}
                key={dateKey}
                onClick={() => setSelectedDate(dateKey)}
                type="button"
              >
                <span className="calendar-date-number">{day.getDate()}</span>
                {dayAppointments.length > 0 && (
                  <div className="calendar-events">
                    {dayAppointments.slice(0, 3).map((appointment) => (
                      <span
                        className={`calendar-event ${serviceColorClass(appointment.serviceType)}`}
                        key={appointment.id}
                      >
                        {formatAppointmentTime(appointment.appointmentDate)} {appointment.customerName}
                      </span>
                    ))}
                    {dayAppointments.length > 3 && (
                      <span className="calendar-event more">+{dayAppointments.length - 3} more</span>
                    )}
                  </div>
                )}
              </button>
            );
          })}
        </div>

        <div className="calendar-agenda">
          <h4>{formatDateLabel(selectedDate)}</h4>
          {selectedAppointments.length === 0 ? (
            <p>No appointments scheduled.</p>
          ) : (
            <div className="agenda-list">
              {selectedAppointments.map((appointment) => (
                <article className="agenda-item" key={appointment.id}>
                  <strong>{formatAppointmentTime(appointment.appointmentDate)}</strong>
                  <div>
                    <span>{appointment.customerName}</span>
                    <small>{serviceTypeLabel(appointment.serviceType)} - {appointment.vehicle || "No vehicle"}</small>
                    <div className="agenda-actions">
                      <button className="ghost-button" onClick={() => onInvoiceAppointment(appointment)} type="button">
                        Invoice
                      </button>
                      <button className="ghost-button" onClick={() => onEditAppointment(appointment)} type="button">
                        Edit
                      </button>
                      <button className="ghost-button" onClick={() => onCancelAppointment(appointment)} type="button">
                        Cancel
                      </button>
                      <button className="danger-button" onClick={() => onDeleteAppointment(appointment.id)} type="button">
                        Delete
                      </button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </div>
      </div>
    </section>
  );
}

function groupAppointmentsByDate(appointments) {
  return appointments.reduce((groups, appointment) => {
    const dateKey = toDateKey(new Date(appointment.appointmentDate));

    if (!groups[dateKey]) {
      groups[dateKey] = [];
    }

    groups[dateKey].push(appointment);
    groups[dateKey].sort((first, second) => new Date(first.appointmentDate) - new Date(second.appointmentDate));
    return groups;
  }, {});
}

function getCalendarDays(monthDate) {
  const firstDay = new Date(monthDate.getFullYear(), monthDate.getMonth(), 1);
  const start = new Date(firstDay);
  start.setDate(firstDay.getDate() - firstDay.getDay());

  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start);
    date.setDate(start.getDate() + index);
    return date;
  });
}

function toDateKey(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

function formatDateLabel(dateKey) {
  return new Date(`${dateKey}T00:00`).toLocaleDateString(undefined, {
    weekday: "long",
    month: "long",
    day: "numeric"
  });
}

function formatAppointmentTime(value) {
  return new Date(value).toLocaleTimeString(undefined, {
    hour: "numeric",
    minute: "2-digit"
  });
}

function formatShortDate(value) {
  return new Date(`${value}T00:00`).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric"
  });
}

function serviceColorClass(serviceType) {
  const colors = {
    INSTALLATION: "event-blue",
    RE_AND_RE: "event-purple",
    BOLT_ON: "event-green",
    BALANCING: "event-purple",
    ROTATION: "event-green",
    REPAIR: "event-red"
  };

  return colors[serviceType] || "event-gray";
}

function tireOptionLabel(value, tires) {
  if (!value) {
    return "No tire";
  }

  const tire = tires.find((entry) => String(entry.id) === String(value));

  if (!tire) {
    return "Unknown tire";
  }

  return `${tire.brand} ${tire.width}/${tire.aspectRatio}R${tire.rimSize} (${tire.availableQuantity ?? tire.quantity} available)`;
}

function urgentStockValue(tire, value) {
  const available = tireAvailableQuantity(tire);

  if (available >= 3) {
    return value;
  }

  return {
    className: "urgent-stock-cell blink",
    value
  };
}

function tireSizeValue(tire) {
  return `${tire.width}/${tire.aspectRatio}/${tire.rimSize}`;
}

function tireAvailableQuantity(tire) {
  return Number(tire.availableQuantity ?? tire.quantity ?? 0);
}

function displayTireLocation(tire) {
  return tire?.locationName || tire?.location || "Unassigned";
}

function filterTiresForAppointment(tires, query, conditionFilter) {
  const normalizedQuery = query.trim().toLowerCase();
  const queryDigits = normalizedQuery.replace(/\D/g, "");
  const availableTires = tires.filter((tire) =>
    tireAvailableQuantity(tire) > 0
    && (conditionFilter === "ALL" || tire.condition === conditionFilter)
  );

  if (!normalizedQuery) {
    return availableTires.slice(0, 3);
  }

  return availableTires.filter((tire) => {
    const size = tireSizeValue(tire).toLowerCase();
    const searchable = [
      tire.brand,
      tire.model,
      tire.season,
      tire.locationName,
      tire.location,
      size,
      size.replace("r", "/")
    ].join(" ").toLowerCase();

    return searchable.includes(normalizedQuery) || Boolean(queryDigits && size.replace(/\D/g, "").includes(queryDigits));
  }).slice(0, 6);
}

function makeInvoiceItem() {
  return {
    tireId: "",
    itemType: "SERVICE",
    itemName: "",
    quantity: 1,
    unitPrice: "0.00"
  };
}

const emptyShopForm = {
  name: "",
  legalName: "",
  phone: "",
  email: "",
  address: "",
  subscriptionPlan: "BASIC",
  active: true
};

const platformRecordTypes = [
  "ALL",
  "USER",
  "TIRE",
  "APPOINTMENT",
  "INVOICE",
  "ESTIMATE",
  "WORK_ORDER",
  "PAYROLL_PERIOD",
  "PAYROLL_RECORD",
  "VENDOR",
  "EXPENSE",
  "ACCOUNTING_ACCOUNT",
  "JOURNAL_ENTRY",
  "APP_NOTIFICATION",
  "AUDIT_LOG",
  "EMPLOYEE_ATTENDANCE",
  "EMPLOYEE_LOAN"
];

const emptyPlatformLocationForm = {
  shopId: "",
  name: "",
  type: "STORE",
  address: "",
  city: "",
  province: "",
  postalCode: "",
  phone: "",
  email: "",
  customerFacing: true
};

const emptyPlatformUserForm = {
  fullName: "",
  email: "",
  phone: "",
  password: "",
  confirmPassword: "",
  role: "OWNER",
  shopId: "",
  locationId: "",
  active: true,
  hourlyRate: "",
  payrollEnabled: false,
  employmentType: ""
};

function platformTypeLabel(type) {
  return String(type || "")
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function PlatformPage({
  linkLocations = [],
  linkRecords = [],
  onAssignAdmin,
  onAssignLegacyData,
  onAssignLocation,
  onAssignRecord,
  onCreateLocation,
  onCreateUser,
  onRefresh,
  onSetUserActive,
  setError,
  shops = [],
  users = []
}) {
  const [shopForm, setShopForm] = useState(emptyShopForm);
  const [locationForm, setLocationForm] = useState(emptyPlatformLocationForm);
  const [userForm, setUserForm] = useState(emptyPlatformUserForm);
  const [editingShopId, setEditingShopId] = useState(null);
  const [recordTypeFilter, setRecordTypeFilter] = useState("ALL");
  const [selectedShopId, setSelectedShopId] = useState("");
  const [selectedLocationId, setSelectedLocationId] = useState("");
  const [showOnlyUnassigned, setShowOnlyUnassigned] = useState(true);
  const [linkMessage, setLinkMessage] = useState("");
  const [isSaving, setIsSaving] = useState(false);
  const activeShops = shops.filter((shop) => shop.active);
  const managedUsers = users.filter((user) => user.role !== "SUPER_ADMIN");
  const selectedShop = shops.find((shop) => String(shop.id) === String(selectedShopId));
  const selectedShopLocations = linkLocations.filter((location) => String(location.shopId) === String(selectedShopId) && location.active);
  const userFormLocations = linkLocations.filter((location) => String(location.shopId) === String(userForm.shopId) && location.active);
  const selectedShopAtLocationLimit = Boolean(selectedShop)
    && !selectedShop.multiLocationAllowed
    && Number(selectedShop.activeLocationCount || selectedShopLocations.length || 0) >= Number(selectedShop.locationLimit || 1);
  const filteredRecords = linkRecords.filter((record) => {
    const typeMatches = recordTypeFilter === "ALL" || record.type === recordTypeFilter;
    const assignmentMatches = !showOnlyUnassigned || !record.shopId;

    return typeMatches && assignmentMatches;
  });
  const linkedCount = linkRecords.filter((record) => record.shopId).length;
  const unlinkedCount = linkRecords.length - linkedCount;
  const linkMessageTone = /could not|requires|choose|not found|invalid|exists|must|failed|error|\/api/i.test(linkMessage)
    ? "error"
    : "found";

  useEffect(() => {
    setSelectedLocationId("");
    setLocationForm((current) => ({ ...current, shopId: selectedShopId }));
  }, [selectedShopId]);

  useEffect(() => {
    setUserForm((current) => ({ ...current, locationId: "" }));
  }, [userForm.shopId]);

  async function submitShop(event) {
    event.preventDefault();
    setIsSaving(true);
    setError("");

    try {
      if (editingShopId) {
        await updatePlatformShop(editingShopId, shopForm);
      } else {
        await createPlatformShop(shopForm);
      }

      setShopForm(emptyShopForm);
      setEditingShopId(null);
      await onRefresh();
    } catch (err) {
      setError(err.message);
    } finally {
      setIsSaving(false);
    }
  }

  function editShop(shop) {
    setEditingShopId(shop.id);
    setShopForm({
      name: shop.name || "",
      legalName: shop.legalName || "",
      phone: shop.phone || "",
      email: shop.email || "",
      address: shop.address || "",
      subscriptionPlan: shop.subscriptionPlan || "BASIC",
      active: Boolean(shop.active)
    });
  }

  async function setShopActive(shop) {
    setIsSaving(true);
    setError("");

    try {
      if (shop.active) {
        await deactivatePlatformShop(shop.id);
      } else {
        await activatePlatformShop(shop.id);
      }

      await onRefresh();
    } catch (err) {
      setError(err.message);
    } finally {
      setIsSaving(false);
    }
  }

  async function submitUser(event) {
    event.preventDefault();
    setIsSaving(true);
    setLinkMessage("");
    setError("");

    if (userForm.password !== userForm.confirmPassword) {
      const message = "Passwords do not match.";
      setLinkMessage(message);
      setError(message);
      setIsSaving(false);
      return;
    }

    try {
      await onCreateUser({
        ...userForm,
        shopId: userForm.shopId ? Number(userForm.shopId) : null,
        locationId: userForm.locationId ? Number(userForm.locationId) : null,
        hourlyRate: userForm.hourlyRate === "" ? null : Number(userForm.hourlyRate),
        payrollEnabled: Boolean(userForm.payrollEnabled),
        employmentType: userForm.employmentType || null
      });
      setUserForm(emptyPlatformUserForm);
      setLinkMessage("User account created.");
    } catch (err) {
      const message = err.message || "Could not create this user.";
      setLinkMessage(message);
      setError(message);
    } finally {
      setIsSaving(false);
    }
  }

  async function setUserActive(user) {
    setIsSaving(true);
    setLinkMessage("");
    setError("");

    try {
      const updatedUser = await onSetUserActive(user, !user.active);
      setLinkMessage(`${updatedUser.fullName} ${updatedUser.active ? "activated" : "deactivated"}.`);
    } catch (err) {
      setLinkMessage(err.message || "Could not update this account.");
    } finally {
      setIsSaving(false);
    }
  }

  async function submitLocation(event) {
    event.preventDefault();
    setIsSaving(true);
    setLinkMessage("");
    setError("");

    if (!locationForm.shopId) {
      setLinkMessage("Choose a shop before creating a location.");
      setIsSaving(false);
      return;
    }

    if (selectedShopAtLocationLimit && String(locationForm.shopId) === String(selectedShopId)) {
      setLinkMessage("Multi-location support requires a Premium plan.");
      setIsSaving(false);
      return;
    }

    try {
      await onCreateLocation({
        ...locationForm,
        shopId: Number(locationForm.shopId)
      });
      setLocationForm({ ...emptyPlatformLocationForm, shopId: selectedShopId });
      setLinkMessage("Location created.");
    } catch (err) {
      setLinkMessage(err.message || "Could not create location.");
    } finally {
      setIsSaving(false);
    }
  }

  async function linkRecord(record) {
    setIsSaving(true);
    setLinkMessage("");
    setError("");

    if (!selectedShopId && !selectedLocationId) {
      setLinkMessage("Choose a shop before linking records.");
      setIsSaving(false);
      return;
    }

    try {
      const locationAwareTypes = ["USER", "TIRE", "APPOINTMENT", "INVOICE", "ESTIMATE", "WORK_ORDER", "PAYROLL_PERIOD", "PAYROLL_RECORD", "EMPLOYEE_ATTENDANCE"];
      await onAssignRecord(record, {
        shopId: selectedShopId ? Number(selectedShopId) : null,
        locationId: locationAwareTypes.includes(record.type) && selectedLocationId ? Number(selectedLocationId) : null
      });
      setLinkMessage(`${record.label} linked to ${selectedShop?.name || "selected shop"}.`);
    } catch (err) {
      setLinkMessage(err.message || "Could not link this record.");
    } finally {
      setIsSaving(false);
    }
  }

  async function assignAdmin(record) {
    setIsSaving(true);
    setLinkMessage("");
    setError("");
    const assigningOwner = record.role === "OWNER";

    if (!selectedShopId) {
      setLinkMessage(`Choose a shop before assigning ${assigningOwner ? "an owner" : "a legacy admin"}.`);
      setIsSaving(false);
      return;
    }

    try {
      const updatedAdmin = await onAssignAdmin(record.id, Number(selectedShopId));
      setLinkMessage(`${updatedAdmin.fullName} is now ${assigningOwner ? "owner" : "legacy admin"} for ${updatedAdmin.shopName}.`);
    } catch (err) {
      setLinkMessage(err.message || `Could not assign this ${assigningOwner ? "owner" : "legacy admin"}.`);
    } finally {
      setIsSaving(false);
    }
  }

  async function assignLocationAdmin(record) {
    setIsSaving(true);
    setLinkMessage("");
    setError("");

    if (!selectedLocationId) {
      setLinkMessage("Choose a location before assigning a location admin.");
      setIsSaving(false);
      return;
    }

    try {
      const updatedAdmin = await onAssignLocation(record.id, Number(selectedLocationId));
      setLinkMessage(`${updatedAdmin.fullName} is now admin for ${updatedAdmin.locationName}.`);
    } catch (err) {
      setLinkMessage(err.message || "Could not assign this location admin.");
    } finally {
      setIsSaving(false);
    }
  }

  async function unlinkRecord(record) {
    setIsSaving(true);
    setLinkMessage("");
    setError("");

    try {
      await onAssignRecord(record, { shopId: null, locationId: null });
      setLinkMessage(`${record.label} moved back to Unassigned.`);
    } catch (err) {
      setLinkMessage(err.message || "Could not unlink this record.");
    } finally {
      setIsSaving(false);
    }
  }

  async function assignLegacyData() {
    setIsSaving(true);
    setLinkMessage("");
    setError("");

    if (!selectedShopId) {
      setLinkMessage("Choose a shop before assigning legacy data.");
      setIsSaving(false);
      return;
    }

    try {
      const result = await onAssignLegacyData(Number(selectedShopId));
      setShowOnlyUnassigned(true);
      setLinkMessage(result?.message || `Legacy data linked to ${selectedShop?.name || "selected shop"}.`);
    } catch (err) {
      setLinkMessage(err.message || "Could not assign legacy data.");
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <section className="work-area">
      <div className="section-toolbar">
        <div>
          <span className="eyebrow">Monarch Solutions</span>
          <h3>Platform Shops</h3>
        </div>
      </div>
      <form className="panel form-grid" onSubmit={submitShop}>
        {editingShopId && (
          <div className="form-banner">
            <span>Editing shop</span>
            <button className="ghost-button" onClick={() => { setEditingShopId(null); setShopForm(emptyShopForm); }} type="button">
              Cancel
            </button>
          </div>
        )}
        <Input label="Shop name" required value={shopForm.name} onChange={(name) => setShopForm({ ...shopForm, name })} />
        <Input label="Legal name" value={shopForm.legalName} onChange={(legalName) => setShopForm({ ...shopForm, legalName })} />
        <Input label="Phone" value={shopForm.phone} onChange={(phone) => setShopForm({ ...shopForm, phone })} />
        <Input label="Email" type="email" value={shopForm.email} onChange={(email) => setShopForm({ ...shopForm, email })} />
        <Input label="Address" value={shopForm.address} onChange={(address) => setShopForm({ ...shopForm, address })} />
        <Select
          label="Subscription"
          value={shopForm.subscriptionPlan}
          onChange={(subscriptionPlan) => setShopForm({ ...shopForm, subscriptionPlan })}
          options={["BASIC", "PREMIUM", "ENTERPRISE"]}
        />
        <label className="checkbox-line">
          <input
            checked={shopForm.active}
            onChange={(event) => setShopForm({ ...shopForm, active: event.target.checked })}
            type="checkbox"
          />
          <span>Active</span>
        </label>
        <button className="primary-button" disabled={isSaving} type="submit">
          {editingShopId ? "Update Shop" : "Create Shop"}
        </button>
      </form>
      <DataTable
        actions={(shop) => (
          <div className="table-actions">
            <button className="ghost-button" disabled={isSaving} onClick={() => editShop(shop)} type="button">
              Edit
            </button>
            <button className="ghost-button" disabled={isSaving} onClick={() => setShopActive(shop)} type="button">
              {shop.active ? "Deactivate" : "Activate"}
            </button>
          </div>
        )}
        columns={["Shop", "Owner", "Plan", "Locations", "Status", "Phone", "Email", "Address", ""]}
        emptyText="No shops created yet."
        rows={shops.map((shop) => {
          const locationLimitLabel = Number(shop.locationLimit || 1) > 1000000 ? "Unlimited" : shop.locationLimit || 1;
          return {
            key: `shop-${shop.id}`,
            source: shop,
            values: [
              shop.name,
              shop.ownerAdminName || "Unassigned",
              shop.subscriptionPlan || "BASIC",
              `${shop.activeLocationCount || 0}/${locationLimitLabel}${shop.overLocationLimit ? " over limit" : ""}`,
              shop.active ? "Active" : "Inactive",
              shop.phone || "-",
              shop.email || "-",
              shop.address || "-"
            ]
          };
        })}
      />

      <section className="panel platform-link-panel">
        <div className="section-toolbar compact">
          <div>
            <span className="eyebrow">Accounts</span>
            <h3>Create Users</h3>
          </div>
          <span className="audit-count">{managedUsers.length} managed</span>
        </div>
        <form className="platform-user-form" onSubmit={submitUser}>
          <Input label="Full name" required value={userForm.fullName} onChange={(fullName) => setUserForm({ ...userForm, fullName })} />
          <Input label="Email" required type="email" value={userForm.email} onChange={(email) => setUserForm({ ...userForm, email })} />
          <Input label="Phone" required type="tel" value={userForm.phone} onChange={(phone) => setUserForm({ ...userForm, phone })} />
          <Input
            label="Password"
            minLength="8"
            pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$"
            required
            title="Use at least 8 characters with uppercase, lowercase, number, and symbol"
            type="password"
            value={userForm.password}
            onChange={(password) => setUserForm({ ...userForm, password })}
          />
          <Input
            label="Confirm password"
            required
            type="password"
            value={userForm.confirmPassword}
            onChange={(confirmPassword) => setUserForm({ ...userForm, confirmPassword })}
          />
          <Select
            label="Role"
            value={userForm.role}
            onChange={(role) => setUserForm({
              ...userForm,
              role,
              locationId: role === "OWNER" || role === "CUSTOMER" ? "" : userForm.locationId
            })}
            options={["OWNER", "ADMIN", "EMPLOYEE", "CUSTOMER"]}
          />
          <Select
            label="Shop"
            value={userForm.shopId}
            onChange={(shopId) => setUserForm({ ...userForm, shopId })}
            options={["", ...activeShops.map((shop) => String(shop.id))]}
            optionLabel={(value) => {
              const shop = shops.find((entry) => String(entry.id) === String(value));
              return shop ? shop.name : "Unassigned";
            }}
          />
          {["ADMIN", "EMPLOYEE"].includes(userForm.role) && (
            <Select
              label="Location"
              value={userForm.locationId}
              onChange={(locationId) => setUserForm({ ...userForm, locationId })}
              options={["", ...userFormLocations.map((location) => String(location.id))]}
              optionLabel={(value) => {
                const location = linkLocations.find((entry) => String(entry.id) === String(value));
                return location ? location.name : userForm.role === "ADMIN" ? "Legacy all locations" : "Unassigned";
              }}
            />
          )}
          {userForm.role === "EMPLOYEE" && (
            <>
              <Input label="Hourly rate" min="0" step="0.01" type="number" value={userForm.hourlyRate} onChange={(hourlyRate) => setUserForm({ ...userForm, hourlyRate })} />
              <Select
                label="Employment"
                value={userForm.employmentType}
                onChange={(employmentType) => setUserForm({ ...userForm, employmentType })}
                options={["", "FULL_TIME", "PART_TIME", "CONTRACT", "SEASONAL"]}
                optionLabel={(value) => value || "Not set"}
              />
              <label className="checkbox-line">
                <input
                  checked={userForm.payrollEnabled}
                  onChange={(event) => setUserForm({ ...userForm, payrollEnabled: event.target.checked })}
                  type="checkbox"
                />
                <span>Payroll</span>
              </label>
            </>
          )}
          <label className="checkbox-line">
            <input
              checked={userForm.active}
              onChange={(event) => setUserForm({ ...userForm, active: event.target.checked })}
              type="checkbox"
            />
            <span>Active</span>
          </label>
          <button className="primary-button" disabled={isSaving} type="submit">
            Create User
          </button>
        </form>
        {linkMessage && <p className={`mini-status ${linkMessageTone}`}>{linkMessage}</p>}
        <div className="platform-link-controls">
          <Select
            label="Assignment shop"
            value={selectedShopId}
            onChange={setSelectedShopId}
            options={["", ...activeShops.map((shop) => String(shop.id))]}
            optionLabel={(value) => {
              const shop = shops.find((entry) => String(entry.id) === String(value));
              return shop ? shop.name : "Choose shop";
            }}
          />
          <Select
            label="Assignment location"
            value={selectedLocationId}
            onChange={setSelectedLocationId}
            options={["", ...selectedShopLocations.map((location) => String(location.id))]}
            optionLabel={(value) => {
              const location = linkLocations.find((entry) => String(entry.id) === String(value));
              return location ? location.name : "Choose location";
            }}
          />
        </div>
        <DataTable
          actions={(user) => (
            <div className="table-actions">
              {user.role === "OWNER" && (
                <button className="primary-button" disabled={isSaving || !selectedShopId} onClick={() => assignAdmin(user)} type="button">
                  Assign Owner
                </button>
              )}
              {user.role === "ADMIN" && (
                <>
                  <button className="ghost-button" disabled={isSaving || !selectedShopId} onClick={() => assignAdmin(user)} type="button">
                    Legacy Shop Admin
                  </button>
                  <button className="ghost-button" disabled={isSaving || !selectedLocationId} onClick={() => assignLocationAdmin(user)} type="button">
                    Location Admin
                  </button>
                </>
              )}
              <button className={user.active ? "danger-button" : "ghost-button"} disabled={isSaving} onClick={() => setUserActive(user)} type="button">
                {user.active ? "Deactivate" : "Activate"}
              </button>
            </div>
          )}
          columns={["User", "Role", "Shop", "Location", "Status", "Contact", ""]}
          emptyText="No platform-created users yet."
          rows={managedUsers.map((user) => ({
            key: `platform-user-${user.id}`,
            searchText: `${user.fullName} ${user.email} ${user.phone} ${user.role} ${user.shopName || ""}`,
            source: user,
            values: [
              user.fullName || "-",
              user.role || "-",
              user.shopName || "Unassigned",
              user.locationName || (user.role === "OWNER" ? "All locations" : user.role === "ADMIN" ? "Legacy all locations" : "-"),
              user.active ? "Active" : "Inactive",
              [user.email, user.phone].filter(Boolean).join(" / ") || "-"
            ]
          }))}
        />
      </section>

      <section className="metric-grid">
        <div className="metric-card"><span>Linkable records</span><strong>{linkRecords.length}</strong></div>
        <div className="metric-card"><span>Linked</span><strong>{linkedCount}</strong></div>
        <div className="metric-card"><span>Unassigned</span><strong>{unlinkedCount}</strong></div>
        <div className="metric-card"><span>Platform shops</span><strong>{shops.length}</strong></div>
        <div className="metric-card"><span>Locations</span><strong>{linkLocations.length}</strong></div>
      </section>

      <section className="panel platform-link-panel">
        <div className="section-toolbar compact">
          <div>
            <span className="eyebrow">Tenant setup</span>
            <h3>Link Existing Records</h3>
          </div>
          <button className="ghost-button" disabled={isSaving} onClick={onRefresh} type="button">
            Refresh
          </button>
        </div>
        <div className="platform-link-controls">
          <Select
            label="Assign to shop"
            value={selectedShopId}
            onChange={setSelectedShopId}
            options={["", ...activeShops.map((shop) => String(shop.id))]}
            optionLabel={(value) => {
              const shop = shops.find((entry) => String(entry.id) === String(value));
              return shop ? shop.name : "Choose shop";
            }}
          />
          <Select
            label="Tire location"
            value={selectedLocationId}
            onChange={setSelectedLocationId}
            options={["", ...selectedShopLocations.map((location) => String(location.id))]}
            optionLabel={(value) => {
              const location = linkLocations.find((entry) => String(entry.id) === String(value));
              return location ? location.name : "No location";
            }}
          />
          <Select
            label="Record type"
            value={recordTypeFilter}
            onChange={setRecordTypeFilter}
            options={platformRecordTypes}
            optionLabel={(value) => value === "ALL" ? "All records" : platformTypeLabel(value)}
          />
          <label className="checkbox-line">
            <input
              checked={showOnlyUnassigned}
              onChange={(event) => setShowOnlyUnassigned(event.target.checked)}
              type="checkbox"
            />
            <span>Unassigned only</span>
          </label>
          <button className="primary-button" disabled={isSaving || !selectedShopId} onClick={assignLegacyData} type="button">
            Assign Legacy Data
          </button>
        </div>

        <form className="location-management-form platform-location-form" onSubmit={submitLocation}>
          <Select
            label="Location shop"
            value={locationForm.shopId}
            onChange={(shopId) => setLocationForm({ ...locationForm, shopId })}
            options={["", ...activeShops.map((shop) => String(shop.id))]}
            optionLabel={(value) => {
              const shop = shops.find((entry) => String(entry.id) === String(value));
              return shop ? shop.name : "Choose shop";
            }}
          />
          <Input label="Location name" required value={locationForm.name} onChange={(name) => setLocationForm({ ...locationForm, name })} />
          <Select
            label="Type"
            value={locationForm.type}
            onChange={(type) => setLocationForm({ ...locationForm, type })}
            options={["STORE", "WAREHOUSE", "MOBILE_SERVICE", "OTHER", "STORAGE", "MOBILE"]}
          />
          <Input label="Address" value={locationForm.address} onChange={(address) => setLocationForm({ ...locationForm, address })} />
          <Input label="City" value={locationForm.city} onChange={(city) => setLocationForm({ ...locationForm, city })} />
          <Input label="Province / state" value={locationForm.province} onChange={(province) => setLocationForm({ ...locationForm, province })} />
          <Input label="Postal code" value={locationForm.postalCode} onChange={(postalCode) => setLocationForm({ ...locationForm, postalCode })} />
          <Input label="Phone" type="tel" value={locationForm.phone} onChange={(phone) => setLocationForm({ ...locationForm, phone })} />
          <Input label="Email" type="email" value={locationForm.email} onChange={(email) => setLocationForm({ ...locationForm, email })} />
          <label className="checkbox-line">
            <input
              checked={Boolean(locationForm.customerFacing)}
              onChange={(event) => setLocationForm({ ...locationForm, customerFacing: event.target.checked })}
              type="checkbox"
            />
            <span>Available for customer booking</span>
          </label>
          <button className="ghost-button" disabled={isSaving || selectedShopAtLocationLimit} type="submit">Add Location</button>
        </form>
        {selectedShopAtLocationLimit && <p className="mini-status">Basic shops can only have one active location. Upgrade to Premium to add more.</p>}
        {linkMessage && <p className={`mini-status ${linkMessageTone}`}>{linkMessage}</p>}
      </section>

      <DataTable
        actions={(record) => (
          <div className="table-actions">
            <button
              className="primary-button"
              disabled={isSaving || (!selectedShopId && !selectedLocationId) || (record.type === "USER" && String(record.status || "").includes("SUPER_ADMIN"))}
              onClick={() => linkRecord(record)}
              type="button"
            >
              Link
            </button>
            {record.shopId && (
              <button className="ghost-button" disabled={isSaving} onClick={() => unlinkRecord(record)} type="button">
                Unlink
              </button>
            )}
          </div>
        )}
        columns={["Type", "Record", "Current shop", "Location", "Status", "Details", ""]}
        emptyText="No records match this platform view."
        rows={filteredRecords.map((record) => ({
          key: `platform-link-${record.type}-${record.id}`,
          searchText: `${record.type} ${record.label} ${record.detail} ${record.shopName || ""}`,
          source: record,
          values: [
            platformTypeLabel(record.type),
            record.label || "-",
            record.shopName || "Unassigned",
            record.locationName || "-",
            record.status || "-",
            record.detail || "-"
          ]
        }))}
      />
    </section>
  );
}

function Tires({
  auth,
  filters,
  form,
  highlightedRow,
  onChange,
  onClearFilters,
  onDelete,
  onFilterChange,
  onFilterSubmit,
  onImportCsv,
  onCreateLocation,
  onRefill,
  onSubmit,
  shopLocations,
  tires
}) {
  const [importStatus, setImportStatus] = useState("idle");
  const [importMessage, setImportMessage] = useState("");
  const [importErrors, setImportErrors] = useState([]);
  const [locationForm, setLocationForm] = useState({
    name: "",
    type: "STORE",
    address: "",
    city: "",
    province: "",
    postalCode: "",
    phone: "",
    email: "",
    customerFacing: true
  });
  const [locationMessage, setLocationMessage] = useState("");
  const importFileInputRef = useRef(null);
  const canManageLocations = canUseMultiLocationScope(auth);

  useEffect(() => {
    if (!auth?.shopId) {
      return;
    }

    onChange((current) => {
      if (current.shopId && (!auth.locationId || current.locationId)) {
        return current;
      }

      return {
        ...current,
        shopId: String(auth.shopId),
        locationId: auth.locationId ? String(auth.locationId) : current.locationId
      };
    });
  }, [auth?.shopId, auth?.locationId, onChange]);

  function exportInventory() {
    const csv = toCsv(
      ["Brand", "Model", "Size", "Season", "Condition", "Quantity", "Reserved", "Available", "Price", "Location"],
      tires.map((tire) => [
        tire.brand,
        tire.model || "",
        `${tire.width}/${tire.aspectRatio}R${tire.rimSize}`,
        tire.season || "",
        tire.condition || "",
        tire.quantity,
        tire.reservedQuantity || 0,
        tire.availableQuantity ?? tire.quantity,
        tire.price,
        displayTireLocation(tire)
      ])
    );

    downloadTextFile("tiretrack-inventory.csv", csv, "text/csv");
  }

  function openCsvImport() {
    importFileInputRef.current?.click();
  }

  async function handleCsvFileChange(event) {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    setImportStatus("loading");
    setImportMessage("Importing CSV...");
    setImportErrors([]);

    try {
      const result = await onImportCsv(file);
      setImportStatus(result.skippedCount > 0 ? "error" : "found");
      setImportMessage(`${result.createdCount || 0} created, ${result.updatedCount || 0} refilled, ${result.skippedCount || 0} skipped.`);
      setImportErrors(result.errors || []);
    } catch (err) {
      setImportStatus("error");
      setImportMessage(err.message || "CSV import failed.");
      setImportErrors([]);
    } finally {
      event.target.value = "";
    }
  }

  async function submitLocation(event) {
    event.preventDefault();
    setLocationMessage("");

    try {
      await onCreateLocation(locationForm);
      setLocationForm({
        name: "",
        type: "STORE",
        address: "",
        city: "",
        province: "",
        postalCode: "",
        phone: "",
        email: "",
        customerFacing: true
      });
      setLocationMessage("Location created.");
    } catch (err) {
      setLocationMessage(err.message || "Could not create location.");
    }
  }

  return (
    <section className="work-area">
      <div className="section-toolbar">
        <div>
          <span className="eyebrow">Inventory</span>
          <h3>Tire Stock</h3>
        </div>
        <div className="toolbar-actions">
          <button className="primary-button with-icon" disabled={importStatus === "loading"} onClick={openCsvImport} type="button">
            <Upload size={16} />
            Import CSV
          </button>
          <button className="ghost-button with-icon" onClick={exportInventory} type="button">
            <Download size={16} />
            Export CSV
          </button>
          <input
            ref={importFileInputRef}
            accept=".csv,text/csv"
            className="hidden-file-input"
            onChange={handleCsvFileChange}
            type="file"
          />
        </div>
      </div>
      {importMessage && (
        <div className="tire-import-feedback">
          <p className={`mini-status ${importStatus}`}>
            {importMessage}
          </p>
          {importErrors.length > 0 && (
            <ul className="tire-import-errors">
              {importErrors.slice(0, 8).map((message) => (
                <li key={message}>{message}</li>
              ))}
            </ul>
          )}
        </div>
      )}
      <form className="panel form-grid" onSubmit={onSubmit}>
        <Input label="Brand" required value={form.brand} onChange={(brand) => onChange({ ...form, brand })} />
        <Input label="Model" value={form.model} onChange={(model) => onChange({ ...form, model })} />
        <Input label="Width" min="5" step="5" type="number" value={form.width} onChange={(width) => onChange({ ...form, width })} />
        <Input label="Aspect" min="1" type="number" value={form.aspectRatio} onChange={(aspectRatio) => onChange({ ...form, aspectRatio })} />
        <Input label="Rim" max="30" min="13" type="number" value={form.rimSize} onChange={(rimSize) => onChange({ ...form, rimSize })} />
        <Select label="Condition" required value={form.condition} onChange={(condition) => onChange({ ...form, condition })} options={["NEW", "USED"]} />
        <Input label="Season" value={form.season} onChange={(season) => onChange({ ...form, season })} />
        <Input label="Quantity" min="0" type="number" value={form.quantity} onChange={(quantity) => onChange({ ...form, quantity })} />
        <Input label="Price" min="0" required type="number" step="0.01" value={form.price} onChange={(price) => onChange({ ...form, price })} />
        <Input label="Location" value={form.location} onChange={(location) => onChange({ ...form, location })} />
        {canManageLocations && shopLocations.length > 0 && (
          <Select
            label="Shop location"
            value={form.locationId || ""}
            onChange={(locationId) => {
              const selectedLocation = shopLocations.find((location) => String(location.id) === String(locationId));
              onChange({
                ...form,
                shopId: String(auth.shopId),
                locationId,
                location: selectedLocation?.name || form.location
              });
            }}
            options={["", ...shopLocations.map((location) => String(location.id))]}
            optionLabel={(value) => {
              const location = shopLocations.find((entry) => String(entry.id) === String(value));
              return location ? location.name : "Unassigned";
            }}
          />
        )}
        <button className="primary-button" type="submit">Add / Refill Tire</button>
      </form>

      {canManageLocations && (
        <section className="panel location-management-panel">
          <form className="location-management-form" onSubmit={submitLocation}>
            <Input label="Location name" required value={locationForm.name} onChange={(name) => setLocationForm({ ...locationForm, name })} />
            <Select
              label="Type"
              value={locationForm.type}
              onChange={(type) => setLocationForm({ ...locationForm, type })}
              options={["STORE", "WAREHOUSE", "MOBILE_SERVICE", "OTHER", "STORAGE", "MOBILE"]}
            />
            <Input label="Address" value={locationForm.address} onChange={(address) => setLocationForm({ ...locationForm, address })} />
            <Input label="City" value={locationForm.city} onChange={(city) => setLocationForm({ ...locationForm, city })} />
            <Input label="Province / state" value={locationForm.province} onChange={(province) => setLocationForm({ ...locationForm, province })} />
            <Input label="Postal code" value={locationForm.postalCode} onChange={(postalCode) => setLocationForm({ ...locationForm, postalCode })} />
            <Input label="Phone" type="tel" value={locationForm.phone} onChange={(phone) => setLocationForm({ ...locationForm, phone })} />
            <Input label="Email" type="email" value={locationForm.email} onChange={(email) => setLocationForm({ ...locationForm, email })} />
            <label className="checkbox-line">
              <input
                checked={Boolean(locationForm.customerFacing)}
                onChange={(event) => setLocationForm({ ...locationForm, customerFacing: event.target.checked })}
                type="checkbox"
              />
              <span>Available for customer booking</span>
            </label>
            <button className="ghost-button" type="submit">Add Location</button>
          </form>
          {shopLocations.length > 0 && (
            <div className="location-chip-row">
              {shopLocations.map((location) => (
                <span key={location.id}>{location.name}</span>
              ))}
            </div>
          )}
          {locationMessage && <p className="mini-status">{locationMessage}</p>}
        </section>
      )}

      <form className="panel inventory-filters" onSubmit={onFilterSubmit}>
        <Select
          label="Filter by"
          value={filters.type}
          onChange={(type) => onFilterChange({ ...filters, type })}
          options={["brand", "season", "location", "condition", "size", "low-stock"]}
          optionLabel={(value) => filterLabel(value)}
        />
        {["brand", "season", "location"].includes(filters.type) && (
          <Input
            label="Search"
            required
            value={filters.query}
            onChange={(query) => onFilterChange({ ...filters, query })}
            placeholder={`Search by ${filterLabel(filters.type).toLowerCase()}`}
          />
        )}
        {filters.type === "condition" && (
          <Select
            label="Condition"
            value={filters.condition}
            onChange={(condition) => onFilterChange({ ...filters, condition })}
            options={["NEW", "USED"]}
          />
        )}
        {filters.type === "size" && (
          <>
            <Input label="Width" required type="number" value={filters.width} onChange={(width) => onFilterChange({ ...filters, width })} />
            <Input label="Aspect" required type="number" value={filters.aspectRatio} onChange={(aspectRatio) => onFilterChange({ ...filters, aspectRatio })} />
            <Input label="Rim" required type="number" value={filters.rimSize} onChange={(rimSize) => onFilterChange({ ...filters, rimSize })} />
          </>
        )}
        {filters.type === "low-stock" && (
          <Input
            label="Threshold"
            min="0"
            required
            type="number"
            value={filters.threshold}
            onChange={(threshold) => onFilterChange({ ...filters, threshold })}
          />
        )}
        <div className="filter-actions">
          <button className="primary-button" type="submit">Search</button>
          <button className="ghost-button" onClick={onClearFilters} type="button">Clear</button>
        </div>
      </form>

      <InventoryTable highlightedRow={highlightedRow} onDelete={onDelete} onRefill={onRefill} tires={tires} />
    </section>
  );
}

function filterLabel(value) {
  const labels = {
    brand: "Brand",
    season: "Season",
    location: "Location",
    condition: "Condition",
    size: "Size",
    "low-stock": "Low stock"
  };

  return labels[value] || value;
}

function filterRecordsByLocation(records, locationId) {
  if (!locationId) {
    return records || [];
  }

  return (records || []).filter((record) => String(record.locationId || "") === String(locationId));
}

function sameLocationValue(first, second) {
  return String(first || "") === String(second || "");
}

function Appointments({
  appointments,
  customers,
  editingId,
  form,
  highlightedRow,
  onCancelEdit,
  onConfirmTireRequest,
  onChange,
  onDelete,
  onEdit,
  onSubmit,
  onTireRequestStatusChange,
  selectedLocationId = "",
  shopLocations = [],
  tireRequests = [],
  tires
}) {
  const matchingCustomers = matchingCustomersForForm(customers, form);
  const selectedCustomer = customers.find((customer) => Number(customer.id) === Number(form.customerId));
  const selectedCustomerVehicles = selectedCustomer?.vehicles || [];
  const [availability, setAvailability] = useState(null);
  const [availabilityError, setAvailabilityError] = useState("");
  const [isCheckingAvailability, setIsCheckingAvailability] = useState(false);
  const selectedVehicleId = form.customerVehicleId || "";
  const selectedAppointmentLocationId = form.locationId || selectedLocationId || "";

  useEffect(() => {
    let cancelled = false;

    async function checkAvailability() {
      if (!selectedVehicleId || !usesInventoryTires(form.serviceType)) {
        setAvailability(null);
        setAvailabilityError("");
        return;
      }

      setIsCheckingAvailability(true);
      setAvailabilityError("");

      try {
        const result = await getAppointmentTireAvailability(selectedVehicleId, selectedAppointmentLocationId, form.serviceType);
        if (!cancelled) {
          setAvailability(result);
        }
      } catch (err) {
        if (!cancelled) {
          setAvailability(null);
          setAvailabilityError(err.message || "Tire availability could not be checked.");
        }
      } finally {
        if (!cancelled) {
          setIsCheckingAvailability(false);
        }
      }
    }

    checkAvailability();

    return () => {
      cancelled = true;
    };
  }, [selectedVehicleId, selectedAppointmentLocationId, form.serviceType]);

  function selectCustomer(customer) {
    onChange({
      ...form,
      customerId: String(customer.id),
      customerName: customer.fullName || "",
      email: customer.email || "",
      phone: customer.phone || "",
      locationId: customer.locationId ? String(customer.locationId) : form.locationId
    });
  }

  function selectCustomerVehicle(vehicleId) {
    const vehicle = selectedCustomerVehicles.find((entry) => String(entry.id) === String(vehicleId));

    if (!vehicle) {
      onChange({ ...form, customerVehicleId: "", vehicle: "" });
      return;
    }

    const tireSetup = vehicle.tireSetup === "staggered"
      ? {
        tireSetup: "staggered",
        tireSize: "",
        frontTireSize: vehicle.frontTireSize || "",
        rearTireSize: vehicle.rearTireSize || "",
        frontQuantity: 2,
        rearQuantity: 2
      }
      : {
        tireSetup: "regular",
        tireSize: vehicle.tireSize || "",
        frontTireSize: "",
        rearTireSize: "",
        frontQuantity: 4,
        rearQuantity: 0
      };

    onChange({
      ...form,
      customerVehicleId: String(vehicle.id),
      vehicle: vehicleName(vehicle),
      locationId: vehicle.locationId ? String(vehicle.locationId) : form.locationId,
      overrideTireAvailability: false,
      tireAvailabilityOverrideReason: "",
      ...tireSetup
    });
  }

  return (
    <section className="work-area">
      <form className="panel form-grid appointment-form" onSubmit={onSubmit}>
        {editingId && (
          <div className="form-banner">
            <span>Editing appointment</span>
            <button className="ghost-button" onClick={onCancelEdit} type="button">
              Cancel
            </button>
          </div>
        )}
        <div className="appointment-customer-lookup">
          <div className="appointment-customer-fields">
            <Input label="Customer" required value={form.customerName} onChange={(customerName) => onChange({ ...form, customerName, customerId: "" })} />
            <Input label="Email" type="email" value={form.email} onChange={(email) => onChange({ ...form, email, customerId: "" })} />
            <Input label="Phone" required type="tel" value={form.phone} onChange={(phone) => onChange({ ...form, phone, customerId: "" })} />
          </div>
          {matchingCustomers.length > 0 && (
            <div className="customer-match-panel appointment-customer-results">
              <span>Matching customers</span>
              {matchingCustomers.map((customer) => (
                <button key={customer.id} onClick={() => selectCustomer(customer)} type="button">
                  <strong>{customer.fullName}</strong>
                  <small>{[customer.email, customer.phone].filter(Boolean).join(" - ")}</small>
                </button>
              ))}
            </div>
          )}
          {selectedCustomerVehicles.length > 0 && (
            <div className="appointment-customer-vehicle">
              <Select
                label="Customer vehicle"
                value={form.customerVehicleId || ""}
                onChange={selectCustomerVehicle}
                options={["", ...selectedCustomerVehicles.map((vehicle) => String(vehicle.id))]}
                optionLabel={(value) => {
                  const vehicle = selectedCustomerVehicles.find((entry) => String(entry.id) === String(value));
                  return value ? `${vehicleName(vehicle)} - ${vehicleTireSize(vehicle)}` : "Select saved vehicle";
                }}
              />
            </div>
          )}
        </div>
        <Input
          label="Vehicle"
          placeholder="Example: 2020 Toyota Camry"
          value={form.vehicle}
          onChange={(vehicle) => onChange({ ...form, vehicle })}
        />
        <ServiceTypeSelect required value={form.serviceType} onChange={(serviceType) => onChange({ ...form, serviceType })} />
        {usesInventoryTires(form.serviceType) ? (
          <TireSetupFields
            disabled={false}
            form={form}
            onChange={onChange}
            tires={tires}
          />
        ) : (
          <OwnTireServiceNote serviceType={form.serviceType} />
        )}
        <TireAvailabilityPanel
          availability={availability}
          error={availabilityError}
          isLoading={isCheckingAvailability}
          mode="staff"
        />
        {needsTireSourcing(availability) && (
          <div className={`availability-override-panel ${form.overrideTireAvailability ? "override-active" : ""}`}>
            <div className="availability-override-copy">
              <span className="availability-override-icon">
                {form.overrideTireAvailability ? <ShieldCheck size={18} /> : <AlertTriangle size={18} />}
              </span>
              <div>
                <strong>{form.overrideTireAvailability ? "Manager override is on" : "Tire sourcing required"}</strong>
                <p>
                  {form.overrideTireAvailability
                    ? "This books the appointment without confirmed stock. Keep the reason specific so the team knows why it was approved."
                    : "Current stock cannot confirm this installation. Save it as a sourcing request, or use an override only when the tires are otherwise handled."}
                </p>
              </div>
            </div>
            <label className="checkbox-row override-toggle">
              <input
                checked={Boolean(form.overrideTireAvailability)}
                onChange={(event) => onChange({ ...form, overrideTireAvailability: event.target.checked })}
                type="checkbox"
              />
              <span>Book anyway with manager override</span>
            </label>
            {form.overrideTireAvailability && (
              <Input
                label="Override reason"
                required
                value={form.tireAvailabilityOverrideReason || ""}
                onChange={(tireAvailabilityOverrideReason) => onChange({ ...form, tireAvailabilityOverrideReason })}
                placeholder="Example: Customer bringing tires, supplier confirmed delivery, manager approval"
              />
            )}
          </div>
        )}
        <AppointmentDatePicker
          appointments={appointments}
          editingId={editingId}
          locationId={form.locationId || selectedLocationId || ""}
          value={form.appointmentDate}
          onChange={(appointmentDate) => onChange({ ...form, appointmentDate })}
        />
        {shopLocations.length > 0 && (
          <Select
            label="Location"
            value={form.locationId || selectedLocationId || ""}
            onChange={(locationId) => onChange({ ...form, locationId })}
            options={["", ...shopLocations.map((location) => String(location.id))]}
            optionLabel={(value) => {
              const location = shopLocations.find((entry) => String(entry.id) === String(value));
              return location ? location.name : "Unassigned";
            }}
          />
        )}
        <Select label="Status" value={form.status} onChange={(status) => onChange({ ...form, status })} options={["BOOKED", "PENDING_TIRE_AVAILABILITY", "COMPLETED", "CANCELLED"]} optionLabel={appointmentStatusLabel} />
        <div className="appointment-reminder-row">
          <Select
            label="Reminder"
            value={form.reminderStatus}
            onChange={(reminderStatus) => onChange({
              ...form,
              reminderStatus,
              reminderAt: reminderStatus === "NOT_SET" ? "" : form.reminderAt
            })}
            options={["NOT_SET", "SCHEDULED", "SENT"]}
            optionLabel={reminderStatusLabel}
          />
          <Input
            label="Reminder date & time"
            type="datetime-local"
            value={form.reminderAt || ""}
            onChange={(reminderAt) => onChange({
              ...form,
              reminderAt,
              reminderStatus: reminderAt ? "SCHEDULED" : "NOT_SET"
            })}
          />
          <Select
            label="Confirmation"
            value={form.confirmationStatus}
            onChange={(confirmationStatus) => onChange({ ...form, confirmationStatus })}
            options={["PENDING", "CONFIRMED", "NO_SHOW"]}
            optionLabel={confirmationStatusLabel}
          />
        </div>
        <Input label="Cancel / no-show reason" value={form.cancelReason || ""} onChange={(cancelReason) => onChange({ ...form, cancelReason })} />
        <Input label="Notes" value={form.notes} onChange={(notes) => onChange({ ...form, notes })} />
        <button className="primary-button" type="submit">
          {appointmentSubmitLabel(editingId, form, availability)}
        </button>
      </form>

      <TireRequestQueue
        onConfirmAppointment={onConfirmTireRequest}
        onStatusChange={onTireRequestStatusChange}
        requests={tireRequests}
      />

      <DataTable
        highlightedRow={highlightedRow}
        actions={(appointment) => (
          <div className="table-actions">
            <button className="ghost-button" onClick={() => onEdit(appointment)} type="button">
              Edit
            </button>
            <button className="danger-button" onClick={() => onDelete(appointment.id)} type="button">
              Delete
            </button>
          </div>
        )}
        columns={["Customer", "Email", "Phone", "Vehicle", "Tire setup", "Location", "Date", "Service", "Reminder", "Confirm", "Status", ""]}
        emptyText="No appointments yet."
        rows={appointments.map((appointment) => {
          const linkedCustomer = customerForAppointment(appointment, customers);
          const email = appointment.email || linkedCustomer?.email || "-";

          return {
            key: `appointment-${appointment.id}`,
            searchText: [
              appointment.customerName,
              email,
              appointment.email,
              appointment.phone,
              linkedCustomer?.phone,
              linkedCustomer?.email,
              appointment.vehicle,
              appointment.tireSize,
              appointment.locationName,
              serviceTypeLabel(appointment.serviceType),
              appointment.notes,
              appointment.reminderStatus,
              reminderStatusLabel(appointment.reminderStatus),
              appointment.confirmationStatus,
              confirmationStatusLabel(appointment.confirmationStatus),
              appointment.status
            ].filter(Boolean).join(" "),
            values: [
              appointment.customerName,
              email,
              appointment.phone,
              appointment.vehicle || "-",
              appointment.tireSize || "-",
              appointment.locationName || "Unassigned",
              dateTime(appointment.appointmentDate),
              serviceTypeLabel(appointment.serviceType),
              reminderStatusLabel(appointment.reminderStatus),
              confirmationStatusLabel(appointment.confirmationStatus),
              appointment.status || "-"
            ],
            source: appointment
          };
        })}
      />
    </section>
  );
}

function TireAvailabilityPanel({ availability, error, isLoading, mode = "customer" }) {
  if (isLoading) {
    return <div className="tire-availability-card loading">Checking tire availability...</div>;
  }

  if (error) {
    return <div className="tire-availability-card error">{error}</div>;
  }

  if (!availability || !availability.tireServiceRequired) {
    return null;
  }

  const otherLocations = availability.otherLocationAvailability || [];
  const warehouseAvailability = availability.warehouseAvailability || [];

  return (
    <div className={`tire-availability-card ${tireAvailabilityTone(availability.status)}`}>
      <div className="availability-card-head">
        <div>
          <span className="eyebrow">Tire availability</span>
          <strong>{statusLabel(availability.status)}</strong>
        </div>
        <span className={`status-badge ${tireAvailabilityTone(availability.status)}`}>{statusLabel(availability.status)}</span>
      </div>
      <p>{availability.reason}</p>
      <div className="availability-metrics">
        <div><span>Required size</span><strong>{availability.requiredSize || "-"}</strong></div>
        <div><span>Selected location</span><strong>{availability.selectedLocationAvailableQuantity ?? 0}</strong></div>
      </div>
      {otherLocations.length > 0 && (
        <div className="availability-location-list">
          <span>{mode === "customer" ? "Other customer locations" : "Other shop locations"}</span>
          {otherLocations.map((location) => (
            <small key={`other-${location.locationId || location.locationName}`}>
              {location.locationName}: {location.availableQuantity} available
            </small>
          ))}
        </div>
      )}
      {mode === "staff" && warehouseAvailability.length > 0 && (
        <div className="availability-location-list internal">
          <span>Internal stock</span>
          {warehouseAvailability.map((location) => (
            <small key={`warehouse-${location.locationId || location.locationName}`}>
              {location.locationName}: {location.availableQuantity} available
            </small>
          ))}
        </div>
      )}
    </div>
  );
}

function TireRequestQueue({ onConfirmAppointment, onStatusChange, requests }) {
  const visibleRequests = [...(requests || [])].sort((first, second) => Number(second.id || 0) - Number(first.id || 0));
  const [responses, setResponses] = useState({});

  function responseFor(requestId) {
    return responses[requestId] || "";
  }

  function setResponse(requestId, value) {
    setResponses((current) => ({ ...current, [requestId]: value }));
  }

  return (
    <section className="panel tire-request-panel">
      <div className="section-toolbar">
        <div>
          <span className="eyebrow">Operations</span>
          <h3>Pending Tire Requests</h3>
        </div>
        <span className="audit-count">{visibleRequests.length} request{visibleRequests.length === 1 ? "" : "s"}</span>
      </div>
      <DataTable
        actions={(request) => (
          <div className="tire-request-actions">
            <input
              aria-label="Shop response"
              onChange={(event) => setResponse(request.id, event.target.value)}
              placeholder="Optional response"
              value={responseFor(request.id)}
            />
            <button className="ghost-button" onClick={() => onStatusChange(request, "SOURCING", responseFor(request.id))} type="button">Sourcing</button>
            <button className="ghost-button" onClick={() => onStatusChange(request, "AVAILABLE", responseFor(request.id))} type="button">Available</button>
            <button className="ghost-button" onClick={() => onStatusChange(request, "UNAVAILABLE", responseFor(request.id))} type="button">Unavailable</button>
            {request.appointmentId && ["SOURCING", "AVAILABLE"].includes(String(request.status || "").toUpperCase()) && (
              <button className="primary-button" onClick={() => onConfirmAppointment(request)} type="button">Confirm appointment</button>
            )}
          </div>
        )}
        columns={["Customer", "Vehicle", "Tire Size", "Location", "Appointment", "Status", "Response", ""]}
        emptyText="No tire sourcing requests."
        rows={visibleRequests.map((request) => ({
          key: `tire-request-${request.id}`,
          source: request,
          searchText: [request.customerName, request.vehicle, request.requestedSize, request.locationName, request.status, request.adminResponse].filter(Boolean).join(" "),
          values: [
            request.customerName || "-",
            request.vehicle || "-",
            request.requestedSize || "-",
            request.locationName || "Unassigned",
            request.appointmentDate ? dateTime(request.appointmentDate) : request.appointmentId ? `#${request.appointmentId}` : "-",
            request.status || "PENDING",
            request.adminResponse || "-"
          ]
        }))}
      />
    </section>
  );
}

function WorkOrdersPage({ appointments, customers, onInvoiceCreated, onNotify, onRefresh, selectedLocationId = "", setError, shopLocations = [], workOrders }) {
  const [form, setForm] = useState(emptyWorkOrder);
  const [editingId, setEditingId] = useState(null);
  const [invoicePreview, setInvoicePreview] = useState(null);
  const [previewWorkOrderId, setPreviewWorkOrderId] = useState(null);
  const sortedWorkOrders = [...workOrders].sort((first, second) => Number(second.id || 0) - Number(first.id || 0));
  const workOrderAppointmentIds = new Set(workOrders.map((workOrder) => Number(workOrder.appointmentId)).filter(Boolean));
  const availableAppointments = appointments.filter((appointment) => !workOrderAppointmentIds.has(Number(appointment.id)));
  const selectedAppointment = appointments.find((appointment) => String(appointment.id) === String(form.appointmentId));
  const selectedCustomer = customers.find((customer) => Number(customer.id) === Number(form.customerId));
  const matchingCustomers = matchingCustomersForForm(customers, form, 4);

  useEffect(() => {
    if (invoicePreview) {
      scrollToSelector("#work-order-invoice-preview");
    }
  }, [invoicePreview]);

  function resetForm() {
    setForm(emptyWorkOrder);
    setEditingId(null);
  }

  function chooseAppointment(appointmentId) {
    const appointment = appointments.find((entry) => String(entry.id) === String(appointmentId));

    if (!appointment) {
      setForm({ ...form, appointmentId: "" });
      return;
    }

    setForm({
      ...form,
      appointmentId,
      customerId: appointment.customerId ? String(appointment.customerId) : "",
      customerName: appointment.customerName || "",
      email: appointment.email || "",
      phone: appointment.phone || "",
      vehicle: appointment.vehicle || "",
      locationId: appointment.locationId ? String(appointment.locationId) : form.locationId,
      serviceType: appointment.serviceType || "INSTALLATION",
      notes: appointment.notes || ""
    });
  }

  function selectCustomer(customer) {
    setForm({
      ...form,
      customerId: String(customer.id),
      customerName: customer.fullName || "",
      email: customer.email || "",
      phone: customer.phone || "",
      locationId: customer.locationId ? String(customer.locationId) : form.locationId
    });
  }

  function editWorkOrder(workOrder) {
    setEditingId(workOrder.id);
    setForm({
      appointmentId: workOrder.appointmentId ? String(workOrder.appointmentId) : "",
      customerId: workOrder.customerId ? String(workOrder.customerId) : "",
      customerName: workOrder.customerName || "",
      email: workOrder.email || "",
      phone: workOrder.phone || "",
      vehicle: workOrder.vehicle || "",
      assignedEmployeeId: workOrder.assignedEmployeeId ? String(workOrder.assignedEmployeeId) : "",
      locationId: workOrder.locationId ? String(workOrder.locationId) : "",
      serviceType: workOrder.serviceType || "INSTALLATION",
      notes: workOrder.notes || ""
    });
  }

  function payloadFromForm() {
    return {
      appointmentId: form.appointmentId ? Number(form.appointmentId) : null,
      customerId: form.customerId ? Number(form.customerId) : null,
      customerName: form.customerName,
      email: form.email,
      phone: form.phone,
      vehicle: form.vehicle,
      assignedEmployeeId: form.assignedEmployeeId ? Number(form.assignedEmployeeId) : null,
      locationId: form.locationId ? Number(form.locationId) : selectedLocationId ? Number(selectedLocationId) : null,
      serviceType: form.serviceType,
      notes: form.notes
    };
  }

  async function submitWorkOrder(event) {
    event.preventDefault();
    setError("");

    try {
      if (editingId) {
        await updateWorkOrder(editingId, payloadFromForm());
      } else {
        await createWorkOrder(payloadFromForm());
      }

      resetForm();
      onNotify(editingId ? "Work order updated." : "Work order created.");
      await onRefresh();
    } catch (err) {
      setError(err.message);
    }
  }

  async function createFromAppointment(appointmentId) {
    setError("");

    try {
      await createWorkOrderFromAppointment(appointmentId);
      onNotify("Work order created from appointment.");
      await onRefresh();
    } catch (err) {
      setError(err.message);
    }
  }

  async function runWorkOrderAction(message, action) {
    setError("");

    try {
      await action();
      onNotify(message);
      await onRefresh();
    } catch (err) {
      setError(err.message);
    }
  }

  async function invoiceWorkOrder(workOrder) {
    setError("");

    try {
      const preview = await previewWorkOrderInvoice(workOrder.id);
      setInvoicePreview(preview);
      setPreviewWorkOrderId(workOrder.id);
    } catch (err) {
      setError(err.message);
    }
  }

  async function confirmInvoiceWorkOrder() {
    if (!previewWorkOrderId) {
      return;
    }

    setError("");

    try {
      const invoice = await convertWorkOrderToInvoice(previewWorkOrderId);
      onNotify("Invoice created from work order.");
      setInvoicePreview(null);
      setPreviewWorkOrderId(null);
      await onRefresh();
      await onInvoiceCreated(invoice);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <section className="work-area">
      <div className="section-toolbar">
        <div>
          <span className="eyebrow">Jobs</span>
          <h3>Work Orders</h3>
        </div>
        <div className="toolbar-actions">
          <StatusBadge value="PENDING" />
          <StatusBadge value="IN_PROGRESS" />
          <StatusBadge value="VEHICLE_READY" />
        </div>
      </div>

      <form className="panel form-grid" onSubmit={submitWorkOrder}>
        {editingId && (
          <div className="form-banner">
            <span>Editing work order</span>
            <button className="ghost-button" onClick={resetForm} type="button">
              Cancel
            </button>
          </div>
        )}
        <div className="invoice-source">
          <div>
            <span className="eyebrow">Appointment source</span>
            <h3>{selectedAppointment ? selectedAppointment.customerName : "Manual work order"}</h3>
            <p>{selectedAppointment ? `${serviceTypeLabel(selectedAppointment.serviceType)} - ${dateTime(selectedAppointment.appointmentDate)}` : "Choose an appointment to fill customer, vehicle, and service details."}</p>
          </div>
          <Select
            label="Choose appointment"
            value={form.appointmentId}
            onChange={chooseAppointment}
            options={["", ...appointments.map((appointment) => String(appointment.id))]}
            optionLabel={(value) => {
              const appointment = appointments.find((entry) => String(entry.id) === String(value));
              return appointment ? `${appointment.customerName} - ${dateTime(appointment.appointmentDate)}` : "Manual work order";
            }}
          />
        </div>
        <Input label="Customer" required value={form.customerName} onChange={(customerName) => setForm({ ...form, customerName, customerId: "" })} />
        <Input label="Email" type="email" value={form.email} onChange={(email) => setForm({ ...form, email, customerId: "" })} />
        <Input label="Phone" type="tel" value={form.phone} onChange={(phone) => setForm({ ...form, phone, customerId: "" })} />
        {matchingCustomers.length > 0 && (
          <div className="customer-match-panel">
            <span>Matching customers</span>
            {matchingCustomers.map((customer) => (
              <button key={customer.id} onClick={() => selectCustomer(customer)} type="button">
                <strong>{customer.fullName}</strong>
                <small>{[customer.email, customer.phone].filter(Boolean).join(" - ")}</small>
              </button>
            ))}
          </div>
        )}
        {selectedCustomer?.vehicles?.length > 0 && (
          <Select
            label="Customer vehicle"
            value=""
            onChange={(vehicleId) => {
              const vehicle = selectedCustomer.vehicles.find((entry) => String(entry.id) === String(vehicleId));
              setForm({ ...form, vehicle: vehicleName(vehicle), locationId: vehicle?.locationId ? String(vehicle.locationId) : form.locationId });
            }}
            options={["", ...selectedCustomer.vehicles.map((vehicle) => String(vehicle.id))]}
            optionLabel={(value) => {
              const vehicle = selectedCustomer.vehicles.find((entry) => String(entry.id) === String(value));
              return value ? vehicleName(vehicle) : "Select saved vehicle";
            }}
          />
        )}
        <Input label="Vehicle" value={form.vehicle} onChange={(vehicle) => setForm({ ...form, vehicle })} />
        {shopLocations.length > 0 && (
          <Select
            label="Location"
            value={form.locationId || selectedLocationId || ""}
            onChange={(locationId) => setForm({ ...form, locationId })}
            options={["", ...shopLocations.map((location) => String(location.id))]}
            optionLabel={(value) => {
              const location = shopLocations.find((entry) => String(entry.id) === String(value));
              return location ? location.name : "Unassigned";
            }}
          />
        )}
        <ServiceTypeSelect required value={form.serviceType} onChange={(serviceType) => setForm({ ...form, serviceType })} />
        <OwnTireServiceNote serviceType={form.serviceType} />
        <Input label="Notes" value={form.notes} onChange={(notes) => setForm({ ...form, notes })} />
        <button className="primary-button" type="submit">
          {editingId ? "Save Work Order" : "Create Work Order"}
        </button>
      </form>

      {invoicePreview && (
        <section className="panel invoice-conversion-preview" id="work-order-invoice-preview">
          <div className="section-toolbar compact">
            <div>
              <span className="eyebrow">Invoice preview</span>
              <h3>{invoicePreview.customerName}</h3>
              <p>{invoicePreview.vehicle || "No vehicle"} - review before saving the final invoice.</p>
            </div>
            <div className="toolbar-actions">
              <button className="primary-button" onClick={confirmInvoiceWorkOrder} type="button">
                Confirm Invoice
              </button>
              <button className="ghost-button" onClick={() => { setInvoicePreview(null); setPreviewWorkOrderId(null); }} type="button">
                Cancel
              </button>
            </div>
          </div>
          <DataTable
            columns={["Item", "Qty", "Unit", "Total"]}
            emptyText="No invoice items."
            rows={(invoicePreview.items || []).map((item, index) => ({
              key: `preview-item-${index}`,
              values: [
                item.itemName || item.itemType,
                item.quantity,
                money(item.unitPrice),
                money(Number(item.quantity || 0) * Number(item.unitPrice || 0))
              ]
            }))}
          />
        </section>
      )}

      {availableAppointments.length > 0 && (
        <DataTable
          actions={(appointment) => (
            <button className="ghost-button" onClick={() => createFromAppointment(appointment.id)} type="button">
              Create Work Order
            </button>
          )}
          columns={["Appointment", "Customer", "Vehicle", "Location", "Service", ""]}
          emptyText="No appointments waiting for work orders."
          rows={availableAppointments.map((appointment) => ({
            key: `appointment-work-source-${appointment.id}`,
            values: [
              dateTime(appointment.appointmentDate),
              appointment.customerName,
              appointment.vehicle || "-",
              appointment.locationName || "Unassigned",
              serviceTypeLabel(appointment.serviceType)
            ],
            source: appointment
          }))}
        />
      )}

      <DataTable
        actions={(workOrder) => (
          <div className="table-actions">
            {workOrder.status !== "COMPLETED" && workOrder.status !== "CANCELLED" && (
              <button className="ghost-button" onClick={() => editWorkOrder(workOrder)} type="button">
                Edit
              </button>
            )}
            {workOrder.status === "PENDING" && (
              <button className="ghost-button" onClick={() => runWorkOrderAction("Work order started.", () => startWorkOrder(workOrder.id))} type="button">
                Start Job
              </button>
            )}
            {["PENDING", "IN_PROGRESS"].includes(workOrder.status) && (
              <button className="ghost-button" onClick={() => runWorkOrderAction("Vehicle marked ready.", () => markWorkOrderVehicleReady(workOrder.id))} type="button">
                Vehicle Ready
              </button>
            )}
            {workOrder.status !== "COMPLETED" && workOrder.status !== "CANCELLED" && (
              <button className="ghost-button" onClick={() => runWorkOrderAction("Work order completed.", () => completeWorkOrder(workOrder.id))} type="button">
                Complete
              </button>
            )}
            {workOrder.status !== "CANCELLED" && !workOrder.invoiceId && (
              <button className="ghost-button" onClick={() => invoiceWorkOrder(workOrder)} type="button">
                Create Invoice
              </button>
            )}
            {workOrder.status !== "COMPLETED" && workOrder.status !== "CANCELLED" && !workOrder.invoiceId && (
              <button className="danger-button" onClick={() => runWorkOrderAction("Work order cancelled.", () => cancelWorkOrder(workOrder.id))} type="button">
                Cancel
              </button>
            )}
          </div>
        )}
        columns={["Customer", "Vehicle", "Location", "Service", "Assigned", "Appointment", "Status", "Invoice", ""]}
        emptyText="No work orders yet."
        rows={sortedWorkOrders.map((workOrder) => ({
          key: `work-order-${workOrder.id}`,
          searchText: [workOrder.customerName, workOrder.phone, workOrder.email, workOrder.vehicle, workOrder.locationName, workOrder.status, workOrder.serviceType].join(" "),
          values: [
            workOrder.customerName,
            workOrder.vehicle || "-",
            workOrder.locationName || "Unassigned",
            serviceTypeLabel(workOrder.serviceType),
            workOrder.assignedEmployeeName || "-",
            dateTime(workOrder.appointmentDate),
            workOrder.status || "PENDING",
            workOrder.invoiceId ? `#${workOrder.invoiceId}` : "-"
          ],
          source: workOrder
        }))}
      />
    </section>
  );
}

function EstimatesPage({ customers, estimates, onNotify, onRefresh, onStartInvoice, selectedLocationId = "", settings, setError, shopLocations = [], tires }) {
  const [form, setForm] = useState(() => ({ ...emptyEstimate, taxRate: settings?.taxRate || "13" }));
  const [editingId, setEditingId] = useState(null);
  const [tireSearch, setTireSearch] = useState("");
  const [previewEstimate, setPreviewEstimate] = useState(null);
  const sortedEstimates = [...estimates].sort((first, second) => Number(second.id || 0) - Number(first.id || 0));
  const selectedCustomer = customers.find((customer) => Number(customer.id) === Number(form.customerId));
  const subtotal = form.items.reduce((total, item) => total + Number(item.quantity || 0) * Number(item.unitPrice || 0), 0);
  const taxRate = Number(form.taxRate || 0) > 1 ? Number(form.taxRate || 0) / 100 : Number(form.taxRate || 0);
  const tax = subtotal * taxRate;
  const matchingCustomers = matchingCustomersForForm(customers, form, 4);
  const normalizedTireSearch = tireSearch.trim().toLowerCase();
  const estimateTireOptions = normalizedTireSearch
    ? tires.filter((tire) => [
      tire.brand,
      tire.model,
      tire.width && tire.aspectRatio && tire.rimSize ? `${tire.width}/${tire.aspectRatio}R${tire.rimSize}` : "",
      tire.condition,
      tire.season,
      tire.location,
      tire.locationName
    ].filter(Boolean).join(" ").toLowerCase().includes(normalizedTireSearch))
    : tires;

  useEffect(() => {
    if (previewEstimate) {
      scrollToSelector("#estimate-preview");
    }
  }, [previewEstimate]);

  function resetForm() {
    setForm({ ...emptyEstimate, taxRate: settings?.taxRate || "13", items: [makeInvoiceItem()] });
    setEditingId(null);
  }

  function selectCustomer(customer) {
    setForm({
      ...form,
      customerId: String(customer.id),
      customerName: customer.fullName || "",
      email: customer.email || "",
      phone: customer.phone || "",
      locationId: customer.locationId ? String(customer.locationId) : form.locationId
    });
  }

  function editEstimate(estimate) {
    setEditingId(estimate.id);
    setForm({
      customerId: estimate.customerId ? String(estimate.customerId) : "",
      customerName: estimate.customerName || "",
      email: estimate.email || "",
      phone: estimate.phone || "",
      vehicle: estimate.vehicle || "",
      locationId: estimate.locationId ? String(estimate.locationId) : "",
      taxRate: String(Number(estimate.taxRate || 0) > 1 ? estimate.taxRate : Number(estimate.taxRate || 0) * 100),
      notes: estimate.notes || "",
      validUntil: estimate.validUntil || "",
      items: (estimate.items || []).length ? estimate.items.map((item) => ({
        tireId: item.tireId ? String(item.tireId) : "",
        itemType: item.itemType || "SERVICE",
        itemName: item.itemName || "",
        quantity: item.quantity || 1,
        unitPrice: String(item.unitPrice ?? "0.00")
      })) : [makeInvoiceItem()]
    });
  }

  function updateItem(index, nextValues) {
    setForm({
      ...form,
      items: form.items.map((item, itemIndex) => itemIndex === index ? { ...item, ...nextValues } : item)
    });
  }

  function addTireLine(tire) {
    const nextLine = invoiceItemFromTire(tire.id, 1, tires);
    const reusableIndex = form.items.findIndex((item) =>
      item.itemType !== "TIRE"
      && !item.tireId
      && !String(item.itemName || "").trim()
      && Number(item.unitPrice || 0) === 0
    );

    if (reusableIndex >= 0) {
      setForm({
        ...form,
        items: form.items.map((item, index) => index === reusableIndex ? nextLine : item)
      });
      return;
    }

    setForm({ ...form, items: [...form.items, nextLine] });
  }

  function payloadFromForm() {
    return {
      customerId: form.customerId ? Number(form.customerId) : null,
      customerName: form.customerName,
        email: form.email,
        phone: form.phone,
        vehicle: form.vehicle,
        locationId: form.locationId ? Number(form.locationId) : selectedLocationId ? Number(selectedLocationId) : null,
        taxRate: Number(form.taxRate || 0),
      notes: form.notes,
      validUntil: form.validUntil || null,
      items: form.items.map((item) => ({
        tireId: item.tireId ? Number(item.tireId) : null,
        itemType: item.itemType,
        itemName: item.itemName?.trim() || (item.itemType === "SERVICE" ? "Service" : ""),
        quantity: Number(item.quantity || 1),
        unitPrice: item.unitPrice === "" ? null : Number(item.unitPrice || 0)
      }))
    };
  }

  async function submitEstimate(event) {
    event.preventDefault();
    setError("");

    try {
      if (editingId) {
        await updateEstimate(editingId, payloadFromForm());
      } else {
        await createEstimate(payloadFromForm());
      }

      resetForm();
      onNotify(editingId ? "Estimate updated." : "Estimate created.");
      await onRefresh();
    } catch (err) {
      setError(err.message);
    }
  }

  async function runEstimateAction(message, action) {
    setError("");

    try {
      await action();
      onNotify(message);
      await onRefresh();
    } catch (err) {
      setError(err.message);
    }
  }

  function invoiceEstimate(estimate) {
    setError("");
    onStartInvoice(estimate);
  }

  return (
    <section className="work-area">
      <div className="section-toolbar">
        <div>
          <span className="eyebrow">Quotes</span>
          <h3>Estimates</h3>
        </div>
        <div className="toolbar-actions">
          <StatusBadge value="DRAFT" />
          <StatusBadge value="APPROVED" />
          <StatusBadge value="CONVERTED" />
        </div>
      </div>

      <form className="panel form-grid" onSubmit={submitEstimate}>
        {editingId && (
          <div className="form-banner">
            <span>Editing estimate</span>
            <button className="ghost-button" onClick={resetForm} type="button">
              Cancel
            </button>
          </div>
        )}
        <Input label="Customer" required value={form.customerName} onChange={(customerName) => setForm({ ...form, customerName, customerId: "" })} />
        <Input label="Email" type="email" value={form.email} onChange={(email) => setForm({ ...form, email, customerId: "" })} />
        <Input label="Phone" required type="tel" value={form.phone} onChange={(phone) => setForm({ ...form, phone, customerId: "" })} />
        {matchingCustomers.length > 0 && (
          <div className="customer-match-panel">
            <span>Matching customers</span>
            {matchingCustomers.map((customer) => (
              <button key={customer.id} onClick={() => selectCustomer(customer)} type="button">
                <strong>{customer.fullName}</strong>
                <small>{[customer.email, customer.phone].filter(Boolean).join(" - ")}</small>
              </button>
            ))}
          </div>
        )}
        {selectedCustomer?.vehicles?.length > 0 && (
          <Select
            label="Customer vehicle"
            value=""
            onChange={(vehicleId) => {
              const vehicle = selectedCustomer.vehicles.find((entry) => String(entry.id) === String(vehicleId));
              setForm({ ...form, vehicle: vehicleName(vehicle), locationId: vehicle?.locationId ? String(vehicle.locationId) : form.locationId });
            }}
            options={["", ...selectedCustomer.vehicles.map((vehicle) => String(vehicle.id))]}
            optionLabel={(value) => {
              const vehicle = selectedCustomer.vehicles.find((entry) => String(entry.id) === String(value));
              return value ? vehicleName(vehicle) : "Select saved vehicle";
            }}
          />
        )}
        <Input label="Vehicle" value={form.vehicle} onChange={(vehicle) => setForm({ ...form, vehicle })} />
        {shopLocations.length > 0 && (
          <Select
            label="Location"
            value={form.locationId || selectedLocationId || ""}
            onChange={(locationId) => setForm({ ...form, locationId })}
            options={["", ...shopLocations.map((location) => String(location.id))]}
            optionLabel={(value) => {
              const location = shopLocations.find((entry) => String(entry.id) === String(value));
              return location ? location.name : "Unassigned";
            }}
          />
        )}
        <Input label="Valid until" type="date" value={form.validUntil} onChange={(validUntil) => setForm({ ...form, validUntil })} />
        <Input label="Tax rate %" min="0" step="0.01" type="number" value={form.taxRate} onChange={(taxRate) => setForm({ ...form, taxRate })} />
        <Input label="Notes" value={form.notes} onChange={(notes) => setForm({ ...form, notes })} />

        <fieldset className="invoice-items-editor">
          <legend>Line items</legend>
          <Input
            label="Search inventory tires"
            placeholder="Brand, size, condition, season, or location..."
            value={tireSearch}
            onChange={setTireSearch}
          />
          {normalizedTireSearch && (
            <div className="tire-search-results">
              {estimateTireOptions.length === 0 ? (
                <span className="empty-note">No matching tires.</span>
              ) : (
                estimateTireOptions.slice(0, 6).map((tire) => (
                  <button key={tire.id} onClick={() => addTireLine(tire)} type="button">
                    <strong>{tireOptionLabel(String(tire.id), tires)}</strong>
                    <small>{Number(tire.availableQuantity ?? tire.quantity ?? 0)} available{tire.locationName || tire.location ? ` - ${tire.locationName || tire.location}` : ""}</small>
                  </button>
                ))
              )}
            </div>
          )}
          <div className="invoice-item-list">
            {form.items.map((item, index) => (
              <div className="invoice-item-row" key={index}>
                <Select label="Type" required value={item.itemType} onChange={(itemType) => updateItem(index, { itemType, tireId: itemType === "SERVICE" ? "" : item.tireId })} options={["SERVICE", "TIRE"]} />
                <Select
                  disabled={item.itemType !== "TIRE"}
                  label="Tire"
                  value={item.tireId}
                  onChange={(tireId) => updateItem(index, invoiceItemFromTire(tireId, item.quantity, tires))}
                  options={["", ...estimateTireOptions.map((tire) => String(tire.id))]}
                  optionLabel={(value) => tireOptionLabel(value, tires)}
                />
                <Input label="Name" value={item.itemName} onChange={(itemName) => updateItem(index, { itemName })} />
                <Input label="Qty" min="1" type="number" value={item.quantity} onChange={(quantity) => updateItem(index, { quantity })} />
                <Input label="Price" min="0" type="number" step="0.01" value={item.unitPrice} onChange={(unitPrice) => updateItem(index, { unitPrice })} />
                <button
                  className="danger-button"
                  disabled={form.items.length === 1}
                  onClick={() => setForm({ ...form, items: form.items.filter((_, itemIndex) => itemIndex !== index) })}
                  type="button"
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
          <button className="ghost-button" onClick={() => setForm({ ...form, items: [...form.items, makeInvoiceItem()] })} type="button">
            Add line
          </button>
        </fieldset>

        <div className="invoice-total-preview">
          <span>Estimate total</span>
          <strong>{money(subtotal + tax)}</strong>
          <small>{money(subtotal)} subtotal + {money(tax)} tax preview</small>
        </div>
        <button className="primary-button" type="submit">
          {editingId ? "Save Estimate" : "Create Estimate"}
        </button>
      </form>

      <DataTable
        actions={(estimate) => (
          <div className="table-actions">
            {estimate.status === "DRAFT" && (
              <button className="ghost-button" onClick={() => editEstimate(estimate)} type="button">
                Edit
              </button>
            )}
            {estimate.status === "DRAFT" && (
              <button className="ghost-button" onClick={() => runEstimateAction("Estimate sent to customer.", () => sendEstimate(estimate.id))} type="button">
                Send
              </button>
            )}
            {["DRAFT", "SENT", "VIEWED"].includes(String(estimate.status || "DRAFT").toUpperCase()) && (
              <button className="ghost-button" onClick={() => runEstimateAction("Estimate approved and appointment created.", () => approveEstimate(estimate.id))} type="button">
                Approve
              </button>
            )}
            {estimate.status === "APPROVED" && (
              <button className="ghost-button" onClick={() => invoiceEstimate(estimate)} type="button">
                Create Invoice
              </button>
            )}
            <button className="ghost-button" onClick={() => setPreviewEstimate(estimate)} type="button">
              Preview
            </button>
            {!["CONVERTED", "CANCELLED"].includes(estimate.status) && (
              <button className="danger-button" onClick={() => runEstimateAction("Estimate cancelled.", () => cancelEstimate(estimate.id))} type="button">
                Cancel
              </button>
            )}
          </div>
        )}
        columns={["Estimate", "Customer", "Vehicle", "Location", "Subtotal", "Tax", "Total", "Valid Until", "Status", "Invoice", ""]}
        emptyText="No estimates yet."
        rows={sortedEstimates.map((estimate) => ({
          key: `estimate-${estimate.id}`,
          searchText: [estimate.estimateNumber, estimate.customerName, estimate.phone, estimate.email, estimate.vehicle, estimate.locationName, estimate.status].join(" "),
          values: [
            estimate.estimateNumber || `#${estimate.id}`,
            estimate.customerName,
            estimate.vehicle || "-",
            estimate.locationName || "Unassigned",
            money(estimate.subtotal),
            money(estimate.taxAmount),
            money(estimate.total),
            estimate.validUntil || "-",
            estimate.status || "DRAFT",
            estimate.convertedInvoiceId ? `#${estimate.convertedInvoiceId}` : "-"
          ],
          source: estimate
        }))}
      />
      {previewEstimate && <PrintableEstimate estimate={previewEstimate} settings={settings} />}
    </section>
  );
}

function TireSetupFields({ disabled, form, onChange, tires }) {
  const isStaggered = form.tireSetup === "staggered";
  const [searchQuery, setSearchQuery] = useState("");
  const [conditionFilter, setConditionFilter] = useState("ALL");
  const matchingTires = filterTiresForAppointment(tires, searchQuery, conditionFilter);

  function changeSetup(tireSetup) {
    if (tireSetup === "staggered") {
      onChange({
        ...form,
        tireSetup,
        tireSize: "",
        frontTireSize: form.frontTireSize || form.tireSize,
        frontQuantity: 2,
        rearQuantity: 2
      });
      return;
    }

    onChange({
      ...form,
      tireSetup: "regular",
      tireSize: form.tireSize || form.frontTireSize,
      rearTireId: "",
      rearTireSize: "",
      frontQuantity: 4,
      rearQuantity: 0
    });
  }

  function selectTire(tire, position = "front") {
    const size = tireSizeValue(tire);

    if (position === "rear") {
      onChange({
        ...form,
        rearTireId: String(tire.id),
        rearTireSize: size,
        rearQuantity: 2
      });
      return;
    }

    if (isStaggered) {
      onChange({
        ...form,
        frontTireId: String(tire.id),
        frontTireSize: size,
        frontQuantity: 2,
        rearQuantity: form.rearQuantity || 2
      });
      return;
    }

    onChange({
      ...form,
      frontTireId: String(tire.id),
      rearTireId: "",
      tireSize: size,
      frontQuantity: 4,
      rearQuantity: 0
    });
  }

  function selectTireById(tireId, position = "front") {
    const tire = tires.find((entry) => String(entry.id) === String(tireId));

    if (!tire) {
      if (position === "rear") {
        onChange({ ...form, rearTireId: "", rearTireSize: "", rearQuantity: 0 });
      } else if (isStaggered) {
        onChange({ ...form, frontTireId: "", frontTireSize: "", frontQuantity: 0 });
      } else {
        onChange({ ...form, frontTireId: "", tireSize: "", rearTireId: "", frontQuantity: 0, rearQuantity: 0 });
      }
      return;
    }

    selectTire(tire, position);
  }

  return (
    <fieldset className={`tire-setup-fields ${disabled ? "disabled" : ""}`}>
      <legend>
        Tire setup
        <span className="help-tip" tabIndex="0">
          ?
          <span className="help-tip-content">
            Regular uses one tire size for all four wheels. Staggered uses different front and rear tire sizes.
          </span>
        </span>
      </legend>
      <Select
        disabled={disabled}
        label="Setup"
        value={form.tireSetup}
        onChange={changeSetup}
        options={["regular", "staggered"]}
        optionLabel={(value) => value === "staggered" ? "Staggered" : "Regular"}
      />
      {!disabled && (
        <div className="tire-search-panel">
          <Input
            label="Search inventory tires"
            value={searchQuery}
            onChange={setSearchQuery}
            placeholder="Search brand, model, size, season, or location"
          />
          <Select
            label="Condition"
            value={conditionFilter}
            onChange={setConditionFilter}
            options={["ALL", "NEW", "USED"]}
            optionLabel={(value) => value === "ALL" ? "All" : value}
          />
          <div className="tire-search-results">
            <span>{matchingTires.length ? "Matching tires" : "No matching tires"}</span>
            {matchingTires.map((tire) => (
              <div className="tire-search-result" key={tire.id}>
                <div>
                  <strong>
                    {tire.brand} {tire.model || ""} {tireSizeValue(tire)}
                    <span className={`condition-pill ${String(tire.condition || "").toLowerCase()}`}>{tire.condition || "N/A"}</span>
                  </strong>
                  <small>{tire.season || "Any season"} - {tireAvailableQuantity(tire)} available - {displayTireLocation(tire)}</small>
                </div>
                <div className="result-actions">
                  <button onClick={() => selectTire(tire, "front")} type="button">
                    {isStaggered ? "Use front" : "Use tire"}
                  </button>
                  {isStaggered && (
                    <button onClick={() => selectTire(tire, "rear")} type="button">
                      Use rear
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
      {isStaggered ? (
        <div className="staggered-fields">
          <Select
            disabled={disabled}
            label="Front tire"
            value={form.frontTireId}
            onChange={(frontTireId) => selectTireById(frontTireId, "front")}
            options={["", ...tires.filter((tire) => tireAvailableQuantity(tire) > 0).map((tire) => String(tire.id))]}
            optionLabel={(value) => tireOptionLabel(value, tires)}
          />
          <Input
            disabled={disabled}
            label="Front size"
            required={!disabled}
            value={form.frontTireSize}
            onChange={(frontTireSize) => onChange({ ...form, frontTireSize })}
            placeholder="245/35/19"
          />
          <Input
            disabled={disabled}
            label="Front qty"
            min="0"
            type="number"
            value={form.frontQuantity}
            onChange={(frontQuantity) => onChange({ ...form, frontQuantity })}
          />
          <Select
            disabled={disabled}
            label="Rear tire"
            value={form.rearTireId}
            onChange={(rearTireId) => selectTireById(rearTireId, "rear")}
            options={["", ...tires.filter((tire) => tireAvailableQuantity(tire) > 0).map((tire) => String(tire.id))]}
            optionLabel={(value) => tireOptionLabel(value, tires)}
          />
          <Input
            disabled={disabled}
            label="Rear size"
            required={!disabled}
            value={form.rearTireSize}
            onChange={(rearTireSize) => onChange({ ...form, rearTireSize })}
            placeholder="275/30/19"
          />
          <Input
            disabled={disabled}
            label="Rear qty"
            min="0"
            type="number"
            value={form.rearQuantity}
            onChange={(rearQuantity) => onChange({ ...form, rearQuantity })}
          />
        </div>
      ) : (
        <div className="regular-tire-fields">
          <Select
            disabled={disabled}
            label="Tire"
            value={form.frontTireId}
            onChange={(frontTireId) => selectTireById(frontTireId)}
            options={["", ...tires.filter((tire) => tireAvailableQuantity(tire) > 0).map((tire) => String(tire.id))]}
            optionLabel={(value) => tireOptionLabel(value, tires)}
          />
          <Input
            disabled={disabled}
            label="Tire size"
            required={!disabled}
            value={form.tireSize}
            onChange={(tireSize) => onChange({ ...form, tireSize })}
            placeholder="205/55/16"
          />
          <Input
            disabled={disabled}
            label="Qty"
            min="0"
            type="number"
            value={form.frontQuantity}
            onChange={(frontQuantity) => onChange({ ...form, frontQuantity, rearQuantity: 0 })}
          />
        </div>
      )}
      {disabled && <p>Tire size is only needed for installation appointments.</p>}
    </fieldset>
  );
}

function AppointmentDatePicker({ appointments, editingId, locationId, onChange, value }) {
  const { date, time } = splitAppointmentDate(value);
  const minDate = todayDateKey();

  function appointmentAtSlot(slot) {
    return appointments.find((appointment) =>
      Number(appointment.id) !== Number(editingId)
      && isBookableAppointment(appointment)
      && sameLocationValue(appointment.locationId, locationId)
      && appointmentDateKey(appointment.appointmentDate) === date
      && appointmentTimeKey(appointment.appointmentDate) === slot
    );
  }

  function updateDate(nextDate) {
    if (!nextDate) {
      onChange("");
      return;
    }

    if (nextDate < minDate) {
      onChange(time ? joinAppointmentDate(minDate, time) : minDate);
      return;
    }

    onChange(time ? joinAppointmentDate(nextDate, time) : nextDate);
  }

  function updateTime(nextTime) {
    if (!date || date < minDate || appointmentAtSlot(nextTime)) {
      return;
    }

    onChange(joinAppointmentDate(date, nextTime));
  }

  return (
    <fieldset className="date-time-picker">
      <legend>Appointment date & time</legend>
      <div className="date-time-fields">
        <label>
          <span>Date</span>
          <input required min={minDate} type="date" value={date} onChange={(event) => updateDate(event.target.value)} />
        </label>
        <label>
          <span>Time</span>
          <input required type="time" value={time} onChange={(event) => updateTime(event.target.value)} />
        </label>
      </div>
      {date ? (
        <>
          <div className="time-slots" aria-label="Quick appointment times">
            {appointmentTimes.map((slot) => {
              const bookedAppointment = appointmentAtSlot(slot);

              return (
                <button
                  className={[time === slot ? "selected" : "", bookedAppointment ? "booked" : ""].filter(Boolean).join(" ")}
                  disabled={date < minDate || Boolean(bookedAppointment)}
                  key={slot}
                  onClick={() => updateTime(slot)}
                  title={bookedAppointment ? `Booked by ${bookedAppointment.customerName}` : "Available"}
                  type="button"
                >
                  {slot}
                </button>
              );
            })}
          </div>
          <AppointmentDayView
            appointments={appointments}
            editingId={editingId}
            locationId={locationId}
            selectedValue={value}
            onSelect={onChange}
          />
        </>
      ) : (
        <p className="appointment-time-empty">Choose a date to see available times.</p>
      )}
    </fieldset>
  );
}

function AppointmentDayView({ appointments, editingId, locationId, onSelect, selectedValue }) {
  const { date, time } = splitAppointmentDate(selectedValue);
  const selectedDate = date || todayDateKey();
  const isPastDate = selectedDate < todayDateKey();

  function appointmentAtSlot(slot) {
    return appointments.find((appointment) =>
      Number(appointment.id) !== Number(editingId)
      && isBookableAppointment(appointment)
      && sameLocationValue(appointment.locationId, locationId)
      && appointmentDateKey(appointment.appointmentDate) === selectedDate
      && appointmentTimeKey(appointment.appointmentDate) === slot
    );
  }

  return (
    <section className="day-view panel">
      <div className="day-view-header">
        <div>
          <span className="eyebrow">Day view</span>
          <h3>{selectedDate}</h3>
        </div>
        <div className="day-view-legend">
          <span><i className="free" />Free</span>
          <span><i className="booked" />Booked</span>
        </div>
      </div>
      <div className="day-view-slots">
        {appointmentTimes.map((slot) => {
          const appointment = appointmentAtSlot(slot);
          const isSelected = time === slot;

          return (
            <button
              className={[
                "day-view-slot",
                appointment ? "booked" : "free",
                isSelected ? "selected" : ""
              ].filter(Boolean).join(" ")}
              disabled={isPastDate || Boolean(appointment)}
              key={slot}
              onClick={() => onSelect(joinAppointmentDate(selectedDate, slot))}
              type="button"
            >
              <strong>{slot}</strong>
              <span>{appointment ? appointment.customerName : "Available"}</span>
              {appointment && <small>{serviceTypeLabel(appointment.serviceType)}</small>}
              {!appointment && <CheckCircle2 size={16} />}
            </button>
          );
        })}
      </div>
    </section>
  );
}

function Invoices({
  appointments,
  form,
  generatedInvoice,
  highlightedRow,
  invoices,
  onChange,
  onDelete,
  onMarkPaid,
  onPreviewInvoice,
  onUpdateStatus,
  onSubmit,
  selectedLocationId = "",
  settings,
  shopLocations = [],
  tires
}) {
  const displayedInvoices = [...invoices].sort((first, second) => Number(second.id || 0) - Number(first.id || 0));
  const selectedAppointment = appointments.find((appointment) => String(appointment.id) === String(form.appointmentId));
  const subtotal = form.items.reduce(
    (total, item) => total + Number(item.quantity || 0) * Number(item.unitPrice || 0),
    0
  );
  const taxRate = Number(settings.taxRate || 13) / 100;
  const tax = subtotal * taxRate;
  const total = subtotal + tax;

  useEffect(() => {
    if (generatedInvoice) {
      scrollToSelector("#invoice-preview");
    }
  }, [generatedInvoice]);

  function exportInvoices() {
    const csv = toCsv(
      ["Invoice", "Customer", "Phone", "Vehicle", "Subtotal", "Tax", "Total", "Paid", "Balance", "Payment", "Status"],
      displayedInvoices.map((invoice) => [
        invoiceNumber(invoice),
        invoice.customerName,
        invoice.phone,
        invoice.vehicle || "",
        invoice.subtotal,
        invoice.taxAmount,
        invoice.total,
        invoiceCollectedAmount(invoice),
        invoiceBalanceAmount(invoice),
        invoice.paymentMethod || "",
        statusLabel(invoiceDisplayStatus(invoice))
      ])
    );

    downloadTextFile("tiretrack-invoices.csv", csv, "text/csv");
  }

  function exportMonthlyReport() {
    const now = new Date();
    const month = now.getMonth();
    const year = now.getFullYear();
    const monthlyInvoices = invoices.filter((invoice) => {
      const date = new Date(invoice.createdAt || invoice.invoiceDate || Date.now());
      return date.getMonth() === month && date.getFullYear() === year;
    });
    const revenue = monthlyInvoices.reduce((sum, invoice) => sum + invoiceCollectedAmount(invoice), 0);
    const reportHtml = `
      <html><head><title>Monthly Sales Report</title></head>
      <body style="font-family: Arial, sans-serif; padding: 32px;">
        <h1>${htmlEscape(settings.shopName || "Shop")} Monthly Sales Report</h1>
        <p>${now.toLocaleString(undefined, { month: "long", year: "numeric" })}</p>
        <h2>${money(revenue)} revenue</h2>
        <p>${monthlyInvoices.length} invoices</p>
        <table style="width:100%;border-collapse:collapse;margin-top:24px;">
          <thead><tr><th align="left">Customer</th><th align="left">Status</th><th align="right">Total</th></tr></thead>
          <tbody>
            ${monthlyInvoices.map((invoice) => `<tr><td>${htmlEscape(invoice.customerName)}</td><td>${htmlEscape(statusLabel(invoiceDisplayStatus(invoice)))}</td><td align="right">${htmlEscape(money(invoiceCollectedAmount(invoice)))}</td></tr>`).join("")}
          </tbody>
        </table>
        <script>window.print()</script>
      </body></html>
    `;
    const reportWindow = window.open("", "_blank");

    if (!reportWindow) {
      setTimeout(() => downloadTextFile("monthly-sales-report.html", reportHtml, "text/html"), 0);
      return;
    }

    reportWindow.document.write(reportHtml);
    reportWindow.document.close();
  }

  function updateItem(index, nextValues) {
    onChange({
      ...form,
      items: form.items.map((item, itemIndex) => itemIndex === index ? { ...item, ...nextValues } : item)
    });
  }

  function chooseAppointment(appointmentId) {
    const appointment = appointments.find((entry) => String(entry.id) === String(appointmentId));

    if (!appointment) {
      onChange({ ...form, appointmentId: "", estimateId: "" });
      return;
    }

    const items = [];

    if (appointment.frontTireId) {
      items.push(invoiceItemFromTire(appointment.frontTireId, appointment.frontQuantity, tires));
    }

    if (appointment.rearTireId && appointment.rearTireId !== appointment.frontTireId) {
      items.push(invoiceItemFromTire(appointment.rearTireId, appointment.rearQuantity, tires));
    }

    onChange({
      ...form,
      appointmentId,
      estimateId: "",
      customerName: appointment.customerName || "",
      phone: appointment.phone || "",
      vehicle: appointment.vehicle || "",
      locationId: appointment.locationId ? String(appointment.locationId) : form.locationId,
      items: items.length ? items : form.items
    });
  }

  return (
    <section className="work-area">
      <div className="section-toolbar">
        <div>
          <span className="eyebrow">Billing</span>
          <h3>Invoices</h3>
        </div>
        <div className="toolbar-actions">
          <button className="ghost-button with-icon" onClick={exportInvoices} type="button">
            <Download size={16} />
            Export CSV
          </button>
          <button className="ghost-button with-icon" onClick={exportMonthlyReport} type="button">
            <FileText size={16} />
            Monthly PDF
          </button>
        </div>
      </div>
      <form className="panel form-grid" onSubmit={onSubmit}>
        <div className="invoice-source">
          <div>
            <span className="eyebrow">{form.estimateId ? "Estimate source" : "Appointment source"}</span>
            <h3>{form.estimateId ? "Estimate invoice draft" : selectedAppointment ? selectedAppointment.customerName : "Manual invoice"}</h3>
            <p>{form.estimateId ? "Review and edit the invoice before saving the estimate conversion." : selectedAppointment ? `${serviceTypeLabel(selectedAppointment.serviceType)} - ${dateTime(selectedAppointment.appointmentDate)}` : "Choose an appointment to fill customer, vehicle, and reserved tires."}</p>
          </div>
          <Select
            label="Choose appointment"
            value={form.appointmentId}
            onChange={chooseAppointment}
            options={["", ...appointments.map((appointment) => String(appointment.id))]}
            optionLabel={(value) => {
              const appointment = appointments.find((entry) => String(entry.id) === String(value));
              return appointment ? `${appointment.customerName} - ${dateTime(appointment.appointmentDate)}` : "Manual invoice";
            }}
          />
        </div>
        <Input label="Customer" required value={form.customerName} onChange={(customerName) => onChange({ ...form, customerName })} />
        <Input label="Phone" required type="tel" value={form.phone} onChange={(phone) => onChange({ ...form, phone })} />
        <Input label="Vehicle" placeholder="Example: 2020 Toyota Camry" value={form.vehicle} onChange={(vehicle) => onChange({ ...form, vehicle })} />
        {shopLocations.length > 0 && (
          <Select
            label="Location"
            value={form.locationId || selectedLocationId || ""}
            onChange={(locationId) => onChange({ ...form, locationId })}
            options={["", ...shopLocations.map((location) => String(location.id))]}
            optionLabel={(value) => {
              const location = shopLocations.find((entry) => String(entry.id) === String(value));
              return location ? location.name : "Unassigned";
            }}
          />
        )}
        <Input label="Company name" value={settings.shopName} onChange={() => {}} disabled />
        <Select label="Payment" value={form.paymentMethod} onChange={(paymentMethod) => onChange({ ...form, paymentMethod })} options={["Cash", "Debit", "Credit", "E-Transfer"]} />
        <Select
          label="Status"
          value={form.status}
          onChange={(status) => onChange({ ...form, status })}
          options={["DRAFT", "SENT", "UNPAID", "PARTIALLY_PAID", "PAID", "VOID"]}
          optionLabel={statusLabel}
        />
        {invoiceStatusKey(form.status) === "PARTIALLY_PAID" && (
          <Input label="Amount paid" min="0" step="0.01" type="number" value={form.amountPaid} onChange={(amountPaid) => onChange({ ...form, amountPaid })} />
        )}
        {invoiceStatusKey(form.status) !== "PAID" && invoiceStatusKey(form.status) !== "VOID" && (
          <Input label={invoiceStatusKey(form.status) === "PARTIALLY_PAID" ? "Balance due date" : "Due date"} required={invoiceStatusKey(form.status) === "PARTIALLY_PAID"} type="date" value={form.dueDate || ""} onChange={(dueDate) => onChange({ ...form, dueDate })} />
        )}

        <fieldset className="invoice-items-editor">
          <legend>Line items</legend>
          <div className="invoice-item-list">
            {form.items.map((item, index) => (
              <div className="invoice-item-row" key={index}>
                <Select label="Type" required value={item.itemType} onChange={(itemType) => updateItem(index, { itemType, tireId: itemType === "SERVICE" ? "" : item.tireId })} options={["SERVICE", "TIRE"]} />
                <Select
                  disabled={item.itemType !== "TIRE"}
                  label="Tire"
                  value={item.tireId}
                  onChange={(tireId) => updateItem(index, invoiceItemFromTire(tireId, item.quantity, tires))}
                  options={["", ...tires.map((tire) => String(tire.id))]}
                  optionLabel={(value) => tireOptionLabel(value, tires)}
                />
                <Input label="Name" value={item.itemName} onChange={(itemName) => updateItem(index, { itemName })} />
                <Input label="Qty" min="1" type="number" value={item.quantity} onChange={(quantity) => updateItem(index, { quantity })} />
                <Input label="Price" min="0" type="number" step="0.01" value={item.unitPrice} onChange={(unitPrice) => updateItem(index, { unitPrice })} />
                <button
                  className="danger-button"
                  disabled={form.items.length === 1}
                  onClick={() => onChange({ ...form, items: form.items.filter((_, itemIndex) => itemIndex !== index) })}
                  type="button"
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
          <button className="ghost-button" onClick={() => onChange({ ...form, items: [...form.items, makeInvoiceItem()] })} type="button">
            Add line
          </button>
        </fieldset>

        <div className="invoice-total-preview">
          <span>Invoice total</span>
          <strong>{money(total)}</strong>
          <small>
            {money(subtotal)} subtotal + {money(tax)} tax preview
            {invoiceStatusKey(form.status) === "PARTIALLY_PAID" ? ` - ${money(Math.max(total - Number(form.amountPaid || 0), 0))} balance` : ""}
          </small>
        </div>
        <button className="primary-button" type="submit">{form.estimateId ? "Create Invoice From Estimate" : "Create Invoice"}</button>
      </form>

      <DataTable
        highlightedRow={highlightedRow}
        actions={(invoice) => (
          <div className="table-actions">
            {invoiceStatusKey(invoice.status) !== "PAID" && (
              <button className="ghost-button" onClick={() => onMarkPaid(invoice)} type="button">
                Mark Paid
              </button>
            )}
            {["SENT", "UNPAID", "PARTIALLY_PAID", "VOID"].map((status) => (
              invoiceStatusKey(invoice.status) !== status && (
                <button className="ghost-button" key={status} onClick={() => onUpdateStatus(invoice, status)} type="button">
                  {statusLabel(status)}
                </button>
              )
            ))}
            <button className="ghost-button" onClick={() => onPreviewInvoice(invoice)} type="button">
              Preview
            </button>
            <button className="danger-button" onClick={() => onDelete(invoice.id)} type="button">
              Delete
            </button>
          </div>
        )}
        columns={["Invoice", "Customer", "Phone", "Vehicle", "Location", "Subtotal", "HST", "Total", "Paid", "Balance", "Payment", "Status", "Due", "Paid At", ""]}
        emptyText="No invoices yet."
        rows={displayedInvoices.map((invoice) => ({
          key: `invoice-${invoice.id}`,
          searchText: [invoiceNumber(invoice), invoice.invoiceNumber, invoice.id, invoice.customerName, invoice.phone, invoice.vehicle, invoice.locationName, invoice.status, statusLabel(invoiceDisplayStatus(invoice)), invoice.paymentMethod].join(" "),
          values: [
            invoiceNumber(invoice),
            invoice.customerName,
            invoice.phone,
            invoice.vehicle || "-",
            invoice.locationName || "Unassigned",
            money(invoice.subtotal),
            money(invoice.taxAmount),
            money(invoice.total),
            money(invoiceCollectedAmount(invoice)),
            money(invoiceBalanceAmount(invoice)),
            invoice.paymentMethod || "-",
            invoiceDisplayStatus(invoice),
            invoice.dueDate || "-",
            dateTime(invoice.paidAt)
          ],
          source: invoice
        }))}
      />

      {generatedInvoice && <PrintableInvoice settings={settings} invoice={generatedInvoice} />}
    </section>
  );
}

function invoiceItemFromTire(tireId, quantity, tires) {
  const tire = tires.find((entry) => String(entry.id) === String(tireId));

  if (!tire) {
    return { ...makeInvoiceItem(), itemType: "TIRE", tireId };
  }

  return {
    tireId: String(tire.id),
    itemType: "TIRE",
    itemName: `${tire.brand} ${tire.width}/${tire.aspectRatio}R${tire.rimSize}`,
    quantity: quantity || 1,
    unitPrice: String(tire.price ?? "0.00")
  };
}

function PrintableInvoice({ settings, invoice }) {
  const items = invoice.items || [];

  return (
    <section className="printable-invoice panel" id="invoice-preview">
      <div className="invoice-toolbar">
        <div>
          <span className="eyebrow">Generated invoice PDF</span>
          <h3>{invoiceNumber(invoice)}</h3>
          <p>Use print to save this invoice as a PDF.</p>
        </div>
        <button className="ghost-button" onClick={() => window.print()} type="button">
          Print / Save PDF
        </button>
      </div>
      <article className="invoice-document">
        <header className="invoice-document-header">
          <div>
            {settings.logoUrl && <img alt={`${settings.shopName} logo`} className="invoice-logo" src={settings.logoUrl} />}
            <h2>{settings.shopName}</h2>
            <p>Invoice</p>
            <p>{settings.phone}</p>
            <p>{settings.address}</p>
          </div>
          <div>
            <strong>{invoiceNumber(invoice)}</strong>
            <span>{dateTime(invoice.createdAt)}</span>
          </div>
        </header>
        <section className="invoice-parties">
          <div>
            <span>Bill to</span>
            <strong>{invoice.customerName}</strong>
            <p>{invoice.phone}</p>
            <p>{invoice.vehicle || "No vehicle"}</p>
          </div>
          <div>
            <span>Payment</span>
            <strong>{statusLabel(invoiceDisplayStatus(invoice))}</strong>
            <p>{invoice.paymentMethod || "-"}</p>
            <p>Paid: {money(invoiceCollectedAmount(invoice))}</p>
            <p>Balance: {money(invoiceBalanceAmount(invoice))}</p>
          </div>
        </section>
        <table className="invoice-lines">
          <thead>
            <tr>
              <th>Item</th>
              <th>Qty</th>
              <th>Unit</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 ? (
              <tr>
                <td colSpan="4">No line items.</td>
              </tr>
            ) : (
              items.map((item, index) => (
                <tr key={item.id || `${item.itemName}-${index}`}>
                  <td>{item.itemName || item.itemType}</td>
                  <td>{item.quantity}</td>
                  <td>{money(item.unitPrice)}</td>
                  <td>{money(item.totalPrice ?? Number(item.quantity || 0) * Number(item.unitPrice || 0))}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
        <section className="invoice-totals">
          <div><span>Subtotal</span><strong>{money(invoice.subtotal ?? items.reduce((sum, item) => sum + Number(item.totalPrice ?? Number(item.quantity || 0) * Number(item.unitPrice || 0)), 0))}</strong></div>
          <div><span>HST</span><strong>{money(invoice.taxAmount ?? 0)}</strong></div>
          <div className="grand-total"><span>Total</span><strong>{money(invoice.total)}</strong></div>
        </section>
        <p className="invoice-terms">{settings.invoiceTerms}</p>
        <p className="monarch-print-footer">Powered by Monarch Solutions | Support: support@monarchsolutions.ca</p>
      </article>
    </section>
  );
}

function PrintableEstimate({ estimate, settings }) {
  const items = estimate.items || [];
  const subtotal = estimate.subtotal ?? items.reduce((sum, item) => sum + Number(item.lineTotal ?? Number(item.quantity || 0) * Number(item.unitPrice || 0)), 0);

  return (
    <section className="printable-invoice panel" id="estimate-preview">
      <div className="invoice-toolbar">
        <div>
          <span className="eyebrow">Estimate document</span>
          <h3>{estimate.estimateNumber || `Estimate #${estimate.id}`}</h3>
          <p>Preview, print, or save this estimate for the customer.</p>
        </div>
        <button className="ghost-button" onClick={() => window.print()} type="button">
          Print / Save PDF
        </button>
      </div>
      <article className="invoice-document estimate-document">
        <header className="invoice-document-header">
          <div>
            {settings.logoUrl && <img alt={`${settings.shopName} logo`} className="invoice-logo" src={settings.logoUrl} />}
            <h2>{settings.shopName}</h2>
            <p>Estimate</p>
            <p>{settings.phone}</p>
            <p>{settings.address}</p>
          </div>
          <div>
            <strong>{estimate.estimateNumber || `Estimate #${estimate.id}`}</strong>
            <span>{dateTime(estimate.createdAt)}</span>
            <span>Valid until {estimate.validUntil || "-"}</span>
            <StatusBadge value={estimate.status || "DRAFT"} />
          </div>
        </header>
        <section className="invoice-parties">
          <div>
            <span>Prepared for</span>
            <strong>{estimate.customerName}</strong>
            <p>{estimate.phone}</p>
            <p>{estimate.vehicle || "No vehicle"}</p>
          </div>
          <div>
            <span>Estimate status</span>
            <strong>{estimate.status || "DRAFT"}</strong>
            <p>{estimate.email || "-"}</p>
          </div>
        </section>
        <table className="invoice-lines">
          <thead>
            <tr>
              <th>Item</th>
              <th>Qty</th>
              <th>Unit</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 ? (
              <tr>
                <td colSpan="4">No line items.</td>
              </tr>
            ) : (
              items.map((item, index) => (
                <tr key={item.id || `${item.itemName}-${index}`}>
                  <td>{item.itemName || item.itemType}</td>
                  <td>{item.quantity}</td>
                  <td>{money(item.unitPrice)}</td>
                  <td>{money(item.lineTotal ?? Number(item.quantity || 0) * Number(item.unitPrice || 0))}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
        <section className="invoice-totals">
          <div><span>Subtotal</span><strong>{money(subtotal)}</strong></div>
          <div><span>HST</span><strong>{money(estimate.taxAmount ?? 0)}</strong></div>
          <div className="grand-total"><span>Total</span><strong>{money(estimate.total)}</strong></div>
        </section>
        {estimate.notes && <p className="invoice-terms">{estimate.notes}</p>}
        <p className="monarch-print-footer">Powered by Monarch Solutions | Support: support@monarchsolutions.ca</p>
      </article>
    </section>
  );
}

function inventoryWarnings(tire) {
  const quantity = Number(tire.quantity || 0);
  const available = Number(tire.availableQuantity ?? tire.quantity ?? 0);
  const reserved = Number(tire.reservedQuantity || 0);
  const warnings = [];

  if (available <= 0) {
    warnings.push({ label: "Out of stock", tone: "red" });
  } else if (available < 3) {
    warnings.push({ label: "Low stock", tone: "red" });
  } else if (available <= 5) {
    warnings.push({ label: "Watch", tone: "yellow" });
  }

  if (reserved > 0 && quantity > 0 && reserved / quantity >= 0.7) {
    warnings.push({ label: "Reserved high", tone: "yellow" });
  }

  if (quantity >= 12 && reserved >= 4) {
    warnings.push({ label: "Fast moving", tone: "green" });
  }

  return warnings;
}

function WarningBadges({ warnings }) {
  if (!warnings.length) {
    return <span className="muted-cell">Healthy</span>;
  }

  return (
    <div className="warning-badges">
      {warnings.map((warning) => (
        <span className={`warning-chip ${warning.tone}`} key={warning.label}>{warning.label}</span>
      ))}
    </div>
  );
}

function InventoryTable({ highlightedRow, onDelete, onRefill, tires }) {
  const [selectedTire, setSelectedTire] = useState(null);

  return (
    <>
      <DataTable
        highlightedRow={highlightedRow}
        actions={(tire) => (
          <div className="table-actions">
            <button className="ghost-button" onClick={() => setSelectedTire(tire)} type="button">
              Details
            </button>
            <button className="ghost-button" onClick={() => onRefill(tire)} type="button">
              Refill
            </button>
            <button className="danger-button" onClick={() => onDelete(tire.id)} type="button">
              Delete
            </button>
          </div>
        )}
        columns={["Brand", "Model", "Size", "Season", "Condition", "Warnings", "Qty", "Reserved", "Available", "Price", "Location", ""]}
        emptyText="No tires yet."
        rows={tires.map((tire) => ({
          key: `tire-${tire.id}`,
          searchText: [tire.brand, tire.model, tire.season, tire.condition, tire.location, tire.locationName, `${tire.width}/${tire.aspectRatio}R${tire.rimSize}`].filter(Boolean).join(" "),
          values: [
            tire.brand,
            tire.model || "-",
            `${tire.width}/${tire.aspectRatio}R${tire.rimSize}`,
            tire.season || "-",
            tire.condition || "-",
            { value: <WarningBadges warnings={inventoryWarnings(tire)} />, text: inventoryWarnings(tire).map((warning) => warning.label).join(" ") },
            tire.quantity,
            tire.reservedQuantity || 0,
            urgentStockValue(tire, tire.availableQuantity ?? tire.quantity),
            money(tire.price),
            displayTireLocation(tire)
          ],
          source: tire
        }))}
      />
      {selectedTire && (
        <TireDrawer
          onClose={() => setSelectedTire(null)}
          onRefill={() => {
            onRefill(selectedTire);
            setSelectedTire(null);
          }}
          tire={selectedTire}
        />
      )}
    </>
  );
}

function TireDrawer({ onClose, onRefill, tire }) {
  const warnings = inventoryWarnings(tire);
  const available = tire.availableQuantity ?? tire.quantity;

  return (
    <div className="drawer-backdrop" role="presentation" onClick={onClose}>
      <aside className="tire-drawer" role="dialog" aria-label="Tire details" onClick={(event) => event.stopPropagation()}>
        <div className="drawer-header">
          <div>
            <span className="eyebrow">Tire Detail</span>
            <h3>{tire.brand} {tire.model || ""}</h3>
            <p>{tire.width}/{tire.aspectRatio}R{tire.rimSize} · {tire.condition || "-"}</p>
          </div>
          <button className="ghost-button" onClick={onClose} type="button">Close</button>
        </div>
        <div className="drawer-stats">
          <div><span>Total</span><strong>{tire.quantity}</strong></div>
          <div><span>Reserved</span><strong>{tire.reservedQuantity || 0}</strong></div>
          <div><span>Available</span><strong>{available}</strong></div>
          <div><span>Value</span><strong>{money(Number(tire.quantity || 0) * Number(tire.price || 0))}</strong></div>
        </div>
        <WarningBadges warnings={warnings} />
        <div className="drawer-meta">
          <span>Season</span><strong>{tire.season || "-"}</strong>
          <span>Shop</span><strong>{tire.shopName || "Unassigned"}</strong>
          <span>Location</span><strong>{displayTireLocation(tire)}</strong>
          <span>Unit price</span><strong>{money(tire.price)}</strong>
        </div>
        <div className="drawer-actions">
          <button className="primary-button" onClick={onRefill} type="button">Refill Tire</button>
        </div>
      </aside>
    </div>
  );
}

function emptyCustomerVehicleForm() {
  return { nickname: "", year: "", make: "", model: "", plateNumber: "", tireSetup: "regular", tireSize: "", frontTireSize: "", rearTireSize: "" };
}

function CustomerPortalShell({ auth, isRefreshing = false, onApproveEstimate, onBookAppointment, onDeleteVehicle, onLogout, onMarkNoticeRead, onPayInvoice, onRefresh, onSaveVehicle, onToggleTheme, portal, themeMode }) {
  const [vehicleForm, setVehicleForm] = useState(emptyCustomerVehicleForm);
  const [bookingForm, setBookingForm] = useState({
    vehicleId: "",
    locationId: auth?.locationId ? String(auth.locationId) : "",
    appointmentDate: todayDateKey(),
    appointmentTime: "",
    serviceType: "INSTALLATION",
    notes: ""
  });
  const [slots, setSlots] = useState([]);
  const [locations, setLocations] = useState([]);
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [isLoadingLocations, setIsLoadingLocations] = useState(false);
  const [availability, setAvailability] = useState(null);
  const [availabilityError, setAvailabilityError] = useState("");
  const [isCheckingAvailability, setIsCheckingAvailability] = useState(false);
  const [payingInvoiceId, setPayingInvoiceId] = useState(null);
  const [approvingEstimateId, setApprovingEstimateId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const previousUnreadCountRef = useRef(null);

  useEffect(() => {
    if (message || error) {
      scrollPageToTop();
    }
  }, [message, error]);

  const vehicles = portal?.vehicles || [];
  const appointments = portal?.appointments || [];
  const invoices = portal?.invoices || [];
  const estimates = portal?.estimates || [];
  const tireRequests = portal?.tireRequests || [];
  const notifications = portal?.notifications || [];
  const unreadCount = notifications.filter((notification) => !notification.read).length;
  const latestUnreadNotice = notifications.find((notification) => !notification.read);
  const unpaidInvoices = invoices.filter((invoice) => !["PAID", "VOID"].includes(invoiceStatusKey(invoice.status)));
  const selectedBookingVehicle = vehicles.find((vehicle) => String(vehicle.id) === String(bookingForm.vehicleId));
  const bookingLocationId = bookingForm.locationId || selectedBookingVehicle?.locationId || auth?.locationId || "";
  const customerNeedsTireSourcing = needsTireSourcing(availability);

  useEffect(() => {
    if ((previousUnreadCountRef.current === null && unreadCount > 0)
        || (previousUnreadCountRef.current !== null && unreadCount > previousUnreadCountRef.current)) {
      playNotificationChime();
    }
    previousUnreadCountRef.current = unreadCount;
  }, [unreadCount]);

  useEffect(() => {
    async function loadCustomerLocations() {
      if (!auth?.shopId) {
        setLocations([]);
        return;
      }

      setIsLoadingLocations(true);

      try {
        const publicLocations = publicStoreLocations(await getPublicShopLocations(auth.shopId));
        setLocations(publicLocations);
        setBookingForm((current) => ({
          ...current,
          locationId: publicLocations?.some((location) => String(location.id) === String(current.locationId))
            ? current.locationId
            : auth?.locationId ? String(auth.locationId) : publicLocations?.length === 1 ? String(publicLocations[0].id) : current.locationId
        }));
      } catch {
        setLocations([]);
      } finally {
        setIsLoadingLocations(false);
      }
    }

    loadCustomerLocations();
  }, [auth?.shopId, auth?.locationId]);

  useEffect(() => {
    async function loadSlots() {
      if (!bookingForm.appointmentDate || (locations.length > 0 && !bookingLocationId)) {
        setSlots([]);
        return;
      }

      setIsLoadingSlots(true);

      try {
        const availableSlots = await getAvailableSlots(bookingForm.appointmentDate, bookingLocationId);
        setSlots(availableSlots || []);
        setBookingForm((current) => ({
          ...current,
          appointmentTime: availableSlots?.includes(current.appointmentTime) ? current.appointmentTime : ""
        }));
      } catch {
        setSlots([]);
        setBookingForm((current) => ({ ...current, appointmentTime: "" }));
      } finally {
        setIsLoadingSlots(false);
      }
    }

    loadSlots();
  }, [bookingForm.appointmentDate, bookingLocationId, locations.length]);

  useEffect(() => {
    let cancelled = false;

    async function checkAvailability() {
      if (!bookingForm.vehicleId || !usesInventoryTires(bookingForm.serviceType)) {
        setAvailability(null);
        setAvailabilityError("");
        return;
      }

      setIsCheckingAvailability(true);
      setAvailabilityError("");

      try {
        const result = await getCustomerTireAvailability(bookingForm.vehicleId, bookingLocationId, bookingForm.serviceType);
        if (!cancelled) {
          setAvailability(result);
        }
      } catch (err) {
        if (!cancelled) {
          setAvailability(null);
          setAvailabilityError(err.message || "Tire availability could not be checked.");
        }
      } finally {
        if (!cancelled) {
          setIsCheckingAvailability(false);
        }
      }
    }

    checkAvailability();

    return () => {
      cancelled = true;
    };
  }, [bookingForm.vehicleId, bookingForm.serviceType, bookingLocationId]);

  useEffect(() => {
    if (!portal) {
      onRefresh().catch(() => {});
    }
  }, [portal]);

  async function refreshPortal() {
    setError("");
    setMessage("");

    try {
      await onRefresh();
      setMessage("Portal refreshed.");
    } catch (err) {
      setError(err.message || "Could not refresh portal.");
    }
  }

  async function submitVehicle(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      if (vehicleForm.tireSetup === "staggered" && (!vehicleForm.frontTireSize || !vehicleForm.rearTireSize)) {
        setError("Enter both front and rear tire sizes for a staggered setup.");
        return;
      }

      await onSaveVehicle(vehicleForm);
      setVehicleForm(emptyCustomerVehicleForm());
      setMessage("Vehicle saved.");
    } catch (err) {
      setError(err.message || "Vehicle could not be saved.");
    }
  }

  async function submitBooking(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      if (!bookingForm.vehicleId) {
        setError("Add or choose a saved vehicle before booking.");
        return;
      }

      if (!bookingForm.appointmentTime) {
        setError("Choose an available appointment time.");
        return;
      }

      const savedAppointment = await onBookAppointment({
        ...bookingForm,
        vehicleId: Number(bookingForm.vehicleId),
        locationId: bookingLocationId ? Number(bookingLocationId) : null
      });
      setBookingForm((current) => ({ ...current, appointmentTime: "", locationId: bookingLocationId || current.locationId, notes: "" }));
      setMessage(savedAppointment?.status === "PENDING_TIRE_AVAILABILITY"
        ? "Tire sourcing request sent. The shop will review availability before confirming your appointment."
        : "Appointment booked.");
    } catch (err) {
      setError(err.message || "Appointment could not be booked.");
    }
  }

  async function payInvoice(invoiceId) {
    setError("");
    setMessage("Marking invoice paid...");
    setPayingInvoiceId(invoiceId);

    try {
      await onPayInvoice(invoiceId);
      setMessage("Payment recorded. This is a manual/simulated portal payment for now.");
    } catch (err) {
      setError(err.message || "Invoice could not be paid.");
      setMessage("");
    } finally {
      setPayingInvoiceId(null);
    }
  }

  async function approveEstimate(estimateId) {
    setError("");
    setMessage("Approving estimate and creating appointment...");
    setApprovingEstimateId(estimateId);

    try {
      await onApproveEstimate(estimateId);
      setMessage("Estimate approved. Your appointment was created from this estimate.");
    } catch (err) {
      setError(err.message || "Estimate could not be approved.");
      setMessage("");
    } finally {
      setApprovingEstimateId(null);
    }
  }

  return (
    <main className="customer-portal">
      <header className="customer-header">
        <div>
          <span className="eyebrow">Customer portal</span>
          <h1>Welcome, {auth.fullName}</h1>
        </div>
        <div className="toolbar-actions">
          <ThemeToggleButton onToggle={onToggleTheme} themeMode={themeMode} />
          <button className="ghost-button with-icon" disabled={isRefreshing} onClick={refreshPortal} type="button"><RefreshCw size={16} />{isRefreshing ? "Refreshing..." : "Refresh"}</button>
          <button className="ghost-button with-icon" onClick={onLogout} type="button"><UserCircle size={17} />Logout</button>
        </div>
      </header>

      {message ? <div className="success-alert">{message}</div> : null}
      {error ? <div className="alert error">{error}</div> : null}
      {unreadCount > 0 && (
        <div className="customer-unread-alert" role="status" aria-live="polite">
          <Bell size={18} />
          <div>
            <strong>{unreadCount} unread notice{unreadCount === 1 ? "" : "s"}</strong>
            <span>{latestUnreadNotice?.title || "You have a new shop notice"}{latestUnreadNotice?.message ? ` - ${latestUnreadNotice.message}` : ""}</span>
          </div>
        </div>
      )}
      {unpaidInvoices.length > 0 && (
        <div className="portal-payment-alert">
          <strong>You have unpaid invoices requiring payment.</strong>
          <span>{unpaidInvoices.length} invoice{unpaidInvoices.length === 1 ? "" : "s"} outstanding.</span>
        </div>
      )}

      <section className="customer-metrics">
        <div className="metric-card"><span>Vehicles</span><strong>{vehicles.length}</strong></div>
        <div className="metric-card"><span>Appointments</span><strong>{appointments.length}</strong></div>
        <div className="metric-card"><span>Invoices</span><strong>{invoices.length}</strong></div>
        <div className="metric-card"><span>Unpaid invoices</span><strong>{unpaidInvoices.length}</strong></div>
        <div className="metric-card"><span>Estimates</span><strong>{estimates.length}</strong></div>
        <div className="metric-card"><span>Tire requests</span><strong>{tireRequests.length}</strong></div>
        <div className="metric-card"><span>Unread notices</span><strong>{unreadCount}</strong></div>
      </section>

      <section className="panel customer-notification-panel">
        <div className="section-toolbar">
          <div>
            <span className="eyebrow">Messages</span>
            <h3>Notices</h3>
          </div>
          <span className="audit-count">{unreadCount} unread</span>
        </div>
        {notifications.length === 0 ? (
          <p className="empty-note">No notices yet.</p>
        ) : (
          <div className="customer-notice-list">
            {notifications.slice(0, 6).map((notification) => (
              <article className={notification.read ? "customer-notice read" : "customer-notice"} key={notification.id}>
                <div>
                  <span>{notification.type || "NOTICE"}</span>
                  <strong>{notification.title || "Notice"}</strong>
                  <p>{notification.message}</p>
                  <small>{dateTime(notification.createdAt)}</small>
                </div>
                {!notification.read && (
                  <button className="ghost-button" onClick={() => onMarkNoticeRead(notification.id)} type="button">
                    Mark Read
                  </button>
                )}
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="customer-grid">
        <form className="panel customer-card-form" onSubmit={submitVehicle}>
          <div className="settings-header">
            <span className="brand-mark"><UserCircle size={20} /></span>
            <div><span className="eyebrow">Garage</span><h3>Save a Vehicle</h3></div>
          </div>
          <Input label="Nickname" value={vehicleForm.nickname} onChange={(nickname) => setVehicleForm({ ...vehicleForm, nickname })} />
          <Input label="Year" value={vehicleForm.year} onChange={(year) => setVehicleForm({ ...vehicleForm, year })} />
          <Input label="Make" required value={vehicleForm.make} onChange={(make) => setVehicleForm({ ...vehicleForm, make })} />
          <Input label="Model" required value={vehicleForm.model} onChange={(model) => setVehicleForm({ ...vehicleForm, model })} />
          <Input label="Plate" value={vehicleForm.plateNumber} onChange={(plateNumber) => setVehicleForm({ ...vehicleForm, plateNumber })} />
          <Select label="Tire setup" value={vehicleForm.tireSetup} onChange={(tireSetup) => setVehicleForm({ ...vehicleForm, tireSetup })} options={["regular", "staggered"]} optionLabel={(option) => option === "regular" ? "Regular" : "Staggered"} />
          {vehicleForm.tireSetup === "staggered" ? (
            <>
              <Input label="Front tire size" required value={vehicleForm.frontTireSize} onChange={(frontTireSize) => setVehicleForm({ ...vehicleForm, frontTireSize })} placeholder="245/35/19" />
              <Input label="Rear tire size" required value={vehicleForm.rearTireSize} onChange={(rearTireSize) => setVehicleForm({ ...vehicleForm, rearTireSize })} placeholder="275/30/19" />
            </>
          ) : (
            <Input label="Tire size" value={vehicleForm.tireSize} onChange={(tireSize) => setVehicleForm({ ...vehicleForm, tireSize })} placeholder="225/45/17" />
          )}
          <button className="primary-button" type="submit">Save Vehicle</button>
        </form>

        <form className="panel customer-card-form" onSubmit={submitBooking}>
          <div className="settings-header">
            <span className="brand-mark"><CalendarDays size={20} /></span>
            <div><span className="eyebrow">Booking</span><h3>Book Service</h3></div>
          </div>
          <Select
            label="Vehicle"
            required
            value={bookingForm.vehicleId}
            onChange={(vehicleId) => {
              const vehicle = vehicles.find((entry) => String(entry.id) === String(vehicleId));
              setBookingForm({ ...bookingForm, vehicleId, locationId: vehicle?.locationId ? String(vehicle.locationId) : bookingForm.locationId });
            }}
            options={["", ...vehicles.map((vehicle) => String(vehicle.id))]}
            optionLabel={(id) => id ? vehicleName(vehicles.find((vehicle) => String(vehicle.id) === String(id))) : "Choose vehicle"}
          />
          {vehicles.length === 0 && <span className="empty-note">Add a vehicle first.</span>}
          {locations.length > 0 && (
            <Select
              label="Location"
              required
              value={bookingLocationId}
              onChange={(locationId) => setBookingForm({ ...bookingForm, locationId })}
              options={["", ...locations.map((location) => String(location.id))]}
              optionLabel={(value) => {
                const location = locations.find((entry) => String(entry.id) === String(value));
                return location ? [location.name, location.city].filter(Boolean).join(" - ") : "Select location";
              }}
            />
          )}
          {isLoadingLocations && <span className="empty-note">Loading locations...</span>}
          <ServiceTypeSelect required value={bookingForm.serviceType} onChange={(serviceType) => setBookingForm({ ...bookingForm, serviceType })} />
          <OwnTireServiceNote serviceType={bookingForm.serviceType} />
          <TireAvailabilityPanel
            availability={availability}
            error={availabilityError}
            isLoading={isCheckingAvailability}
            mode="customer"
          />
          {customerNeedsTireSourcing && (
            <div className="customer-sourcing-note">
              <strong>Request tire sourcing</strong>
              <span>The shop will look for this tire and confirm the appointment once availability is sorted.</span>
            </div>
          )}
          <Input label="Date" min={todayDateKey()} required type="date" value={bookingForm.appointmentDate} onChange={(appointmentDate) => setBookingForm({ ...bookingForm, appointmentDate })} />
          <fieldset className="customer-slot-picker">
            <legend>Available times</legend>
            {isLoadingSlots ? (
              <span className="empty-note">Loading slots...</span>
            ) : slots.length ? (
              <div className="time-slots">
                {slots.map((slot) => (
                  <button
                    className={bookingForm.appointmentTime === slot ? "selected" : ""}
                    key={slot}
                    onClick={() => setBookingForm({ ...bookingForm, appointmentTime: slot })}
                    type="button"
                  >
                    {slot}
                  </button>
                ))}
              </div>
            ) : (
              <span className="empty-note">No available times for this date.</span>
            )}
          </fieldset>
          <label className="customer-notes"><span>Notes</span><textarea value={bookingForm.notes} onChange={(event) => setBookingForm({ ...bookingForm, notes: event.target.value })} rows="3" /></label>
          <button className="primary-button" disabled={!vehicles.length || !slots.length || !bookingForm.appointmentTime} type="submit">
            {customerBookingSubmitLabel(availability)}
          </button>
        </form>
      </section>

      <section className="work-area">
        <DataTable
          actions={(vehicle) => <button className="danger-button" onClick={() => onDeleteVehicle(vehicle.id)} type="button">Remove</button>}
          columns={["Vehicle", "Location", "Plate", "Setup", "Tire Size", "Saved", ""]}
          emptyText="No saved vehicles yet."
          rows={vehicles.map((vehicle) => ({ key: `vehicle-${vehicle.id}`, source: vehicle, values: [vehicleName(vehicle), vehicle.locationName || "Preferred shop", vehicle.plateNumber || "-", vehicle.tireSetup || "regular", vehicleTireSize(vehicle), dateTime(vehicle.createdAt)] }))}
        />
        <DataTable
          columns={["Date", "Location", "Vehicle", "Service", "Status", "Notes"]}
          emptyText="No appointments yet."
          rows={appointments.map((appointment) => ({ key: `customer-appt-${appointment.id}`, values: [dateTime(appointment.appointmentDate), appointment.locationName || "Preferred shop", appointment.vehicle || "-", serviceTypeLabel(appointment.serviceType), appointment.status || "-", appointment.notes || "-"] }))}
        />
        <DataTable
          columns={["Tire Size", "Vehicle", "Location", "Appointment", "Status", "Shop Response"]}
          emptyText="No tire requests yet."
          rows={tireRequests.map((request) => ({
            key: `customer-tire-request-${request.id}`,
            source: request,
            searchText: [request.requestedSize, request.vehicle, request.locationName, request.status, request.adminResponse].filter(Boolean).join(" "),
            values: [
              request.requestedSize || "-",
              request.vehicle || "-",
              request.locationName || "Preferred shop",
              request.appointmentDate ? dateTime(request.appointmentDate) : request.appointmentId ? `#${request.appointmentId}` : "-",
              request.status || "PENDING",
              request.adminResponse || tireRequestCustomerMessage(request.status)
            ]
          }))}
        />
        <DataTable
          actions={(estimate) => (
            ["DRAFT", "SENT", "VIEWED"].includes(String(estimate.status || "DRAFT").toUpperCase()) ? (
              <button className="primary-button" disabled={approvingEstimateId === estimate.id} onClick={() => approveEstimate(estimate.id)} type="button">
                {approvingEstimateId === estimate.id ? "Approving..." : "Approve"}
              </button>
            ) : null
          )}
          columns={["Estimate", "Vehicle", "Total", "Status", "Valid Until", "Appointment", "Created", ""]}
          emptyText="No estimates yet."
          rows={estimates.map((estimate) => ({
            key: `customer-estimate-${estimate.id}`,
            source: estimate,
            values: [
              estimate.estimateNumber || `#${estimate.id}`,
              estimate.vehicle || "-",
              money(estimate.total),
              estimate.status || "DRAFT",
              estimate.validUntil || "-",
              estimate.appointmentId ? `#${estimate.appointmentId}` : "-",
              dateTime(estimate.createdAt)
            ]
          }))}
        />
        <DataTable
          actions={(invoice) => (
            invoiceStatusKey(invoice.status) !== "PAID" ? (
              <button className="primary-button" disabled={payingInvoiceId === invoice.id} onClick={() => payInvoice(invoice.id)} type="button">
                {payingInvoiceId === invoice.id ? "Paying..." : "Pay"}
              </button>
            ) : null
          )}
          columns={["Invoice", "Vehicle", "Total", "Paid", "Balance", "Status", "Due", "Paid At", "Created", ""]}
          emptyText="No invoices yet."
          rows={invoices.map((invoice) => ({ key: `customer-invoice-${invoice.id}`, source: invoice, values: [invoiceNumber(invoice), invoice.vehicle || "-", money(invoice.total), money(invoiceCollectedAmount(invoice)), money(invoiceBalanceAmount(invoice)), invoiceDisplayStatus(invoice), invoice.dueDate || "-", dateTime(invoice.paidAt), dateTime(invoice.createdAt)] }))}
        />
      </section>
      <div className="monarch-footer customer-footer">
        <strong>Powered by Monarch Solutions</strong>
        <a href="mailto:support@monarchsolutions.ca">Support: support@monarchsolutions.ca</a>
      </div>
    </main>
  );
}

function vehicleName(vehicle) {
  if (!vehicle) {
    return "Select vehicle";
  }

  return [vehicle.nickname, vehicle.year, vehicle.make, vehicle.model].filter(Boolean).join(" ") || "Vehicle";
}

function vehicleTireSize(vehicle) {
  if (!vehicle) {
    return "-";
  }

  if (vehicle.tireSetup === "staggered") {
    return `Front: ${vehicle.frontTireSize || "-"} / Rear: ${vehicle.rearTireSize || "-"}`;
  }

  return vehicle.tireSize || "-";
}

function PublicBookingPage({ onToggleTheme, themeMode }) {
  const [form, setForm] = useState({
    customerName: "",
    email: "",
    phone: "",
    shopId: "",
    locationId: "",
    vehicle: "",
    tireSize: "",
    appointmentDate: todayDateKey(),
    appointmentTime: "",
    serviceType: "INSTALLATION",
    notes: ""
  });
  const [slots, setSlots] = useState([]);
  const [shops, setShops] = useState([]);
  const [locations, setLocations] = useState([]);
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [isLoadingLocations, setIsLoadingLocations] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (message || error) {
      scrollPageToTop();
    }
  }, [message, error]);

  useEffect(() => {
    async function loadPublicShops() {
      try {
        const publicShops = await getPublicShops();
        setShops(publicShops || []);
        if ((publicShops || []).length === 1) {
          setForm((current) => ({ ...current, shopId: current.shopId || String(publicShops[0].id) }));
        }
      } catch {
        setShops([]);
      }
    }

    loadPublicShops();
  }, []);

  useEffect(() => {
    async function loadPublicLocations() {
      if (!form.shopId) {
        setLocations([]);
        return;
      }

      setIsLoadingLocations(true);

      try {
        const publicLocations = publicStoreLocations(await getPublicShopLocations(form.shopId));
        setLocations(publicLocations);
        setForm((current) => ({
          ...current,
          locationId: publicLocations?.some((location) => String(location.id) === String(current.locationId))
            ? current.locationId
            : publicLocations?.length === 1 ? String(publicLocations[0].id) : ""
        }));
      } catch {
        setLocations([]);
        setForm((current) => ({ ...current, locationId: "" }));
      } finally {
        setIsLoadingLocations(false);
      }
    }

    loadPublicLocations();
  }, [form.shopId]);

  useEffect(() => {
    async function loadSlots() {
      if (!form.appointmentDate || (locations.length > 0 && !form.locationId)) {
        setSlots([]);
        return;
      }

      setIsLoadingSlots(true);
      setError("");

      try {
        const availableSlots = await getAvailableSlots(form.appointmentDate, form.locationId);
        setSlots(availableSlots || []);
        setForm((current) => ({
          ...current,
          appointmentTime: availableSlots?.includes(current.appointmentTime) ? current.appointmentTime : ""
        }));
      } catch (err) {
        setError(err.message || "Could not load available slots.");
      } finally {
        setIsLoadingSlots(false);
      }
    }

    loadSlots();
  }, [form.appointmentDate, form.locationId, locations.length]);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (!form.appointmentTime) {
      setError("Choose an available appointment time.");
      return;
    }

    setIsSubmitting(true);

    try {
      await createPublicBooking({
        ...form,
        shopId: form.shopId ? Number(form.shopId) : null,
        locationId: form.locationId ? Number(form.locationId) : null
      });
      setMessage("Booking request confirmed. We will see you at the selected time.");
      setForm({
        customerName: "",
        email: "",
        phone: "",
        shopId: form.shopId,
        locationId: form.locationId,
        vehicle: "",
        tireSize: "",
        appointmentDate: form.appointmentDate,
        appointmentTime: "",
        serviceType: "INSTALLATION",
        notes: ""
      });
      const availableSlots = await getAvailableSlots(form.appointmentDate, form.locationId);
      setSlots(availableSlots || []);
    } catch (err) {
      setError(err.message || "Booking could not be created.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="public-booking-shell">
      <section className="public-booking-panel">
        <div className="public-booking-header">
          <span className="brand-mark"><Disc3 size={22} /></span>
          <div>
            <span className="eyebrow">Book tire service</span>
            <h1>TireTrack Booking</h1>
          </div>
          <ThemeToggleButton onToggle={onToggleTheme} themeMode={themeMode} />
        </div>

        {message ? <div className="success-alert">{message}</div> : null}
        {error ? <div className="alert error">{error}</div> : null}

        <form className="public-booking-form" onSubmit={submit}>
          <Input label="Name" required value={form.customerName} onChange={(customerName) => update("customerName", customerName)} />
          <Input label="Email" type="email" value={form.email} onChange={(email) => update("email", email)} />
          <Input label="Phone" required value={form.phone} onChange={(phone) => update("phone", phone)} />
          {shops.length > 0 && (
            <Select
              label="Shop"
              required
              value={form.shopId}
              onChange={(shopId) => setForm((current) => ({ ...current, shopId, locationId: "" }))}
              options={["", ...shops.map((shop) => String(shop.id))]}
              optionLabel={(value) => shops.find((shop) => String(shop.id) === String(value))?.name || "Select shop"}
            />
          )}
          {locations.length > 0 && (
            <Select
              label="Location"
              required
              value={form.locationId}
              onChange={(locationId) => update("locationId", locationId)}
              options={["", ...locations.map((location) => String(location.id))]}
              optionLabel={(value) => {
                const location = locations.find((entry) => String(entry.id) === String(value));
                return location ? [location.name, location.city].filter(Boolean).join(" - ") : "Select location";
              }}
            />
          )}
          {isLoadingLocations && <span className="empty-note">Loading locations...</span>}
          <Input label="Vehicle" required value={form.vehicle} onChange={(vehicle) => update("vehicle", vehicle)} />
          <Input label="Tire size" value={form.tireSize} onChange={(tireSize) => update("tireSize", tireSize)} placeholder="225/45/17" />
          <ServiceTypeSelect
            required
            value={form.serviceType}
            onChange={(serviceType) => update("serviceType", serviceType)}
          />
          <OwnTireServiceNote serviceType={form.serviceType} />
          <Input label="Date" min={todayDateKey()} required type="date" value={form.appointmentDate} onChange={(appointmentDate) => update("appointmentDate", appointmentDate)} />

          <fieldset className="public-slot-picker">
            <legend>Available times</legend>
            {isLoadingSlots ? (
              <span className="empty-note">Loading slots...</span>
            ) : slots.length ? (
              <div className="time-slots">
                {slots.map((slot) => (
                  <button
                    className={form.appointmentTime === slot ? "selected" : ""}
                    key={slot}
                    onClick={() => update("appointmentTime", slot)}
                    type="button"
                  >
                    {slot}
                  </button>
                ))}
              </div>
            ) : (
              <span className="empty-note">No open slots for this day.</span>
            )}
          </fieldset>

          <label className="public-notes">
            <span>Notes</span>
            <textarea value={form.notes} onChange={(event) => update("notes", event.target.value)} rows="4" />
          </label>

          <div className="public-booking-actions">
            <button className="primary-button with-icon" disabled={isSubmitting} type="submit">
              <CalendarDays size={18} />
              {isSubmitting ? "Booking..." : "Book Appointment"}
            </button>
            <button className="ghost-button" onClick={() => { window.location.href = "/login"; }} type="button">
              Staff Sign In
            </button>
          </div>
        </form>
        <div className="login-footer">
          <strong>Powered by Monarch Solutions</strong>
          <a href="mailto:support@monarchsolutions.ca">Support: support@monarchsolutions.ca</a>
        </div>
      </section>
    </main>
  );
}

function LoginScreen({ onSubmit, loginForm, setLoginForm, error, isSubmitting, onToggleTheme, themeMode }) {
  return (
    <main className="login-shell">
      <ThemeToggleButton className="theme-floating-toggle" onToggle={onToggleTheme} themeMode={themeMode} />
      <motion.section
        animate={{ opacity: 1, y: 0 }}
        className="login-panel"
        initial={{ opacity: 0, y: 16 }}
        transition={{ duration: 0.4 }}
      >
        <div className="brand-mark large"><Disc3 size={30} /></div>
        <span className="eyebrow">Business dashboard</span>
        <h1>TireTrack</h1>
        <p>Sign in to manage inventory, appointments, invoices, and analytics.</p>

        {error ? <div className="alert error">{error}</div> : null}

        <form onSubmit={onSubmit} className="login-form">
          <label>
            Email
            <input
              type="email"
              value={loginForm.email}
              onChange={(event) => setLoginForm({ ...loginForm, email: event.target.value })}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={loginForm.password}
              onChange={(event) => setLoginForm({ ...loginForm, password: event.target.value })}
              required
            />
          </label>
          <button className="primary-button with-icon" disabled={isSubmitting} type="submit">
            <LogIn size={18} />
            {isSubmitting ? "Signing In..." : "Sign In"}
          </button>
          <button className="ghost-button" onClick={() => { window.location.href = "/booking"; }} type="button">
            Customer Booking
          </button>
          <button className="ghost-button" onClick={() => { window.location.href = "/customer/signup"; }} type="button">
            Create Customer Account
          </button>
        </form>
        <div className="login-footer">
          <strong>Powered by Monarch Solutions</strong>
          <a href="mailto:support@monarchsolutions.ca">Support: support@monarchsolutions.ca</a>
        </div>
      </motion.section>
    </main>
  );
}

function CustomerSignupScreen({ error, form, isSubmitting, onSubmit, onToggleTheme, setForm, themeMode }) {
  const [shops, setShops] = useState([]);
  const [locations, setLocations] = useState([]);
  const [isLoadingLocations, setIsLoadingLocations] = useState(false);

  useEffect(() => {
    async function loadPublicShops() {
      try {
        const publicShops = await getPublicShops();
        setShops(publicShops || []);
        if ((publicShops || []).length === 1) {
          setForm((current) => ({ ...current, shopId: current.shopId || String(publicShops[0].id) }));
        }
      } catch {
        setShops([]);
      }
    }

    loadPublicShops();
  }, [setForm]);

  useEffect(() => {
    async function loadPublicLocations() {
      if (!form.shopId) {
        setLocations([]);
        return;
      }

      setIsLoadingLocations(true);

      try {
        const publicLocations = publicStoreLocations(await getPublicShopLocations(form.shopId));
        setLocations(publicLocations);
        setForm((current) => ({
          ...current,
          locationId: publicLocations?.some((location) => String(location.id) === String(current.locationId))
            ? current.locationId
            : publicLocations?.length === 1 ? String(publicLocations[0].id) : ""
        }));
      } catch {
        setLocations([]);
        setForm((current) => ({ ...current, locationId: "" }));
      } finally {
        setIsLoadingLocations(false);
      }
    }

    loadPublicLocations();
  }, [form.shopId, setForm]);

  return (
    <main className="login-shell">
      <ThemeToggleButton className="theme-floating-toggle" onToggle={onToggleTheme} themeMode={themeMode} />
      <motion.section
        animate={{ opacity: 1, y: 0 }}
        className="login-panel"
        initial={{ opacity: 0, y: 16 }}
        transition={{ duration: 0.4 }}
      >
        <div className="brand-mark large"><UserCircle size={30} /></div>
        <span className="eyebrow">Customer account</span>
        <h1>TireTrack</h1>
        <p>Create an account to save vehicles, book faster, and view your invoices.</p>

        {error ? <div className="alert error">{error}</div> : null}

        <form className="login-form" onSubmit={onSubmit}>
          <label>
            Full name
            <input required value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} />
          </label>
          <label>
            Email
            <input
              pattern="^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,63}$"
              required
              title="Enter a valid email address"
              type="email"
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
            />
          </label>
          <label>
            Phone
            <input
              inputMode="tel"
              maxLength="12"
              required
              type="tel"
              value={form.phone}
              onChange={(event) => setForm({ ...form, phone: formatCanadianPhoneInput(event.target.value) })}
            />
          </label>
          {shops.length > 0 && (
            <Select
              label="Shop"
              required
              value={form.shopId || ""}
              onChange={(shopId) => setForm({ ...form, shopId, locationId: "" })}
              options={["", ...shops.map((shop) => String(shop.id))]}
              optionLabel={(value) => shops.find((shop) => String(shop.id) === String(value))?.name || "Select shop"}
            />
          )}
          {locations.length > 0 && (
            <Select
              label="Preferred location"
              required
              value={form.locationId || ""}
              onChange={(locationId) => setForm({ ...form, locationId })}
              options={["", ...locations.map((location) => String(location.id))]}
              optionLabel={(value) => {
                const location = locations.find((entry) => String(entry.id) === String(value));
                return location ? [location.name, location.city].filter(Boolean).join(" - ") : "Select location";
              }}
            />
          )}
          {isLoadingLocations && <span className="empty-note">Loading locations...</span>}
          <label>
            Password
            <input
              minLength="8"
              pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$"
              required
              title="Use at least 8 characters with uppercase, lowercase, number, and symbol"
              type="password"
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
            />
          </label>
          <label>
            Confirm password
            <input
              required
              type="password"
              value={form.confirmPassword}
              onChange={(event) => setForm({ ...form, confirmPassword: event.target.value })}
            />
          </label>
          <button className="primary-button with-icon" disabled={isSubmitting} type="submit">
            <LogIn size={18} />
            {isSubmitting ? "Creating..." : "Create Account"}
          </button>
          <button className="ghost-button" onClick={() => { window.location.href = "/login"; }} type="button">
            Staff / Customer Sign In
          </button>
        </form>
        <div className="login-footer">
          <strong>Powered by Monarch Solutions</strong>
          <a href="mailto:support@monarchsolutions.ca">Support: support@monarchsolutions.ca</a>
        </div>
      </motion.section>
    </main>
  );
}

function DashboardSkeleton() {
  return (
    <div className="skeleton-stack" aria-label="Loading dashboard">
      <section className="metric-grid">
        {Array.from({ length: 5 }).map((_, index) => (
          <div className="skeleton-card" key={index} />
        ))}
      </section>
      <section className="split">
        <div className="skeleton-panel" />
        <div className="skeleton-panel" />
      </section>
    </div>
  );
}

function ActivityPanel({ children, icon: Icon, title, className = "" }) {
  return (
    <motion.section
      animate={{ opacity: 1, y: 0 }}
      className={`activity-panel panel ${className}`}
      initial={{ opacity: 0, y: 10 }}
      transition={{ duration: 0.35 }}
    >
      <div className="activity-title">
        <span className="metric-icon small"><Icon size={17} /></span>
        <h3>{title}</h3>
      </div>
      <div className="activity-list">{children}</div>
    </motion.section>
  );
}

function AccountingPage({
  accountForm,
  accounts,
  activeAccountingTab,
  expenseForm,
  message,
  onAccountChange,
  onExpenseChange,
  onSubmitAccount,
  onSubmitExpense,
  onSubmitVendor,
  onPayExpense,
  onRangeChange,
  onTabChange,
  onVendorChange,
  report,
  reportLoading = false,
  reportRange = defaultAccountingRange,
  selectedLocationId = "",
  shopLocations = [],
  vendorForm,
  vendors
}) {
  const trialBalance = report?.trialBalance || [];
  const profitAndLoss = report?.profitAndLoss || [];
  const balanceSheet = report?.balanceSheet || [];
  const recentExpenses = report?.recentExpenses || [];
  const recentJournalEntries = report?.recentJournalEntries || [];
  const expenseAccounts = accounts.filter((account) => account.type === "EXPENSE");
  const expenseTotal = calculateExpenseTotal(expenseForm);
  const paidExpenses = recentExpenses.filter((expense) => String(expense.status || "").toUpperCase() === "PAID");
  const unpaidExpenses = recentExpenses.filter((expense) => String(expense.status || "").toUpperCase() === "UNPAID");
  const overdueExpenses = recentExpenses.filter((expense) => expenseDisplayStatus(expense) === "OVERDUE");
  const paidExpenseTotal = paidExpenses.reduce((sum, expense) => sum + Number(expense.total || 0), 0);
  const unpaidExpenseTotal = unpaidExpenses.reduce((sum, expense) => sum + Number(expense.total || 0), 0);
  const overdueExpenseTotal = overdueExpenses.reduce((sum, expense) => sum + Number(expense.total || 0), 0);
  const vendorTotals = recentExpenses.reduce((totals, expense) => {
    const vendor = expense.vendor || "Unknown";
    totals[vendor] = (totals[vendor] || 0) + Number(expense.total || 0);
    return totals;
  }, {});
  const topVendorSpend = Object.entries(vendorTotals)
    .map(([name, value]) => ({ name, value }))
    .sort((first, second) => second.value - first.value)
    .slice(0, 5);
  const activeMeta = accountingTabMeta[activeAccountingTab] || accountingTabMeta.Dashboard;
  const [selectedLedgerAccountId, setSelectedLedgerAccountId] = useState("");
  const selectedLedgerAccount = trialBalance.find((account) => String(account.accountId) === String(selectedLedgerAccountId)) || trialBalance[0] || null;
  const selectedLedgerLines = selectedLedgerAccount
    ? recentJournalEntries.flatMap((entry) =>
      (entry.lines || [])
        .filter((line) => String(line.account?.id) === String(selectedLedgerAccount.accountId))
        .map((line) => ({ entry, line }))
    )
    : [];
  const accountsByType = ["ASSET", "LIABILITY", "EQUITY", "REVENUE", "EXPENSE"].map((type) => ({
    type,
    accounts: accounts.filter((account) => account.type === type),
    balance: trialBalance
      .filter((balance) => balance.type === type)
      .reduce((sum, balance) => sum + Number(balance.balance || 0), 0)
  }));

  return (
    <section className="accounting-page accounting-page-full">
      <main className="accounting-main">
        <section className="accounting-hero panel">
          <div>
            <span className="eyebrow">{activeMeta.eyebrow}</span>
            <h2>{activeAccountingTab}</h2>
            <p>{activeMeta.description}</p>
          </div>
          <div className="accounting-hero-stat">
            <span>{activeMeta.statLabel}</span>
            <strong>{activeMeta.statValue({ report, recentExpenses, vendors, accounts, recentJournalEntries })}</strong>
          </div>
        </section>

        <AccountingRangePicker loading={reportLoading} onChange={onRangeChange} range={reportRange} />

        <div className="accounting-mobile-nav">
          <select aria-label="Accounting section" value={activeAccountingTab} onChange={(event) => onTabChange(event.target.value)}>
            {accountingTabs.map((tab) => <option key={tab} value={tab}>{tab}</option>)}
          </select>
        </div>

        {message && <div className="success-alert">{message}</div>}

        {activeAccountingTab === "Dashboard" && (
          <>
            <section className="accounting-dashboard-strip panel">
              <div className="accounting-dashboard-title">
                <span className="eyebrow">Finance Workspace</span>
                <h3>Accounting Health</h3>
              </div>
              <div className="accounting-mini-ledger">
                <div className="paid"><span>Paid</span><strong>{paidExpenses.length}</strong></div>
                <div className="unpaid"><span>Unpaid</span><strong>{unpaidExpenses.length}</strong></div>
                <div className="overdue"><span>Overdue</span><strong>{overdueExpenses.length}</strong></div>
                <div><span>Accounts</span><strong>{accounts.length}</strong></div>
              </div>
            </section>
            <section className="accounting-kpis">
              <AccountingKpi label="Revenue" value={money(report?.revenue)} detail={`${accountingRangeLabel(reportRange)} posted sales`} />
              <AccountingKpi label="Expenses" value={money(report?.expenses)} detail="Includes payroll costs when payroll is paid" />
              <AccountingKpi label="Profit / loss" value={money(report?.netIncome)} detail="Revenue less all posted costs" />
            </section>
            <section className="accounting-chart-grid">
              <AccountingFinancialChart report={report} />
              <AccountingExpenseStatusChart paid={paidExpenses.length} unpaid={unpaidExpenses.length} overdue={overdueExpenses.length} />
              <AccountingVendorSpendChart vendors={topVendorSpend} />
            </section>
            <section className="accounting-overview-grid">
              <ReportPanel title="Profit & Loss" rows={profitAndLoss} />
              <ReportPanel title="Balance Sheet" rows={balanceSheet} />
            </section>
          </>
        )}

        {activeAccountingTab === "Expenses" && (
          <section className="expenses-workspace">
            <section className="expense-status-overview">
              <AccountingKpi label="Paid Expenses" value={money(paidExpenseTotal)} detail={`${paidExpenses.length} posted`} />
              <AccountingKpi label="Unpaid Bills" value={money(unpaidExpenseTotal)} detail={`${unpaidExpenses.length} open`} />
              <AccountingKpi label="Overdue" value={money(overdueExpenseTotal)} detail={`${overdueExpenses.length} urgent`} />
            </section>

            <section className="expenses-stack">
              <form className="panel accounting-form accounting-form-card" onSubmit={onSubmitExpense}>
              <div className="accounting-panel-head">
            <div>
              <span className="eyebrow">Expenses</span>
              <h3>Record Expense</h3>
              <p>Creates an expense record and balanced journal entry.</p>
            </div>
          </div>
          <div className="accounting-field-grid">
            <label>
              <span>Vendor</span>
              <select value={expenseForm.vendorId} onChange={(event) => {
                const vendor = vendors.find((entry) => String(entry.id) === event.target.value);
                onExpenseChange({
                  ...expenseForm,
                  vendorId: event.target.value,
                  vendor: vendor?.name || "",
                  category: vendor?.category || expenseForm.category,
                  categoryKey: vendor?.categoryKey === "OTHER" ? "OTHER" : expenseForm.categoryKey,
                  customCategory: vendor?.categoryKey === "OTHER" ? vendor?.customCategory || "" : expenseForm.customCategory
                });
              }}>
                <option value="">Manual vendor</option>
                {vendors.map((vendor) => <option key={vendor.id} value={vendor.id}>{vendor.name}</option>)}
              </select>
            </label>
            <label><span>Vendor name</span><input placeholder="Supplier or payee" value={expenseForm.vendor} onChange={(event) => onExpenseChange({ ...expenseForm, vendor: event.target.value, vendorId: "" })} required /></label>
            <label><span>Date</span><input type="date" value={expenseForm.expenseDate} onChange={(event) => onExpenseChange({ ...expenseForm, expenseDate: event.target.value })} /></label>
          </div>
          <div className="accounting-field-grid">
            <label>
              <span>Expense account</span>
              <select value={expenseForm.expenseAccountId} onChange={(event) => onExpenseChange({ ...expenseForm, expenseAccountId: event.target.value })}>
                <option value="">5000 - Operating Expenses</option>
                {expenseAccounts.map((account) => <option key={account.id} value={account.id}>{account.code} - {account.name}</option>)}
              </select>
            </label>
            {shopLocations.length > 0 && (
              <label>
                <span>Location</span>
                <select value={expenseForm.locationId || selectedLocationId || ""} onChange={(event) => onExpenseChange({ ...expenseForm, locationId: event.target.value })}>
                  <option value="">Shared / all locations</option>
                  {shopLocations.map((location) => <option key={location.id} value={location.id}>{location.name}</option>)}
                </select>
              </label>
            )}
            <label>
              <span>Category</span>
              <select value={expenseForm.categoryKey} onChange={(event) => onExpenseChange({ ...expenseForm, categoryKey: event.target.value, category: resolveAccountingLabel(event.target.value, expenseForm.customCategory) })}>
                {expenseCategoryOptions.map((option) => <option key={option} value={option}>{accountingOptionLabel(option)}</option>)}
              </select>
            </label>
            {expenseForm.categoryKey === "OTHER" && (
              <label><span>Custom category</span><input placeholder="Add category" value={expenseForm.customCategory} onChange={(event) => onExpenseChange({ ...expenseForm, customCategory: event.target.value, category: event.target.value })} /></label>
            )}
          </div>
          <div className="accounting-field-grid totals">
            <label><span>Subtotal</span><input min="0" step="0.01" type="number" value={expenseForm.subtotal} onChange={(event) => onExpenseChange({ ...expenseForm, subtotal: event.target.value })} /></label>
            <label><span>Tax</span><input min="0" step="0.01" type="number" value={expenseForm.taxAmount} onChange={(event) => onExpenseChange({ ...expenseForm, taxAmount: event.target.value })} /></label>
            <label><span>Total</span><input readOnly value={money(expenseTotal)} /></label>
          </div>
          <div className="accounting-field-grid payment">
            <label><span>Status</span><select value={expenseForm.status} onChange={(event) => onExpenseChange({ ...expenseForm, status: event.target.value })}><option>PAID</option><option>UNPAID</option></select></label>
            <label>
              <span>Payment method</span>
              <select value={expenseForm.paymentMethodKey} onChange={(event) => onExpenseChange({ ...expenseForm, paymentMethodKey: event.target.value, paymentMethod: resolveAccountingLabel(event.target.value, expenseForm.customPaymentMethod) })}>
                {accountingPaymentOptions.map((option) => <option key={option} value={option}>{accountingOptionLabel(option)}</option>)}
              </select>
            </label>
            {expenseForm.paymentMethodKey === "OTHER" && (
              <label><span>Custom method</span><input placeholder="Add method" value={expenseForm.customPaymentMethod} onChange={(event) => onExpenseChange({ ...expenseForm, customPaymentMethod: event.target.value, paymentMethod: event.target.value })} /></label>
            )}
            {expenseForm.status === "UNPAID" && (
              <label><span>Due date</span><input type="date" value={expenseForm.dueDate} onChange={(event) => onExpenseChange({ ...expenseForm, dueDate: event.target.value })} /></label>
            )}
          </div>
          <label className="accounting-notes"><span>Internal note</span><textarea rows="3" value={expenseForm.notes} onChange={(event) => onExpenseChange({ ...expenseForm, notes: event.target.value })} /></label>
          <div className="accounting-submit-row">
            <span>Posts to the selected expense account plus Cash/AP.</span>
            <button className="primary-button" type="submit">Post Expense</button>
          </div>
              </form>
              <AccountingTablePanel title="Recent Expenses" detail="Posted vendor costs and unpaid bills.">
                <DataTable
                  actions={(expense) => (
                    expenseDisplayStatus(expense) !== "PAID" ? (
                      <div className="table-actions compact">
                        <button className="primary-button" onClick={() => onPayExpense(expense, "CASH")} type="button">Pay Cash</button>
                        <button className="ghost-button" onClick={() => onPayExpense(expense, "CREDIT_CARD")} type="button">Pay Credit</button>
                      </div>
                    ) : null
                  )}
                  columns={["Date", "Due", "Vendor", "Location", "Account", "Category", "Status", "Total", "Paid At", "Admin", ""]}
                  emptyText="No expenses recorded."
                  rows={recentExpenses.map((expense) => ({
                    key: `expense-${expense.id}`,
                    source: expense,
                    values: [
                      expense.expenseDate || "-",
                      expense.dueDate || "-",
                      expense.vendor,
                      expense.locationName || "Shared",
                      accountName(accounts, expense.expenseAccountId),
                      expense.category || "-",
                      expenseDisplayStatus(expense),
                      money(expense.total),
                      dateTime(expense.paidAt),
                      expense.createdBy || "-"
                    ]
                  }))}
                />
              </AccountingTablePanel>
            </section>
          </section>
        )}

        {activeAccountingTab === "Vendors" && (
          <section className="vendors-workspace">
            <section className="vendor-summary-grid">
              <AccountingKpi label="Vendors" value={vendors.length} detail="Saved suppliers" />
              <AccountingKpi label="Vendor Spend" value={money(Object.values(vendorTotals).reduce((sum, value) => sum + Number(value || 0), 0))} detail="Current report" />
              <AccountingKpi label="Top Vendor" value={topVendorSpend[0]?.name || "-"} detail={topVendorSpend[0] ? money(topVendorSpend[0].value) : "No spend yet"} />
            </section>

            {topVendorSpend.length > 0 && (
              <section className="top-vendor-list panel">
                <div className="accounting-section-title">
                  <h3>Vendor Activity</h3>
                  <p>Top suppliers by posted expense total.</p>
                </div>
                <div className="leader-bars">
                  {topVendorSpend.map((vendor, index) => (
                    <div className="leader-bar-row" key={vendor.name}>
                      <div className="leader-bar-label">
                        <span title={vendor.name}><b>{index + 1}</b>{vendor.name}</span>
                        <small>Current report spend</small>
                        <strong>{money(vendor.value)}</strong>
                      </div>
                      <div className="leader-bar-track">
                        <motion.div
                          animate={{ width: `${Math.max((vendor.value / Math.max(topVendorSpend[0]?.value || 1, 1)) * 100, 4)}%` }}
                          className="leader-bar-fill"
                          initial={{ width: 0 }}
                          transition={{ duration: 0.8, ease: "easeOut" }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            )}

            <section className="vendors-stack">
              <form className="panel accounting-form accounting-form-card" onSubmit={onSubmitVendor}>
            <div className="accounting-panel-head">
              <div>
                <span className="eyebrow">Vendors</span>
                <h3>Add Vendor</h3>
                <p>Save common suppliers so expenses link to vendor history.</p>
              </div>
            </div>
            <label><span>Name</span><input placeholder="Vendor name" value={vendorForm.name} onChange={(event) => onVendorChange({ ...vendorForm, name: event.target.value })} required /></label>
            <label>
              <span>Category</span>
              <select value={vendorForm.categoryKey} onChange={(event) => onVendorChange({ ...vendorForm, categoryKey: event.target.value, category: resolveAccountingLabel(event.target.value, vendorForm.customCategory) })}>
                {vendorCategoryOptions.map((option) => <option key={option} value={option}>{accountingOptionLabel(option)}</option>)}
              </select>
            </label>
            {vendorForm.categoryKey === "OTHER" && (
              <label><span>Custom category</span><input placeholder="Add category" value={vendorForm.customCategory} onChange={(event) => onVendorChange({ ...vendorForm, customCategory: event.target.value, category: event.target.value })} /></label>
            )}
            <label><span>Email</span><input value={vendorForm.email} onChange={(event) => onVendorChange({ ...vendorForm, email: event.target.value })} /></label>
            <label>
              <span>Phone</span>
              <input
                inputMode="tel"
                maxLength="12"
                type="tel"
                value={vendorForm.phone}
                onChange={(event) => onVendorChange({ ...vendorForm, phone: formatCanadianPhoneInput(event.target.value) })}
              />
            </label>
            <button className="primary-button" type="submit">Add Vendor</button>
              </form>
              <AccountingTablePanel title="Vendors" detail="Saved suppliers and current-period spending.">
                <DataTable
                  columns={["Vendor", "Category", "Phone", "Spent", "Admin"]}
                  emptyText="No vendors configured."
                  rows={vendors.map((vendor) => [
                    vendor.name,
                    vendor.category || "-",
                    vendor.phone || "-",
                    money(vendorTotals[vendor.name] || 0),
                    vendor.createdBy || "-"
                  ])}
                />
              </AccountingTablePanel>
            </section>
          </section>
        )}

        {activeAccountingTab === "Accounts" && (
          <section className="accounts-workspace">
            <section className="accounts-overview">
              {accountsByType.map((group) => (
                <AccountingKpi
                  detail={`${group.accounts.length} account${group.accounts.length === 1 ? "" : "s"}`}
                  key={group.type}
                  label={accountingOptionLabel(group.type)}
                  value={money(group.balance)}
                />
              ))}
            </section>

            <section className="accounting-two-column accounts-layout">
              <form className="panel accounting-form accounting-form-card" onSubmit={onSubmitAccount}>
                <div className="accounting-panel-head">
                  <div>
                    <span className="eyebrow">Chart of accounts</span>
                    <h3>Add Account</h3>
                    <p>Expense accounts become selectable when posting vendor costs.</p>
                  </div>
                </div>
                <label><span>Code</span><input placeholder="Example: 6100" value={accountForm.code} onChange={(event) => onAccountChange({ ...accountForm, code: event.target.value })} required /></label>
                <label><span>Name</span><input placeholder="Account name" value={accountForm.name} onChange={(event) => onAccountChange({ ...accountForm, name: event.target.value })} required /></label>
                <label><span>Type</span><select value={accountForm.type} onChange={(event) => onAccountChange({ ...accountForm, type: event.target.value })}><option>ASSET</option><option>LIABILITY</option><option>EQUITY</option><option>REVENUE</option><option>EXPENSE</option></select></label>
                <button className="primary-button" type="submit">Add Account</button>
              </form>
              <AccountingTablePanel title="Expense Accounts" detail="Only EXPENSE accounts appear in the expense posting dropdown.">
                <DataTable
                  columns={["Code", "Account", "Created By"]}
                  emptyText="No expense accounts configured."
                  rows={expenseAccounts.map((account) => [
                    account.code,
                    account.name,
                    account.createdBy || "-"
                  ])}
                />
              </AccountingTablePanel>
            </section>

            <section className="account-type-grid">
              {accountsByType.map((group) => (
                <AccountTypePanel key={group.type} group={group} />
              ))}
            </section>
          </section>
        )}

        {activeAccountingTab === "Reports" && (
          <section className="reports-workspace">
            <section className="report-statement-strip panel">
              <div>
                <span className="eyebrow">Reports</span>
                <h3>Financial Statements</h3>
                <p>Review performance and position from posted invoice, payment, and expense journals.</p>
              </div>
              <div className="report-statement-metrics">
                <div><span>Net Income</span><strong>{money(report?.netIncome)}</strong></div>
                <div><span>Assets</span><strong>{money(report?.assets)}</strong></div>
                <div><span>Liabilities</span><strong>{money(report?.liabilities)}</strong></div>
                <div><span>Equity</span><strong>{money(report?.equity)}</strong></div>
              </div>
            </section>

            <section className="accounting-two-column">
              <ReportPanel title="Profit & Loss" rows={profitAndLoss} />
              <ReportPanel title="Balance Sheet" rows={balanceSheet} />
            </section>
          </section>
        )}

        {activeAccountingTab === "Ledger" && (
          <section className="ledger-account-view">
            <section className="panel ledger-account-toolbar">
              <div>
                <span className="eyebrow">Ledger</span>
                <h3>Account Detail</h3>
                <p>Select one account to review its balance and journal activity.</p>
              </div>
              <label>
                <span>Account</span>
                <select value={selectedLedgerAccount ? String(selectedLedgerAccount.accountId) : ""} onChange={(event) => setSelectedLedgerAccountId(event.target.value)}>
                  {trialBalance.map((account) => (
                    <option key={account.accountId} value={account.accountId}>{account.code} - {account.name}</option>
                  ))}
                </select>
              </label>
            </section>

            {selectedLedgerAccount && (
              <section className="ledger-account-summary">
                <AccountingKpi label="Debits" value={money(selectedLedgerAccount.debits)} detail={selectedLedgerAccount.code} />
                <AccountingKpi label="Credits" value={money(selectedLedgerAccount.credits)} detail={selectedLedgerAccount.type} />
                <AccountingKpi label="Balance" value={money(selectedLedgerAccount.balance)} detail={selectedLedgerAccount.name} />
              </section>
            )}

            <AccountingTablePanel title="Selected Account Balance" detail="Focused trial balance row for the selected account.">
              <DataTable
                columns={["Code", "Account", "Type", "Debits", "Credits", "Balance"]}
                emptyText="No ledger activity."
                rows={(selectedLedgerAccount ? [selectedLedgerAccount] : []).map((account) => [
                  account.code,
                  account.name,
                  account.type,
                  money(account.debits),
                  money(account.credits),
                  money(account.balance)
                ])}
              />
            </AccountingTablePanel>
            <AccountingTablePanel title="Account Journal Lines" detail="Only entries touching the selected account are shown.">
              <DataTable
                columns={["Date", "Description", "Source", "Debit", "Credit", "Memo", "Posted By"]}
                emptyText="No journal lines for this account in the current report range."
                rows={selectedLedgerLines.map(({ entry, line }) => [
                  entry.entryDate,
                  entry.description,
                  entry.source || "-",
                  money(line.debit),
                  money(line.credit),
                  line.memo || "-",
                  entry.postedBy || "-"
                ])}
              />
            </AccountingTablePanel>
          </section>
        )}
      </main>
    </section>
  );
}

function AccountingRangePicker({ loading, onChange, range }) {
  const currentRange = range || defaultAccountingRange;
  const [customRange, setCustomRange] = useState({
    start: currentRange.start || todayDateKey(),
    end: currentRange.end || todayDateKey()
  });

  useEffect(() => {
    if (currentRange.mode === "custom") {
      setCustomRange({
        start: currentRange.start || todayDateKey(),
        end: currentRange.end || currentRange.start || todayDateKey()
      });
    }
  }, [currentRange.mode, currentRange.start, currentRange.end]);

  function chooseMode(mode) {
    if (mode === "custom") {
      onChange?.({ mode: "custom", ...customRange });
      return;
    }

    onChange?.({ ...defaultAccountingRange, mode });
  }

  function applyCustomRange() {
    onChange?.({ mode: "custom", start: customRange.start, end: customRange.end });
  }

  return (
    <section className="accounting-range-panel panel">
      <div>
        <span className="eyebrow">Report range</span>
        <strong>{accountingRangeLabel(currentRange)}</strong>
      </div>
      <div className="segmented-control">
        {["lifetime", "today", "month", "custom"].map((mode) => (
          <button
            className={currentRange.mode === mode ? "active" : ""}
            disabled={loading}
            key={mode}
            onClick={() => chooseMode(mode)}
            type="button"
          >
            {mode === "lifetime" ? "Lifetime" : mode === "month" ? "Month" : mode === "custom" ? "Custom" : "Today"}
          </button>
        ))}
      </div>
      {currentRange.mode === "custom" && (
        <div className="accounting-custom-range">
          <Input label="Start" type="date" value={customRange.start} onChange={(start) => setCustomRange({ ...customRange, start })} />
          <Input label="End" type="date" value={customRange.end} onChange={(end) => setCustomRange({ ...customRange, end })} />
          <button className="ghost-button" disabled={loading} onClick={applyCustomRange} type="button">
            Apply
          </button>
        </div>
      )}
      {loading && <span className="mini-status loading">Loading report...</span>}
    </section>
  );
}

function AccountingKpi({ detail, label, value }) {
  return (
    <article className="accounting-kpi-card panel">
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{detail}</small>
    </article>
  );
}

function AccountingChartCard({ children, detail, title }) {
  return (
    <section className="analytics-panel accounting-chart-card panel">
      <div className="sales-chart-header">
        <div>
          <span className="eyebrow">Accounting</span>
          <h3>{title}</h3>
          <p>{detail}</p>
        </div>
      </div>
      {children}
    </section>
  );
}

function AccountingFinancialChart({ report }) {
  const data = [
    { className: "available", name: "Revenue", value: Number(report?.revenue || 0) },
    { className: "warning", name: "Expenses", value: Number(report?.expenses || 0) },
    { className: Number(report?.netIncome || 0) >= 0 ? "available" : "urgent", name: "Net income", value: Number(report?.netIncome || 0) }
  ];
  const maxValue = Math.max(...data.map((item) => Math.abs(item.value)), 1);

  return (
    <AccountingChartCard title="Financial Mix" detail="Revenue, expenses, and net income.">
      <div className="bar-chart accounting-bar-chart">
        {data.map((item) => (
          <div className="bar-row" key={item.name}>
            <span>{item.name}</span>
            <div className="bar-track">
              <motion.div
                animate={{ width: `${Math.max((Math.abs(item.value) / maxValue) * 100, item.value === 0 ? 0 : 4)}%` }}
                className={`bar-fill ${item.className}`}
                initial={{ width: 0 }}
                transition={{ duration: 0.8, ease: "easeOut" }}
              />
            </div>
            <strong>{money(item.value)}</strong>
          </div>
        ))}
      </div>
    </AccountingChartCard>
  );
}

function AccountingExpenseStatusChart({ overdue, paid, unpaid }) {
  const segments = [
    { className: "available", color: "#18d3b2", label: "Paid", value: paid },
    { className: "warning", color: "#f59e0b", label: "Unpaid", value: unpaid },
    { className: "urgent", color: "#ef4444", label: "Overdue", value: overdue }
  ];
  const visibleSegments = segments.filter((segment) => Number(segment.value || 0) > 0);
  const total = visibleSegments.reduce((sum, segment) => sum + Number(segment.value || 0), 0);
  const radius = 15.9155;
  let offset = 25;

  return (
    <AccountingChartCard title="Expense Status" detail="Vendor bills that need attention.">
      <div className="accounting-donut-chart">
        {total === 0 ? (
          <p className="empty-note">No expense status data yet.</p>
        ) : (
          <div className="accounting-donut-safe">
            <div className="accounting-donut-visual" aria-label="Accounting expense status chart">
              <svg viewBox="0 0 42 42" role="img">
                <circle className="donut-ring" cx="21" cy="21" r={radius} />
                {visibleSegments.map((segment) => {
                  const length = (Number(segment.value || 0) / total) * 100;
                  const dashArray = `${length} ${100 - length}`;
                  const strokeDashoffset = offset;
                  offset -= length;

                  return (
                    <circle
                      className={`donut-segment ${segment.className}`}
                      cx="21"
                      cy="21"
                      key={segment.label}
                      r={radius}
                      strokeDasharray={dashArray}
                      strokeDashoffset={strokeDashoffset}
                    />
                  );
                })}
              </svg>
              <div className="donut-center">
                <strong>{total}</strong>
                <span>bills</span>
              </div>
            </div>
            <div className="accounting-donut-legend">
              {segments.map((segment) => (
                <div key={segment.label}>
                  <span style={{ background: segment.color }} />
                  <strong>{segment.label}</strong>
                  <small>{segment.value}</small>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </AccountingChartCard>
  );
}

function AccountingVendorSpendChart({ vendors }) {
  const trendData = vendors.map((vendor, index) => ({
    name: vendor.name,
    value: vendor.value,
    rank: index + 1
  }));

  return (
    <AccountingChartCard title="Top Vendor Spend" detail="Largest expense vendors in the current report.">
      {vendors.length === 0 ? (
        <p className="empty-note">No vendor expense data yet.</p>
      ) : (
        <div className="sales-line-chart accounting-chart">
          <ResponsiveContainer width="100%" height={210}>
            <LineChart data={trendData} margin={{ bottom: 4, left: 0, right: 18, top: 16 }}>
              <defs>
                <linearGradient id="vendorSpendGradient" x1="0" x2="0" y1="0" y2="1">
                  <stop offset="0%" stopColor="#18d3b2" stopOpacity={0.28} />
                  <stop offset="100%" stopColor="#18d3b2" stopOpacity={0.02} />
                </linearGradient>
              </defs>
              <CartesianGrid stroke="rgba(255,255,255,0.06)" vertical={false} />
              <XAxis dataKey="name" tick={{ fill: "#8f95a3", fontSize: 11 }} tickFormatter={shortChartLabel} tickLine={false} axisLine={false} interval={0} />
              <YAxis tick={{ fill: "#8f95a3", fontSize: 11 }} tickLine={false} axisLine={false} tickFormatter={(value) => `$${Number(value || 0).toLocaleString()}`} width={72} />
              <Tooltip formatter={(value) => money(value)} contentStyle={tooltipStyle} />
              <Area dataKey="value" fill="url(#vendorSpendGradient)" stroke="none" type="monotone" />
              <Line type="monotone" dataKey="value" stroke="#18d3b2" strokeWidth={4} dot={{ fill: "#18d3b2", r: 4 }} activeDot={{ r: 6 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}
    </AccountingChartCard>
  );
}

function shortChartLabel(value) {
  const text = String(value || "");
  return text.length > 10 ? `${text.slice(0, 9)}...` : text;
}

function AccountingSimplePanel({ children, detail, title }) {
  return (
    <section className="accounting-card panel">
      <AccountingSectionTitle title={title} detail={detail} />
      {children}
    </section>
  );
}

function AccountingTablePanel({ children, detail, title }) {
  return (
    <section className="accounting-table-card">
      <AccountingSectionTitle title={title} detail={detail} />
      {children}
    </section>
  );
}

function AccountTypePanel({ group }) {
  return (
    <section className="panel account-type-panel">
      <div className="account-type-head">
        <div>
          <span className="eyebrow">{group.type}</span>
          <h3>{accountingOptionLabel(group.type)}</h3>
        </div>
        <strong>{money(group.balance)}</strong>
      </div>
      {group.accounts.length === 0 ? (
        <p className="empty-note">No accounts yet.</p>
      ) : (
        <div className="account-type-list">
          {group.accounts.map((account) => (
            <div key={account.id}>
              <span>{account.code}</span>
              <strong>{account.name}</strong>
              <small>{account.systemAccount ? "System" : account.createdBy || "Custom"}</small>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function StatusPill({ label, tone, value }) {
  return (
    <div className={`accounting-status-pill ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function accountName(accounts, id) {
  const account = accounts.find((entry) => String(entry.id) === String(id));
  return account ? `${account.code} - ${account.name}` : "5000 - Operating Expenses";
}

function calculateExpenseTotal(expense) {
  return Number((Number(expense.subtotal || 0) + Number(expense.taxAmount || 0)).toFixed(2));
}

function expenseDisplayStatus(expense) {
  if (String(expense.status || "").toUpperCase() === "UNPAID") {
    const dateValue = expense.dueDate ? new Date(`${expense.dueDate}T00:00:00`) : null;
    if (dateValue && dateValue < new Date(new Date().setHours(0, 0, 0, 0))) {
      return "OVERDUE";
    }
  }

  return expense.status || "PAID";
}

function invoiceDisplayStatus(invoice) {
  const status = invoiceStatusKey(invoice.status);

  if (status === "PAID") {
    return "PAID";
  }

  if (invoice.dueDate) {
    const dueDate = new Date(`${invoice.dueDate}T00:00:00`);
    const today = new Date(new Date().setHours(0, 0, 0, 0));

    if (dueDate < today) {
      return "OVERDUE";
    }
  }

  return status === "UNPAID" ? "UNPAID" : status;
}

function ReportPanel({ rows, title }) {
  const detail = title === "Profit & Loss"
    ? "Revenue and expense performance."
    : "Assets, liabilities, and equity position.";

  return (
    <div>
      <AccountingSectionTitle title={title} detail={detail} />
      <DataTable
        columns={["Code", "Account", "Type", "Balance"]}
        emptyText="No report data yet."
        rows={rows.map((account) => [
          account.code,
          account.name,
          account.type,
          money(account.balance)
        ])}
      />
    </div>
  );
}

function AccountingSectionTitle({ detail, title }) {
  return (
    <div className="accounting-section-title">
      <h3>{title}</h3>
      <p>{detail}</p>
    </div>
  );
}

function EmployeePortal({ auth, onOpenPayroll }) {
  const [todayAttendance, setTodayAttendance] = useState(null);
  const [history, setHistory] = useState([]);
  const [range, setRange] = useState(() => {
    const start = new Date();
    start.setDate(start.getDate() - 13);
    return { start: toDateKey(start), end: todayDateKey() };
  });
  const [isLoading, setIsLoading] = useState(true);
  const [isWorking, setIsWorking] = useState(false);
  const [message, setMessage] = useState("");
  const [portalError, setPortalError] = useState("");

  useEffect(() => {
    loadAttendance();
  }, []);

  async function loadAttendance(nextRange = range) {
    setIsLoading(true);
    setPortalError("");

    try {
      const [today, rows] = await Promise.all([
        getMyTodayAttendance().catch(() => null),
        getMyAttendanceRange(nextRange.start, nextRange.end).catch(() => [])
      ]);
      setTodayAttendance(today);
      setHistory(rows || []);
    } catch (err) {
      setPortalError(err.message || "Attendance could not be loaded.");
    } finally {
      setIsLoading(false);
    }
  }

  async function runClockAction(action, successText) {
    setIsWorking(true);
    setMessage("");
    setPortalError("");

    try {
      const updated = await action();
      setTodayAttendance(updated);
      setMessage(successText);
      await loadAttendance();
    } catch (err) {
      setPortalError(err.message || "Attendance action failed.");
    } finally {
      setIsWorking(false);
    }
  }

  async function submitRange(event) {
    event.preventDefault();
    await loadAttendance(range);
  }

  const clockedIn = Boolean(todayAttendance?.clockIn);
  const clockedOut = Boolean(todayAttendance?.clockOut);
  const statusText = !todayAttendance
    ? "Not clocked in"
    : clockedOut
      ? "Clocked out"
      : clockedIn
        ? "Clocked in"
        : todayAttendance.status || "Pending";

  return (
    <section className="work-area employee-portal">
      <div className="section-toolbar">
        <div>
          <span className="eyebrow">{formatDateLabel(todayDateKey())}</span>
          <h3>Hi, {auth?.fullName || "there"}</h3>
        </div>
        <button className="ghost-button" onClick={onOpenPayroll} type="button">My Payroll</button>
      </div>

      {isLoading && <div className="loading">Loading attendance...</div>}
      {portalError && <div className="alert error">{portalError}</div>}
      {message && <div className="success-alert">{message}</div>}

      <section className="metric-grid">
        <div className="metric-card"><span>Today</span><strong>{statusText}</strong></div>
        <div className="metric-card"><span>Clock in</span><strong>{attendanceTime(todayAttendance?.clockIn)}</strong></div>
        <div className="metric-card"><span>Clock out</span><strong>{attendanceTime(todayAttendance?.clockOut)}</strong></div>
        <div className="metric-card"><span>Worked hours</span><strong>{attendanceHours(todayAttendance?.workedHours)}</strong></div>
        <div className="metric-card"><span>Shop</span><strong>{auth?.shopName || "Unassigned"}</strong></div>
      </section>

      <section className="panel employee-clock-panel">
        <button
          className="primary-button"
          disabled={isWorking || clockedIn}
          onClick={() => runClockAction(clockIn, "Clocked in. Have a good shift.")}
          type="button"
        >
          Clock In
        </button>
        <button
          className="ghost-button"
          disabled={isWorking || !clockedIn || clockedOut}
          onClick={() => runClockAction(clockOut, "Clocked out. Hours saved.")}
          type="button"
        >
          Clock Out
        </button>
      </section>

      <section className="panel audit-log-panel">
        <div className="section-toolbar">
          <div>
            <span className="eyebrow">My attendance</span>
            <h3>History</h3>
          </div>
          <form className="attendance-range-form" onSubmit={submitRange}>
            <input type="date" value={range.start} onChange={(event) => setRange({ ...range, start: event.target.value })} />
            <input type="date" value={range.end} onChange={(event) => setRange({ ...range, end: event.target.value })} />
            <button className="ghost-button" type="submit">Load</button>
          </form>
        </div>
        <AttendanceTable records={history} emptyText="No attendance records in this range." />
      </section>
    </section>
  );
}

function AttendancePage({ auth, selectedLocationId = "" }) {
  const [employees, setEmployees] = useState([]);
  const [todayRows, setTodayRows] = useState([]);
  const [unresolvedAbsences, setUnresolvedAbsences] = useState([]);
  const [employeeRows, setEmployeeRows] = useState([]);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState("");
  const [absenceDate, setAbsenceDate] = useState(todayDateKey());
  const [range, setRange] = useState(() => {
    const start = new Date();
    start.setDate(start.getDate() - 13);
    return { start: toDateKey(start), end: todayDateKey() };
  });
  const [resolveDrafts, setResolveDrafts] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [isWorking, setIsWorking] = useState(false);
  const [message, setMessage] = useState("");
  const [attendanceError, setAttendanceError] = useState("");

  useEffect(() => {
    loadAttendanceAdmin();
  }, [auth?.role, selectedLocationId]);

  async function loadAttendanceAdmin(nextEmployeeId = selectedEmployeeId) {
    setIsLoading(true);
    setAttendanceError("");

    try {
      const [employeeList, dayRows, unresolved] = await Promise.all([
        getAttendanceEmployees().catch(() => []),
        getAttendanceByDate(todayDateKey(), selectedLocationId).catch(() => []),
        getUnresolvedAbsences().catch(() => [])
      ]);
      const safeEmployees = employeeList || [];
      const resolvedEmployeeId = nextEmployeeId || safeEmployees[0]?.id || "";
      setEmployees(safeEmployees);
      setTodayRows(dayRows || []);
      setUnresolvedAbsences(unresolved || []);
      setSelectedEmployeeId(resolvedEmployeeId);
      setEmployeeRows(resolvedEmployeeId ? await getEmployeeAttendanceRange(resolvedEmployeeId, range.start, range.end, selectedLocationId).catch(() => []) : []);
    } catch (err) {
      setAttendanceError(err.message || "Attendance data could not be loaded.");
    } finally {
      setIsLoading(false);
    }
  }

  async function loadEmployeeRange(event) {
    event.preventDefault();

    if (!selectedEmployeeId) {
      setAttendanceError("Choose an employee first.");
      return;
    }

    setIsWorking(true);
    setAttendanceError("");

    try {
      setEmployeeRows(await getEmployeeAttendanceRange(selectedEmployeeId, range.start, range.end, selectedLocationId));
    } catch (err) {
      setAttendanceError(err.message || "Employee attendance could not be loaded.");
    } finally {
      setIsWorking(false);
    }
  }

  async function markAbsent() {
    if (!selectedEmployeeId) {
      setAttendanceError("Choose an employee first.");
      return;
    }

    setIsWorking(true);
    setMessage("");
    setAttendanceError("");

    try {
      await markEmployeeAbsent(selectedEmployeeId, absenceDate);
      setMessage("Absence marked.");
      await loadAttendanceAdmin(selectedEmployeeId);
    } catch (err) {
      setAttendanceError(err.message || "Could not mark absence.");
    } finally {
      setIsWorking(false);
    }
  }

  async function resolveAttendanceAbsence(attendance) {
    const draft = resolveDrafts[attendance.id] || {};

    setIsWorking(true);
    setMessage("");
    setAttendanceError("");

    try {
      await resolveAbsence(attendance.id, draft.decision || "UNPAID_ABSENCE", draft.notes || attendance.notes || "");
      setMessage("Absence resolved.");
      await loadAttendanceAdmin(selectedEmployeeId);
    } catch (err) {
      setAttendanceError(err.message || "Could not resolve absence.");
    } finally {
      setIsWorking(false);
    }
  }

  function updateResolveDraft(id, field, value) {
    setResolveDrafts((current) => ({
      ...current,
      [id]: {
        decision: "UNPAID_ABSENCE",
        notes: "",
        ...(current[id] || {}),
        [field]: value
      }
    }));
  }

  const selectedEmployee = employees.find((employee) => String(employee.id) === String(selectedEmployeeId));

  return (
    <section className="work-area attendance-page">
      <div className="section-toolbar">
        <div>
          <span className="eyebrow">Attendance review</span>
          <h3>Employee Attendance</h3>
        </div>
        <button className="ghost-button with-icon" disabled={isWorking} onClick={() => loadAttendanceAdmin(selectedEmployeeId)} type="button">
          <RefreshCw size={16} />
          Refresh
        </button>
      </div>

      {isLoading && <div className="loading">Loading attendance...</div>}
      {attendanceError && <div className="alert error">{attendanceError}</div>}
      {message && <div className="success-alert">{message}</div>}

      <section className="metric-grid">
        <div className="metric-card"><span>Employees</span><strong>{employees.length}</strong></div>
        <div className="metric-card"><span>Today records</span><strong>{todayRows.length}</strong></div>
        <div className="metric-card"><span>Unresolved absences</span><strong>{unresolvedAbsences.length}</strong></div>
        <div className="metric-card"><span>Selected employee</span><strong>{selectedEmployee?.fullName || "-"}</strong></div>
        <div className="metric-card"><span>Shop</span><strong>{selectedEmployee?.shopName || "Unassigned"}</strong></div>
      </section>

      <section className="panel attendance-admin-controls">
        <Select
          label="Employee"
          value={selectedEmployeeId}
          onChange={setSelectedEmployeeId}
          options={["", ...employees.map((employee) => String(employee.id))]}
          optionLabel={(value) => {
            const employee = employees.find((entry) => String(entry.id) === String(value));
            return employee ? `${employee.fullName} (${employee.shopName || "Unassigned"})` : "Choose employee";
          }}
        />
        <Input label="Absence date" type="date" value={absenceDate} onChange={setAbsenceDate} />
        <button className="ghost-button" disabled={isWorking || !selectedEmployeeId} onClick={markAbsent} type="button">
          Mark Absent
        </button>
        <form className="attendance-range-form" onSubmit={loadEmployeeRange}>
          <input type="date" value={range.start} onChange={(event) => setRange({ ...range, start: event.target.value })} />
          <input type="date" value={range.end} onChange={(event) => setRange({ ...range, end: event.target.value })} />
          <button className="primary-button" disabled={isWorking || !selectedEmployeeId} type="submit">Search</button>
        </form>
      </section>

      <section className="split">
        <section className="panel audit-log-panel">
          <div className="section-toolbar compact">
            <div>
              <span className="eyebrow">Today</span>
              <h3>Clock Activity</h3>
            </div>
          </div>
          <AttendanceTable records={todayRows} emptyText="No one has clocked in today." />
        </section>

        <section className="panel audit-log-panel">
          <div className="section-toolbar compact">
            <div>
              <span className="eyebrow">Exceptions</span>
              <h3>Unresolved Absences</h3>
            </div>
          </div>
          <DataTable
            actions={(attendance) => (
              <div className="attendance-resolve-actions">
                <select
                  value={resolveDrafts[attendance.id]?.decision || "UNPAID_ABSENCE"}
                  onChange={(event) => updateResolveDraft(attendance.id, "decision", event.target.value)}
                >
                  <option value="UNPAID_ABSENCE">Unpaid Absence</option>
                  <option value="PAID_ABSENCE">Paid Absence</option>
                  <option value="EXCUSED">Excused</option>
                  <option value="SICK_DAY">Sick Day</option>
                  <option value="VACATION">Vacation</option>
                </select>
                <input
                  placeholder="Notes"
                  value={resolveDrafts[attendance.id]?.notes || ""}
                  onChange={(event) => updateResolveDraft(attendance.id, "notes", event.target.value)}
                />
                <button className="primary-button" disabled={isWorking} onClick={() => resolveAttendanceAbsence(attendance)} type="button">
                  Resolve
                </button>
              </div>
            )}
            columns={["Employee", "Date", "Status", "Decision", "Notes", ""]}
            emptyText="No unresolved absences."
            rows={unresolvedAbsences.map((attendance) => ({
              key: `absence-${attendance.id}`,
              source: attendance,
              values: [
                attendance.employeeName || "-",
                attendance.workDate || "-",
                attendance.status || "-",
                attendance.absenceDecision || "-",
                attendance.notes || "-"
              ]
            }))}
          />
        </section>
      </section>

      <section className="panel audit-log-panel">
        <div className="section-toolbar compact">
          <div>
            <span className="eyebrow">Employee range</span>
            <h3>{selectedEmployee?.fullName || "Attendance History"}</h3>
          </div>
        </div>
        <AttendanceTable records={employeeRows} emptyText="No attendance records for this employee and range." />
      </section>
    </section>
  );
}

function AttendanceTable({ emptyText, records }) {
  return (
    <DataTable
      columns={["Date", "Employee", "Clock In", "Clock Out", "Hours", "Status", "Decision"]}
      emptyText={emptyText}
      rows={(records || []).map((record) => ({
        key: `attendance-${record.id}`,
        searchText: `${record.employeeName || ""} ${record.status || ""} ${record.absenceDecision || ""}`,
        source: record,
        values: [
          record.workDate || "-",
          record.employeeName || "-",
          attendanceTime(record.clockIn),
          attendanceTime(record.clockOut),
          attendanceHours(record.workedHours),
          record.status || "-",
          record.absenceDecision || "-"
        ]
      }))}
    />
  );
}

function attendanceTime(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleTimeString(undefined, { hour: "numeric", minute: "2-digit" });
}

function attendanceHours(value) {
  if (value === null || value === undefined || value === "") {
    return "0.00";
  }

  return Number(value || 0).toFixed(2);
}

const payrollAdjustmentTypes = [
  "BONUS",
  "REIMBURSEMENT",
  "DEDUCTION",
  "PENALTY",
  "LOAN_DEDUCTION",
  "TAX_DEDUCTION",
  "OTHER"
];

function PayrollPage({ auth, mode, selectedLocationId = "", shopLocations = [] }) {
  const isAdmin = mode === "admin" && isShopManagerRole(auth?.role);
  const isEmployee = mode === "employee" && auth?.role === "EMPLOYEE";
  const locationScopeId = auth?.locationId ? String(auth.locationId) : selectedLocationId || "";
  const emptyPeriodForm = { startDate: "", endDate: "", locationId: locationScopeId, notes: "" };
  const emptyShiftForm = { employeeId: "", shiftDate: "", clockIn: "09:00", clockOut: "17:00", breakMinutes: "30", notes: "" };
  const emptySlotForm = { shiftDate: "", startTime: "09:00", endTime: "17:00", requiredEmployees: "2", notes: "" };
  const emptyLoanForm = { employeeId: "", originalAmount: "", installmentAmount: "", notes: "" };
  const [periods, setPeriods] = useState([]);
  const [records, setRecords] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [workShifts, setWorkShifts] = useState([]);
  const [shiftSlots, setShiftSlots] = useState([]);
  const [loans, setLoans] = useState([]);
  const [employeeDrafts, setEmployeeDrafts] = useState({});
  const [adjustmentDrafts, setAdjustmentDrafts] = useState({});
  const [recordNotes, setRecordNotes] = useState({});
  const [selectedPeriodId, setSelectedPeriodId] = useState("");
  const [periodForm, setPeriodForm] = useState(emptyPeriodForm);
  const [shiftForm, setShiftForm] = useState(emptyShiftForm);
  const [slotForm, setSlotForm] = useState(emptySlotForm);
  const [loanForm, setLoanForm] = useState(emptyLoanForm);
  const [editingPeriodId, setEditingPeriodId] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isWorking, setIsWorking] = useState(false);
  const [message, setMessage] = useState("");
  const [payrollError, setPayrollError] = useState("");
  const periodFormRef = useRef(null);
  const summary = payrollSummary(records);

  useEffect(() => {
    loadPayroll();
  }, [auth?.id, mode, locationScopeId]);

  async function loadPayroll(nextPeriodId = selectedPeriodId) {
    setIsLoading(true);
    setPayrollError("");

    try {
      if (isAdmin) {
        const [periodList, employeeList, shiftList, loanList] = await Promise.all([
          getPayrollPeriods(locationScopeId).catch((err) => {
            throw err;
          }),
          getPayrollEmployees(locationScopeId).catch(() => []),
          getWorkShifts().catch(() => []),
          getPayrollLoans().catch(() => [])
        ]);
        const safePeriods = periodList || [];
        const safeEmployees = employeeList || [];
        const resolvedPeriodId = nextPeriodId || safePeriods[0]?.id || "";
        const selectedPeriod = safePeriods.find((period) => String(period.id) === String(resolvedPeriodId));
        const periodRecords = resolvedPeriodId ? await getPayrollRecordsForPeriod(resolvedPeriodId).catch(() => []) : [];

        setPeriods(safePeriods);
        setEmployees(safeEmployees);
        setWorkShifts(shiftList || []);
        setLoans(loanList || []);
        setShiftSlots(resolvedPeriodId ? await getPayrollShiftSlots(resolvedPeriodId).catch(() => []) : []);
        setEmployeeDrafts(makeEmployeeDrafts(safeEmployees));
        setAdjustmentDrafts(makeAdjustmentDrafts(periodRecords));
        setRecordNotes(makeRecordNotes(periodRecords));
        setSelectedPeriodId(resolvedPeriodId);
        setShiftForm((current) => ({
          ...current,
          employeeId: current.employeeId || safeEmployees[0]?.id || "",
          shiftDate: current.shiftDate || selectedPeriod?.startDate || todayDateKey()
        }));
        setSlotForm((current) => ({
          ...current,
          shiftDate: current.shiftDate || selectedPeriod?.startDate || todayDateKey()
        }));
        setLoanForm((current) => ({
          ...current,
          employeeId: current.employeeId || safeEmployees[0]?.id || ""
        }));
        setPeriodForm((current) => editingPeriodId ? current : ({ ...current, locationId: locationScopeId }));
        setRecords(periodRecords || []);
      } else if (isEmployee && auth?.id) {
        const [employeeRecords, slots] = await Promise.all([
          getPayrollRecordsForEmployee(auth.id).catch((err) => {
            throw err;
          }),
          getPayrollShiftSlots().catch(() => [])
        ]);
        setRecords(employeeRecords || []);
        setShiftSlots(slots || []);
      }
    } catch (err) {
      setPayrollError(payrollErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }

  async function reloadShiftSlots(periodId = selectedPeriodId) {
    setShiftSlots(await getPayrollShiftSlots(periodId || null).catch(() => []));
  }

  async function runPayrollAction(action, successText) {
    setIsWorking(true);
    setMessage("");
    setPayrollError("");

    try {
      const result = await action();
      setMessage(successText(result));
      await loadPayroll(selectedPeriodId);
    } catch (err) {
      setPayrollError(payrollErrorMessage(err));
    } finally {
      setIsWorking(false);
    }
  }

  async function submitPeriod(event) {
    event.preventDefault();
    setIsWorking(true);
    setMessage("");
    setPayrollError("");

    try {
      const wasEditing = Boolean(editingPeriodId);
      const periodPayload = {
        ...periodForm,
        locationId: periodForm.locationId ? Number(periodForm.locationId) : locationScopeId ? Number(locationScopeId) : null
      };
      const saved = wasEditing
        ? await updatePayrollPeriod(editingPeriodId, periodPayload)
        : await createPayrollPeriod(periodPayload);
      const nextPeriodId = saved?.id || selectedPeriodId;

      setEditingPeriodId(null);
      setPeriodForm(emptyPeriodForm);
      setSelectedPeriodId(nextPeriodId);
      setMessage(wasEditing ? "Payroll period updated." : "Payroll period created.");
      await loadPayroll(nextPeriodId);
    } catch (err) {
      setPayrollError(payrollErrorMessage(err));
    } finally {
      setIsWorking(false);
    }
  }

  function editPeriod(period) {
    setEditingPeriodId(period.id);
    setPeriodForm({
      startDate: period.startDate || "",
      endDate: period.endDate || "",
      locationId: period.locationId ? String(period.locationId) : "",
      notes: period.notes || ""
    });
    window.requestAnimationFrame(() => {
      periodFormRef.current?.scrollIntoView?.({ behavior: "smooth", block: "start" });
    });
  }

  async function selectPeriod(periodId) {
    setSelectedPeriodId(periodId);
    setPayrollError("");
    setMessage("");
    setIsWorking(true);

    try {
      const period = periods.find((entry) => String(entry.id) === String(periodId));
      if (period?.startDate) {
        setShiftForm((current) => ({ ...current, shiftDate: period.startDate }));
        setSlotForm((current) => ({ ...current, shiftDate: period.startDate }));
      }
      setShiftSlots(periodId ? await getPayrollShiftSlots(periodId).catch(() => []) : []);
      const periodRecords = periodId ? await getPayrollRecordsForPeriod(periodId) || [] : [];
      setRecords(periodRecords);
      setAdjustmentDrafts(makeAdjustmentDrafts(periodRecords));
      setRecordNotes(makeRecordNotes(periodRecords));
    } catch (err) {
      setPayrollError(payrollErrorMessage(err));
    } finally {
      setIsWorking(false);
    }
  }

  async function generatePeriodPayroll(periodId) {
    setIsWorking(true);
    setMessage("");
    setPayrollError("");
    setSelectedPeriodId(periodId);

    try {
      const generation = await generatePayroll(periodId);
      const createdRecords = Array.isArray(generation) ? generation : generation?.records || [];
      const skippedReasons = Array.isArray(generation) ? [] : generation?.skippedReasons || [];
      const periodRecords = await getPayrollRecordsForPeriod(periodId).catch(() => []);

      setRecords(periodRecords || []);
      setAdjustmentDrafts(makeAdjustmentDrafts(periodRecords || []));
      setRecordNotes(makeRecordNotes(periodRecords || []));
      await loadPayroll(periodId);
      const fallbackMessage = (createdRecords || []).length === 0
        ? existingPayrollMessage(periodRecords)
        : `Generated ${(createdRecords || []).length} payroll records.`;
      setMessage(generation?.message || fallbackMessage);
      if (skippedReasons.length) {
        setPayrollError(skippedReasons.join(" "));
      }
    } catch (err) {
      setPayrollError(payrollErrorMessage(err));
    } finally {
      setIsWorking(false);
    }
  }

  async function saveEmployeeSettings(employeeId) {
    const draft = employeeDrafts[employeeId] || {};
    await runPayrollAction(
      () => updateEmployeePayrollSettings(employeeId, {
        payrollEnabled: Boolean(draft.payrollEnabled),
        hourlyRate: draft.hourlyRate === "" ? null : Number(draft.hourlyRate),
        employmentType: draft.employmentType || null
      }),
      () => "Employee payroll settings saved."
    );
  }

  function updateEmployeeDraft(employeeId, field, value) {
    setEmployeeDrafts((current) => ({
      ...current,
      [employeeId]: {
        ...(current[employeeId] || {}),
        [field]: value
      }
    }));
  }

  function updateAdjustmentDraft(recordId, field, value) {
    setAdjustmentDrafts((current) => ({
      ...current,
      [recordId]: {
        ...(current[recordId] || defaultAdjustmentDraft()),
        [field]: value
      }
    }));
  }

  async function submitAdjustment(recordId, event) {
    event.preventDefault();
    const draft = adjustmentDrafts[recordId] || defaultAdjustmentDraft();
    await runPayrollAction(
      () => addPayrollAdjustment(recordId, {
        type: draft.type,
        amount: Number(draft.amount || 0),
        notes: draft.notes,
        employeeLoanId: draft.employeeLoanId ? Number(draft.employeeLoanId) : null
      }),
      () => "Payroll adjustment added."
    );
  }

  async function removeAdjustment(recordId, adjustmentId) {
    await runPayrollAction(
      () => deletePayrollAdjustment(recordId, adjustmentId),
      () => "Payroll adjustment removed."
    );
  }

  async function saveRecordNotes(recordId) {
    await runPayrollAction(
      () => updatePayrollRecordNotes(recordId, recordNotes[recordId] || ""),
      () => "Payroll notes saved."
    );
  }

  async function submitLoan(event) {
    event.preventDefault();
    await runPayrollAction(
      () => createEmployeeLoan({
        employeeId: Number(loanForm.employeeId),
        originalAmount: Number(loanForm.originalAmount || 0),
        installmentAmount: loanForm.installmentAmount === "" ? 0 : Number(loanForm.installmentAmount || 0),
        notes: loanForm.notes
      }),
      () => "Employee loan created."
    );
    setLoanForm({ ...emptyLoanForm, employeeId: loanForm.employeeId });
  }

  async function cancelLoan(loanId) {
    await runPayrollAction(
      () => cancelEmployeeLoan(loanId),
      () => "Employee loan cancelled."
    );
  }

  async function submitWorkShift(event) {
    event.preventDefault();
    setIsWorking(true);
    setMessage("");
    setPayrollError("");

    try {
      await createWorkShift({
        employeeId: Number(shiftForm.employeeId),
        shiftDate: shiftForm.shiftDate,
        clockIn: `${shiftForm.shiftDate}T${shiftForm.clockIn}`,
        clockOut: `${shiftForm.shiftDate}T${shiftForm.clockOut}`,
        breakMinutes: Number(shiftForm.breakMinutes || 0),
        notes: shiftForm.notes || "Payroll shift"
      });
      setMessage("Work shift added. Generate payroll after all shifts are entered.");
      await loadPayroll(selectedPeriodId);
    } catch (err) {
      setPayrollError(payrollErrorMessage(err));
    } finally {
      setIsWorking(false);
    }
  }

  async function removeWorkShift(id) {
    await runPayrollAction(
      () => deleteWorkShift(id),
      () => "Work shift deleted."
    );
  }

  async function submitShiftSlot(event) {
    event.preventDefault();
    setIsWorking(true);
    setMessage("");
    setPayrollError("");

    try {
      await createPayrollShiftSlot({
        payrollPeriodId: Number(selectedPeriodId),
        shiftDate: slotForm.shiftDate,
        startTime: slotForm.startTime,
        endTime: slotForm.endTime,
        requiredEmployees: Number(slotForm.requiredEmployees || 1),
        notes: slotForm.notes
      });
      setMessage("Shift slot added. Employees can sign up until spots are filled.");
      await reloadShiftSlots(selectedPeriodId);
    } catch (err) {
      setPayrollError(payrollErrorMessage(err));
    } finally {
      setIsWorking(false);
    }
  }

  async function runSlotAction(action, successText) {
    setIsWorking(true);
    setMessage("");
    setPayrollError("");

    try {
      await action();
      setMessage(successText);
      await reloadShiftSlots(isAdmin ? selectedPeriodId : null);
    } catch (err) {
      setPayrollError(payrollErrorMessage(err));
    } finally {
      setIsWorking(false);
    }
  }

  if (!isAdmin && !isEmployee) {
    return <div className="alert error">You do not have permission to access this payroll resource.</div>;
  }

  const selectedPeriod = periods.find((period) => String(period.id) === String(selectedPeriodId));
  const visibleWorkShifts = isAdmin
    ? workShifts.filter((shift) => isShiftInPeriod(shift, selectedPeriod))
    : [];
  const visibleShiftSlots = isAdmin
    ? shiftSlots
    : shiftSlots.filter((slot) => slot.spotsLeft > 0 || slot.signedUpByCurrentUser);

  return (
    <section className="work-area payroll-page">
      <div className="section-toolbar">
        <div>
          <span className="eyebrow">{isAdmin ? "Payroll administration" : "Payroll history"}</span>
          <h3>{isAdmin ? "Payroll" : "My Payroll"}</h3>
        </div>
        {isAdmin && (
          <button className="ghost-button with-icon" disabled={isWorking} onClick={() => loadPayroll(selectedPeriodId)} type="button">
            <RefreshCw size={16} />
            Refresh Payroll
          </button>
        )}
      </div>

      {isLoading && <div className="loading">Loading payroll data...</div>}
      {payrollError && <div className="alert error">{payrollError}</div>}
      {message && <div className="success-alert">{message}</div>}

      {!isLoading && (
        <>
          <section className="metric-grid">
            <div className="metric-card"><span>Total records</span><strong>{summary.totalRecords}</strong></div>
            <div className="metric-card"><span>Pending payroll</span><strong>{summary.pending}</strong></div>
            <div className="metric-card"><span>Approved payroll</span><strong>{summary.approved}</strong></div>
            <div className="metric-card"><span>Paid payroll</span><strong>{summary.paid}</strong></div>
            <div className="metric-card"><span>Cancelled payroll</span><strong>{summary.cancelled}</strong></div>
            <div className="metric-card"><span>Total gross pay</span><strong>{money(summary.grossPay)}</strong></div>
            <div className="metric-card"><span>Total deductions</span><strong>{money(summary.deductions)}</strong></div>
            <div className="metric-card"><span>Total net pay</span><strong>{money(summary.netPay)}</strong></div>
          </section>

          {isAdmin && (
            <>
              <form className="panel form-grid payroll-period-form" onSubmit={submitPeriod} ref={periodFormRef}>
                <div className="settings-header">
                  <span className="brand-mark"><BriefcaseBusiness size={20} /></span>
                  <div>
                    <span className="eyebrow">Payroll periods</span>
                    <h3>{editingPeriodId ? "Update Period" : "Create Period"}</h3>
                  </div>
                </div>
                <Input label="Start date" required type="date" value={periodForm.startDate} onChange={(startDate) => setPeriodForm({ ...periodForm, startDate })} />
                <Input label="End date" required type="date" value={periodForm.endDate} onChange={(endDate) => setPeriodForm({ ...periodForm, endDate })} />
                {shopLocations.length > 0 && (
                  <Select
                    label="Location"
                    value={periodForm.locationId || locationScopeId || ""}
                    onChange={(locationId) => setPeriodForm({ ...periodForm, locationId })}
                    options={["", ...shopLocations.map((location) => String(location.id))]}
                    optionLabel={(value) => shopLocations.find((location) => String(location.id) === String(value))?.name || "All locations"}
                  />
                )}
                <label className="settings-terms">
                  <span>Notes</span>
                  <textarea rows="3" value={periodForm.notes} onChange={(event) => setPeriodForm({ ...periodForm, notes: event.target.value })} />
                </label>
                <div className="toolbar-actions">
                  {editingPeriodId && (
                    <button className="ghost-button" onClick={() => { setEditingPeriodId(null); setPeriodForm(emptyPeriodForm); }} type="button">
                      Cancel Edit
                    </button>
                  )}
                  <button className="primary-button" disabled={isWorking} type="submit">
                    {editingPeriodId ? "Update Period" : "Create Period"}
                  </button>
                </div>
              </form>

              <section className="panel audit-log-panel">
                <div className="section-toolbar">
                  <div>
                    <span className="eyebrow">Pay cycles</span>
                    <h3>Payroll Periods</h3>
                  </div>
                  <label className="payroll-period-picker">
                    <span>Selected records period</span>
                    <select value={selectedPeriodId} onChange={(event) => selectPeriod(event.target.value)}>
                      <option value="">No period selected</option>
                      {periods.map((period) => (
                        <option key={period.id} value={period.id}>
                          {period.startDate} to {period.endDate}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>
                <DataTable
                  actions={(period) => (
                    <div className="table-actions compact">
                      <button className="ghost-button" onClick={() => editPeriod(period)} type="button">Edit</button>
                      <button className="primary-button" disabled={isWorking} onClick={() => generatePeriodPayroll(period.id)} type="button">Generate</button>
                      <button className="ghost-button" onClick={() => selectPeriod(period.id)} type="button">Records</button>
                      <button className="danger-button" disabled={isWorking} onClick={() => runPayrollAction(() => deletePayrollPeriod(period.id), () => "Payroll period deleted.")} type="button">Delete</button>
                    </div>
                  )}
                  columns={["Start", "End", "Location", "Status", "Notes", "Created", ""]}
                  emptyText="No payroll periods yet."
                  rows={periods.map((period) => ({
                    key: `payroll-period-${period.id}`,
                    source: period,
                    values: [
                      period.startDate || "-",
                      period.endDate || "-",
                      period.locationName || "All locations",
                      period.status || "-",
                      period.notes || "-",
                      dateTime(period.createdAt)
                    ]
                  }))}
                />
              </section>

              <section className="panel audit-log-panel payroll-schedule-panel">
                <div className="section-toolbar">
                  <div>
                    <span className="eyebrow">Availability schedule</span>
                    <h3>Shift Signups</h3>
                  </div>
                  <span className="audit-count">First come, first serve</span>
                </div>

                <form className="payroll-slot-form" onSubmit={submitShiftSlot}>
                  <Input label="Shift date" required type="date" value={slotForm.shiftDate} onChange={(shiftDate) => setSlotForm({ ...slotForm, shiftDate })} />
                  <Input label="Start time" required type="time" value={slotForm.startTime} onChange={(startTime) => setSlotForm({ ...slotForm, startTime })} />
                  <Input label="End time" required type="time" value={slotForm.endTime} onChange={(endTime) => setSlotForm({ ...slotForm, endTime })} />
                  <Input label="People needed" min="1" required type="number" value={slotForm.requiredEmployees} onChange={(requiredEmployees) => setSlotForm({ ...slotForm, requiredEmployees })} />
                  <Input label="Notes" value={slotForm.notes} onChange={(notes) => setSlotForm({ ...slotForm, notes })} placeholder="Front desk, bay 1, closing..." />
                  <button className="primary-button" disabled={isWorking || !selectedPeriodId} type="submit">Add Slot</button>
                </form>

                <ShiftSlotBoard
                  employees={employees}
                  isAdmin={isAdmin}
                  isWorking={isWorking}
                  onAssign={(slotId, employeeId) => runSlotAction(() => assignEmployeeToPayrollShiftSlot(slotId, employeeId), "Employee assigned to shift.")}
                  onCancel={(slotId) => runSlotAction(() => cancelPayrollShiftSignup(slotId), "Shift signup cancelled.")}
                  onDelete={(slotId) => runSlotAction(() => deletePayrollShiftSlot(slotId), "Shift slot deleted.")}
                  onRemoveSignup={(slotId, signupId) => runSlotAction(() => removePayrollShiftSignup(slotId, signupId), "Employee removed from shift.")}
                  onSignup={(slotId) => runSlotAction(() => signupForPayrollShiftSlot(slotId), "You are signed up for this shift.")}
                  slots={visibleShiftSlots}
                />
              </section>

              <section className="panel audit-log-panel payroll-shifts-panel">
                <div className="section-toolbar">
                  <div>
                    <span className="eyebrow">Hours source</span>
                    <h3>Work Shifts</h3>
                  </div>
                  <span className="audit-count">
                    {selectedPeriod ? `${selectedPeriod.startDate} to ${selectedPeriod.endDate}` : "Select a period"}
                  </span>
                </div>

                <form className="payroll-shift-form" onSubmit={submitWorkShift}>
                  <label>
                    <span>Employee</span>
                    <select required value={shiftForm.employeeId} onChange={(event) => setShiftForm({ ...shiftForm, employeeId: event.target.value })}>
                      <option value="">Choose employee</option>
                      {employees.map((employee) => (
                        <option key={employee.id} value={employee.id}>{employee.fullName}</option>
                      ))}
                    </select>
                  </label>
                  <Input label="Shift date" required type="date" value={shiftForm.shiftDate} onChange={(shiftDate) => setShiftForm({ ...shiftForm, shiftDate })} />
                  <Input label="Clock in" required type="time" value={shiftForm.clockIn} onChange={(clockIn) => setShiftForm({ ...shiftForm, clockIn })} />
                  <Input label="Clock out" required type="time" value={shiftForm.clockOut} onChange={(clockOut) => setShiftForm({ ...shiftForm, clockOut })} />
                  <Input label="Break minutes" min="0" type="number" value={shiftForm.breakMinutes} onChange={(breakMinutes) => setShiftForm({ ...shiftForm, breakMinutes })} />
                  <Input label="Notes" value={shiftForm.notes} onChange={(notes) => setShiftForm({ ...shiftForm, notes })} placeholder="Payroll shift" />
                  <button className="primary-button" disabled={isWorking} type="submit">Add Shift</button>
                </form>

                <DataTable
                  actions={(shift) => (
                    <button className="danger-button" disabled={isWorking} onClick={() => removeWorkShift(shift.id)} type="button">
                      Delete
                    </button>
                  )}
                  columns={["Employee", "Date", "Clock In", "Clock Out", "Break", "Hours", "Notes", ""]}
                  emptyText="No work shifts in this payroll period yet."
                  rows={visibleWorkShifts.map((shift) => ({
                    key: `work-shift-${shift.id}`,
                    source: shift,
                    values: [
                      shift.employeeName || shift.employee?.fullName || "-",
                      shift.shiftDate || "-",
                      compactTime(shift.clockIn),
                      compactTime(shift.clockOut),
                      `${shift.breakMinutes ?? 0} min`,
                      numberCell(shift.workedHours),
                      shift.notes || "-"
                    ]
                  }))}
                />
              </section>
            </>
          )}

          {isEmployee && (
            <section className="panel audit-log-panel payroll-schedule-panel">
              <div className="section-toolbar">
                <div>
                  <span className="eyebrow">Available shifts</span>
                  <h3>Shift Signups</h3>
                </div>
                <span className="audit-count">First come, first serve</span>
              </div>
              <ShiftSlotBoard
                employees={[]}
                isAdmin={false}
                isWorking={isWorking}
                onCancel={(slotId) => runSlotAction(() => cancelPayrollShiftSignup(slotId), "Shift signup cancelled.")}
                onSignup={(slotId) => runSlotAction(() => signupForPayrollShiftSlot(slotId), "You are signed up for this shift.")}
                slots={visibleShiftSlots}
              />
            </section>
          )}

          <section className="panel audit-log-panel">
            <div className="section-toolbar">
              <div>
                <span className="eyebrow">{isAdmin ? "Selected period" : "Employee payroll"}</span>
                <h3>{isAdmin ? "Payroll Records" : "My Payroll Records"}</h3>
              </div>
            </div>
            <DataTable
              actions={isAdmin ? (record) => (
                <div className="table-actions compact">
                  <button className="ghost-button" disabled={isWorking || record.status !== "PENDING"} onClick={() => runPayrollAction(() => approvePayrollRecord(record.id), () => "Payroll record approved.")} type="button">Approve</button>
                  <button className="primary-button" disabled={isWorking || record.status === "PAID" || record.status === "CANCELLED"} onClick={() => runPayrollAction(() => payPayrollRecord(record.id), () => "Payroll record marked paid.")} type="button">Mark Paid</button>
                  <button className="danger-button" disabled={isWorking || record.status === "PAID"} onClick={() => runPayrollAction(() => cancelPayrollRecord(record.id), () => "Payroll record cancelled.")} type="button">Cancel</button>
                </div>
              ) : null}
              columns={isAdmin
                ? ["Employee", "Email", "Regular", "Rate", "Gross", "Deductions", "Net", "Status", "Accounting", "Paid At", ""]
                : ["Period", "Regular", "Gross", "Deductions", "Net", "Status", "Paid At"]}
              emptyText={isAdmin ? "No payroll records for this period." : "No payroll records found yet."}
              rows={records.map((record) => ({
                key: `payroll-record-${record.id}`,
                source: record,
                values: isAdmin ? [
                  record.employeeName || record.employee?.fullName || "-",
                  record.employeeEmail || record.employee?.email || "-",
                  numberCell(record.regularHours),
                  money(record.hourlyRate),
                  money(record.grossPay),
                  money(record.totalDeductions),
                  money(record.netPay),
                  record.status || "-",
                  record.accountingSynced ? "Synced" : "-",
                  dateTime(record.paidAt)
                ] : [
                  `${record.periodStartDate || record.payrollPeriod?.startDate || "-"} to ${record.periodEndDate || record.payrollPeriod?.endDate || "-"}`,
                  numberCell(record.regularHours),
                  money(record.grossPay),
                  money(record.totalDeductions),
                  money(record.netPay),
                  record.status || "-",
                  dateTime(record.paidAt)
                ]
              }))}
            />
          </section>

          {isAdmin && (
            <section className="panel audit-log-panel payroll-loans-panel">
              <div className="section-toolbar">
                <div>
                  <span className="eyebrow">Advances</span>
                  <h3>Employee Loans</h3>
                </div>
              </div>
              <form className="payroll-loan-form" onSubmit={submitLoan}>
                <label>
                  <span>Employee</span>
                  <select required value={loanForm.employeeId} onChange={(event) => setLoanForm({ ...loanForm, employeeId: event.target.value })}>
                    <option value="">Choose employee</option>
                    {employees.map((employee) => (
                      <option key={employee.id} value={employee.id}>{employee.fullName}</option>
                    ))}
                  </select>
                </label>
                <Input label="Amount" min="0.01" required step="0.01" type="number" value={loanForm.originalAmount} onChange={(originalAmount) => setLoanForm({ ...loanForm, originalAmount })} />
                <Input label="Installment" min="0" step="0.01" type="number" value={loanForm.installmentAmount} onChange={(installmentAmount) => setLoanForm({ ...loanForm, installmentAmount })} />
                <Input label="Notes" value={loanForm.notes} onChange={(notes) => setLoanForm({ ...loanForm, notes })} />
                <button className="primary-button" disabled={isWorking} type="submit">Create Loan</button>
              </form>
              <DataTable
                actions={(loan) => (
                  <button className="danger-button" disabled={isWorking || loan.status !== "ACTIVE"} onClick={() => cancelLoan(loan.id)} type="button">
                    Cancel
                  </button>
                )}
                columns={["Employee", "Original", "Remaining", "Installment", "Status", "Notes", ""]}
                emptyText="No employee loans yet."
                rows={loans.map((loan) => ({
                  key: `employee-loan-${loan.id}`,
                  source: loan,
                  values: [
                    loan.employeeName || "-",
                    money(loan.originalAmount),
                    money(loan.remainingBalance),
                    money(loan.installmentAmount),
                    loan.status || "-",
                    loan.notes || "-"
                  ]
                }))}
              />
            </section>
          )}

          <section className="panel audit-log-panel payroll-adjustments-panel">
            <div className="section-toolbar">
              <div>
                <span className="eyebrow">Manual payroll</span>
                <h3>{isAdmin ? "Adjustments" : "Pay Breakdown"}</h3>
              </div>
            </div>
            <div className="payroll-record-cards">
              {records.length === 0 ? (
                <div className="empty-state payroll-slot-empty">
                  <span className="brand-mark"><CircleDollarSign size={18} /></span>
                  <strong>No payroll records.</strong>
                </div>
              ) : (
                records.map((record) => {
                  const editable = isAdmin && record.status === "PENDING";
                  const draft = adjustmentDrafts[record.id] || defaultAdjustmentDraft();
                  const loanOptions = activeLoansForRecord(loans, record);

                  return (
                    <article className="payroll-record-card" key={`payroll-adjustment-card-${record.id}`}>
                      <div className="payroll-record-card-head">
                        <div>
                          <strong>{record.employeeName || "Payroll record"}</strong>
                          <span>{record.periodStartDate || "-"} to {record.periodEndDate || "-"}</span>
                        </div>
                        <StatusBadge value={record.status || "PENDING"} />
                      </div>
                      <div className="payroll-breakdown-grid">
                        <span>Gross <strong>{money(record.grossPay)}</strong></span>
                        <span>Bonus <strong>{money(record.bonusAmount)}</strong></span>
                        <span>Reimbursement <strong>{money(record.reimbursementAmount)}</strong></span>
                        <span>Deductions <strong>{money(record.totalDeductions)}</strong></span>
                        <span>Net <strong>{money(record.netPay)}</strong></span>
                        <span>Accounting <strong>{record.accountingSynced ? "Synced" : "-"}</strong></span>
                      </div>

                      <div className="payroll-adjustment-list">
                        {(record.adjustments || []).length === 0 ? (
                          <span className="empty-note">No adjustments.</span>
                        ) : (
                          record.adjustments.map((adjustment) => (
                            <div className="payroll-adjustment-line" key={adjustment.id}>
                              <span>{adjustmentTypeLabel(adjustment.type)}</span>
                              <strong>{money(adjustment.amount)}</strong>
                              <em>{adjustment.notes || "-"}</em>
                              {editable && (
                                <button className="danger-button" disabled={isWorking} onClick={() => removeAdjustment(record.id, adjustment.id)} type="button">Remove</button>
                              )}
                            </div>
                          ))
                        )}
                      </div>

                      {isAdmin && (
                        <div className="payroll-record-notes">
                          <label>
                            <span>Notes</span>
                            <textarea disabled={!editable} rows="2" value={recordNotes[record.id] ?? ""} onChange={(event) => setRecordNotes((current) => ({ ...current, [record.id]: event.target.value }))} />
                          </label>
                          <button className="ghost-button" disabled={isWorking || !editable} onClick={() => saveRecordNotes(record.id)} type="button">Save Notes</button>
                        </div>
                      )}

                      {editable && (
                        <form className="payroll-adjustment-form" onSubmit={(event) => submitAdjustment(record.id, event)}>
                          <label>
                            <span>Type</span>
                            <select value={draft.type} onChange={(event) => {
                              const nextType = event.target.value;
                              updateAdjustmentDraft(record.id, "type", nextType);
                              if (nextType !== "LOAN_DEDUCTION") {
                                updateAdjustmentDraft(record.id, "employeeLoanId", "");
                              }
                            }}>
                              {payrollAdjustmentTypes.map((type) => (
                                <option key={type} value={type}>{adjustmentTypeLabel(type)}</option>
                              ))}
                            </select>
                          </label>
                          <Input label="Amount" min="0.01" required step="0.01" type="number" value={draft.amount} onChange={(amount) => updateAdjustmentDraft(record.id, "amount", amount)} />
                          <label>
                            <span>Loan</span>
                            <select disabled={draft.type !== "LOAN_DEDUCTION"} value={draft.employeeLoanId || ""} onChange={(event) => updateAdjustmentDraft(record.id, "employeeLoanId", event.target.value)}>
                              <option value="">No linked loan</option>
                              {loanOptions.map((loan) => (
                                <option key={loan.id} value={loan.id}>
                                  #{loan.id} / {money(loan.remainingBalance)} left
                                </option>
                              ))}
                            </select>
                          </label>
                          <Input label="Notes" value={draft.notes} onChange={(notes) => updateAdjustmentDraft(record.id, "notes", notes)} />
                          <button className="primary-button" disabled={isWorking} type="submit">Add</button>
                        </form>
                      )}
                    </article>
                  );
                })
              )}
            </div>
          </section>

          {isAdmin && (
            <section className="panel audit-log-panel">
              <div className="section-toolbar">
                <div>
                  <span className="eyebrow">Employee eligibility</span>
                  <h3>Employee Payroll Settings</h3>
                </div>
              </div>
              <DataTable
                actions={(employee) => (
                  <button className="primary-button" disabled={isWorking} onClick={() => saveEmployeeSettings(employee.id)} type="button">
                    Save
                  </button>
                )}
                columns={["Employee", "Email", "Payroll Enabled", "Hourly Rate", "Employment Type", ""]}
                emptyText="No employees found."
                rows={employees.map((employee) => {
                  const draft = employeeDrafts[employee.id] || {};
                  return {
                    key: `employee-payroll-${employee.id}`,
                    source: employee,
                    values: [
                      employee.fullName || "-",
                      employee.email || "-",
                      {
                        value: (
                          <input
                            checked={Boolean(draft.payrollEnabled)}
                            onChange={(event) => updateEmployeeDraft(employee.id, "payrollEnabled", event.target.checked)}
                            type="checkbox"
                          />
                        ),
                        text: draft.payrollEnabled ? "enabled" : "disabled"
                      },
                      {
                        value: (
                          <input
                            min="0"
                            onChange={(event) => updateEmployeeDraft(employee.id, "hourlyRate", event.target.value)}
                            step="0.01"
                            type="number"
                            value={draft.hourlyRate ?? ""}
                          />
                        ),
                        text: draft.hourlyRate
                      },
                      {
                        value: (
                          <select value={draft.employmentType || ""} onChange={(event) => updateEmployeeDraft(employee.id, "employmentType", event.target.value)}>
                            <option value="">Not set</option>
                            <option value="FULL_TIME">Full time</option>
                            <option value="PART_TIME">Part time</option>
                            <option value="CONTRACT">Contract</option>
                            <option value="SEASONAL">Seasonal</option>
                          </select>
                        ),
                        text: draft.employmentType || "not set"
                      }
                    ]
                  };
                })}
              />
            </section>
          )}
        </>
      )}
    </section>
  );
}

function payrollSummary(records) {
  return (records || []).reduce((summary, record) => {
    const status = String(record.status || "PENDING").toUpperCase();
    summary.totalRecords += 1;
    summary.grossPay += Number(record.grossPay || 0);
    summary.deductions += Number(record.totalDeductions || 0);
    summary.netPay += Number(record.netPay || 0);
    if (status === "PENDING") summary.pending += 1;
    if (status === "APPROVED") summary.approved += 1;
    if (status === "PAID") summary.paid += 1;
    if (status === "CANCELLED") summary.cancelled += 1;
    return summary;
  }, { totalRecords: 0, pending: 0, approved: 0, paid: 0, cancelled: 0, grossPay: 0, deductions: 0, netPay: 0 });
}

function defaultAdjustmentDraft() {
  return { type: "BONUS", amount: "", notes: "", employeeLoanId: "" };
}

function makeAdjustmentDrafts(records) {
  return (records || []).reduce((drafts, record) => {
    drafts[record.id] = defaultAdjustmentDraft();
    return drafts;
  }, {});
}

function makeRecordNotes(records) {
  return (records || []).reduce((notes, record) => {
    notes[record.id] = record.notes || "";
    return notes;
  }, {});
}

function adjustmentTypeLabel(type) {
  return String(type || "")
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function activeLoansForRecord(loans, record) {
  return (loans || []).filter((loan) =>
    loan.status === "ACTIVE"
    && String(loan.employeeId) === String(record.employeeId)
    && Number(loan.remainingBalance || 0) > 0
  );
}

function ShiftSlotBoard({ employees, isAdmin, isWorking, onAssign, onCancel, onDelete, onRemoveSignup, onSignup, slots }) {
  const [selectedEmployees, setSelectedEmployees] = useState({});
  const groups = groupShiftSlotsByDate(slots);

  function updateSelectedEmployee(slotId, employeeId) {
    setSelectedEmployees((current) => ({ ...current, [slotId]: employeeId }));
  }

  if (groups.length === 0) {
    return (
      <div className="empty-state payroll-slot-empty">
        <span className="brand-mark"><CalendarDays size={18} /></span>
        <strong>No shift slots yet.</strong>
        <p>{isAdmin ? "Add slots for the selected payroll period." : "No available shifts are posted right now."}</p>
      </div>
    );
  }

  return (
    <div className="payroll-slot-calendar">
      {groups.map(({ date, slots: daySlots }) => (
        <section className="payroll-slot-day" key={date}>
          <div className="payroll-slot-day-head">
            <span>{compactDate(date)}</span>
            <strong>{daySlots.length} slot{daySlots.length === 1 ? "" : "s"}</strong>
          </div>
          <div className="payroll-slot-list">
            {daySlots.map((slot) => {
              const isFull = Number(slot.spotsLeft || 0) <= 0;
              const selectedEmployeeId = selectedEmployees[slot.id] || employees[0]?.id || "";

              return (
                <article className={`payroll-slot-card ${isFull ? "full" : ""}`} key={slot.id}>
                  <div className="payroll-slot-card-head">
                    <strong>{slot.startTime} - {slot.endTime}</strong>
                    <StatusBadge value={isFull ? "FULL" : "OPEN"} />
                  </div>
                  <div className="payroll-slot-capacity">
                    <span>{slot.filledCount}/{slot.requiredEmployees} filled</span>
                    <strong>{slot.spotsLeft} open</strong>
                  </div>
                  {slot.notes && <p>{slot.notes}</p>}
                  <div className="payroll-slot-signups">
                    {(slot.signups || []).length === 0 ? (
                      <span className="empty-note">No one signed up yet.</span>
                    ) : (
                      (slot.signups || []).map((signup) => (
                        <span className="payroll-signup-pill" key={signup.id}>
                          {signup.fullName}
                          {isAdmin && (
                            <button disabled={isWorking} onClick={() => onRemoveSignup(slot.id, signup.id)} type="button">x</button>
                          )}
                        </span>
                      ))
                    )}
                  </div>
                  {isAdmin ? (
                    <div className="payroll-slot-actions">
                      <select value={selectedEmployeeId} onChange={(event) => updateSelectedEmployee(slot.id, event.target.value)}>
                        {employees.map((employee) => (
                          <option key={employee.id} value={employee.id}>{employee.fullName}</option>
                        ))}
                      </select>
                      <button className="ghost-button" disabled={isWorking || isFull || !selectedEmployeeId} onClick={() => onAssign(slot.id, selectedEmployeeId)} type="button">
                        Assign
                      </button>
                      <button className="danger-button" disabled={isWorking} onClick={() => onDelete(slot.id)} type="button">
                        Delete
                      </button>
                    </div>
                  ) : (
                    <div className="payroll-slot-actions">
                      {slot.signedUpByCurrentUser ? (
                        <button className="ghost-button" disabled={isWorking} onClick={() => onCancel(slot.id)} type="button">
                          Cancel Signup
                        </button>
                      ) : (
                        <button className="primary-button" disabled={isWorking || isFull} onClick={() => onSignup(slot.id)} type="button">
                          Sign Up
                        </button>
                      )}
                    </div>
                  )}
                </article>
              );
            })}
          </div>
        </section>
      ))}
    </div>
  );
}

function groupShiftSlotsByDate(slots) {
  const groups = (slots || []).reduce((result, slot) => {
    const date = slot.shiftDate || "Unscheduled";
    result[date] = [...(result[date] || []), slot];
    return result;
  }, {});

  return Object.entries(groups)
    .sort(([first], [second]) => first.localeCompare(second))
    .map(([date, daySlots]) => ({
      date,
      slots: daySlots.sort((first, second) => String(first.startTime).localeCompare(String(second.startTime)))
    }));
}

function makeEmployeeDrafts(employees) {
  return (employees || []).reduce((drafts, employee) => {
    drafts[employee.id] = {
      payrollEnabled: Boolean(employee.payrollEnabled),
      hourlyRate: employee.hourlyRate ?? "",
      employmentType: employee.employmentType || ""
    };
    return drafts;
  }, {});
}

function payrollErrorMessage(err) {
  const message = err?.message || "Payroll request failed.";
  if (message.includes("403")) {
    return "You do not have permission to access this payroll resource.";
  }

  return message;
}

function existingPayrollMessage(records) {
  if ((records || []).length > 0) {
    return "Payroll records already exist for this period. Select Records to review them.";
  }

  return "No eligible payroll records were generated. Make sure employees have payroll enabled, hourly rates set, and shifts in this period.";
}

function isShiftInPeriod(shift, period) {
  if (!period?.startDate || !period?.endDate) {
    return true;
  }

  return shift.shiftDate >= period.startDate && shift.shiftDate <= period.endDate;
}

function compactTime(value) {
  if (!value) {
    return "-";
  }

  return String(value).split("T")[1]?.slice(0, 5) || value;
}

function numberCell(value) {
  return Number(value || 0).toLocaleString(undefined, {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0
  });
}

function AuditLogsPage({ logs }) {
  const normalizedLogs = logs.map(normalizeAuditLog);
  const actionCounts = normalizedLogs.reduce((counts, log) => {
    const action = log.action || "UNKNOWN";
    counts[action] = (counts[action] || 0) + 1;
    return counts;
  }, {});

  return (
    <section className="work-area">
      <div className="audit-summary">
        <div className="panel audit-summary-card">
          <span className="metric-icon small"><ClipboardList size={17} /></span>
          <span>Total Events</span>
          <strong>{normalizedLogs.length}</strong>
        </div>
        {Object.entries(actionCounts).slice(0, 3).map(([action, count]) => (
          <div className="panel audit-summary-card" key={action}>
            <span className="metric-icon small"><ShieldCheck size={17} /></span>
            <span>{action}</span>
            <strong>{count}</strong>
          </div>
        ))}
      </div>

      <section className="panel audit-log-panel">
        <div className="section-toolbar">
          <div>
            <span className="eyebrow">Admin traceability</span>
            <h3>Audit Logs</h3>
          </div>
          <span className="audit-count">{normalizedLogs.length} latest records</span>
        </div>

        <DataTable
          columns={["Action", "Entity", "Entity ID", "Performed By", "Message", "Timestamp"]}
          emptyText="No audit logs yet."
          rows={normalizedLogs.map((log) => ({
            key: `audit-${log.id || log.createdAt}`,
            values: [
              log.action,
              log.entityType,
              log.entityId,
              log.performedBy || "system",
              log.label || log.message || "-",
              dateTime(log.createdAt)
            ],
            source: log
          }))}
        />
      </section>
    </section>
  );
}

function CustomersPage({ customers, onSendNotice }) {
  const [noticeDrafts, setNoticeDrafts] = useState({});
  const [noticeMessage, setNoticeMessage] = useState("");
  const [selectedCustomerId, setSelectedCustomerId] = useState(null);
  const totalRevenue = customers.reduce((sum, customer) => sum + Number(customer.totalSpent || 0), 0);
  const outstandingTotal = customers.reduce((sum, customer) => sum + Number(customer.outstandingBalance || 0), 0);
  const urgentCustomers = customers.filter((customer) => customer.hasOverdueBalance || customer.hasBalanceDueSoon).length;
  const selectedCustomer = customers.find((customer) => Number(customer.id) === Number(selectedCustomerId)) || customers[0];

  useEffect(() => {
    if (noticeMessage) {
      scrollPageToTop();
    }
  }, [noticeMessage]);

  useEffect(() => {
    setNoticeMessage("");
  }, [selectedCustomerId]);

  function updateDraft(customerId, field, value) {
    setNoticeDrafts((current) => ({
      ...current,
      [customerId]: {
        title: "Account notice",
        type: "NOTICE",
        message: "",
        ...(current[customerId] || {}),
        [field]: value
      }
    }));
  }

  async function sendNotice(customer) {
    const draft = noticeDrafts[customer.id] || {};
    setNoticeMessage("");

    if (!draft.message?.trim()) {
      setNoticeMessage("Write a notice message before sending.");
      return;
    }

    await onSendNotice(customer.id, draft);
    setNoticeDrafts((current) => ({ ...current, [customer.id]: { title: "Account notice", type: "NOTICE", message: "" } }));
    setNoticeMessage(`Notice sent to ${customer.fullName}.`);
  }

  function suggestPaymentNotice(customer) {
    const nextInvoice = customer.unpaidInvoices?.[0];
    const dueText = customer.nextPaymentDueDate ? ` by ${customer.nextPaymentDueDate}` : "";
    updateDraft(customer.id, "title", customer.hasOverdueBalance ? "Payment overdue" : "Payment due soon");
    updateDraft(customer.id, "type", "PAYMENT_DUE");
    updateDraft(customer.id, "message", `Invoice ${nextInvoice ? invoiceNumber(nextInvoice) : `#${customer.nextUnpaidInvoiceId || ""}`} has an outstanding balance of ${money(nextInvoice?.balanceDue ?? customer.outstandingBalance)}${dueText}. Please pay it through your TireTrack account.`);
  }

  function suggestInvoiceNotice(customer, invoice) {
    const status = invoiceStatusKey(invoice.status);
    const dueText = invoice.dueDate ? ` due by ${invoice.dueDate}` : "";
    updateDraft(customer.id, "title", status === "PARTIALLY_PAID" ? "Partial payment balance" : "Invoice payment due");
    updateDraft(customer.id, "type", "PAYMENT_DUE");
    updateDraft(customer.id, "message", `Invoice ${invoiceNumber(invoice)} is ${statusLabel(status).toLowerCase()} with ${money(invoice.balanceDue ?? invoice.total)} outstanding${dueText}. Please pay it through your TireTrack account.`);
  }

  function suggestAppointmentNotice(customer) {
    updateDraft(customer.id, "title", "Appointment reminder");
    updateDraft(customer.id, "type", "APPOINTMENT");
    updateDraft(customer.id, "message", `Reminder: you have an appointment scheduled for ${dateTime(customer.nextAppointmentDate)}${customer.nextAppointmentVehicle ? ` for ${customer.nextAppointmentVehicle}` : ""}.`);
  }

  return (
    <section className="work-area">
      <section className="metric-grid">
        <div className="metric-card"><span>Customers</span><strong>{customers.length}</strong></div>
        <div className="metric-card"><span>Total customer revenue</span><strong>{money(totalRevenue)}</strong></div>
        <div className="metric-card"><span>Outstanding balance</span><strong>{money(outstandingTotal)}</strong></div>
        <div className="metric-card"><span>Payment alerts</span><strong>{urgentCustomers}</strong></div>
        <div className="metric-card"><span>Vehicles saved</span><strong>{customers.reduce((sum, customer) => sum + Number(customer.vehicleCount || 0), 0)}</strong></div>
      </section>
      <section className="customer-crm-layout">
        <section className="panel audit-log-panel">
          <div className="section-toolbar">
            <div>
              <span className="eyebrow">Customer CRM</span>
              <h3>Customer Activity</h3>
            </div>
            <span className="audit-count">{customers.length} accounts</span>
          </div>

          <DataTable
            actions={(customer) => (
              <button className="ghost-button" onClick={() => setSelectedCustomerId(customer.id)} type="button">
                Open
              </button>
            )}
            columns={["Status", "Customer", "Phone", "Outstanding", "Due", "Next Booking", ""]}
            emptyText="No customer accounts yet."
            rows={customers.map((customer) => ({
              key: `customer-${customer.id}`,
              searchText: customer.email,
              values: [
                { value: <CustomerAlertPill customer={customer} />, text: customerAlertLabel(customer) },
                customer.fullName,
                customer.phone,
                {
                  value: money(customer.outstandingBalance),
                  text: money(customer.outstandingBalance),
                  className: customerAlertClass(customer)
                },
                {
                  value: customer.nextPaymentDueDate || "-",
                  text: customer.nextPaymentDueDate || "-",
                  className: customerAlertClass(customer)
                },
                customer.nextAppointmentDate ? dateTime(customer.nextAppointmentDate) : "-"
              ],
              source: customer
            }))}
          />
        </section>

        <aside className="customer-detail-panel panel">
          {selectedCustomer ? (
            <CustomerDetail
              customer={selectedCustomer}
              noticeDrafts={noticeDrafts}
              noticeMessage={noticeMessage}
              onSendNotice={sendNotice}
              onSuggestAppointment={suggestAppointmentNotice}
              onSuggestInvoice={suggestInvoiceNotice}
              onSuggestPayment={suggestPaymentNotice}
              onUpdateDraft={updateDraft}
            />
          ) : (
            <p className="empty-note">Select a customer to view details.</p>
          )}
        </aside>
      </section>
    </section>
  );
}

function CustomerDetail({ customer, noticeDrafts, noticeMessage, onSendNotice, onSuggestAppointment, onSuggestInvoice, onSuggestPayment, onUpdateDraft }) {
  const draft = noticeDrafts[customer.id] || { title: "Account notice", type: "NOTICE", message: "" };
  const unpaidInvoices = [...(customer.unpaidInvoices || [])].sort((first, second) => {
    const firstPartial = invoiceStatusKey(first.status) === "PARTIALLY_PAID";
    const secondPartial = invoiceStatusKey(second.status) === "PARTIALLY_PAID";

    if (firstPartial !== secondPartial) {
      return firstPartial ? -1 : 1;
    }

    return String(first.dueDate || "9999-12-31").localeCompare(String(second.dueDate || "9999-12-31"));
  });

  return (
    <>
      <div className="customer-detail-header">
        <div>
          <span className="eyebrow">Selected customer</span>
          <h3>{customer.fullName}</h3>
          <p>{customer.email} · {customer.phone}</p>
        </div>
        <CustomerAlertPill customer={customer} />
      </div>
      <div className="customer-detail-stats">
        <div><span>Outstanding</span><strong>{money(customer.outstandingBalance)}</strong></div>
        <div><span>Payment due</span><strong>{customer.nextPaymentDueDate || "-"}</strong></div>
        <div><span>Next booking</span><strong>{customer.nextAppointmentDate ? dateTime(customer.nextAppointmentDate) : "-"}</strong></div>
        <div><span>Total paid</span><strong>{money(customer.totalSpent)}</strong></div>
      </div>
      {unpaidInvoices.length > 0 && (
        <div className="customer-unpaid-invoices">
          <div className="section-toolbar compact">
            <div>
              <span className="eyebrow">Payment follow-up</span>
              <h3>Unpaid Invoices</h3>
            </div>
            <span className="audit-count">{unpaidInvoices.length} open</span>
          </div>
          {unpaidInvoices.map((invoice) => (
            <div className="customer-unpaid-invoice" key={invoice.id}>
              <div>
                <strong>{invoiceNumber(invoice)}</strong>
                <small>{statusLabel(invoiceStatusKey(invoice.status))} - {money(invoice.balanceDue ?? invoice.total)} due - Due {invoice.dueDate || "-"}</small>
                <small>{invoice.vehicle || "No vehicle"}</small>
              </div>
              <button className={invoiceStatusKey(invoice.status) === "PARTIALLY_PAID" ? "primary-button" : "ghost-button"} onClick={() => onSuggestInvoice(customer, invoice)} type="button">
                Notice
              </button>
            </div>
          ))}
        </div>
      )}
      <div className="customer-notice-composer">
        <input value={draft.title} onChange={(event) => onUpdateDraft(customer.id, "title", event.target.value)} placeholder="Notice title" />
        <select value={draft.type} onChange={(event) => onUpdateDraft(customer.id, "type", event.target.value)}>
          <option value="NOTICE">Notice</option>
          <option value="PAYMENT_DUE">Payment Due</option>
          <option value="APPOINTMENT">Appointment</option>
        </select>
        <textarea value={draft.message} onChange={(event) => onUpdateDraft(customer.id, "message", event.target.value)} placeholder="Message" rows="4" />
        <div className="toolbar-actions">
          {Number(customer.outstandingBalance || 0) > 0 && (
            <button className={customer.hasOverdueBalance ? "danger-button" : "ghost-button"} onClick={() => onSuggestPayment(customer)} type="button">
              Payment Notice
            </button>
          )}
          {customer.hasUpcomingAppointment && (
            <button className="ghost-button" onClick={() => onSuggestAppointment(customer)} type="button">
              Reminder
            </button>
          )}
          <button className="primary-button" onClick={() => onSendNotice(customer)} type="button">
            Send Notice
          </button>
        </div>
      </div>
      {noticeMessage && <div className={noticeMessage.includes("sent") ? "success-alert" : "alert"}>{noticeMessage}</div>}
    </>
  );
}

function customerAlertLabel(customer) {
  if (customer.hasOverdueBalance) {
    return "Due";
  }

  if ((customer.unpaidInvoices || []).some((invoice) => invoiceStatusKey(invoice.status) === "PARTIALLY_PAID")) {
    return "Partial";
  }

  if (Number(customer.outstandingBalance || 0) > 0) {
    return customer.hasBalanceDueSoon ? "Due Soon" : "Outstanding";
  }

  if (customer.hasUpcomingAppointment) {
    return "Booked";
  }

  return "Clear";
}

function customerAlertClass(customer) {
  if (customer.hasOverdueBalance) {
    return "customer-cell-red";
  }

  if (Number(customer.outstandingBalance || 0) > 0) {
    return "customer-cell-yellow";
  }

  if (customer.hasUpcomingAppointment) {
    return "customer-cell-blue";
  }

  return "";
}

function CustomerAlertPill({ customer }) {
  const label = customerAlertLabel(customer);
  const tone = customer.hasOverdueBalance
    ? "red"
    : label === "Partial"
      ? "yellow"
    : Number(customer.outstandingBalance || 0) > 0
      ? "yellow"
      : customer.hasUpcomingAppointment
        ? "blue"
        : "green";

  return <span className={`customer-alert-pill ${tone}`}>{label}</span>;
}

function normalizeAuditLog(log) {
  const message = log.label || log.message || "";
  const inferredAction = message.split(" ")[0]?.toUpperCase() || "UNKNOWN";
  const inferredEntity = log.tab ? log.tab.replace(/s$/, "") : inferEntityFromMessage(message);
  const inferredEntityId = inferEntityIdFromMessage(message);

  return {
    ...log,
    action: log.action || inferredAction,
    entityType: log.entityType || inferredEntity || "-",
    entityId: log.entityId ?? inferredEntityId ?? "-",
    performedBy: log.performedBy || "system",
    label: message || log.action || "Audit event"
  };
}

function inferEntityFromMessage(message) {
  const text = message.toLowerCase();

  if (text.includes("invoice")) {
    return "Invoice";
  }

  if (text.includes("appointment")) {
    return "Appointment";
  }

  if (text.includes("tire") || text.includes("inventory")) {
    return "Tire";
  }

  return "-";
}

function inferEntityIdFromMessage(message) {
  const match = /#(\d+)/.exec(message);
  return match ? match[1] : null;
}

function SettingsPage({ onSave, settings }) {
  const [draft, setDraft] = useState({ ...defaultCompanySettings, ...(settings || {}) });
  const [saveMessage, setSaveMessage] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setDraft({ ...defaultCompanySettings, ...(settings || {}) });
  }, [settings]);

  useEffect(() => {
    if (saveMessage) {
      scrollPageToTop();
    }
  }, [saveMessage]);

  function update(field, value) {
    setDraft((current) => ({ ...current, [field]: value }));
  }

  function uploadLogo(event) {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    if (!file.type.startsWith("image/")) {
      setSaveMessage("Choose an image file for the logo.");
      event.target.value = "";
      return;
    }

    if (file.size > 1024 * 1024) {
      setSaveMessage("Logo image must be under 1 MB.");
      event.target.value = "";
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      update("logoUrl", String(reader.result || ""));
      setSaveMessage("Logo ready. Save settings to keep it.");
    };
    reader.onerror = () => setSaveMessage("Logo could not be read.");
    reader.readAsDataURL(file);
    event.target.value = "";
  }

  async function submit(event) {
    event.preventDefault();
    setIsSaving(true);
    setSaveMessage("");

    try {
      await onSave(draft);
      setSaveMessage("Settings saved and verified in the database.");
    } catch (err) {
      setSaveMessage(err.message || "Settings could not be verified.");
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <section className="work-area">
      <form className="panel form-grid settings-form" onSubmit={submit}>
        <div className="settings-header">
          <span className="brand-mark"><SettingsIcon size={20} /></span>
          <div>
            <span className="eyebrow">Company settings</span>
            <h3>Shop Branding & Invoice Defaults</h3>
          </div>
        </div>
        <Input label="Shop name" required value={draft.shopName} onChange={(shopName) => update("shopName", shopName)} />
        <Input label="Logo URL" value={draft.logoUrl} onChange={(logoUrl) => update("logoUrl", logoUrl)} placeholder="https://..." />
        <label className="logo-upload-control">
          <span>Upload logo</span>
          <input accept="image/*" onChange={uploadLogo} type="file" />
          <strong><Upload size={16} /> Choose image</strong>
        </label>
        <Input label="Phone" value={draft.phone} onChange={(phone) => update("phone", phone)} />
        <Input label="Address" value={draft.address} onChange={(address) => update("address", address)} />
        <Input label="Tax rate %" min="0" step="0.01" type="number" value={draft.taxRate} onChange={(taxRate) => update("taxRate", taxRate)} />
        <label className="settings-terms">
          <span>Default invoice terms</span>
          <textarea value={draft.invoiceTerms} onChange={(event) => update("invoiceTerms", event.target.value)} rows="4" />
        </label>
        <div className="settings-preview">
          {draft.logoUrl && <img alt="" src={draft.logoUrl} />}
          <strong>{draft.shopName || "Your Shop Name"}</strong>
          <span>{draft.phone || "Phone not set"}</span>
          <span>{draft.address || "Address not set"}</span>
        </div>
        {saveMessage && <div className={saveMessage.includes("verified") ? "success-alert" : "alert"}>{saveMessage}</div>}
        <button className="primary-button" disabled={isSaving} type="submit">
          {isSaving ? "Saving..." : "Save Settings"}
        </button>
      </form>
    </section>
  );
}

function ThemeToggleButton({ className = "", onToggle, themeMode }) {
  const nextMode = themeMode === "light" ? "dark" : "light";

  return (
    <button className={`ghost-button with-icon theme-toggle ${className}`.trim()} onClick={onToggle} type="button">
      {themeMode === "light" ? <Moon size={16} /> : <Sun size={16} />}
      {nextMode === "light" ? "Light" : "Dark"}
    </button>
  );
}

function StatusBadge({ value }) {
  const normalized = String(value || "").toUpperCase();
  const tone = statusClassMap[normalized] || "gray";

  return <span className={`status-badge ${tone}`}>{normalized ? statusLabel(normalized) : "-"}</span>;
}

function renderCellValue(value) {
  const rawValue = value?.value ?? value;

  if (typeof rawValue === "string" && statusClassMap[rawValue.toUpperCase()]) {
    return <StatusBadge value={rawValue} />;
  }

  return rawValue;
}

function EmptyTableState({ text }) {
  return (
    <div className="empty-state">
      <span className="brand-mark"><FileText size={18} /></span>
      <strong>{text}</strong>
      <p>Use the form above to create your first record here.</p>
    </div>
  );
}

function DataTable({ actions, columns, emptyText, highlightedRow, rows }) {
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(1);
  const normalizedRows = rows.map((row, index) =>
    Array.isArray(row) ? { key: index, values: row, source: row } : row
  );
  const searchableRows = normalizedRows.filter((row) => {
    const haystack = [
      row.searchText,
      ...row.values.map((value) => value?.text ?? value?.value ?? value)
    ].join(" ").toLowerCase();

    return haystack.includes(query.trim().toLowerCase());
  });
  const pageSize = 8;
  const pageCount = Math.max(Math.ceil(searchableRows.length / pageSize), 1);
  const currentPage = Math.min(page, pageCount);
  const visibleRows = searchableRows.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  function updateQuery(value) {
    setQuery(value);
    setPage(1);
  }

  return (
    <div className="table-wrap">
      <div className="table-toolbar">
        <div className="table-search">
          <input
            aria-label="Search table"
            onChange={(event) => updateQuery(event.target.value)}
            placeholder="Search table"
            value={query}
          />
        </div>
        <div className="table-pagination">
          <span>{searchableRows.length} rows</span>
          <button disabled={currentPage === 1} onClick={() => setPage(currentPage - 1)} type="button">Prev</button>
          <strong>{currentPage}/{pageCount}</strong>
          <button disabled={currentPage === pageCount} onClick={() => setPage(currentPage + 1)} type="button">Next</button>
        </div>
      </div>
      <table>
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column}>{column}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {visibleRows.length === 0 ? (
            <tr>
              <td colSpan={columns.length}>
                <EmptyTableState text={emptyText} />
              </td>
            </tr>
          ) : (
            visibleRows.map((row) => (
              <tr className={highlightedRow === row.key ? "highlight-row" : ""} key={row.key}>
                {row.values.map((value, index) => (
                  <td className={value?.className || ""} key={`${row.key}-${index}`}>
                    {renderCellValue(value)}
                  </td>
                ))}
                {actions && <td>{actions(row.source)}</td>}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

function Input({ label, onChange, type, inputMode, maxLength, ...props }) {
  const isPhoneInput = type === "tel" || String(label || "").toLowerCase().includes("phone");
  const isTireSizeInput = String(label || "").toLowerCase().includes("tire size");
  const resolvedInputMode = isPhoneInput || isTireSizeInput ? "tel" : inputMode;
  const resolvedMaxLength = isPhoneInput ? 12 : isTireSizeInput ? 9 : maxLength;
  const resolvedType = isPhoneInput ? "tel" : type;

  function handleChange(event) {
    const value = event.target.value;
    if (isPhoneInput) {
      onChange(formatCanadianPhoneInput(value));
      return;
    }
    if (isTireSizeInput) {
      onChange(formatTireSizeInput(value));
      return;
    }
    onChange(value);
  }

  return (
    <label>
      <span>{label}</span>
      <input
        {...props}
        inputMode={resolvedInputMode}
        maxLength={resolvedMaxLength}
        type={resolvedType}
        onChange={handleChange}
      />
    </label>
  );
}

function Select({ label, onChange, optionLabel, options, value, ...props }) {
  return (
    <label>
      <span>{label}</span>
      <select {...props} value={value} onChange={(event) => onChange(event.target.value)}>
        {options.length === 0 && <option value="">None available</option>}
        {options.map((option) => (
          <option key={option} value={option}>
            {optionLabel ? optionLabel(option) : option}
          </option>
        ))}
      </select>
    </label>
  );
}

function ServiceTypeSelect({ label = "Service", onChange, value, ...props }) {
  return (
    <Select
      {...props}
      label={(
        <span className="field-label-with-help">
          {label}
          <span aria-label="Service type help" className="help-tip" tabIndex="0">
            ?
            <span className="help-tip-content">
              Installation uses shop inventory. Re & Re means the customer brings loose tires and the shop removes and replaces tires on the rims. Bolt On means the customer brings a mounted set already on rims.
            </span>
          </span>
        </span>
      )}
      onChange={onChange}
      optionLabel={serviceTypeLabel}
      options={SERVICE_TYPE_OPTIONS}
      value={value}
    />
  );
}

function OwnTireServiceNote({ serviceType }) {
  const message = ownTireServiceMessage(serviceType);

  if (!message) {
    return null;
  }

  return (
    <div className="own-tire-service-note">
      <span className="own-tire-service-icon"><Package size={17} /></span>
      <div>
        <strong>{serviceTypeLabel(serviceType)}</strong>
        <p>{message}</p>
      </div>
    </div>
  );
}

export default App;
