package src.figure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import src.Constants;
import src.Sensor;
import src.util.Utils;

/**
 * This class creates a modern, smooth bar.
 */
public final class SleekBar extends Figure
{
    private final Sensor sensor;
    private final String title;
    private final int roundness;
    private final int thickness;
    private final int separation;

    /**
     * Creates a new SleekBar.
     *
     * @param sensor The sensor to display the data of
     * @param dimension The preferred size to set this Figure
     * @param title The title to display at the bottom of this bar - this should be kept very short
     * @param roundness The higher the value, the more rounded the edges of the bar will be. A value of 0 will create
     *                  a completely rectangular bar. A value of 25-75 is standard for a slightly rounded bar.
     * @param thickness The pixel thickness of the border of the bar
     * @param separation How many pixels of a gap to leave between the bar's border and the edge of the panel
     */
    public SleekBar(Sensor sensor, Dimension dimension, String title, int roundness, int thickness, int separation)
    {
        super(this.sensor = sensor);
        this.title = title;
        this.roundness = roundness;
        this.thickness = thickness;
        this.separation = separation;
        this.setBackground(Color.BLACK);
        this.setPreferredSize(dimension);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        //initial setup
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double fillPerc = ((this.sensor.getData() - this.sensor.min()) / (this.sensor.max() - this.sensor.min()));
        int borderSize = this.separation + this.thickness/2;

        //calculate the font size for all rendered text
        Utils.setFontFromWidth(g2d, "100%", this.getWidth() - borderSize * 2);
        FontMetrics metrics = g2d.getFontMetrics();
        int interiorDrawableHeight = this.getHeight() - 2 * borderSize - metrics.getAscent();
        int fillHeight = (int)(borderSize + interiorDrawableHeight * (1 - fillPerc));

        //create the shape of the outline of the bar
        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(this.separation, this.separation,
                                                                   this.getWidth() - this.separation * 2,
                                                                   this.getHeight() - this.separation * 2 - metrics.getAscent(),
                                                                   this.roundness, this.roundness);

        //fill the interior of the bar up to the value
        g2d.setClip(rect);
        g2d.setColor(Color.MAGENTA);
        g2d.fillRect(0, fillHeight, this.getWidth(), this.getHeight());
        g2d.setClip(null);

        //get the percent utilization
        String percentStr = String.valueOf(Math.round(fillPerc * 100)) + "%";
        int drawHeight;

        //greater than 50%, draw the value beneath the fill point
        if (Math.round(fillPerc * 100) > 50)
        {
            drawHeight = fillHeight + metrics.getAscent();
        }
        //less than or equal 50%, draw the value above the fill point
        else
        {
            drawHeight = fillHeight - metrics.getAscent() / 3;
        }

        //draw the percent utilization
        g2d.setColor(Color.WHITE);
        g2d.drawString(percentStr,
                       this.getWidth()/2 - metrics.stringWidth(percentStr)/2,
                       drawHeight);

        //draw the title at the bottom
        g2d.drawString(this.title,
        this.getWidth()/2 - metrics.stringWidth(this.title)/2,
        this.getHeight());

        //create the bar's outline
        g2d.setColor(Constants.THEME_COLOR);
        g2d.setStroke(new BasicStroke(this.thickness));
        g2d.draw(rect);
    }
}
