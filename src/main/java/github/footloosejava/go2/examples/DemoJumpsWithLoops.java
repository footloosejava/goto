/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */

package github.footloosejava.go2.examples;

import github.footloosejava.go2.Goto;

public class DemoJumpsWithLoops extends Goto {

    /**
     * This shows that go2 can be made into/out of loops.
     *
     * When jumping backwards into a loop, the variable in the loop that was
     * initialized at loop entry is not re-initialized and therefore it will
     * immediately escape the loop upon re-evaluation, unless the code adjusts
     * the variable.
     */
    public void run() {

        // demo to see jumping into and out of loops
        System.out.println("Now lets try some loops...");

        int j = 0;

        _label(300);

        j = j + 1;
        System.out.println("\tLoop " + j);
        if (j < 4) {
            _goto(300);
        }

        // lets see
        System.out.println("Looping out of for()");

        // so we don't see 30,000 debug statements
        setGotoDebug(false);

        boolean once = false;

        // jump in and out of loop!
        j = 0;

        _label(400);

        for (int i = 0; i < 3; i++) {
            if (j < 10) {
                System.out.println("in for(...) i = " + i);
            }
            if (j < 30000) {
                j = j + 1;
                _goto(400);
                _label(4001);
                System.out.println("Value of i = " + i);
                once = true;
            }
        }


        setGotoDebug(true);

        // lets jump back into the for(...) loop above.
        if (!once) {
            _goto(4001);
        }
    }
}