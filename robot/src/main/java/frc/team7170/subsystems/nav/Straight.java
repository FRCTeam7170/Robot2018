package frc.team7170.subsystems.nav;

import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;
import frc.team7170.util.CalcUtil;


public class Straight extends Maneuver {

    private final double distance;
    private final Acceleration accel_L;
    private final Acceleration accel_R;

    public Straight(double distance, double max_out, double transition_in, double transition_out, double stop_accel,
                    double start_decel, boolean lin_accel, boolean lin_decel) {
        this.distance = distance;
        accel_L = new Acceleration(max_out, transition_in, transition_out, stop_accel, start_decel, lin_accel, lin_decel, distance<0);
        accel_R = new Acceleration(max_out, transition_in, transition_out, stop_accel, start_decel, lin_accel, lin_decel, distance<0);
    }

    @Override
    void run() {
        running = true;
        Navigation.reset_encoders();
        Drive.set_tank(accel_L.get(0), accel_R.get(0), false, false);
    }

    @Override
    void update() {
        double left_enc_dist = Math.abs(Navigation.get_Lenc_dist());
        double right_enc_dist = Math.abs(Navigation.get_Renc_dist());
        Drive.set_tank(accel_L.get(left_enc_dist/distance), accel_R.get(right_enc_dist/distance), false, false);
    }

    @Override
    boolean finished() {
        return CalcUtil.in_threshold(Navigation.get_Lenc_dist(), distance, RobotMap.Maneuvers.straight_distance_tolerance) &
                CalcUtil.in_threshold(Navigation.get_Renc_dist(), distance, RobotMap.Maneuvers.straight_distance_tolerance);
    }

    @Override
    public String toString() {
        return "Straight("+distance+")";
    }
}
