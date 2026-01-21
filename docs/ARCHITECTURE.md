# Architecture

## Overview

Kotlin-AI-Cloud-Platform is a lightweight, modular set of JVM services that expose REST endpoints for user, auth,
payments, notifications, analytics, and cloud planning. Each service is a small HTTP server that shares in-memory
state for quick demos and deterministic tests.

## Components

```
+-----------------------+        +---------------------+
|  User Service (8080)  |<------>|  Auth Service (8081) |
+-----------------------+        +---------------------+
           |                                 |
           v                                 v
+-----------------------+        +---------------------+
| Payments (8082)       |        | Notifications (8083)|
+-----------------------+        +---------------------+
           |
           v
+-----------------------+        +---------------------+
| Analytics (8084)      |        | Cloud Planner (8085)|
+-----------------------+        +---------------------+
```

## Data Flow

1. A client creates users and authenticates via `/users` and `/login`.
2. Payments and notifications operate on those user IDs.
3. Analytics endpoints apply lightweight models for forecasting, sentiment, and keyword extraction.
4. Cloud planning endpoints allocate workloads to nodes and return Kubernetes/Docker plans.

## Design Goals

- **Fast local setup**: no external services required.
- **Deterministic behavior**: in-memory state and offline-safe tests.
- **Extensible modules**: analytics and cloud planning are isolated services.
