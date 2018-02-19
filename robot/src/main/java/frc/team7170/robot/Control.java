package frc.team7170.robot;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.Joystick;


public class Control {

    private final static Logger LOGGER = Logger.getLogger(Control.class.getName());

    private static Joystick joystick;
    private static Joystick gamepad;
    private static boolean use_gamepad;

    public static void init() {
        LOGGER.info("Initializing control system.");
        joystick = new Joystick(RobotMap.Controllers.joystick);
        gamepad = new Joystick(RobotMap.Controllers.gamepad);
        use_gamepad = false;
    }

    public static void toggle_controllers() {
        LOGGER.info("Swapped controllers. "+(use_gamepad ? "Joystick" : "Gamepad")+" is now primary controller.");
        use_gamepad = !use_gamepad;
    }

    public static boolean buttton(int btn) {
        return use_gamepad ? gamepad.getRawButton(btn) : joystick.getRawButton(btn);
    }

    public static boolean buttton_pressed(int btn) {
        return use_gamepad ? gamepad.getRawButtonPressed(btn) : joystick.getRawButtonPressed(btn);
    }

    public static boolean buttton_released(int btn) {
        return use_gamepad ? gamepad.getRawButtonReleased(btn) : joystick.getRawButtonReleased(btn);
    }

    public static boolean trigger() {
        return use_gamepad ? gamepad.getTrigger() : joystick.getTrigger();
    }

    public static boolean trigger_pressed() {
        return use_gamepad ? gamepad.getTriggerPressed() : joystick.getTriggerPressed();
    }

    public static boolean trigger_released() {
        return use_gamepad ? gamepad.getTriggerReleased() : joystick.getTriggerReleased();
    }

    public static double X() {
        return use_gamepad ? gamepad.getX() : joystick.getX();
    }

    public static double Y() {
        return use_gamepad ? gamepad.getY() : joystick.getY();
    }

    public static double Z() {
        return use_gamepad ? gamepad.getZ() : joystick.getZ();
    }

    public static double throttle() {
        return use_gamepad ? gamepad.getThrottle() : joystick.getThrottle();
    }

    public static int POV() {
        return use_gamepad ? gamepad.getPOV() : joystick.getPOV();
    }
}
