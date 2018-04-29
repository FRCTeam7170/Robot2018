package frc.team7170.subsystems.drive;

import frc.team7170.jobs.Job;
import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;


/**
 * Job for turning on the spot for a certain amount of degrees.
 */
public class JTurn extends Job {

    private final double degrees;
    // private final double predicted_final_enc;
    private final Acceleration accel;
    private final Drive drive;

    public JTurn(double degrees, double max_out, double transition_in, double transition_out, double stop_accel,
                 double start_decel, boolean lin_accel, boolean lin_decel) {
        drive = Drive.get_instance();
        requires(drive);
        this.degrees = degrees;
        // this.predicted_final_enc = Math.abs(degrees) * RobotMap.RobotDims.wheel_to_centre / RobotMap.RobotDims.wheel_radius;
        accel = new Acceleration(max_out, transition_in, transition_out, stop_accel, start_decel, lin_accel, lin_decel, degrees<0);
    }

    @Override
    protected void init() {
        // drive.reset_encoders();
        // Only set turn (Z) speed, keep forward (Y) speed at what it is already at.
        drive.reset_gyro();
        drive.set_arcade(drive.get_Y(), accel.get(0), false, false);
    }

    @Override
    protected void update() {
        // Only set turn (Z) speed, keep forward (Y) speed at what it is already at.
        // Progress of maneuver is calculated by average encoder distance over projected distance.
        // drive.set_arcade(drive.get_Y(), accel.get((Math.abs(drive.get_Lenc()) + Math.abs(drive.get_Renc()))/2/predicted_final_enc),
        //         false, false);
        // TODO: check how gyro output works
    }

    @Override
    protected boolean is_finished() {
        //return CalcUtil.in_threshold((Math.abs(drive.get_Lenc()) + Math.abs(drive.get_Renc()))/2,
        //        predicted_final_enc, RobotMap.Maneuvers.turn_angle_tolerance);
        //return (Math.abs(drive.get_Lenc()) + Math.abs(drive.get_Renc()))/2/predicted_final_enc >= 1;
        return drive.get_gyro()/degrees >= 1;
    }

    @Override
    protected void end() {
        // End by setting turn (Z) speed to whatever 100% of acceleration algorithm progress is
        drive.set_arcade(drive.get_Y(), accel.get(1), false, false);
    }

    @Override
    protected void interrupted() {
        end();
    }

    @Override
    public String toString() {
        return "JTurn("+degrees+")";
    }
}
