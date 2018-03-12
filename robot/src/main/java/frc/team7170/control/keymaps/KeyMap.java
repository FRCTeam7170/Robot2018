package frc.team7170.control.keymaps;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.RpcAnswer;
import frc.team7170.comm.Communicator;
import frc.team7170.control.HIDButtonAccessor;
import frc.team7170.control.HIDAxisAccessor;
import frc.team7170.control.Control;
import frc.team7170.control.Control._POV;
import frc.team7170.control.Action;
import frc.team7170.robot.RobotMap;
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
 *
 * In order for each keymap to be shown on the dashboard, each keymap must be declared as a
 * {@link frc.team7170.comm.Communicator} and override {@link KeyMap#transmitter(NetworkTableEntry)}, making it a
 * {@link frc.team7170.comm.Transmitter} with the parameters suggested in the source code comment above the abstract
 * definition. Then simply call {@link KeyMap#post_to_entry(NetworkTableEntry)} with the given entry as an argument.
 * Note that the reason this cannot be handled entirely in this superclass ({@link KeyMap}) is because
 * {@link frc.team7170.comm.Communication#register_communicator(frc.team7170.comm.Communicator)} only scans for declared
 * methods, not inherited methods, and hence such a transmitter implementation here would be ignored. A possible
 * workaround for this is to indeed declare this class as the {@link frc.team7170.comm.Communicator} and have each
 * keymap register itself to this class, but with the poll rate being static (and anything other than static is simply
 * inefficient), the registration for each keymap would have to happen prior to registering this class with
 * {@link frc.team7170.comm.Communication}, which would surely result in a more awkward procedure (call to
 * {@link Communicator#register_comm()} would have to be deferred until each keymap registers itself).
 */
public abstract class KeyMap {

    HashMap<Action, HIDButtonAccessor> buttons = new HashMap<>();

    HashMap<Action, HIDAxisAccessor> axes = new HashMap<>();

    /**
     * Can optionally be set to either {@link Control#joystick} or {@link Control#gamepad} to allow the use of
     * {@link _POV#get_degree()}.
     */
    _POV POV;

    public HashMap<Action, HIDButtonAccessor> get_buttons() {
        return buttons;
    }

    public HashMap<Action, HIDAxisAccessor> get_axes() {
        return axes;
    }

    public _POV get_POV() {
        return POV;
    }

    /**
     * List of keymaps that have called {@link KeyMap#post_to_entry(NetworkTableEntry)}. This allows easy lookup of
     * all the keymaps available to be selected in the dashboard from within
     * {@link Control#rpccaller_keymap(RpcAnswer)}.
     * @see Control#rpccaller_keymap(RpcAnswer)
     */
    public static HashMap<String, KeyMap> registered_keymaps = new HashMap<>();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    // @SuppressWarnings("unused")
    // @Transmitter(poll_rate = TransmitFrequency.STATIC, value = RobotMap.Communication.DB_avail_keymaps)
    public abstract void transmitter(NetworkTableEntry entry);

    /**
     * Constructs a string listing all the bindings for this entry and appends it to the given entry's string value.
     * The resulting string should follow the form
     *      "keymap1=action1:binding1,action2:binding2,...,actionN:bindingN,;keymap2=action1:binding1,...,;..."
     * I.e. the separators between
     *      keymaps is ";",
     *      each keymap and its action-binding pairs is "=",
     *      each action-binding pair is ",",
     *      and each action and its binding is ":".
     * This string can then be parsed and displayed on the dashboard.
     */
    final void post_to_entry(NetworkTableEntry entry) {
        if (!entry.getName().equals(RobotMap.Communication.DB_avail_keymaps)) {
            throw new RuntimeException("Entry used to transmit keymap details must be "+RobotMap.Communication.DB_avail_keymaps);
        }
        try {
            registered_keymaps.putIfAbsent(toString(), (KeyMap) getClass().getDeclaredMethod("get_instance").invoke(null));
        } catch (Exception e) {
            throw new RuntimeException("Problem mapping instance of "+toString()+" to registered_keymaps.");
        }
        StringBuilder sb = new StringBuilder(500);
        sb.append(entry.getString("")).append(toString()).append('=');
        for (Action act : buttons.keySet()) {
            sb.append(act.toString()).append(":").append(buttons.get(act).toString()).append(",");
        }
        for (Action act : axes.keySet()) {
            sb.append(act.toString()).append(":").append(axes.get(act).toString()).append(",");
        }
        sb.append(";");
        entry.setString(sb.toString());
    }
}
