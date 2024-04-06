package src;

import static src.Logger.logError;
import static src.Logger.logWarning;

import src.figure.Figure;

/**
 * An enum to maintain information about various Sensors.
 *
 * About Sensors:
 *      • Sensors can be hardware or software related
 *      • Sensors can be updated at different rates
 *      • Each Sensor stores the latest data it received
 *      • Each Sensor can be associated with one and only one Figure, which will be repainted when the Sensor is updated
 *      • A Sensor's min and max indicate the smallest and largest values that Figures should expect
 *          ◦ Some Figures, such as IconField, may not use these values, but it is important for other Figures such as gauges and
 *            thermostats
 *      • A Sensor's warning min and max indicate values in a concerning range (such as high temperatures) but which are not
 *        outside the range of what the Sensor could report
 */
public enum Sensor
{
    RAM_USAGE                 (Unit.PERCENTAGE,            0,  100,    10,  80),
    MAX_SINGLE_CORE_CPU_USAGE (Unit.PERCENTAGE,            0,  100,     5,  90), //highest single-core CPU usage
    COMBINED_CPU_USAGE        (Unit.PERCENTAGE,            0,  100,     0,  80),
    CPU_TEMPERATURE           (Unit.DEGREES_CELSIUS,      35,   87,    35,  85),
    CPU_POWER_USAGE           (Unit.WATTS,                 0,  170,    20, 150),
    GPU_TEMPERATURE           (Unit.DEGREES_CELSIUS,      30,   85,    30,  65),
    GPU_POWER_USAGE           (Unit.WATTS,                 0,  450,    12, 300),
    GPU_USAGE                 (Unit.PERCENTAGE,            0,  100,     0,  98),
    GPU_VRAM_USAGE            (Unit.PERCENTAGE,            0,  100,     0,  80),
    FPS                       (Unit.FRAMES_PER_SECOND,     0, 1000,    10, 165), //above monitor's refresh rate (165 Hz) indicates GPU is doing unnecessary work
    INTERNET_DOWNLOAD_USAGE   (Unit.MEGABYTES_PER_SECOND,  0,  600,     0, 300),
    INTERNET_UPLOAD_USAGE     (Unit.MEGABYTES_PER_SECOND,  0,   30,     0,  15),
    AIR_TEMPERATURE           (Unit.DEGREES_FAHRENHEIT,   60,  110,    65,  95), //inside the case
    WATER_TEMPERATURE         (Unit.DEGREES_FAHRENHEIT,   60,  105,    65,  95), //i.e. coolant temperature
    SYSTEM_POWER_USAGE        (Unit.WATTS,                 0,  800,   100, 650), //total system power usage measured at the outlet
    SECONDARY_POWER_USAGE     (Unit.WATTS,                 0,  400,    50, 300), //system power usage excluding the CPU and GPU
    SYSTEM_COST_PER_HOUR      (Unit.DOLLARS,               0,    2, 0.005,   1); //based on total system power usage

    //member variables
    protected static final Sensor[] VALUES = Sensor.values(); //saved to avoid expensive copying
    private final double min; //the smallest value this Sensor should ever reach
    private final double max; //the largest value this Sensor should ever reach
    private final double warningMin; //the value beneath which warnings should be emitted
    private final double warningMax; //the value above which warnings should be emitted
    private final Unit unit; //the Unit for this Sensor
    private volatile double data; //the raw data for this Sensor
    private Figure figure; //the Figure this Sensor is displayed on

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
            logWarning("Value larger than warning max: " + this);
        }
        else if (this.data < this.warningMin)
        {
            logWarning("Value smaller than warning min: " + this);
        }

        //warn about values outside the max and min
        if (this.data > this.max)
        {
            logError("Value larger than Sensor maximum: " + this);
        }
        else if (this.data < this.min)
        {
            logError("Value smaller than Sensor minimum: " + this);
        }

        //repaint the figure
        this.figure.repaint();
    }

    /**
     * Returns the latest data for this Sensor.
     *
     * @return The latest data for this Sensor
     */
    public double getData()
    {
        return this.data;
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
        if (this.unit.prepend())
        {
            return this.name() + " -> " + this.unit + getData();
        }
        else
        {
            return this.name() + " -> " + getData() + " " + this.unit;
        }
    }
}
