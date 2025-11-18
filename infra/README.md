# Orchestra Infrastructure

This directory now contains a full Docker Compose stack for Orchestra: PostgreSQL, RabbitMQ, Keycloak, the API, executor and the web UI.

## Configuration

All credentials and service endpoints live in `infra/.env`. The file in the repo ships with sane defaults:

```bash
cp infra/.env infra/.env.local   # optional backup before editing
```

Update the values if you need to customize database passwords or RabbitMQ credentials.

## Bootstrapping the full stack

```bash
cd infra
docker compose up --build -d
```

The first run builds Docker images for:

- `orchestra-api` (port `8085`)
- `orchestra-executor`
- `orchestra-web` (Vite dev server on `http://localhost:3000`)

Subsequent runs can skip the build step:

```bash
docker compose up -d
```

Stop everything with `docker compose down`.

## Hot Reload workflow

`docker-compose.override.yml` is automatically picked up by Docker Compose and mounts your local sources into the containers for fast feedback.

- **Backend (`orchestra-api`, `orchestra-executor`)**
  - Make sure `mvn compile` (or your IDE) has produced `target/classes` at least once.
  - Those directories are bind-mounted into the running containers.
  - `spring-boot-devtools` watches the mounted classes and restarts the app automatically when they change.
  - Debug ports are exposed on `5005` (API) and `5006` (executor).

- **Frontend (`orchestra-web`)**
  - The entire `apps/orchestra-web` folder is mounted.
  - `node_modules` stays inside the container via an anonymous volume, avoiding OS-specific issues.
  - The Vite dev server (HMR) serves the UI on `http://localhost:3000` and proxies API calls to `orchestra-api`.

With this setup a single `docker compose up -d` starts the whole platform with hot reload enabled for day-to-day development.
