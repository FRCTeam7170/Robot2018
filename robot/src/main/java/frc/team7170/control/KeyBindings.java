package frc.team7170.control;

import frc.team7170.control.keymaps.DefaultJoystickBindings;
import frc.team7170.control.keymaps.KeyMap;
import java.util.logging.Logger;


public class KeyBindings {

    private final static Logger LOGGER = Logger.getLogger(KeyBindings.class.getName());

    private static KeyMap keymap = new DefaultJoystickBindings();

    public static HIDButtonAccessor action2button(Action act) {
        HIDButtonAccessor btn = keymap.get_buttons().get(act);
        if (btn == null) {
            LOGGER.warning("Unregistered action requested: " + act + " not in KeyMap " + keymap.getClass().getName() + ".");
        }
        return btn;
    }

    public static HIDAxisAccessor action2axis(Action act) {
        HIDAxisAccessor axis = keymap.get_axes().get(act);
        if (axis == null) {
            LOGGER.warning("Unregistered action requested: " + act + " not in KeyMap " + keymap.getClass().getName() + ".");
        }
        return axis;
    }

    public static void set_keymap(KeyMap km) {
        LOGGER.info("Switching KeyMap from " + keymap.getClass().getName() + " to " + km.getClass().getName() + ".");
        keymap = km;
    }
}
