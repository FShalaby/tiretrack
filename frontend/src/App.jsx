import { useEffect, useMemo, useState } from "react";
import { motion } from "framer-motion";
import {
  AlertTriangle,
  Bell,
  CalendarDays,
  CheckCircle2,
  CircleDollarSign,
  ClipboardList,
  Download,
  Disc3,
  FileText,
  Gauge,
  LogIn,
  Package,
  RefreshCw,
  Search,
  Settings as SettingsIcon,
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
  createNotification,
  createPublicBooking,
  createTire,
  createVendor,
  deleteAppointment,
  deleteCustomerVehicle,
  deleteInvoice,
  deleteTire,
  getAppointments,
  getAccountingAccounts,
  getAccountingReport,
  getAuditLogs,
  getDashboard,
  getInvoice,
  getInvoices,
  getNotifications,
  getLowStockTires,
  getSalesData,
  getSettings,
  getTires,
  getAvailableSlots,
  getCustomerPortal,
  getCustomers,
  getVendors,
  login as loginApi,
  logout as logoutApi,
  markCustomerNotificationRead,
  markAllNotificationsRead,
  markNotificationRead,
  payExpense as payExpenseApi,
  payCustomerInvoice,
  register as registerApi,
  refreshToken as refreshTokenApi,
  searchTiresByBrand,
  searchTiresByCondition,
  searchTiresByLocation,
  searchTiresBySeason,
  searchTiresBySize,
  sendCustomerNotice,
  updateAppointment,
  updateInvoiceStatus,
  updateSettings,
  updateTire
} from "./api";

const tabs = ["Dashboard", "Tires", "Appointments", "Invoices", "Customers", "Accounting", "Audit Logs", "Settings"];
const employeeHiddenTabs = ["Dashboard", "Customers", "Accounting", "Audit Logs", "Settings"];
const tabIcons = {
  Dashboard: Gauge,
  Tires: Disc3,
  Appointments: CalendarDays,
  Invoices: FileText,
  Customers: UserCircle,
  Accounting: CircleDollarSign,
  "Audit Logs": ClipboardList,
  Settings: SettingsIcon
};
const metricIcons = {
  "Tires in stock": Package,
  "Low stock": AlertTriangle,
  Invoices: FileText,
  Revenue: CircleDollarSign,
  "Today appointments": CalendarDays,
  "Customer alerts": UserCircle
};
const statusClassMap = {
  BOOKED: "blue",
  COMPLETED: "green",
  CANCELLED: "red",
  PAID: "green",
  UNPAID: "yellow",
  PARTIAL: "yellow",
  OVERDUE: "red",
  DUE_SOON: "yellow",
  REMINDER: "blue",
  NEW: "green",
  USED: "yellow"
};
const chartColors = ["#18d3b2", "#7c8cff", "#ef4444", "#f59e0b"];
const appointmentTimes = ["08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"];
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
  background: "#17171d",
  border: "1px solid rgba(255,255,255,0.08)",
  borderRadius: 10,
  color: "#d7d9e0"
};

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
  location: ""
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
  customerName: "",
  phone: "",
  vehicle: "",
  tireSetup: "regular",
  tireSize: "",
  frontTireId: "",
  frontQuantity: 4,
  frontTireSize: "",
  rearTireId: "",
  rearQuantity: 2,
  rearTireSize: "",
  appointmentDate: "",
  serviceType: "INSTALLATION",
  notes: "",
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
  notes: ""
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

