package src;

/**
 * An enum of different unit types and their abbreviations.
 */
public enum Unit
{
    WATTS,
    CENTS,
    PERCENTAGE,
    DEGREES_CELSIUS,
    DEGREES_FAHRENHEIT,
    FRAMES_PER_SECOND,
    MEGABITS_PER_SECOND;

    @Override
    public String toString()
    {
        return switch (this)
        {
            case WATTS -> "W";
            case CENTS -> "¢";
            case PERCENTAGE -> "%";
            case DEGREES_CELSIUS -> "°C";
            case DEGREES_FAHRENHEIT -> "°F";
            case FRAMES_PER_SECOND -> "fps";
            case MEGABITS_PER_SECOND -> "Mb/s";
        };
    }
}
