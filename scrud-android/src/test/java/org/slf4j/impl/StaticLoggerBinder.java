    package org.slf4j.impl;

    import org.slf4j.ILoggerFactory;
    import org.slf4j.spi.LoggerFactoryBinder;

    /**
     * Force tests to use JDK14 for logging instead of the Android logging.
     *
     * @author Eric Pabst (epabst@gmail.com)
     *         Date: 9/20/12
     *         Time: 4:10 PM
     */
    @SuppressWarnings("UnusedDeclaration")
    public class StaticLoggerBinder implements LoggerFactoryBinder {
        private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

        /**
         * Declare the version of the SLF4J API this implementation is compiled against.
         * The value of this field is usually modified with each release.
         */
        // to avoid constant folding by the compiler, this field must *not* be final
        public static String REQUESTED_API_VERSION = "1.6";  // !final

        /**
         * Return the singleton of this class.
         *
         * @return the StaticLoggerBinder singleton
         */
        public static final StaticLoggerBinder getSingleton() {
          return SINGLETON;
        }

        private StaticLoggerBinder() {
        }

        @Override
        public ILoggerFactory getLoggerFactory() {
            return new JDK14LoggerFactory();
        }

        @Override
        public String getLoggerFactoryClassStr() {
            return "org.slf4j.impl.JDK14LoggerFactory";
        }
    }
