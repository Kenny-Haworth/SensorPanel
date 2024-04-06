package src.figure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import src.Constants;
import src.Sensor;

/**
 * This class creates a modern, smooth gauge.
 */
public final class SleekGauge extends Figure
{
    private static final int MAX_ANGLE = 300;
    private final Sensor sensor;
    private final int gaugeWidth;

    /**
     * Creates a new SleekGauge.
     *
     * @param sensor The sensor who's values should be displayed on this gauge
     */
    public SleekGauge(Sensor sensor)
    {
        super(sensor);
        this.sensor = sensor;
        this.setBackground(Color.BLACK);

        //make this panel 45% of the height of the frame and square in size
        this.setPreferredSize(new Dimension((int)(Constants.FRAME_HEIGHT * 0.45),
                                            (int)(Constants.FRAME_HEIGHT * 0.45)));

        //make the gauge width 8% of the width of this panel
        this.gaugeWidth = (int)(this.getPreferredSize().width * 0.08);
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
        g2d.fillArc(0, 0, this.getWidth(), this.getHeight(), startingAngle - angle, -MAX_ANGLE + angle);

        //draw the used portion of the gauge
        g2d.setColor(Constants.THEME_COLOR);
        g2d.fillArc(0, 0, this.getWidth(), this.getHeight(), startingAngle, -angle);

        //fill the interior of the gauge in black
        g2d.setColor(Color.BLACK);
        g2d.fillArc(this.gaugeWidth, this.gaugeWidth,
                    this.getWidth() - (this.gaugeWidth * 2),
                    this.getHeight() - (this.gaugeWidth * 2),
                    0, 360);

        //fill in a white circle at the border of the used and unused portion of the gauge
        g2d.setColor(Color.WHITE);

        double cosX = Math.cos(Math.toRadians(startingAngle - angle));
        double sinY = Math.sin(Math.toRadians(startingAngle - angle));
        double widthX = this.getWidth() * (1 + cosX)/2;
        double heightY = this.getHeight() * (1 - sinY)/2;

        g2d.fillOval((int)(widthX - (this.gaugeWidth * widthX/this.getWidth())),
                     (int)(heightY - (this.gaugeWidth * heightY/this.getHeight())),
                     this.gaugeWidth, this.gaugeWidth);

        //calculate the font size
        int fontWidthPixels = this.getWidth() - this.gaugeWidth * 4;
        Font font = g2d.getFont();
        Rectangle2D rect = g2d.getFontMetrics(font).getStringBounds("100", g2d);
        g2d.setFont(font.deriveFont((float)((double)font.getSize2D() * fontWidthPixels/rect.getWidth())));

        //display the sensor's value in the center
        FontMetrics metrics = g2d.getFontMetrics();
        String value = String.valueOf(Math.round(this.sensor.getData()));
        g2d.drawString(value,
                       this.getWidth()/2 - metrics.stringWidth(value)/2,
                       this.getHeight()/2 + metrics.getAscent()/3);

        //display the units at the bottom
        g2d.setFont(new Font("Arial", Font.PLAIN, (int)(g2d.getFont().getSize()/1.75)));
        metrics = g2d.getFontMetrics();
        String unit = this.sensor.unit().toString();
        g2d.drawString(unit,
                       this.getWidth()/2 - metrics.stringWidth(unit)/2,
                       this.getHeight() - metrics.getAscent()/4);
    }
}
