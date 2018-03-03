package frc.team7170.control;

import edu.wpi.first.wpilibj.GenericHID;


/**
 * Wrapper for a single button on a given {@link GenericHID}.
 */
public class HIDButtonAccessor {

    final int port;
    final GenericHID joy;

    /**
     * @param port The port number on {@link HIDButtonAccessor#joy} to poll for the button state.
     * @param joy The {@link GenericHID} to poll for the button state.
     */
    HIDButtonAccessor(int port, GenericHID joy) {
        this.port = port;
        this.joy = joy;
    }

    public boolean get() {
        return joy.getRawButton(port);
    }

    /**
     * Return if the button has been pressed since the last check.
     */
    public boolean get_pressed() {
        return joy.getRawButtonPressed(port);
    }

    /**
     * Return if the button has been released since the last check.
     */
    public boolean get_released() {
        return joy.getRawButtonReleased(port);
    }
}
