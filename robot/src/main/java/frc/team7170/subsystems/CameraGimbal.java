package frc.team7170.subsystems;

import java.util.logging.Logger;
import edu.wpi.first.wpilibj.Servo;
import frc.team7170.robot.RobotMap;

public class CameraGimbal {

    private final static Logger LOGGER = Logger.getLogger(CameraGimbal.class.getName());

    private static Servo cam_X = new Servo(RobotMap.PWM.camera_servo_X);
    private static Servo cam_Y = new Servo(RobotMap.PWM.camera_servo_Y);

    /**
     * Must be called regularly in robot main loop as the servos are only incremented by a certain step/speed.
     * @param angle Direction to move camera FOV to. Look at switch statement for valid angles.
     *              Meant to be used with POV-thumb-joystick-thing on Logitech joystick.
     */
    public static void set(int angle) {
        switch (angle) {
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
