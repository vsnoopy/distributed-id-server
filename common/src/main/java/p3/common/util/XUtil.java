package p3.common.util;

/**
 * Class containing shared general methods.
 */
public class XUtil
{
    public static final String error_return_prefix = "ERROR: ";

    /**
     * Builds error message on server side to be sent to client.
     * @param error_message Error message.
     * @param item An item that caused the error.
     * @return Message to be sent to client.
     */
    public static String BuildErrorMessage(String error_message, String item)
    {
        final String error_return_message;

        error_return_message = error_return_prefix + error_message + ": " + item;

        return error_return_message;
    }

    /**
     * Determine if the message sent to the client from the server
     * is an error message.
     * @param return_message Message sent from server to client.
     * @return true if message represents an error, false otherwise.
     */
    public static boolean ErrorMessageReturn(String return_message)
    {
        boolean error_return = false;

        if (return_message.startsWith(error_return_prefix)) {
            PrintError(return_message);
            error_return = true;
        }

        return error_return;
    }

    /**
     * Displays a message string colorized based on color mapped to a particular number.
     * @param str String to be displayed.
     */
    public static void PrintThread(String str)
    {
        final String color;
        final long thread_id;

        thread_id = Thread.currentThread().getId();
        color = XColor.SelectColor((int)thread_id);

        System.out.println(color + "[tid=" + thread_id + "] : " + XColor.RESET + str);
    }

    /**
     * Takes a header and prepends with the active thread and colorized.
     * @param header Header message to be display.
     */
    public static void DisplayTestHeader(String header)
    {
        System.out.println();
        System.out.println(
            XColor.RED_UNDERLINED + "Testing [tid=" + Thread.currentThread().getId() + "]: " + header + XColor.RESET);
    }

    /**
     * Take footer message and displays it colorized.
     * @param footer Footer message to be displayed
     */
    public static void DisplayTestFooter(String footer)
    {
        System.out.println(XColor.YELLOW_UNDERLINED + "Testing: " + footer + XColor.RESET);
    }

    /**
     * Display the given title colorized and underlined.
     * @param title Title to be display.
     */
    public static void DumpPrintTitle(String title)
    {
        System.out.println(XColor.YELLOW_UNDERLINED + title + XColor.RESET);
    }

    /**
     * Displays a message string colorized.
     * @param str String to be displayed.
     */
    public static void DumpPrint(String str)
    {
        System.out.println(XColor.GREEN + str + XColor.RESET);
    }

    /**
     * Displays a message string colorized.
     * @param str String to be displayed.
     */
    public static void PrintDebug(String str)
    {
        System.out.println(XColor.YELLOW + str + XColor.RESET);
    }

    /**
     * Displays a message string colorized.
     * @param str String to be displayed.
     */
    public static void PrintError(String str)
    {
        System.out.println(XColor.RED + str + XColor.RESET);
    }
}
