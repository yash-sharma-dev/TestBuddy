CREATE TABLE IF NOT EXISTS token_usage_log (
    id BIGSERIAL PRIMARY KEY,
    endpoint_path VARCHAR(255),
    operation_type VARCHAR(50),
    input_tokens INT,
    output_tokens INT,
    cache_read_tokens INT,
    cache_creation_tokens INT,
    cost_usd DECIMAL(10, 6),
    created_at TIMESTAMP DEFAULT NOW()
);
