package src;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.Point;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.plaf.FontUIResource;

/**
 * A custom SensorPanel for a 1920x515 display within a gaming computer.
 *
 * This program supports the following features:
 *      TODO
 *
 * The following sensors are monitored and displayed:
 *      TODO
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

    private boolean lockPosition = true;
    private Point dragPoint; //to allow dragging the frame by clicking on any part of it

    /**
     * Creates the sensor panel GUI.
     */
    public SensorPanel()
    {
        //set the global font for the program
        Utils.setGlobalFont(new FontUIResource(Font.MONOSPACED, Font.PLAIN, 20));

        //create the main frame
        JFrame frame = new JFrame("Sensor Panel");
        frame.setSize(1920, 515); //TODO needs to work with the scaling of Windows
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().setBackground(Color.BLUE);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent event)
            {
                dragPoint = event.getPoint();

                //create a popup menu when the user right clicks
                /**
                 * Create a popup menu when the user right clicks with the following options:
                 *      • Toggle the frame being always on top
                 *      • Toggle displaying the frame's border
                 *      • Toggle allowing the frame to resize
                 *      • Toggle locking the frame's position
                 *      • TODO Resetting the frame to its default size
                 *      • TODO Resetting the frame to its default position
                 *      • TODO Resetting the frame to its default size and position
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

                    //construct all the components into the popup menu
                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.add(onTopItem);
                    popupMenu.add(borderItem);
                    popupMenu.add(resizeItem);
                    popupMenu.add(positionItem);

                    //display the popup menu
                    popupMenu.show(frame, event.getX(), event.getY());
                }
            }
        });

        //allow the user to move the frame by clicking and dragging on any part of the frame
        frame.addMouseMotionListener(new MouseMotionAdapter()
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
    }
}
