package frc.team7170.subsystems;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Solenoid;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;


/**
 * This class handles all pneumatics on the robot, but does not actually do any sort of logic to determine when the arm
 * should be extended, for example. Such code is all contained in {@link frc.team7170.subsystems.arm.Arm}.
 *
 * @see frc.team7170.subsystems.arm.Arm
 */
public class Pneumatics extends Module {

    private final static Logger LOGGER = Logger.getLogger(Pneumatics.class.getName());

    private static Pneumatics instance = new Pneumatics();  // Singleton
    public static Pneumatics get_instance() {
        return instance;
    }
    private Pneumatics() {
        LOGGER.info("Initializing pneumatics system.");

        Dispatcher.get_instance().register_module(this);
    }

    private static Compressor compressor = new Compressor(RobotMap.CAN.PCM);
    private static Solenoid arm_left = new Solenoid(RobotMap.CAN.PCM, RobotMap.PCM.left_solenoid);
    private static Solenoid arm_right = new Solenoid(RobotMap.CAN.PCM, RobotMap.PCM.right_solenoid);

    @Override
    protected void update() {}

    @Override
    protected void enabled() {}

    @Override
    protected void disabled() {}

    @Override
    public String toString() {
        return "Pneumatics module.";
    }

    public static boolean get_solenoids() {
        return arm_left.get();  // Doesn't matter which
    }

    /**
     * Sets both solenoids to the given value. Note that because of this level of abstraction it is impossible to turn
     * one solenoid off and the other on, which could be dangerous.
     * @param on Whether to turn the solenoids on or off.
     */
    public static void set_solenoids(boolean on) {
        arm_left.set(on);
        arm_right.set(on);
    }

    public static void toggle_solenoids() {
        set_solenoids(!get_solenoids());
    }


    // Compressor accessors

    public static void compressor_start() {
        LOGGER.fine("Starting compressor.");
        compressor.start();
    }

    public static void compressor_stop() {
        LOGGER.fine("Stopping compressor.");
        compressor.stop();
    }

    public static void compressor_auto() {
        LOGGER.fine("Setting compressor to auto.");
        compressor.setClosedLoopControl(true);
    }

    public static double get_compressor_current() {
        return compressor.getCompressorCurrent();
    }

    public static boolean get_pressure_low() {
        return compressor.getPressureSwitchValue();
    }
}
