package p3;

import org.junit.jupiter.api.Test;
import p3.common.util.XColor;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for XColor class.
 */
public class XColorTest {

    /**
     * Test method for colors.
     */
    @Test
    public void testSelectColor() {
        assertEquals(XColor.SelectColor(0), "\033[0;31m");
        assertEquals(XColor.SelectColor(1), "\033[0;33m");
        assertEquals(XColor.SelectColor(2), "\033[0;32m");
    }
}
