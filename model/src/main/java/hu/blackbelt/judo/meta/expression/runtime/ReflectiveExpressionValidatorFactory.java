package hu.blackbelt.judo.meta.expression.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A factory for creating {@link ExpressionValidatorExecutor} instances using reflection.
 * <p>
 * This utility class dynamically loads and instantiates a validator class by its name.
 * It provides a robust fallback mechanism that returns a proxy implementation if the specified
 * class or one of its dependencies cannot be found at runtime. This is particularly useful
 * in environments like OSGi where dependencies might be optional or unavailable.
 * </p>
 * <p>
 * This class is not meant to be instantiated.
 * </p>
 */
public final class ReflectiveExpressionValidatorFactory {

    private static final Logger log = LoggerFactory.getLogger(ReflectiveExpressionValidatorFactory.class);

    /**
     * A private constructor to prevent instantiation of this utility class.
     */
    private ReflectiveExpressionValidatorFactory() {
        // Utility class
    }

    /**
     * A functional interface that represents a validation task which can throw
     * an {@link ExpressionValidationException}. This is similar to {@link Runnable} but
     * allows for a checked exception specific to validation logic.
     */
    @FunctionalInterface
    public interface ValidatorRunnable {
        /**
         * Executes the validation logic.
         *
         * @throws ExpressionValidationException if the validation fails.
         */
        void run() throws ExpressionValidationException;
    }

    /**
     * Creates an instance of {@link ExpressionValidatorExecutor} from a given class name.
     * <p>
     * If the specified class or any of its dependencies cannot be found (e.g., due to a
     * {@link ClassNotFoundException} or {@link NoClassDefFoundError}), this method logs a
     * warning and returns a proxy. The proxy's {@code execute} method will run the provided
     * {@code fallback} logic.
     * </p>
     * <p>
     * For any other instantiation errors, it wraps the original cause in a
     * {@link RuntimeException} to provide a full stack trace for debugging.
     * </p>
     *
     * @param className The fully qualified name of the class that implements {@link ExpressionValidatorExecutor}.
     * @param fallback The {@link ValidatorRunnable} to execute if the specified class cannot be loaded.
     * @return An instance of the specified validator class, or a proxy that executes the fallback.
     * @throws IllegalArgumentException if the specified class does not implement {@link ExpressionValidatorExecutor}.
     * @throws RuntimeException if the class fails to instantiate for reasons other than not being found.
     */
    public static ExpressionValidatorExecutor createExecutor(String className, ValidatorRunnable fallback) {
        try {
            Class<?> executorClass = getExecutorClass(className);
            // Instantiate the class using its default public constructor
            return (ExpressionValidatorExecutor) executorClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            Throwable cause = e;
            // Always unwrap InvocationTargetException to get to the root cause
            if (e instanceof java.lang.reflect.InvocationTargetException && e.getCause()!= null) {
                cause = e.getCause();
            }

            // Now, perform checks on the actual underlying cause of the failure.
            // In an OSGi context, both can indicate a missing import.
            if (cause instanceof ClassNotFoundException || cause instanceof NoClassDefFoundError) {
                // The fallback logic is appropriate here, as the validator implementation is unavailable.
                if (log.isDebugEnabled()) {
                    log.error("Validator class '{}' or one of its dependencies not found. Using fallback.", className, cause);
                } else {
                    log.warn("Validator class '{}' or one of its dependencies not found. Using fallback.", className);
                }
                return getProxy(fallback);
            } else {
                // For all other instantiation failures, re-throw a runtime exception
                // that preserves the original cause for a full stack trace.
                throw new RuntimeException("Failed to instantiate validator class '" + className + "'", cause);
            }
        }
    }

    /**
     * Loads a class by its name and verifies that it implements the {@link ExpressionValidatorExecutor} interface.
     *
     * @param className The fully qualified name of the class to load.
     * @return The {@link Class} object for the specified name.
     * @throws ClassNotFoundException if the class cannot be found by the classloader.
     * @throws IllegalArgumentException if the loaded class does not implement {@link ExpressionValidatorExecutor}.
     */
    private static Class<?> getExecutorClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = ExpressionValidatorExecutor.class.getClassLoader();
        // Attempt to get the Class object for the specified class name
        Class<?> executorClass = classLoader.loadClass(className);

        // Check if the class implements the desired interface
        if (!ExpressionValidatorExecutor.class.isAssignableFrom(executorClass)) {
            throw new IllegalArgumentException("Class " + className + " does not implement ExpressionValidatorExecutor");
        }
        return executorClass;
    }

    /**
     * Creates a dynamic proxy for the {@link ExpressionValidatorExecutor} interface that executes
     * the given fallback logic.
     *
     * @param fallback The {@link ValidatorRunnable} to be executed when the proxy's {@code execute} method is called.
     * @return A proxy instance of {@link ExpressionValidatorExecutor}.
     */
    private static ExpressionValidatorExecutor getProxy(ValidatorRunnable fallback) {
        // If the class is not found, return a proxy that executes the fallback
        return (ExpressionValidatorExecutor) Proxy.newProxyInstance(
                ExpressionValidatorExecutor.class.getClassLoader(),
                new Class<?>[]{ExpressionValidatorExecutor.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("execute")) {
                            fallback.run();
                        }
                        return null;
                    }
                }
        );
    }
}