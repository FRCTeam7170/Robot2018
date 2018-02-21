package frc.team7170.robot;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.IterativeRobot;
import frc.team7170.robot.control.Control;
import frc.team7170.robot.control.KeyBindings;
import frc.team7170.subsystems.Pneumatics;
import frc.team7170.subsystems.comm.Communication;
import frc.team7170.subsystems.nav.Navigation;
import frc.team7170.util.TimedTask;
import frc.team7170.subsystems.Drive;
import frc.team7170.subsystems.Arm;


public class Robot extends IterativeRobot {

    private final static Logger LOGGER = Logger.getLogger(Robot.class.getName());


    //----------Inherited initialization functions----------//

    public void robotInit() {
        LOGGER.info("Initializing robot...");
        Control.init();
        Communication.init();
        Drive.init();
        // CameraGimbal.init();
        Navigation.init();
        Pneumatics.init();
        Arm.init();
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
        Arm.update();
        Navigation.update();
    }


    public void disabledPeriodic() {

    }


    public void autonomousPeriodic() {

    }


    public void teleopPeriodic() {
        Drive.set_arcade(KeyBindings.action2axis(KeyBindings.Action.DRIVE_Y).get(),
                KeyBindings.action2axis(KeyBindings.Action.DRIVE_X).get(), true, true);
    }


    public void testPeriodic() {

    }
}