const emptyInvoice = {
  companyName: "Your Shop Name",
  customerName: "",
  phone: "",
  vehicle: "",
  paymentMethod: "Cash",
  status: "PAID",
  appointmentId: "",
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

function dateTime(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString();
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

function todayDateKey() {
  return toDateKey(new Date());
}

function appointmentDateKey(value) {
  return splitAppointmentDate(value).date;
}

function appointmentTimeKey(value) {
  return splitAppointmentDate(value).time;
}

function isBookableAppointment(appointment) {
  return appointment.status !== "CANCELLED" && appointment.status !== "COMPLETED";
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

function defaultTabForRole(role) {
  if (role === "CUSTOMER") {
    return "Portal";
  }

  return role === "EMPLOYEE" ? "Tires" : "Dashboard";
}

function canAccessTab(role, tab) {
  if (role === "CUSTOMER") {
    return tab === "Portal";
  }

  return role !== "EMPLOYEE" || !employeeHiddenTabs.includes(tab);
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

function buildTireSize(form) {
  if (form.serviceType !== "INSTALLATION") {
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
      tireSize: tireSize || "",
      frontTireSize: "",
      rearTireSize: ""
    };
  }

  return {
    tireSetup: "staggered",
    tireSize: "",
    frontTireSize: staggeredMatch[1] || "",
    rearTireSize: staggeredMatch[2] || ""
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
  const [signupForm, setSignupForm] = useState({ fullName: "", email: "", phone: "", password: "" });
  const [signupError, setSignupError] = useState("");
  const [signupSubmitting, setSignupSubmitting] = useState(false);
  const [activeTab, setActiveTab] = useState(() => defaultTabForRole(loadStoredAuth()?.role));
  const [activeAccountingTab, setActiveAccountingTab] = useState("Dashboard");
  const [globalQuery, setGlobalQuery] = useState("");
  const [showNotifications, setShowNotifications] = useState(false);
  const [appNotifications, setAppNotifications] = useState([]);
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
  const [invoices, setInvoices] = useState([]);
  const [salesData, setSalesData] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [customerPortal, setCustomerPortal] = useState(null);
  const [accountingReport, setAccountingReport] = useState(null);
  const [accountingAccounts, setAccountingAccounts] = useState([]);
  const [vendors, setVendors] = useState([]);
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

  async function loadData(currentAuth = auth) {
    setLoading(true);
    setError("");

    try {
      if (currentAuth?.role === "CUSTOMER") {
        const portal = await getCustomerPortal();
        setCustomerPortal(portal);
        setLoading(false);
        return;
      }

      if (currentAuth?.role === "ADMIN") {
        const [summary, tireList, appointmentList, invoiceList, salesList, savedSettings, auditLogs, customerList, accounting, accounts, vendorList, notifications] = await Promise.all([
          getDashboard(),
          getTires(),
          getAppointments(),
          getInvoices(),
          getSalesData(),
          getSettings().catch(() => loadCompanySettings()),
          getAuditLogs().catch(() => []),
          getCustomers().catch(() => []),
          getAccountingReport().catch(() => null),
          getAccountingAccounts().catch(() => []),
          getVendors().catch(() => []),
          getNotifications().catch(() => [])
        ]);

        const mergedSettings = { ...defaultCompanySettings, ...(savedSettings || {}) };
        setDashboard(summary);
        setTires(tireList || []);
        setInventoryTires(tireList || []);
        setAppointments(appointmentList || []);
        setInvoices(invoiceList || []);
        setSalesData(salesList || []);
        setCustomers(customerList || []);
        setAccountingReport(accounting);
        setAccountingAccounts(accounts || []);
        setVendors(vendorList || []);
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
        const [tireList, appointmentList, invoiceList] = await Promise.all([
          getTires(),
          getAppointments(),
          getInvoices()
        ]);

        setDashboard(null);
        setTires(tireList || []);
        setInventoryTires(tireList || []);
        setAppointments(appointmentList || []);
        setInvoices(invoiceList || []);
        setSalesData([]);
        setCustomers([]);
        setAccountingReport(null);
        setAccountingAccounts([]);
        setVendors([]);
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
    setSignupSubmitting(true);

    try {
      const response = await registerApi({ ...signupForm, role: "CUSTOMER" });
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

  async function refreshCustomerPortal() {
    const portal = await getCustomerPortal();
    setCustomerPortal(portal);
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
    await createCustomerAppointment(appointment);
    scrollToFeedback();
    await refreshCustomerPortal();
  }

  async function payInvoiceFromPortal(id) {
    await payCustomerInvoice(id);
    scrollToFeedback();
    await refreshCustomerPortal();
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
      getAccountingReport().catch(() => null),
      getAccountingAccounts().catch(() => []),
      getVendors().catch(() => [])
    ]);
    setAccountingReport(accounting);
    setAccountingAccounts(accounts || []);
    setVendors(vendorList || []);
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

  function jumpToResult(result) {
    setActiveTab(result.tab);
    setGlobalQuery("");
    setHighlightedRow(result.id);
    window.setTimeout(() => setHighlightedRow(null), 2200);
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

  const lowStockTires = useMemo(
    () => tires.filter((tire) => Number(tire.quantity) <= 5),
    [tires]
  );
  const activeAppointments = useMemo(
    () => {
      const paidAppointmentIds = new Set(
        invoices
          .filter((invoice) => invoice.status === "PAID" && invoice.appointmentId)
          .map((invoice) => Number(invoice.appointmentId))
      );

      return appointments.filter((appointment) =>
        appointment.status !== "COMPLETED"
        && appointment.status !== "CANCELLED"
        && !paidAppointmentIds.has(Number(appointment.id))
      );
    },
    [appointments, invoices]
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
      .filter((appointment) => [
        appointment.customerName,
        appointment.phone,
        appointment.vehicle,
        appointment.serviceType,
        appointment.status
      ].join(" ").toLowerCase().includes(query))
      .slice(0, 4)
      .map((appointment) => ({
        id: `appointment-${appointment.id}`,
        entityId: appointment.id,
        label: appointment.customerName,
        meta: `${appointment.serviceType} · ${compactDate(appointment.appointmentDate)}`,
        tab: "Appointments"
      }));
    const invoiceMatches = invoices
      .filter((invoice) => [
        invoice.customerName,
        invoice.phone,
        invoice.vehicle,
        invoice.status,
        invoice.paymentMethod
      ].join(" ").toLowerCase().includes(query))
      .slice(0, 4)
      .map((invoice) => ({
        id: `invoice-${invoice.id}`,
        entityId: invoice.id,
        label: invoice.customerName,
        meta: `${money(invoice.total)} · ${invoice.status || "UNPAID"}`,
        tab: "Invoices"
      }));

    return [...tireMatches, ...appointmentMatches, ...invoiceMatches].slice(0, 8);
  }, [appointments, globalQuery, invoices, tires]);
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
      price: Number(tireForm.price)
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

    const appointment = {
      customerName: appointmentForm.customerName,
      phone: appointmentForm.phone,
      vehicle: appointmentForm.vehicle,
      tireSize: buildTireSize(appointmentForm),
      frontTireId: appointmentForm.serviceType === "INSTALLATION" && appointmentForm.frontTireId ? Number(appointmentForm.frontTireId) : null,
      frontQuantity: appointmentForm.serviceType === "INSTALLATION" ? Number(appointmentForm.frontQuantity || 0) : 0,
      rearTireId: appointmentForm.serviceType === "INSTALLATION" && appointmentForm.tireSetup === "staggered" && appointmentForm.rearTireId ? Number(appointmentForm.rearTireId) : null,
      rearQuantity: appointmentForm.serviceType === "INSTALLATION" && appointmentForm.tireSetup === "staggered" ? Number(appointmentForm.rearQuantity || 0) : 0,
      appointmentDate: appointmentForm.appointmentDate,
      serviceType: appointmentForm.serviceType,
      notes: appointmentForm.notes,
      reminderStatus: appointmentForm.reminderStatus,
      reminderAt: appointmentForm.reminderAt || null,
      confirmationStatus: appointmentForm.confirmationStatus,
      cancelReason: appointmentForm.cancelReason,
      status: appointmentForm.status
    };

    if (appointmentForm.serviceType === "INSTALLATION" && !appointment.frontTireId) {
      setError("Select an inventory tire for this installation appointment.");
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

    const matchingAppointment = appointments.find((existingAppointment) =>
      Number(existingAppointment.id) !== Number(editingAppointmentId)
      && isBookableAppointment(existingAppointment)
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
      phone: appointment.phone || "",
      vehicle: appointment.vehicle || "",
      ...tireSetup,
      frontTireId: appointment.frontTireId ? String(appointment.frontTireId) : "",
      frontQuantity: appointment.frontQuantity || 0,
      rearTireId: appointment.rearTireId ? String(appointment.rearTireId) : "",
      rearQuantity: appointment.rearQuantity || 0,
      appointmentDate: appointment.appointmentDate || "",
      serviceType: appointment.serviceType || "INSTALLATION",
      notes: appointment.notes || "",
      reminderStatus: appointment.reminderStatus || "NOT_SET",
      reminderAt: appointment.reminderAt || "",
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
    const savedInvoice = await createInvoice({
        ...invoiceForm,
        companyName: undefined,
        taxRate: Number(companySettings.taxRate || 13),
        appointmentId: invoiceForm.appointmentId ? Number(invoiceForm.appointmentId) : null,
        items
      });
      const printableInvoice = savedInvoice?.id ? await getInvoice(savedInvoice.id) : savedInvoice;

      setGeneratedInvoice(printableInvoice);
      logActivity(`Created invoice for ${invoiceForm.customerName}`, "Invoices");
      setInvoiceForm(makeInvoiceForm(companySettings));
      notifySuccess(`Invoice created for ${invoiceForm.customerName}.`);
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
      location: tire.location || ""
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

  async function removeInvoice(id) {
    await deleteInvoice(id);
    notifySuccess("Invoice deleted.");
    await loadData();
  }

  async function markInvoicePaid(invoice) {
    await updateInvoiceStatus(invoice.id, "PAID");
    logActivity(`Marked invoice #${invoice.id} paid`, "Invoices");
    notifySuccess(`Invoice #${invoice.id} marked paid.`);
    await loadData();
  }

  async function previewInvoice(invoice) {
    const printableInvoice = invoice?.id ? await getInvoice(invoice.id) : invoice;
    setGeneratedInvoice(printableInvoice);
  }

  async function updateInvoiceLifecycleStatus(invoice, status) {
    await updateInvoiceStatus(invoice.id, status);
    logActivity(`Marked invoice #${invoice.id} ${status}`, "Invoices");
    notifySuccess(`Invoice #${invoice.id} marked ${status}.`);
    await loadData();
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
      phone: appointment.phone || "",
      vehicle: appointment.vehicle || "",
      items: items.length ? items : nextInvoiceForm.items
    });
    setActiveTab("Invoices");
  }

  if (isPublicBooking) {
    return <PublicBookingPage />;
  }

  if (isCustomerSignupRoute) {
    return (
      <CustomerSignupScreen
        error={signupError}
        form={signupForm}
        isSubmitting={signupSubmitting}
        onSubmit={handleCustomerSignup}
        setForm={setSignupForm}
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
      />
    );
  }

  if (authLoading) {
    return (
      <main className="login-shell">
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
        onPayInvoice={payInvoiceFromPortal}
        onRefresh={refreshCustomerPortal}
        onSaveVehicle={saveCustomerVehicle}
        portal={customerPortal}
      />
    );
  }

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

        <button className="sidebar-booking-link with-icon" onClick={() => { window.location.href = "/booking"; }} type="button">
          <CalendarDays size={17} />
          Public Booking
        </button>
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
                className="icon-button"
                aria-label="Notifications"
                onClick={() => setShowNotifications((current) => !current)}
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
                        className={notification.read ? "read" : ""}
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
            <button className="ghost-button with-icon" onClick={() => loadData(auth)} type="button">
              <RefreshCw size={16} />
              Refresh
            </button>
            <button className="ghost-button with-icon" onClick={() => { window.location.href = "/booking"; }} type="button">
              <CalendarDays size={16} />
              Public Booking
            </button>
            <button className="ghost-button with-icon" onClick={handleLogout} type="button" title="Log out">
              <UserCircle size={19} />
              {auth?.fullName || "Log out"}
            </button>
          </div>
        </header>

        {successMessage && <div className="success-alert">{successMessage}</div>}
        {error && <div className="alert">{error}</div>}
        {loading ? <DashboardSkeleton /> : null}

        {!loading && activeTab === "Dashboard" && (
          <Dashboard
            appointments={activeAppointments}
            customers={customers}
            dashboard={dashboard}
            invoices={invoices}
            tires={tires}
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

        {!loading && activeTab === "Tires" && (
          <Tires
            filters={tireFilters}
            form={tireForm}
            onClearFilters={clearTireFilters}
            onChange={setTireForm}
            onDelete={removeTire}
            onFilterChange={setTireFilters}
            onFilterSubmit={applyTireFilters}
            onRefill={refillTire}
            onSubmit={submitTire}
            highlightedRow={highlightedRow}
            tires={inventoryTires}
          />
        )}

        {!loading && activeTab === "Appointments" && (
          <Appointments
            appointments={appointments}
            editingId={editingAppointmentId}
            form={appointmentForm}
            onChange={setAppointmentForm}
            onCancelEdit={cancelAppointmentEdit}
            onDelete={removeAppointment}
            onEdit={editAppointment}
            onSubmit={submitAppointment}
            highlightedRow={highlightedRow}
            tires={tires}
          />
        )}

        {!loading && activeTab === "Invoices" && (
          <Invoices
            form={invoiceForm}
            generatedInvoice={generatedInvoice}
            settings={companySettings}
            invoices={invoices}
            onChange={setInvoiceForm}
            onDelete={removeInvoice}
            onMarkPaid={markInvoicePaid}
            onPreviewInvoice={previewInvoice}
            onUpdateStatus={updateInvoiceLifecycleStatus}
            onSubmit={submitInvoice}
            highlightedRow={highlightedRow}
            appointments={activeAppointments}
            tires={tires}
          />
        )}

        {!loading && activeTab === "Customers" && (
          <CustomersPage customers={customers} onSendNotice={sendNoticeToCustomer} />
        )}

        {!loading && activeTab === "Accounting" && (
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
            onVendorChange={setVendorForm}
            report={accountingReport}
            vendorForm={vendorForm}
            vendors={vendors}
          />
        )}

        {!loading && activeTab === "Audit Logs" && (
          <AuditLogsPage logs={activityLog} />
        )}

        {!loading && activeTab === "Settings" && (
          <SettingsPage settings={companySettings} onSave={saveCompanySettings} />
        )}
      </main>
    </div>
  );
}

function Dashboard({
  appointments,
  customers,
  dashboard,
  invoices,
  lowStockTires,
  onCancelAppointment,
  onDeleteAppointment,
  onEditAppointment,
  onInvoiceAppointment,
  activityLog,
  onJumpActivity,
  salesData,
  tires
}) {
  const [range, setRange] = useState("month");
  const [customRange, setCustomRange] = useState({ start: todayDateKey(), end: todayDateKey() });
  const rangeStart = getRangeStart(range, customRange.start);
  const rangeEnd = range === "custom" ? customRange.end : todayDateKey();
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
  const rangeRevenue = filteredInvoices.reduce((total, invoice) => total + Number(invoice.total || 0), 0);
  const todayRevenue = todayInvoices.reduce((total, invoice) => total + Number(invoice.total || 0), 0);
  const urgentRestock = lowStockTires.filter((tire) => Number(tire.availableQuantity ?? tire.quantity ?? 0) < 3);
  const outstandingCustomerBalance = customers.reduce((total, customer) => total + Number(customer.outstandingBalance || 0), 0);
  const paymentAlertCustomers = customers.filter((customer) => Number(customer.outstandingBalance || 0) > 0);
  const appointmentReminderCustomers = customers.filter((customer) => customer.hasUpcomingAppointment);
  const followUpCustomers = [...paymentAlertCustomers, ...appointmentReminderCustomers]
    .filter((customer, index, list) => list.findIndex((entry) => entry.id === customer.id) === index)
    .sort((first, second) => followUpPriority(first) - followUpPriority(second));
  const upcomingAppointments = [...appointments]
    .filter((appointment) => new Date(appointment.appointmentDate) >= new Date(new Date().setHours(0, 0, 0, 0)))
    .sort((first, second) => new Date(first.appointmentDate) - new Date(second.appointmentDate))
    .slice(0, 5);
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
  const cards = [
    ["Tires in stock", dashboard?.totalTiresInStock ?? 0],
    ["Low stock", dashboard?.lowStockCount ?? 0],
    ["Invoices", dashboard?.totalInvoices ?? 0],
    ["Revenue", money(dashboard?.totalRevenue)],
    ["Today appointments", dashboard?.todayAppointments ?? 0]
  ];

  return (
    <>
      <section className="metric-grid">
        {cards.map(([label, value]) => (
          <motion.article
            animate={{ opacity: 1, y: 0 }}
            className="metric-card"
            initial={{ opacity: 0, y: 10 }}
            key={label}
            transition={{ duration: 0.32 }}
            whileHover={{ y: -4 }}
          >
            <div className="metric-icon">
              {(() => {
                const Icon = metricIcons[label] || Gauge;
                return <Icon size={20} />;
              })()}
            </div>
            <span>{label}</span>
            <strong>{value}</strong>
          </motion.article>
        ))}
      </section>

      <section className="dashboard-filters panel">
        <div>
          <span className="eyebrow">Dashboard range</span>
          <h3>{range === "custom" ? `${rangeStart} to ${rangeEnd}` : `This ${range}`}</h3>
        </div>
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
        <strong>{money(rangeRevenue)} revenue</strong>
      </section>

      <section className="today-panel panel">
        <div className="today-panel-header">
          <div>
            <span className="eyebrow">Today</span>
            <h3>Daily Operations</h3>
          </div>
          <strong>{compactDate(new Date())}</strong>
        </div>
        <div className="today-stats">
          <div><span>Appointments</span><strong>{todayAppointments.length}</strong></div>
          <div><span>Invoices</span><strong>{todayInvoices.length}</strong></div>
          <div><span>Revenue</span><strong>{money(todayRevenue)}</strong></div>
          <div><span>Low stock actions</span><strong>{urgentRestock.length}</strong></div>
        </div>
        <div className="today-actions">
          {urgentRestock.slice(0, 3).map((tire) => (
            <span className="warning-chip danger" key={tire.id}>{tire.brand} {tire.width}/{tire.aspectRatio}R{tire.rimSize}</span>
          ))}
          {urgentRestock.length === 0 && <span className="warning-chip good">No urgent restocks</span>}
        </div>
      </section>

      <section className="split">
        <SalesChart salesData={filteredSalesData.length ? filteredSalesData : salesData} />
        <InventoryBars lowStockTires={lowStockTires} tires={tires} />
      </section>

      <section className="dashboard-density">
        <ActivityPanel icon={CalendarDays} title="Upcoming Appointments">
          {upcomingAppointments.length === 0 ? (
            <p className="empty-note">No upcoming appointments.</p>
          ) : (
            upcomingAppointments.map((appointment) => (
              <div className="activity-row" key={appointment.id}>
                <span>{appointment.customerName}</span>
                <strong>{dateTime(appointment.appointmentDate)}</strong>
                <StatusBadge value={appointment.status || "BOOKED"} />
              </div>
            ))
          )}
        </ActivityPanel>

        <ActivityPanel icon={ShieldCheck} title="Tire Condition Mix">
          <DashboardDonut
            ariaLabel="Tire condition mix chart"
            centerLabel="units"
            segments={conditionTotals}
          />
        </ActivityPanel>

        <ActivityPanel icon={Package} title="Top Inventory">
          <InventoryLeaderBars items={inventoryBars} />
        </ActivityPanel>
      </section>

      <section className="split dashboard-secondary">
        <ActivityPanel className="follow-up" icon={Bell} title="Customer Follow Ups">
          {followUpCustomers.length === 0 ? (
            <p className="empty-note">No customer follow ups.</p>
          ) : (
            <>
              <div className="follow-up-summary">
                <div>
                  <span>Outstanding</span>
                  <strong>{money(outstandingCustomerBalance)}</strong>
                </div>
                <div>
                  <span>Payment alerts</span>
                  <strong>{paymentAlertCustomers.length}</strong>
                </div>
                <div>
                  <span>Bookings</span>
                  <strong>{appointmentReminderCustomers.length}</strong>
                </div>
              </div>
              <div className="follow-up-list">
                {followUpCustomers.slice(0, 2).map((customer) => (
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
              {followUpCustomers.length > 2 ? (
                <p className="follow-up-more">+{followUpCustomers.length - 2} more customer follow ups</p>
              ) : null}
            </>
          )}
        </ActivityPanel>

        <ActivityPanel icon={FileText} title="Latest Invoices">
          {latestInvoices.length === 0 ? (
            <p className="empty-note">No invoices yet.</p>
          ) : (
            latestInvoices.map((invoice) => (
              <div className="activity-row" key={invoice.id}>
                <span>{invoice.customerName}</span>
                <strong>{money(invoice.total)}</strong>
                <StatusBadge value={invoiceDisplayStatus(invoice)} />
              </div>
            ))
          )}
        </ActivityPanel>
      </section>

      <section className="panel activity-feed">
        <div>
          <span className="eyebrow">Activity</span>
          <h3>Recent Actions</h3>
        </div>
        {activityLog.length === 0 ? (
          <p className="empty-note">No activity yet.</p>
        ) : (
          <div className="activity-feed-list">
            {activityLog.slice(0, 6).map((activity) => (
              <button key={activity.id} onClick={() => onJumpActivity(activity.tab)} type="button">
                <strong>{activity.label}</strong>
                <span>{dateTime(activity.createdAt)}</span>
              </button>
            ))}
          </div>
        )}
      </section>

      <section className="split">
        <div>
          <h3>Low Stock</h3>
          <DataTable
            columns={["Brand", "Size", "Qty", "Location"]}
            emptyText="No low stock tires."
            rows={lowStockTires.map((tire) => [
              urgentStockValue(tire, tire.brand),
              `${tire.width}/${tire.aspectRatio}R${tire.rimSize}`,
              urgentStockValue(tire, tire.quantity),
              tire.location || "-"
            ])}
          />
        </div>
        <div>
          <h3>Inventory Health</h3>
          <section className="health-panel">
            <div className="health-stat">
              <span>Total SKUs</span>
              <strong>{tires.length}</strong>
            </div>
            <div className="health-stat">
              <span>Total Units</span>
              <strong>{totalUnits}</strong>
            </div>
            <div className="health-stat">
              <span>Inventory Value</span>
              <strong>{money(inventoryValue)}</strong>
            </div>
            <div className="health-stat">
              <span>Average Units/SKU</span>
              <strong>{averageUnits}</strong>
            </div>
          </section>
        </div>
      </section>

      <AppointmentCalendar
        appointments={appointments}
        onCancelAppointment={onCancelAppointment}
        onDeleteAppointment={onDeleteAppointment}
        onEditAppointment={onEditAppointment}
        onInvoiceAppointment={onInvoiceAppointment}
      />
    </>
  );
}

function InventoryBars({ lowStockTires, tires }) {
  const availableUnits = tires.reduce((total, tire) => total + Number(tire.availableQuantity ?? tire.quantity ?? 0), 0);
  const reservedUnits = tires.reduce((total, tire) => total + Number(tire.reservedQuantity || 0), 0);
  const lowStockCount = lowStockTires.length;
  const segments = [
    { label: "Available", value: availableUnits, className: "available", color: "#18d3b2" },
    { label: "Reserved", value: reservedUnits, className: "reserved", color: "#7c8cff" },
    { label: "Low stock", value: lowStockCount, className: "urgent", color: "#ef4444" }
  ];

  return (
    <section className="analytics-panel panel">
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

function SalesChart({ salesData }) {
  const points = salesData
    .map((day) => ({ date: day.date, revenue: Number(day.revenue || 0) }))
    .sort((first, second) => new Date(first.date) - new Date(second.date));
  const totalRevenue = points.reduce((total, point) => total + point.revenue, 0);

  return (
    <section className="analytics-panel panel">
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
                    <small>{appointment.serviceType} - {appointment.vehicle || "No vehicle"}</small>
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
  return `${tire.width}/${tire.aspectRatio}R${tire.rimSize}`;
}

function tireAvailableQuantity(tire) {
  return Number(tire.availableQuantity ?? tire.quantity ?? 0);
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

function Tires({
  filters,
  form,
  highlightedRow,
  onChange,
  onClearFilters,
  onDelete,
  onFilterChange,
  onFilterSubmit,
  onRefill,
  onSubmit,
  tires
}) {
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
        tire.location || ""
      ])
    );

    downloadTextFile("tiretrack-inventory.csv", csv, "text/csv");
  }

  return (
    <section className="work-area">
      <div className="section-toolbar">
        <div>
          <span className="eyebrow">Inventory</span>
          <h3>Tire Stock</h3>
        </div>
        <button className="ghost-button with-icon" onClick={exportInventory} type="button">
          <Download size={16} />
          Export CSV
        </button>
      </div>
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
        <button className="primary-button" type="submit">Add / Refill Tire</button>
      </form>

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

function Appointments({
  appointments,
  editingId,
  form,
  highlightedRow,
  onCancelEdit,
  onChange,
  onDelete,
  onEdit,
  onSubmit,
  tires
}) {
  return (
    <section className="work-area">
      <form className="panel form-grid" onSubmit={onSubmit}>
        {editingId && (
          <div className="form-banner">
            <span>Editing appointment</span>
            <button className="ghost-button" onClick={onCancelEdit} type="button">
              Cancel
            </button>
          </div>
        )}
        <Input label="Customer" required value={form.customerName} onChange={(customerName) => onChange({ ...form, customerName })} />
        <Input label="Phone" required type="tel" value={form.phone} onChange={(phone) => onChange({ ...form, phone })} />
        <Input
          label="Vehicle"
          placeholder="Example: 2020 Toyota Camry"
          value={form.vehicle}
          onChange={(vehicle) => onChange({ ...form, vehicle })}
        />
        <TireSetupFields
          disabled={form.serviceType !== "INSTALLATION"}
          form={form}
          onChange={onChange}
          tires={tires}
        />
        <AppointmentDatePicker
          appointments={appointments}
          editingId={editingId}
          value={form.appointmentDate}
          onChange={(appointmentDate) => onChange({ ...form, appointmentDate })}
        />
        <Select label="Service" required value={form.serviceType} onChange={(serviceType) => onChange({ ...form, serviceType })} options={["INSTALLATION", "BALANCING", "ROTATION", "REPAIR"]} />
        <Select label="Status" value={form.status} onChange={(status) => onChange({ ...form, status })} options={["BOOKED", "COMPLETED", "CANCELLED"]} />
        <Select label="Reminder" value={form.reminderStatus} onChange={(reminderStatus) => onChange({ ...form, reminderStatus })} options={["NOT_SET", "SCHEDULED", "SENT"]} />
        <Input label="Reminder at" type="datetime-local" value={form.reminderAt || ""} onChange={(reminderAt) => onChange({ ...form, reminderAt })} />
        <Select label="Confirmation" value={form.confirmationStatus} onChange={(confirmationStatus) => onChange({ ...form, confirmationStatus })} options={["PENDING", "CONFIRMED", "NO_SHOW"]} />
        <Input label="Cancel / no-show reason" value={form.cancelReason || ""} onChange={(cancelReason) => onChange({ ...form, cancelReason })} />
        <Input label="Notes" value={form.notes} onChange={(notes) => onChange({ ...form, notes })} />
        <button className="primary-button" type="submit">
          {editingId ? "Save Changes" : "Book Appointment"}
        </button>
      </form>

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
        columns={["Customer", "Phone", "Vehicle", "Tire setup", "Date", "Service", "Reminder", "Confirm", "Status", ""]}
        emptyText="No appointments yet."
        rows={appointments.map((appointment) => ({
          key: `appointment-${appointment.id}`,
          values: [
            appointment.customerName,
            appointment.phone,
            appointment.vehicle || "-",
            appointment.tireSize || "-",
            dateTime(appointment.appointmentDate),
            appointment.serviceType,
            appointment.reminderStatus || "NOT_SET",
            appointment.confirmationStatus || "PENDING",
            appointment.status || "-"
          ],
          source: appointment
        }))}
      />
    </section>
  );
}

function TireSetupFields({ disabled, form, onChange, tires }) {
  const isStaggered = form.tireSetup === "staggered";
  const [searchQuery, setSearchQuery] = useState("");
  const [conditionFilter, setConditionFilter] = useState("ALL");
  const matchingTires = filterTiresForAppointment(tires, searchQuery, conditionFilter);

  function selectTire(tire, position = "front") {
    const size = tireSizeValue(tire);

    if (position === "rear") {
      onChange({
        ...form,
        rearTireId: String(tire.id),
        rearTireSize: size,
        rearQuantity: form.rearQuantity || 2
      });
      return;
    }

    if (isStaggered) {
      onChange({
        ...form,
        frontTireId: String(tire.id),
        frontTireSize: size,
        frontQuantity: form.frontQuantity || 2
      });
      return;
    }

    onChange({
      ...form,
      frontTireId: String(tire.id),
      rearTireId: "",
      tireSize: size,
      frontQuantity: form.frontQuantity || 4,
      rearQuantity: 0
    });
  }

  function selectTireById(tireId, position = "front") {
    const tire = tires.find((entry) => String(entry.id) === String(tireId));

    if (!tire) {
      if (position === "rear") {
        onChange({ ...form, rearTireId: "", rearTireSize: "" });
      } else if (isStaggered) {
        onChange({ ...form, frontTireId: "", frontTireSize: "" });
      } else {
        onChange({ ...form, frontTireId: "", tireSize: "", rearTireId: "" });
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
        onChange={(tireSetup) => onChange({ ...form, tireSetup })}
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
                  <small>{tire.season || "Any season"} - {tireAvailableQuantity(tire)} available - {tire.location || "No location"}</small>
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
            placeholder="245/35R19"
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
            placeholder="275/30R19"
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
            placeholder="205/55R16"
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

function AppointmentDatePicker({ appointments, editingId, onChange, value }) {
  const { date, time } = splitAppointmentDate(value);
  const minDate = todayDateKey();

  function appointmentAtSlot(slot) {
    return appointments.find((appointment) =>
      Number(appointment.id) !== Number(editingId)
      && isBookableAppointment(appointment)
      && appointmentDateKey(appointment.appointmentDate) === date
      && appointmentTimeKey(appointment.appointmentDate) === slot
    );
  }

  function updateDate(nextDate) {
    if (nextDate < minDate) {
      onChange(joinAppointmentDate(minDate, time || "09:00"));
      return;
    }

    onChange(joinAppointmentDate(nextDate, time || "09:00"));
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
      <div className="time-slots" aria-label="Quick appointment times">
        {appointmentTimes.map((slot) => {
          const bookedAppointment = appointmentAtSlot(slot);

          return (
            <button
              className={[time === slot ? "selected" : "", bookedAppointment ? "booked" : ""].filter(Boolean).join(" ")}
              disabled={!date || date < minDate || Boolean(bookedAppointment)}
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
        selectedValue={value}
        onSelect={onChange}
      />
    </fieldset>
  );
}

function AppointmentDayView({ appointments, editingId, onSelect, selectedValue }) {
  const { date, time } = splitAppointmentDate(selectedValue);
  const selectedDate = date || todayDateKey();
  const isPastDate = selectedDate < todayDateKey();

  function appointmentAtSlot(slot) {
    return appointments.find((appointment) =>
      Number(appointment.id) !== Number(editingId)
      && isBookableAppointment(appointment)
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
              <span>{appointment ? appointment.customerName : "Open slot"}</span>
              {appointment && <small>{appointment.serviceType}</small>}
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
  settings,
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

  function exportInvoices() {
    const csv = toCsv(
      ["Customer", "Phone", "Vehicle", "Subtotal", "Tax", "Total", "Payment", "Status"],
      displayedInvoices.map((invoice) => [
        invoice.customerName,
        invoice.phone,
        invoice.vehicle || "",
        invoice.subtotal,
        invoice.taxAmount,
        invoice.total,
        invoice.paymentMethod || "",
        invoice.status || ""
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
    const revenue = monthlyInvoices.reduce((sum, invoice) => sum + Number(invoice.total || 0), 0);
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
            ${monthlyInvoices.map((invoice) => `<tr><td>${htmlEscape(invoice.customerName)}</td><td>${htmlEscape(invoice.status || "")}</td><td align="right">${htmlEscape(money(invoice.total))}</td></tr>`).join("")}
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
      onChange({ ...form, appointmentId: "" });
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
      customerName: appointment.customerName || "",
      phone: appointment.phone || "",
      vehicle: appointment.vehicle || "",
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
            <span className="eyebrow">Appointment source</span>
            <h3>{selectedAppointment ? selectedAppointment.customerName : "Manual invoice"}</h3>
            <p>{selectedAppointment ? `${selectedAppointment.serviceType} - ${dateTime(selectedAppointment.appointmentDate)}` : "Choose an appointment to fill customer, vehicle, and reserved tires."}</p>
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
        <Input label="Company name" value={settings.shopName} onChange={() => {}} disabled />
        <Select label="Payment" value={form.paymentMethod} onChange={(paymentMethod) => onChange({ ...form, paymentMethod })} options={["Cash", "Debit", "Credit", "E-Transfer"]} />
        <Select label="Status" value={form.status} onChange={(status) => onChange({ ...form, status })} options={["DRAFT", "SENT", "UNPAID", "PARTIAL", "PAID", "VOID"]} />

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
          <small>{money(subtotal)} subtotal + {money(tax)} tax preview</small>
        </div>
        <button className="primary-button" type="submit">Create Invoice</button>
      </form>

      <DataTable
        highlightedRow={highlightedRow}
        actions={(invoice) => (
          <div className="table-actions">
            {invoice.status !== "PAID" && (
              <button className="ghost-button" onClick={() => onMarkPaid(invoice)} type="button">
                Mark Paid
              </button>
            )}
            {["SENT", "UNPAID", "PARTIAL", "VOID"].map((status) => (
              invoice.status !== status && (
                <button className="ghost-button" key={status} onClick={() => onUpdateStatus(invoice, status)} type="button">
                  {status}
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
        columns={["Customer", "Phone", "Vehicle", "Subtotal", "HST", "Total", "Payment", "Status", "Due", "Paid At", ""]}
        emptyText="No invoices yet."
        rows={displayedInvoices.map((invoice) => ({
          key: `invoice-${invoice.id}`,
          values: [
            invoice.customerName,
            invoice.phone,
            invoice.vehicle || "-",
            money(invoice.subtotal),
            money(invoice.taxAmount),
            money(invoice.total),
            invoice.paymentMethod || "-",
            invoice.status || "-",
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
    <section className="printable-invoice panel">
      <div className="invoice-toolbar">
        <div>
          <span className="eyebrow">Generated invoice PDF</span>
          <h3>Invoice #{invoice.id}</h3>
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
            <strong>Invoice #{invoice.id}</strong>
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
            <strong>{invoice.status || "-"}</strong>
            <p>{invoice.paymentMethod || "-"}</p>
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
            tire.location || "-"
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
          <span>Location</span><strong>{tire.location || "-"}</strong>
          <span>Unit price</span><strong>{money(tire.price)}</strong>
        </div>
        <button className="primary-button" onClick={onRefill} type="button">Refill Tire</button>
      </aside>
    </div>
  );
}

function emptyCustomerVehicleForm() {
  return { nickname: "", year: "", make: "", model: "", plateNumber: "", tireSetup: "regular", tireSize: "", frontTireSize: "", rearTireSize: "" };
}

function CustomerPortalShell({ auth, onBookAppointment, onDeleteVehicle, onLogout, onMarkNoticeRead, onPayInvoice, onRefresh, onSaveVehicle, portal }) {
  const [vehicleForm, setVehicleForm] = useState(emptyCustomerVehicleForm);
  const [bookingForm, setBookingForm] = useState({
    vehicleId: "",
    appointmentDate: todayDateKey(),
    appointmentTime: "",
    serviceType: "INSTALLATION",
    notes: ""
  });
  const [slots, setSlots] = useState([]);
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (message || error) {
      scrollPageToTop();
    }
  }, [message, error]);

  const vehicles = portal?.vehicles || [];
  const appointments = portal?.appointments || [];
  const invoices = portal?.invoices || [];
  const notifications = portal?.notifications || [];
  const unreadCount = notifications.filter((notification) => !notification.read).length;

  useEffect(() => {
    async function loadSlots() {
      if (!bookingForm.appointmentDate) {
        setSlots([]);
        return;
      }

      setIsLoadingSlots(true);

      try {
        const availableSlots = await getAvailableSlots(bookingForm.appointmentDate);
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
  }, [bookingForm.appointmentDate]);

  useEffect(() => {
    if (!portal) {
      onRefresh().catch(() => {});
    }
  }, [portal, onRefresh]);

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
      if (!bookingForm.appointmentTime) {
        setError("Choose an available appointment time.");
        return;
      }

      await onBookAppointment(bookingForm);
      setBookingForm((current) => ({ ...current, appointmentTime: "", notes: "" }));
      setMessage("Appointment booked.");
    } catch (err) {
      setError(err.message || "Appointment could not be booked.");
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
          <button className="ghost-button with-icon" onClick={onRefresh} type="button"><RefreshCw size={16} />Refresh</button>
          <button className="ghost-button with-icon" onClick={onLogout} type="button"><UserCircle size={17} />Logout</button>
        </div>
      </header>

      {message ? <div className="success-alert">{message}</div> : null}
      {error ? <div className="alert error">{error}</div> : null}

      <section className="customer-metrics">
        <div className="metric-card"><span>Vehicles</span><strong>{vehicles.length}</strong></div>
        <div className="metric-card"><span>Appointments</span><strong>{appointments.length}</strong></div>
        <div className="metric-card"><span>Invoices</span><strong>{invoices.length}</strong></div>
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
              <Input label="Front tire size" required value={vehicleForm.frontTireSize} onChange={(frontTireSize) => setVehicleForm({ ...vehicleForm, frontTireSize })} placeholder="245/35R19" />
              <Input label="Rear tire size" required value={vehicleForm.rearTireSize} onChange={(rearTireSize) => setVehicleForm({ ...vehicleForm, rearTireSize })} placeholder="275/30R19" />
            </>
          ) : (
            <Input label="Tire size" value={vehicleForm.tireSize} onChange={(tireSize) => setVehicleForm({ ...vehicleForm, tireSize })} placeholder="225/45R17" />
          )}
          <button className="primary-button" type="submit">Save Vehicle</button>
        </form>

        <form className="panel customer-card-form" onSubmit={submitBooking}>
          <div className="settings-header">
            <span className="brand-mark"><CalendarDays size={20} /></span>
            <div><span className="eyebrow">Booking</span><h3>Book Service</h3></div>
          </div>
          <Select label="Vehicle" required value={bookingForm.vehicleId} onChange={(vehicleId) => setBookingForm({ ...bookingForm, vehicleId })} options={vehicles.map((vehicle) => String(vehicle.id))} optionLabel={(id) => vehicleName(vehicles.find((vehicle) => String(vehicle.id) === String(id)))} />
          <Select label="Service" required value={bookingForm.serviceType} onChange={(serviceType) => setBookingForm({ ...bookingForm, serviceType })} options={["INSTALLATION", "BALANCING", "ROTATION", "REPAIR"]} />
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
          <button className="primary-button" disabled={!vehicles.length || !slots.length || !bookingForm.appointmentTime} type="submit">Book Appointment</button>
        </form>
      </section>

      <section className="work-area">
        <DataTable
          actions={(vehicle) => <button className="danger-button" onClick={() => onDeleteVehicle(vehicle.id)} type="button">Remove</button>}
          columns={["Vehicle", "Plate", "Setup", "Tire Size", "Saved", ""]}
          emptyText="No saved vehicles yet."
          rows={vehicles.map((vehicle) => ({ key: `vehicle-${vehicle.id}`, source: vehicle, values: [vehicleName(vehicle), vehicle.plateNumber || "-", vehicle.tireSetup || "regular", vehicleTireSize(vehicle), dateTime(vehicle.createdAt)] }))}
        />
        <DataTable
          columns={["Date", "Vehicle", "Service", "Status", "Notes"]}
          emptyText="No appointments yet."
          rows={appointments.map((appointment) => ({ key: `customer-appt-${appointment.id}`, values: [dateTime(appointment.appointmentDate), appointment.vehicle || "-", appointment.serviceType || "-", appointment.status || "-", appointment.notes || "-"] }))}
        />
        <DataTable
          actions={(invoice) => (
            invoice.status !== "PAID" ? (
              <button className="primary-button" onClick={() => onPayInvoice(invoice.id)} type="button">
                Pay
              </button>
            ) : null
          )}
          columns={["Invoice", "Vehicle", "Total", "Status", "Due", "Paid At", "Created", ""]}
          emptyText="No invoices yet."
          rows={invoices.map((invoice) => ({ key: `customer-invoice-${invoice.id}`, source: invoice, values: [`#${invoice.id}`, invoice.vehicle || "-", money(invoice.total), invoice.status || "-", invoice.dueDate || "-", dateTime(invoice.paidAt), dateTime(invoice.createdAt)] }))}
        />
      </section>
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

function PublicBookingPage() {
  const [form, setForm] = useState({
    customerName: "",
    phone: "",
    vehicle: "",
    tireSize: "",
    appointmentDate: todayDateKey(),
    appointmentTime: "",
    serviceType: "INSTALLATION",
    notes: ""
  });
  const [slots, setSlots] = useState([]);
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (message || error) {
      scrollPageToTop();
    }
  }, [message, error]);

  useEffect(() => {
    async function loadSlots() {
      if (!form.appointmentDate) {
        setSlots([]);
        return;
      }

      setIsLoadingSlots(true);
      setError("");

      try {
        const availableSlots = await getAvailableSlots(form.appointmentDate);
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
  }, [form.appointmentDate]);

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
      await createPublicBooking(form);
      setMessage("Booking request confirmed. We will see you at the selected time.");
      setForm({
        customerName: "",
        phone: "",
        vehicle: "",
        tireSize: "",
        appointmentDate: form.appointmentDate,
        appointmentTime: "",
        serviceType: "INSTALLATION",
        notes: ""
      });
      const availableSlots = await getAvailableSlots(form.appointmentDate);
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
        </div>

        {message ? <div className="success-alert">{message}</div> : null}
        {error ? <div className="alert error">{error}</div> : null}

        <form className="public-booking-form" onSubmit={submit}>
          <Input label="Name" required value={form.customerName} onChange={(customerName) => update("customerName", customerName)} />
          <Input label="Phone" required value={form.phone} onChange={(phone) => update("phone", phone)} />
          <Input label="Vehicle" required value={form.vehicle} onChange={(vehicle) => update("vehicle", vehicle)} />
          <Input label="Tire size" value={form.tireSize} onChange={(tireSize) => update("tireSize", tireSize)} placeholder="225/45R17" />
          <Select
            label="Service"
            required
            value={form.serviceType}
            onChange={(serviceType) => update("serviceType", serviceType)}
            options={["INSTALLATION", "BALANCING", "ROTATION", "REPAIR"]}
          />
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
      </section>
    </main>
  );
}

function LoginScreen({ onSubmit, loginForm, setLoginForm, error, isSubmitting }) {
  return (
    <main className="login-shell">
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
      </motion.section>
    </main>
  );
}

function CustomerSignupScreen({ error, form, isSubmitting, onSubmit, setForm }) {
  return (
    <main className="login-shell">
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
            <input required type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
          </label>
          <label>
            Phone
            <input required value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} />
          </label>
          <label>
            Password
            <input required type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} />
          </label>
          <button className="primary-button with-icon" disabled={isSubmitting} type="submit">
            <LogIn size={18} />
            {isSubmitting ? "Creating..." : "Create Account"}
          </button>
          <button className="ghost-button" onClick={() => { window.location.href = "/login"; }} type="button">
            Staff / Customer Sign In
          </button>
        </form>
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
  onTabChange,
  onVendorChange,
  report,
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
              <AccountingKpi label="Revenue" value={money(report?.revenue)} detail="Posted sales" />
              <AccountingKpi label="Expenses" value={money(report?.expenses)} detail="Posted costs" />
              <AccountingKpi label="Net income" value={money(report?.netIncome)} detail="Revenue less expenses" />
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
                  columns={["Date", "Due", "Vendor", "Account", "Category", "Status", "Total", "Paid At", "Admin", ""]}
                  emptyText="No expenses recorded."
                  rows={recentExpenses.map((expense) => ({
                    key: `expense-${expense.id}`,
                    source: expense,
                    values: [
                      expense.expenseDate || "-",
                      expense.dueDate || "-",
                      expense.vendor,
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
            <label><span>Phone</span><input value={vendorForm.phone} onChange={(event) => onVendorChange({ ...vendorForm, phone: event.target.value })} /></label>
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
  const status = String(invoice.status || "UNPAID").toUpperCase();

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
    const dueText = customer.nextPaymentDueDate ? ` by ${customer.nextPaymentDueDate}` : "";
    updateDraft(customer.id, "title", customer.hasOverdueBalance ? "Payment overdue" : "Payment due soon");
    updateDraft(customer.id, "type", "PAYMENT_DUE");
    updateDraft(customer.id, "message", `Invoice #${customer.nextUnpaidInvoiceId || ""} has an outstanding balance of ${money(customer.outstandingBalance)}${dueText}. Please pay it through your TireTrack account.`);
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

function CustomerDetail({ customer, noticeDrafts, noticeMessage, onSendNotice, onSuggestAppointment, onSuggestPayment, onUpdateDraft }) {
  const draft = noticeDrafts[customer.id] || { title: "Account notice", type: "NOTICE", message: "" };

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
  const [draft, setDraft] = useState(settings);
  const [saveMessage, setSaveMessage] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (saveMessage) {
      scrollPageToTop();
    }
  }, [saveMessage]);

  function update(field, value) {
    setDraft((current) => ({ ...current, [field]: value }));
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

function StatusBadge({ value }) {
  const normalized = String(value || "").toUpperCase();
  const tone = statusClassMap[normalized] || "gray";

  return <span className={`status-badge ${tone}`}>{normalized || "-"}</span>;
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
    const haystack = row.values.map((value) => value?.text ?? value?.value ?? value).join(" ").toLowerCase();

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

function Input({ label, onChange, ...props }) {
  return (
    <label>
      <span>{label}</span>
      <input {...props} onChange={(event) => onChange(event.target.value)} />
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

export default App;
