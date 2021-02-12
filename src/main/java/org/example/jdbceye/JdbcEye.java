package org.example.jdbceye;

import org.example.jdbceye.transformer.WrappedConnectionTransformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JdbcEye {

    private final static Logger logger = Logger.getLogger(String.format("JdbcEye - %s", JdbcEye.class.getName()));

    public static void premain(final String agentArgs, final Instrumentation inst) {
        logger.log(Level.INFO, "Static agent.");
    }

    public static void agentmain(final String agentArgs, final Instrumentation inst) throws UnmodifiableClassException {
        logger.log(Level.INFO, "Dynamic agent.");

        final String prepStmntClassStr = "org.jboss.jca.adapters.jdbc.WrappedConnection";
        Class<?> prepStmntClass = null;

        for (Class<?> clazz :  inst.getAllLoadedClasses()) {
            if (clazz.getName().equals(prepStmntClassStr)) {
                prepStmntClass = clazz;
                break;
            }
        }

        if (prepStmntClass != null) {
            logger.log(Level.INFO, "Adding transformer.");
            final ClassLoader prepStmntClassLoader = prepStmntClass.getClassLoader();
            inst.addTransformer(new WrappedConnectionTransformer(prepStmntClassStr, prepStmntClassLoader), true);
            inst.retransformClasses(prepStmntClass);
        } else {
            logger.log(Level.INFO, "java.sql.PreparedStatement was not found.");
        }
    }

}
