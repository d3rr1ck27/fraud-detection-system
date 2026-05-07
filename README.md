# Fraud Detection System

A multi-service fraud detection platform combining a Spring Boot backend, a Python machine learning service, and a React frontend. The backend exposes the public API and orchestrates business logic, the ML service serves trained models for real-time scoring, and the frontend provides the analyst-facing dashboard for reviewing flagged transactions.

## Architecture

- **backend/** — Java / Spring Boot service (Maven). Hosts REST controllers, persistence, and integration with the ML service.
- **ml-service/** — Python / FastAPI service. Loads scikit-learn models and returns fraud probability scores.
- **frontend/** — React single-page app for analysts and administrators.

## Getting started

The repository ships with a `docker-compose.yml` stub that runs all three services together. Replace the placeholder image references with real builds (or a `build:` context) once each service has a Dockerfile, then run:

```
docker compose up --build
```

Each service can also be run independently during development; see the README inside each directory (to be added) for service-specific instructions.
