package p3.common.util;

/**
 * Class contain items related to system colors.
 */
public class XColor
{
    // Reset
    public static final String RESET = "\033[0m";  // Text Reset

    // Regular Colors -> 0
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE
    public static final String XYZ = "\033[0;38m";   // WHITE

    // Bold -> 1
    public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    // Underline -> 4
    public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
    public static final String RED_UNDERLINED = "\033[4;31m";    // RED
    public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
    public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
    public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
    public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

    // Inverse -> 7
    public static final String BLACK_INVERSE = "\033[7;30m";  // BLACK
    public static final String RED_INVERSE = "\033[7;31m";    // RED
    public static final String GREEN_INVERSE = "\033[7;32m";  // GREEN
    public static final String YELLOW_INVERSE = "\033[7;33m"; // YELLOW
    public static final String BLUE_INVERSE = "\033[7;34m";   // BLUE
    public static final String PURPLE_INVERSE = "\033[7;35m"; // PURPLE
    public static final String CYAN_INVERSE = "\033[7;36m";   // CYAN
    public static final String WHITE_INVERSE = "\033[7;37m";  // WHITE

    // Strikethrough -> 9
    public static final String BLACK_STRIKETHROUGH = "\033[9;30m";  // BLACK
    public static final String RED_STRIKETHROUGH = "\033[9;31m";    // RED
    public static final String GREEN_STRIKETHROUGH = "\033[9;32m";  // GREEN
    public static final String YELLOW_STRIKETHROUGH = "\033[9;33m"; // YELLOW
    public static final String BLUE_STRIKETHROUGH = "\033[9;34m";   // BLUE
    public static final String PURPLE_STRIKETHROUGH = "\033[9;35m"; // PURPLE
    public static final String CYAN_STRIKETHROUGH = "\033[9;36m";   // CYAN
    public static final String WHITE_STRIKETHROUGH = "\033[9;37m";  // WHITE

    // Background
    public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
    public static final String RED_BACKGROUND = "\033[41m";    // RED
    public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
    public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
    public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
    public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

    // High Intensity
    public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
    public static final String RED_BRIGHT = "\033[0;91m";    // RED
    public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
    public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
    public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
    public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
    public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
    public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

    // Bold High Intensity
    public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
    public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
    public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
    public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
    public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

    // High Intensity backgrounds
    public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
    public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
    public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
    public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
    public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
    public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
    public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
    public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE

    static final String[] color_array = {
            RED, YELLOW, GREEN, BLUE, PURPLE, CYAN,
            RED_BRIGHT, YELLOW_BRIGHT, GREEN_BRIGHT, BLUE_BRIGHT, PURPLE_BRIGHT, CYAN_BRIGHT,
            RED_BOLD, YELLOW_BOLD, GREEN_BOLD, BLUE_BOLD, PURPLE_BOLD, CYAN_BOLD,
            RED_BOLD_BRIGHT, YELLOW_BOLD_BRIGHT, GREEN_BOLD_BRIGHT, BLUE_BOLD_BRIGHT, PURPLE_BOLD_BRIGHT, CYAN_BOLD_BRIGHT
            /*
            RED_UNDERLINED, YELLOW_UNDERLINED, GREEN_UNDERLINED, BLUE_UNDERLINED, PURPLE_UNDERLINED, CYAN_UNDERLINED,
            RED_STRIKETHROUGH, YELLOW_STRIKETHROUGH, GREEN_STRIKETHROUGH, BLUE_STRIKETHROUGH, PURPLE_STRIKETHROUGH, CYAN_STRIKETHROUGH,
            RED_INVERSE, YELLOW_INVERSE, GREEN_INVERSE, BLUE_INVERSE, PURPLE_INVERSE, CYAN_INVERSE,
            RED_BACKGROUND, YELLOW_BACKGROUND, GREEN_BACKGROUND, BLUE_BACKGROUND, PURPLE_BACKGROUND, CYAN_BACKGROUND,
            RED_BACKGROUND_BRIGHT, YELLOW_BACKGROUND_BRIGHT, GREEN_BACKGROUND_BRIGHT, BLUE_BACKGROUND_BRIGHT, PURPLE_BACKGROUND_BRIGHT, CYAN_BACKGROUND_BRIGHT,
             */
    };

    /**
     * Selects a color code based on a number that is mapped to a particular color.
     * The number can be some type of ID (i.e. thread, session, etc.) that can be
     * mapped consistently to a specific color.  The purpose of the number id to
     * color code mapping is to allow for the caller to display output text in a
     * color based on the ID, making is easier to follow.  For example, which thread
     * is writing to screen when multiple threads are writing to the console.
     * @param number Number to be mapped to a color code
     * @return Color code.
     */
    public static String SelectColor(int number)
    {
        String color = null;
        int index = 0;

        index = number % color_array.length;
        color = color_array[index];

        return color;
    }
}
