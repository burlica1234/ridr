# 🔁 Typical Flows

This document describes the most important business and system flows in the Ridr platform.

The flows below cover:
- authentication
- user profile setup
- route search
- parking discovery
- report submission
- moderation workflows

These flows reflect the current architecture of Ridr as a **modular monolith** with external route computation through **OSRM**.

---

## 🧩 Flow 1 — User Registration and Login

```mermaid
sequenceDiagram
    participant CLIENT as Web Client
    participant API as Backend API
    participant AUTH as Identity Module
    participant DB as PostgreSQL

    CLIENT ->> API: POST /api/auth/register
    API ->> AUTH: Validate registration request
    AUTH ->> DB: Insert user
    DB -->> AUTH: User persisted
    AUTH -->> API: Registration success
    API -->> CLIENT: 201 Created

    CLIENT ->> API: POST /api/auth/login
    API ->> AUTH: Validate credentials
    AUTH ->> DB: Load user by email
    DB -->> AUTH: User found
    AUTH -->> API: JWT access token + refresh token
    API -->> CLIENT: 200 OK
```

### 🧠 Flow Description

| Step | Action                                      | Notes                                   |
|------|---------------------------------------------|-----------------------------------------|
| 1    | Client sends route request with JWT         | Gateway authenticates token             |
| 2    | Routing Module checks cache                 | Avoids repeated expensive computations  |
| 3    | OSRM generates raw route alternatives       | Provides base route geometry            |
| 4    | System loads rules, incidents, parking      | Adds business context                   |
| 5    | Route suggestions are computed and scored   | Safety, comfort, compliance             |
| 6    | Route data is stored and cached             | Improves future performance             |
| 7    | Final response returned to client           | Includes enriched route options         |


## 🧩 Flow 2 — Nearby Parking Discovery

```mermaid
sequenceDiagram
    participant CLIENT as API Client
    participant GATEWAY as API Gateway
    participant PARK as Parking Module
    participant CACHE as Redis Cache
    participant DBP as PostgreSQL/PostGIS

    CLIENT ->> GATEWAY: GET /api/parking-spots/nearby<br/>JWT token + coordinates
    GATEWAY ->> PARK: Forward request with validated JWT
    PARK ->> CACHE: Check cached nearby parking result
    CACHE -->> PARK: Cache miss
    PARK ->> DBP: Spatial query within radius
    DBP -->> PARK: Matching parking spots
    PARK ->> CACHE: Cache parking result
    PARK -->> GATEWAY: Return parking spots
    GATEWAY -->> CLIENT: Response with nearby validated parking
```

### 🧠 Flow Description

| Step | Action                                      | Notes                                  |
|------|---------------------------------------------|----------------------------------------|
| 1    | Client requests nearby parking              | Based on location or destination       |
| 2    | Parking Module checks cache                | Speeds up repeated queries             |
| 3    | Spatial query executed in PostGIS          | Uses radius-based filtering            |
| 4    | Matching parking spots retrieved           | Includes validation status             |
| 5    | Results cached                             | Improves subsequent lookups            |
| 6    | Response returned to client                | Contains nearby parking options        |

## 🧩 Flow 3 — Submit Incident Report

```mermaid
sequenceDiagram
    participant CLIENT as API Client
    participant GATEWAY as API Gateway
    participant REP as Reports Module
    participant DBR as PostgreSQL

    CLIENT ->> GATEWAY: POST /api/reports<br/>JWT token + report payload
    GATEWAY ->> REP: Forward request with validated JWT
    REP ->> REP: Validate report request
    REP ->> DBR: Insert incident report
    DBR -->> REP: Success
    REP -->> GATEWAY: Return 201 Created
    GATEWAY -->> CLIENT: Response with report confirmation
```

### 🧠 Flow Description
| Step | Action                                      | Notes                                  |
|------|---------------------------------------------|----------------------------------------|
| 1    | Client submits incident report              | Includes type, severity, location      |
| 2    | Reports Module validates request           | Ensures valid input data               |
| 3    | Report stored in database                  | Becomes part of system context         |
| 4    | Confirmation returned to client            | Report successfully registered         |

