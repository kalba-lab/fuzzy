package lab.kalba.fuzzy.trigger;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Functional interface for converting fuzzy truth values to boolean.
 */
@FunctionalInterface
public interface TriggerFunction extends Predicate<Float> {

    TriggerFunction EXACT_TRUE = v -> v == 1.0f;
    TriggerFunction POSITIVE = v -> v > 0;
    TriggerFunction NON_NEGATIVE = v -> v >= 0;
    TriggerFunction MAJORITY = v -> v > 0.5f;
    TriggerFunction STRONG = v -> v >= 0.7f;
    TriggerFunction ALWAYS_TRUE = v -> true;
    TriggerFunction ALWAYS_FALSE = v -> false;

    static TriggerFunction aboveThreshold(float threshold) {
        return v -> v > threshold;
    }

    static TriggerFunction atOrAboveThreshold(float threshold) {
        return v -> v >= threshold;
    }

    static TriggerFunction belowThreshold(float threshold) {
        return v -> v < threshold;
    }

    static TriggerFunction inRange(float min, float max) {
        return v -> v >= min && v <= max;
    }

    static TriggerFunction approximately(float target, float epsilon) {
        return v -> Math.abs(v - target) <= epsilon;
    }

    default TriggerFunction and(TriggerFunction other) {
        Objects.requireNonNull(other);
        return v -> this.test(v) && other.test(v);
    }

    default TriggerFunction or(TriggerFunction other) {
        Objects.requireNonNull(other);
        return v -> this.test(v) || other.test(v);
    }

    default TriggerFunction negate() {
        return v -> !this.test(v);
    }
}