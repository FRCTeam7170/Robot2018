package frc.team7170.robot;

import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.DriverStation;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.Receiver;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.JRunnable;
import frc.team7170.jobs.Job;
import frc.team7170.subsystems.arm.*;
import frc.team7170.subsystems.drive.Drive;
import frc.team7170.subsystems.drive.JStraight;
import frc.team7170.subsystems.drive.JTurn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;


public class Auto implements Communicator {

    private final static Logger LOGGER = Logger.getLogger(Auto.class.getName());

    private static Auto instance = new Auto();  // Singleton
    public static Auto get_instance() {
        return instance;
    }
    private Auto() {
        LOGGER.info("Initializing autonomous system.");
        /*
        // Drive to switch
        switch_left.add(new JStraight(3.3, 0.75, 0.25, 0.0, 0.4, 0.6, false, false));
        // Turn to face swuitch from side
        switch_left.add(new JTurn(90, 0.50, 0.25, 0.0, 0.4, 0.6, false, false));
        switch_left.add(new JMoveArm(45));
        // Hold arm and shoot cube
        HashSet<Job> meshed_jobs = new HashSet<>();
        meshed_jobs.add(new JHoldArm(1000));
        meshed_jobs.add(new JRunnable(() -> Arm.get_instance().endE_push(), () -> {}, () -> Arm.get_instance().endE_kill(), 1000, Arm.get_instance(), Drive.get_instance()));
        switch_left.add(Job.mesh(meshed_jobs, true));
        // Arm should fall back into position after 1 second


        // Drive to switch
        switch_right.add(new JStraight(3.3, 0.75, 0.25, 0.0, 0.4, 0.6, false, false));
        // Turn to face swuitch from side
        switch_right.add(new JTurn(-90, 0.50, 0.25, 0.0, 0.4, 0.6, false, false));
        switch_right.add(new JMoveArm(45));
        // Hold arm and shoot cube
        meshed_jobs.clear();
        meshed_jobs.add(new JHoldArm(1000));
        meshed_jobs.add(new JRunnable(() -> Arm.get_instance().endE_push(), () -> {}, () -> Arm.get_instance().endE_kill(), 1000, Arm.get_instance(), Drive.get_instance()));
        switch_right.add(Job.mesh(meshed_jobs, true));
        // Arm should fall back into position after 1 second


        // Drive to switch
        switch_mid_1.add(new JStraight(3.0, 0.75, 0.25, 0.0, 0.4, 0.6, false, false));
        switch_mid_1.add(new JMoveArm(45));
        // Hold arm and shoot cube
        meshed_jobs.clear();
        meshed_jobs.add(new JHoldArm(1000));
        meshed_jobs.add(new JRunnable(() -> Arm.get_instance().endE_push(), () -> {}, () -> Arm.get_instance().endE_kill(), 1000, Arm.get_instance(), Drive.get_instance()));
        switch_mid_1.add(Job.mesh(meshed_jobs, true));
        // Arm should fall back into position after 1 second
        */
    }

    private int delay_on_start = 0;  // Milliseconds

    private ArrayList<Job> switch_left = new ArrayList<>();
    private ArrayList<Job> switch_mid_1 = new ArrayList<>();
    private ArrayList<Job> switch_mid_2 = new ArrayList<>();
    private ArrayList<Job> switch_right = new ArrayList<>();

    private boolean resolved = false;

    public boolean resolve_auto() {
        if (!DriverStation.getInstance().isFMSAttached()) {
            LOGGER.warning("FMS not attached. Obtaining test GSM from driver station.");
        }
        String gsm = DriverStation.getInstance().getGameSpecificMessage();
        if (gsm.length() != 3 || gsm.matches("[^LR]")) {
            LOGGER.severe("GSM string invalid ("+gsm+"). Aborting autonomous resolution.");
            return false;
        }
        int loc = DriverStation.getInstance().getLocation();
        if (loc == 0) {
            LOGGER.severe("Location number invalid. Aborting autonomous resolution.");
            return false;
        }
        ArrayList<Job> auto = new ArrayList<>();
        auto.add(new JRunnable(delay_on_start, Drive.get_instance(), ArmRotate.get_instance(), ArmEndE.get_instance()));
        // Arm.get_instance().go_to_base_position();  // This should allow driving while we move the arm.
        gsm = gsm.substring(0, 2);
        switch (loc) {
            case 1:
                switch (gsm) {
                    case "LL":
                    case "RL":
                        auto.addAll(switch_left);
                        break;
                    case "LR":
                        auto.addAll(switch_left);
                        break;
                    case "RR":
                        auto.addAll(switch_left);
                        break;
                }
                break;
            case 2:
                switch (gsm) {
                    case "LL":
                    case "LR":
                        auto.addAll(switch_left);
                        break;
                    case "RL":
                    case "RR":
                        auto.addAll(switch_left);
                        break;
                }
                break;
            case 3:
                switch (gsm) {
                    case "LR":
                    case "RR":
                        auto.addAll(switch_left);
                        break;
                    case "RL":
                        auto.addAll(switch_left);
                        break;
                    case "LL":
                        auto.addAll(switch_left);
                        break;
                }
                break;
        }
        resolved = true;
        auto.forEach((job) -> Dispatcher.get_instance().add_job(job));
        return true;
    }

    /**
     * If for some reason autonomous doesn't resolve when {@link Auto#resolve_auto()} is called in the
     * {@link Robot#autonomousInit()}, repeatedly check it here.
     */
    public void run_auto() {
        if (!resolved) {
            if (resolve_auto()) {
                LOGGER.severe("Autonomous not resolved in initialization. Attempting to resolve...Failed.");
                return;
            }
            LOGGER.info("Autonomous not resolved in initialization. Attempting to resolve...Success.");
        }
        // TODO: 2G threshold may not be good
        // Vector acceleration
        double decel = Math.sqrt(Math.pow(Drive.get_instance().get_accel_X(), 2) + Math.pow(Drive.get_instance().get_accel_Y(), 2));
        if (decel >= 19.6) {  // 2 g's of deceleration
            LOGGER.warning("2G Deceleration threshold reached from an assumed collision. Cancelling autonomous.");
            Dispatcher.get_instance().cancel_all();  // Fail safe in case robot forcefully crashes during auto
            ArmRotate.get_instance().set_enabled(false);
            ArmEndE.get_instance().set_enabled(false);
            Drive.get_instance().set_enabled(false);
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = {
            "O_AUTO_DELAY_MS"
    })
    public void transmitter_static(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_AUTO_DELAY_MS":
                entry.setDouble(delay_on_start);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Receiver({
            "I_AUTO_DELAY"
    })
    public void receiver(EntryNotification event) {
        switch (event.name) {
            case "I_AUTO_DELAY":
                if (event.value.isDouble()) {
                    delay_on_start = (int) event.value.getDouble();
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
        }
    }
}
