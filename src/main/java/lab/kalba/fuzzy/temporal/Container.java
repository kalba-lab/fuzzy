package lab.kalba.fuzzy.temporal;

/*
 *  Temporal fuzzy logic API
 *  Kalba Lab, 2022-2024
 */

/**
 * Interface for object container (factory).
 * Container can produce objects in state determined by a parameter.
 *
 * @param <O> is a type of "wrapped" objects
 * @param <T> is a parameter (dependency) that determines object state
 */
public interface Container<O, T> {

    /**
     * Method-factory that produces object in state determined by parameter
     * @param parameter determines the state of produced object
     * @return object of type O in the state determined by the parameter
     */
    O get(T parameter);

}
