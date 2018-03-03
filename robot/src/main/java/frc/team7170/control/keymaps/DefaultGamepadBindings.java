package frc.team7170.control.keymaps;

import frc.team7170.control.Control;
import frc.team7170.control.Action;


public class DefaultGamepadBindings extends KeyMap {
    public DefaultGamepadBindings() {
        axes.put(Action.A_DRIVE_Y, Control.gamepad.Axes.LY);
        axes.put(Action.A_DRIVE_X, Control.gamepad.Axes.LX);
        axes.put(Action.A_ARM_ANALOG, Control.gamepad.Axes.RY);
        buttons.put(Action.B_ENDE_PUSH, Control.gamepad.Buttons.LB);
        buttons.put(Action.B_ENDE_SUCK, Control.gamepad.Buttons.RB);
        buttons.put(Action.B_ENDE_OFF, Control.gamepad.Buttons.B);
        buttons.put(Action.B_TRY_ARM_TOGGLE, Control.gamepad.Buttons.A);

        POV = Control.gamepad.POV;
    }
}
