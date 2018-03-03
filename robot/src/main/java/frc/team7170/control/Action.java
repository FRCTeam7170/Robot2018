package frc.team7170.control;


/**
 * Enum of all valid actions usable in the key binding system.
 * @see KeyBindings
 *
 * {@link frc.team7170.control.keymaps.KeyMap}s don't necessarily need to provide a mapping for each action, but if they
 * don't, {@link KeyBindings#action2axis(Action)} and {@link KeyBindings#action2button(Action)} return null, and, as
 * such, null checking must be implemented.
 *
 * All actions that require an axis (double) input should be prefixed with "A_..."
 * All actions that require a button (boolean) input should be prefixed with "B_..."
 */
public enum Action {
    A_DRIVE_Y,
    A_DRIVE_X,
    A_THROTTLE_CONTROL,

    B_ARM_UP,
    B_ARM_DOWN,
    A_ARM_ANALOG,

    B_ENDE_SUCK,
    B_ENDE_PUSH,
    B_ENDE_OFF,

    B_ARM_RETRACT,
    B_TRY_ARM_EXTEND,
    B_TRY_ARM_TOGGLE
}
