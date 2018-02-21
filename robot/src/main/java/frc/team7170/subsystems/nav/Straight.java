package frc.team7170.subsystems.nav;

import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;
import frc.team7170.util.CalcUtil;


public class Straight extends Maneuver {

    private final double distance;
    private final Acceleration accel;

    public Straight(double distance, double max_out, double transition_in, double transition_out, double stop_accel,
                    double start_decel, boolean lin_accel, boolean lin_decel) {
        this.distance = distance;
        accel = new Acceleration(max_out, transition_in, transition_out, stop_accel, start_decel, lin_accel, lin_decel, distance<0);
    }

    @Override
    void run() {
        running = true;
        Navigation.reset_encoders();
        Drive.set_arcade(accel.get(0), Drive.get_Z(), false, false);
    }

    @Override
    void update() {
        Drive.set_arcade(accel.get((Math.abs(Navigation.get_Lenc_dist()) + Math.abs(Navigation.get_Renc_dist()))/2/distance),
                Drive.get_Z(), false, false);
    }

    @Override
    boolean finished() {
        return CalcUtil.in_threshold((Math.abs(Navigation.get_Lenc_dist()) + Math.abs(Navigation.get_Renc_dist()))/2,
                distance, RobotMap.Maneuvers.straight_distance_tolerance);
    }

    @Override
    public String toString() {
        return "Straight("+distance+")";
    }
}
