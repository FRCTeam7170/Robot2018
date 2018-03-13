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
import frc.team7170.subsystems.arm.Arm;
import frc.team7170.subsystems.drive.Drive;
import java.util.ArrayList;
import java.util.logging.Logger;


public class Auto implements Communicator {

    private final static Logger LOGGER = Logger.getLogger(Auto.class.getName());

    private static Auto instance = new Auto();  // Singleton
    public static Auto get_instance() {
        return instance;
    }
    private Auto() {
        LOGGER.info("Initializing communication system.");
    }

    private int delay_on_start = 0;  // Milliseconds

    // Straight to scale and start raising arm, turn 45ish right, shoot
    private ArrayList<Job> A1_1 = new ArrayList<>();
    // Straight to switch, raise arm and 90 right, shoot
    private ArrayList<Job> A1_2 = new ArrayList<>();
    // Straight to switch-scale gap, then 90 right, then straight to switch, then 45ish right and raise arm, shoot
    private ArrayList<Job> A1_3 = new ArrayList<>();
    // Turn 45ish left , straight to switch, turn 45ish right and raise arm, shoot
    private ArrayList<Job> A2_1 = new ArrayList<>();
    // Turn 45ish right, straight to switch, turn 45ish left and raise arm, shoot
    private ArrayList<Job> A2_2 = new ArrayList<>();
    // Straight to scale and start raising arm, turn 45ish left, shoot
    private ArrayList<Job> A3_1 = new ArrayList<>();
    // Straight to switch, raise arm and 90 left, shoot
    private ArrayList<Job> A3_2 = new ArrayList<>();
    // Straight to switch-scale gap, then 90 left, then straight to switch, then 45ish left and raise arm, shoot
    private ArrayList<Job> A3_3 = new ArrayList<>();

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
        auto.add(new JRunnable(delay_on_start, Drive.get_instance(), Arm.get_instance()));
        gsm = gsm.substring(0, 2);
        switch (loc) {
            case 1:
                switch (gsm) {
                    case "LL":
                    case "RL":
                        auto.addAll(A1_1);
                        break;
                    case "LR":
                        auto.addAll(A1_2);
                        break;
                    case "RR":
                        auto.addAll(A1_3);
                        break;
                }
                break;
            case 2:
                switch (gsm) {
                    case "LL":
                    case "LR":
                        auto.addAll(A2_1);
                        break;
                    case "RL":
                    case "RR":
                        auto.addAll(A2_2);
                        break;
                }
                break;
            case 3:
                switch (gsm) {
                    case "LR":
                    case "RR":
                        auto.addAll(A3_1);
                        break;
                    case "RL":
                        auto.addAll(A3_2);
                        break;
                    case "LL":
                        auto.addAll(A3_3);
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
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = {
            "O_AUTO_DELAY_M"
    })
    public void transmitter_static(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_AUTO_DELAY_M":
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
