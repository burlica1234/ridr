# 🏗️ Architecture

This document describes the architectural design of the **Ridr** platform.

Ridr is built as a **modular monolith** in the first phase.  
This architecture provides:
- clear internal boundaries between domains
- simpler development and deployment
- lower operational complexity
- an easy future path toward service extraction if the platform grows significantly

---

## Architecture Style

**Architecture Style:** Modular Monolith

### Why this approach?
- the project is still in an early product stage
- business logic is complex, but operational scale is still manageable
- strong module boundaries are needed, but microservices would add unnecessary infrastructure complexity
- the system can later evolve toward service decomposition if traffic, team size, or operational constraints justify it

---

## System Diagrams

### Flowchart

```mermaid
flowchart LR
    USER[End User / Admin / Moderator] -->|REST HTTPS| FE[React Frontend]
    FE -->|REST API| APIGW[API Gateway / Reverse Proxy]

    subgraph RIDR["Ridr Backend Platform"]
        direction TB

        subgraph MODULES["Core Application Modules"]
            direction LR
            AUTH[Identity Module]
            VEH[Vehicle Module]
            CITY[City Module]
            RULE[Legal Rules Module]
            PARK[Parking Module]
            REP[Reports Module]
            ROUTE[Routing Module]
            ADMIN[Admin Module]
        end

        DB[(PostgreSQL + PostGIS)]
        CACHE[(Redis Cache)]
    end

    subgraph EXTERNAL["External Systems"]
        OSRM[OSRM Routing Engine]
        OSM[OpenStreetMap / Tile Provider]
        STORAGE[Object Storage Future]
        MQ[Message Broker Future]
    end

    APIGW --> AUTH
    APIGW --> VEH
    APIGW --> CITY
    APIGW --> RULE
    APIGW --> PARK
    APIGW --> REP
    APIGW --> ROUTE
    APIGW --> ADMIN

    AUTH --> DB
    VEH --> DB
    CITY --> DB
    RULE --> DB
    PARK --> DB
    REP --> DB
    ROUTE --> DB
    ADMIN --> DB

    PARK --> CACHE
    RULE --> CACHE
    ROUTE --> CACHE

    FE --> OSM
    ROUTE --> OSRM
    REP --> STORAGE
    ADMIN --> MQ

    ROUTE --> RULE
    ROUTE --> PARK
    ROUTE --> REP
    ROUTE --> CITY
```

## C4 Model Overview

### C1 — System Context Diagram

```mermaid
graph TB
    USER[End User / Rider]
    ADMIN[Admin / Moderator]
    SYSTEM[Ridr Platform]
    ROUTER[OSRM Routing Engine]
    MAPS[OpenStreetMap]
    MUNICIPAL[Municipal / Local Regulation Sources]
    STORAGE[Object Storage Future]

    USER -->|search routes, discover parking, report incidents| SYSTEM
    ADMIN -->|review reports, validate parking, manage rules| SYSTEM
    SYSTEM -->|request route geometry| ROUTER
    SYSTEM -->|render maps and tiles| MAPS
    SYSTEM -->|maintain local mobility rules| MUNICIPAL
    SYSTEM -->|store report media future| STORAGE
```

### C2 - Container Diagram

```mermaid
flowchart LR
    subgraph USERS["Users"]
        U1[End User]
        U2[Admin / Moderator]
    end

    subgraph CLIENTS["Client Applications"]
        FE[React Frontend]
    end

    subgraph PLATFORM["Ridr Platform"]
        APIGW[API Gateway / Reverse Proxy]
        APP[Spring Boot Backend - Modular Monolith]
        DB[(PostgreSQL + PostGIS)]
        CACHE[(Redis Cache)]
    end

    subgraph EXTERNAL["External Systems"]
        OSRM[OSRM Routing Engine]
        OSM[OpenStreetMap]
        STORAGE[Object Storage Future]
        MQ[Message Broker Future]
    end

    U1 --> FE
    U2 --> FE
    FE -->|REST API| APIGW
    FE -->|Map tiles| OSM

    APIGW --> APP
    APP --> DB
    APP --> CACHE
    APP --> OSRM
    APP --> STORAGE
    APP --> MQ
```

