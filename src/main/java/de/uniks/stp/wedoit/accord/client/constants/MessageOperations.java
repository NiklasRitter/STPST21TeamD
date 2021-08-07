package de.uniks.stp.wedoit.accord.client.constants;

/**
 * includes constants to create and handle incoming quotes
 */
public class MessageOperations {
    // Constants for quote a message
    public static final String QUOTE_PREFIX = "###quoteINIT###";
    public static final String QUOTE_SUFFIX = "###quoteSTOP###";
    public static final String QUOTE_MESSAGE = "###quoteMESSAGE###";

    public static final String QUOTE = "quote";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String COPY = "copy";

    public static final String BOLD_STYLING_KEY = "*";
    public static final String BOLD_STYLING_KEY_SPLITTER = "(?<=\\" + BOLD_STYLING_KEY + ")";
}
