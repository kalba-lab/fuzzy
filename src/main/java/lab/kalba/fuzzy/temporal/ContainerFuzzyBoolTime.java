package lab.kalba.fuzzy.temporal;

/*
 *  Temporal fuzzy logic API
 *  Kalba Lab, 2022-2024
 */

import lab.kalba.fuzzy.core.FuzzyBool;
import java.time.LocalDateTime;

/**
 * Container that produces FuzzyBool objects depending on time.
 */
public interface ContainerFuzzyBoolTime extends Container<FuzzyBool, LocalDateTime> {

}
