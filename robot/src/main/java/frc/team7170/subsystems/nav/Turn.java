package frc.team7170.subsystems.nav;


import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;

public class Turn extends Maneuver {

    private final double degrees;
    private final double speed;
    private long init_l_encoder;
    // private long init_r_encoder;

    public Turn(double degrees, double speed) {
        this.degrees = degrees;
        this.speed = speed;
    }

    @Override
    void run() {
        running = true;
        // If we're not doing any form of correction (see correct() for why),
        // we should only need to consider the change of one encoder
        init_l_encoder = Navigation.left_enc.get();
        // init_r_encoder = Navigation.right_enc.get();
        set_arcade(0, speed);
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
        /* The deviation here should be negligible considering we're using arcadeDrive, so no correction needed.
         * Should I be wrong about that however...
         * Let O be the center of the robot--the desired point to turn about.
         * Let A, B be the points along the long edges of the bot, directly above the centre wheels
         *     such that A, B, and O are collinear.
         * Then let a, b be the speeds at which A, B revolve around the points B, A, respectively.
         *     (Recognize that if a = 0 the bot revolves around B and vice versa.)
         * Note when a = b we achieve perfect rotation (the centre of rotation coincides with O.)
         * Let rA, rB be the distance from the centre of rotation to A, B, respectively.
         * Note when we achieve perfect rotation rA = rB = OA = OB because the centre of rotation coincides with O.
         * From this we can derive:
         *     rA = AB(1 + a - b)/2
         *     rB = AB(1 + b - a)/2
         * Which can then be used in conjunction with tankDrive to correct rotation.
         */
    }

    @Override
    boolean finished() {
        return CalcUtil.in_threshold(Navigation.left_enc.get() - init_l_encoder,
                degrees * RobotMap.RobotDims.wheel_to_centre / RobotMap.RobotDims.wheel_radius,
                RobotMap.Maneuvers.turn_angle_tolerance);
    }
}
