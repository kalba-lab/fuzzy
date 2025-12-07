package lab.kalba.fuzzy.core;

/**
 * Core interface for fuzzy logic operations with signed truth values.
 * Truth values range from -1 (absolutely false) to +1 (absolutely true).
 */
public interface FuzzyValue {

    float MIN_VALUE = -1.0f;
    float MAX_VALUE = +1.0f;

    float value();

    FuzzyValue not();

    FuzzyValue and(FuzzyValue other);

    FuzzyValue and(float otherValue);

    FuzzyValue or(FuzzyValue other);

    FuzzyValue or(float otherValue);

    static boolean isValid(float value) {
        return value >= MIN_VALUE && value <= MAX_VALUE;
    }

    static float clamp(float value) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
    }
}