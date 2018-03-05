package frc.team7170.subsystems.drive;

import frc.team7170.util.CalcUtil;


/**
 * Class to handle smooth acceleration during various autonomous maneuvers.
 */
public class Acceleration {

    private boolean lin_accel;
    private boolean lin_decel;
    private boolean max_reached = false;
    private boolean reverse_out;
    private double max_out;
    private double transition_out;
    private double transition_in;
    private double stop_accel;
    private double start_decel;
    private double prev_out;
    private double prev_in = 0;
    private double max_reached_at_lin;

    /**
     * @param max_out The maximum motor output to reach before stopping acceleration. It is not guaranteed
     *                that this threshold will actually be reached--it depends on multiple other variables.
     * @param transition_in The motor output to start accelerating from. In most cases this should simply be the
     *                      transition_out of the previous maneuver, or zero. If you want to hasten the acceleration
     *                      period however, you can set this to a non-zero value (not too high though, that will
     *                      cause a current spike.)
     * @param transition_out The motor output to end the maneuver at.
     * @param stop_accel The point at which to stop the acceleration period expressed as a decimal percentage of the
     *                   overall maneuver (0 is the very start, 1 is the very end.)
     * @param start_decel The point at which to start the deceleration period expressed as a decimal percentage of
     *                    the overall maneuver (0 is the very start, 1 is the very end.) This threshold always overrides
     *                    the stop_accel threshold if the max_out hasn't been achieved yet.
     * @param lin_accel Whether to use a linear acceleration (quadratic motor output) model.
     * @param lin_decel Whether to use a linear deceleration (quadratic motor output) model.
     * @param reverse_out Whether to reverse the sign of the output values.
     */
    public Acceleration(double max_out, double transition_in, double transition_out, double stop_accel,
                        double start_decel, boolean lin_accel, boolean lin_decel, boolean reverse_out) {
        this.max_out = max_out;
        this.transition_in = transition_in;
        this.transition_out = transition_out;
        this.stop_accel = stop_accel;
        this.start_decel = start_decel;
        this.lin_accel = lin_accel;
        this.lin_decel = lin_decel;
        this.prev_out = transition_in;
        this.reverse_out = reverse_out;
    }

    /**
     * Should be called repeatedly with a ever-increasing progress value.
     * Most likely this progress value will be derived from encoder values.
     * @param prog The progress of the maneuver as a decimal percentage. Should be in [0, 1].
     * @return The motor output appropriate for the given progress.
     */
    public double get(double prog) {
        prog = CalcUtil.apply_bounds(prog, 0, 1);  // restrict to [0, 1]
        if (!max_reached && prog < start_decel) {  // Accelerate
            // Use chosen acceleration algorithm
            prev_out = lin_accel ? accel_lin(prog) : accel_const(prog);
            if (prev_out >= max_out) {  // If we've reached the desired maximum motor output
                max_reached = true;  // Stop accelerating
                // Make sure we don't "overshoot" the desired max_out (this can happen with a steep motor output curve).
                prev_out = max_out;
            }
        }
        if (prog >= start_decel) {  // Decelerate
            // Use chosen deceleration algorithm
            prev_out = lin_decel ? decel_lin(prog) : decel_const(prog);
        }
        prev_in = prog;  // Save the prog value for the purpose of the constant acceleration algorithms
        return reverse_out ? -prev_out : prev_out;  // Reverse the output if reverse_out was set to true
    }

    /**
     * Constant acceleration algorithm. This features an auto-correcting acceleration rate using the previous
     * progress value, unlike the linear acceleration algorithm.
     * This algorithm, like the other acceleration/deceleration algorithms, works by calculating the change in motor
     * output over the change in progress (the gradient of the motor output vs progress curve) and then using this
     * function to find the motor output appropriate to a given progress.
     */
    private double accel_const(double prog) {
        return (max_out - prev_out)/(stop_accel - prev_in) * prog + transition_in;
    }

    /**
     * Constant deceleration algorithm. This features an auto-correcting deceleration rate using the previous
     * progress value, unlike the linear deceleration algorithm.
     * This algorithm, like the other acceleration/deceleration algorithms, works by calculating the change in motor
     * output over the change in progress (the gradient of the motor output vs progress curve) and then using this
     * function to find the motor output appropriate to a given progress.
     */
    private double decel_const(double prog) {
        return (transition_out - prev_out)/(1 - prev_in) * (prog - 1) + transition_out;
    }

    /**
     * Linear acceleration algorithm.
     * This algorithm, like the other acceleration/deceleration algorithms, works by calculating the change in motor
     * output over the change in progress (the gradient of the motor output vs progress curve) and then using this
     * function to find the motor output appropriate to a given progress.
     */
    private double accel_lin(double prog) {
        return (max_out - transition_in)/(stop_accel*stop_accel) * prog*prog + transition_in;
    }

    /**
     * Linear deceleration algorithm.
     * This algorithm, like the other acceleration/deceleration algorithms, works by calculating the change in motor
     * output over the change in progress (the gradient of the motor output vs progress curve) and then using this
     * function to find the motor output appropriate to a given progress.
     */
    private double decel_lin(double prog) {
        // Because this algorithm does not auto-correct using the previous motor output we must store the motor output
        // at which we started decelerating at to properly calculate the gradient.
        if (max_reached_at_lin != 0) {
            max_reached_at_lin = prev_out;
        }
        return (max_reached_at_lin - transition_out)/((start_decel - 1)*(start_decel - 1)) * (prog - 1)*(prog - 1) + transition_out;
    }
}
