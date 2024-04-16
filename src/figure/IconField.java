package src.figure;

import static src.util.Logger.logError;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import src.Constants;
import src.Sensor;
import src.util.Utils;

/**
 * This class displays an icon with a Sensor's data to the right of it.
 */
public final class IconField extends Figure
{
    //variables common to all IconFields
    private static final int WIDTH = (int)(Constants.FRAME_WIDTH * 0.125);
    private static final int HEIGHT = (int)(Constants.FRAME_HEIGHT * 0.15);
    private static final float FONT_SIZE;
    private static final float SMALL_FONT_SIZE;

    //other member variables
    private final Sensor sensor; //the sensor to display the data of
    private final Image icon; //the icon to display

    //initialize font sizes
    static
    {
        //create a temporary buffer to obtain a Graphics instance
        BufferedImage buffer = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = (Graphics2D)buffer.getGraphics();

        //determine the standard font size
        Utils.setFontFromWidthAndHeight(g2d, "00 Mb/s", WIDTH - HEIGHT, HEIGHT);
        FONT_SIZE = g2d.getFont().getSize();

        //determine the small font size for longer text
        Utils.setFontFromWidthAndHeight(g2d, "000 Mb/s", WIDTH - HEIGHT, HEIGHT);
        SMALL_FONT_SIZE = g2d.getFont().getSize();
    }

    /**
     * Creates a new IconField.
     *
     * @param sensor The sensor to display the data of
     * @param iconPath The path to the icon to display
     */
    public IconField(Sensor sensor, String iconPath)
    {
        super(sensor);
        this.sensor = sensor;
        this.setBackground(Color.BLACK);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        //load the icon
        Image iconTmp = null;

        try
        {
            //resize the icon to be square and the same height as the panel
            iconTmp = ImageIO.read(new File(iconPath)).getScaledInstance(HEIGHT, HEIGHT, Image.SCALE_SMOOTH);
        }
        catch (IOException e)
        {
            logError("Unable to load the following icon: " + iconPath, e);
        }

        this.icon = iconTmp;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        //initial setup
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);

        //get the text to display
        String value = this.sensor.getRoundedData() + " " + this.sensor.unit();
        g2d.setFont(g2d.getFont().deriveFont(value.length() <= 7 ? FONT_SIZE : SMALL_FONT_SIZE));

        //display the text to the right of the icon, center aligned
        FontMetrics metrics = g2d.getFontMetrics();

        double emptySpace = this.getWidth() - this.icon.getWidth(null) - metrics.stringWidth(value);
        g2d.drawString(value, this.icon.getWidth(null) + (int)emptySpace/2,
                       (this.getHeight() - metrics.getHeight())/2 + metrics.getAscent());

        //display the icon on the left side
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(this.icon, 0, 0, null);
    }
}
