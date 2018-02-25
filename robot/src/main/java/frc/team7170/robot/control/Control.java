package frc.team7170.robot.control;

import java.util.logging.Logger;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import frc.team7170.robot.RobotMap;


public class Control {

    private final static Logger LOGGER = Logger.getLogger(Control.class.getName());

    // Technically I don't think these have to be a Joystick because we're simply accessing raw values anyway: could prob just be GenericHID
    private static Joystick _joystick = new Joystick(RobotMap.Controllers.joystick);
    private static Joystick _gamepad = new Joystick(RobotMap.Controllers.gamepad);


    /* ----- Joystick Convenience Bindings ----- */

    public static class _JoystickButtons {
        public final HIDButtonAccessor B1      = new HIDButtonAccessor(1, _joystick);
        public final HIDButtonAccessor B2      = new HIDButtonAccessor(2, _joystick);
        public final HIDButtonAccessor B3      = new HIDButtonAccessor(3, _joystick);
        public final HIDButtonAccessor B4      = new HIDButtonAccessor(4, _joystick);
        public final HIDButtonAccessor B5      = new HIDButtonAccessor(5, _joystick);
        public final HIDButtonAccessor B6      = new HIDButtonAccessor(6, _joystick);
        public final HIDButtonAccessor B7      = new HIDButtonAccessor(7, _joystick);
        public final HIDButtonAccessor B8      = new HIDButtonAccessor(8, _joystick);
        public final HIDButtonAccessor B9      = new HIDButtonAccessor(9, _joystick);
        public final HIDButtonAccessor B10     = new HIDButtonAccessor(10, _joystick);
        public final HIDButtonAccessor B11     = new HIDButtonAccessor(11, _joystick);
        public final HIDButtonAccessor B12     = new HIDButtonAccessor(12, _joystick);
        public final HIDButtonAccessor TRIGGER = B1;
        public final HIDButtonAccessor THUMB   = B2;
    }

    public static class _JoystickAxes {
        public final HIDAxisAccessor X        = new HIDAxisAccessor(0, _joystick);
        public final HIDAxisAccessor Y        = new HIDAxisAccessor(1, _joystick);
        public final HIDAxisAccessor Z        = new HIDAxisAccessor(2, _joystick);
        public final HIDAxisAccessor TWIST    = Z;
        public final HIDAxisAccessor THROTTLE = new HIDAxisAccessor(3, _joystick);
    }

    public static class _JoystickPOV {
        public final HIDPOVAccessor TOP          = new HIDPOVAccessor(0, _joystick);
        public final HIDPOVAccessor TOP_RIGHT    = new HIDPOVAccessor(45, _joystick);
        public final HIDPOVAccessor RIGHT        = new HIDPOVAccessor(90, _joystick);
        public final HIDPOVAccessor BOTTOM_RIGHT = new HIDPOVAccessor(135, _joystick);
        public final HIDPOVAccessor BOTTOM       = new HIDPOVAccessor(180, _joystick);
        public final HIDPOVAccessor BOTTOM_LEFT  = new HIDPOVAccessor(225, _joystick);
        public final HIDPOVAccessor LEFT         = new HIDPOVAccessor(270, _joystick);
        public final HIDPOVAccessor TOP_LEFT     = new HIDPOVAccessor(315, _joystick);
        public final HIDPOVAccessor CENTRE       = new HIDPOVAccessor(-1, _joystick);

        public int get_degree() {
            return _joystick.getPOV();
        }
    }

    public static class BigJoystick {
        public final _JoystickButtons Buttons = new _JoystickButtons();  // Singleton
        public final _JoystickAxes Axes = new _JoystickAxes();  // Singleton
        public final _JoystickPOV POV = new _JoystickPOV();  // Singleton
    }
    public static final BigJoystick joystick = new BigJoystick();  // Singleton


    /* ----- Gamepad Convenience Bindings ----- */

    public static class _GamepadButtons {
        public final HIDButtonAccessor A      = new HIDButtonAccessor(1, _gamepad);
        public final HIDButtonAccessor B      = new HIDButtonAccessor(2, _gamepad);
        public final HIDButtonAccessor X      = new HIDButtonAccessor(3, _gamepad);
        public final HIDButtonAccessor Y      = new HIDButtonAccessor(4, _gamepad);
        public final HIDButtonAccessor LB     = new HIDButtonAccessor(5, _gamepad);
        public final HIDButtonAccessor RB     = new HIDButtonAccessor(6, _gamepad);
        public final HIDButtonAccessor BACK   = new HIDButtonAccessor(7, _gamepad);
        public final HIDButtonAccessor START  = new HIDButtonAccessor(8, _gamepad);
        public final HIDButtonAccessor LJOYIN = new HIDButtonAccessor(9, _gamepad);
        public final HIDButtonAccessor RJOYIN = new HIDButtonAccessor(10, _gamepad);
    }

    public static class _GamepadAxes {
        public final HIDAxisAccessor LX = new HIDAxisAccessor(0, _gamepad);
        public final HIDAxisAccessor LY = new HIDAxisAccessor(1, _gamepad);
        public final HIDAxisAccessor LT = new HIDAxisAccessor(2, _gamepad);
        public final HIDAxisAccessor RT = new HIDAxisAccessor(3, _gamepad);
        public final HIDAxisAccessor RX = new HIDAxisAccessor(4, _gamepad);
        public final HIDAxisAccessor RY = new HIDAxisAccessor(5, _gamepad);
    }

    public static class _GamepadPOV {
        public final HIDPOVAccessor TOP          = new HIDPOVAccessor(0, _gamepad);
        public final HIDPOVAccessor TOP_RIGHT    = new HIDPOVAccessor(45, _gamepad);
        public final HIDPOVAccessor RIGHT        = new HIDPOVAccessor(90, _gamepad);
        public final HIDPOVAccessor BOTTOM_RIGHT = new HIDPOVAccessor(135, _gamepad);
        public final HIDPOVAccessor BOTTOM       = new HIDPOVAccessor(180, _gamepad);
        public final HIDPOVAccessor BOTTOM_LEFT  = new HIDPOVAccessor(225, _gamepad);
        public final HIDPOVAccessor LEFT         = new HIDPOVAccessor(270, _gamepad);
        public final HIDPOVAccessor TOP_LEFT     = new HIDPOVAccessor(315, _gamepad);
        public final HIDPOVAccessor CENTRE       = new HIDPOVAccessor(-1, _gamepad);

        public int get_degree() {
            return _gamepad.getPOV();
        }
    }

    public static class Gamepad {
        public final _GamepadButtons Buttons = new _GamepadButtons();  // Singleton
        public final _GamepadAxes Axes = new _GamepadAxes();  // Singleton
        public final _GamepadPOV POV = new _GamepadPOV();  // Singleton
    }
    public static final Gamepad gamepad = new Gamepad();  // Singleton


    /* ----- Misc ----- */

    public static void init() {
        LOGGER.info("Initializing control system.");
        /*
        _joystick = new Joystick(RobotMap.Controllers.joystick);
        _gamepad = new Joystick(RobotMap.Controllers.gamepad);
        */
    }

    public static void set_gamepad_rumble(GenericHID.RumbleType side, double value) {
        _gamepad.setRumble(side, value);
    }
}
