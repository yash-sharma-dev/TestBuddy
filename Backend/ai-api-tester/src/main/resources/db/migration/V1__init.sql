CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE test_runs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    spec_filename VARCHAR(500),
    instructions TEXT,
    environment VARCHAR(100),
    status VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE test_cases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    run_id UUID REFERENCES test_runs(id),
    name VARCHAR(500),
    endpoint VARCHAR(1000),
    method VARCHAR(10),
    headers JSONB,
    payload JSONB,
    expected_status INTEGER,
    expected_schema JSONB,
    test_type VARCHAR(50),
    chain_from UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE test_results (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    test_case_id UUID REFERENCES test_cases(id),
    run_id UUID REFERENCES test_runs(id),
    actual_status INTEGER,
    actual_response JSONB,
    response_time_ms BIGINT,
    passed BOOLEAN,
    error_message TEXT,
    executed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
