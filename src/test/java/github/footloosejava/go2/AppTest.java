/*
 * Created on June 25, 2007, 11:27 AM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */
package github.footloosejava.go2;

import github.footloosejava.go2.examples.Demo;
import github.footloosejava.go2.transformers.GotoLoader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * rigorous Test :-)
     * @throws java.lang.Exception
     */
    public void testApp() throws Exception {
        // FAIL CASE
        try {
            Class c = GotoLoader.load(github.footloosejava.go2.testing.Test.class.getName());
            Runnable runnable = (Runnable) c.newInstance();
            runnable.run();
            throw new Exception("I should have failed here - Test class not loaded through the special loader.");
        } catch (Exception ex) {
            // correct
        }

        // SUCCESS CASE
        try {
            Runnable runnable = (Runnable) GotoLoader.newInstance(github.footloosejava.go2.testing.Test.class.getName());
            runnable.run();
        } catch (Exception ex) {
            throw new Exception("I should not have failed here - Test class was loaded through the special loader.", ex);
        }

        // FAIL CASE
        try {
            new Demo().run();
            throw new Exception("I should have failed here- Test class was not loaded through the special loader.");
        } catch (Exception ex) {
            //
        }

        // SUCCESS CASE
        try {
            Runnable runnable = (Runnable) GotoLoader.newInstance(Demo.class.getName());
            runnable.run();

        } catch (Exception ex) {
            throw new Exception("I should not have failed here - Test class was loaded through the special loader.", ex);
        }
    }
}
