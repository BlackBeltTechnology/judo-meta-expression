package hu.blackbelt.judo.meta.expression.runtime;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionUtils {

    private static final Logger log = LoggerFactory.getLogger(ExpressionUtils.class);

    private boolean failOnError;

    private ResourceSet resourceSet;

    public ExpressionUtils() {
    }


    public ExpressionUtils(final ResourceSet resourceSet) {
        this(resourceSet, false);
    }

    public ExpressionUtils(final ResourceSet resourceSet, final boolean failOnError) {
        this.resourceSet = resourceSet;
        this.failOnError = failOnError;

        // TODO: Processes here
    }

    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Get stream of source iterator.
     *
     * @param sourceIterator source iterator
     * @param parallel       flag controlling returned stream (serial or parallel)
     * @param <T>            type of source iterator
     * @return return serial (parallel = <code>false</code>) or parallel (parallel = <code>true</code>) stream
     */
    public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    /**
     * Get all model elements.
     *
     * @param <T> generic type of model elements
     * @return model elements
     */
    public <T> Stream<T> all() {
        return asStream((Iterator<T>) resourceSet.getAllContents(), false);
    }
    
    /**
     * Get model elements with specific type
     *
     * @param clazz class of model element types
     * @param <T>   specific type
     * @return all elements with clazz type
     */
    public <T> Stream<T> all(final Class<T> clazz) {
        return all().filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }
}
