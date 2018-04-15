/*
 * DemoMultiJumps.java
 *
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */
package github.footloosejava.go2.examples;

import github.footloosejava.go2.Goto;

public class DemoMultiJumps extends Goto {

    /**
     * This shows how to do go2 based on an index variable.
     *
     * JMP is the index variable here and must be the first argument in the
     * multi jump.
     */
    public void run() {

        int JMP = 0;

        _label(27);

        // we will jump back and exit here
        if (JMP == 8) {
            return;
        }

        System.out.println("Trying Jump to " + JMP);

        // this will jump to the label indicated by the index of JMP variable
        // labels can be in any order, but thats the order they will be addressed as
        _multiGoto(JMP, 551, 552, 553, 554, 555, 556, 557, 558, 27);


        // since goto does not fit into existing language, code sections may not be reachable
        // and therefore need to add 'if(flag) throw ...' tor call the convenient 'unreachable' method 
        // to not get compiler error that the remaining code is_unreachable
        _unreachable("Jump should not appear before first label in multiJump.");

        _label(551);
        _label(552);
        _label(553);
        _label(554);
        _label(555);
        _label(556);
        _label(557);
        _label(558);

        JMP++;
        if (JMP < 9) {
            _goto(27);
        }

        System.out.println("All done!  Stopping");
    }
}