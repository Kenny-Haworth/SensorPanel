package src.util;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * A JPanel that supports rounded borders, rectangular borders, and variable border thickness and gap separation.
 */
public final class RoundedPanel extends JPanel
{
    private final int roundness;
    private final int thickness;
    private final int separation;

    /**
     * Creates a new RoundedPanel.
     *
     * The color of the border is determined by the foreground color.
     *
     * @param roundness The higher the value, the more rounded the edges of the panel will be. A value of 0 will create a
     *                  completely rectangular border. A value of 50-100 is standard for a slightly rounded border.
     * @param thickness The pixel thickness of the border
     * @param separation How many pixels of a gap to leave between the border and edge of the panel. Recommended minimum is half
     *                   the value of the thickness parameter.
     */
    public RoundedPanel(int roundness, int thickness, int separation)
    {
        super();
        this.roundness = roundness;
        this.thickness = thickness;
        this.separation = separation;
    }

    @Override
    public void paintBorder(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(this.thickness));
        g2d.drawRoundRect(this.separation, this.separation,
                          this.getWidth() - this.separation * 2,
                          this.getHeight() - this.separation * 2,
                          this.roundness, this.roundness);
    }

    /**
     * Returns the amount of pixels the border uses (in either the x or y direction),
     * including some blank space to ensure components don't get too close to the border.
     *
     * @return The amount of pixels used to draw the border
     */
    public int getBorderReservedSpace()
    {
        return (this.thickness + this.separation) * 2;
    }
}
