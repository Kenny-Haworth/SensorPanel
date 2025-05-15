package src;

import static src.util.Logger.logWarning;

import java.text.DecimalFormat;

import src.figure.Figure;

/**
 * An enum to maintain information about various Sensors.
 *
 * About Sensors:
 *      • Sensors can be hardware or software related
 *      • Sensors can be updated at different rates
 *      • Each Sensor stores the latest data it received
 *      • Each Sensor can be associated with one and only one Figure which will be repainted automatically when the Sensor is
 *        updated
 *      • A Sensor's min and max indicate the smallest and largest values that Figures should expect
 *          ◦ Some Figures, such as IconField, may not use these values, but it is important for other Figures such as gauges and
 *            thermostats
 *      • A Sensor's warning min and max indicate values in a concerning range (such as high temperatures) but which are not
 *        outside the range of what the Sensor could report
 *      • The order Sensor values are declared matters - it must match the same order Sensors are exported in HwInfo
 */
@SuppressWarnings("java:S3066") //this enum is designed to have a mutable, thread-safe state (with up to 1 setter and many getters)
public enum Sensor
{
    RAM_USAGE                 (Unit.PERCENTAGE,            0,    100,     0,     95),
    MAX_SINGLE_CORE_CPU_USAGE (Unit.PERCENTAGE,            0,    100,   7.5,    100), //highest single-core CPU usage
    COMBINED_CPU_USAGE        (Unit.PERCENTAGE,            0,    100,     0,    100),
    CPU_TEMPERATURE           (Unit.DEGREES_CELSIUS,      35,     89,    35,     89),
    CPU_POWER_USAGE           (Unit.WATTS,                 0,    170,    20,    150),
    GPU_TEMPERATURE           (Unit.DEGREES_CELSIUS,      24,     85,    24,     65),
    GPU_POWER_USAGE           (Unit.WATTS,                 0,    450,    12,    450),
    GPU_USAGE                 (Unit.PERCENTAGE,            0,    100,     0,    100),
    VRAM_USAGE                (Unit.PERCENTAGE,            0,    100,     0,     95),
    FPS                       (Unit.FRAMES_PER_SECOND,     0, 10_000,     0, 10_000),
    INTERNET_DOWNLOAD_USAGE   (Unit.MEGABITS_PER_SECOND,   0,    600,     0,    600),
    INTERNET_UPLOAD_USAGE     (Unit.MEGABITS_PER_SECOND,   0,     35,     0,     25),
    AIR_TEMPERATURE           (Unit.DEGREES_FAHRENHEIT,   60,    110,    60,     91), //inside the case
    WATER_TEMPERATURE         (Unit.DEGREES_FAHRENHEIT,   60,    110,    60,    105), //i.e. coolant temperature
    SYSTEM_POWER_USAGE        (Unit.WATTS,                 0,   1500,   100,    720), //total system power usage measured at the outlet
    SECONDARY_POWER_USAGE     (Unit.WATTS,              -500,   1500,  -500,    720), //system power usage excluding the CPU and GPU
    SYSTEM_COST_PER_HOUR      (Unit.CENTS,                 0,    100,     5,   45.5); //calculated from total system power usage, maximum set from a 720W maximum draw at 65¢ per kwh

    //member variables
    protected static final Sensor[] VALUES = Sensor.values(); //saved to avoid expensive copying
    private final double min; //the smallest value this Sensor should ever reach
    private final double max; //the largest value this Sensor should ever reach
    private final double warningMin; //the value beneath which warnings should be emitted
    private final double warningMax; //the value above which warnings should be emitted
    private final Unit unit; //the Unit for this Sensor
    private volatile double data; //the raw data for this Sensor
    private volatile Figure figure; //the Figure this Sensor is displayed on - NOSONAR, the object's state is NOT updated here

    /**
     * Creates a new Sensor.
     *
     * @param unit The Unit for this Sensor
     * @param min The smallest value this Sensor should ever reach
     * @param max The largest value this Sensor should ever reach
     * @param warningMin The value beneath which warnings should be emitted
     * @param warningMax The value above which warnings should be emitted
     */
    private Sensor(Unit unit, double min, double max, double warningMin, double warningMax)
    {
        this.unit = unit;
        this.min = min;
        this.max = max;
        this.warningMin = warningMin;
        this.warningMax = warningMax;
    }

    /**
     * Sets the Figure for this Sensor.
     *
     * The given Figure will be repainted when this Sensor updates.
     *
     * @param figure The Figure associated with this Sensor
     */
    public void setFigure(Figure figure)
    {
        this.figure = figure;
    }

    /**
     * Sets this Sensor to the given value.
     *
     * @param data The value to set this Sensor to
     */
    public void set(double data)
    {
        //set the Sensor's value
        this.data = data;

        //warn about values outside the warning threshold
        if (this.data > this.warningMax)
        {
            logWarning("High " + this);
        }
        else if (this.data < this.warningMin)
        {
            logWarning("Low " + this);
        }

        //warn about values outside the max and min
        if (this.data > this.max)
        {
            logWarning("Critically high " + this);
        }
        else if (this.data < this.min)
        {
            logWarning("Critically low " + this);
        }

        //repaint the figure
        if (this.figure != null) //null figures means the value is unused and not displayed on the GUI
        {
            this.figure.repaint();
        }
    }

    /**
     * Returns the data for this Sensor.
     *
     * @return The data for this Sensor
     */
    public double getData()
    {
        return this.data;
    }

    /**
     * Returns the rounded data for this Sensor converted to a String.
     *
     * @return The rounded data for this Sensor
     */
    public String getRoundedData()
    {
        return switch (this)
        {
            case SYSTEM_COST_PER_HOUR -> new DecimalFormat("##.#").format(this.data);
            default -> String.valueOf(Math.round(this.data));
        };
    }

    /**
     * Returns the minimum data value for this Sensor.
     *
     * @return The minimum data value
     */
    public double min()
    {
        return this.min;
    }

    /**
     * Returns the maximum data value for this Sensor.
     *
     * @return The maximum data value
     */
    public double max()
    {
        return this.max;
    }

    /**
     * Returns the Unit for this Sensor.
     *
     * @return The Unit
     */
    public Unit unit()
    {
        return this.unit;
    }

    @Override
    public String toString()
    {
        return this.name().replace('_', ' ') + " " + getData() + " " + this.unit;
    }
}
