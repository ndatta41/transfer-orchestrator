CREATE TABLE audit_log (
   id UUID PRIMARY KEY NOT NULL,
   transfer_id UUID NOT NULL REFERENCES transfers(id) ON DELETE CASCADE,
   action VARCHAR(255),
   timestamp TIMESTAMPTZ NOT NULL DEFAULT now(),
   actor VARCHAR(255),
   metadata TEXT
);

-- Indexes for performance
CREATE INDEX idx_audit_log_transfer ON audit_log(transfer_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
