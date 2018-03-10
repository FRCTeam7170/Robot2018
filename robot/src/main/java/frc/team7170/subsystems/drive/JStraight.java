package frc.team7170.subsystems.drive;

import frc.team7170.jobs.Job;
import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;


/**
 * Job for driving in a straight line for a certain distance (without correction, so it may not actually be straight.)
 */
public class JStraight extends Job {

    private final double distance;
    private final Acceleration accel;
    private final Drive drive;

    public JStraight(double distance, double max_out, double transition_in, double transition_out, double stop_accel,
                     double start_decel, boolean lin_accel, boolean lin_decel) {
        drive = Drive.get_instance();
        requires(drive);
        this.distance = distance;
        accel = new Acceleration(max_out, transition_in, transition_out, stop_accel, start_decel, lin_accel, lin_decel, distance<0);
    }

    @Override
    protected void init() {
        drive.reset_encoders();
        // Only set forward (Y) speed, keep turn (Z) speed at what it is already at.
        drive.set_arcade(accel.get(0), drive.get_Z(), false, false);
    }

    @Override
    protected void update() {
        // Only set forward (Y) speed, keep turn (Z) speed at what it is already at.
        // Progress of maneuver is calculated by average encoder distance over projected distance.
        drive.set_arcade(accel.get((Math.abs(drive.get_Lenc_dist()) + Math.abs(drive.get_Renc_dist()))/2/distance),
                drive.get_Z(), false, false);
    }

    @Override
    protected boolean is_finished() {
        return CalcUtil.in_threshold((Math.abs(drive.get_Lenc_dist()) + Math.abs(drive.get_Renc_dist()))/2,
                distance, RobotMap.Maneuvers.straight_distance_tolerance);
    }

    @Override
    protected void end() {
        // End by setting forward (Y) speed to whatever 100% of acceleration algorithm progress is
        drive.set_arcade(accel.get(1), drive.get_Y(), false, false);
    }

    @Override
    protected void interrupted() {
        end();
    }

    @Override
    public String toString() {
        return "JStraight("+distance+")";
    }
}
