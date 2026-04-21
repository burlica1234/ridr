# 🚴 Ridr – Micro-Mobility Navigation Platform (Backend Focused)

## 🎯 Overview

Ridr is a **micro-mobility navigation and compliance platform** designed for vehicles such as:
- bicycles
- e-scooters
- skateboards
- other lightweight urban transport

The system focuses on:
- intelligent route discovery
- parking spot identification
- local law awareness
- community-driven incident reporting

The platform integrates **geospatial data, routing engines, and business logic** to provide safer and more compliant routes.

The system currently has **no dedicated frontend requirement**, and all functionality can be tested using:
- Postman (REST APIs)
- Swagger / OpenAPI
- optional WebSocket clients (future)
- OSRM routing service
- database inspection tools

---

## 🧠 Learning Outcomes

By building this project, we gain hands-on experience with:

- designing a modular backend architecture
- building REST APIs using Spring Boot
- working with geospatial data using PostgreSQL + PostGIS
- integrating external routing systems (OSRM)
- implementing caching strategies with Redis
- modeling real-world business domains (rules, reports, mobility)
- designing scalable systems without over-engineering
- writing structured technical documentation (architecture, flows, data model)

---

## 🏗️ Architecture Overview

Ridr follows a **Modular Monolith Architecture**, where:
- all components are deployed as a single application
- each domain is separated into its own module
- clear boundaries are maintained between responsibilities

Core modules include:
- Identity (authentication and users)
- Vehicles (vehicle types and preferences)
- Cities (operational areas)
- Legal Rules (mobility restrictions)
- Parking (parking discovery and validation)
- Reports (community incident data)
- Routing (core business logic)
- Admin (moderation and audit)

The system integrates:
- PostgreSQL + PostGIS (data storage)
- Redis (caching layer)
- OSRM (routing engine)

---

## ⚙️ Core Technologies

- **Java 21**
- **Spring Boot 3**
- **Spring Security (JWT)**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL + PostGIS**
- **Redis**
- **OSRM (routing engine)**
- **Docker + Docker Compose**
- **GitHub Actions (CI/CD)**

---

## 🚀 Features

- Route search with safety, comfort, and compliance scoring
- Parking spot discovery using geospatial queries
- Community-driven incident reporting
- Local law-aware routing
- Admin validation workflows
- Caching for improved performance
- Extensible architecture for future growth

---

## 🧪 Testing the System

You can interact with the system using:

- **Postman**
    - call REST endpoints (routes, parking, reports, auth)
- **Swagger / OpenAPI**
    - explore API contracts
- **Database tools**
    - inspect PostgreSQL/PostGIS data
- **OSRM service**
    - verify routing responses

---

## 📚 Documentation

Full technical documentation is available here:

👉 [Project Documentation](./docs/index.md)

Includes:
- architecture diagrams (C4, flowcharts)
- communication design
- data model (ER + PostGIS)
- flows and sequence diagrams
- services and module responsibilities
- CI/CD and pipeline setup

---

## 📌 Project Status

This project is designed to:
- start as a structured backend system
- evolve into a production-ready platform
- support future extensions such as:
    - multi-city rollout
    - real-time updates
    - mobile clients
    - analytics and reporting

---

## 🔭 Future Improvements

- real-time notifications (WebSockets)
- async processing (RabbitMQ / Kafka)
- mobile application integration
- advanced route personalization
- reputation system for community reports
- image uploads for incidents
- performance optimizations at scale

---

## 🧭 Final Notes

Ridr is not just a routing system — it is an **urban mobility intelligence platform**.

It combines:
- routing
- legal compliance
- community data
- geospatial analysis

to provide a smarter and safer navigation experience for micro-mobility users.