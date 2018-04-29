package frc.team7170.control.keymaps;

import edu.wpi.first.networktables.NetworkTableEntry;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.control.Control;
import frc.team7170.control.Action;
import frc.team7170.robot.RobotMap;


public class JoystickBindings1 extends KeyMap implements Communicator {

    private static KeyMap instance = new JoystickBindings1();  // Singleton
    public static KeyMap get_instance() {
        return instance;
    }
    private JoystickBindings1() {
        axes.put(Action.A_DRIVE_Y, Control.get_instance().joystick.axes.Y);
        axes.put(Action.A_DRIVE_Z, Control.get_instance().joystick.axes.X);
        axes.put(Action.A_THROTTLE_CONTROL, Control.get_instance().joystick.axes.THROTTLE);
        buttons.put(Action.B_ENDE_PUSH, Control.get_instance().joystick.buttons.TRIGGER);
        buttons.put(Action.B_ENDE_SUCK, Control.get_instance().joystick.buttons.THUMB);
        buttons.put(Action.B_TRY_ARM_TOGGLE, Control.get_instance().joystick.buttons.B11);
        buttons.put(Action.B_ARM_BASE, Control.get_instance().joystick.buttons.B12);
        axes.put(Action.A_ARM_ANALOG, Control.get_instance().joystick.axes.TWIST);

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
