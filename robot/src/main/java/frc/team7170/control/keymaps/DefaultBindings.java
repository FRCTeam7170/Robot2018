package frc.team7170.control.keymaps;


import frc.team7170.control.Control;
import frc.team7170.control.KeyBindings;

public class DefaultBindings extends KeyMap {
    DefaultBindings() {
        axes.put(KeyBindings.Action.DRIVE_Y, Control.joystick.Axes.Y);
        axes.put(KeyBindings.Action.DRIVE_X, Control.joystick.Axes.X);
        buttons.put(KeyBindings.Action.ENDE_PUSH, Control.joystick.Buttons.TRIGGER);
        buttons.put(KeyBindings.Action.ENDE_SUCK, Control.joystick.Buttons.B3);
        buttons.put(KeyBindings.Action.ENDE_OFF, Control.joystick.Buttons.B4);
        buttons.put(KeyBindings.Action.TRY_ARM_TOGGLE, Control.joystick.Buttons.THUMB);
        POV.put(KeyBindings.Action.ARM_UP, Control.joystick.POV.TOP);
        POV.put(KeyBindings.Action.ARM_DOWN, Control.joystick.POV.BOTTOM);
    }
}
