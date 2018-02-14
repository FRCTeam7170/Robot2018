package frc.team7170.subsystems.nav;

import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;

public class Straight extends Maneuver {

    private final double speed;
    private final double distance;
    private long predicted_d_enc;

    public Straight(double speed, double distance) {
        this.speed = speed;
        this.distance = distance;
    }

    @Override
    void run() {
        running = true;
        Navigation.reset();
        predicted_d_enc = (long) (180.0*distance/Math.PI/RobotMap.RobotDims.wheel_radius);
        Drive.set_tank(speed, speed, false);
    }

    @Override
    void update() {
        correct();
        if (finished()) {
            Navigation.man_complete();
        }
    }

    @Override
    void correct() {
    }

    @Override
    boolean finished() {
        return false;
    }
}
