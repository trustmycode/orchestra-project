ALTER TABLE test_runs ADD COLUMN execution_context JSONB;
ALTER TABLE test_step_results ADD COLUMN input_context_snapshot JSONB;
ALTER TABLE test_step_results ADD COLUMN output_context_delta JSONB;
