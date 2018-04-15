/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */
package github.footloosejava.go2.testing;

import github.footloosejava.go2.Goto;

public class Test extends Goto implements Runnable {

    public void run() {

        setGotoDebug(true);

        int x = 0;


        _label(0);


        System.out.println("X=" + x);

        if (x > 0) {
            System.out.println("we are now jumped back!");
            _goto(200);
        }

        int JJ = 2563;


        _label(1);
        _label(2563);
        _label(6);

        _goto(100);

        x++;
        _unreachable("Should not see this line!!  After Goto 100 command. X=" + x);

        _label(100);

        x++;
        System.out.println("Here after line 100");

        System.out.println("X=" + x);
        if (x == 1) {
            System.out.println("It worked!  We passed over the stuff between jump and label 100!");
        } else {
            _unreachable("Goto not functioning!");
        }

        _goto(0);

        // this should never be reached
        System.out.println("After Goto 0 command");

        _label(200);
        System.out.println("Now lets try some loops...");

        int j = 0;

        _label(300);
        j = j + 1;
        System.out.println("\tLoop " + j);
        if (j < 4) {
            _goto(300);
        }

        System.out.println("Now calling run2()");
        try {
            run2();
        } catch (RuntimeException ex) {
            System.out.println("Do nothing!");
        }


        System.out.println("Looping out of for()");

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
        if (!once) {
            _goto(4001);
        }

        int iJump = 0;

        _label(27);
        if (iJump == 8) {
            System.out.println("finished back jump! it all works!");
            _goto(666);
        }

        
        _label(26);
        System.out.println("Trying Jump to " + iJump);
        int xxy = 10;

        // this will jump to the label indicated by the index of iJump variable
        // 0 will goto 551, 1 to 552 etc.
        _multiGoto(iJump, 551, 552, 553, 554, 555, 556, 557, 558, 27);

        _unreachable("No jump in _multiGoto lands here!");

        _label(551);
        _label(552);
        _label(553);
        _label(554);
        _label(555);
        _label(556);
        _label(557);
        _label(558);

        iJump++;
        if (iJump < 9) {
            _goto(26);
        }

        _label(666);
        System.out.println("All done!  Stopping");
    }

    private void run2() {
        _label(1);
        _goto(100);
        System.out.println("After _goto(100)");

        _label(100);
        System.out.println("After 100");
        System.out.println("Finished run2()");
        throw new RuntimeException();
    }
}