### C3 — Component Diagram (Reservation Service)

```mermaid
flowchart TB
    subgraph RM["Routing Module"]
        API[Route Controller]
        SERVICE[Route Service]
        OSRMCLIENT[OSRM Client]
        SCORE[Route Scoring Component]
        COMPLIANCE[Compliance Evaluator]
        INCIDENTS[Incident Context Loader]
        PARKING[Parking Context Loader]
        CACHE[Route Cache Adapter]
        REPO[Route Repository]
    end

    subgraph DATA["Platform Data"]
        DB[(PostgreSQL + PostGIS)]
        REDIS[(Redis)]
    end

    subgraph DEPS["Module Dependencies"]
        RULEMOD[Legal Rules Module]
        REPMOD[Reports Module]
        PARKMOD[Parking Module]
        CITYMOD[City Module]
        OSRM[OSRM Routing Engine]
    end

    API --> SERVICE
    SERVICE --> OSRMCLIENT
    SERVICE --> SCORE
    SERVICE --> COMPLIANCE
    SERVICE --> INCIDENTS
    SERVICE --> PARKING
    SERVICE --> CACHE
    SERVICE --> REPO

    OSRMCLIENT --> OSRM
    COMPLIANCE --> RULEMOD
    COMPLIANCE --> CITYMOD
    INCIDENTS --> REPMOD
    PARKING --> PARKMOD
    REPO --> DB
    CACHE --> REDIS
    SCORE --> DB
```

# 🧩 N-Layer Architecture (inside each microservice)
```mermaid
flowchart TB
    subgraph PRES["Presentation Layer"]
        CTRL[Controller]
    end

    subgraph APP["Application Layer"]
        SVC[Service]
        UC[Use Case Coordinator]
    end

    subgraph DOMAIN["Domain Layer"]
        ENT[Entities / Domain Objects]
        RULES[Domain Rules / Policies]
        DTO[DTO / Commands / Queries]
    end

    subgraph PERS["Persistence Layer"]
        REPO[Repository]
    end

    subgraph INFRA["Infrastructure Layer"]
        DB[(PostgreSQL + PostGIS)]
        CACHE[(Redis)]
        EXT[External Clients]
    end

    CTRL --> SVC
    SVC --> UC
    UC --> ENT
    UC --> RULES
    SVC --> REPO
    REPO --> DB
    SVC --> CACHE
    SVC --> EXT
```

```mermaid
flowchart TB
    subgraph ROOT["com.ridr.<module_name>"]
        subgraph CTRL["controller package"]
            C1[RouteController]
            C2[GlobalExceptionHandler]
        end

        subgraph SVC["service package"]
            S1[RouteService]
            S2[RouteScoringService]
            S3[ComplianceService]
        end

        subgraph REPO["repository package"]
            R1[RouteRequestRepository]
            R2[RouteSuggestionRepository]
        end

        subgraph MOD["model package"]
            M1[RouteRequestEntity]
            M2[RouteSuggestionEntity]
            M3[RouteSearchRequestDto]
            M4[RouteSearchResponseDto]
        end

        subgraph CLIENT["client package"]
            CL1[OsrmClient]
            CL2[ParkingClient]
            CL3[ReportsClient]
            CL4[RulesClient]
        end

        subgraph CONF["config package"]
            CF1[ApplicationConfig]
            CF2[SecurityConfig]
            CF3[RedisConfig]
            CF4[WebClientConfig]
        end

        subgraph UTIL["util package"]
            U1[GeometryUtil]
            U2[ScoreCalculator]
            U3[RouteMapper]
        end
    end

    C1 --> S1
    S1 --> S2
    S1 --> S3
    S1 --> R1
    S1 --> R2
    S1 --> CL1
    S1 --> CL2
    S1 --> CL3
    S1 --> CL4
    R1 --> M1
    R2 --> M2
    C1 --> M3
    S1 --> M4
    S2 --> U2
    S3 --> U1
    C1 --> CF2
    S1 --> CF4
    S1 --> U3
```

