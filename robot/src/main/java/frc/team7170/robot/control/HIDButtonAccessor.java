package frc.team7170.robot.control;

import edu.wpi.first.wpilibj.GenericHID;


public class HIDButtonAccessor {

    final int port;
    final GenericHID joy;

    HIDButtonAccessor(int port, GenericHID joy) {
        this.port = port;
        this.joy = joy;
    }

    public boolean get() {
        return joy.getRawButton(port);
    }

    public boolean get_pressed() {
        return joy.getRawButtonPressed(port);
    }

    public boolean get_released() {
        return joy.getRawButtonReleased(port);
    }
}
