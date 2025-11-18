CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    settings JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE processes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    key VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(255),
    UNIQUE(tenant_id, key)
);
CREATE TABLE process_versions (
    id UUID PRIMARY KEY,
    process_id UUID NOT NULL REFERENCES processes(id),
    version INT NOT NULL,
    name VARCHAR(255),
    description TEXT,
    source_type VARCHAR(50) NOT NULL,
    source_uri VARCHAR(1024),
    control_flow_graph JSONB,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(255),
    UNIQUE(process_id, version)
);
-- === USERS & RBAC ===
CREATE TABLE users (
    id UUID PRIMARY KEY,
    external_id VARCHAR(255) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE user_tenant_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, tenant_id)
);
-- === SPECS, SCENARIOS & DATA ===
CREATE TABLE protocol_specs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    protocol_id VARCHAR(50) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    raw_spec_uri VARCHAR(1024),
    parsed_summary JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(255),
    UNIQUE(tenant_id, service_name, version)
);
CREATE TABLE scenario_suites (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    process_id UUID NOT NULL REFERENCES processes(id),
    process_version_id UUID REFERENCES process_versions(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    tags TEXT [],
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(255)
);
CREATE TABLE test_scenarios (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    suite_id UUID REFERENCES scenario_suites(id) ON DELETE
    SET NULL,
        key VARCHAR(255) NOT NULL,
        name VARCHAR(255) NOT NULL,
        version INT NOT NULL DEFAULT 1,
        status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
        -- DRAFT, PUBLISHED, DEPRECATED
        tags TEXT [],
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        created_by VARCHAR(255),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_by VARCHAR(255),
        UNIQUE(suite_id, key, version)
);
CREATE TABLE scenario_steps (
    id UUID PRIMARY KEY,
    scenario_id UUID NOT NULL REFERENCES test_scenarios(id) ON DELETE CASCADE,
    order_index INT NOT NULL,
    alias VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    kind VARCHAR(50) NOT NULL,
    -- ACTION, ASSERTION
    channel_type VARCHAR(50) NOT NULL,
    -- HTTP_REST, KAFKA, DB, QUEUE, GRPC
    endpoint_ref JSONB,
    action JSONB,
    expectations JSONB,
    UNIQUE(scenario_id, order_index),
    UNIQUE(scenario_id, alias)
);
CREATE TABLE test_data_sets (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    scope VARCHAR(50) NOT NULL,
    -- GLOBAL, SUITE, SCENARIO
    suite_id UUID REFERENCES scenario_suites(id),
    scenario_id UUID REFERENCES test_scenarios(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    tags TEXT [],
    origin VARCHAR(50) NOT NULL,
    -- MANUAL, AI_GENERATED, IMPORTED
    data JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(255)
);
-- === ENVIRONMENTS & CONNECTORS ===
CREATE TABLE environments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    -- Mappings of alias to profile IDs for different connector types
    -- e.g., { "db": { "main_db": "uuid-of-db-profile" }, "kafka": { ... } }
    profile_mappings JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(255),
    UNIQUE (tenant_id, name)
);
-- === TEST RUNS & RESULTS ===
CREATE TABLE test_runs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    scenario_id UUID NOT NULL REFERENCES test_scenarios(id),
    scenario_version INT NOT NULL,
    data_set_id UUID REFERENCES test_data_sets(id),
    environment_id UUID REFERENCES environments(id),
    mode VARCHAR(50) NOT NULL,
    -- RUN_ALL_STEPS, STOP_ON_FIRST_FAILURE, etc.
    start_step_index INT,
    status VARCHAR(50) NOT NULL,
    -- PENDING, QUEUED, IN_PROGRESS, PASSED, FAILED, CANCELLED
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    triggered_by VARCHAR(255)
);
CREATE TABLE test_step_results (
    id BIGSERIAL PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES test_runs(id) ON DELETE CASCADE,
    step_id UUID NOT NULL,
    -- FK to scenario_steps.id, not enforced for historical flexibility
    step_alias VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    -- PENDING, RUNNING, PASSED, FAILED, SKIPPED, FLAKY
    duration_ms BIGINT,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    payload JSONB,
    violations JSONB
);
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID,
    user_id VARCHAR(255),
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255),
    entity_id VARCHAR(255),
    details JSONB,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
-- === INDEXES ===
CREATE INDEX idx_processes_tenant_id ON processes(tenant_id);
CREATE INDEX idx_process_versions_process_id ON process_versions(process_id);
CREATE INDEX idx_user_tenant_roles_tenant_id ON user_tenant_roles(tenant_id);
CREATE INDEX idx_protocol_specs_tenant_id ON protocol_specs(tenant_id);
CREATE INDEX idx_scenario_suites_tenant_id ON scenario_suites(tenant_id);
CREATE INDEX idx_scenario_suites_process_id ON scenario_suites(process_id);
CREATE INDEX idx_test_scenarios_tenant_id ON test_scenarios(tenant_id);
CREATE INDEX idx_test_scenarios_suite_id ON test_scenarios(suite_id);
CREATE INDEX idx_scenario_steps_scenario_id ON scenario_steps(scenario_id);
CREATE INDEX idx_test_data_sets_tenant_id ON test_data_sets(tenant_id);
CREATE INDEX idx_environments_tenant_id ON environments(tenant_id);
CREATE INDEX idx_test_runs_tenant_id ON test_runs(tenant_id);
CREATE INDEX idx_test_runs_scenario_id ON test_runs(scenario_id);
CREATE INDEX idx_test_runs_status ON test_runs(status);
CREATE INDEX idx_test_step_results_run_id ON test_step_results(run_id);
CREATE INDEX idx_audit_logs_tenant_id ON audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
INSERT INTO tenants (id, name, created_at, updated_at)
VALUES (
        '00000000-0000-0000-0000-000000000000',
        'Default Tenant',
        NOW(),
        NOW()
    );