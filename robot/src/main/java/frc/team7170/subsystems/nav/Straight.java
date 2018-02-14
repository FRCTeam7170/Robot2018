package frc.team7170.subsystems.nav;

import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;
import frc.team7170.util.CalcUtil;


public class Straight extends Maneuver {

    private final double speed;
    private final double distance;

    public Straight(double speed, double distance) {
        this.speed = speed;
        this.distance = distance;
    }

    @Override
    void run() {
        running = true;
        Navigation.reset_encoders();
        Drive.set_tank(speed, speed, false);
    }

    @Override
    void correct() {
        double left_enc_dist = Math.abs(Navigation.left_enc.getDistance());
        double right_enc_dist = Math.abs(Navigation.right_enc.getDistance());
        // Correct for deviations in direction
        if (!CalcUtil.in_threshold(left_enc_dist - right_enc_dist, 0, RobotMap.Maneuvers.encoder_desync_tolerance_dist)) {
            Drive.set_tank(Drive.rob_L * right_enc_dist / left_enc_dist,
                    Drive.rob_R * left_enc_dist / right_enc_dist, false);
        }
        // Need to control acceleration... need algorithm to take accelerometer and encoder values and provide motor
        //     inputs such that we get a smooth velocity curve all while maximizing time spent at maximum speed and
        //     thus minimizing acceleration time (within reason: not TOO jerky)
    }

    @Override
    boolean finished() {
        return CalcUtil.in_threshold(Navigation.left_enc.getDistance(), distance, RobotMap.Maneuvers.straight_distance_tolerance);
    }
}
