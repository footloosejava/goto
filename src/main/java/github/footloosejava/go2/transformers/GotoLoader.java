/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 *
 */
package github.footloosejava.go2.transformers;

import github.footloosejava.go2.Goto;
import github.footloosejava.go2.interfaces.GotoTransformed;
import org.objectweb.asm.ClassReader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author footloosejava
 */
public final class GotoLoader extends ClassLoader {

    private final boolean debug = false;
    private final boolean debugRegularLoads = false;
    private final boolean debug_defineClassCalls = false;
    private final List<String> classPackagesToIgnore = new ArrayList<>();
    private final List<String> classPackagesToCheck = new ArrayList<>();
    final String GOTO = Goto.class.getName();
    final String GOTO_TRANSFORMED = GotoTransformed.class.getName();

    /**
     * Creates a new instance of GotoClassLoader
     */
    private GotoLoader() {
        super(GotoLoader.class.getClassLoader());
    }

    /**
     * This will load a new class that is goto enabled. First checks to see if
     * already loaded. Then tries to load via TransformGotos.getTransform(...)
     *
     * @return null if not found or class produced.
     */
    private synchronized Class loadGotoEnabledClass(String className) throws Exception {

        // first see if already registered and loaded
        Class c = super.findLoadedClass(className);

        if (c != null) {
            return c;
        }

        // try {

        if (debug) {
            System.out.println(this + " >>>>> Class may need 'goto' transform:" + className);
        }

        // make new class reader
        ClassReader cr = new ClassReader(className);

        // make new class visitor
        byte[] bytes = TransformGotos.getTransform(cr, false);

        if (className.equals(GOTO) && bytes == null) {
            throw new RuntimeException("loadGotoEnabledClass"
                + "(" + className + ")"
                + " did not return a class!");
        }

        if (bytes != null) {
            if (debug_defineClassCalls) {
                System.out.println(this + " >>>>> Now defining class from bytes...");
            }
            c = defineClass(className, bytes, 0, bytes.length);
            if (debug) {
                System.out.println(this + " >>>>> Class " + c + " successfully defined.");
            }
            return c;
        }

//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new ClassNotFoundException("Exception converting gotos" +
//                    " in class: " + className + ". Reason: " + ex.getMessage(),ex);
//        }

        return null;
    }

    /**
     * If the class NameImpl ends with 'Impl' then this is a marker that it may
     * be a GotoEnabled class. We check to see if an Interface Exists for 'Name'
     * before the 'Impl' part. This interface and the class requested are both
     * checked to make sure they have Interface GotoEnabled.
     * <p>
     * After the transform, the 'Impl' class also has a new interface
     * 'GotoTransformed'! The class should never be used as its 'Impl' class,
     * but rather as its Interface version. Loading is best done by a Factory
     * Class pattern, so that Test t = TestFactory.newInstance() and that means
     * making the Impl version have a package private constructor.
     *
     * @param className
     * @return
     * @throws java.lang.ClassNotFoundException
     */
    @Override
    public synchronized Class findClass(String className) throws ClassNotFoundException {

        if (!isSunJavaLanguageClass(className)) {

            boolean loadIt = isGotoLoadable(className);

            if (loadIt) {

                try {

                    // if(debug) System.out.println(this + " > Finding class: " + className);
                    Class c = loadGotoEnabledClass(className);

                    if (className.equals(GOTO) && c == null) {
                        throw new RuntimeException("loadGotoEnabledClass(" + GOTO + " did not return a class!");
                    }

                    if (c != null) {
                        return c;
                    }
                } catch (Exception ex) {
                    if (debug) {
                        System.out.println(this + " > class not found by loadGotoEnabledClass(...): " + className
                            + "\nEXCEPTION CAUSE: " + ex.getMessage());
                    }
                }
            }

        }
        if (debugRegularLoads) {
            System.out.println(this + " >>>>> Delegating findClass to super()");
        }
        return super.findClass(className);
    }

    private boolean isGotoLoadable(final String className) {

        boolean loadIt = false;

        // IF class is wm.gotos.Goto
        if (className.equals(GOTO)) {
            loadIt = true;

        } else if (className.equals(GOTO_TRANSFORMED)) {
            loadIt = false;

        } else if (classPackagesToCheck.size() > 0) {
            for (String pkg : classPackagesToCheck) {
                if (className.startsWith(pkg)) {
                    loadIt = true;
                }
            }

        } else if (classPackagesToIgnore.size() > 0) {
            for (String pkg : classPackagesToCheck) {
                if (className.startsWith(pkg)) {
                    loadIt = false;
                }
            }
        } else {
            loadIt = true;
        }
        return loadIt;
    }

    @Override
    public synchronized Class loadClass(String className) throws ClassNotFoundException {

        Class c = super.findLoadedClass(className);

        if (c != null) {
            return c;
        } else {
            try {
                c = findClass(className);
                return c;
            } catch (ClassNotFoundException ex) {
                if (debugRegularLoads) {
                    System.out.println(this + " >>>>> could not locate " + className + ". Delegating to super()");// ex.printStackTrace();
                }
            }

            // will be anyways when we get here
            c = super.loadClass(className);

            if (debugRegularLoads) {
                System.out.println(this + " >>>>> super() found class " + c);
            }
            return c;
        }
    }

    /**
     * false if starts with java. or javax. or sun.
     */
    public static boolean isSunJavaLanguageClass(String className) {
        char c = className.charAt(0);
        // fast omit classes not starting with the letters our words start with
        if (className.length() < 4 || c != 'j' || c != 's') {
            return false;
        }
        return className.startsWith("java.") || className.startsWith("java/")
            || className.startsWith("javax.") || className.startsWith("javax/")
            || className.startsWith("sun.") || className.startsWith("sun/");
    }

    private static final GotoLoader g2l = new GotoLoader();

    public static Class<?> load(String name) throws ClassNotFoundException {
        return g2l.loadClass(name);
    }

    public static Object newInstance(String name) throws Exception {
        Class c = load(name);
        if (c != null) {
            return c.newInstance();
        }
        throw new Exception("Class + " + name + " could not be made.");
    }

    public static synchronized void addPackageToLoadNormally(String packageName) {
        if (!packageName.endsWith(".")) {
            packageName += ".";
        }
        g2l.classPackagesToIgnore.add(packageName);
    }

    public static synchronized void addPackageToOnlyCheck(String packageName) {
        if (!packageName.endsWith(".")) {
            packageName += ".";
        }
        g2l.classPackagesToCheck.add(packageName);
    }
}
