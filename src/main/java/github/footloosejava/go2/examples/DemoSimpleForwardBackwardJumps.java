/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */

package github.footloosejava.go2.examples;

import github.footloosejava.go2.Goto;

public class DemoSimpleForwardBackwardJumps extends Goto {

    /**
     * This shows simple forward and backward go2.
     * 
     * Labels can be in any order and are any integer number >= ZERO.
     */
    public void run() {

        setGotoDebug(true);

        int x = 0;
        _label(0);

        System.out.println("X=" + x);

        if (x > 0) {
            System.out.println("we are now jumped back!");
            _goto(200);
        }

        _label(1);

        _goto(100);

        x++;

        // this should never be reached
        System.out.println("Should not see this line!!  After Goto 100 command. X=" + x);

        _label(100);

        x++;
        System.out.println("Here after label 100.  X = " + x);

        if (x == 1) {
            System.out.println("It worked!  We passed over the stuff between jump and label 100!");
        } else {
            _unreachable("Goto not functioning!");
        }

        // lets go back to start at label 0
        _goto(0);

        // this should never be reached
        System.out.println("After Goto 0 command");

        _label(200);
    }

}