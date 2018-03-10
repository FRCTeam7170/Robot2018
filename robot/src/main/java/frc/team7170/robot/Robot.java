package frc.team7170.robot;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import frc.team7170.control.Control;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.subsystems.drive.Drive;


public class Robot extends IterativeRobot {

    private final static Logger LOGGER = Logger.getLogger(Robot.class.getName());

    private Drive drive = Drive.get_instance();
    private Control ctrl = Control.get_instance();


    //----------Inherited initialization functions----------//

    public void robotInit() {
        LOGGER.info("Initializing robot...");
        // TODO: Manually force load & initialization of each module with Class.forName() ?
        CameraServer.getInstance().startAutomaticCapture();
        LOGGER.info("Initialization done.");
    }


    public void disabledInit() {
        System.out.println("DISABLED INIT");
    }


    public void autonomousInit() {
        System.out.println("AUTO INIT");
    }


    public void teleopInit() {
        System.out.println("TELEOP INIT");
    }


    public void testInit() {
        System.out.println("TEST INIT");
    }


    //----------Inherited periodic functions----------//

    public void robotPeriodic() {

    }


    public void disabledPeriodic() {

    }


    public void autonomousPeriodic() {

    }


    public void teleopPeriodic() {
        drive.set_arcade(ctrl.joystick.Axes.Y.get(),
                ctrl.joystick.Axes.X.get(), false, false);
        System.out.println("LEFT: "+drive.get_Lenc()+" , RIGHT: "+drive.get_Renc());
        if (ctrl.joystick.Buttons.TRIGGER.get_pressed()) {
            drive.reset_encoders();
        }

    }


    public void testPeriodic() {

    }
}