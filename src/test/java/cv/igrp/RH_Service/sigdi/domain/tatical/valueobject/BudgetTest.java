package cv.igrp.RH_Service.sigdi.domain.tatical.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.FuelParams;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Budget.equals() is deliberately scale-insensitive and TacticalActivity.java:385-389 depends on
 * it -- the PAA-02 guard compares budgets with Objects.equals and must not demote an APPROVED
 * activity when 1000 is re-sent as 1000.00. TacticalActivityTest.java:61-76 asserts that
 * behaviour. This file adds the missing half of the pair: the hashCode contract. Reverting the
 * canonicalisation in Budget.hashCode() puts equal budgets in different buckets, which is what
 * FUT-08 / DEB-01 names.
 */
class BudgetTest {

    private static final String CLASSIFIER = "02.02.01";

    @Test
    void equalsIgnoresScale() {
        // Passes before the fix and must keep passing: it is the local expression of the
        // behaviour TacticalActivityTest.java:61-76 protects.
        Budget thousand = Budget.of(new BigDecimal("1000"), CLASSIFIER);
        Budget thousandWithCents = Budget.of(new BigDecimal("1000.00"), CLASSIFIER);

        assertEquals(thousand, thousandWithCents);
    }

    @Test
    void equalObjectsShareHashCode() {
        Budget a = Budget.of(new BigDecimal("1000"), CLASSIFIER);
        Budget b = Budget.of(new BigDecimal("1000.00"), CLASSIFIER);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalObjectsShareHashCodeAcrossScales() {
        String[] literals = {"10", "10.0", "10.00", "1E+1"};

        for (String left : literals) {
            for (String right : literals) {
                Budget leftBudget = Budget.of(new BigDecimal(left), CLASSIFIER);
                Budget rightBudget = Budget.of(new BigDecimal(right), CLASSIFIER);

                assertEquals(leftBudget, rightBudget,
                        () -> left + " and " + right + " represent the same amount");
                assertEquals(leftBudget.hashCode(), rightBudget.hashCode(),
                        () -> "hashCode differs between " + left + " and " + right
                                + " although equals() reports them equal");
            }
        }
    }

    @Test
    void hashSetTreatsEqualBudgetsAsOne() {
        Budget thousand = Budget.of(new BigDecimal("1000"), CLASSIFIER);
        Budget thousandWithCents = Budget.of(new BigDecimal("1000.00"), CLASSIFIER);

        Set<Budget> forward = new HashSet<>();
        forward.add(thousand);
        forward.add(thousandWithCents);

        Set<Budget> reversed = new HashSet<>();
        reversed.add(thousandWithCents);
        reversed.add(thousand);

        assertEquals(1, forward.size(),
                () -> "1000 then 1000.00 collapsed to " + forward.size() + " entries");
        assertEquals(1, reversed.size(),
                () -> "1000.00 then 1000 collapsed to " + reversed.size() + " entries");
        assertTrue(forward.contains(thousandWithCents),
                () -> "the set must find an equal budget written with another scale");
    }

    @Test
    void differentClassifierIsNotEqual() {
        Budget first = Budget.of(new BigDecimal("1000"), CLASSIFIER);
        Budget second = Budget.of(new BigDecimal("1000"), "02.02.02");

        assertNotEquals(first, second);
    }

    @Test
    void fuelParamsEqualsIgnoresScale() {
        // FuelParams carries the same equals/hashCode split as Budget
        // (FuelParams.java:69-80). Asserted here rather than grepped for
        // "stripTrailingZeros": a token being present does not prove the hash became
        // coherent with equals.
        FuelParams plain = FuelParams.of(new BigDecimal("150"), new BigDecimal("0.08"));
        FuelParams scaled = FuelParams.of(new BigDecimal("150.00"), new BigDecimal("0.0800"));

        assertEquals(plain, scaled);
    }

    @Test
    void fuelParamsEqualObjectsShareHashCode() {
        FuelParams plain = FuelParams.of(new BigDecimal("150"), new BigDecimal("0.08"));
        FuelParams scaled = FuelParams.of(new BigDecimal("150.00"), new BigDecimal("0.0800"));

        assertEquals(plain, scaled);
        assertEquals(plain.hashCode(), scaled.hashCode());
    }
}
