package frc.team7170.control.keymaps;

import frc.team7170.control.Control;
import frc.team7170.control.Action;


public class DefaultGamepadBindings extends KeyMap {
    public DefaultGamepadBindings() {
        axes.put(Action.A_DRIVE_Y, Control.get_instance().gamepad.Axes.LY);
        axes.put(Action.A_DRIVE_Z, Control.get_instance().gamepad.Axes.LX);
        axes.put(Action.A_ARM_ANALOG, Control.get_instance().gamepad.Axes.RY);
        buttons.put(Action.B_ENDE_PUSH, Control.get_instance().gamepad.Buttons.LB);
        buttons.put(Action.B_ENDE_SUCK, Control.get_instance().gamepad.Buttons.RB);
        buttons.put(Action.B_ENDE_OFF, Control.get_instance().gamepad.Buttons.B);
        buttons.put(Action.B_TRY_ARM_TOGGLE, Control.get_instance().gamepad.Buttons.A);

        POV = Control.get_instance().gamepad.POV;
    }
}
