package hu.blackbelt.judo.meta.expression.adapters.measure;

/**
 * Handler of measure changes. This interface can be used to keep dimension cache in sync.
 *
 * @param <M> base measure type
 */
public interface MeasureChangedHandler<M> {

    /**
     * Measure is added.
     *
     * @param measure measure
     */
    void measureAdded(M measure);

    /**
     * Measure is changed.
     *
     * @param measure measure
     */
    void measureChanged(M measure);

    /**
     * Measure is removed.
     *
     * @param measure measure
     */
    void measureRemoved(M measure);
}
