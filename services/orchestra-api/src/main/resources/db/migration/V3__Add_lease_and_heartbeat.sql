ALTER TABLE test_runs ADD COLUMN locked_by VARCHAR(255);
ALTER TABLE test_runs ADD COLUMN lock_until TIMESTAMPTZ;
ALTER TABLE test_runs ADD COLUMN heartbeat_at TIMESTAMPTZ;

CREATE INDEX idx_test_runs_heartbeat ON test_runs(heartbeat_at);
