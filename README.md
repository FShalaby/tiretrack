# 🚗 TireTrack

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-success?style=for-the-badge&logo=springboot" />
  <img src="https://img.shields.io/badge/React-Frontend-61DAFB?style=for-the-badge&logo=react" />
  <img src="https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql" />
  <img src="https://img.shields.io/badge/Multi--Shop-SaaS-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Status-Beta%20%7C%20Production%20Hardening-orange?style=for-the-badge" />
</p>

<p align="center">
  <strong>Enterprise-inspired tire shop management software built for modern tire and rim businesses.</strong>
</p>

<p align="center">
  Inventory • Appointments • Work Orders • Estimates • Invoices • Payroll • Accounting • Customer Portal • Multi-Shop SaaS
</p>

---

# 🚀 Overview

TireTrack is a modern full-stack business management platform built specifically for tire and rim businesses.

Designed around real-world tire shop workflows, TireTrack combines inventory management, appointments, work orders, estimates, invoicing, payroll, accounting, customer management, and multi-shop SaaS operations into a single platform.

The goal is simple:

> Help tire shops spend less time managing paperwork and more time growing their business.

Built using modern technologies including Spring Boot, React, MySQL, JWT Authentication, and enterprise-inspired architecture patterns.

---

# ✨ Core Features

## 🛞 Inventory Management

Manage both new and used tire inventory with operational workflows designed specifically for tire shops.

### Features

* New tire inventory
* Used tire inventory
* Rim inventory
* Barcode support
* Batch tracking
* Inventory refill workflows
* Inventory search & filtering
* Low-stock monitoring
* Quantity management
* Tire condition tracking
* CSV import/export
* PDF export
* Inventory analytics
* Inventory reservation workflows

---

## 👥 Customer & Vehicle Management

Centralized customer records with vehicle tracking and service history.

### Features

* Customer profiles
* Vehicle management
* Service history
* Appointment history
* Invoice history
* Customer portal access
* Customer-specific records
* Contact management

---

## 📅 Appointment Management

Designed around real-world scheduling operations.

### Features

* Public online booking
* Customer portal booking
* Business-hours scheduling
* Dynamic slot generation
* Double-booking prevention
* Appointment status tracking
* Customer search
* Vehicle assignment
* Upcoming appointment management
* Employee/admin scheduling workflows

---

## 📋 Work Order Management

Track jobs from creation to completion.

### Features

* Create work orders
* Appointment-to-work-order conversion
* In Progress workflow
* Vehicle Ready workflow
* Completion tracking
* Work order history
* Work order notes
* Work order to invoice conversion

---

## 📝 Estimate Management

Professional estimate workflow built for tire sales and services.

### Features

* Create estimates
* Tire estimates
* Service estimates
* Customer approval workflow
* Estimate rejection workflow
* Estimate notifications
* Estimate-to-invoice conversion
* Inventory-safe estimate process

---

## 💰 Invoicing & Payments

Comprehensive billing and payment management.

### Features

* Dynamic invoice generation
* PDF invoices
* Printable invoices
* Tax calculations
* Appointment-linked invoices
* Work-order-linked invoices
* Full payments
* Partial payments
* Outstanding balance tracking
* Payment status management
* Invoice lifecycle management
* Revenue reporting

---

## ⏱️ Attendance Management

Track employee attendance directly within the platform.

### Features

* Employee clock-in
* Employee clock-out
* Attendance tracking
* Shift history
* Absence management
* Attendance reporting

---

## 💵 Payroll Management

Integrated payroll workflows connected to attendance and accounting.

### Features

* Payroll periods
* Payroll generation
* Hourly pay calculation
* Employee loans
* Payroll adjustments
* Payroll approvals
* Payroll payment workflow
* Payroll accounting integration
* Payroll history

---

## 📊 Accounting & Financial Tracking

Built-in accounting functionality for operational visibility.

### Features

* Revenue tracking
* Expense tracking
* Journal entries
* Payroll cost tracking
* Invoice payment accounting
* Financial summaries
* Business reporting
* Accounting diagnostics
* Dashboard financial metrics

---

## 🔔 Notifications & Audit Logs

Keep users informed while maintaining accountability.

### Notifications

* Customer notifications
* Appointment notifications
* Estimate notifications
* Payroll notifications
* Internal business notifications

### Audit Logs

* User activity tracking
* Inventory changes
* Payroll events
* Accounting events
* Administrative actions
* Business operation logs

---

# 🏪 Multi-Shop SaaS Foundation

TireTrack includes a scalable SaaS architecture designed for future growth.

## 👑 SUPER_ADMIN

