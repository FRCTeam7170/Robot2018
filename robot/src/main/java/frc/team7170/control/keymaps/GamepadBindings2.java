package frc.team7170.control.keymaps;

import edu.wpi.first.networktables.NetworkTableEntry;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.control.Control;
import frc.team7170.control.Action;
import frc.team7170.robot.RobotMap;


public class GamepadBindings2 extends KeyMap implements Communicator {

    private static KeyMap instance = new GamepadBindings2();  // Singleton
    public static KeyMap get_instance() {
        return instance;
    }
    private GamepadBindings2() {
        axes.put(Action.A_DRIVE_Y, Control.get_instance().gamepad.axes.RY);
        axes.put(Action.A_DRIVE_Z, Control.get_instance().gamepad.axes.RX);
        axes.put(Action.A_ARM_ANALOG, Control.get_instance().gamepad.axes.LY);
        axes.put(Action.A_ENDE_ANALOG_PUSH, Control.get_instance().gamepad.axes.RT);
        axes.put(Action.A_ENDE_ANALOG_SUCK, Control.get_instance().gamepad.axes.LT);
        buttons.put(Action.B_TRY_ARM_TOGGLE, Control.get_instance().gamepad.buttons.LJOYIN);
        buttons.put(Action.B_ARM_BASE, Control.get_instance().gamepad.buttons.X);
        buttons.put(Action.B_TOGGLE_SPEED, Control.get_instance().gamepad.buttons.RJOYIN);

        POV = Control.get_instance().gamepad.POV;

        register_comm();
    }

    @Override
    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = RobotMap.Communication.DB_avail_keymaps)
    public void transmitter(NetworkTableEntry entry) {
        post_to_entry(entry);
    }
}
