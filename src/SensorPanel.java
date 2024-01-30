package src;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.plaf.FontUIResource;
import javax.swing.SwingUtilities;

/**
 * A custom SensorPanel for a display within a Windows gaming computer.
 *
 * This program supports the following features:
 *      TODO
 *
 * The following sensors are monitored and displayed:
 *      • CPU usage (percent, combined)
 *      • CPU usage (percent, max of any core)
 *      • CPU temperature (°C)
 *      • CPU power consumption (watts)
 *      • GPU usage (percent)
 *      • GPU VRAM usage (percent)
 *      • GPU temperature (°C)
 *      • GPU power consumption (watts)
 *      • RAM usage (percent)
 *      • SSD and HDD temperatures (°C)
 *      • SSD and HDD read and write rates (MB/s)
 *          ◦ The above 2 options must not turn on sleeping HDDS
 *      • Internet up and down usage (MB/s)
 *      • Total system estimated power usage (watts)
 *      • Total system estimated cost to use per hour (USD)
 *          ◦ The above two options only work if the computer power consumption can be accurately estimated
 *          ◦ May be able to estimate power usage using UPS
 *      • Water temperature (°F)
 *      • Air temperature in case (°F)
 *      • FPS (possibly with a graph of over time)
 *      • Optional:
 *          ◦ Pump speed (percent)
 *          ◦ Fan speeds (percent)
 *          ◦ Disk utilization (percent)
 *          ◦ Date
 *          ◦ Time
 *          ◦ Outside temperature
 *          ◦ Weather forecast
 *              ▪ Rain, snow, rainstorm, thunderstorm, fog, overcast, sunny, partially cloudy, hail, windy
 *              ▪ The day's high, low, and current temperature
 *
 * TODO implement the following features
 *      • When the user mouses over the display, show something different
 *          ◦ Maybe giant font that says the status is one word - NORMAL, MODERATE, CRITICAL
 *      • Connect speakers to the breadboard. Then when temperatures get too high, programmatically emitt noise from the speaker
 *        and have a computer (an audible alarm) and say something out loud 3 times like "High GPU temperature, 70 C" or other
 *        things like that
 *      • On the StreamDeck, when the selected RGB is changed, send a signal to this program to change the background color.
 *        Probably something like writing the color we want to use to a file.
 *      • Set a thermostat for program icon
 *      • Automatically detect when the sensor panel monitor is connected to Windows. When it is, move the sensor panel to this
 *        display (regardless of other dispay connections/disconnections) and show it. When it is not, disable this sensor panel.
 *        This sensor panel should do little to no processing when the monitor is disabled and receive a Windows callback
 *        (possibly through JNA) when a new monitor is connected to determine if the sensor panel should be shown again.
 *      • Two GUI view modes - themed to a color or color range and graded based on temperature (green to red). Consider a
 *        combination of these two or a switchover from themed to temperature-colored once certain temperatures are met.
 *          ◦ Consider - each hardware component goes into a separate panel on the main display. If any children of that panel
 *            throw a temperature warning, the entire panel changes color. This could be a good solution for a combination of
 *            theme-driven colors and temperature-driven colors
 *      • System logging
 *          ◦ Power-on time
 *          ◦ Total uptime
 *          ◦ Total power usage (kwh)
 *          ◦ Save the above information on a per-day basis for viewing later
 */
public final class SensorPanel
{
    /**
     * The entry point of the program.
     *
     * @param args Ignored
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(SensorPanel::new);
    }

    private static final int FRAME_WIDTH = 1920;
    private static final int FRAME_HEIGHT = 515;
    private final JFrame frame; //the main frame for the program
    private boolean lockPosition = true; //to allow for locking or unlocking the frame's position
    private Point dragPoint; //to allow dragging the frame by clicking on any part of it

    /**
     * Creates the sensor panel GUI.
     */
    public SensorPanel()
    {
        //set the global font for the program
        Utils.setGlobalFont(new FontUIResource(Font.MONOSPACED, Font.PLAIN, 20));

        //create the main frame
        this.frame = new JFrame("Sensor Panel");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.getContentPane().setBackground(Color.BLUE);
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

                    //add an option to toggle locking the position of the grame
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

        //set the frame's size, position, and attributes and show it
        resetFrame();
        this.frame.setVisible(true);
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
            if (device.getDisplayMode().getWidth() == FRAME_WIDTH &&
                device.getDisplayMode().getHeight() == FRAME_HEIGHT)
            {
                //position the frame onto the sensor panel and use the bounds to size it to account for Windows scaling
                this.frame.setSize(device.getDefaultConfiguration().getBounds().getSize());
                this.frame.setLocation(device.getDefaultConfiguration().getBounds().getLocation());
                break;
            }
        }
    }
}
