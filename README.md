## Temporal Fuzzy Logic
#### _API for temporal fuzzy logic with signed Float values in [-1, +1]_

Fuzzy temporal logic is a fascinating approach to present data with variable and fuzzy truth.
The API is built on the following preconditions:

1. **Truth values** are Float in range [-1, +1], rounded to two decimal places.

2. **Operation NOT** defined as:
   _not a ≡ -a, where a ∈ [-1, 1]_

3. **Operation AND** defined as:
   _a AND b ≡ if (a < 0 || b < 0) then -|a × b| else |a × b|_

4. **Operation OR** defined as:
   _a OR b ≡ if (a ≠ 0 && b ≠ 0) then Max(a, b) else a + b_

5. **Trigger** is a bridge between fuzzy value and traditional boolean logic.
   It uses a lambda function to determine when a fuzzy value can be interpreted as true.

6. **TemporalFuzzyBoolFactory** is a "time machine" that produces FuzzyBool objects
   in states depending on time. The factory uses a time function that defines rules
   to produce objects with different states based on LocalDateTime.

### Architecture

```
Container<O, T>                    — generic object factory
    ↓
ContainerFuzzyBoolTime             — specialization for FuzzyBool + LocalDateTime
    ↓
TemporalFuzzyBoolFactory           — "time machine" implementation


FuzzyLogical<T>                    — generic fuzzy algebra
    ↓
FuzzyLogicalSignedFloat            — specialization for Float[-1, +1]
    ↓
FuzzyBool                          — immutable fuzzy boolean
```

### Usage

```java
// Simple FuzzyBool
FuzzyBool value = FuzzyBool.of(0.7f);
FuzzyBool result = value.and(FuzzyBool.of(0.5f));  // 0.35

// With trigger
boolean decision = value.trigger(TriggerFunction.STRONG);  // true if >= 0.7

// Time-dependent factory
TemporalFuzzyBoolFactory storeIsOpen = new TemporalFuzzyBoolFactory(time -> {
    int hour = time.getHour();
    if (hour >= 9 && hour < 18) return FuzzyBool.TRUE;
    return FuzzyBool.FALSE;
});

FuzzyBool openNow = storeIsOpen.now();

// Compose factories
TemporalFuzzyBoolFactory condition1 = ...;
TemporalFuzzyBoolFactory condition2 = ...;
TemporalFuzzyBoolFactory both = condition1.and(condition2);
```

### Build

```bash
mvn clean test
```

### Requirements

- Java 17+
- Maven 3.6+

---
_lab.kalba, 2022-2024_
