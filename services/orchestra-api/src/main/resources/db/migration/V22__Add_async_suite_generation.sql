ALTER TABLE scenario_suites ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE scenario_suites ADD COLUMN generation_job_id UUID;
CREATE INDEX idx_scenario_suites_status ON scenario_suites(status);


