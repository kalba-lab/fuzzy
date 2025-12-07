package lab.kalba.fuzzy.temporal;

import lab.kalba.fuzzy.core.FuzzyBool;
import lab.kalba.fuzzy.trigger.TriggerFunction;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntPredicate;

/**
 * Factory for creating time-dependent fuzzy values.
 */
public class TemporalFuzzyFactory {

    private final Function<LocalDateTime, FuzzyBool> timeFunction;
    private final String name;

    public TemporalFuzzyFactory() {
        this(time -> FuzzyBool.UNKNOWN, "unnamed");
    }

    public TemporalFuzzyFactory(Function<LocalDateTime, FuzzyBool> timeFunction) {
        this(timeFunction, "unnamed");
    }

    public TemporalFuzzyFactory(Function<LocalDateTime, FuzzyBool> timeFunction, String name) {
        this.timeFunction = Objects.requireNonNull(timeFunction);
        this.name = Objects.requireNonNull(name);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static TemporalFuzzyFactory constant(FuzzyBool value) {
        Objects.requireNonNull(value);
        return new TemporalFuzzyFactory(time -> value, "constant(" + value.value() + ")");
    }

    public static TemporalFuzzyFactory duringHours(int startHour, int endHour) {
        return new TemporalFuzzyFactory(time -> {
            int hour = time.getHour();
            boolean inRange;
            if (startHour <= endHour) {
                inRange = hour >= startHour && hour < endHour;
            } else {
                inRange = hour >= startHour || hour < endHour;
            }
            return inRange ? FuzzyBool.TRUE : FuzzyBool.FALSE;
        }, "duringHours(" + startHour + "-" + endHour + ")");
    }

    public static TemporalFuzzyFactory onDays(DayOfWeek... days) {
        return new TemporalFuzzyFactory(time -> {
            DayOfWeek today = time.getDayOfWeek();
            for (DayOfWeek day : days) {
                if (day == today) return FuzzyBool.TRUE;
            }
            return FuzzyBool.FALSE;
        }, "onDays");
    }

    public FuzzyBool get(LocalDateTime time) {
        Objects.requireNonNull(time);
        return timeFunction.apply(time);
    }

    public FuzzyBool now() {
        return get(LocalDateTime.now());
    }

    public String getName() {
        return name;
    }

    public Function<LocalDateTime, FuzzyBool> getTimeFunction() {
        return timeFunction;
    }

    public TemporalFuzzyFactory and(TemporalFuzzyFactory other) {
        Objects.requireNonNull(other);
        return new TemporalFuzzyFactory(
            time -> this.get(time).and(other.get(time)),
            "(" + this.name + " AND " + other.name + ")"
        );
    }

    public TemporalFuzzyFactory or(TemporalFuzzyFactory other) {
        Objects.requireNonNull(other);
        return new TemporalFuzzyFactory(
            time -> this.get(time).or(other.get(time)),
            "(" + this.name + " OR " + other.name + ")"
        );
    }

    public TemporalFuzzyFactory not() {
        return new TemporalFuzzyFactory(
            time -> this.get(time).not(),
            "NOT(" + this.name + ")"
        );
    }

    @Override
    public String toString() {
        return "TemporalFuzzyFactory[" + name + "]";
    }

    public static final class Builder {
        private String name = "unnamed";
        private Function<LocalDateTime, FuzzyBool> timeFunction;

        private Builder() {}

        public Builder name(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        public Builder withFunction(Function<LocalDateTime, FuzzyBool> function) {
            this.timeFunction = Objects.requireNonNull(function);
            return this;
        }

        public HourBasedBuilder hourBased() {
            return new HourBasedBuilder(this);
        }

        public TemporalFuzzyFactory build() {
            if (timeFunction == null) {
                timeFunction = time -> FuzzyBool.UNKNOWN;
            }
            return new TemporalFuzzyFactory(timeFunction, name);
        }
    }

    public static final class HourBasedBuilder {
        private final Builder parent;
        private final List<HourRule> rules = new ArrayList<>();
        private float defaultValue = 0.0f;
        private TriggerFunction trigger = TriggerFunction.EXACT_TRUE;

        private HourBasedBuilder(Builder parent) {
            this.parent = parent;
        }

        public HourBasedBuilder when(IntPredicate condition, float truthValue) {
            rules.add(new HourRule(condition, truthValue));
            return this;
        }

        public HourBasedBuilder whenHourBetween(int startHour, int endHour, float truthValue) {
            return when(h -> h >= startHour && h < endHour, truthValue);
        }

        public HourBasedBuilder otherwise(float truthValue) {
            this.defaultValue = truthValue;
            return this;
        }

        public HourBasedBuilder withTrigger(TriggerFunction trigger) {
            this.trigger = Objects.requireNonNull(trigger);
            return this;
        }

        public TemporalFuzzyFactory build() {
            parent.timeFunction = buildFunction();
            return parent.build();
        }

        private Function<LocalDateTime, FuzzyBool> buildFunction() {
            final List<HourRule> capturedRules = new ArrayList<>(rules);
            final float capturedDefault = defaultValue;
            final TriggerFunction capturedTrigger = trigger;

            return time -> {
                int hour = time.getHour();
                for (HourRule rule : capturedRules) {
                    if (rule.condition.test(hour)) {
                        return FuzzyBool.of(rule.truthValue, capturedTrigger);
                    }
                }
                return FuzzyBool.of(capturedDefault, capturedTrigger);
            };
        }

        private static class HourRule {
            final IntPredicate condition;
            final float truthValue;

            HourRule(IntPredicate condition, float truthValue) {
                this.condition = condition;
                this.truthValue = truthValue;
            }
        }
    }
}