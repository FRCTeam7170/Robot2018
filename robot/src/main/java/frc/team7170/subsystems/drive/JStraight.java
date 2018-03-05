package frc.team7170.subsystems.drive;

import frc.team7170.jobs.Job;
import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;


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
        drive.set_arcade(accel.get(0), drive.get_Z(), false, false);
    }

    @Override
    protected void update() {
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
