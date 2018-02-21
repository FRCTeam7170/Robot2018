package frc.team7170.subsystems.nav;

import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;
import frc.team7170.util.CalcUtil;


public class Turn extends Maneuver {

    private final double degrees;
    private final double predicted_final_enc;
    private final Acceleration accel;

    public Turn(double degrees, double max_out, double transition_in, double transition_out, double stop_accel,
                double start_decel, boolean lin_accel, boolean lin_decel) {
        this.degrees = degrees;
        this.predicted_final_enc = Math.abs(degrees) * RobotMap.RobotDims.wheel_to_centre / RobotMap.RobotDims.wheel_radius;
        accel = new Acceleration(max_out, transition_in, transition_out, stop_accel, start_decel, lin_accel, lin_decel, degrees<0);
    }

    @Override
    void run() {
        running = true;
        Navigation.reset_encoders();
        Drive.set_arcade(Drive.get_Y(), accel.get(0), false, false);
    }

    @Override
    void update() {
        Drive.set_arcade(Drive.get_Y(), accel.get((Math.abs(Navigation.get_Lenc()) + Math.abs(Navigation.get_Renc()))/2/predicted_final_enc),
                false, false);
    }

    @Override
    boolean finished() {
        return CalcUtil.in_threshold((Math.abs(Navigation.get_Lenc()) + Math.abs(Navigation.get_Renc()))/2,
                predicted_final_enc, RobotMap.Maneuvers.turn_angle_tolerance);
    }

    @Override
    public String toString() {
        return "Turn("+degrees+")";
    }
}
