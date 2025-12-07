package lab.kalba.fuzzy.temporal;

import lab.kalba.fuzzy.core.FuzzyBool;
import lab.kalba.fuzzy.trigger.TriggerFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TemporalFuzzyFactory")
class TemporalFuzzyFactoryTest {

    private static final LocalDateTime MONDAY_10AM = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final LocalDateTime MONDAY_9PM = LocalDateTime.of(2024, 1, 15, 21, 0);

    @Test
    @DisplayName("default factory should return UNKNOWN")
    void defaultFactoryShouldReturnUnknown() {
        TemporalFuzzyFactory factory = new TemporalFuzzyFactory();
        assertEquals(FuzzyBool.UNKNOWN, factory.get(LocalDateTime.now()));
    }

    @Test
    @DisplayName("constant factory should return same value")
    void constantFactoryShouldReturnSameValue() {
        TemporalFuzzyFactory factory = TemporalFuzzyFactory.constant(FuzzyBool.of(0.7f));
        assertEquals(0.7f, factory.get(MONDAY_10AM).value());
        assertEquals(0.7f, factory.get(MONDAY_9PM).value());
    }

    @Test
    @DisplayName("duringHours should work")
    void duringHoursShouldWork() {
        TemporalFuzzyFactory factory = TemporalFuzzyFactory.duringHours(9, 18);
        assertEquals(FuzzyBool.TRUE, factory.get(MONDAY_10AM));
        assertEquals(FuzzyBool.FALSE, factory.get(MONDAY_9PM));
    }

    @Test
    @DisplayName("hour-based builder should work")
    void hourBasedBuilderShouldWork() {
        TemporalFuzzyFactory factory = TemporalFuzzyFactory.builder()
            .name("businessHours")
            .hourBased()
            .whenHourBetween(9, 17, 1.0f)
            .whenHourBetween(17, 18, 0.5f)
            .otherwise(-1.0f)
            .build();

        assertEquals(1.0f, factory.get(MONDAY_10AM).value());
        assertEquals(-1.0f, factory.get(MONDAY_9PM).value());
    }

    @Test
    @DisplayName("Tom and Sun scenario")
    void tomAndSunScenario() {
        TemporalFuzzyFactory sunHasSet = TemporalFuzzyFactory.builder()
            .name("sunHasSet")
            .hourBased()
            .when(h -> h >= 0 && h < 6, 1.0f)
            .when(h -> h >= 6 && h < 7, 0.5f)
            .when(h -> h >= 7 && h < 19, -1.0f)
            .when(h -> h >= 19 && h < 20, -0.5f)
            .when(h -> h >= 20 && h < 21, 0.4f)
            .when(h -> h >= 21 && h < 22, 0.6f)
            .when(h -> h >= 22, 0.9f)
            .otherwise(1.0f)
            .withTrigger(TriggerFunction.atOrAboveThreshold(0.6f))
            .build();

        TemporalFuzzyFactory tomIsGoingHome = TemporalFuzzyFactory.builder()
            .name("tomIsGoingHome")
            .hourBased()
            .when(h -> h >= 0 && h < 18, -1.0f)
            .when(h -> h >= 18 && h < 19, 0.5f)
            .when(h -> h >= 19 && h < 20, 0.6f)
            .when(h -> h >= 20 && h < 21, 0.7f)
            .when(h -> h >= 21 && h < 22, 0.9f)
            .when(h -> h >= 22 && h < 23, 0.8f)
            .when(h -> h >= 23, 0.4f)
            .otherwise(-1.0f)
            .withTrigger(TriggerFunction.atOrAboveThreshold(0.7f))
            .build();

        // 21:30 - good time to meet
        LocalDateTime evening = LocalDateTime.of(2024, 1, 15, 21, 30);
        assertTrue(sunHasSet.get(evening).trigger());
        assertTrue(tomIsGoingHome.get(evening).trigger());

        // 15:00 - bad time
        LocalDateTime afternoon = LocalDateTime.of(2024, 1, 15, 15, 0);
        assertFalse(sunHasSet.get(afternoon).trigger());
        assertFalse(tomIsGoingHome.get(afternoon).trigger());
    }

    @Test
    @DisplayName("AND composition should work")
    void andCompositionShouldWork() {
        TemporalFuzzyFactory a = TemporalFuzzyFactory.constant(FuzzyBool.of(0.8f));
        TemporalFuzzyFactory b = TemporalFuzzyFactory.constant(FuzzyBool.of(0.5f));
        assertEquals(0.4f, a.and(b).now().value(), 0.01f);
    }

    @Test
    @DisplayName("OR composition should work")
    void orCompositionShouldWork() {
        TemporalFuzzyFactory a = TemporalFuzzyFactory.constant(FuzzyBool.of(0.8f));
        TemporalFuzzyFactory b = TemporalFuzzyFactory.constant(FuzzyBool.of(0.5f));
        assertEquals(0.8f, a.or(b).now().value(), 0.01f);
    }
}