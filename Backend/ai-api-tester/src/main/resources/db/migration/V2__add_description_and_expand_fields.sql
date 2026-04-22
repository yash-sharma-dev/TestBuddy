-- Add description column and increase size of existing text columns to avoid truncation errors
ALTER TABLE test_cases ADD COLUMN description TEXT;
ALTER TABLE test_cases ALTER COLUMN name TYPE TEXT;
ALTER TABLE test_cases ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE test_cases ALTER COLUMN test_type TYPE TEXT;
