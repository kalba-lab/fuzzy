package lab.kalba.fuzzy.core;

import lab.kalba.fuzzy.trigger.TriggerFunction;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Immutable implementation of fuzzy boolean with signed truth values [-1, +1].
 */
public final class FuzzyBool implements FuzzyValue {

    public static final FuzzyBool TRUE = new FuzzyBool(1.0f);
    public static final FuzzyBool FALSE = new FuzzyBool(-1.0f);
    public static final FuzzyBool UNKNOWN = new FuzzyBool(0.0f);

    private static final float PRECISION = 100.0f;

    private final float truth;
    private final TriggerFunction triggerFunction;

    public FuzzyBool(float truth) {
        this(truth, TriggerFunction.EXACT_TRUE);
    }

    public FuzzyBool(float truth, TriggerFunction triggerFunction) {
        if (!FuzzyValue.isValid(truth)) {
            throw new IllegalArgumentException(
                String.format("Truth value must be in range [-1, +1], got: %.4f", truth)
            );
        }
        this.truth = round(truth);
        this.triggerFunction = Objects.requireNonNull(triggerFunction);
    }

    public static FuzzyBool of(float truth) {
        if (truth == 1.0f) return TRUE;
        if (truth == -1.0f) return FALSE;
        if (truth == 0.0f) return UNKNOWN;
        return new FuzzyBool(truth);
    }

    public static FuzzyBool of(float truth, TriggerFunction trigger) {
        return new FuzzyBool(truth, trigger);
    }

    public static FuzzyBool fromBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public float value() {
        return truth;
    }

    @Override
    public FuzzyBool not() {
        if (truth == 0.0f) return UNKNOWN;
        return FuzzyBool.of(-truth, triggerFunction);
    }

    @Override
    public FuzzyBool and(FuzzyValue other) {
        Objects.requireNonNull(other);
        return and(other.value());
    }

    @Override
    public FuzzyBool and(float otherValue) {
        validateValue(otherValue);
        float product = Math.abs(truth * otherValue);
        float result = (truth < 0 || otherValue < 0) ? -product : product;
        return FuzzyBool.of(round(result), triggerFunction);
    }

    @Override
    public FuzzyBool or(FuzzyValue other) {
        Objects.requireNonNull(other);
        return or(other.value());
    }

    @Override
    public FuzzyBool or(float otherValue) {
        validateValue(otherValue);
        float result;
        if (truth != 0 && otherValue != 0) {
            result = Math.max(truth, otherValue);
        } else {
            result = truth + otherValue;
        }
        return FuzzyBool.of(round(result), triggerFunction);
    }

    public boolean trigger() {
        return triggerFunction.test(truth);
    }

    public boolean trigger(TriggerFunction trigger) {
        Objects.requireNonNull(trigger);
        return trigger.test(truth);
    }

    public boolean trigger(Predicate<Float> condition) {
        Objects.requireNonNull(condition);
        return condition.test(truth);
    }

    public FuzzyBool withTrigger(TriggerFunction newTrigger) {
        return new FuzzyBool(truth, newTrigger);
    }

    public TriggerFunction getTriggerFunction() {
        return triggerFunction;
    }

    public boolean isPositive() {
        return truth > 0;
    }

    public boolean isNegative() {
        return truth < 0;
    }

    public boolean isUncertain() {
        return truth == 0;
    }

    public boolean isAbsolutelyTrue() {
        return truth == 1.0f;
    }

    public boolean isAbsolutelyFalse() {
        return truth == -1.0f;
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

    private static float round(float value) {
        return Math.round(value * PRECISION) / PRECISION;
    }

    private static void validateValue(float value) {
        if (!FuzzyValue.isValid(value)) {
            throw new IllegalArgumentException(
                String.format("Value must be in range [-1, +1], got: %.4f", value)
            );
        }
    }

    public static final class Builder {
        private float value = 0.0f;
        private TriggerFunction trigger = TriggerFunction.EXACT_TRUE;

        private Builder() {}

        public Builder value(float value) {
            this.value = value;
            return this;
        }

        public Builder trigger(TriggerFunction trigger) {
            this.trigger = Objects.requireNonNull(trigger);
            return this;
        }

        public Builder trigger(Predicate<Float> condition) {
            Objects.requireNonNull(condition);
            this.trigger = condition::test;
            return this;
        }

        public FuzzyBool build() {
            return new FuzzyBool(value, trigger);
        }
    }
}