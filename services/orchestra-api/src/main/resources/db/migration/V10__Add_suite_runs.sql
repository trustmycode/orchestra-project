CREATE TABLE suite_runs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    suite_id UUID NOT NULL REFERENCES scenario_suites(id),
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    context JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(255)
);

ALTER TABLE test_runs ADD COLUMN suite_run_id UUID REFERENCES suite_runs(id);

CREATE INDEX idx_suite_runs_tenant_id ON suite_runs(tenant_id);
CREATE INDEX idx_test_runs_suite_run_id ON test_runs(suite_run_id);

ALTER TABLE suite_runs ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_suite_runs ON suite_runs USING (tenant_id = current_setting('app.current_tenant')::uuid);

