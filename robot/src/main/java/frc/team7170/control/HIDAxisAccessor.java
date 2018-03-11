package frc.team7170.control;

import edu.wpi.first.wpilibj.GenericHID;


/**
 * Wrapper for a single axis on a given {@link GenericHID}.
 */
public class HIDAxisAccessor {

    final int port;
    final GenericHID joy;

    /**
     * @param port The port number on {@link HIDAxisAccessor#joy} to poll for the axis value.
     * @param joy The {@link GenericHID} to poll for the axis value.
     */
    HIDAxisAccessor(int port, GenericHID joy) {
        this.port = port;
        this.joy = joy;
    }

    public double get() {
        return joy.getRawAxis(port);
    }

    @Override
    public String toString() {
        return joy.getName()+"."+port;
    }
}
