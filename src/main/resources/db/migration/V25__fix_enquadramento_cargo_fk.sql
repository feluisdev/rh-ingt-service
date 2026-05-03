-- Fix incorrect FK on employee_professional_assignments: cargo_id must reference t_job, not t_cargo.
ALTER TABLE employee_professional_assignments
    DROP CONSTRAINT IF EXISTS fk_enquadramento_cargo;

ALTER TABLE employee_professional_assignments
    ADD CONSTRAINT fk_enquadramento_cargo FOREIGN KEY (cargo_id) REFERENCES t_job(id);
