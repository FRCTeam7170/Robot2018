package frc.team7170.control.keymaps;

import frc.team7170.control.HIDButtonAccessor;
import frc.team7170.control.HIDAxisAccessor;
import frc.team7170.control.Control;
import frc.team7170.control.Control._POV;
import frc.team7170.control.Action;
import java.util.HashMap;


/**
 * Template for a key bindings profile.
 *
 * buttons is a map of {@link Action}/{@link HIDButtonAccessor} pairs, where a given value is the button that should be
 * polled for information on the corresponding action.
 * Note these buttons could also be a {@link frc.team7170.control.HIDPOVAccessor}.
 *
 * Likewise, axes is a map of {@link Action}/{@link HIDAxisAccessor} pairs, where a given value is the axis that
 * should be polled for information on the corresponding action.
 *
 * Actions are not restricted to being mapped to specifically either {@link HIDButtonAccessor}s or {@link HIDAxisAccessor}s,
 * despite having differing return values (boolean and double, respectively.) It is up to the implementation of each
 * keymap to appropriately bind actions, for any two actions won't necessarily require the same type of input. A motor
 * speed, for example, intrinsically requires a numerical input in some range (i.e. axis input.) A pneumatic arm, on
 * the other hand, naturally requires a boolean on or off value (i.e. button input.)
 */
abstract class KeyMap {

    HashMap<Action, HIDButtonAccessor> buttons = new HashMap<>();

    HashMap<Action, HIDAxisAccessor> axes = new HashMap<>();

    /**
     * Can optionally be set to either {@link Control#joystick} or {@link Control#gamepad} to allow the use of
     * {@link _POV#get_degree()}.
     */
    _POV POV;
}
