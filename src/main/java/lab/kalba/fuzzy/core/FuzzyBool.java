package lab.kalba.fuzzy.core;

/*
 *  Temporal fuzzy logic API
 *  Kalba Lab, 2022-2024
 */

import lab.kalba.fuzzy.trigger.TriggerFunction;
import java.util.Objects;

/**
 * Immutable fuzzy boolean with signed truth values in [-1, +1].
 */
public final class FuzzyBool implements FuzzyLogicalSignedFloat {

    /** Constant for absolute truth (+1.0) */
    public static final FuzzyBool TRUE = new FuzzyBool(1.0f);

    /** Constant for absolute falsity (-1.0) */
    public static final FuzzyBool FALSE = new FuzzyBool(-1.0f);

    /** Constant for unknown/neutral state (0.0) */
    public static final FuzzyBool UNKNOWN = new FuzzyBool(0.0f);

    private static final float PRECISION = 100.0f;

    private final float truth;
    private final TriggerFunction triggerFunction;

    /**
     * Creates FuzzyBool with default trigger (EXACT_TRUE)
     */
    public FuzzyBool(float truth) {
        this(truth, TriggerFunction.EXACT_TRUE);
    }

    /**
     * Creates FuzzyBool with custom trigger function
     */
    public FuzzyBool(float truth, TriggerFunction triggerFunction) {
        if (!isValueValid(truth)) {
            throw new IllegalArgumentException("Value must be between -1.0F and +1.0F");
        }
        this.truth = round(truth);
        this.triggerFunction = Objects.requireNonNull(triggerFunction);
    }

    /**
     * Factory method with caching for common values
     */
    public static FuzzyBool of(float truth) {
        if (truth == 1.0f) return TRUE;
        if (truth == -1.0f) return FALSE;
        if (truth == 0.0f) return UNKNOWN;
        return new FuzzyBool(truth);
    }

    /**
     * Factory method with custom trigger
     */
    public static FuzzyBool of(float truth, TriggerFunction trigger) {
        return new FuzzyBool(truth, trigger);
    }

    /**
     * Convert boolean to FuzzyBool
     */
    public static FuzzyBool fromBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Get current truth value
     */
    public float getTruth() {
        return truth;
    }

    /**
     * Get trigger function
     */
    public TriggerFunction getTriggerFunction() {
        return triggerFunction;
    }

    /**
     * Create new FuzzyBool with different trigger
     */
    public FuzzyBool withTrigger(TriggerFunction newTrigger) {
        return new FuzzyBool(this.truth, newTrigger);
    }

    /**
     * Fuzzy logical operation NOT.
     * not a ≡ -a
     */
    @Override
    public Float fuzzyNot() {
        return (truth == 0.0f) ? 0.0f : -truth;
    }

    /**
     * Returns new FuzzyBool with negated value
     */
    public FuzzyBool not() {
        if (truth == 0.0f) return UNKNOWN;
        return FuzzyBool.of(-truth, triggerFunction);
    }

    /**
     * Fuzzy logical operation AND.
     * a AND b ≡ if (a < 0 || b < 0) then -|a × b| else |a × b|
     */
    @Override
    public Float fuzzyAnd(Float secondOperandValue) throws IllegalArgumentException {
        if (!isValueValid(secondOperandValue)) {
            throw new IllegalArgumentException("Argument must be between -1.0F and +1.0F");
        }
        float product = Math.abs(truth * secondOperandValue);
        product = round(product);
        return (truth < 0 || secondOperandValue < 0) ? -product : product;
    }

    /**
     * Returns new FuzzyBool as result of AND operation
     */
    public FuzzyBool and(FuzzyBool other) {
        Objects.requireNonNull(other);
        return FuzzyBool.of(fuzzyAnd(other.truth), triggerFunction);
    }

    /**
     * Returns new FuzzyBool as result of AND operation with float
     */
    public FuzzyBool and(float otherValue) {
        return FuzzyBool.of(fuzzyAnd(otherValue), triggerFunction);
    }

    /**
     * Fuzzy logical operation OR.
     * a OR b ≡ if (a ≠ 0 && b ≠ 0) then Max(a, b) else a + b
     */
    @Override
    public Float fuzzyOr(Float secondOperandValue) throws IllegalArgumentException {
        if (!isValueValid(secondOperandValue)) {
            throw new IllegalArgumentException("Argument must be between -1.0F and +1.0F");
        }
        if (truth != 0 && secondOperandValue != 0) {
            return Math.max(truth, secondOperandValue);
        }
        return truth + secondOperandValue;
    }

    /**
     * Returns new FuzzyBool as result of OR operation
     */
    public FuzzyBool or(FuzzyBool other) {
        Objects.requireNonNull(other);
        return FuzzyBool.of(fuzzyOr(other.truth), triggerFunction);
    }

    /**
     * Returns new FuzzyBool as result of OR operation with float
     */
    public FuzzyBool or(float otherValue) {
        return FuzzyBool.of(fuzzyOr(otherValue), triggerFunction);
    }

    /**
     * Trigger with given fuzzy value
     */
    @Override
    public boolean trigger(Float fuzzyValue) {
        if (!isValueValid(fuzzyValue)) {
            return false;
        }
        return triggerFunction.test(fuzzyValue);
    }

    /**
     * Trigger with current truth value using instance's trigger function
     */
    public boolean trigger() {
        return triggerFunction.test(this.truth);
    }

    /**
     * Trigger with current truth value using provided trigger function
     */
    public boolean trigger(TriggerFunction trigger) {
        Objects.requireNonNull(trigger);
        return trigger.test(this.truth);
    }

    /**
     * Check if value is positive (> 0)
     */
    public boolean isPositive() {
        return truth > 0;
    }

    /**
     * Check if value is negative (< 0)
     */
    public boolean isNegative() {
        return truth < 0;
    }

    /**
     * Check if value is zero (unknown)
     */
    public boolean isUnknown() {
        return truth == 0;
    }

    private static float round(float value) {
        return Math.round(value * PRECISION) / PRECISION;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FuzzyBool other = (FuzzyBool) obj;
        return Float.compare(other.truth, truth) == 0;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(truth);
    }

    @Override
    public String toString() {
        return String.format("FuzzyBool[%.2f]", truth);
    }

}
