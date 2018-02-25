package frc.team7170.robot.control;

import edu.wpi.first.wpilibj.GenericHID;


public class HIDAxisAccessor {

    final int port;
    final GenericHID joy;

    HIDAxisAccessor(int port, GenericHID joy) {
        this.port = port;
        this.joy = joy;
    }

    public double get() {
        return joy.getRawAxis(port);
    }
}