## 🧩 Flow 4 — Authentication and Authorization

```mermaid
sequenceDiagram
    participant CLIENT as API Client
    participant AUTH as Identity Module
    participant GATEWAY as API Gateway
    participant ROUTE as Routing Module

    CLIENT ->> AUTH: POST /api/auth/login<br/>email + password
    AUTH ->> AUTH: Validate credentials (Spring Security + DB)
    AUTH -->> CLIENT: Return JWT access token + refresh token
    CLIENT ->> GATEWAY: POST /api/routes/search<br/>Authorization: Bearer <token>
    GATEWAY ->> GATEWAY: Validate JWT signature and expiration
    GATEWAY ->> GATEWAY: Check roles and permissions (RBAC)
    GATEWAY ->> ROUTE: Forward request with user context
    ROUTE ->> ROUTE: Verify token claims (user id, role)
    ROUTE -->> GATEWAY: Return authorized route data
    GATEWAY -->> CLIENT: Response with route results
```

### 🧠 Flow Description

| Step | Action                                      | Notes                                           |
|------|---------------------------------------------|-------------------------------------------------|
| 1    | Client sends login credentials              | Email and password provided                    |
| 2    | Identity Module validates credentials       | Uses secure hashing and DB lookup              |
| 3    | JWT token issued                            | Contains user identity and roles               |
| 4    | Client sends authenticated request          | Uses Bearer token                              |
| 5    | Gateway validates token                     | Signature, expiry, and roles                   |
| 6    | Request forwarded with user context         | Enables downstream authorization               |
| 7    | Authorized response returned                | Only allowed data is exposed                   |

## 🧩 Flow 5 — Validate Parking Spot

```mermaid
sequenceDiagram
    participant ADMIN as Admin Client
    participant GATEWAY as API Gateway
    participant MOD as Admin Module
    participant PARK as Parking Module
    participant DBP as PostgreSQL

    ADMIN ->> GATEWAY: POST /api/admin/parking-spots/{id}/validate<br/>JWT token
    GATEWAY ->> MOD: Forward request with validated JWT
    MOD ->> PARK: Load parking spot by id
    PARK -->> MOD: Parking spot details
    MOD ->> DBP: Update validation status
    DBP -->> MOD: Success
    MOD -->> GATEWAY: Return validation result
    GATEWAY -->> ADMIN: Response with updated status
```

### 🧠 Flow Description

| Step | Action                                      | Notes                                  |
|------|---------------------------------------------|----------------------------------------|
| 1    | Admin requests validation of parking spot   | Requires elevated privileges           |
| 2    | Parking data is loaded                     | Ensures correct entity is reviewed     |
| 3    | Validation status updated                  | Approved or rejected                   |
| 4    | Updated result returned                    | Influences future recommendations      |

## 🧩 Flow 6 — Review Incident Report

```mermaid
sequenceDiagram
    participant ADMIN as Admin Client
    participant GATEWAY as API Gateway
    participant MOD as Admin Module
    participant REP as Reports Module
    participant DBR as PostgreSQL

    ADMIN ->> GATEWAY: POST /api/admin/reports/{id}/review<br/>JWT token
    GATEWAY ->> MOD: Forward request with validated JWT
    MOD ->> REP: Load incident report by id
    REP -->> MOD: Incident report details
    MOD ->> DBR: Update report status
    DBR -->> MOD: Success
    MOD -->> GATEWAY: Return review result
    GATEWAY -->> ADMIN: Response with updated report status
```

### 🧠 Flow Description

| Step | Action                                      | Notes                                  |
|------|---------------------------------------------|----------------------------------------|
| 1    | Admin reviews incident report              | Moderation process                     |
| 2    | Report data is loaded                      | Ensures correct context                |
| 3    | Report status updated                      | Reviewed, resolved, or rejected        |
| 4    | Result returned                            | Affects routing relevance              |

