package frc.team7170.subsystems;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import frc.team7170.robot.RobotMap;
import frc.team7170.robot.control.KeyBindings;
import edu.wpi.first.wpilibj.DigitalOutput;


public class Arm {

    private final static Logger LOGGER = Logger.getLogger(Arm.class.getName());

    // TODO: TEMP
    private final static DigitalOutput dos = new DigitalOutput(9);

    private static Spark spark_left_endE;
    private static Spark spark_right_endE;
    private static Spark spark_left_arm;
    private static Spark spark_right_arm;

    private static AnalogPotentiometer pot;

    private static boolean extended = false;


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

    public static void update() {
        /*
        if (KeyBindings.action2button(KeyBindings.Action.ENDE_PUSH).get_pressed()) {
            push_endE();
        } else if (KeyBindings.action2button(KeyBindings.Action.ENDE_SUCK).get_pressed()) {
            suck_endE();
        } else if (KeyBindings.action2button(KeyBindings.Action.ENDE_OFF).get_pressed()) {
            kill_endE();
        }
        arm_analog(KeyBindings.action2axis(KeyBindings.Action.ARM_ANALOG).get());
        if (KeyBindings.action2pov(KeyBindings.Action.ARM_UP).get()) {
            arm_up();
        } else if (KeyBindings.action2pov(KeyBindings.Action.ARM_DOWN).get()) {
            arm_down();
        } else {
            arm_kill();
        }
        */
        /*
        if (KeyBindings.action2button(KeyBindings.Action.TRY_ARM_TOGGLE).get_pressed()) {
            Pneumatics.set_solenoids(!Pneumatics.get_solenoids());
        }
        */
        if (extended & !safe_zone()) {
            extended = false;
            dos.set(true);
            // Pneumatics.set_solenoids(false);
        } else if (!extended & !danger_zone()) {
            extended = true;
            dos.set(false);
            // Pneumatics.set_solenoids(true);
        }
    }

    public static boolean safe_zone() {
        double pot_read = pot.get();
        return (pot_read <= RobotMap.Arm.pot_value_kill_lower_inner |
                pot_read >= RobotMap.Arm.pot_value_kill_upper_inner);
    }

    public static boolean danger_zone() {
        double pot_read = pot.get();
        return (pot_read >= RobotMap.Arm.pot_value_kill_lower_outer &
                pot_read <= RobotMap.Arm.pot_value_kill_upper_outer);
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

    public static void arm_up() {
        spark_left_arm.set(RobotMap.Arm.arm_speed);
        spark_right_arm.set(RobotMap.Arm.arm_speed);
    }

    public static void arm_down() {
        spark_left_arm.set(-RobotMap.Arm.arm_speed);
        spark_right_arm.set(-RobotMap.Arm.arm_speed);
    }

    public static void arm_kill() {
        spark_left_arm.set(0);
        spark_right_arm.set(0);
    }

    public static void arm_analog(double speed) {
        spark_left_arm.set(speed);
        spark_right_arm.set(speed);
    }
}
