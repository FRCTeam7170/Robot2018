package frc.team7170.robot.control;

import edu.wpi.first.wpilibj.GenericHID;


public class HIDPOVAccessor extends HIDButtonAccessor {

    private boolean pressed;

    HIDPOVAccessor(int degrees, GenericHID joy) {
        super(degrees, joy);
    }

    public boolean get() {
        if (joy.getPOV() == port) {
            pressed = true;
        } else {
            pressed = false;
        }
        return pressed;
    }

    public boolean get_pressed() {
        return !pressed & get();
    }

    public boolean get_released() {
        return pressed & !get();
    }
}
