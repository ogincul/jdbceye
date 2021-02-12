package org.example.jdbceye.transformer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.logging.Logger;
import java.util.logging.Level;

public class WrappedConnectionTransformer implements ClassFileTransformer {

    private final String prepStmntClassStr;
    private final ClassLoader prepStmntClassLoader;
    private final static Logger logger = Logger.getLogger(String.format("JdbcEye - %s", WrappedConnectionTransformer.class.getName()));

    public WrappedConnectionTransformer(final String prepStmntClassStr, final ClassLoader prepStmntClassLoader) {
        this.prepStmntClassStr = prepStmntClassStr;
        this.prepStmntClassLoader = prepStmntClassLoader;

        logger.log(Level.INFO, String.format("Transformer created for %s.", prepStmntClassStr));
    }

    @Override
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {
        logger.log(Level.FINE, String.format("Checking if transformation for %s is wanted.", className));

        byte[] prepStmntByteArray = classfileBuffer;
        if (prepStmntClassStr.replaceAll("\\.", "/").equals(className) && loader == prepStmntClassLoader) {
            logger.log(Level.INFO, String.format("Transforming %s...", prepStmntClassStr));

            final ClassPool classPool = ClassPool.getDefault();
            classPool.appendClassPath(new LoaderClassPath(loader));
            try {
                logger.log(Level.INFO, String.format("Attempting to get from pool %s...", prepStmntClassStr));
                CtClass ctClass = classPool.get(prepStmntClassStr);
                CtMethod[] ctMethods = ctClass.getDeclaredMethods("prepareStatement");
                for (CtMethod ctMethod : ctMethods) {
                    ctMethod.insertBefore("System.out.println(\"JdbcEye: prepareStatement > \" + sql);");
                    ctMethod.insertBefore("sql = sql.replaceAll(\"t1.address_full\", \"substr(t1.address_full, 1, 2)\");");
                }
                prepStmntByteArray = ctClass.toBytecode();
                ctClass.detach();
                logger.log(Level.INFO, String.format("Finished the transformation of %s.", prepStmntClassStr));
            } catch (NotFoundException | CannotCompileException | IOException e) {
                logger.log(Level.SEVERE, "Transformation failed.", e);
            }
        }

        return prepStmntByteArray;
    }
}
