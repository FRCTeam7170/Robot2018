package frc.team7170.robot;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import frc.team7170.util.TimedTask;
import frc.team7170.subsystems.Drive;


public class Robot extends IterativeRobot {

    private final static Logger LOGGER = Logger.getLogger(Robot.class.getName());

    private Joystick joy = new Joystick(RobotMap.Controllers.joystick_port);

    // TODO: TEMP
    private Solenoid solenoid5 = new Solenoid(15,0);
    private Solenoid solenoid4 = new Solenoid(15,1);

    // These store the X and Y values from the joystick
    private double joy_X, joy_Y;


    //----------Inherited initialization functions----------//

    public void robotInit() {
        System.out.println("ROBOT INIT");
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
        joy_X = joy.getX();
        joy_Y = joy.getY();

        // TODO: TEMP
        if(joy.getRawButtonPressed(4)) {
            solenoid4.set(!solenoid4.get());
            solenoid5.set(!solenoid5.get());
        }

        Drive.set_arcade(joy_X, joy_Y, true);
    }


    private TimedTask testp = new TimedTask(() -> System.out.println("TEST PERIODIC"), 3000);
    public void testPeriodic() {
        testp.run();
    }
}