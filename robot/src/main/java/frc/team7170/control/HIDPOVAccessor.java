package frc.team7170.control;

import edu.wpi.first.wpilibj.GenericHID;


/**
 * Wrapper for a single POV direction on a given {@link GenericHID}.
 * Works off of same interface as {@link HIDButtonAccessor}, which essentially allows for a POV direction to function
 * as a button press.
 */
public class HIDPOVAccessor extends HIDButtonAccessor {

    private boolean pressed;

    /**
     * @param degrees The POV direction to poll.
     * @param joy The {@link GenericHID} to poll for the button state.
     */
    HIDPOVAccessor(int degrees, GenericHID joy) {
        super(degrees, joy);
    }

    @Override
    public boolean get() {
        if (joy.getPOV() == port) {
            pressed = true;
        } else {
            pressed = false;
        }
        return pressed;
    }

    @Override
    public boolean get_pressed() {
        return !pressed && get();
    }

    @Override
    public boolean get_released() {
        return pressed && !get();
    }
}
