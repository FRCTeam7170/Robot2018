package frc.team7170.control;

import edu.wpi.first.wpilibj.GenericHID;

import java.util.logging.Logger;


/**
 * Wrapper for a single POV direction on a given {@link GenericHID}.
 * Works off of same interface as {@link HIDButtonAccessor}, which essentially allows for a POV direction to function
 * as a button press.
 */
public class HIDPOVAccessor extends HIDButtonAccessor {

    private final static Logger LOGGER = Logger.getLogger(HIDPOVAccessor.class.getName());

    private static boolean warned = false;

    /**
     * @param degrees The POV direction to poll.
     * @param joy The {@link GenericHID} to poll for the button state.
     */
    HIDPOVAccessor(int degrees, GenericHID joy) {
        super(degrees, joy);
    }

    @Override
    public boolean get() {
        return joy.getPOV() == port;
    }

    @Override
    public boolean get_pressed() {
        if (!warned) {
            warned = true;
            LOGGER.warning("get_pressed() and get_released() functionality not implemented for POV buttons. Relaying call to get().");
        }
        return get();
    }

    @Override
    public boolean get_released() {
        if (!warned) {
            warned = true;
            LOGGER.warning("get_pressed() and get_released() functionality not implemented for POV buttons. Relaying call to get().");
        }
        return !get();
    }

    @Override
    public String toString() {
        return joy.getName()+".POV"+port;
    }
}