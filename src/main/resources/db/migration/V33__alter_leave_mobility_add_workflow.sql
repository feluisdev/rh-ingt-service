-- V33: add workflow columns to t_leave_mobility
ALTER TABLE t_leave_mobility
    ADD COLUMN IF NOT EXISTS status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS destination_unit_id UUID,
    ADD COLUMN IF NOT EXISTS justification       TEXT,
    ADD COLUMN IF NOT EXISTS document_id         UUID,
    ADD COLUMN IF NOT EXISTS rejection_reason    TEXT;