Platform-wide control and management.

### Capabilities

* Shop management
* User management
* Shop assignment
* Subscription management
* Platform oversight
* Administrative reporting

---

## 🛠️ ADMIN

Full control over shop operations.

### Capabilities

* Inventory management
* Customer management
* Appointment management
* Work orders
* Estimates
* Invoices
* Payroll
* Accounting
* Employee management
* Shop settings

---

## 👨‍🔧 EMPLOYEE

Focused operational access.

### Capabilities

* Appointment workflows
* Inventory operations
* Attendance tracking
* Customer assistance
* Daily operational tasks

---

## 👤 CUSTOMER

Self-service customer experience.

### Capabilities

* Appointment booking
* Customer portal access
* Invoice viewing
* Service history
* Appointment tracking

---

# 🏢 Shop & Location Management

Built to support growing businesses.

### Features

* Shop creation
* Shop assignment
* Multi-location foundation
* Shop-specific settings
* Subscription plan support

### Subscription Plans

* BASIC
* PREMIUM
* ENTERPRISE

---

# 🔐 Security

Security is built into every layer of the platform.

### Features

* JWT Authentication
* Refresh Tokens
* BCrypt Password Hashing
* Spring Security
* Role-Based Access Control (RBAC)
* Protected APIs
* Environment Variable Secrets
* Multi-Tenant Security Foundation
* Secure Route Protection

---

# ⚙️ Backend Architecture

TireTrack follows a layered enterprise-inspired architecture.

```text
Controller Layer
        ↓
Service Layer
        ↓
Repository Layer
        ↓
MySQL Database
```

## Backend Stack

* Java
* Spring Boot
* Spring Security
* JWT
* JPA / Hibernate
* Flyway
* MySQL
* Maven

### Architectural Highlights

* DTO-based API design
* Service-layer business logic
* Repository abstraction
* Transactional workflows
* Migration-based schema management
* Audit logging
* Multi-tenant foundation
* Environment-based configuration

---

# 🖥️ Frontend

Modern React frontend focused on speed, usability, and operational efficiency.

## Frontend Stack

* React
* Vite
* Axios
* Recharts
* Framer Motion
* Lucide React
* CSS

### Frontend Features

* Responsive design
* Role-aware navigation
* Dashboard analytics
* Protected routes
* Customer portal
* Notifications
* Searchable tables
* Interactive charts
* Modern UI/UX
* Business-focused workflows

---

# 📊 Dashboard & Analytics

Actionable business insights for daily operations.

### Features

* Revenue analytics
* Appointment summaries
* Inventory insights
* Payroll metrics
* Accounting metrics
* Operational dashboards
* Recent activity feeds
* Interactive charts

---

# 🧠 Real-World Business Logic

TireTrack was designed around realistic tire shop operations.

### Inventory Logic

* Inventory reservations
* Inventory validation
* Automatic quantity updates
* Batch management
* Refill workflows

### Scheduling Logic

* Business-hour scheduling
* Slot generation
* Double-booking prevention
* Appointment validation

### Financial Logic

* Tax calculations
* Invoice lifecycle management
* Partial payment tracking
* Revenue reporting
* Payroll synchronization

---

# 📂 Project Structure

```text
src/main/java/com/aem/tiretrack
├── config
├── controller
├── dto
├── enums
├── exception
├── model
├── repository
├── security
├── service
└── db/migration
```

---

# 🛣️ Current Roadmap

### Completed

✅ Authentication & Security

✅ Inventory Management

✅ Customer Management

✅ Vehicle Management

✅ Appointment System

✅ Work Orders

✅ Estimates

✅ Invoices

✅ Attendance

✅ Payroll

✅ Accounting

✅ Customer Portal

✅ Notifications

✅ Audit Logs

✅ Shop Management

✅ Location Management

✅ Multi-Shop Foundation

---

### In Progress

🚧 Final Tenant Isolation

🚧 Production Hardening

🚧 Advanced Reporting

🚧 Vendor Invoice Import

🚧 SaaS Deployment Infrastructure

---

# 👨‍💻 Author

## Fouad Shalaby

Computer Science Graduate passionate about:

* Enterprise Software
* SaaS Platforms
* Backend Engineering
* Business Systems
* Software Architecture
* Full-Stack Development

### Connect With Me

**GitHub:** https://github.com/FShalaby

**LinkedIn:** https://www.linkedin.com/in/shalabyf

---

# © Copyright

© 2026 Fouad Shalaby. All Rights Reserved.

This software and its source code are proprietary and confidential.

Unauthorized copying, modification, distribution, or use of this software is prohibited without written permission from the author.
