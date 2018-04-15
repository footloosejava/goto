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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Example4 extends Goto implements Runnable {

    public void run() {

        Lock lock = new ReentrantLock();

        try {
            lock.lock();
            System.out.println("Lock obtained");

            _goto(1);
            _unreachable();

            _label(2);
            System.out.println("Label 2");

            if (yes()) {
                return;
            }
        } finally {
            lock.unlock();
            System.out.println("Lock released");
        }

        _unreachable();

        _label(1);
        System.out.println("Label 1");

        _goto(2); // go back into try-for and return from method
        _unreachable();
    }

    public static void main(String args[]) throws Exception {
        Runnable demo = (Runnable) GotoLoader.newInstance(Example4.class.getName());
        demo.run();
    }
}