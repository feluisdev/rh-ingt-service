package cv.igrp.RH_Service.shared.infrastructure.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/**
 * Reusable predicates for JPA Specifications.
 *
 * code  → exact, case-insensitive match (uses = with lower())
 * nome  → pg_trgm word_similarity (requires pg_trgm extension in PostgreSQL)
 *         word_similarity(term, field) > 0.3
 *         Unlike similarity(), this measures how well the search term matches
 *         the best contiguous substring of the field — suitable for finding
 *         short words inside longer names.
 */
public final class SearchSpecificationHelper {

    public static final double SIMILARITY_THRESHOLD = 0.3;

    private SearchSpecificationHelper() {}

    public static Predicate exactCode(CriteriaBuilder cb, Path<String> field, String value) {
        return cb.equal(cb.lower(field), value.trim().toLowerCase());
    }

    public static Predicate nameSimilarity(CriteriaBuilder cb, Path<String> field, String value) {
        return cb.greaterThan(
            cb.function("word_similarity", Double.class,
                cb.literal(value.trim().toLowerCase()),
                cb.lower(field)
            ),
            SIMILARITY_THRESHOLD
        );
    }
}
