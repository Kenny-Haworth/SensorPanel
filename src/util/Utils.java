package src.util;

import static src.util.Logger.log;
import static src.util.Logger.logError;
import static src.util.Logger.logWarning;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * A class to hold various, non-related utilities.
 */
public final class Utils
{
    /**
     * Prevents instantiation of this class.
     */
    private Utils() {}

    /**
     * Sets the graphic's font to the largest size possible while still fitting the given text into the given pixel width.
     *
     * @param g2d The graphics context to set the font of
     * @param text The text that will be displayed. This need not be the exact text to be displayed, but can, for example, be text
     *             of the largest expected length for a component to keep the text a consistent size as the length changes.
     * @param pixelWidth The number of pixels wide that is available to draw the text
     */
    public static void setFontFromWidth(Graphics2D g2d, String text, int pixelWidth)
    {
        Font font = g2d.getFont();
        Rectangle2D rect = g2d.getFontMetrics(font).getStringBounds(text, g2d);
        g2d.setFont(font.deriveFont((float)((double)font.getSize2D() * pixelWidth/rect.getWidth())));
    }

    /**
     * Sets the graphic's font to the largest size possible while still fitting the given text into the given pixel height.
     *
     * @param g2d The graphics context to set the font of
     * @param text The text that will be displayed. This need not be the exact text to be displayed, but can, for example, be text
     *             of the largest expected length for a component to keep the text a consistent size as the length changes.
     * @param pixelHeight The number of pixels high that is available to draw the text
     */
    public static void setFontFromHeight(Graphics2D g2d, String text, int pixelHeight)
    {
        Font font = g2d.getFont();
        Rectangle2D rect = g2d.getFontMetrics(font).getStringBounds(text, g2d);
        g2d.setFont(font.deriveFont((float)((double)font.getSize2D() * pixelHeight/rect.getHeight())));
    }

    /**
     * Sets the graphic's font to the largest size possible while still fitting the given text into the given pixel width and height.
     *
     * @param g2d The graphics context to set the font of
     * @param text The text that will be displayed. This need not be the exact text to be displayed, but can, for example, be text
     *             of the largest expected length for a component to keep the text a consistent size as the length changes.
     * @param pixelWidth The number of pixels wide that is available to draw the text
     * @param pixelHeight The number of pixels high that is available to draw the text
     */
    public static void setFontFromWidthAndHeight(Graphics2D g2d, String text, int pixelWidth, int pixelHeight)
    {
        Font font = g2d.getFont();
        Rectangle2D rect = g2d.getFontMetrics(font).getStringBounds(text, g2d);
        g2d.setFont(font.deriveFont((float)Math.min(((double)font.getSize2D() * pixelWidth/rect.getWidth()),
                                                    ((double)font.getSize2D() * pixelHeight/rect.getHeight()))));
    }

