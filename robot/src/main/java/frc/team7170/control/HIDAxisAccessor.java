package frc.team7170.control;

import edu.wpi.first.wpilibj.GenericHID;


/**
 * Wrapper for a single axis on a given {@link GenericHID}.
 */
public class HIDAxisAccessor {

    private final int port;
    private final GenericHID joy;
    private double scale = 1.0;
    private double offset = 0.0;

    /**
     * @param port The port number on {@link HIDAxisAccessor#joy} to poll for the axis value.
     * @param joy The {@link GenericHID} to poll for the axis value.
     */
    HIDAxisAccessor(int port, GenericHID joy) {
        this(port, joy, 1.0, 0.0);
    }

    HIDAxisAccessor(int port, GenericHID joy, double scale, double offset) {
        this.port = port;
        this.joy = joy;
        this.scale = scale;
        this.offset = offset;
    }

    public double get() {
        return joy.getRawAxis(port) * scale + offset;
    }

    public void set_scale(double scale) {
        this.scale = scale;
    }

    public void set_offset(double offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return joy.getName()+"."+port;
    }
}
