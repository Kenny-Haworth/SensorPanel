package src;

/**
 * An enum of different unit types and their abbreviations.
 */
public enum Unit
{
    WATTS,
    DOLLARS,
    PERCENTAGE,
    DEGREES_CELSIUS,
    DEGREES_FAHRENHEIT,
    FRAMES_PER_SECOND,
    MEGABYTES_PER_SECOND;

    /**
     * Whether this unit should be prepended or appended to the value.
     * If a unit should not be prepended, it should be appended.
     *
     * For example, "%" should be at the end of a value (83%) but "$" should be at the beginning of a value "$35".
     *
     * @return True if this unit should be prepended, false if it should be appended
     */
    public boolean prepend()
    {
        return this == DOLLARS;
    }

    @Override
    public String toString()
    {
        return switch (this)
        {
            case WATTS -> "W";
            case DOLLARS -> "$";
            case PERCENTAGE -> "%";
            case DEGREES_CELSIUS -> "°C";
            case DEGREES_FAHRENHEIT -> "°F";
            case FRAMES_PER_SECOND -> "fps";
            case MEGABYTES_PER_SECOND -> "MB/s";
        };
    }
}
