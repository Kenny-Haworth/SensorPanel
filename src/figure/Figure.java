package src.figure;

import javax.swing.JPanel;

import src.Sensor;

/**
 * An abstract class for all Figures to extend.
 *
 * This class ensures common functionality for all Figures.
 */
public abstract sealed class Figure extends JPanel permits SleekGauge, Thermostat, IconField, SleekBar
{
    /**
     * Links this Figure with a Sensor.
     * This ensures that when the Sensor is updated, this Figure will be automatically repainted.
     *
     * @param sensor The Sensor linked to this Figure
     */
    protected Figure(Sensor sensor)
    {
        super();
        sensor.setFigure(this);
    }
}
