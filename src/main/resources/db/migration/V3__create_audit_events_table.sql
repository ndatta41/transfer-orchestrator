CREATE TABLE audit_events (
      id UUID PRIMARY KEY NOT NULL,
      transfer_id UUID NOT NULL REFERENCES transfers(id) ON DELETE CASCADE,
      action VARCHAR(255) NOT NULL,
      timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
      actor VARCHAR(255) NOT NULL,
      metadata TEXT
);

CREATE INDEX idx_audit_transfer ON audit_events(transfer_id);
CREATE INDEX idx_audit_timestamp ON audit_events(timestamp);

