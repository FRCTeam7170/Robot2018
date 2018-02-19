package frc.team7170.subsystems.nav;

import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;
import frc.team7170.util.CalcUtil;


public class Turn extends Maneuver {

    private final double degrees;
    private final double predicted_final_enc;
    private final Acceleration accel_L;
    private final Acceleration accel_R;

    public Turn(double degrees, double max_out, double transition_in, double transition_out, double stop_accel,
                double start_decel, boolean lin_accel, boolean lin_decel) {
        this.degrees = degrees;
        this.predicted_final_enc = Math.abs(degrees) * RobotMap.RobotDims.wheel_to_centre / RobotMap.RobotDims.wheel_radius;
        accel_L = new Acceleration(max_out, transition_in, transition_out, stop_accel, start_decel, lin_accel, lin_decel, degrees<0);
        accel_R = new Acceleration(max_out, transition_in, transition_out, stop_accel, start_decel, lin_accel, lin_decel, degrees>0);
    }

    @Override
    void run() {
        running = true;
        Navigation.reset_encoders();
        Drive.set_tank(accel_L.get(0), accel_R.get(0), false, false);
    }

    @Override
    void update() {
        int left_enc = Math.abs(Navigation.get_Lenc());
        int right_enc = Math.abs(Navigation.get_Renc());
        Drive.set_tank(accel_L.get(left_enc/predicted_final_enc), accel_R.get(right_enc/predicted_final_enc),
                false, false);
    }

    @Override
    boolean finished() {
        return CalcUtil.in_threshold(Math.abs(Navigation.get_Lenc()), predicted_final_enc, RobotMap.Maneuvers.turn_angle_tolerance) &
                CalcUtil.in_threshold(Math.abs(Navigation.get_Renc()), predicted_final_enc, RobotMap.Maneuvers.turn_angle_tolerance);
    }

    @Override
    public String toString() {
        return "Turn("+degrees+")";
    }
}
