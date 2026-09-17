# QR-Based Event Registration and Attendance Management System

A full-stack web application for managing college/organizational events end-to-end — from event creation and student registration to QR-code-based attendance tracking and digital certificate generation.

## Overview

This system streamlines the entire event lifecycle for three types of users:

- **Admin** – Manages users, oversees all events, and reviews feedback across the platform
- **Organizer** – Creates and manages events, scans QR codes to mark attendance, and views attendance dashboards
- **Student** – Browses events, registers, receives a QR ticket, submits feedback, and downloads certificates

## Features

- 🔐 Secure authentication and role-based access control (Admin / Organizer / Student)
- 📅 Event creation and management
- 📝 Student event registration
- 📱 QR code ticket generation for each registration
- ✅ QR code scanning for real-time attendance marking
- 📊 Attendance dashboard for organizers
- 💬 Post-event feedback collection and overview
- 🎓 Digital certificate generation for participants
- 👥 User management for admins

## Tech Stack

**Backend**
- Java, Spring Boot
- Maven

**Frontend**
- React (Vite)
- JavaScript (JSX)
- Axios (API communication)

## Project Structure

```
event-management-system/
├── src/                    # Spring Boot backend source
├── frontend/                # React frontend
│   ├── src/
│   │   ├── api/             # API service calls
│   │   ├── components/      # Reusable UI components
│   │   ├── context/          # Auth context/provider
│   │   └── pages/
│   │       ├── admin/
│   │       ├── organizer/
│   │       ├── student/
│   │       └── auth/
│   └── package.json
├── pom.xml                  # Backend Maven config
└── README.md
```

## Getting Started

### Prerequisites
- Java 17+ and Maven
- Node.js and npm

### Backend Setup
```bash
cd Event Management Backend
./mvnw spring-boot:run
```

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

The frontend will typically run on `http://localhost:5173` and the backend on `http://localhost:8080` (adjust based on your configuration).

## Author

**Harshavarthini**
GitHub: [@Harshavarthini-13](https://github.com/Harshavarthini-13)
