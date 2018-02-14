package frc.team7170.subsystems.nav;

import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;
import frc.team7170.util.CalcUtil;


public class Turn extends Maneuver {

    private final double degrees;
    private final double speed;

    public Turn(double degrees, double speed) {
        this.degrees = degrees;
        this.speed = speed;
    }

    @Override
    void run() {
        running = true;
        Navigation.reset_encoders();
        Drive.set_arcade(0, speed, false);
    }

    @Override
    void correct() {
        /* If A and B are set motor speeds, and a and b are the encoder inputs (the true motor speeds),
         * then we can correct A, B to approach perfect rotation as follows:
         *     A_new = A_old * b/a
         *     B_new = B_old * a/b
         */
        int left_enc = Math.abs(Navigation.left_enc.get());
        int right_enc = Math.abs(Navigation.right_enc.get());
        if (!CalcUtil.in_threshold(left_enc - right_enc, 0, RobotMap.Maneuvers.encoder_desync_tolerance)) {
            Drive.set_tank(Drive.rob_L * right_enc / left_enc, Drive.rob_R * left_enc / right_enc, false);
        }
    }

    @Override
    boolean finished() {
        return CalcUtil.in_threshold((Math.abs(Navigation.left_enc.get())+Math.abs(Navigation.right_enc.get()))/2,  // Get average between both encoders
                degrees * RobotMap.RobotDims.wheel_to_centre / RobotMap.RobotDims.wheel_radius, RobotMap.Maneuvers.turn_angle_tolerance);
    }
}
