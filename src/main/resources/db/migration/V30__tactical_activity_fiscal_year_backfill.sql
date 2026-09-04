-- Migration: V30__tactical_activity_fiscal_year_backfill.sql
-- Phase 93 (INT-01) / finding ACH-C-02.
--
-- The `fiscal_year` column on t_tactical_activities already exists (it is mapped by
-- TacticalActivitiesEntity and queried by CreateScenarioCommandHandler through
-- findAllByFiscalYear / findAllByFiscalYearAndOrganicUnitId). What never existed was
-- anything that WROTE it: no production command set it, so every activity created through
-- the application carried NULL forever and every scenario simulation returned zero
-- activities. The write path is fixed in code by a @PrePersist/@PreUpdate callback on the
-- entity; this migration repairs the rows that already exist.
--
-- Without this backfill the code fix alone would only help activities touched after the
-- deploy: historical activities would stay invisible to the simulation until someone
-- happened to edit them, which is exactly the kind of half-fix that reads as fixed and
-- behaves as broken.
--
-- Cabo Verde's fiscal year is the calendar year, so start_date's year is the fiscal year --
-- the same rule the entity callback applies, kept deliberately identical here.
--
-- Idempotent: only touches rows where fiscal_year IS NULL, so re-running changes nothing.
-- Rows with a NULL start_date are left alone; there is nothing to derive from, and guessing
-- would put activities in a year no one chose.

UPDATE t_tactical_activities
   SET fiscal_year = EXTRACT(YEAR FROM start_date)::INTEGER
 WHERE fiscal_year IS NULL
   AND start_date IS NOT NULL;

-- The Envers audit shadow is deliberately NOT backfilled. Its rows record what each
-- revision actually looked like at the time, and fiscal_year genuinely was NULL then.
-- Rewriting history to match the present would make the audit trail lie about the very
-- defect this migration repairs.
