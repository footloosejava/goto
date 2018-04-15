/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */

package github.footloosejava.go2.examples;

import github.footloosejava.go2.Goto;
import github.footloosejava.go2.transformers.GotoLoader;


public class Demo extends Goto implements Runnable {

    @Override
    public void run() {
        System.out.println();
        System.out.println("********* Showing Simple Jumps ***************");
        new DemoSimpleForwardBackwardJumps().run();

        System.out.println();
        System.out.println("********* Showing Jumps With Loops ***************");
        new DemoJumpsWithLoops().run();

        System.out.println();
        System.out.println("********* Showing Multi Jumps ***************");
        new DemoMultiJumps().run();
    }

    public static void main(String args[]) throws Exception {

        // the class we load must extend Goto or else it will not be transformed
        String name = Demo.class.getName();

        /* Here we load the class at runtime at it is transformed.
         * IF Demo creates any classes in its code that need transformation
         * THEN they are automatically transformed by the classloader used when itself (Demo) was created.
         * THEREFORE only the root class needs to be loaded with GotoLoader
         * The root class should have an interface so that it can be used without reflection.
         */
        Runnable demo = (Runnable) GotoLoader.newInstance(name);
        demo.run();
    }
}