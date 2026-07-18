#!/bin/sh
set -eu

: "${POSTGRES_APP_PASSWORD:?POSTGRES_APP_PASSWORD is required}"
: "${POSTGRES_ADMIN_PASSWORD:?POSTGRES_ADMIN_PASSWORD is required}"

psql --set=ON_ERROR_STOP=1 \
  --set=app_password="$POSTGRES_APP_PASSWORD" \
  --set=admin_password="$POSTGRES_ADMIN_PASSWORD" \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" <<'SQL'
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'orchestra_app') THEN
    CREATE ROLE orchestra_app LOGIN;
  END IF;
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'orchestra_admin') THEN
    CREATE ROLE orchestra_admin LOGIN BYPASSRLS;
  END IF;
END
$$;

ALTER ROLE orchestra_app WITH LOGIN PASSWORD :'app_password' NOBYPASSRLS;
ALTER ROLE orchestra_admin WITH LOGIN PASSWORD :'admin_password' BYPASSRLS;
SQL
