package p3;

import org.junit.jupiter.api.Test;
import p3.common.util.XUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for XUtil class.
 */
public class XUtilTest {

    /**
     * Simple test method.
     */
    @Test
    public void testDisplayTestHeader() {
        try {
            XUtil.DisplayTestHeader("Test Header");
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    /**
     * Simple test method.
     */
    @Test
    public void testDisplayTestFooter() {
        try {
            XUtil.DisplayTestFooter("Test Footer");
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    /**
     * Simple test method.
     */
    @Test
    public void testDumpPrintTitle() {
        try {
            XUtil.DumpPrintTitle("Test Title");
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    /**
     * Simple test method.
     */
    @Test
    public void testDumpPrint() {
        try {
            XUtil.DumpPrint("Test Print");
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    /**
     * Simple test method.
     */
    @Test
    public void testPrintThread() {
        try {
            XUtil.PrintThread("Test Thread");
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }
}