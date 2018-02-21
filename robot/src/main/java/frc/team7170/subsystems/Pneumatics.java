package frc.team7170.subsystems;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Solenoid;
import frc.team7170.robot.RobotMap;


public class Pneumatics {

    private final static Logger LOGGER = Logger.getLogger(Pneumatics.class.getName());

    private static Compressor compressor;
    private static Solenoid arm_left;
    private static Solenoid arm_right;

    public static void init() {
        LOGGER.info("Initializing pneumatics system.");
        compressor = new Compressor(RobotMap.CAN.PCM);
        arm_left = new Solenoid(RobotMap.CAN.PCM, RobotMap.PCM.left_solenoid);
        arm_right = new Solenoid(RobotMap.CAN.PCM, RobotMap.PCM.right_solenoid);
    }

    public static boolean get_solenoids() {
        return arm_left.get();  // Doesn't matter which
    }

    public static void set_solenoids(boolean on) {
        arm_left.set(on);
        arm_right.set(on);
    }

    public static void toggle_solenoids() {
        arm_left.set(!arm_left.get());
        arm_right.set(!arm_right.get());
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
