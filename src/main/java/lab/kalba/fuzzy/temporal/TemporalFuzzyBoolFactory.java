package lab.kalba.fuzzy.temporal;

/*
 *  Temporal fuzzy logic API
 *  Kalba Lab, 2022-2024
 */

import lab.kalba.fuzzy.core.FuzzyBool;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Function;

/**
 * TemporalFuzzyBoolFactory is a "time machine" that can produce a FuzzyBool object
 * in state depends on time. The factory has a specific "time function" that defines
 * rules to produce FuzzyBool objects. Time function expects parameter of LocalDateTime
 * which can be interpreted as definite time.
 */
public class TemporalFuzzyBoolFactory implements ContainerFuzzyBoolTime {

    private final Function<LocalDateTime, FuzzyBool> timeFunction;

    /**
     * Constructor with the default time function (returns UNKNOWN)
     */
    public TemporalFuzzyBoolFactory() {
        this.timeFunction = time -> FuzzyBool.UNKNOWN;
    }

    /**
     * Constructor with custom time function
     */
    public TemporalFuzzyBoolFactory(Function<LocalDateTime, FuzzyBool> timeFunction) {
        this.timeFunction = Objects.requireNonNull(timeFunction);
    }

    /**
     * Get the time function
     */
    public Function<LocalDateTime, FuzzyBool> getTimeFunction() {
        return timeFunction;
    }

    /**
     * Produces FuzzyBool object according to the time function
     * @param time is definite time
     * @return FuzzyBool object in state according to the time function and definite time
     */
    @Override
    public FuzzyBool get(LocalDateTime time) {
        Objects.requireNonNull(time);
        return timeFunction.apply(time);
    }

    /**
     * Produces FuzzyBool object for current time
     * @return FuzzyBool object in state according to the time function and current time
     */
    public FuzzyBool now() {
        return get(LocalDateTime.now());
    }

    /**
     * Creates new factory that combines this and other with AND operation
     */
    public TemporalFuzzyBoolFactory and(TemporalFuzzyBoolFactory other) {
        Objects.requireNonNull(other);
        return new TemporalFuzzyBoolFactory(
            time -> this.get(time).and(other.get(time))
        );
    }

    /**
     * Creates new factory that combines this and other with OR operation
     */
    public TemporalFuzzyBoolFactory or(TemporalFuzzyBoolFactory other) {
        Objects.requireNonNull(other);
        return new TemporalFuzzyBoolFactory(
            time -> this.get(time).or(other.get(time))
        );
    }

    /**
     * Creates new factory that negates this factory's result
     */
    public TemporalFuzzyBoolFactory not() {
        return new TemporalFuzzyBoolFactory(
            time -> this.get(time).not()
        );
    }

}
