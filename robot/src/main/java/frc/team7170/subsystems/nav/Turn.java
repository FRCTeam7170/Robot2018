package frc.team7170.subsystems.nav;


import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;
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
        Drive.set_arcade(0, speed, false);
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
        return CalcUtil.in_threshold(Navigation.left_enc.get() - init_l_encoder,
                degrees * RobotMap.RobotDims.wheel_to_centre / RobotMap.RobotDims.wheel_radius,
                RobotMap.Maneuvers.turn_angle_tolerance);
    }
}
