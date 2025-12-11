# Fuzzy Temporal Logic Library

Java library for fuzzy logic with signed truth values [-1, +1] and temporal operators.

## Concept

```
    Classical Logic          Fuzzy Logic [0,1]         Signed Fuzzy Logic [-1,+1]
    
         TRUE                    1.0 ─ TRUE                 +1.0 ─ TRUE
          │                       │                           │
          │                      0.5 ─ partially true        +0.5 ─ partially true
          │                       │                           │
          ▼                      0.0 ─ FALSE                  0.0 ─ UNKNOWN
        FALSE                                                 │
                                                            -0.5 ─ partially false
                                                              │
                                                            -1.0 ─ FALSE
```

**Key difference:** In classical fuzzy logic, 0 means "false". In signed fuzzy logic, 0 means "unknown" - neither true nor false. Negative values represent degrees of falsity.

## Truth Values

| Value | Meaning |
|-------|---------|
| +1.0 | Absolutely true |
| +0.5 | Partially true |
| 0.0 | Unknown |
| -0.5 | Partially false |
| -1.0 | Absolutely false |

## Operations

**NOT:**
```
not(a) = -a
```

**AND:**
```
a AND b = if (a < 0 OR b < 0) then -|a × b| else |a × b|
```

**OR:**
```
a OR b = if (a ≠ 0 AND b ≠ 0) then max(a, b) else a + b
```

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    TemporalFuzzyFactory                         │
│                                                                 │
│   ┌─────────────┐      time        ┌─────────────┐             │
│   │ LocalDateTime├─────────────────►│  FuzzyBool  │             │
│   └─────────────┘                  └──────┬──────┘             │
│                                           │                     │
└───────────────────────────────────────────┼─────────────────────┘
                                            │
                                            ▼
                              ┌─────────────────────────┐
                              │    TriggerFunction      │
                              │                         │
                              │  fuzzy value → boolean  │
                              └────────────┬────────────┘
                                           │
                                           ▼
                                    true / false
```

## API Reference

### FuzzyBool

**What is it?** A truth value that can be anything from -1 (absolutely false) to +1 (absolutely true), with 0 meaning "unknown".

**Why signed values?** Classical fuzzy logic uses [0, 1] where 0 means "false". But in real life, "false" and "unknown" are different things. When you meet a stranger, you don't distrust them (that would be negative) - you simply don't know yet (zero). Signed values let you distinguish between:
- I believe this is true (+)
- I believe this is false (−)
- I don't have enough information (0)

```java
// Creation
FuzzyBool.of(0.7f)                    // from value
FuzzyBool.of(0.7f, triggerFunction)   // with custom trigger
FuzzyBool.fromBoolean(true)           // TRUE or FALSE
FuzzyBool.TRUE                        // constant +1.0
FuzzyBool.FALSE                       // constant -1.0
FuzzyBool.UNKNOWN                     // constant 0.0

// Operations
a.not()                               // negation
a.and(b)                              // conjunction
a.and(0.5f)                           // conjunction with float
a.or(b)                               // disjunction
a.or(0.5f)                            // disjunction with float
a.value()                             // get float value

// Triggers (fuzzy → boolean)
a.trigger()                           // use default trigger
a.trigger(TriggerFunction.POSITIVE)   // value > 0
a.trigger(TriggerFunction.MAJORITY)   // value > 0.5
a.trigger(TriggerFunction.STRONG)     // value >= 0.7
a.trigger(v -> v > 0.6f)              // custom lambda

// Utility
a.isPositive()                        // value > 0
a.isNegative()                        // value < 0
a.isUncertain()                       // value == 0
```

### TriggerFunction

**What is it?** A bridge between fuzzy and boolean worlds.

**Why do we need it?** Fuzzy logic gives you nuanced values like 0.7 or -0.3. But real decisions are often binary: open the door or not, send the notification or not, buy or not. TriggerFunction defines *when* a fuzzy value becomes "true enough" to act on.

**Example:** You have confidence = 0.7. Is that enough?
- For a casual suggestion: yes (threshold > 0.5)
- For a medical diagnosis: no (threshold > 0.95)
- For a nuclear launch: definitely no (threshold = 1.0)

**Two places to set a trigger:**

1. **In the factory** - becomes the default for all values it produces:
```java
TemporalFuzzyFactory.builder()
    .hourBased()
    .when(h -> h >= 9, 1.0f)
    .withTrigger(TriggerFunction.MAJORITY)  // default trigger
    .build();
```

2. **When calling trigger()** — use or override:
```java
FuzzyBool result = factory.now();
result.trigger();                          // uses factory's default (MAJORITY)
result.trigger(TriggerFunction.STRONG);    // overrides with STRONG
```

**Predefined triggers:**

```java
// Predefined
TriggerFunction.EXACT_TRUE            // value == 1.0
TriggerFunction.POSITIVE              // value > 0
TriggerFunction.NON_NEGATIVE          // value >= 0
TriggerFunction.MAJORITY              // value > 0.5
TriggerFunction.STRONG                // value >= 0.7
TriggerFunction.ALWAYS_TRUE           // always true
TriggerFunction.ALWAYS_FALSE          // always false

// Factory methods
TriggerFunction.aboveThreshold(0.6f)       // value > 0.6
TriggerFunction.atOrAboveThreshold(0.6f)   // value >= 0.6
TriggerFunction.belowThreshold(0.3f)       // value < 0.3
TriggerFunction.inRange(0.3f, 0.8f)        // 0.3 <= value <= 0.8

