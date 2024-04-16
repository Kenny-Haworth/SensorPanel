package src;

import static src.util.Logger.log;
import static src.util.Logger.logError;
import static src.util.Logger.logWarning;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Timer;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.plaf.FontUIResource;
import javax.swing.SwingUtilities;

import src.figure.IconField;
import src.figure.SleekGauge;
import src.figure.Thermostat;
import src.util.Utils;

/**
 * A custom Sensor Panel for a display within a Windows gaming computer.
 *
 * This program supports the following features:
 *      • TODO
 *
 * See Sensor.java for a list of supported sensors.
 */
public final class SensorPanel
{
    //member variables
    private final JFrame frame; //the main frame for the program
    private boolean lockPosition = true; //to allow for locking or unlocking the frame's position
    private Point dragPoint; //to allow dragging the frame by clicking on any part of it

    /**
     * The entry point of the program.
     *
     * @param args Ignored
     */
    public static void main(String[] args)
    {
        //log computer power on time
        log("Computer powered on.");

        //TODO log computer power off time and total uptime
        Runtime.getRuntime().addShutdownHook(new Thread(() -> log("Computer powered off.")));

        //startup all necessary programs
        handleStartupPrograms();

        //create the main GUI
        SwingUtilities.invokeLater(SensorPanel::new);
    }

    /**
     * Creates the sensor panel GUI.
     */
    public SensorPanel()
    {
        //set the global default font for the program
        Utils.setGlobalFont(new FontUIResource("Arial", Font.PLAIN, 20));

        //setup the main frame
        this.frame = new JFrame("Sensor Panel");
        setupMainFrame();

        //construct all components that will be part of the main frame
        SleekGauge singleCoreCpuUsage = new SleekGauge(Sensor.MAX_SINGLE_CORE_CPU_USAGE);
        SleekGauge combinedCpuUsage = new SleekGauge(Sensor.COMBINED_CPU_USAGE);
        Thermostat airTherm = new Thermostat(Sensor.AIR_TEMPERATURE, "res/air.png");
        Thermostat waterTherm = new Thermostat(Sensor.WATER_TEMPERATURE, "res/water.png");
        IconField systemPowerUsage = new IconField(Sensor.SYSTEM_POWER_USAGE, "res/lightning_bolt.jpg");
        IconField secondaryPowerUsage = new IconField(Sensor.SECONDARY_POWER_USAGE, "res/motherboard.png");
        IconField costPerHour = new IconField(Sensor.SYSTEM_COST_PER_HOUR, "res/money.png");
        IconField fps = new IconField(Sensor.FPS, "res/fps.png");
        IconField internetUpload = new IconField(Sensor.INTERNET_UPLOAD_USAGE, "res/up.png");
        IconField internetDownload = new IconField(Sensor.INTERNET_DOWNLOAD_USAGE, "res/down.png");

        //create the main panel to add all components to
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.BLACK);

        //add all the components to the main panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridheight = 6;

        mainPanel.add(singleCoreCpuUsage, gbc);

        gbc.gridx = 1;
        mainPanel.add(combinedCpuUsage, gbc);

        gbc.gridx = 2;
        mainPanel.add(airTherm, gbc);

        gbc.gridx = 3;
        mainPanel.add(waterTherm, gbc);

        gbc.gridx = 4;
        gbc.gridheight = 1;
        mainPanel.add(systemPowerUsage, gbc);

        gbc.gridy++;
        mainPanel.add(secondaryPowerUsage, gbc);

        gbc.gridy++;
        mainPanel.add(costPerHour, gbc);

        gbc.gridy++;
        mainPanel.add(fps, gbc);

        gbc.gridy++;
        mainPanel.add(internetUpload, gbc);

        gbc.gridy++;
        mainPanel.add(internetDownload, gbc);

        //show the main frame
        this.frame.add(mainPanel);
        this.frame.setVisible(true);

