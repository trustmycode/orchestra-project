CREATE TABLE ai_prompts (
    id UUID PRIMARY KEY,
    key VARCHAR(255) NOT NULL UNIQUE,
    template TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO ai_prompts (id, key, template, version) VALUES
('11111111-1111-1111-1111-111111111111', 'data_planner_system_v1', 'You are an expert test data planner. Your goal is to generate a DataPlan based on the user request. You have access to tools: SchemaLookupTool (to check API structure) and DictionaryLookupTool (to check available values). Use them if the request implies specific entities or IDs. Output the final plan in JSON.', 1),
('22222222-2222-2222-2222-222222222222', 'analyst_system_prompt', 'You are a QA analyst. Review the test run report and suggest improvements.', 1),
('33333333-3333-3333-3333-333333333333', 'data_generation_prompt', 'Generate realistic test data for the following context. Return valid JSON.', 1);

