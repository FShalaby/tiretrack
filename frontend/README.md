# TireTrack Frontend

React frontend for the TireTrack Spring Boot API.

## Setup

Install Node.js first, then run:

```bash
npm install
npm run dev
```

The Vite dev server runs on:

```text
http://localhost:5173
```

API calls to `/api` are proxied to the Spring Boot backend at:

```text
http://localhost:8080
```

Start the backend before using the frontend:

```bash
../mvnw spring-boot:run
```
