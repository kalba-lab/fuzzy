package lab.kalba.fuzzy.core;

/*
 *  Temporal fuzzy logic API
 *  Kalba Lab, 2022-2024
 */

import lab.kalba.fuzzy.trigger.TriggerFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FuzzyBoolTest {

    // Creation tests

    @Test
    void shouldCreateWithValue() {
        FuzzyBool fb = new FuzzyBool(0.5f);
        assertEquals(0.5f, fb.getTruth());
    }

    @Test
    void shouldCreateWithFactory() {
        FuzzyBool fb = FuzzyBool.of(0.5f);
        assertEquals(0.5f, fb.getTruth());
    }

    @Test
    void shouldReturnConstantsForSpecialValues() {
        assertSame(FuzzyBool.TRUE, FuzzyBool.of(1.0f));
        assertSame(FuzzyBool.FALSE, FuzzyBool.of(-1.0f));
        assertSame(FuzzyBool.UNKNOWN, FuzzyBool.of(0.0f));
    }

    @Test
    void shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new FuzzyBool(-1.1f));
        assertThrows(IllegalArgumentException.class, () -> new FuzzyBool(+2.144f));
        assertThrows(IllegalArgumentException.class, () -> new FuzzyBool(+1.0001f));
    }

    // NOT operation

    @Test
    void notOperationShouldBeSymmetric() {
        assertEquals(+1.0f, FuzzyBool.of(-1.0f).fuzzyNot());
        assertEquals(-1.0f, FuzzyBool.of(+1.0f).fuzzyNot());
        assertEquals(-0.8f, FuzzyBool.of(+0.8f).fuzzyNot());
    }

    @Test
    void notMethodShouldReturnNewFuzzyBool() {
        FuzzyBool original = FuzzyBool.of(0.8f);
        FuzzyBool negated = original.not();

        assertEquals(0.8f, original.getTruth());  // original unchanged
        assertEquals(-0.8f, negated.getTruth());
    }

    @Test
    void notOfZeroShouldBeZero() {
        assertEquals(0.0f, FuzzyBool.UNKNOWN.fuzzyNot());
        assertSame(FuzzyBool.UNKNOWN, FuzzyBool.UNKNOWN.not());
    }

    // AND operation

    @Test
    void andWithZeroShouldBeZero() {
        assertEquals(0.0f, FuzzyBool.of(+1.0f).fuzzyAnd(0.0f));
        assertEquals(0.0f, FuzzyBool.of(0.0f).fuzzyAnd(0.5f));
    }

    @Test
    void andWithNegativeShouldBeNegative() {
        assertEquals(-1.0f, FuzzyBool.of(+1.0f).fuzzyAnd(-1.0f));
        assertEquals(-1.0f, FuzzyBool.of(-1.0f).fuzzyAnd(-1.0f));
        assertEquals(-1.0f, FuzzyBool.of(-1.0f).fuzzyAnd(+1.0f));
    }

    @Test
    void andShouldReturnProduct() {
        assertEquals(+0.36f, FuzzyBool.of(+0.9f).fuzzyAnd(+0.4f));
        assertEquals(-0.36f, FuzzyBool.of(-0.9f).fuzzyAnd(+0.4f));
        assertEquals(+0.47f, FuzzyBool.of(+0.99f).fuzzyAnd(+0.47f));
        assertEquals(-0.46f, FuzzyBool.of(-0.91f).fuzzyAnd(-0.5f));
    }

    @Test
    void andMethodShouldReturnNewFuzzyBool() {
        FuzzyBool a = FuzzyBool.of(0.8f);
        FuzzyBool b = FuzzyBool.of(0.5f);
        FuzzyBool result = a.and(b);

        assertEquals(0.8f, a.getTruth());  // original unchanged
        assertEquals(0.5f, b.getTruth());  // original unchanged
        assertEquals(0.4f, result.getTruth());
    }

    // OR operation

    @Test
    void orShouldReturnMax() {
        assertEquals(+0.4f, FuzzyBool.of(+0.4f).fuzzyOr(-0.4f));
        assertEquals(+0.84f, FuzzyBool.of(+0.84f).fuzzyOr(+0.76f));
    }

    @Test
    void orWithZeroShouldReturnSum() {
        assertEquals(-1.0f, FuzzyBool.of(-1.0f).fuzzyOr(0.0f));
        assertEquals(+0.5f, FuzzyBool.of(0.0f).fuzzyOr(+0.5f));
    }

    @Test
    void orMethodShouldReturnNewFuzzyBool() {
        FuzzyBool a = FuzzyBool.of(0.3f);
        FuzzyBool b = FuzzyBool.of(0.7f);
        FuzzyBool result = a.or(b);

        assertEquals(0.3f, a.getTruth());  // original unchanged
        assertEquals(0.7f, b.getTruth());  // original unchanged
        assertEquals(0.7f, result.getTruth());
    }

    // Trigger tests

    @Test
    void defaultTriggerShouldReturnTrueOnlyForOne() {
        assertTrue(FuzzyBool.TRUE.trigger());
        assertFalse(FuzzyBool.of(0.99f).trigger());
        assertFalse(FuzzyBool.UNKNOWN.trigger());
        assertFalse(FuzzyBool.FALSE.trigger());
    }

    @Test
    void triggerWithFunctionShouldWork() {
        FuzzyBool fb = FuzzyBool.of(0.6f);

        assertTrue(fb.trigger(TriggerFunction.POSITIVE));
        assertTrue(fb.trigger(TriggerFunction.MAJORITY));
        assertFalse(fb.trigger(TriggerFunction.STRONG));
        assertFalse(fb.trigger(TriggerFunction.EXACT_TRUE));
    }

    @Test
    void triggerWithCustomThreshold() {
        FuzzyBool fb = FuzzyBool.of(0.75f);

        assertTrue(fb.trigger(TriggerFunction.aboveThreshold(0.5f)));
        assertTrue(fb.trigger(TriggerFunction.atOrAboveThreshold(0.75f)));
        assertFalse(fb.trigger(TriggerFunction.aboveThreshold(0.75f)));
    }

    // State check methods

    @Test
    void shouldDetectPositiveNegativeUnknown() {
        assertTrue(FuzzyBool.of(0.5f).isPositive());
        assertFalse(FuzzyBool.of(0.5f).isNegative());
        assertFalse(FuzzyBool.of(0.5f).isUnknown());

        assertFalse(FuzzyBool.of(-0.5f).isPositive());
        assertTrue(FuzzyBool.of(-0.5f).isNegative());
        assertFalse(FuzzyBool.of(-0.5f).isUnknown());

        assertFalse(FuzzyBool.UNKNOWN.isPositive());
        assertFalse(FuzzyBool.UNKNOWN.isNegative());
        assertTrue(FuzzyBool.UNKNOWN.isUnknown());
    }

    // Equality tests

    @Test
    void shouldBeEqualIfSameTruth() {
        FuzzyBool fb1 = FuzzyBool.of(0.5f);
        FuzzyBool fb2 = FuzzyBool.of(0.5f);
        assertEquals(fb1, fb2);
    }

    @Test
    void shouldNotBeEqualIfDifferentTruth() {
        FuzzyBool fb1 = FuzzyBool.of(0.5f);
        FuzzyBool fb2 = FuzzyBool.of(0.6f);
        assertNotEquals(fb1, fb2);
    }

    // fromBoolean tests

    @Test
    void fromBooleanShouldWork() {
        assertSame(FuzzyBool.TRUE, FuzzyBool.fromBoolean(true));
        assertSame(FuzzyBool.FALSE, FuzzyBool.fromBoolean(false));
    }

    // withTrigger tests

    @Test
    void withTriggerShouldCreateNewInstance() {
        FuzzyBool original = FuzzyBool.of(0.8f);
        FuzzyBool withPositive = original.withTrigger(TriggerFunction.POSITIVE);

        assertFalse(original.trigger());  // EXACT_TRUE
        assertTrue(withPositive.trigger());  // POSITIVE
        assertEquals(original.getTruth(), withPositive.getTruth());
    }

}
