# 🏗️ Architecture

## System Diagrams

### Flowchart

```mermaid
flowchart LR
    USER[User / Admin / Moderator] -->|REST HTTPS| FE[React Frontend]
    USER -->|WebSocket future| APIGW[API Gateway / Reverse Proxy]
    FE -->|REST API| APIGW

    subgraph BACKEND["Ridr Backend Platform"]
        direction TB

        subgraph MODULES["Core Modules"]
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

        PG[(PostgreSQL + PostGIS)]
        REDIS[(Redis Cache)]
    end

    subgraph EXTERNAL["External Systems"]
        OSRM[OSRM Routing Engine]
        OSM[OpenStreetMap / Tile Provider]
        STORAGE[MinIO / S3 Future]
        MQ[RabbitMQ Future]
    end

    APIGW --> AUTH
    APIGW --> VEH
    APIGW --> CITY
    APIGW --> RULE
    APIGW --> PARK
    APIGW --> REP
    APIGW --> ROUTE
    APIGW --> ADMIN

    AUTH --> PG
    VEH --> PG
    CITY --> PG
    RULE --> PG
    PARK --> PG
    REP --> PG
    ROUTE --> PG
    ADMIN --> PG

    PARK --> REDIS
    RULE --> REDIS
    ROUTE --> REDIS

    FE --> OSM
    ROUTE --> OSRM
    REP --> STORAGE
    ADMIN --> MQ
    REP --> MQ
    PARK --> MQ

    ROUTE --> CITY
    ROUTE --> RULE
    ROUTE --> PARK
    ROUTE --> REP
    RULE --> VEH
    ADMIN --> RULE
    ADMIN --> PARK
    ADMIN --> REP
    
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

    USER -->|search routes, check parking, report incidents| SYSTEM
    ADMIN -->|review reports, validate parking, manage rules| SYSTEM
    SYSTEM -->|request raw routes| ROUTER
    SYSTEM -->|map data and tiles| MAPS
    SYSTEM -->|import local regulations| MUNICIPAL
    SYSTEM -->|store photos future| STORAGE
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

    subgraph SYSTEM["Ridr Platform"]
        APIGW[API Gateway / Reverse Proxy]

        subgraph CORE["Spring Boot Backend"]
            direction TB
            AUTH[Identity Module]
            VEH[Vehicle Module]
            CITY[City Module]
            RULE[Legal Rules Module]
            PARK[Parking Module]
            REP[Reports Module]
            ROUTE[Routing Module]
            ADMIN[Admin Module]
        end

        REDIS[(Redis Cache)]
        PG[(PostgreSQL + PostGIS)]
    end

    subgraph EXTERNAL["External Systems"]
        OSRM[OSRM Routing Engine]
        OSM[OpenStreetMap]
        MINIO[MinIO / S3 Future]
        MQ[RabbitMQ Future]
    end

    U1 --> FE
    U2 --> FE
    FE -->|REST / WebSocket future| APIGW

    APIGW --> AUTH
    APIGW --> VEH
    APIGW --> CITY
    APIGW --> RULE
    APIGW --> PARK
    APIGW --> REP
    APIGW --> ROUTE
    APIGW --> ADMIN

    AUTH --> PG
    VEH --> PG
    CITY --> PG
    RULE --> PG
    PARK --> PG
    REP --> PG
    ROUTE --> PG
    ADMIN --> PG

    PARK --> REDIS
    RULE --> REDIS
    ROUTE --> REDIS

    FE --> OSM
    ROUTE --> OSRM
    REP --> MINIO
    ADMIN --> MQ
    REP --> MQ
    PARK --> MQ
   ```

### C3 — Component Diagram (Reservation Service)

```mermaid
flowchart TB
    subgraph RM["Routing Module"]
        API[Route Controller]
        SVC[Route Service]
        OSRMCLIENT[OSRM Client]
        SCORE[Route Scoring Component]
        COMPLIANCE[Compliance Evaluator]
        INCIDENTS[Incident Context Loader]
        PARKING[Parking Context Loader]
        CACHE[Route Cache Adapter]
        REPO[Route Repository]
    end

    subgraph DB["Platform Data"]
        PG[(PostgreSQL + PostGIS)]
        REDIS[(Redis)]
    end

    subgraph EXT["Dependencies"]
        OSRM[OSRM Routing Engine]
        RULEMOD[Legal Rules Module]
        REPMOD[Reports Module]
        PARKMOD[Parking Module]
        CITYMOD[City Module]
    end

    API --> SVC
    SVC --> OSRMCLIENT
    SVC --> SCORE
    SVC --> COMPLIANCE
    SVC --> INCIDENTS
    SVC --> PARKING
    SVC --> CACHE
    SVC --> REPO

    OSRMCLIENT --> OSRM
    COMPLIANCE --> RULEMOD
    COMPLIANCE --> CITYMOD
    INCIDENTS --> REPMOD
    PARKING --> PARKMOD
    REPO --> PG
    CACHE --> REDIS
    SCORE --> PG
```

# 🧩 N-Layer Architecture (inside each microservice)
```mermaid
    flowchart TB
  subgraph PRES["Presentation Layer"]
    CTRL[Controller]
  end

  subgraph BUS["Application / Business Layer"]
    SVC[Service]
    UC[Use Case Coordinator]
  end

  subgraph DOM["Domain Layer"]
    ENT[Entities / Domain Objects]
    RULES[Domain Rules / Policies]
    DTO[DTO / Commands / Queries]
  end

  subgraph PERS["Persistence Layer"]
    REPO[Repository]
  end

  subgraph DATA["Infrastructure Layer"]
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
    CONTROLLER ->> CONTROLLER: Validate DTO
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
    SERVICE ->> SERVICE: Compute safety, comfort, compliance
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
