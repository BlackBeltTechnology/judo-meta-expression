package hu.blackbelt.judo.meta.expression.runtime;

/*-
 * #%L
 * JUDO :: Expression :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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

    record ValueMethod(Class type, String name) {}

    private static final LoadingCache<ValueMethod, Optional<Method>> METHOD_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000) // Evict oldest entries after 10000 are cached
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public Optional<Method> load(ValueMethod key) throws IntrospectionException {
                    Method readMethod = new PropertyDescriptor(key.name(), key.type()).getReadMethod();
                    return Optional.ofNullable(readMethod);
                }
            });

    @SuppressWarnings("unchecked")
    public static <T, R> R getValue(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException("Input value cannot be null.");
        }

        ValueMethod key = new ValueMethod(value.getClass(), name);
        try {
            Optional<Method> optionalMethod = METHOD_CACHE.get(key);
            if (optionalMethod.isPresent()) {
                Method readMethod = optionalMethod.get();
                return (R) readMethod.invoke(value);
            } else {
                throw new IllegalArgumentException("No readable property '" + name + "' found for class " + value.getClass().getName());
            }

        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to introspect property '" + name + "'", e.getCause());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke getter for property '" + name + "'", e);
        }
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
