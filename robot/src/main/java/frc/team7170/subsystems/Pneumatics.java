package frc.team7170.subsystems;

import java.util.logging.Logger;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.RpcAnswer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Solenoid;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.RPCCaller;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;


/**
 * This class handles all pneumatics on the robot, but does not actually do any sort of logic to determine when the arm
 * should be extended, for example. Such code is all contained in {@link frc.team7170.subsystems.arm.Arm}.
 *
 * @see frc.team7170.subsystems.arm.Arm
 */
public class Pneumatics extends Module implements Communicator {

    private final static Logger LOGGER = Logger.getLogger(Pneumatics.class.getName());

    private static Pneumatics instance = new Pneumatics();  // Singleton
    public static Pneumatics get_instance() {
        return instance;
    }
    private Pneumatics() {
        LOGGER.info("Initializing pneumatics system.");

        Dispatcher.get_instance().register_module(this);
        register_comm();
    }

    private Compressor compressor = new Compressor(RobotMap.CAN.PCM);
    private Solenoid arm_left = new Solenoid(RobotMap.CAN.PCM, RobotMap.PCM.left_solenoid);
    private Solenoid arm_right = new Solenoid(RobotMap.CAN.PCM, RobotMap.PCM.right_solenoid);

    @Override
    protected void update() {}

    @Override
    protected void enabled() {
        LOGGER.info("Pneumatics enabled.");
    }

    @Override
    protected void disabled() {
        LOGGER.info("Pneumatics disabled.");
        // Retract the arms, but the compressor is allowed to continue functioning.
        arm_left.set(false);
        arm_right.set(false);
    }

    @Override
    public String toString() {
        return "Pneumatics System";
    }

    public boolean get_solenoids() {
        return arm_left.get();  // Doesn't matter which
    }

    /**
     * Sets both solenoids to the given value. Note that because of this level of abstraction it is impossible to turn
     * one solenoid off and the other on, which could be dangerous.
     * @param on Whether to turn the solenoids on or off.
     */
    public void set_solenoids(boolean on) {
        if (!get_enabled()) {
            return;
        }
        arm_left.set(on);
        arm_right.set(on);
    }

    public void toggle_solenoids() {
        set_solenoids(!get_solenoids());
    }


    // Compressor accessors

    public void compressor_start() {
        LOGGER.fine("Starting compressor.");
        compressor.start();
    }

    public void compressor_stop() {
        LOGGER.fine("Stopping compressor.");
        compressor.stop();
    }

    /**
     * @return True if the compressor is on.
     */
    public boolean get_compressor_state() {
        return compressor.getClosedLoopControl();
    }

    public double get_compressor_current() {
        return compressor.getCompressorCurrent();
    }

    public boolean get_pressure_low() {
        return compressor.getPressureSwitchValue();
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = {
            "O_CAN_ID_PCM_S",
            "O_PCM_SOLENOID_LEFT_S",
            "O_PCM_SOLENOID_RIGHT_S"
    })
    public void transmitter_static(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_CAN_ID_PCM_S":
                entry.setDouble(RobotMap.CAN.PCM);
                break;
            case "O_PCM_SOLENOID_LEFT_S":
                entry.setDouble(RobotMap.PCM.left_solenoid);
                break;
            case "O_PCM_SOLENOID_RIGHT_S":
                entry.setDouble(RobotMap.PCM.right_solenoid);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.SLOW, value = {
            "O_PNEUMATICS_ENABLED_S"
    })
    public void transmitter_slow(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_PNEUMATICS_ENABLED_S":
                entry.setBoolean(get_enabled());
                break;
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.FAST, value = {
            "O_PNEUMATICS_ARM_STATE_S",
            "O_PNEUMATICS_COMPRESSOR_STATE_S",
            "O_PNEUMATICS_COMPRESSOR_CURRENT_S",
            "O_PNEUMATICS_COMPRESSOR_LOW_PRESSURE_S"
    })
    public void transmitter_fast(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_PNEUMATICS_ARM_STATE_S":
                entry.setBoolean(get_solenoids());
                break;
            case "O_PNEUMATICS_COMPRESSOR_STATE_S":
                entry.setBoolean(get_compressor_state());
                break;
            case "O_PNEUMATICS_COMPRESSOR_CURRENT_S":
                entry.setDouble(get_compressor_current());
                break;
            case "O_PNEUMATICS_COMPRESSOR_LOW_PRESSURE_S":
                entry.setBoolean(get_pressure_low());
                break;
        }
    }

    @SuppressWarnings("unused")
    @RPCCaller("R_PNEUMATICS_ENABLE")
    public void rpccaller_enable(RpcAnswer rpc) {
        if (rpc.params.getBytes()[0] == 1) {
            LOGGER.info("Pneumatics enabled via RPC.");
            set_enabled(true);
            rpc.postResponse(new byte[] {1});  // Success
        } else if (rpc.params.getBytes()[0] == 0) {
            LOGGER.info("Pneumatics disabled via RPC.");
            set_enabled(false);
            rpc.postResponse(new byte[] {1});  // Success
        }
        rpc.postResponse(new byte[] {0});  // Failure
    }
}
