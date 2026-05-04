-- V35: Fix fk_eua_job to reference t_job instead of t_cargo
-- The V28 migration incorrectly pointed fk_eua_job to t_cargo (legacy table).
-- The correct target is t_job, which maps to JobEntity.

-- Drop the incorrect FK constraint
ALTER TABLE employee_unit_assignments
    DROP CONSTRAINT IF EXISTS fk_eua_job;

-- Recreate it pointing to the correct table
ALTER TABLE employee_unit_assignments
    ADD CONSTRAINT fk_eua_job FOREIGN KEY (job_id) REFERENCES t_job(id);
