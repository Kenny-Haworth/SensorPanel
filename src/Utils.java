package src;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * A class to hold various, non-related utilities.
 */
public final class Utils
{
    public static void setGlobalFont(FontUIResource font)
    {
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(UIManager.getDefaults().keys().asIterator(), Spliterator.ORDERED), false)
                     .filter(key -> UIManager.get(key) instanceof FontUIResource)
                     .forEach(key -> UIManager.put(key, font));
    }
}
