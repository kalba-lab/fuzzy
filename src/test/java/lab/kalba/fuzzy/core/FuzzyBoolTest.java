package lab.kalba.fuzzy.core;

import lab.kalba.fuzzy.trigger.TriggerFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FuzzyBool")
class FuzzyBoolTest {

    @Test
    @DisplayName("should create with valid value")
    void shouldCreateWithValidValue() {
        FuzzyBool fb = FuzzyBool.of(0.5f);
        assertEquals(0.5f, fb.value());
    }

    @Test
    @DisplayName("should return cached constants")
    void shouldReturnCachedConstants() {
        assertSame(FuzzyBool.TRUE, FuzzyBool.of(1.0f));
        assertSame(FuzzyBool.FALSE, FuzzyBool.of(-1.0f));
        assertSame(FuzzyBool.UNKNOWN, FuzzyBool.of(0.0f));
    }

    @Test
    @DisplayName("should throw for invalid values")
    void shouldThrowForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> FuzzyBool.of(1.5f));
        assertThrows(IllegalArgumentException.class, () -> FuzzyBool.of(-1.5f));
    }

    @Test
    @DisplayName("NOT should negate value")
    void notShouldNegateValue() {
        assertEquals(-0.7f, FuzzyBool.of(0.7f).not().value(), 0.01f);
        assertEquals(0.7f, FuzzyBool.of(-0.7f).not().value(), 0.01f);
        assertEquals(0.0f, FuzzyBool.UNKNOWN.not().value());
    }

    @Test
    @DisplayName("AND should calculate product with sign")
    void andShouldCalculateProduct() {
        assertEquals(0.4f, FuzzyBool.of(0.8f).and(0.5f).value(), 0.01f);
        assertEquals(-0.4f, FuzzyBool.of(0.8f).and(-0.5f).value(), 0.01f);
        assertEquals(0.0f, FuzzyBool.of(0.8f).and(0.0f).value());
    }

    @Test
    @DisplayName("OR should return max or sum")
    void orShouldReturnMaxOrSum() {
        assertEquals(0.8f, FuzzyBool.of(0.8f).or(0.5f).value());
        assertEquals(0.5f, FuzzyBool.of(0.0f).or(0.5f).value());
    }

    @Test
    @DisplayName("trigger should convert to boolean")
    void triggerShouldConvertToBoolean() {
        assertTrue(FuzzyBool.TRUE.trigger());
        assertFalse(FuzzyBool.of(0.99f).trigger());
        assertTrue(FuzzyBool.of(0.7f).trigger(TriggerFunction.POSITIVE));
        assertTrue(FuzzyBool.of(0.7f).trigger(TriggerFunction.MAJORITY));
    }

    @Test
    @DisplayName("utility methods should work")
    void utilityMethodsShouldWork() {
        assertTrue(FuzzyBool.of(0.5f).isPositive());
        assertTrue(FuzzyBool.of(-0.5f).isNegative());
        assertTrue(FuzzyBool.UNKNOWN.isUncertain());
        assertTrue(FuzzyBool.TRUE.isAbsolutelyTrue());
        assertTrue(FuzzyBool.FALSE.isAbsolutelyFalse());
    }

    @Test
    @DisplayName("equals and hashCode should work")
    void equalsAndHashCodeShouldWork() {
        FuzzyBool a = FuzzyBool.of(0.5f);
        FuzzyBool b = FuzzyBool.of(0.5f);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, FuzzyBool.of(0.6f));
    }

    @Test
    @DisplayName("builder should work")
    void builderShouldWork() {
        FuzzyBool fb = FuzzyBool.builder()
            .value(0.7f)
            .trigger(TriggerFunction.POSITIVE)
            .build();
        assertEquals(0.7f, fb.value());
        assertTrue(fb.trigger());
    }

    @Test
    @DisplayName("fluent API should work")
    void fluentApiShouldWork() {
        float result = FuzzyBool.of(0.8f)
            .and(0.5f)
            .or(0.3f)
            .not()
            .value();
        assertEquals(-0.4f, result, 0.01f);
    }
}