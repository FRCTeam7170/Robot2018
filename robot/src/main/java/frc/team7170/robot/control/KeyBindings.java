package frc.team7170.robot.control;

import java.util.logging.Logger;


public class KeyBindings {

    private final static Logger LOGGER = Logger.getLogger(KeyBindings.class.getName());

    private static KeyMap keymap = new DefaultBindings();

    public enum Action {
        DRIVE_Y,
        DRIVE_X,
        THROTTLE_CONTROL,
        ARM_UP,
        ARM_DOWN,
        ENDE_SUCK,
        ENDE_PUSH,
        ENDE_OFF,
        ARM_RETRACT,
        TRY_ARM_EXTEND,
        TRY_ARM_TOGGLE;
    }

    public static Control.HIDButtonAccessor action2button(Action act) {
        Control.HIDButtonAccessor btn = keymap.buttons.get(act);
        if (btn == null) {
            LOGGER.warning("Unregistered action requested: " + act + " not in KeyMap " + keymap.getClass().getName() + ".");
        }
        return btn;
    }

    public static Control.HIDAxisAccessor action2axis(Action act) {
        Control.HIDAxisAccessor axis = keymap.axes.get(act);
        if (axis == null) {
            LOGGER.warning("Unregistered action requested: " + act + " not in KeyMap " + keymap.getClass().getName() + ".");
        }
        return axis;
    }

    public static Control.HIDPOVAccessor action2pov(Action act) {
        Control.HIDPOVAccessor pov = keymap.POV.get(act);
        if (pov == null) {
            LOGGER.warning("Unregistered action requested: " + act + " not in KeyMap " + keymap.getClass().getName() + ".");
        }
        return pov;
    }

    public static void set_keymap(KeyMap keymap) {
        LOGGER.info("Switching KeyMap from " + KeyBindings.keymap.getClass().getName() + " to " + keymap.getClass().getName() + ".");
        KeyBindings.keymap = keymap;
    }
}
