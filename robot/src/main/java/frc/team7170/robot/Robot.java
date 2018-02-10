package frc.team7170.robot;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import frc.team7170.util.TimedTask;
import frc.team7170.subsystems.Drive;


public class Robot extends IterativeRobot {

    // TODO
    private final static Logger LOGGER = Logger.getLogger(Robot.class.getName());

    private Joystick joy = new Joystick(RobotMap.Controllers.joystick_port);

    // Servo instances for camera gimbal
    private Servo cam_X = new Servo(RobotMap.PWM.camera_servo_X);
    private Servo cam_Y = new Servo(RobotMap.PWM.camera_servo_Y);

    // TODO: TEMP
    private Solenoid solenoid5 = new Solenoid(15,0);
    private Solenoid solenoid4 = new Solenoid(15,1);

    // These store the X and Y values from the joystick
    public double joy_X, joy_Y;


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


    private TimedTask autoP = new TimedTask(() -> System.out.println("AUTO PERIODIC"), 3000);
    public void autonomousPeriodic() {
        autoP.run();
    }


    public void teleopPeriodic() {
        joy_X = joy.getX();
        joy_Y = joy.getY();

        // Manual control of camera gimbal via POV
        turn_camera();

        if(joy.getRawButtonPressed(4)) {
            solenoid4.set(!solenoid4.get());
            solenoid5.set(!solenoid5.get());
        }

        Drive.set(joy_X, joy_Y, true);
    }


    private TimedTask testp = new TimedTask(() -> System.out.println("TEST PERIODIC"), 3000);
    public void testPeriodic() {
        testp.run();
    }


    //----------Custom functions----------//

    /**
     * Manually adjust camera position using POV
     */
    private void turn_camera() {
        switch (joy.getPOV()) {
            case -1:  // POV Not being pressed
                break;
            case 0:  // Degree measures where 0 corresponds to north and they increase positively in clockwise direction
                cam_Y.set(cam_Y.get() + RobotMap.Camera.speed_Y);
                break;
            case 45:
                cam_Y.set(cam_Y.get() + RobotMap.Camera.speed_Y_45);
                cam_X.set(cam_X.get() + RobotMap.Camera.speed_X_45);
                break;
            case 90:
                cam_X.set(cam_X.get() + RobotMap.Camera.speed_X);
                break;
            case 135:
                cam_Y.set(cam_Y.get() - RobotMap.Camera.speed_Y_45);
                cam_X.set(cam_X.get() + RobotMap.Camera.speed_X_45);
                break;
            case 180:
                cam_Y.set(cam_Y.get() - RobotMap.Camera.speed_Y);
                break;
            case 225:
                cam_Y.set(cam_Y.get() - RobotMap.Camera.speed_Y_45);
                cam_X.set(cam_X.get() - RobotMap.Camera.speed_X_45);
                break;
            case 270:
                cam_X.set(cam_X.get() - RobotMap.Camera.speed_X);
                break;
            case 315:
                cam_Y.set(cam_Y.get() + RobotMap.Camera.speed_Y_45);
                cam_X.set(cam_X.get() - RobotMap.Camera.speed_X_45);
                break;
        }
    }
}