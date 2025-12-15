CREATE TABLE transfers (
       id UUID PRIMARY KEY,
       consumer_id VARCHAR(255) NOT NULL,
       provider_id VARCHAR(255) NOT NULL,
       data_type VARCHAR(255) NOT NULL,
       state VARCHAR(50) NOT NULL,
       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
       updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_transfers_state ON transfers(state);
CREATE INDEX idx_transfers_created_at ON transfers(created_at);
