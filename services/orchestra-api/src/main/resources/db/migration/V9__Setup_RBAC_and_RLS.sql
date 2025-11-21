-- 1. Create Application Role (if not exists)
-- Note: We use a DO block because CREATE ROLE cannot run inside a transaction block,
-- but Flyway wraps migrations in transactions. However, exception handling in DO block
-- allows us to skip if exists, effectively making it idempotent.
-- WARNING: If this fails due to transaction restrictions in your Postgres version,
-- ensure the role 'orchestra_app' is created manually.
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'orchestra_app') THEN
    CREATE ROLE orchestra_app WITH LOGIN PASSWORD 'orchestra';
  END IF;
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'orchestra_admin') THEN
    CREATE ROLE orchestra_admin WITH LOGIN PASSWORD 'orchestra' BYPASSRLS;
  END IF;
END
$$;

-- 2. Grant Privileges
GRANT CONNECT ON DATABASE orchestra TO orchestra_app;
GRANT USAGE ON SCHEMA public TO orchestra_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO orchestra_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO orchestra_app;

GRANT CONNECT ON DATABASE orchestra TO orchestra_admin;
GRANT USAGE ON SCHEMA public TO orchestra_admin;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO orchestra_admin;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO orchestra_admin;

-- 3. Enable Row Level Security on tables with tenant_id
ALTER TABLE processes ENABLE ROW LEVEL SECURITY;
ALTER TABLE protocol_specs ENABLE ROW LEVEL SECURITY;
ALTER TABLE scenario_suites ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_scenarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_data_sets ENABLE ROW LEVEL SECURITY;
ALTER TABLE environments ENABLE ROW LEVEL SECURITY;
ALTER TABLE db_connection_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE kafka_cluster_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_runs ENABLE ROW LEVEL SECURITY;
ALTER TABLE data_resolvers ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_tenant_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- 4. Create RLS Policies
-- Policy: Users can only see rows where tenant_id matches the session variable app.current_tenant

CREATE POLICY tenant_isolation_processes ON processes USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_protocol_specs ON protocol_specs USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_scenario_suites ON scenario_suites USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_test_scenarios ON test_scenarios USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_test_data_sets ON test_data_sets USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_environments ON environments USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_db_profiles ON db_connection_profiles USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_kafka_profiles ON kafka_cluster_profiles USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_test_runs ON test_runs USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_data_resolvers ON data_resolvers USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_user_tenant_roles ON user_tenant_roles USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY tenant_isolation_audit_logs ON audit_logs USING (tenant_id = current_setting('app.current_tenant')::uuid);

-- 5. Ensure Admin/Migration user can bypass RLS
-- The default 'orchestra' user is usually superuser or owner, so it bypasses RLS by default.
-- Explicitly granting it ensures migrations don't break if it's not superuser.
ALTER ROLE orchestra BYPASSRLS;

-- 6. Grant BYPASSRLS to orchestra_app is NOT done, enforcing security.
-- The application MUST set 'app.current_tenant' at the start of transaction.

