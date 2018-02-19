package frc.team7170.subsystems;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import frc.team7170.robot.RobotMap;


public class Arm {

    private final static Logger LOGGER = Logger.getLogger(Arm.class.getName());

    private static Spark spark_left_endE;
    private static Spark spark_right_endE;
    private static Spark spark_left_arm;
    private static Spark spark_right_arm;

    private static AnalogPotentiometer pot;


    public static void init() {
        LOGGER.info("Initializing arm system.");

        spark_left_endE = new Spark(RobotMap.PWM.endE_left_motor);
        spark_right_endE = new Spark(RobotMap.PWM.endE_right_motor);
        spark_left_arm = new Spark(RobotMap.PWM.arm_left_motor);
        spark_right_arm = new Spark(RobotMap.PWM.arm_right_motor);

        spark_left_endE.setInverted(RobotMap.Arm.reverse_endE_left);
        spark_right_endE.setInverted(RobotMap.Arm.reverse_endE_right);

        pot = new AnalogPotentiometer(RobotMap.AIO.arm_pot, RobotMap.Arm.pot_scale, RobotMap.Arm.pot_offset);
    }

    public static void suck_endE() {
        spark_left_endE.set(RobotMap.Arm.endE_speed);
        spark_right_endE.set(RobotMap.Arm.endE_speed);
    }

    public static void push_endE() {
        spark_left_endE.set(-RobotMap.Arm.endE_speed);
        spark_right_endE.set(-RobotMap.Arm.endE_speed);
    }

    public static void kill_endE() {
        spark_left_endE.set(0);
        spark_right_endE.set(0);
    }

    
}
