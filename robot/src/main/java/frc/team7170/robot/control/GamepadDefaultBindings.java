package frc.team7170.robot.control;


public class GamepadDefaultBindings extends KeyMap {
    GamepadDefaultBindings() {
        axes.put(KeyBindings.Action.DRIVE_Y, Control.gamepad.Axes.LY);
        axes.put(KeyBindings.Action.DRIVE_X, Control.gamepad.Axes.LX);
        buttons.put(KeyBindings.Action.ENDE_PUSH, Control.gamepad.Buttons.LB);
        buttons.put(KeyBindings.Action.ENDE_SUCK, Control.gamepad.Buttons.RB);
        buttons.put(KeyBindings.Action.ENDE_OFF, Control.gamepad.Buttons.B);
        buttons.put(KeyBindings.Action.TRY_ARM_TOGGLE, Control.gamepad.Buttons.A);
        axes.put(KeyBindings.Action.ARM_ANALOG, Control.gamepad.Axes.RY);
    }
}
