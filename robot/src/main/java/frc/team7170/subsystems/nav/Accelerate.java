package frc.team7170.subsystems.nav;


public class Accelerate {

    private boolean max_reached = false;
    private double max_out;
    private double transition_out;
    private double transition_in;
    private double stop_accel;
    private double start_decel;
    private double prev_out;
    private double prev_in;

    public Accelerate(double max_out, double transition_in, double transition_out, double stop_accel, double start_decel) {
        this.max_out = max_out;
        this.transition_in = transition_in;
        this.transition_out = transition_out;
        this.stop_accel = stop_accel;
        this.start_decel = start_decel;
    }

    double update(double prog) {
        if (!max_reached & prog < start_decel) {  // Accelerate
            prev_out = (max_out - prev_out)/(stop_accel - prev_in) * prog + transition_in;
            if (prev_out >= max_out) {
                max_reached = true;  // Stop accelerating
                prev_out = max_out;
            }
        }
        if (prog >= start_decel) {  // Decelerate
            prev_out = (transition_out - prev_out)/(1 - prev_in) * (prog - 1) + transition_out;
        }
        prev_in = prog;
        return prev_out;
    }
}
