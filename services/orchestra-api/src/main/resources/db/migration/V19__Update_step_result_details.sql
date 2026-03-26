ALTER TABLE test_step_results RENAME COLUMN input_context_snapshot TO resolved_input;
ALTER TABLE test_step_results RENAME COLUMN output_context_delta TO context_delta;
ALTER TABLE test_step_results ADD COLUMN structured_output JSONB;


