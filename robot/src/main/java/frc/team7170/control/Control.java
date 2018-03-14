package frc.team7170.control;

import java.util.HashSet;
import java.util.logging.Logger;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.RpcAnswer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.RPCCaller;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.control.keymaps.DefaultGamepadBindings;
import frc.team7170.control.keymaps.DefaultJoystickBindings;
import frc.team7170.control.keymaps.KeyMap;
import frc.team7170.robot.RobotMap;


/**
 * The control system is simply the system responsible for managing human input from the joysticks.
 *
 * This class provides a whole suite of convenience bindings for both the Logitech ED Extreme Pro joystick and the
 * Logitech Gamepad F310. This makes referencing buttons/axes on the devices much easier with something like
 * {@code joystick.Buttons.TRIGGER.get()} as opposed to {@code joystick.getButton(1)}, which isn't as explicit
 * and requires an id number.
 *
 * Each POV direction of the devices can be treated as a button mutually exclusive with each other POV
 * direction thanks to the {@link HIDPOVAccessor} class.
 *
 * To make switching key bindings easier in the future, all attempts to poll a button or axis should be done so through
 * {@link Control#action2button(Action)} or {@link Control#action2axis(Action)}. Note these methods return null if a
 * given action is unbound for the current key binding, so null checking must be implemented.
 */
public class Control implements Communicator {

    private final static Logger LOGGER = Logger.getLogger(Control.class.getName());

    private static Control instance = new Control();  // Singleton
    public static Control get_instance() {
        return instance;
    }
    private Control() {
        LOGGER.info("Initializing control system.");
        register_comm();
    }

    // Technically these don't have to be a Joystick because we're simply accessing raw values
    private final Joystick _joystick = new Joystick(RobotMap.Controllers.joystick);
    private final Joystick _gamepad = new Joystick(RobotMap.Controllers.gamepad);

    /**
     * A map of action-button/axis pairs so we can easily switch bindings without having to change code in multiple different classes
     */
    private KeyMap keymap = DefaultGamepadBindings.get_instance();

    /**
     * Set of all actions that have had warnings logged about being unbound already to prevent console spam.
     */
    private HashSet<Action> warned = new HashSet<>();


    // Here we provide an easy way to reference buttons/axes by doing something like:
    //     joystick.Buttons.TRIGGER.get()
    // More verbose, but also more explicit than something like joystick.getButton(1)

    public class _POV {
        public final HIDPOVAccessor TOP;
        public final HIDPOVAccessor TOP_RIGHT;
        public final HIDPOVAccessor RIGHT;
        public final HIDPOVAccessor BOTTOM_RIGHT;
        public final HIDPOVAccessor BOTTOM;
        public final HIDPOVAccessor BOTTOM_LEFT;
        public final HIDPOVAccessor LEFT;
        public final HIDPOVAccessor TOP_LEFT;
        public final HIDPOVAccessor CENTRE;

        private GenericHID joy;

        _POV(GenericHID joy) {
            TOP             = new HIDPOVAccessor(0, joy);
            TOP_RIGHT       = new HIDPOVAccessor(45, joy);
            RIGHT           = new HIDPOVAccessor(90, joy);
            BOTTOM_RIGHT    = new HIDPOVAccessor(135, joy);
            BOTTOM          = new HIDPOVAccessor(180, joy);
            BOTTOM_LEFT     = new HIDPOVAccessor(225, joy);
            LEFT            = new HIDPOVAccessor(270, joy);
            TOP_LEFT        = new HIDPOVAccessor(315, joy);
            CENTRE          = new HIDPOVAccessor(-1, joy);
            this.joy = joy;
        }

        public int get_degree() {
            return joy.getPOV();
        }
    }

    public class _JoystickButtons {
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

        _JoystickButtons() {}
    }

    public class _JoystickAxes {
        public final HIDAxisAccessor X        = new HIDAxisAccessor(0, _joystick);
        public final HIDAxisAccessor Y        = new HIDAxisAccessor(1, _joystick);
        public final HIDAxisAccessor Z        = new HIDAxisAccessor(2, _joystick);
        public final HIDAxisAccessor TWIST    = Z;
        public final HIDAxisAccessor THROTTLE = new HIDAxisAccessor(3, _joystick);

        _JoystickAxes() {}
    }

    public class _Joystick {
        public final _JoystickButtons Buttons = new _JoystickButtons();  // Singleton
        public final _JoystickAxes Axes = new _JoystickAxes();  // Singleton
        public final _POV POV = new _POV(_joystick);