## 🧩 Flow 7 — Create Local Rule

```mermaid
sequenceDiagram
    participant ADMIN as Admin Client
    participant GATEWAY as API Gateway
    participant RULE as Legal Rules Module
    participant DBR as PostgreSQL/PostGIS

    ADMIN ->> GATEWAY: POST /api/admin/rules<br/>JWT token + rule payload
    GATEWAY ->> RULE: Forward request with validated JWT
    RULE ->> RULE: Validate local rule request
    RULE ->> DBR: Insert local rule
    DBR -->> RULE: Success
    RULE -->> GATEWAY: Return 201 Created
    GATEWAY -->> ADMIN: Response with new rule
```

### 🧠 Flow Description

| Step | Action                                      | Notes                                  |
|------|---------------------------------------------|----------------------------------------|
| 1    | Admin creates local rule                   | Defines city-specific constraint       |
| 2    | Rule data validated                        | Ensures consistency                    |
| 3    | Rule stored in database                    | Immediately usable by routing          |
| 4    | Confirmation returned                      | Rule successfully created              |

## 🧩 Flow 8 — Route Search (Cache Hit)

```mermaid
sequenceDiagram
    participant CLIENT as API Client
    participant GATEWAY as API Gateway
    participant ROUTE as Routing Module
    participant CACHE as Redis Cache

    CLIENT ->> GATEWAY: POST /api/routes/search
    GATEWAY ->> ROUTE: Forward request with validated JWT
    ROUTE ->> CACHE: Check cached route result
    CACHE -->> ROUTE: Cache hit
    ROUTE -->> GATEWAY: Return cached route suggestions
    GATEWAY -->> CLIENT: 200 OK
```

### 🧠 Flow Description

| Step | Action                                      | Notes                                  |
|------|---------------------------------------------|----------------------------------------|
| 1    | Client sends route request                 | Same or similar query                  |
| 2    | Routing Module checks cache                | Finds existing result                  |
| 3    | Cached result returned                     | No OSRM or DB recomputation            |
| 4    | Response sent to client                    | Faster user experience                 |

## ✅ Summary of All Flows

| Flow | Type | Key Components | Communication |
|------|------|----------------|---------------|
| 1. Route Search | Core | Routing Module, OSRM, Legal Rules, Reports, Parking | REST + HTTP + Redis |
| 2. Nearby Parking Discovery | Core | Parking Module, Redis, PostGIS | REST |
| 3. Submit Incident Report | Core | Reports Module, PostgreSQL | REST |
| 4. Authentication and Authorization | Security | Identity Module, API Gateway | REST + JWT |
| 5. Validate Parking Spot | Moderation | Admin Module, Parking Module | REST |
| 6. Review Incident Report | Moderation | Admin Module, Reports Module | REST |
| 7. Create Local Rule | Administration | Admin Module, Legal Rules Module | REST |
| 8. Route Search (Cache Hit) | Performance | Routing Module, Redis | REST |

## 🌐 Global Interaction Overview — IntelliJ-Compatible

