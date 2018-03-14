package frc.team7170.control;

import edu.wpi.first.wpilibj.GenericHID;


/**
 * Wrapper for a single button on a given {@link GenericHID}.
 */
public class HIDButtonAccessor {

    final int port;
    final GenericHID joy;
    boolean do_sim = false;
    boolean sim = false;

    /**
     * @param port The port number on {@link HIDButtonAccessor#joy} to poll for the button state.
     * @param joy The {@link GenericHID} to poll for the button state.
     */
    HIDButtonAccessor(int port, GenericHID joy) {
        this.port = port;
        this.joy = joy;
    }

    public boolean get() {
        if (do_sim) {
            return sim;
        }
        return joy.getRawButton(port);
    }

    /**
     * Return if the button has been pressed since the last check.
     */
    public boolean get_pressed() {
        if (do_sim) {
            return sim;
        }
        return joy.getRawButtonPressed(port);
    }

    /**
     * Return if the button has been released since the last check.
     */
    public boolean get_released() {
        if (do_sim) {
            return !sim;
        }
        return sim && joy.getRawButtonReleased(port);
    }

    public void simulate(boolean state) {
        do_sim = true;
        sim = state;
    }

    public void stop_simulate() {
        do_sim = false;
    }

    @Override
    public String toString() {
        return joy.getName()+"."+port;
    }
}
