package frc.team7170.robot;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import frc.team7170.control.Action;
import frc.team7170.control.Control;
import frc.team7170.control.HIDAxisAccessor;
import frc.team7170.control.HIDButtonAccessor;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.subsystems.arm.Arm;
import frc.team7170.subsystems.drive.Drive;


public class Robot extends IterativeRobot {

    private final static Logger LOGGER = Logger.getLogger(Robot.class.getName());

    /**
     * Because of the level of abstraction provide by {@link Dispatcher} and {@link frc.team7170.comm.Communication}, it
     * is possible that certain classes, namely singletons, may never load even though they are expected to. For
     * example, {@link Dispatcher} updates each module through its {@link Module#update()} method, but if the module is
     * never loaded, and hence never initialized, the update method for that module may be neglected. Also, certain
     * modules may need to be loaded before others to avoid null pointers, etcetera. This could be circumnavigated with
     * null checks and making the dispatcher manually register each module or something similar, but this is cleaner and
     * makes the initialization sequence more explicit. Therefore, this method force loads each specified class in the
     * specified order.
     */
    private void load_classes() {
        try {
            // Some of these may be unnecessary due to the fact that some modules cross reference each other
            // Nonetheless, they're all listed for completeness sake
            Class.forName("frc.team7170.jobs.Dispatcher");
            Class.forName("frc.team7170.comm.Communication");
            Class.forName("frc.team7170.comm.MiscSender");
            Class.forName("frc.team7170.control.Control");
            Class.forName("frc.team7170.control.keymaps.DefaultJoystickBindings");
            Class.forName("frc.team7170.control.keymaps.DefaultGamepadBindings");
            Class.forName("frc.team7170.subsystems.drive.Drive");
            Class.forName("frc.team7170.subsystems.arm.Arm");
            Class.forName("frc.team7170.subsystems.Pneumatics");
            Class.forName("frc.team7170.robot.Auto");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Exception while loading classes.");
        }
    }


    //----------Inherited initialization functions----------//

    public void robotInit() {
        LOGGER.info("Initializing robot...");
        load_classes();
        LOGGER.info("Starting camera capture.");
        CameraServer.getInstance().startAutomaticCapture();
        LOGGER.info("Initialization done.");
    }


    public void disabledInit() {
        LOGGER.info("ROBOT IN DISABLED");
        Dispatcher.get_instance().cancel_all();
        Drive.get_instance().set_enabled(false);
        Arm.get_instance().set_enabled(false);
    }


    public void autonomousInit() {
        LOGGER.info("ROBOT IN AUTONOMOUS");
        if (Auto.get_instance().resolve_auto()) {
            LOGGER.info("Resolving autonomous...Success.");
        } else {
            LOGGER.severe("Resolving autonomous...Failed.");
        }
        Drive.get_instance().set_enabled(true);
        Arm.get_instance().set_enabled(true);
    }


    public void teleopInit() {
        LOGGER.info("ROBOT IN TELEOP");
        Drive.get_instance().set_enabled(true);
        Arm.get_instance().set_enabled(true);
    }


    public void testInit() {
        LOGGER.info("ROBOT IN TEST");
    }


    //----------Inherited periodic functions----------//

    public void robotPeriodic() {
        Dispatcher.get_instance().run();
    }


    public void disabledPeriodic() {}


    public void autonomousPeriodic() {
        Auto.get_instance().run_auto();
    }


    public void teleopPeriodic() {
        // Poll drive controls
        HIDAxisAccessor y_axis = Control.get_instance().action2axis(Action.A_DRIVE_Y);
        HIDAxisAccessor z_axis = Control.get_instance().action2axis(Action.A_DRIVE_Z);
        if (y_axis != null && z_axis != null) {
            Drive.get_instance().set_arcade(y_axis.get(), z_axis.get(),
                    // TODO: TEMP -- testing smooth algorithm when B12 on joystick is pressed
                    Control.get_instance().joystick.Buttons.B12.get(), true);
        }
        // Poll endE controls
        HIDAxisAccessor endE_axis = Control.get_instance().action2axis(Action.A_ENDE_ANALOG);
        if (endE_axis != null) {
            Arm.get_instance().endE_analog(endE_axis.get());
        } else {
            HIDButtonAccessor endE_push_btn = Control.get_instance().action2button(Action.B_ENDE_PUSH);
            HIDButtonAccessor endE_suck_btn = Control.get_instance().action2button(Action.B_ENDE_SUCK);
            HIDButtonAccessor endE_off_btn = Control.get_instance().action2button(Action.B_ENDE_OFF);
            if (endE_push_btn != null && endE_push_btn.get_pressed()) {
                Arm.get_instance().endE_push();
            } else if (endE_suck_btn != null && endE_suck_btn.get_pressed()) {
                Arm.get_instance().endE_suck();
            } else if (endE_off_btn != null && endE_off_btn.get_pressed()) {
                Arm.get_instance().endE_kill();
            }
        }
        // Poll arm rotate controls
        HIDAxisAccessor arm_axis = Control.get_instance().action2axis(Action.A_ARM_ANALOG);
        if (arm_axis != null) {
            Arm.get_instance().arm_analog(arm_axis.get());
        } else {
            HIDButtonAccessor arm_up = Control.get_instance().action2button(Action.B_ARM_UP);
            HIDButtonAccessor arm_down = Control.get_instance().action2button(Action.B_ARM_DOWN);
            if (arm_up != null && arm_up.get()) {
                Arm.get_instance().arm_up();
            } else if (arm_down != null && arm_down.get()) {
                Arm.get_instance().arm_down();
            } else {
                Arm.get_instance().arm_kill();
            }
        }
        // Poll arm extension controls
        HIDButtonAccessor extend_btn = Control.get_instance().action2button(Action.B_TRY_ARM_EXTEND);
        HIDButtonAccessor retract_btn = Control.get_instance().action2button(Action.B_ARM_RETRACT);
        HIDButtonAccessor toggle_btn = Control.get_instance().action2button(Action.B_TRY_ARM_TOGGLE);
        if (extend_btn != null && extend_btn.get_pressed()) {
            Arm.get_instance().try_extend();
        } else if (retract_btn != null && retract_btn.get_pressed()) {
            Arm.get_instance().retract();
        } else if (toggle_btn != null && toggle_btn.get_pressed()) {
            Arm.get_instance().try_toggle();
        }
    }


    public void testPeriodic() {}
}