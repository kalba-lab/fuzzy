package lab.kalba.fuzzy.core;

/*
 *  Temporal fuzzy logic API
 *  Kalba Lab, 2022-2024
 */

/**
 * The common fuzzy logic algebra
 * @param <T> is a base type for fuzzy logic realization
 */
public interface FuzzyLogical<T> {

    /**
     * Logical operation NOT
     * @return result of operation in base type (fuzzy value)
     */
    T fuzzyNot();

    /**
     * Logical operation AND
     * @param secondOperandValue is a value of second (right) variable in operation,
     *                           while first operand is an instance of class
     * @return result of operation in base type (fuzzy value)
     */
    T fuzzyAnd(T secondOperandValue);

    /**
     * Logical operation OR
     * @param secondOperandValue is a value of second (right) variable in operation,
     *                           while first operand is an instance of class
     * @return result of operation in base type (fuzzy value)
     */
    T fuzzyOr(T secondOperandValue);

    /**
     * The trigger is a "bridge" between fuzzy and traditional 2-values boolean logic.
     * Trigger uses the trigger function for determining a dependence returned result
     * on fuzzy value (an argument)
     * @param fuzzyValue is a current value of truth
     * @return true if an argument has value that can be interpreted as "true"
     *         according to the trigger function
     */
    boolean trigger(T fuzzyValue);

}
