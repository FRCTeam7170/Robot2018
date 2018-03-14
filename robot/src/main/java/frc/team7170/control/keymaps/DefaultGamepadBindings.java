package frc.team7170.control.keymaps;

import edu.wpi.first.networktables.NetworkTableEntry;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.control.Control;
import frc.team7170.control.Action;
import frc.team7170.robot.RobotMap;


public class DefaultGamepadBindings extends KeyMap implements Communicator {

    private static KeyMap instance = new DefaultGamepadBindings();  // Singleton
    public static KeyMap get_instance() {
        return instance;
    }
    private DefaultGamepadBindings() {
        axes.put(Action.A_DRIVE_Y, Control.get_instance().gamepad.Axes.LY);
        axes.put(Action.A_DRIVE_Z, Control.get_instance().gamepad.Axes.LX);
        axes.put(Action.A_ARM_ANALOG, Control.get_instance().gamepad.Axes.RY);
        buttons.put(Action.B_ENDE_PUSH, Control.get_instance().gamepad.Buttons.LB);
        buttons.put(Action.B_ENDE_SUCK, Control.get_instance().gamepad.Buttons.RB);
        buttons.put(Action.B_ENDE_OFF, Control.get_instance().gamepad.Buttons.B);
        // buttons.put(Action.B_TRY_ARM_TOGGLE, Control.get_instance().gamepad.Buttons.A);

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
