package src.figure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import src.Sensor;
import src.util.Utils;

/**
 * This class creates a modern, smooth gauge.
 */
public final class SleekGauge extends Figure
{
    private static final int MAX_ANGLE = 300; //how many degrees to close the circle
    private final Sensor sensor; //the sensor to display the data of
    private final Color color; //the color for this gauge
    private final int thickness; //the thickness of this gauge
    private final Image icon; //an icon to display in the upper right

    /**
     * Creates a new SleekGauge.
     *
     * @param sensor The sensor to display the data of
     * @param color The color to set this gauge to
     * @param iconPath The path to an icon to load and display
     * @param height The pixel height to use for this Figure.
     *               This value should not be larger than the width as the gauge should look square in size.
     * @param width The pixel width to use for this Figure. width - height = the amount of pixel space to
     *              the right to place the icon, allowing the icon to be visually separated from the gauge.
     */
    public SleekGauge(Sensor sensor, Color color, String iconPath, int width, int height)
    {
        super(this.sensor = sensor);
        this.color = color;
        this.setBackground(Color.BLACK);
        this.setPreferredSize(new Dimension(width, height));

        //make the thickness a percentage of the height of this panel
        this.thickness = (int)(this.getPreferredSize().height * 0.08);
        int iconSize = (int)(this.getPreferredSize().height * 0.28);
        this.icon = Utils.loadImage(iconPath, iconSize, iconSize);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //get the angle from the sensor's current value
        int startingAngle = MAX_ANGLE/2 - 270;
        int angle = (int)(((this.sensor.getData() - this.sensor.min()) / (this.sensor.max() - this.sensor.min())) * MAX_ANGLE);

        //draw the unused portion of the gauge first
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillArc(0, 0, this.getHeight(), this.getHeight(), startingAngle - angle, -MAX_ANGLE + angle);

        //draw the used portion of the gauge
        g2d.setColor(this.color);
        g2d.fillArc(0, 0, this.getHeight(), this.getHeight(), startingAngle, -angle);

        //fill the interior of the gauge in black
        g2d.setColor(Color.BLACK);
        g2d.fillArc(this.thickness, this.thickness,
                    this.getHeight() - (this.thickness * 2),
                    this.getHeight() - (this.thickness * 2),
                    0, 360);

        //fill in a white circle at the border of the used and unused portion of the gauge
        g2d.setColor(Color.WHITE);

        double cosX = Math.cos(Math.toRadians(startingAngle - angle));
        double sinY = Math.sin(Math.toRadians(startingAngle - angle));
        double widthX = this.getHeight() * (1 + cosX)/2;
        double heightY = this.getHeight() * (1 - sinY)/2;

        g2d.fillOval((int)(widthX - (this.thickness * widthX/this.getHeight())),
                     (int)(heightY - (this.thickness * heightY/this.getHeight())),
                     this.thickness, this.thickness);

        //set the font size
        Utils.setFontFromWidth(g2d, "100", this.getHeight() - this.thickness * 4);

        //display the sensor's value in the center
        FontMetrics metrics = g2d.getFontMetrics();
        String data = this.sensor.getRoundedData();
        g2d.drawString(data,
                       this.getHeight()/2 - metrics.stringWidth(data)/2,
                       this.getHeight()/2 + metrics.getAscent()/3);

        //display the units at the bottom
        g2d.setFont(new Font("Arial", Font.PLAIN, (int)(g2d.getFont().getSize()/1.75)));
        metrics = g2d.getFontMetrics();
        String unit = this.sensor.unit().toString();
        g2d.drawString(unit,
                       this.getHeight()/2 - metrics.stringWidth(unit)/2,
                       this.getHeight() - metrics.getAscent()/4);

        //display the icon in the upper right
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(this.icon, this.getWidth() - this.icon.getWidth(null), 0, null);
    }
}
