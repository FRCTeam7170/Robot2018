package frc.team7170.control.keymaps;

import edu.wpi.first.networktables.NetworkTableEntry;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.control.Control;
import frc.team7170.control.Action;
import frc.team7170.robot.RobotMap;


public class GamepadBindings1 extends KeyMap implements Communicator {

    private static KeyMap instance = new GamepadBindings1();  // Singleton
    public static KeyMap get_instance() {
        return instance;
    }
    private GamepadBindings1() {
        axes.put(Action.A_DRIVE_Y, Control.get_instance().gamepad.axes.RY);
        axes.put(Action.A_DRIVE_Z, Control.get_instance().gamepad.axes.RX);
        axes.put(Action.A_ARM_ANALOG_DOWN, Control.get_instance().gamepad.axes.LT);
        axes.put(Action.A_ARM_ANALOG_UP, Control.get_instance().gamepad.axes.RT);
        buttons.put(Action.B_ENDE_PUSH, Control.get_instance().gamepad.buttons.RB);
        buttons.put(Action.B_ENDE_SUCK, Control.get_instance().gamepad.buttons.LB);
        buttons.put(Action.B_TRY_ARM_EXTEND, Control.get_instance().gamepad.buttons.A);
        buttons.put(Action.B_ARM_RETRACT, Control.get_instance().gamepad.buttons.B);
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
