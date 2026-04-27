-- JSON schema validation result per test: NULL means no schema was provided
ALTER TABLE test_results ADD COLUMN IF NOT EXISTS schema_valid BOOLEAN;
