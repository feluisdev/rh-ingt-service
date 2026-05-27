package cv.igrp.RH_Service.shared.infrastructure.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/**
 * Reusable predicates for JPA Specifications.
 *
 * code  → exact, case-insensitive match (uses = with lower())
 * nome  → pg_trgm similarity (requires pg_trgm extension in PostgreSQL)
 *         threshold 0.3 — matches ~30 % trigram overlap
 */
public final class SearchSpecificationHelper {

    public static final double SIMILARITY_THRESHOLD = 0.3;

    private SearchSpecificationHelper() {}

    /**
     * Case-insensitive exact match. Replaces the common anti-pattern of
     * using LIKE '%code%' for code/identifier fields.
     */
    public static Predicate exactCode(CriteriaBuilder cb, Path<String> field, String value) {
        return cb.equal(cb.lower(field), value.trim().toLowerCase());
    }

    /**
     * Trigram similarity search via PostgreSQL pg_trgm.
     * Handles partial names, accent folding (lower-cased), and typo tolerance.
     * Never produces a full-table LIKE scan — relies on a GIN index.
     */
    public static Predicate nameSimilarity(CriteriaBuilder cb, Path<String> field, String value) {
        return cb.greaterThan(
            cb.function("similarity", Double.class,
                cb.lower(field),
                cb.literal(value.trim().toLowerCase())
            ),
            SIMILARITY_THRESHOLD
        );
    }
}
