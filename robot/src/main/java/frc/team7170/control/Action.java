package frc.team7170.control;


/**
 * Enum of all valid actions usable in the key binding system.
 * @see Control
 *
 * {@link frc.team7170.control.keymaps.KeyMap}s don't necessarily need to provide a mapping for each action, but if they
 * don't, {@link Control#action2axis(Action)} and {@link Control#action2button(Action)} return null, and, as
 * such, null checking must be implemented.
 *
 * All actions that require an axis (double) input should be prefixed with "A_..."
 * All actions that require a button (boolean) input should be prefixed with "B_..."
 */
public enum Action {
    A_DRIVE_Y,
    A_DRIVE_Z,
    A_DRIVE_L,
    A_DRIVE_R,
    A_THROTTLE_CONTROL,

    B_ARM_UP,
    B_ARM_DOWN,
    A_ARM_ANALOG,
    A_ARM_ANALOG_UP,
    A_ARM_ANALOG_DOWN,
    // TODO: Preset scale/switch positions for arm

    B_ENDE_SUCK,
    B_ENDE_PUSH,
    A_ENDE_ANALOG,

    B_ARM_RETRACT,
    B_TRY_ARM_EXTEND,
    B_TRY_ARM_TOGGLE
}
