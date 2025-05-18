package src.figure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import src.Constants;
import src.Sensor;
import src.util.Utils;

/**
 * This class creates a thermostat.
 */
public final class Thermostat extends Figure
{
    private static final double BULB_SIZE = 0.73; //percentage from 0 to 1
    private static final int BULB_MAX_ANGLE = 300; //180 to 360 range
    private final Sensor sensor; //the sensor to display the data of
    private final int thermWidth; //the wall width of this thermostat
    private final Image icon; //an icon to display in the upper right

    /**
     * Creates a new thermostat.
     *
     * @param sensor The sensor to display the data of
     * @param iconPath The path to an icon to load and display
     * @param size The size to set this thermostat
     */
    public Thermostat(Sensor sensor, String iconPath, Dimension size)
    {
        super(sensor);
        this.sensor = sensor;
        this.setBackground(Color.BLACK);
        this.setPreferredSize(size);

        //make the thermometer's width a percentage of the width of this panel
        this.thermWidth = (int)(this.getPreferredSize().width * 0.035);
        int iconSize = (int)(this.getPreferredSize().width * 0.3);
        this.icon = Utils.loadImage(iconPath, iconSize, iconSize);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Constants.THEME_COLOR);

        //determine the interior size of the thermostat's bulb
        int diameter = (int)(this.getWidth() * BULB_SIZE);
        double radius = diameter/2D;

        //fill the bottom of the thermostat, an upside-down unclosed arc
        g2d.fillArc((int)(this.getWidth()/2D - radius),
                    this.getHeight() - diameter,
                    diameter, diameter,
                    -(BULB_MAX_ANGLE/2 + 90), BULB_MAX_ANGLE);

        //draw vertical lines as sides of the thermostat
        g2d.setStroke(new BasicStroke(this.thermWidth));
        double radAngle = Math.toRadians((BULB_MAX_ANGLE - 180)/2); //angle from the horizontal up to the arc's end
        double cosX = Math.cos(radAngle);

        int xLeftLine = (int)(this.getWidth()/2D - (cosX * radius) + this.thermWidth/2D);
        int xRightLine = (int)(this.getWidth()/2D + (cosX * radius) - this.thermWidth/2D);
        int yLowerHeight = (int)(this.getHeight() - radius);

        int arc2diameter = (int)Math.round(diameter * cosX); //the diameter and radius of the top of the thermostat
        double arc2radius = arc2diameter/2D;

        g2d.drawLine(xLeftLine, (int)arc2radius, xLeftLine, yLowerHeight); //left vertical line
        g2d.drawLine(xRightLine, (int)arc2radius, xRightLine, yLowerHeight); //right vertical line

        g2d.setStroke(new BasicStroke(1));

        //overwrite the interior of the bulb with empty space
        g2d.setColor(Color.BLACK);
        g2d.fillArc((int)(this.getWidth()/2D - radius + this.thermWidth),
                    this.getHeight() - diameter + this.thermWidth,
                    diameter - this.thermWidth * 2,
                    diameter - this.thermWidth * 2,
                    0, 360);

        //fill the top of the thermostat, a single connecting arc
        g2d.setColor(Constants.THEME_COLOR);
        g2d.fillArc((int)(this.getWidth()/2D - arc2radius),
                    0,
                    arc2diameter,
                    arc2diameter,
                    0, 180);

        //overwrite the interior of the top of the thermostat with empty space
        g2d.setColor(Color.BLACK);
        g2d.fillArc((int)(this.getWidth()/2D - arc2radius + this.thermWidth),
                    this.thermWidth + 1, //+1 offset due to aliasing not fully overwriting the bottom of the top of the thermostat
                    arc2diameter - this.thermWidth * 2,
                    arc2diameter - this.thermWidth * 2,
                    0, 180);

        //determine what percentage the interior of the bulb constitutes the entire fillable height
        double fillPerc = ((this.sensor.getData() - this.sensor.min()) / (this.sensor.max() - this.sensor.min()));
        int fillableHeight = this.getHeight() - this.thermWidth * 2;

        g2d.setClip(0, this.thermWidth + (int)((1 - fillPerc) * fillableHeight), this.getWidth(), this.getHeight());

        //fill the interior of the bulb
        g2d.setColor(Color.RED);
        g2d.fillArc((int)(this.getWidth()/2D - radius + this.thermWidth),
                    this.getHeight() - diameter + this.thermWidth,
                    diameter - this.thermWidth * 2,
                    diameter - this.thermWidth * 2,
                    0, 360);

        //fill the vertical section
        g2d.fillRect(xLeftLine + this.thermWidth/2,
                     (int)arc2radius,
                     arc2diameter - this.thermWidth * 2,
                     this.getHeight() - diameter);

        //fill the top of the thermostat
        g2d.fillArc((int)(this.getWidth()/2D - arc2radius + this.thermWidth),
                    this.thermWidth,
                    arc2diameter - this.thermWidth * 2,
                    arc2diameter - this.thermWidth * 2,
                    0, 180);

        g2d.setClip(null);
        g2d.setColor(Color.WHITE);

        //set the font size
        String value = this.sensor.getRoundedData() + this.sensor.unit();
        Utils.setFontFromWidth(g2d, value + "0", diameter - this.thermWidth);

        //display the sensor's value in the center
        FontMetrics metrics = g2d.getFontMetrics();
        g2d.drawString(value,
                       this.getWidth()/2 - metrics.stringWidth(value)/2,
                       (int)(this.getHeight() - radius + metrics.getAscent()/3));

        //display the icon in the upper right
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(this.icon, this.getWidth() - this.icon.getWidth(null), 0, null);
    }
}
