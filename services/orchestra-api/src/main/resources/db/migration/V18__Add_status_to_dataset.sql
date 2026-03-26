ALTER TABLE test_data_sets ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'READY';
ALTER TABLE test_data_sets ADD COLUMN generation_job_id UUID;
ALTER TABLE test_data_sets ALTER COLUMN data DROP NOT NULL;


