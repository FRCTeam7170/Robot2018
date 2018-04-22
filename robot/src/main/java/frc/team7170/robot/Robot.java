package frc.team7170.robot;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.Receiver;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.control.Control;
import frc.team7170.control.keymaps.DefaultGamepadBindings;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.JRunnable;
import frc.team7170.jobs.Module;
import frc.team7170.subsystems.arm.ArmEndE;
import frc.team7170.subsystems.arm.ArmRotate;
import frc.team7170.subsystems.drive.Acceleration;
import frc.team7170.subsystems.drive.Drive;
import frc.team7170.util.CalcUtil;
import frc.team7170.util.DebugUtil;

import java.util.logging.Logger;


public class Robot extends IterativeRobot implements Communicator {

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
            //Class.forName("frc.team7170.comm.Communication");
            //Class.forName("frc.team7170.comm.MiscSender");
            Class.forName("frc.team7170.control.Control");
            Class.forName("frc.team7170.control.keymaps.DefaultJoystickBindings");
            Class.forName("frc.team7170.control.keymaps.DefaultGamepadBindings");
            Class.forName("frc.team7170.control.keymaps.JoelBindings");
            Class.forName("frc.team7170.subsystems.drive.Drive");
            Class.forName("frc.team7170.subsystems.arm.ArmRotate");
            Class.forName("frc.team7170.subsystems.arm.ArmEndE");
            Class.forName("frc.team7170.subsystems.Pneumatics");
            //Class.forName("frc.team7170.robot.Auto");
        } catch (ClassNotFoundException e) {
            // Shouldn't happen -- using constant strings for class paths
            throw new RuntimeException("Exception while loading classes.");
        }
    }

    private UsbCamera camera;


    //----------Inherited initialization functions----------//

    public void robotInit() {
        LOGGER.info("Initializing robot...");
        load_classes();
        register_comm();
        LOGGER.info("Starting camera capture.");
        try {
            camera = CameraServer.getInstance().startAutomaticCapture();
            camera.setResolution(RobotMap.Camera.resolution_w, RobotMap.Camera.resolution_h);
            camera.setFPS(RobotMap.Camera.fps);
            camera.setBrightness((int) (100 * RobotMap.Camera.brightness));
        } catch (Throwable e) {
            LOGGER.severe("Camera init failed.");
        }
        LOGGER.info("Setting keymap.");
        Control.get_instance().set_keymap(DefaultGamepadBindings.get_instance());
        LOGGER.info("Initialization done.");
    }


    public void disabledInit() {
        LOGGER.info("ROBOT IN DISABLED");
        Dispatcher.get_instance().cancel_all();
        Drive.get_instance().set_enabled(false);
        ArmRotate.get_instance().set_enabled(false);
        ArmEndE.get_instance().set_enabled(false);
    }


    public void autonomousInit() {
        LOGGER.info("ROBOT IN AUTONOMOUS");
        /* TODO: TEMP -- AUTO DISABLED
        if (Auto.get_instance().resolve_auto()) {
            LOGGER.info("Resolving autonomous...Success.");
        } else {
            LOGGER.severe("Resolving autonomous...Failed.");
        }
        */
        // Dispatcher.get_instance().add_job(new JStraight(3.3, 0.75, 0.25, 0.0, 0.4, 0.6, false, false));
        // Dispatcher.get_instance().add_job(new JTurn(180, 0.5, 0.25, 0.0, 0.4, 0.6, false, false));
        // Dispatcher.get_instance().add_job(new JMoveArm(30));
        // Dispatcher.get_instance().add_job(new JHoldArm());
        // Arm.get_instance().go_to_base_position();
        Drive.get_instance().set_enabled(true);
        ArmRotate.get_instance().set_enabled(true);
        ArmEndE.get_instance().set_enabled(true);
    }


    public void teleopInit() {
        LOGGER.info("ROBOT IN TELEOP");
        Drive.get_instance().set_enabled(true);
        ArmRotate.get_instance().set_enabled(true);
        ArmEndE.get_instance().set_enabled(true);
    }


    public void testInit() {
        LOGGER.info("ROBOT IN TEST");
        Drive.get_instance().set_enabled(true);
        ArmRotate.get_instance().set_enabled(true);
        ArmEndE.get_instance().set_enabled(true);

        Dispatcher.get_instance().add_job(new JRunnable(
                () -> System.out.println("Test job running for 5 seconds. Blocking Arm and Drive."),
                () -> {},
                () -> System.out.println("Done blocking Arm and Drive."),
                5000, Drive.get_instance(), ArmRotate.get_instance(), ArmEndE.get_instance()
        ));
        Dispatcher.get_instance().add_job(new JRunnable(
                () -> System.out.println("Test job running for 4 seconds. Blocking Arm."),
                () -> {},
                () -> System.out.println("Done blocking Arm."),
                4000, ArmRotate.get_instance(), ArmEndE.get_instance()
        ));
        Dispatcher.get_instance().add_job(new JRunnable(
                () -> System.out.println("Test job running for 2 seconds. Blocking Drive."),
                () -> {},
                () -> System.out.println("Done blocking Drive."),
                2000, Drive.get_instance()
        ));

        DebugUtil.assert_(CalcUtil.in_threshold(5, 6, 2), "CalcUtil.in_threshold");
        DebugUtil.assert_(!CalcUtil.in_threshold(0.8, 0.5, 0.1), "CalcUtil.in_threshold");
        DebugUtil.assert_(CalcUtil.apply_bounds(1.32, 0, 1) == 1, "CalcUtil.apply_bounds");
        DebugUtil.assert_(CalcUtil.apply_bounds(-0.13, 0, 1) == 0, "CalcUtil.apply_bounds");
        DebugUtil.assert_(CalcUtil.apply_bounds(0.13, 0, 1) == 0.13, "CalcUtil.apply_bounds");

        Acceleration accel = new Acceleration(0.85, 0.25, 0.1, 0.4, 0.8, false, false, false);
        System.out.println("Running acceleration algorithm with 1000 steps with following params: maxout=0.85, transin=0.25, transout=0.1, stopaccel=0.4, startdecel=0.8, constaccel, constdecel, not reversed");
        for (int i = 0; i < 1; i += 0.001) {
            System.out.print(accel.get(i)+" ");
        }
        System.out.println();

        accel = new Acceleration(0.99, 0.17, 0.22, 0.3, 0.9, true, true, true);
        System.out.println("Running acceleration algorithm with 1000 steps with following params: maxout=0.99, transin=0.17, transout=0.22, stopaccel=0.3, startdecel=0.9, linaccel, lindecel, reversed");
        for (int i = 0; i < 1; i += 0.001) {
            System.out.print(accel.get(i)+" ");
        }
        System.out.println();

        System.out.println("RUNNING TELEOP PERIODIC");
    }


    //----------Inherited periodic functions----------//

    public void robotPeriodic() {
        Dispatcher.get_instance().run();
    }


    public void disabledPeriodic() {}


    public void autonomousPeriodic() {
        // TODO: TEMP -- AUTO RUN DISABLED
        // Auto.get_instance().run_auto();
    }


    public void teleopPeriodic() {
        Drive.get_instance().poll_controls();
        ArmEndE.get_instance().poll_controls();
        ArmRotate.get_instance().poll_controls();
    }


    public void testPeriodic() {
        teleopPeriodic();
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = {
            "O_CAMERA_RES_W_MS",
            "O_CAMERA_RES_H_MS",
            "O_CAMERA_FPS_MS",
            "O_CAMERA_BRIGHTNESS_MS"
    })
    public void transmitter_static(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_CAMERA_RES_W_MS":
                entry.setDouble(RobotMap.Camera.resolution_w);
                break;
            case "O_CAMERA_RES_H_MS":
                entry.setDouble(RobotMap.Camera.resolution_h);
                break;
            case "O_CAMERA_FPS_MS":
                entry.setDouble(RobotMap.Camera.fps);
                break;
            case "O_CAMERA_BRIGHTNESS_MS":
                entry.setDouble(RobotMap.Camera.brightness);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Receiver({
            "I_CAMERA_RES_W",
            "I_CAMERA_RES_H",
            "I_CAMERA_FPS",
            "I_CAMERA_BRIGHTNESS"
    })
    public void receiver(EntryNotification event) {
        switch (event.name) {
            case "I_CAMERA_RES_W":
                if (event.value.isDouble()) {
                    RobotMap.Camera.resolution_w = (int) CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 1280.0);
                    camera.setResolution(RobotMap.Camera.resolution_w, RobotMap.Camera.resolution_h);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_CAMERA_RES_H":
                if (event.value.isDouble()) {
                    RobotMap.Camera.resolution_h = (int) CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 720.0);
                    camera.setResolution(RobotMap.Camera.resolution_w, RobotMap.Camera.resolution_h);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_CAMERA_FPS":
                if (event.value.isDouble()) {
                    RobotMap.Camera.fps = (int) CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 30.0);
                    camera.setFPS(RobotMap.Camera.fps);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_CAMERA_BRIGHTNESS":
                if (event.value.isDouble()) {
                    RobotMap.Camera.brightness = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 1.0);
                    camera.setBrightness((int)(100*RobotMap.Camera.brightness));
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
        }
    }
}