        _Joystick() {}
    }
    public final _Joystick joystick = new _Joystick();  // Singleton


    public class _GamepadButtons {
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

        _GamepadButtons() {}
    }

    public class _GamepadAxes {
        public final HIDAxisAccessor LX = new HIDAxisAccessor(0, _gamepad);
        public final HIDAxisAccessor LY = new HIDAxisAccessor(1, _gamepad);
        public final HIDAxisAccessor LT = new HIDAxisAccessor(2, _gamepad);
        public final HIDAxisAccessor RT = new HIDAxisAccessor(3, _gamepad);
        public final HIDAxisAccessor RX = new HIDAxisAccessor(4, _gamepad);
        public final HIDAxisAccessor RY = new HIDAxisAccessor(5, _gamepad);

        _GamepadAxes() {}
    }

    public class _Gamepad {
        public final _GamepadButtons Buttons = new _GamepadButtons();  // Singleton
        public final _GamepadAxes Axes = new _GamepadAxes();  // Singleton
        public final _POV POV = new _POV(_gamepad);

        _Gamepad() {}
    }
    public final _Gamepad gamepad = new _Gamepad();  // Singleton


    /**
     * Sets the rumble on the gamepad.
     * @see Joystick#setRumble(GenericHID.RumbleType, double)
     * @param side A {@link edu.wpi.first.wpilibj.GenericHID.RumbleType} side.
     * @param value Magnitude of the rumble in [0, 1].
     */
    public void set_gamepad_rumble(GenericHID.RumbleType side, double value) {
        _gamepad.setRumble(side, value);
    }

    /**
     * This is the recommended way to reference buttons on HIDs.
     * @param act The action to poll the keymap for.
     * @return The {@link HIDButtonAccessor} object bound to the given action or null if the action is unbound.
     */
    public HIDButtonAccessor action2button(Action act) {
        HIDButtonAccessor btn = keymap.get_buttons().get(act);
        if (btn == null && !warned.contains(act)) {
            warned.add(act);
            LOGGER.warning("Unregistered action requested: " + act + " not in KeyMap " + keymap.getClass().getName() + ".");
        }
        return btn;
    }

    /**
     * This is the recommended way to reference axes on HIDs.
     * @param act The action to poll the keymap for.
     * @return The {@link HIDAxisAccessor} object bound to the given action or null if the action is unbound.
     */
    public HIDAxisAccessor action2axis(Action act) {
        HIDAxisAccessor axis = keymap.get_axes().get(act);
        if (axis == null && !warned.contains(act)) {
            warned.add(act);
            LOGGER.warning("Unregistered action requested: " + act + " not in KeyMap " + keymap.getClass().getName() + ".");
        }
        return axis;
    }

    /**
     * Change the keymap. This is useful for when switching joysticks or drivers.
     * @param km The new keymap to use.
     */
    public void set_keymap(KeyMap km) {
        if (km == null) {
            LOGGER.warning("Attempted to set keymap to null.");
            return;
        }
        LOGGER.info("Switching KeyMap from " + keymap.getClass().getName() + " to " + km.getClass().getName() + ".");
        warned.clear();
        keymap = km;
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = {
            "O_JOYSTICK_PORT_NS",
            "O_GAMEPAD_PORT_S"
    })
    public void transmitter_static(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_JOYSTICK_PORT_S":
                entry.setDouble(RobotMap.Controllers.joystick);
                break;
            case "O_GAMEPAD_PORT_S":
                entry.setDouble(RobotMap.Controllers.gamepad);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.SLOW, value = {
            "O_CURR_KEYMAP_S"
    })
    public void transmitter_slow(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_CURR_KEYMAP_M":
                entry.setString(keymap.toString());
                break;
        }
    }

    @SuppressWarnings("unused")
    @RPCCaller("R_SET_KEYMAP")
    public void rpccaller_keymap(RpcAnswer rpc) {
        // Only allow changing keymap while the robot is in disabled mode
        if (DriverStation.getInstance().isDisabled()) {
            set_keymap(KeyMap.registered_keymaps.get(rpc.params));
            rpc.postResponse(new byte[] {1});  // Success (unless the value mapped to rpc.params was null)
            return;
        }
        LOGGER.warning("Attempted to change keymap while robot is not in disabled state.");
        rpc.postResponse(new byte[] {0});  // Failure
    }
}
