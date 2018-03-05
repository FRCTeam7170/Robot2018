package frc.team7170.robot;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.IterativeRobot;
import frc.team7170.control.Control;
import frc.team7170.control.Action;
import frc.team7170.subsystems.Pneumatics;
import frc.team7170.comm.Communication;
import frc.team7170.subsystems.drive.Acceleration;
import frc.team7170.subsystems.drive.Drive;
import frc.team7170.subsystems.arm.Arm;
import edu.wpi.first.wpilibj.Encoder;
import frc.team7170.util.TimedTask;


public class Robot extends IterativeRobot {

    private final static Logger LOGGER = Logger.getLogger(Robot.class.getName());

    // TODO: TEMP
    private Encoder enc = new Encoder(7, 8);
    private Acceleration accel;
    private TimedTask reset_enc = new TimedTask(() -> {
            enc.reset();
            accel = new Acceleration(0.75, 0.25, 0, 0.25, 0.75, false, false, false);
        }, 10000);


    //----------Inherited initialization functions----------//

    public void robotInit() {
        /*
        LOGGER.info("Initializing robot...");
        Control.init();
        Communication.init();
        Drive.init();
        // CameraGimbal.init();
        Pneumatics.init();
        Arm.init();
        LOGGER.info("Initialization done.");
        */
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
        /*
        Drive.get_instance().set_arcade(Control.action2axis(Action.DRIVE_Y).get(),
                Control.action2axis(Action.DRIVE_X).get(), true, true);
                */
        reset_enc.run();
        double encval = Math.abs(enc.get());
        if (encval < 90) {
            double d = accel.get(encval/90.0);
            Drive.get_instance().set_tank(d, d, false, false);
        } else {
            Drive.get_instance().set_tank(0, 0, false, false);
        }
        System.out.println(encval);
    }


    public void testPeriodic() {

    }
}