// Composition
trigger1.and(trigger2)                // both must be true
trigger1.or(trigger2)                 // either is true
trigger1.negate()                     // invert
```

### TemporalFuzzyFactory

**What is it?** A "truth machine" that produces different fuzzy values depending on *when* you ask.

**Why do we need it?** Many real-world truths depend on time. "Is the store open?" is not a fixed fact - it's TRUE at 10am, FALSE at midnight, and PARTIALLY TRUE at 17:55 when they're closing soon. TemporalFuzzyFactory lets you model these time-dependent predicates and combine them with AND/OR/NOT.

```java
// Simple factories
new TemporalFuzzyFactory()                          // always returns UNKNOWN
TemporalFuzzyFactory.constant(FuzzyBool.of(0.5f))   // always same value
TemporalFuzzyFactory.duringHours(9, 18)             // TRUE during hours, FALSE otherwise
TemporalFuzzyFactory.onDays(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)  // TRUE on these days

// Builder with custom function
TemporalFuzzyFactory.builder()
    .name("myFactory")
    .withFunction(time -> {
        // time is LocalDateTime, return FuzzyBool
        if (time.getHour() >= 9) return FuzzyBool.TRUE;
        return FuzzyBool.FALSE;
    })
    .build();

// Builder with hour-based rules
TemporalFuzzyFactory.builder()
    .name("storeOpen")
    .hourBased()
    .when(h -> h >= 9 && h < 12, 1.0f)       // h is hour 0-23
    .when(h -> h >= 12 && h < 13, 0.5f)      // lunch break
    .whenHourBetween(13, 17, 1.0f)           // shortcut for range
    .otherwise(-1.0f)                         // default value
    .withTrigger(TriggerFunction.POSITIVE)   // optional trigger
    .build();

// Evaluation
factory.now()                          // evaluate at current time
factory.get(LocalDateTime.of(...))     // evaluate at specific time
factory.getName()                      // get factory name

// Composition
factory1.and(factory2)                 // both conditions
factory1.or(factory2)                  // either condition
factory1.not()                         // invert
```

## Usage

```java
import lab.kalba.fuzzy.core.FuzzyBool;
import lab.kalba.fuzzy.temporal.TemporalFuzzyFactory;
import lab.kalba.fuzzy.trigger.TriggerFunction;

// Basic operations
FuzzyBool a = FuzzyBool.of(0.8f);
FuzzyBool b = FuzzyBool.of(0.5f);

a.not();      // -0.8
a.and(b);     // 0.4
a.or(b);      // 0.8

// Convert to boolean
a.trigger(TriggerFunction.POSITIVE);   // true (0.8 > 0)
a.trigger(TriggerFunction.MAJORITY);   // true (0.8 > 0.5)
```

## Example: When to Meet a Friend

Tom wants to meet his friend outside, but only when:
- The sun has set (he doesn't like daylight)
- His friend is heading home from work

Both conditions are fuzzy and depend on time:

```
Hour:   0    6    12   18   21   24
        ├────┼────┼────┼────┼────┤
        
Sun:   +1.0      -1.0      +0.9      "sunHasSet"
        ████░░░░░░░░░░░░░░░████
        
Friend: -1.0           +0.9 +0.4     "friendIsGoingHome"  
        ░░░░░░░░░░░░░░░████████
        
Meet:   -1.0           +0.8          "goodTimeToMeet" (AND)
        ░░░░░░░░░░░░░░░████░░░
```

```java
// Sun position throughout the day
TemporalFuzzyFactory sunHasSet = TemporalFuzzyFactory.builder()
    .name("sunHasSet")
    .hourBased()
    .when(h -> h >= 0 && h < 6, 1.0f)    // night: definitely set
    .when(h -> h >= 6 && h < 7, 0.5f)    // dawn: partially
    .when(h -> h >= 7 && h < 19, -1.0f)  // day: definitely not
    .when(h -> h >= 19 && h < 20, -0.5f) // dusk: starting to set
    .when(h -> h >= 20 && h < 21, 0.4f)  // evening: mostly set
    .when(h -> h >= 21, 0.9f)            // night: almost fully set
    .otherwise(0.0f)
    .withTrigger(TriggerFunction.atOrAboveThreshold(0.6f))
    .build();

// Friend's commute schedule
TemporalFuzzyFactory friendIsGoingHome = TemporalFuzzyFactory.builder()
    .name("friendIsGoingHome")
    .hourBased()
    .when(h -> h >= 0 && h < 17, -1.0f)  // at work or sleeping
    .when(h -> h >= 17 && h < 18, 0.3f)  // might leave soon
    .when(h -> h >= 18 && h < 19, 0.7f)  // probably commuting
    .when(h -> h >= 19 && h < 21, 0.9f)  // likely on the way
    .when(h -> h >= 21, 0.4f)            // probably home already
    .otherwise(-1.0f)
    .withTrigger(TriggerFunction.atOrAboveThreshold(0.7f))
    .build();

// Combine: good time to meet = sun has set AND friend is going home
TemporalFuzzyFactory goodTimeToMeet = sunHasSet.and(friendIsGoingHome);

// Check at different times
LocalDateTime evening = LocalDateTime.now().withHour(20).withMinute(30);
FuzzyBool result = goodTimeToMeet.get(evening);

System.out.println("Truth value: " + result.value());  // ~0.36
System.out.println("Should meet: " + result.trigger(TriggerFunction.POSITIVE));  // true
```

## Building

```bash
mvn clean package
```

## Requirements

- Java 17+
- Maven 3.6+

## License

MIT
