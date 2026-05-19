# TireTrack

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-success?style=for-the-badge&logo=springboot" />
  <img src="https://img.shields.io/badge/React-Frontend-61DAFB?style=for-the-badge&logo=react" />
  <img src="https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql" />
  <img src="https://img.shields.io/badge/Status-Production%20Ready-22c55e?style=for-the-badge" />
</p>

<p align="center">
  A full-stack tire shop management platform built for modern tire and rim businesses.
</p>

---

# Overview

TireTrack 🚗

A modern full-stack tire shop management platform built for real-world automotive businesses. TireTrack combines inventory management, appointment scheduling, invoicing, customer booking, analytics, authentication, and role-based business operations into a single scalable system.

Designed with production-style architecture using Spring Boot, MySQL, React, JWT authentication, and enterprise-inspired backend workflows.

✨ Features
🔐 Authentication & Security
JWT authentication
Refresh token system
BCrypt password hashing
Role-Based Access Control (RBAC)
Secure protected APIs
Environment-based secret management
Stateless authentication architecture
👥 Multi-Role System
ADMIN
Full dashboard analytics
Financial reporting
Inventory management
Employee management
Settings management
Audit logs
Invoice/payment oversight
EMPLOYEE
Appointment management
Tire inventory operations
Customer checkout/invoicing
Reservation handling
CUSTOMER / PUBLIC
Public appointment booking
Available time slot viewing
Customer portal access
Appointment tracking
📊 Dashboard & Analytics
Revenue analytics
Inventory distribution charts
Tire condition breakdown
Top inventory insights
Operational overview panels
Recent activity feed
Appointment overview
Invoice summaries
Animated metric cards
Interactive charts using Recharts
🛞 Tire Inventory Management
New & used tire tracking
Inventory quantity management
Reserved inventory system
Low-stock warnings
Out-of-stock indicators
Fast-moving inventory detection
Tire search & filtering
Refill existing inventory
CSV inventory export
Tire detail drawer
Size-based searching
Condition tracking
📅 Appointment System
Public customer booking
Business-hours scheduling
Available slot generation
Double-booking prevention
Appointment validation
Reservation-aware inventory logic
Customer vehicle tracking
Time-slot scheduling
Appointment status management
Employee/admin appointment workflows
Upcoming appointments panel
🧾 Invoicing & Payments
Dynamic invoice generation
Printable invoices
PDF invoice export
Invoice lifecycle management
Tax calculation system
Appointment-linked invoices
Inventory deduction workflows
Reservation consumption handling
Invoice CSV export
Payment status tracking
Company branding support
Monthly sales reports
🏢 Company Settings
Shop name management
Logo URL support
Phone/address management
Configurable tax rates
Default invoice terms
Persistent settings storage
🧠 Backend Architecture

TireTrack follows layered backend architecture:

Controller Layer
↓
Service Layer
↓
Repository Layer
↓
Database
Backend Technologies
Java
Spring Boot
Spring Security
JWT
JPA / Hibernate
MySQL
Maven
Architectural Highlights
DTO separation
Service-layer business logic
Repository abstraction
JWT authentication filter
Role-based route protection
Transactional workflows
Environment configuration
Public/internal API separation
🎨 Frontend

Modern React frontend focused on usability and operational efficiency.

Frontend Technologies
React
Vite
Axios
Recharts
Framer Motion
Lucide React
CSS
Frontend Features
Responsive UI
Animated dashboard
Protected frontend routes
Role-aware navigation
Public booking page
Global search
Notifications system
Sticky sidebar
Interactive charts
Pagination & searchable tables
Loading skeletons
Modern business UI/UX
🔄 Real-World Business Logic

TireTrack was designed around realistic tire shop workflows:

Inventory Reservation Logic

Appointments reserve tire inventory before checkout.

When invoicing:

Reserved inventory is consumed first
Remaining stock is validated
Inventory quantities update automatically
Appointment status updates when paid
Scheduling Logic
Prevents overlapping appointments
Prevents past bookings
Generates valid business-hour slots
Supports operational scheduling workflows
Security Logic
Employees cannot access business analytics
Admin routes are protected
Public booking routes remain open
Tokens are validated on protected requests
📂 Project Structure
src/main/java/com/aem/tiretrack
├── config
├── controller
├── dto
├── enums
├── exception
├── model
├── repository
├── security
└── service
🚀 Getting Started
Prerequisites
Java 17+
Maven
MySQL
Node.js
npm
⚙️ Backend Setup
1. Clone Repository
git clone https://github.com/FShalaby/tiretrack.git
cd tiretrack
2. Configure Environment Variables

Set JWT secret in your system environment variables:

JWT_SECRET=your_secure_secret_here
3. Configure Database

Update:

src/main/resources/application.properties

Example:

spring.datasource.url=jdbc:mysql://localhost:3306/tiretrack
spring.datasource.username=root
spring.datasource.password=yourpassword
4. Run Backend
mvn spring-boot:run
💻 Frontend Setup
cd frontend
npm install
npm run dev
🔑 API Security Example

Protected requests require:

Authorization: Bearer YOUR_TOKEN
📌 Example API Routes
Public Booking
POST /api/public/bookings
Login
POST /api/auth/login
Dashboard (Admin Only)
GET /api/dashboard
Inventory
GET /api/tires
🧪 Current Status

TireTrack is actively evolving with production-inspired architecture and business workflows.

Planned Improvements
Automated reminders
Email notifications
SMS integration
Multi-location support
Advanced reporting
Technician assignment
Deployment & cloud hosting
Unit/integration testing
Refresh token hardening
📸 Screenshots

Add dashboard, invoice, booking, and analytics screenshots here.

👨‍💻 Author
Fouad Shalaby

Computer Science Graduate passionate about backend engineering, scalable software architecture, business systems, and full-stack application development.

GitHub: FShalaby GitHub
LinkedIn: LinkedIn Profile
📄 License

This project is licensed under the MIT License.
