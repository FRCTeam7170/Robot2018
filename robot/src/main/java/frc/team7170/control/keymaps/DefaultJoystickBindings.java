package frc.team7170.control.keymaps;

import frc.team7170.control.Control;
import frc.team7170.control.Action;


public class DefaultJoystickBindings extends KeyMap {
    public DefaultJoystickBindings() {
        axes.put(Action.A_DRIVE_Y, Control.joystick.Axes.Y);
        axes.put(Action.A_DRIVE_X, Control.joystick.Axes.X);
        buttons.put(Action.B_ENDE_PUSH, Control.joystick.Buttons.TRIGGER);
        buttons.put(Action.B_ENDE_SUCK, Control.joystick.Buttons.B3);
        buttons.put(Action.B_ENDE_OFF, Control.joystick.Buttons.B4);
        buttons.put(Action.B_TRY_ARM_TOGGLE, Control.joystick.Buttons.THUMB);
        buttons.put(Action.B_ARM_UP, Control.joystick.POV.TOP);
        buttons.put(Action.B_ARM_DOWN, Control.joystick.POV.BOTTOM);

        POV = Control.joystick.POV;
    }
}
