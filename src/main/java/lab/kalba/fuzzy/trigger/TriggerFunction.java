package lab.kalba.fuzzy.trigger;

/*
 *  Temporal fuzzy logic API
 *  Kalba Lab, 2022-2024
 */

import java.util.function.Predicate;

/**
 * TriggerFunction is a bridge between fuzzy value and traditional boolean logic.
 * It determines when a fuzzy value can be interpreted as "true".
 */
@FunctionalInterface
public interface TriggerFunction extends Predicate<Float> {

    /** Returns true only when value is exactly +1.0 */
    TriggerFunction EXACT_TRUE = v -> v == 1.0f;

    /** Returns true when value is positive (v > 0) */
    TriggerFunction POSITIVE = v -> v > 0;

    /** Returns true when value is non-negative (v >= 0) */
    TriggerFunction NON_NEGATIVE = v -> v >= 0;

    /** Returns true when value is greater than 0.5 */
    TriggerFunction MAJORITY = v -> v > 0.5f;

    /** Returns true when value is at least 0.7 */
    TriggerFunction STRONG = v -> v >= 0.7f;

    /** Always returns true */
    TriggerFunction ALWAYS_TRUE = v -> true;

    /** Always returns false */
    TriggerFunction ALWAYS_FALSE = v -> false;

    /**
     * Creates trigger that returns true when value > threshold
     */
    static TriggerFunction aboveThreshold(float threshold) {
        return v -> v > threshold;
    }

    /**
     * Creates trigger that returns true when value >= threshold
     */
    static TriggerFunction atOrAboveThreshold(float threshold) {
        return v -> v >= threshold;
    }

    /**
     * Creates trigger that returns true when value < threshold
     */
    static TriggerFunction belowThreshold(float threshold) {
        return v -> v < threshold;
    }

    /**
     * Creates trigger that returns true when value is in [min, max]
     */
    static TriggerFunction inRange(float min, float max) {
        return v -> v >= min && v <= max;
    }

}
