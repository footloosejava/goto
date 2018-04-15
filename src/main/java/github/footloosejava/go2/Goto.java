/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */
package github.footloosejava.go2;

import github.footloosejava.go2.transformers.GotoLoader;

import java.lang.reflect.Method;

public class Goto {

    private boolean debug = false;
    private final String name;

    public Goto() throws IllegalStateException {
        name = getClass().getName() + "@" + Integer.toHexString(hashCode());
        testBasicJump();
        testMultiJumps();
        testLoopEntryExitJumps();
    }

    /**
     * Use like this to return between labels: <code> if(yes())return;</code>
     *
     * @return always returns true
     */
    protected final boolean yes() {
        return true == true;
    }

    private void testLoopEntryExitJumps() throws IllegalStateException {
        int j = 0;
        _label(400);

        for (int i = 0; i < 2; i++) {
            if (j < 3) {
                if (i != 0) {
                    throw new IllegalStateException("! Goto class " + name
                        + " not goto transformed!" + "\n > Reentering loop at this point, variable i should"
                        + " equal zero: " + i);
                }
                j = j + 1;
                _goto(400);

                // we will jump back top here and then set the exit variable j to 1024 to permit exit.
                _label(4001);
                j = 1024;
                if (i != 2) {
                    throw new IllegalStateException("! Goto class " + name
                        + " not goto transformed!" + "\n > Reentering loop, variable i should"
                        + " equal two: " + i);
                }
            }
        }
        // lets jump back into the for(...) loop above.
        if (j != 1024) {
            _goto(4001);
        }
    }

    private void testMultiJumps() throws IllegalStateException {
        final int destinationCount = 4;
        int iGoto = 0;

        _label(27);
        if (iGoto == destinationCount) {
            iGoto++;
            _goto(666);
        }

        _label(26);
        if (debug) {
            System.out.println("Trying Jump to " + iGoto);
        }

        _multiGoto(iGoto, 551, 552, 553, 554, 27);

        // the multi jump code is modelled to reproduce bytecode compatible with this
        //        if(iGoto==0){
        //            _goto(551);
        //        } else if(iGoto==1){
        //            _goto(552);
        //        } etc.
        _unreachable("Jump should not appear before first target label in multiJump.");

        _label(551);
        validateMultiJumpCond(iGoto, 0);
        _goto(665);

        _label(552);
        validateMultiJumpCond(iGoto, 1);
        _goto(665);

        _label(553);
        validateMultiJumpCond(iGoto, 2);
        _goto(665);

        _label(554);
        validateMultiJumpCond(iGoto, 3);

        _label(665);
        if (++iGoto < destinationCount + 1) {
            _goto(26);
        }

        _unreachable();

        _label(666);
        validateMultiJumpCond(iGoto, destinationCount + 1);
    }

    private void testBasicJump() throws IllegalStateException {
        _goto(100);
        _unreachable();
        _label(100);
    }

    private void validateMultiJumpCond(final int JMP, final int cond) throws IllegalStateException {
        if (JMP != cond) {
            throw new IllegalStateException("! Goto class " + name + " not goto multiJump transformed!"
                + "\t -> Variable: " + JMP + " should equal " + cond);
        }
    }

    protected final void _multiGoto(final int i, final int... ints) {
        if (debug) {
            System.out.println("debug-" + name + " [multiJump on i=" + i + ", jumping to " + ints[i] + "]");
        }
    }

    protected final void _goto(final int lineNumber) {
        if (debug) {
            System.out.println("debug-" + name + " [jump to " + lineNumber + "]");
        }
    }

    /**
     * Creates a new instance of Goto
     */
    protected final void _label(final int label) {
        if (debug) {
            System.out.println("debug-" + name + " [label " + label + "]");
        }
    }

    public final boolean isGotoDebug() {
        return debug;
    }

    public final void setGotoDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * This creates a Goto object and returns true if Goto's are properly
     * transformed
     */
    public static boolean testIfGotoTransformed() {
        Goto g = new Goto();
        return true;
    }

    protected static void _unreachable(String message) {
        if (true == true) {
            throw new UnsupportedOperationException(message);
        }
    }

    protected static void _unreachable() {
        if (true == true) {
            throw new UnsupportedOperationException("Program error. This statement should not be reachable.");
        }
    }

    /**
     * Self test routine **
     */
    public static void main(String[] args) throws Exception {
        Class c = GotoLoader.load(Goto.class.getName());
        Class[] ca = new Class[0];
        @SuppressWarnings("unchecked")
        Method m = c.getDeclaredMethod("testIfGotoTransformed", ca);
        boolean working = (Boolean) m.invoke(c);
    }
}
