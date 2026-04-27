-- Multi-tenancy: associate each test run with its owning user
ALTER TABLE test_runs ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);

CREATE INDEX IF NOT EXISTS idx_test_runs_user_id ON test_runs(user_id);
