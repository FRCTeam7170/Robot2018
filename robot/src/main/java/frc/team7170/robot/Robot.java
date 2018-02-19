package frc.team7170.robot;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.IterativeRobot;
import frc.team7170.subsystems.CameraGimbal;
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
        Control.init();
        Communication.init();
        Drive.init();
        // CameraGimbal.init();
        Navigation.init();
        Pneumatics.init();
        Arm.init();
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

    private TimedTask robotp = new TimedTask(() -> System.out.println("ROBOT PERIODIC"), 3000);
    public void robotPeriodic() {
        robotp.run();
    }


    private TimedTask disabledp = new TimedTask(() -> System.out.println("DISABLED PERIODIC"), 3000);
    public void disabledPeriodic() {
        disabledp.run();
    }


    private TimedTask autop = new TimedTask(() -> System.out.println("AUTO PERIODIC"), 3000);
    public void autonomousPeriodic() {
        autop.run();
    }


    public void teleopPeriodic() {
        Drive.set_arcade(Control.X(), Control.Y(), true, true);
    }


    private TimedTask testp = new TimedTask(() -> System.out.println("TEST PERIODIC"), 3000);
    public void testPeriodic() {
        testp.run();
    }
}