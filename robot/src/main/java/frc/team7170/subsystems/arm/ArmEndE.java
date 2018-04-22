package frc.team7170.subsystems.arm;

import edu.wpi.first.wpilibj.Spark;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;

import java.util.logging.Logger;

public class ArmEndE extends Module {

    private final static Logger LOGGER = Logger.getLogger(ArmEndE.class.getName());

    private static ArmEndE instance = new ArmEndE();  // Singleton
    public static ArmEndE get_instance() {
        return instance;
    }
    private ArmEndE() {
        LOGGER.info("Initializing arm end effector system.");

        spark_left_endE.setInverted(RobotMap.Arm.reverse_endE_left);
        spark_right_endE.setInverted(RobotMap.Arm.reverse_endE_right);

        Dispatcher.get_instance().register_module(this);
    }

    private Spark spark_left_endE = new Spark(RobotMap.PWM.endE_left_motor);
    private Spark spark_right_endE = new Spark(RobotMap.PWM.endE_right_motor);

    @Override
    protected void enabled() {
        LOGGER.info("Arm end effector enabled.");
    }

    @Override
    protected void disabled() {
        LOGGER.info("Arm end effector disabled.");
        endE_kill();
    }

    @Override
    public String toString() {
        return "Arm End Effector System";
    }

    public void endE_suck() {
        endE_analog(RobotMap.Arm.endE_speed);
    }

    public void endE_push() {
        endE_analog(-RobotMap.Arm.endE_speed);
    }

    public void endE_kill() {
        endE_analog(0);
    }

    public void endE_analog(double speed) {
        if (!get_enabled()) {
            return;
        }
        spark_left_endE.set(speed);
        spark_right_endE.set(speed);
    }

    public double get_endE_speed() {
        return spark_left_endE.get();  // Doesn't matter which
    }

}
