package frc.team7170.subsystems.nav;


import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;

public class Turn extends Maneuver {

    private final double degrees;
    private final double speed;
    private long init_l_encoder;
    private long init_r_encoder;

    public Turn(double degrees, double speed) {
        this.degrees = degrees;
        this.speed = speed;
    }

    @Override
    void run() {
        init_l_encoder = Navigation.left_enc.get();
        init_r_encoder = Navigation.right_enc.get();
        set_arcade(0, speed);
    }

    @Override
    void update() {
        correct();
        finished();
    }

    @Override
    void correct() {

    }

    @Override
    void finished() {
        return CalcUtil.in_threshold(degrees * RobotMap.RobotDims.wheel_to_centre / RobotMap.RobotDims.wheel_radius, degrees, RobotMap.Maneuvers.turn_angle_tolerance);
    }
}
