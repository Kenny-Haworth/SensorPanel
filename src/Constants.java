package src;

import java.awt.Color;
import java.util.regex.Pattern;

/**
 * A class to hold various constants for this program.
 */
public final class Constants
{
    /**
     * Prevents instantiation of this class.
     */
    private Constants() {}

    public static final int FRAME_WIDTH = 1920;
    public static final int FRAME_HEIGHT = 515;
    public static final int UPDATE_RATE_SECONDS = 3;
    public static final Color THEME_COLOR = new Color(41, 171, 250); //a light cyan
    public static final double CENTS_PER_KWH = 65;
    public static final Pattern SPLIT_SPACES = Pattern.compile("\\p{Space}+");
}