```mermaid
flowchart LR
    subgraph CLIENT [Client Applications]
        A1[Login Request]
        A2[Route Search]
        A3[Parking Search]
        A4[Incident Report Submission]
        A5[Admin Actions]
    end

    subgraph GATEWAY [API Gateway]
        G1[JWT Validation and Routing]
    end

    subgraph AUTH [Identity Module]
        AU1[Validate Credentials]
        AU2[Issue JWT Token]
    end

    subgraph ROUTE [Routing Module]
        R1[Search Routes]
        R2[Load Rules]
        R3[Load Incidents]
        R4[Load Parking]
        R5[Compute Route Scores]
    end

    subgraph PARK [Parking Module]
        P1[Search Nearby Parking]
        P2[Store Parking Entries]
        P3[Expose Approved Spots]
    end

    subgraph REP [Reports Module]
        RP1[Store Incident Reports]
        RP2[Expose Incident Context]
    end

    subgraph RULE [Legal Rules Module]
        L1[Manage Local Rules]
        L2[Provide Active Restrictions]
    end

    subgraph ADMIN [Admin Module]
        AD1[Validate Parking]
        AD2[Review Reports]
        AD3[Audit Admin Actions]
    end

    subgraph OSRM [Routing Engine]
        O1[Compute Raw Routes]
    end

    subgraph CACHE [Redis]
        C1[Route Cache]
        C2[Parking Cache]
        C3[Rules Cache]
    end

    subgraph DB [PostgreSQL + PostGIS]
        D1[(User Data)]
        D2[(Vehicle Data)]
        D3[(City Data)]
        D4[(Rules Data)]
        D5[(Parking Data)]
        D6[(Reports Data)]
        D7[(Route Data)]
        D8[(Audit Data)]
    end

    A1 --> GATEWAY
    GATEWAY --> AUTH
    AUTH --> D1
    AUTH --> GATEWAY

    A2 --> GATEWAY
    GATEWAY --> ROUTE
    ROUTE --> OSRM
    ROUTE --> RULE
    ROUTE --> REP
    ROUTE --> PARK
    ROUTE --> CACHE
    ROUTE --> D7

    A3 --> GATEWAY
    GATEWAY --> PARK
    PARK --> CACHE
    PARK --> D5

    A4 --> GATEWAY
    GATEWAY --> REP
    REP --> D6

    A5 --> GATEWAY
    GATEWAY --> ADMIN
    ADMIN --> RULE
    ADMIN --> PARK
    ADMIN --> REP
    ADMIN --> D8

    RULE --> D4
    RULE --> CACHE

    PARK --> D5
    REP --> D6
```

### 🧠 Diagram Explanation

| Area | Description | Notes |
|------|-------------|-------|
| CLIENT | Represents frontend or external clients interacting with the platform | Includes end users, admins, and moderators |
| GATEWAY | Single entry point that validates JWT and routes requests | Centralizes access control and request forwarding |
| AUTH | Handles authentication and token issuing | Responsible for login, registration, and JWT lifecycle |
| ROUTING | Main orchestrator for route computation and enrichment | Combines OSRM output with rules, reports, and parking context |
| PARKING | Manages parking spots and nearby parking discovery | Uses PostGIS and validation state for trusted recommendations |
| REPORTS | Stores and exposes community-submitted incident data | Provides hazard context for route scoring |
| RULES | Stores and provides legal and local mobility restrictions | Enables city-specific and vehicle-specific compliance logic |
| ADMIN | Handles moderation, validation, and audit-sensitive operations | Maintains trusted platform data |
| OSRM | External engine for raw route calculation | Provides route geometry, distance, and duration |
| CACHE | Redis stores repeated lookup results | Improves performance for routes, parking, and rules |
| DB | PostgreSQL + PostGIS stores business and geospatial data | Primary source of truth for transactional and spatial records |

### 🚦 Key Communication Types

| Communication Type | Examples | Technologies | Notes |
|--------------------|----------|-------------|-------|
| REST (Sync) | `/api/auth/login`, `/api/routes/search`, `/api/reports` | Spring Web | Main communication style for user-facing operations |
| External HTTP | Route calculation against OSRM | WebClient / HTTP | Used by Routing Module to obtain raw route candidates |
| Caching | Routes, rules, parking lookups | Redis | Reduces repeated expensive operations |
| Geospatial Persistence | Parking, reports, route geometry, city boundaries | PostgreSQL + PostGIS | Supports radius queries, zone checks, and geometry storage |
| Security | Authentication and RBAC enforcement | JWT + API Gateway | Protects endpoints and scopes access by role |
| Future Async Messaging | Report and moderation events | RabbitMQ | Planned for future event-driven processing |
| Future Real-Time Updates | Hazard alerts, live route notifications | WebSocket | Planned for future user-facing live updates |
