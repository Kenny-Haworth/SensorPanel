package src.util;

import static src.util.Logger.log;
import static src.util.Logger.logError;
import static src.util.Logger.logWarning;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.TimerTask;

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
     * This method first ensures the executable exists and the program is not already running.
     *
     * @param path The full path to the program, excluding the executable
     * @param executable The executable name
     */
    public static void launchProgram(String path, String executable)
    {
        //ensure the executable exists
        String fullPath = path + "\\" + executable;
        if (!Files.exists(Paths.get(fullPath)))
        {
            logWarning("The executable \"" + fullPath + "\" does not exist, so the program cannot be started.");
            return;
        }

        //check if the program is already running
        try
        {
            Process process = new ProcessBuilder("cmd", "/c", "tasklist /fi \"imagename eq " + executable + "\"")
                              .redirectErrorStream(true).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                boolean programRunning = true;
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
                    logError("Checking if " + executable + " is alive exited with error code " + exitCode);
                }

                //the program does not need to be started because it is already running
                if (programRunning)
                {
                    log(executable + " is already running");
                    return;
                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            logError("Unable to determine if " + executable + " is currently running", e);
            return; //better to not start the program at all than to run it twice
        }

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
}
