package frc.team7170.control.keymaps;

import edu.wpi.first.networktables.NetworkTableEntry;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.control.Control;
import frc.team7170.control.Action;
import frc.team7170.robot.RobotMap;


public class DefaultJoystickBindings extends KeyMap implements Communicator {

    private static KeyMap instance = new DefaultJoystickBindings();  // Singleton
    public static KeyMap get_instance() {
        return instance;
    }
    private DefaultJoystickBindings() {
        axes.put(Action.A_DRIVE_Y, Control.get_instance().joystick.Axes.Y);
        axes.put(Action.A_DRIVE_Z, Control.get_instance().joystick.Axes.X);
        buttons.put(Action.B_ENDE_PUSH, Control.get_instance().joystick.Buttons.TRIGGER);
        buttons.put(Action.B_ENDE_SUCK, Control.get_instance().joystick.Buttons.B3);
        buttons.put(Action.B_ENDE_OFF, Control.get_instance().joystick.Buttons.B4);
        buttons.put(Action.B_TRY_ARM_TOGGLE, Control.get_instance().joystick.Buttons.THUMB);
        buttons.put(Action.B_ARM_UP, Control.get_instance().joystick.POV.TOP);
        buttons.put(Action.B_ARM_DOWN, Control.get_instance().joystick.POV.BOTTOM);

        POV = Control.get_instance().joystick.POV;

        register_comm();
    }

    @Override
    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = RobotMap.Communication.DB_avail_keymaps)
    public void transmitter(NetworkTableEntry entry) {
        post_to_entry(entry);
    }
}
