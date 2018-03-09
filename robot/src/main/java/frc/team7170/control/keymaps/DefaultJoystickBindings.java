package frc.team7170.control.keymaps;

import frc.team7170.control.Control;
import frc.team7170.control.Action;

import java.util.HashMap;


public class DefaultJoystickBindings extends KeyMap {
    public DefaultJoystickBindings() {
        axes.put(Action.A_DRIVE_Y, Control.get_instance().joystick.Axes.Y);
        axes.put(Action.A_DRIVE_X, Control.get_instance().joystick.Axes.X);
        buttons.put(Action.B_ENDE_PUSH, Control.get_instance().joystick.Buttons.TRIGGER);
        buttons.put(Action.B_ENDE_SUCK, Control.get_instance().joystick.Buttons.B3);
        buttons.put(Action.B_ENDE_OFF, Control.get_instance().joystick.Buttons.B4);
        buttons.put(Action.B_TRY_ARM_TOGGLE, Control.get_instance().joystick.Buttons.THUMB);
        buttons.put(Action.B_ARM_UP, Control.get_instance().joystick.POV.TOP);
        buttons.put(Action.B_ARM_DOWN, Control.get_instance().joystick.POV.BOTTOM);

        POV = Control.get_instance().joystick.POV;
    }
}
