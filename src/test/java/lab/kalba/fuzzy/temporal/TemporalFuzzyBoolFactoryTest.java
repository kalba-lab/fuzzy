package lab.kalba.fuzzy.temporal;

/*
 *  Temporal fuzzy logic API
 *  Kalba Lab, 2022-2024
 */

import lab.kalba.fuzzy.core.FuzzyBool;
import lab.kalba.fuzzy.trigger.TriggerFunction;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TemporalFuzzyBoolFactoryTest {

    private TemporalFuzzyBoolFactory temporalFuzzyBoolFactory;
    private final LocalDateTime SPECIFIC_TIME = LocalDateTime.now();

    @Test
    void defaultFactoryShouldReturnUnknown() {
        temporalFuzzyBoolFactory = new TemporalFuzzyBoolFactory();
        FuzzyBool result = temporalFuzzyBoolFactory.get(SPECIFIC_TIME);
        assertEquals(FuzzyBool.UNKNOWN, result);
    }

    @Test
    void shouldBeEquals() {
        temporalFuzzyBoolFactory = new TemporalFuzzyBoolFactory();
        FuzzyBool fuzzyBool1 = temporalFuzzyBoolFactory.get(SPECIFIC_TIME);
        FuzzyBool fuzzyBool2 = temporalFuzzyBoolFactory.get(SPECIFIC_TIME);
        assertEquals(fuzzyBool1, fuzzyBool2);
    }

    @Test
    void shouldBeNotEquals() {
        temporalFuzzyBoolFactory = new TemporalFuzzyBoolFactory(time -> {
            if (time.isAfter(SPECIFIC_TIME)) {
                return FuzzyBool.of(0.3f);
            } else {
                return FuzzyBool.of(-0.18f);
            }
        });

        FuzzyBool fuzzyBool1 = temporalFuzzyBoolFactory.get(SPECIFIC_TIME);
        FuzzyBool fuzzyBool2 = temporalFuzzyBoolFactory.get(SPECIFIC_TIME.plusHours(2));
        assertNotEquals(fuzzyBool1, fuzzyBool2);
    }

    @Test
    void nowShouldReturnCurrentTime() {
        temporalFuzzyBoolFactory = new TemporalFuzzyBoolFactory(time -> FuzzyBool.of(0.5f));
        FuzzyBool result = temporalFuzzyBoolFactory.now();
        assertEquals(0.5f, result.getTruth());
    }

    @Test
    void andCompositionShouldWork() {
        TemporalFuzzyBoolFactory factory1 = new TemporalFuzzyBoolFactory(time -> FuzzyBool.of(0.8f));
        TemporalFuzzyBoolFactory factory2 = new TemporalFuzzyBoolFactory(time -> FuzzyBool.of(0.5f));

        TemporalFuzzyBoolFactory combined = factory1.and(factory2);
        FuzzyBool result = combined.get(SPECIFIC_TIME);

        assertEquals(0.4f, result.getTruth());
    }

    @Test
    void orCompositionShouldWork() {
        TemporalFuzzyBoolFactory factory1 = new TemporalFuzzyBoolFactory(time -> FuzzyBool.of(0.3f));
        TemporalFuzzyBoolFactory factory2 = new TemporalFuzzyBoolFactory(time -> FuzzyBool.of(0.7f));

        TemporalFuzzyBoolFactory combined = factory1.or(factory2);
        FuzzyBool result = combined.get(SPECIFIC_TIME);

        assertEquals(0.7f, result.getTruth());
    }

    @Test
    void notCompositionShouldWork() {
        TemporalFuzzyBoolFactory factory = new TemporalFuzzyBoolFactory(time -> FuzzyBool.of(0.6f));

        TemporalFuzzyBoolFactory negated = factory.not();
        FuzzyBool result = negated.get(SPECIFIC_TIME);

        assertEquals(-0.6f, result.getTruth());
    }

    @Test
    void iAmGoingToMeetTom() {

        // object's name is a "predicate"
        TemporalFuzzyBoolFactory sunHasSet = new TemporalFuzzyBoolFactory(time -> {
            float truth = 0;
            int hour = time.getHour();

            if (isBetweenOpen(0,  hour, 5))  truth = +1.0f;
            if (isBetweenOpen(5,  hour, 6))  truth = +0.5f;
            if (isBetweenOpen(6,  hour, 19)) truth = -1.0f;
            if (isBetweenOpen(19, hour, 20)) truth = -0.5f;
            if (isBetweenOpen(20, hour, 21)) truth = +0.4f;
            if (isBetweenOpen(21, hour, 22)) truth = +0.6f;
            if (isBetweenOpen(22, hour, 23)) truth = +0.9f;
            if (isBetweenOpen(23, hour, 24)) truth = +1.0f;

            return FuzzyBool.of(truth, TriggerFunction.atOrAboveThreshold(0.6f));
        });

        // object's name is a "predicate"
        TemporalFuzzyBoolFactory tomIsGoingHome = new TemporalFuzzyBoolFactory(time -> {
            float truth = 0;
            int hour = time.getHour();

            if (isBetweenOpen(0,  hour, 18)) truth = -1.0f;
            if (isBetweenOpen(18, hour, 19)) truth = +0.5f;
            if (isBetweenOpen(19, hour, 20)) truth = +0.6f;
            if (isBetweenOpen(20, hour, 21)) truth = +0.7f;
            if (isBetweenOpen(21, hour, 22)) truth = +0.9f;
            if (isBetweenOpen(22, hour, 23)) truth = +0.8f;
            if (isBetweenOpen(23, hour, 24)) truth = +0.4f;

            return FuzzyBool.of(truth, TriggerFunction.atOrAboveThreshold(0.7f));
        });

        LocalDateTime testTime;

        // At 21:30 both conditions are true
        testTime = LocalDateTime.of(2022, 1, 1, 21, 30);
        assertTrue(sunHasSet.get(testTime).trigger() && tomIsGoingHome.get(testTime).trigger());

        // Using composed factory
        TemporalFuzzyBoolFactory goodTimeToMeet = sunHasSet.and(tomIsGoingHome);
        assertTrue(goodTimeToMeet.get(testTime).getTruth() >= 0.5f);

        // At 15:00 neither condition is true
        testTime = LocalDateTime.of(2022, 1, 1, 15, 0);
        assertFalse(sunHasSet.get(testTime).trigger() && tomIsGoingHome.get(testTime).trigger());
        assertTrue(goodTimeToMeet.get(testTime).getTruth() < 0);
    }

    /**
     * is x ∈ [a, b)
     */
    private boolean isBetweenOpen(int a, int x, int b) {
        return a <= x && x < b;
    }

}