```mermaid
sequenceDiagram
    participant CLIENT as Client
    participant CONTROLLER as RouteController
    participant SERVICE as RouteService
    participant CACHE as Redis Cache
    participant OSRM as OSRM Client
    participant RULES as Legal Rules Service
    participant REPORTS as Reports Service
    participant PARKING as Parking Service
    participant REPOSITORY as Route Repository

    CLIENT ->> CONTROLLER: POST /api/routes/search
    CONTROLLER ->> CONTROLLER: Validate request DTO
    CONTROLLER ->> SERVICE: RouteSearchRequest
    SERVICE ->> CACHE: Check cached route result
    CACHE -->> SERVICE: Cache miss
    SERVICE ->> OSRM: Request raw route alternatives
    OSRM -->> SERVICE: Raw routes
    SERVICE ->> RULES: Load active local rules
    RULES -->> SERVICE: Applicable rules
    SERVICE ->> REPORTS: Load nearby incidents
    REPORTS -->> SERVICE: Incident context
    SERVICE ->> PARKING: Load parking near destination
    PARKING -->> SERVICE: Parking suggestions
    SERVICE ->> SERVICE: Compute route scores
    SERVICE ->> REPOSITORY: Persist route request and suggestions
    REPOSITORY -->> SERVICE: Stored results
    SERVICE ->> CACHE: Save cached response
    SERVICE ->> CONTROLLER: RouteSearchResponse
    CONTROLLER ->> CLIENT: HTTP Response
```

## ✅ Layer Responsibilities

| Layer | Package | Purpose | Example classes |
|----------------------------|------------------|------------------------------------------------------|-----------------------------------------------|
| **Presentation Layer** | `controller` | Exposes REST endpoints, handles input/output mapping | `RouteController`, `ParkingController`, `ReportController` |
| **Business Layer** | `service` | Implements business logic and cross-module orchestration | `RouteService`, `ParkingService`, `LegalRuleService` |
| **Persistence Layer** | `repository` | Handles relational and geospatial database access using JPA and PostGIS | `RouteRequestRepository`, `ParkingSpotRepository`, `LocalRuleRepository` |
| **Domain / Data Models** | `model` | Contains JPA entities, DTOs, commands, queries, and scoring models | `RouteRequestEntity`, `ParkingSpotEntity`, `LocalRuleEntity`, `RouteSearchRequestDto` |
| **Infrastructure Layer** | `client`, `config` | Handles external integrations, cache, security, and technical adapters | `OsrmClient`, `RedisConfig`, `SecurityConfig`, `WebClientConfig` |
| **Cross-Cutting Concerns** | `common`, `util`, `audit` | Shared utilities, exception handling, logging, audit, and mapping helpers | `GlobalExceptionHandler`, `GeometryUtil`, `AuditService`, `RouteMapper` |

# Key notes:

- API Gateway is the single entry point for frontend clients and future mobile applications.
- The Routing Module is the main orchestrator of the platform: it receives route requests, calls the routing engine, evaluates legal constraints, loads nearby incidents, checks parking options, and computes route scores.
- The Legal Rules Module is one of the most important differentiators of Ridr because it allows city-specific and vehicle-specific restrictions to be modeled as configurable data instead of hardcoded logic.
- The Parking Module and Reports Module rely heavily on PostgreSQL + PostGIS for proximity search, spatial filtering, and zone-aware validations.
- Redis is used as a performance optimization layer for route caching, nearby parking lookups, and frequently requested city rules.
- The Admin Module is responsible for moderation and validation workflows, turning community-submitted data into trusted operational data.
- Ridr starts as a modular monolith to reduce operational complexity while still preserving strong internal domain boundaries.
- If the platform grows significantly, the most likely future extraction candidates are the Routing Module, Reports Module, Admin Module, and Identity Module.
