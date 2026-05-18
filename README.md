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

TireTrack is a professional full-stack management system designed for tire shops to manage:

* Tire inventory
* Customer appointments
* Invoice generation
* Dashboard analytics
* Stock monitoring
* Sales tracking

The system was built with scalability and real business workflows in mind rather than simple CRUD functionality.

---

# Features

## Dashboard Analytics

* Real-time business dashboard
* Total inventory count
* Revenue tracking
* Invoice analytics
* Low-stock monitoring
* Daily appointment summaries

## Tire Inventory Management

* Create, update, delete, and manage tires
* Tire sizing support:

  * Width
  * Aspect ratio
  * Rim size
* Tire condition support:

  * NEW
  * USED
* Season support:

  * Summer
  * Winter
  * All Season
* Inventory quantity tracking
* Tire location management
* Advanced filtering and search

## Appointment System

* Customer appointment booking
* Appointment status tracking
* Tire/service scheduling
* Date and time support
* Service type management
* Real-time appointment dashboard integration

## Invoice System

* Invoice creation and management
* Multiple invoice items
* Tire inventory deduction after sales
* Automatic total calculations
* Service and tire item support
* Payment method tracking
* Sales history tracking

---

# Tech Stack

## Backend

* Java 21
* Spring Boot 3
* Spring Data JPA
* Hibernate
* Maven
* MySQL

## Frontend

* React
* Vite
* JavaScript
* Modern CSS

## Development Tools

* VS Code
* Postman
* Git
* GitHub

---

# System Architecture

```text
React Frontend
       ↓
Spring Boot REST API
       ↓
Service Layer
       ↓
JPA/Hibernate
       ↓
MySQL Database
```

---

# Database Design

The application uses a relational MySQL database with:

* Tires
* Appointments
* Invoices
* Invoice Items

Key relationships include:

* One invoice → many invoice items
* Inventory deduction during invoice processing
* Dashboard aggregation queries across multiple modules

---

# Backend Highlights

## Validation

The backend includes robust validation using Jakarta Validation:

* Required field validation
* Numeric constraints
* Tire size validation
* Enum validation
* Business rule enforcement

## Business Logic

* Automatic invoice total calculations
* Automatic inventory deduction
* Low-stock detection
* Dashboard aggregation logic
* Appointment tracking

## REST API

Structured RESTful API design with:

* Controller layer
* Service layer
* Repository layer
* DTO usage
* Transactional invoice processing

---

# Dashboard Preview

The frontend dashboard includes:

* Premium modern admin dashboard UI
* Real-time inventory monitoring
* Appointment management system
* Invoice management workflow
* Business analytics and KPI tracking
* Analytical dashboard charts
* Revenue and inventory insights
* Responsive cross-device layout
* Low-stock visualization
* Operational business summaries

The dashboard experience was designed to resemble modern enterprise SaaS platforms used in production business environments.

---

# API Endpoints

## Tires

```http
GET    /api/tires
POST   /api/tires
PUT    /api/tires/{id}
DELETE /api/tires/{id}
```

## Tire Search

```http
GET /api/tires/search/brand
GET /api/tires/search/size
GET /api/tires/search/condition
GET /api/tires/search/season
GET /api/tires/search/location
GET /api/tires/low-stock
```

## Appointments

```http
GET    /api/appointments
POST   /api/appointments
PUT    /api/appointments/{id}
DELETE /api/appointments/{id}
```

## Invoices

```http
GET    /api/invoices
POST   /api/invoices
DELETE /api/invoices/{id}
```

## Dashboard

```http
GET /api/dashboard
```

---

# Frontend Setup

```bash
cd tiretrack-frontend
npm install
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

---

# Backend Setup

## Configure MySQL

Update your:

```properties
application.properties
```

with your MySQL credentials.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tiretrack
spring.datasource.username=root
spring.datasource.password=yourpassword
```

## Run Backend

```bash
mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

# Future Improvements

Planned future upgrades include:

* Authentication & authorization
* Role-based access control
* PDF invoice exports
* Customer management
* Email/SMS appointment reminders
* Sales analytics charts
* Inventory forecasting
* Cloud deployment

---

# What Makes This Project Different

Unlike simple CRUD applications, TireTrack focuses on:

* Real business workflows
* Inventory synchronization
* Invoice automation
* Analytics aggregation
* Professional full-stack architecture
* Scalable backend design
* Interactive dashboard analytics
* Operational data visualization
* Business intelligence concepts
* Real-time inventory management

The project was intentionally designed to resemble production-level business software rather than a tutorial-style application.

The backend architecture demonstrates understanding of:

* REST API design
* Service-oriented architecture
* Transaction management
* Relational database modeling
* Entity relationships
* Validation and business rules
* DTO-based data transfer
* Aggregation queries and analytics

The frontend demonstrates:

* Modern React architecture
* Responsive admin dashboard design
* State management concepts
* API integration
* Dynamic data rendering
* Professional UI/UX styling
* Business dashboard visualization patterns

This project reflects practical software engineering decisions commonly found in real operational management systems.

---

# Author

## Fouad Shalaby

Computer Science Graduate

* Backend Development
* Full-Stack Engineering
* Spring Boot & Java
* React Development
* System Architecture

GitHub:

```text
https://github.com/FShalaby
```

---

# License

This project is for educational, portfolio, and demonstration purposes.
