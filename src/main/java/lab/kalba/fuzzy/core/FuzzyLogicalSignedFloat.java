package lab.kalba.fuzzy.core;

/*
 *  Temporal fuzzy logic API
 *  Kalba Lab, 2022-2024
 */

/**
 * Fuzzy logic with signed float values in range [-1, +1]
 */
public interface FuzzyLogicalSignedFloat extends FuzzyLogical<Float> {

    float MIN_VALUE = -1.0f;
    float MAX_VALUE = +1.0f;

    /**
     * Check validity of argument.
     * Value should be in between -1 and +1, i.e. [-1, +1]
     * @param value is an argument
     * @return true when argument is in [-1, +1]
     */
    default boolean isValueValid(Float value) {
        return value >= MIN_VALUE && value <= MAX_VALUE;
    }

}
