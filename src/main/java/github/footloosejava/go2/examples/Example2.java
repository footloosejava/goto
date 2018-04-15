/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 *
 * This shows how to do a goto class file transformation.
 */
package github.footloosejava.go2.examples;

import github.footloosejava.go2.Goto;
import github.footloosejava.go2.interfaces.ProgramStarter;
import github.footloosejava.go2.transformers.GotoLoader;

public class Example2 extends Goto implements ProgramStarter {

    @Override
    public void start(String[] args) {
        DemoProject.main(args);
    }

    public static void main(String args[]) throws Exception {
        ProgramStarter starter = (ProgramStarter) GotoLoader.newInstance(Example2.class.getName());
        starter.start(args);
    }
}