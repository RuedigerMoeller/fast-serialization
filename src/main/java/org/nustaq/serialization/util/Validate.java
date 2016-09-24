package org.nustaq.serialization.util;

/**
 * Created by fabianterhorst on 24.09.16.
 */

/**
 * The <tt>Validate</tt> class supports convenience validations to be
 * applied. It is fully Java 8 lambda capable and heavily uses them.
 */
public final class Validate {

    private static final String MESSAGE_PARAM_NOT_EQUAL = "%s must be equal to %s";
    private static final String MESSAGE_PARAM_NOT_GREATER_THAN = "%s must be greater than %s";
    private static final String MESSAGE_PARAM_NOT_LOWER_THAN = "%s must be lower than %s";
    private static final String MESSAGE_PARAM_NOT_GREATER_EQUAL = "%s must be greater than or equal to %s";
    private static final String MESSAGE_PARAM_NOT_LOWER_EQUAL = "%s must be lower than or equal to %s";
    private static final String MESSAGE_PARAM_NOT_NULL = "%s must not be null";

    public static void validate(MessageBuilder messageBuilder, Validation validation) {
        validate(messageBuilder, validation, new ExceptionBuilder() {
            @Override
            public Exception build(String message) {
                return new ValidationException(message);
            }
        });
    }

    public static void validate(MessageBuilder messageBuilder, Validation validation, ExceptionBuilder exceptionBuilder) {
        if (!validation.validate()) {
            Exception exception = exceptionBuilder.build(messageBuilder.build());
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            }
            ExceptionUtil.rethrow(exception);
        }
    }

    public static void equals(String paramName, final int expected, final int value) {
        validate(message(MESSAGE_PARAM_NOT_EQUAL, paramName, expected), new Validation() {
            @Override
            public boolean validate() {
                return expected == value;
            }
        });
    }

    public static void greaterThan(String paramName, final int minimum, final int value) {
        validate(message(MESSAGE_PARAM_NOT_GREATER_THAN, paramName, minimum), new Validation() {
            @Override
            public boolean validate() {
                return minimum < value;
            }
        });
    }

    public static void lowerThan(String paramName, final int maximum, final int value) {
        validate(message(MESSAGE_PARAM_NOT_LOWER_THAN, paramName, maximum), new Validation() {
            @Override
            public boolean validate() {
                return maximum > value;
            }
        });
    }

    public static void greaterOrEqual(String paramName, final int minimum, final int value) {
        validate(message(MESSAGE_PARAM_NOT_GREATER_EQUAL, paramName, minimum), new Validation() {
            @Override
            public boolean validate() {
                return minimum <= value;
            }
        });
    }

    public static void lowerOrEqual(String paramName, final int maximum, final int value) {
        validate(message(MESSAGE_PARAM_NOT_LOWER_EQUAL, paramName, maximum), new Validation() {
            @Override
            public boolean validate() {
                return maximum >= value;
            }
        });
    }

    public static void notNull(String paramName, final Object value) {
        validate(message(MESSAGE_PARAM_NOT_NULL, paramName), new Validation() {
            @Override
            public boolean validate() {
                return value != null;
            }
        }, new ExceptionBuilder() {
            @Override
            public Exception build(String message) {
                return null;
            }
        });
    }

    private Validate() {
    }

    private static MessageBuilder message(final String message, final Object param) {
        return new MessageBuilder() {
            @Override
            public String build() {
                return String.format(message, param);
            }
        };
    }

    private static MessageBuilder message(final String message, final Object param1, final Object param2) {
        return new MessageBuilder() {
            @Override
            public String build() {
                return String.format(message, param1, param2);
            }
        };
    }

    /**
     * The <tt>Validation</tt> interface is used to implement internal and
     * external validations based on Java 8 lambdas.
     */
    public static interface Validation {

        /**
         * This method implements the validation logic and returns <tt>true</tt>
         * if the validation passed or <tt>false</tt> if not.
         *
         * @return true if validation passed, otherwise false
         */
        boolean validate();
    }

    /**
     * The <tt>MessageBuilder</tt> interface is used to delay creation of
     * exception messages up to the point where a validation really failed.
     * This prevents heavy string concatinations or other sort of costly
     * operations to be as lazy as possible and to only happen if really
     * necessary.
     */
    public static interface MessageBuilder {

        /**
         * Generates the content and builds the exception message.
         *
         * @return the generated exception message
         */
        String build();
    }

    /**
     * The <tt>ExceptionBuilder</tt> interface is used to delay creation of
     * exception up to the point where a validation really failed and a message
     * was created.
     */
    public static interface ExceptionBuilder {

        /**
         * Generates the exception using the given exceptional message
         *
         * @return the exceptional message
         */
        Exception build(String message);
    }

    /**
     * This exception class is thrown whenever a validation fails. It is a subclass
     * of {@link java.lang.IllegalArgumentException} since most validations happen
     * on parameters passed to any constructor or function.
     */
    public static class ValidationException
            extends IllegalArgumentException {

        /**
         * Creation of a ValidationException using an exception message string.
         *
         * @param s string to be used as exception message
         */
        protected ValidationException(String s) {
            super(s);
        }
    }

}
