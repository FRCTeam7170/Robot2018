package frc.team7170.robot;

import java.util.logging.Logger;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.Receiver;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.control.Action;
import frc.team7170.control.Control;
import frc.team7170.control.HIDAxisAccessor;
import frc.team7170.control.HIDButtonAccessor;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.JRunnable;
import frc.team7170.jobs.Module;
import frc.team7170.subsystems.arm.Arm;
import frc.team7170.subsystems.drive.Acceleration;
import frc.team7170.subsystems.drive.Drive;
import frc.team7170.subsystems.drive.JStraight;
import frc.team7170.util.CalcUtil;
import frc.team7170.util.DebugUtil;


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
        camera = CameraServer.getInstance().startAutomaticCapture();
        camera.setResolution(RobotMap.Camera.resolution_w, RobotMap.Camera.resolution_h);
        camera.setFPS(RobotMap.Camera.fps);
        camera.setBrightness((int)(100*RobotMap.Camera.brightness));
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
        /* TODO: TEMP
        if (Auto.get_instance().resolve_auto()) {
            LOGGER.info("Resolving autonomous...Success.");
        } else {
            LOGGER.severe("Resolving autonomous...Failed.");
        }
        */
        Dispatcher.get_instance().add_job(new JStraight(3.05, 0.75, 0.25, 0.0, 0.4, 0.6, false, false));
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
        Drive.get_instance().set_enabled(true);
        Arm.get_instance().set_enabled(true);

        Dispatcher.get_instance().add_job(new JRunnable(
                () -> System.out.println("Test job running for 5 seconds. Blocking Arm and Drive."),
                () -> {},
                () -> System.out.println("Done blocking Arm and Drive."),
                5000, Drive.get_instance(), Arm.get_instance()
        ));
        Dispatcher.get_instance().add_job(new JRunnable(
                () -> System.out.println("Test job running for 4 seconds. Blocking Arm."),
                () -> {},
                () -> System.out.println("Done blocking Arm."),
                4000, Arm.get_instance()
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
            System.out.print(accel.get(i));
        }
        System.out.println();

        accel = new Acceleration(0.99, 0.17, 0.22, 0.3, 0.9, true, true, true);
        System.out.println("Running acceleration algorithm with 1000 steps with following params: maxout=0.99, transin=0.17, transout=0.22, stopaccel=0.3, startdecel=0.9, linaccel, lindecel, reversed");
        for (int i = 0; i < 1; i += 0.001) {
            System.out.print(accel.get(i));
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