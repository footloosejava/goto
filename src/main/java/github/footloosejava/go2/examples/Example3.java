/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 *
 * Just some sample code. not showing anything in particular
 * This shows how to do a goto class file transformation.
 */
package github.footloosejava.go2.examples;

import github.footloosejava.go2.Goto;
import github.footloosejava.go2.transformers.GotoLoader;

public class Example3 extends Goto implements Runnable {

    public void run() {
        int x = 0;
        _label(0);

        if (x > 0) {
            System.out.println("We jumped back to label 0 before reaching this code!");
            _goto(1);
        }


        x++;
        _goto(0);

        _unreachable();

        if (yes()) {

            _label(1);
            System.out.println("We reached label 1");
            // do some code then return
            if (yes()) {
                return;
            }

            _label(2);
            System.out.println("We reached label 2");
            // do some other code then return
            if (yes()) {
                return;
            }
            
        }
        _unreachable();
    }

    public static void main(String args[]) throws Exception {
        Runnable demo = (Runnable) GotoLoader.newInstance(Example3.class.getName());
        demo.run();
    }
}