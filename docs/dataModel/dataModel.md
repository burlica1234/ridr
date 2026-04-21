# 🗃️ Data Model

This document describes the core data model of the Ridr platform.

Ridr combines:
- relational data
- geospatial data
- moderation state
- route-related scoring context

The model is designed to support:
- users and authentication
- city-specific rules
- vehicle-aware routing
- parking discovery
- community incident reports
- route request history
- route suggestions with enriched metadata

---

## Data Model Overview

The Ridr platform relies on **PostgreSQL + PostGIS** as the primary data store.

This choice enables:
- standard transactional persistence
- geospatial indexing
- radius-based queries
- zone validation
- route geometry storage
- city boundary management

---

## ER Diagram

```mermaid
erDiagram

    USER {
        UUID id PK
        String email
        String passwordHash
        String fullName
        String role
        Timestamp createdAt
        Timestamp updatedAt
    }

    VEHICLE_TYPE {
        UUID id PK
        String code
        String displayName
        Integer defaultMaxSpeed
        Timestamp createdAt
        Timestamp updatedAt
    }

    USER_VEHICLE {
        UUID id PK
        UUID userId
        UUID vehicleTypeId
        String nickname
        String routingPreference
        Timestamp createdAt
        Timestamp updatedAt
    }

    CITY {
        UUID id PK
        String name
        String countryCode
        String status
        Geometry boundary
        Timestamp createdAt
        Timestamp updatedAt
    }

    LOCAL_RULE {
        UUID id PK
        UUID cityId
        UUID vehicleTypeId
        String zoneType
        String ruleType
        String restrictionLevel
        String legalReference
        Timestamp effectiveFrom
        Timestamp effectiveTo
        Boolean active
        Timestamp createdAt
        Timestamp updatedAt
    }

    PARKING_SPOT {
        UUID id PK
        UUID cityId
        UUID submittedBy
        Geometry location
        String parkingType
        Integer capacityEstimate
        Integer safetyRating
        String validationStatus
        String notes
        Timestamp createdAt
        Timestamp updatedAt
    }

    INCIDENT_REPORT {
        UUID id PK
        UUID cityId
        UUID userId
        Geometry location
        String reportType
        String severity
        String status
        String description
        String imageUrl
        Timestamp createdAt
        Timestamp updatedAt
    }

    ROUTE_REQUEST {
        UUID id PK
        UUID userId
        UUID cityId
        UUID vehicleTypeId
        Geometry origin
        Geometry destination
        Timestamp requestedAt
    }

    ROUTE_SUGGESTION {
        UUID id PK
        UUID routeRequestId
        Geometry geometry
        Integer distanceMeters
        Integer estimatedDurationSeconds
        Integer safetyScore
        Integer comfortScore
        Integer complianceScore
        Timestamp createdAt
    }

    AUDIT_LOG {
        UUID id PK
        UUID actorId
        String actionType
        String targetType
        UUID targetId
        Timestamp createdAt
    }

    USER ||--o{ USER_VEHICLE : owns
    VEHICLE_TYPE ||--o{ USER_VEHICLE : type
    CITY ||--o{ LOCAL_RULE : defines
    VEHICLE_TYPE ||--o{ LOCAL_RULE : scoped_to
    CITY ||--o{ PARKING_SPOT : contains
    USER ||--o{ PARKING_SPOT : submits
    CITY ||--o{ INCIDENT_REPORT : contains
    USER ||--o{ INCIDENT_REPORT : creates
    USER ||--o{ ROUTE_REQUEST : requests
    CITY ||--o{ ROUTE_REQUEST : within
    VEHICLE_TYPE ||--o{ ROUTE_REQUEST : requested_for
    ROUTE_REQUEST ||--o{ ROUTE_SUGGESTION : returns
    USER ||--o{ AUDIT_LOG : performs
```

```mermaid
flowchart TB
    subgraph CORE["Core Tables"]
        USER[USER]
        VEHICLE_TYPE[VEHICLE_TYPE]
        USER_VEHICLE[USER_VEHICLE]
        CITY[CITY]
        LOCAL_RULE[LOCAL_RULE]
        PARKING_SPOT[PARKING_SPOT]
        INCIDENT_REPORT[INCIDENT_REPORT]
        ROUTE_REQUEST[ROUTE_REQUEST]
        ROUTE_SUGGESTION[ROUTE_SUGGESTION]
        AUDIT_LOG[AUDIT_LOG]
    end

    USER --> USER_VEHICLE
    VEHICLE_TYPE --> USER_VEHICLE
    CITY --> LOCAL_RULE
    VEHICLE_TYPE --> LOCAL_RULE
    CITY --> PARKING_SPOT
    USER --> PARKING_SPOT
    CITY --> INCIDENT_REPORT
    USER --> INCIDENT_REPORT
    USER --> ROUTE_REQUEST
    CITY --> ROUTE_REQUEST
    VEHICLE_TYPE --> ROUTE_REQUEST
    ROUTE_REQUEST --> ROUTE_SUGGESTION
    USER --> AUDIT_LOG
```