        //continually update the sensors using values from different programs
        monitorHwInfoSensors();
        monitorFanControlSensors();
        monitorTpLinkSensors();
    }

    /**
     * Starts up programs that require special privileges or timing.
     *
     * The majority of Windows startup programs can simply be started using the startup folder or Task Scheduler. However, neither
     * of these work to automatically start programs which require administrative privileges and must run using a GUI on a
     * non-admin account.
     *
     * A solution to this is RunAsTool, a third-party program which allows non-admin users to run programs with administrative
     * privileges without requiring an administrative password. While the tool doesn't do this automatically upon a user logging
     * in, it's a simple matter to use Task Scheduler to run RunAsTool when the user logs in to start the necessary programs with
     * administrative privileges. The drawback to this is that it fires a UAC prompt for every program it starts - which can
     * quickly become obnoxious to press "Yes" for every program desired to be launched when logging in.
     *
     * As SensorPanel is started with administrative privileges, any programs it starts up will be started with administrative
     * privileges as well, and these programs can run with GUIs. Thus, only the SensorPanel itself is started using RunAsTool via
     * Task Scheduler, and it starts the rest of the programs requiring administrative privileges, allowing there to be only a
     * single UAC prompt.
     *
     * This leaves one last issue - some programs must start under a standard user's account (non-admin) but must start after an
     * admin program has started. An example of this is FanControl and SignalRGB. FanControl must launch before SignalRGB does,
     * but FanControl must run as an admin and SignalRGB must run under the standard user's account. In Windows, there is no way
     * for an admin to launch a program as another user without supplying the user's password (as there is in Linux). A workaround
     * for this is to directly fire a Task Scheduler task tied to the standard user's account.
     *
     * Thus, admin programs can be launched here as any other program is launched and non-admin programs can be launched using
     * Task Scheduler.
     */
    private static void handleStartupPrograms()
    {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        executor.submit(() -> Utils.launchProgram("C:/Program Files (x86)/FanControl/FanControl.exe", false));
        executor.submit(() -> Utils.launchProgram("C:/Program Files (x86)/MSI Afterburner/MSIAfterburner.exe", false));
        executor.submit(() -> Utils.launchProgram("C:/Program Files (x86)/RivaTuner Statistics Server/RTSS.exe", false));
        executor.submit(() -> Utils.launchProgram("C:/Program Files/HWiNFO64/HWiNFO64.EXE", false));
        executor.submit(() -> Utils.launchProgram("C:/Program Files/Keyboard Chatter Blocker/KeyboardChatterBlocker.exe", true));
        executor.schedule(() -> Utils.runTaskSchedulerTask("\\Custom\\SignalRGB", "SignalRgbLauncher.exe", false), 5, TimeUnit.SECONDS);
        executor.shutdown();
    }

    /**
     * Sets up the main program frame, which includes setting various attributes of the frame including:
     *      • Size
     *      • Position
     *      • Right-click context menu
     *      • Drag ability
     *      • Always being on top
     *      • Not displaying a border
     *      • Not being resizable
     *      • Locking its position
     */
    private void setupMainFrame()
    {
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent event)
            {
                dragPoint = event.getPoint();

                /**
                 * Create a popup menu when the user right clicks with the following options:
                 *      • Toggle the frame being always on top
                 *      • Toggle displaying the frame's border
                 *      • Toggle allowing the frame to resize
                 *      • Toggle locking the frame's position
                 *      • Resetting the frame to its defaults
                 */
                if (SwingUtilities.isRightMouseButton(event))
                {
                    //add an option to toggle the frame being always on top
                    JCheckBoxMenuItem onTopItem = new JCheckBoxMenuItem("Always on Top");
                    onTopItem.setSelected(frame.isAlwaysOnTop());
                    onTopItem.addActionListener(e -> frame.setAlwaysOnTop(onTopItem.isSelected()));

                    //add an option to display the frame's border
                    JCheckBoxMenuItem borderItem = new JCheckBoxMenuItem("Display Border");
                    JCheckBoxMenuItem resizeItem = new JCheckBoxMenuItem("Frame Resizable");
                    borderItem.setSelected(!frame.isUndecorated());
                    borderItem.addActionListener(e ->
                    {
                        if (!borderItem.isSelected())
                        {
                            resizeItem.setSelected(false);
                            frame.setResizable(false);
                        }

                        frame.dispose();
                        frame.setUndecorated(!borderItem.isSelected());
                        frame.setVisible(true);
                    });

                    //add an option to toggle allowing the frame to resize
                    resizeItem.setSelected(frame.isResizable());
                    resizeItem.addActionListener(e ->
                    {
                        if (frame.isUndecorated() && resizeItem.isSelected())
                        {
                            frame.dispose();
                            frame.setUndecorated(false);
                            frame.setVisible(true);
                        }

                        frame.setResizable(resizeItem.isSelected());
                    });

                    //add an option to toggle locking the position of the frame
                    JCheckBoxMenuItem positionItem = new JCheckBoxMenuItem("Lock Position");
                    positionItem.setSelected(lockPosition);
                    positionItem.addActionListener(e -> lockPosition = !lockPosition);

                    //add an option to reset the frame to its default size and position
                    JMenuItem resetItem = new JMenuItem("Reset Frame");
                    resetItem.addActionListener(e -> resetFrame());

                    //construct all the components into the popup menu
                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.add(onTopItem);
                    popupMenu.add(borderItem);
                    popupMenu.add(resizeItem);
                    popupMenu.add(positionItem);
                    popupMenu.add(resetItem);

                    //display the popup menu
                    popupMenu.show(frame, event.getX(), event.getY());
                }
            }
        });

        //allow the user to move the frame by clicking and dragging on any part of the frame
        this.frame.addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                if (!lockPosition)
                {
                    frame.setLocation(frame.getLocation().x + (e.getPoint().x - dragPoint.x),
                                      frame.getLocation().y + (e.getPoint().y - dragPoint.y));
                }
            }
        });

        //set the frame's size, position, and attributes
        resetFrame();
    }

    /**
     * Resets the sensor panel to its defaults. This includes the frame:
     *      • Size
     *      • Position
     *      • Always being on top
     *      • Not displaying a border
     *      • Not being resizable
     *      • Locking its position
     */
    private void resetFrame()
    {
        //reset default settings
        this.frame.setAlwaysOnTop(true);
        this.frame.setResizable(false);
        this.lockPosition = true;

        if (!this.frame.isUndecorated())
        {
            this.frame.dispose();
            this.frame.setUndecorated(true);
            this.frame.setVisible(true);
        }

        //loop over the connected monitors
        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
        {
            //this monitor has the same resolution as the sensor panel
            if (device.getDisplayMode().getWidth() == Constants.FRAME_WIDTH &&
                device.getDisplayMode().getHeight() == Constants.FRAME_HEIGHT)
            {
                //position the frame onto the sensor panel and use the bounds to size it to account for Windows scaling
                this.frame.setSize(device.getDefaultConfiguration().getBounds().getSize());
                this.frame.setLocation(device.getDefaultConfiguration().getBounds().getLocation());
                break;
            }
        }
    }

    /**
     * Periodically updates the sensors from HwInfo.
     *
     * Updating occurs at a fixed interval that should match HwInfo's update rate.
     */
    private static void monitorHwInfoSensors()
    {
        new Timer("HwInfo Sensor Thread").scheduleAtFixedRate(Utils.timer(() ->
        {
            try
            {
                //get the sensor values by querying the Windows registry
                Process process = new ProcessBuilder("cmd", "/c", "reg query HKEY_CURRENT_USER\\SOFTWARE\\HWiNFO64\\VSB")
                                  .redirectErrorStream(true).start();

                StringBuilder builder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
                {
                    reader.lines().forEach(line ->
                    {
                        builder.append(line);
                        line = line.trim();

                        //the line with "ValueRaw" contains the Sensor index and the raw sensor value
                        if (line.startsWith("ValueRaw"))
                        {
                            String[] components = Constants.SPLIT_SPACES.split(line);
                            int index = Integer.parseInt(components[0].substring(8, components[0].length()));
                            double value = Double.parseDouble(components[2]);

                            //convert KB/s to MB/s
                            if (Sensor.VALUES[index] == Sensor.INTERNET_DOWNLOAD_USAGE ||
                                Sensor.VALUES[index] == Sensor.INTERNET_UPLOAD_USAGE)
                            {
                                value = value/1000;
                            }

                            //update the Sensor's value
                            Sensor.VALUES[index].set(value);
                        }
                    });
                }

                //calculate combined power usage of everything but the CPU and GPU
                Sensor.SECONDARY_POWER_USAGE.set(Sensor.SYSTEM_POWER_USAGE.getData() -
                                                 Sensor.CPU_POWER_USAGE.getData() -
                                                 Sensor.GPU_POWER_USAGE.getData());

                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    logError("Reading HwInfo registry values exiting with nonzero value: " + exitCode +
                             ". Output of command: " + builder);
                }
            }
            catch (IOException | InterruptedException e)
            {
                logError("Unable to query HwInfo values", e);
            }
        }),
        5000, //give HwInfo ample time to startup
        Constants.UPDATE_RATE_SECONDS * 1000);
    }

    /**
     * Periodically updates the sensors from FanControl.
     *
     * Updating occurs at the same update rate as FanControl (once every second).
     */
    private static void monitorFanControlSensors()
    {
        new Thread(() ->
        {
            //FanControl updates are sent via UDP socket
            try (DatagramSocket socket = new DatagramSocket(48620))
            {
                byte[] buffer = new byte[16];

                //data will be continuously received
                while (true)
                {
                    //receive the packet
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    //extract the temperature and sensor number from the packet
                    String[] data = new String(packet.getData(), 0, packet.getLength()).split(":");
                    double temperature = Utils.celsiusToFahrenheit(Double.parseDouble(data[1]));

                    //channel 0 is air temperature
                    if ("0".equals(data[0]))
                    {
                        Sensor.AIR_TEMPERATURE.set(temperature);
                    }
                    //channel 1 is water temperature
                    else if ("1".equals(data[0]))
                    {
                        Sensor.WATER_TEMPERATURE.set(temperature);
                    }
                    else
                    {
                        logWarning("Unexpected data received from FanControl UDP socket: " +
                                   Arrays.stream(data).collect(Collectors.joining(" ")));
                    }
                }
            }
            catch (Exception e)
            {
                logError("Exception encountered attempting to query FanControl values", e);
            }
        },
        "FanControl Sensor Thread")
        .start();
    }

    /**
     * Periodically updates the sensors from TpLink.
     *
     * This includes a single HS110 smart plug that provides real-time energy usage information for the PC.
     */
    private static void monitorTpLinkSensors()
    {
        new Timer("TpLink Sensor Thread").scheduleAtFixedRate(Utils.timer(() ->
        {
            try
            {
                //get the sensor value by querying it using kasa
                Process process = new ProcessBuilder("cmd", "/c", "kasa --host 192.168.0.134 --type plug emeter")
                                  .redirectErrorStream(true).start();

                StringBuilder builder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
                {
                    reader.lines().forEach(line ->
                    {
                        builder.append(line);
                        line = line.trim();

                        //the line with "Power" contains the current wattage draw
                        if (line.startsWith("Power:"))
                        {
                            String[] components = Constants.SPLIT_SPACES.split(line);

                            //update the system power usage
                            double currentWattage = Double.parseDouble(components[1]);
                            Sensor.SYSTEM_POWER_USAGE.set(currentWattage);

                            //convert wattage to cost per hour
                            double costPerHour = currentWattage/1000 * Constants.USD_PER_KWH; //TODO base upon TOU
                            Sensor.SYSTEM_COST_PER_HOUR.set(costPerHour);
                        }
                    });
                }

                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    logError("Reading TpLink values using Kasa exiting with nonzero value: " + exitCode +
                             ". Output of command: " + builder);
                }
            }
            catch (IOException | InterruptedException e)
            {
                logError("Unable to query TpLink Sensor values using Kasa", e);
            }
        }),
        0, Constants.UPDATE_RATE_SECONDS * 1000);
    }
}