    /**
     * Sets the global default font for the program.
     *
     * @param font A default font to set for the program
     */
    public static void setGlobalFont(FontUIResource font)
    {
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(UIManager.getDefaults().keys().asIterator(), Spliterator.ORDERED), false)
                     .filter(key -> UIManager.get(key) instanceof FontUIResource)
                     .forEach(key -> UIManager.put(key, font));
    }

    /**
     * Converts a temperature from celsius to fahrenheit.
     *
     * @param celsius The temperature in celsius
     * @return A temperature in fahrenheit
     */
    public static double celsiusToFahrenheit(double celsius)
    {
        return celsius * 1.8 + 32;
    }

    /**
     * Loads image at the given path and scales it to the given size.
     *
     * If the image fails to be loaded, exits the program.
     *
     * @param iconPath The path to load the icon from
     * @param width The width to set the image
     * @param width The height to set the image
     * @return An Image set to the given size
     */
    public static Image loadImage(String iconPath, int width, int height)
    {
        try
        {
            return ImageIO.read(new File(iconPath)).getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
        catch (IOException e)
        {
            logError("Failed to load icon: " + iconPath, e);
            System.exit(1);
            return null;
        }
    }

    /**
     * Centers the given component on the given monitor.
     *
     * @param component The component to center
     * @param device The monitor to center the component on
     */
    public static void centerComponent(Component component, GraphicsDevice device)
    {
        Rectangle bounds = device.getDefaultConfiguration().getBounds();
        Dimension size = component.getSize();
        component.setLocation(bounds.x + (bounds.width - size.width) / 2,
                              bounds.y + (bounds.height - size.height) / 2);
    }

    /**
     * Creates a new TimerTask using the given Runnable.
     *
     * This allows construction of TimerTasks using lambda expressions.
     *
     * @param runnable The runnable to use for the TimerTask's run() method.
     * @return A TimerTask
     */
    public static TimerTask timer(Runnable runnable)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                runnable.run();
            }
        };
    }

    /**
     * Launches the given program.
     *
     * This method ensures the executable exists before attempting to launch it.
     *
     * @param fullPath The full path to the program
     * @param allowMultipleInstances False to only launch the program if it is not already running,
     *                               true to allow multiple instances
     */
    public static void launchProgram(String fullPath, boolean allowMultipleInstances)
    {
        //ensure the executable exists
        if (!Files.exists(Paths.get(fullPath)))
        {
            logWarning("The executable \"" + fullPath + "\" does not exist, so the program cannot be started.");
            return;
        }

        String executable = fullPath.substring(fullPath.lastIndexOf('/') + 1);

        //the program does not need to be started because it is already running
        if (!allowMultipleInstances && programRunning(executable))
        {
            log(executable + " is already running");
            return;
        }

        //get the path without the executable in it
        String path = fullPath.substring(0, fullPath.lastIndexOf('/'));

        //launch the program
        try
        {
            Process process = new ProcessBuilder("cmd", "/c", "start /D \"" + path + "\" " + executable)
                              .redirectErrorStream(true).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                List<String> lines = reader.lines().collect(Collectors.toList());
                if (!lines.isEmpty())
                {
                    logWarning("Unexpected output attempting to startup " + executable + ": " +
                                lines.stream().collect(Collectors.joining("\n")));
                }
                else
                {
                    String name = executable.split("\\.")[0];
                    log(name + " started successfully");
                }

                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    logError(executable + " exited with error code " + exitCode);
                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            logError("Unable to start " + executable + " automatically", e);
        }
    }

    /**
     * Starts a Task Scheduler task to launch a program.
     *
     * This method is especially useful to launch programs as another user, something Windows does not have a direct command for.
     * This can only be done if this Java program was launched with administrative privileges.
     *
     * @param taskName The name of the Task Scheduler task to run
     * @param executable The executable name (unused if allowMultipleInstances is true)
     * @param allowMultipleInstances False to only launch the program if it is not already running,
     *                               true to allow multiple instances
     */
    public static void runTaskSchedulerTask(String taskName, String executable, boolean allowMultipleInstances)
    {
        //the task does not need to be run because the program is already running
        if (!allowMultipleInstances && programRunning(executable))
        {
            log(executable + " is already running");
            return;
        }

        //run the task
        try
        {
            Process process = new ProcessBuilder("cmd", "/c", "schtasks /run /tn " + taskName)
                              .redirectErrorStream(true).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                List<String> lines = reader.lines().collect(Collectors.toList());

                //the task started successfully
                if (lines.size() == 1 &&
                    lines.get(0).equals("SUCCESS: Attempted to run the scheduled task \"" + taskName + "\"."))
                {
                    String name = executable.split("\\.")[0];
                    log(name + " started successfully");
                }
                //something went wrong
                else
                {
                    logWarning("Unexpected output attempting to run " + taskName + " via Task Scheduler: " +
                                lines.stream().collect(Collectors.joining("\n")));
                }

                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    logError("The Task Scheduler task " + taskName + " exited with error code " + exitCode);
                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            logError("Unable to start Task Scheduler task " + taskName + " automatically", e);
        }
    }

    /**
     * Determines if any instances of the given executable are already running.
     *
     * If an exception occurs, this method will log it and return true.
     *
     * @param executable The executable to check for instances of
     * @return True if the given executable is already running, false otherwise
     */
    private static boolean programRunning(String executable)
    {
        boolean programRunning = true; //assume the program is running by default

        try
        {
            Process process = new ProcessBuilder("cmd", "/c", "tasklist /fi \"imagename eq " + executable + "\"")
                              .redirectErrorStream(true).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    //this line indicates no instances of the program are currently running
                    if ("INFO: No tasks are running which match the specified criteria.".equals(line))
                    {
                        programRunning = false;
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    logError("The command to check for any instances of " + executable + " exited with error code " + exitCode);
                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            logError("Unable to determine if " + executable + " is currently running", e);
        }

        return programRunning;
    }
}
