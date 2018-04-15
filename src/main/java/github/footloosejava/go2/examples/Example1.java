/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 *
 * This shows how to do a goto class file transformation.
 */
package github.footloosejava.go2.examples;

import github.footloosejava.go2.Goto;
import github.footloosejava.go2.transformers.GotoLoader;

public class Example1 extends Goto implements Runnable {

    public void run() {
        int x = 0;

        _label(0);

        if (x > 0) {
            System.out.println("We jumped back to label 0 before reaching this code!");
            _goto(1);
        }

        x++;
        _goto(0);

        _label(1);
    }

    public static void main(String args[]) throws Exception {
        Runnable demo = (Runnable) GotoLoader.newInstance(Example1.class.getName());
        demo.run();
    }
}
