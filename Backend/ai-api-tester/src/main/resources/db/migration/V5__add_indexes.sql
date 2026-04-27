-- Query performance indexes
-- Eliminates sequential scans when filtering by run_id (the most common query pattern)
CREATE INDEX IF NOT EXISTS idx_test_cases_run_id
    ON test_cases(run_id);

CREATE INDEX IF NOT EXISTS idx_test_results_run_id
    ON test_results(run_id, executed_at DESC);

CREATE INDEX IF NOT EXISTS idx_test_results_test_case_id
    ON test_results(test_case_id);
