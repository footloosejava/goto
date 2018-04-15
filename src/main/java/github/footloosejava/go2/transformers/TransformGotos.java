/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */
package github.footloosejava.go2.transformers;

import github.footloosejava.go2.Goto;
import github.footloosejava.go2.interfaces.GotoTransformed;
import github.footloosejava.go2.testing.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class TransformGotos {

    private static final String NAME = TransformGotos.class.toString();
    private static final boolean debug = false;
    private static final boolean debugAssembly = debug && true;
    private static final boolean showTransformedClass = debug && false;
    private static final boolean debug_getTransform_yes = debug && false;
    // the class interface that any class that does the gotos had (may or may not be transformed
    private static final String MARK_INTERFACE_ALREADY_PROCESSED = GotoTransformed.class.getName().replace('.', '/');

    /**
     * Transforms several class files at once and saves them. Class files can
     * have '.' or '/' as delimiter
     */
    public static synchronized void transformClassFiles(String... classNames) throws Exception {

        for (String className : classNames) {
            if (showTransformedClass) {
                //ASMifierClassVisitor.main(new String[]{className});
                System.out.println("That was **********************************"
                    + "******** ORIG FILE ***********************************");
            }

            boolean saveFile = true;
            final String fixedClassName = className.replace('.', '/');
            ClassReader cr = new ClassReader(fixedClassName);

            byte[] bytes = getTransform(cr, saveFile);

            if (bytes != null && saveFile) {
                saveClassFile(fixedClassName, bytes);
                if (debug) {
                    println("*******************");
                    println("Lets see new code!!");
                    if (showTransformedClass) {
                        //ASMifierClassVisitor.main(new String[]{className});
                    }
                }
            }
        }
    }

    /**
     * @return the bytes for the transformed class or null
     */
    public static synchronized byte[] getTransform(ClassReader cr, boolean changeFile) throws Exception {
        // we parse class with this
        // or asm4?
        Visitor v = new Visitor(Opcodes.ASM5);
        if (requiresTransform(cr, v)) {
            if (debug_getTransform_yes) {
                println("Calling getClassBytes on: " + cr.getClassName());
            }
            return getClassBytes(cr, v, changeFile);
        }
        return null;
    }

    /**
     * DO not perform this on the actual class to be loaded, just the interface!
     * Otherwise, when it gets the class out of the classloader it will already
     * exist
     * <p>
     * Could be changed by using ASM visitor to visit class and determine if
     * implements GotoEnabled!
     */
    public static synchronized boolean requiresTransform(ClassReader cr, Visitor visitor) {

        // we do this to catch after class name is read
        visitor.enter(cr);

        boolean ready = visitor.isGotoEnabled() && !visitor.isGotoTransformed();

        if (debug) {
            println("Class " + cr.getClassName() + " is ready to be transformed > " + ready);

            if (cr.getClassName().equals(Goto.class.getName())) {
                println(" > It is the Goto class!");
            } else {
                println(" > It has subclassed " + Goto.class + ": " + visitor.isGotoEnabled());
            }
            println(" > It has implemented" + MARK_INTERFACE_ALREADY_PROCESSED + ": " + visitor.isGotoTransformed());
        }

        return ready;
    }

    public static final String OBJECT = Object.class.getName().replace('.', '/');

    /**
     * This will return the bytes for a transformed class or null if not needed
     * or problem. It requires a classreader and a preparsed visitor on the same
     * class.
     */
    public static synchronized byte[] getClassBytes(ClassReader cr, Visitor visitor, boolean changeFile)
        throws Exception {

        String className = cr.getClassName();

        String supername = cr.getSuperName();

        if (supername != null) {
            if (debug) {
                println("We have a superName to look into! > " + supername);
            }

            if (!changeFile) {
                if (debug) {
                    println(" > calling back into GotoLoader.load(" + supername + ")");
                }
                GotoLoader.load(supername.replace('/', '.'));
            } else {
                transformClassFiles(supername.replace('/', '.'));
            }
        }

        // make sure we visited this class last
        if (!visitor.getName().equals(className)) {
            throw new Exception("getClassBytes(...) the Visitor passed in did "
                + "not parse same class name [" + visitor.getName() + "] as "
                + "the ClassReader passed in [" + className + "].");
        }

        if (debugAssembly) {
            println("Now, lets see class file:");
            // lets see assembler code before transformation
            //ASMifierClassVisitor.main(new String[]{className.replace('/', '.')});
        }

        // get list of interfaces check to see if needs to be done
        if (visitor.isGotoEnabled()) {
            if (debug) {
                println(" > Found class that does gotos: " + cr.getClassName());
            }

            // does it already have donemarker?
            if (visitor.isGotoTransformed()) {
                println(" > Found class that is already transformed: " + cr.getClassName());
                return null;
            }
        } else {
            if (debug) {
                println(" > Found class that does not do gotos (not have interface): " + cr.getClassName());
            }
            return null;
        }

        // make changes if go2 to labels found in code.
        // we have interface and may be/not yet transformed
        // lets build tree node structure of class
        if (debug) {
            println(" > Class requires transform [" + className + " ...");
        }
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        // not yet implemented
        if (cn.innerClasses.size() > 0) {
            // we need to also do inner classes!
            println("We need to do inner classes too for !" + cn.name);
            for (Object o : cn.innerClasses) {
                InnerClassNode icn = (InnerClassNode) o;
                println(" >> Inner node name: " + icn.name);
                println(" WE DONT ACTUALY DO INNER CLASSES YET!");
            }
        }

        boolean changed = NodeTransform.makeChanges(cn);

        // based on statius or whether it is needed to be loaded even if not changed
        if (changed || visitor.isGotoEnabled()) {
            if (debug) {
                println(" > Class bytes transformed for gotos: " + cn.name);
            }
            return getNewClassBytes(cn);
        } else {
            return null;
        }
    }

    /**
     * Writes out node and makes into bytes for class.
     */
    @SuppressWarnings("unchecked")
    private static byte[] getNewClassBytes(final ClassNode cn) {
        byte[] classBytes;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cn.interfaces.add(MARK_INTERFACE_ALREADY_PROCESSED);
        cn.accept(cw);
        classBytes = cw.toByteArray();
        return classBytes;
    }

    /**
     * Saves class changes to file.
     */
    private static void saveClassFile(final String className, final byte[] classBytes)
        throws IOException, URISyntaxException {

        String file = className.replace('.', '/') + ".class";
        URL url = ClassLoader.getSystemResource(file);
        File f = new File(url.toURI());

        println("Saving updated class: " + f);
        OutputStream os = new FileOutputStream(f);
        os.write(classBytes);
        os.close();
    }

    private static void println(String msg) {
        System.out.println(NAME + " > " + msg);
    }

    public static void main(String[] args) throws Exception {
        transformClassFiles(Test.class.getName());
    }
}
