package src.util;

import java.io.FileWriter;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * A class to log all info, warnings, and errors to file.
 */
public final class Logger
{
    /**
     * Prevents instantiation of this class.
     */
    private Logger() {}

    /**
     * Logs the given text and prints it to stdout.
     *
     * @param text The text to print and log
     */
    public static void log(String text)
    {
        System.out.println(text);
        write(text);
    }

    /**
     * Logs the given text and prints it to stderr.
     *
     * @param text The text to print and log
     */
    public static void logWarning(String text)
    {
        System.err.println("Warning: " + text);
        write("Warning: " + text);
    }

    /**
     * Logs the given text, prints it to stderr, and displays it in an error window.
     *
     * @param text The text to log and print
     */
    public static void logError(String text)
    {
        System.err.println("Error: " + text);
        write("Error: " + text);

        //TODO display this error in InfoWindow.displayError()!
    }

    /**
     * Logs the given text and exception, prints them to stderr, and displays them in an error window.
     *
     * @param text The text to log and print
     * @param e The exception to print and log
     */
    public static void logError(String text, Throwable e)
    {
        System.err.println("Error: " + text + " Stacktrace:\n");
        e.printStackTrace();
        write("Error: " + text + ". Stacktrace:\n" + e.getMessage());

        //TODO display this error in InfoWindow.displayError()!
    }

    /**
     * Writes the given text to the log with the date and time prepended to it.
     *
     * @param text The text to log
     */
    private static synchronized void write(String text)
    {
        String formattedDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm: ").format(new Date());

        try (FileWriter writer = new FileWriter("logs/log.log", true))
        {
            writer.write(formattedDateTime);
            writer.write(text + "\n");
        }
        catch (IOException e)
        {
            System.err.println("Critical error! Unable to write to log.log! Stacktrace:\n");
            e.printStackTrace();
        }
    }
}
