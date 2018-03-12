package frc.team7170.robot;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import frc.team7170.control.Control;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.subsystems.drive.Drive;


public class Robot extends IterativeRobot {

    private final static Logger LOGGER = Logger.getLogger(Robot.class.getName());

    // TODO: TEMP
    private Drive drive = Drive.get_instance();
    private Control ctrl = Control.get_instance();

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
        LOGGER.info("DISABLED INIT");
    }


    public void autonomousInit() {
        LOGGER.info("AUTO INIT");
    }


    public void teleopInit() {
        LOGGER.info("TELEOP INIT");
    }


    public void testInit() {
        LOGGER.info("TEST INIT");
    }


    //----------Inherited periodic functions----------//

    public void robotPeriodic() {}


    public void disabledPeriodic() {}


    public void autonomousPeriodic() {}


    public void teleopPeriodic() {
        drive.set_arcade(ctrl.joystick.Axes.Y.get(),
                ctrl.joystick.Axes.X.get(), false, false);
        System.out.println("LEFT: "+drive.get_Lenc()+" , RIGHT: "+drive.get_Renc());
        if (ctrl.joystick.Buttons.TRIGGER.get_pressed()) {
            drive.reset_encoders();
        }
    }


    public void testPeriodic() {}
}