package frc.team7170.comm;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import frc.team7170.robot.RobotMap;
import java.util.logging.Logger;


/**
 * Sends miscellaneous information to the dashboard. Namely, robot dimensions, enabled/disabled, auto/teleop/test, if DS
 * is attached, if FMS is attached, game specific message, alliance colour, location on alliance (1/2/3), match time
 * estimate, and if the rio is browned out.
 * TODO: PDP and power usage information?
 */
public class MiscSender implements Communicator {

    private final static Logger LOGGER = Logger.getLogger(MiscSender.class.getName());

    private static MiscSender instance = new MiscSender();  // Singleton
    public static MiscSender get_instance() {
        return instance;
    }
    private MiscSender() {
        register_comm();
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = {
            "O_DIMENSION_WHEEL_WIDTH_NS",
            "O_DIMENSION_WHEEL_RADIUS_NS",
            "O_DIMENSION_WHEEL_TO_WHEEL_NS",
            "O_DIMENSION_WHEEL_SPACING_NS",
            "O_DIMENSION_WIDTH_NS",
            "O_DIMENSION_LENGTH_NS",
            "O_DIMENSION_HEIGHT_NS"
    })
    public void transmitter_static(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_DIMENSION_WHEEL_WIDTH_NS":
                entry.setDouble(RobotMap.RobotDims.wheel_width);
                break;
            case "O_DIMENSION_WHEEL_RADIUS_NS":
                entry.setDouble(RobotMap.RobotDims.wheel_radius);
                break;
            case "O_DIMENSION_WHEEL_TO_WHEEL_NS":
                entry.setDouble(RobotMap.RobotDims.wheel_to_wheel);
                break;
            case "O_DIMENSION_WHEEL_SPACING_NS":
                entry.setDouble(RobotMap.RobotDims.wheel_spacing);
                break;
            case "O_DIMENSION_WIDTH_NS":
                entry.setDouble(RobotMap.RobotDims.robot_width);
                break;
            case "O_DIMENSION_LENGTH_NS":
                entry.setDouble(RobotMap.RobotDims.robot_length);
                break;
            case "O_DIMENSION_HEIGHT_NS":
                entry.setDouble(RobotMap.RobotDims.robot_height);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.SLOW, value = {
            "O_ROBOT_ENABLED_NT",
            "O_ROBOT_MODE_NT",
            "O_DS_ATTACHED_NT",
            "O_FMS_ATTACHED_NT",
            "O_ALLIANCE_COLOUR_NT",
            "O_ALLIANCE_LOCATION_NT",
            "O_GAME_SPECIFIC_MESSAGE_NT"
    })
    public void transmitter_slow(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_ROBOT_ENABLED_NT":
                entry.setBoolean(DriverStation.getInstance().isEnabled());
                break;
            case "O_ROBOT_MODE_NT":
                if (DriverStation.getInstance().isAutonomous()) {
                    entry.setDouble(0.0);
                } else if (DriverStation.getInstance().isOperatorControl()) {
                    entry.setDouble(1.0);
                } else if (DriverStation.getInstance().isTest()) {
                    entry.setDouble(2.0);
                } else {
                    LOGGER.warning("No valid robot state determined.");
                }
                break;
            case "O_DS_ATTACHED_NT":
                entry.setBoolean(DriverStation.getInstance().isDSAttached());
                break;
            case "O_FMS_ATTACHED_NT":
                entry.setBoolean(DriverStation.getInstance().isFMSAttached());
                break;
            case "O_ALLIANCE_COLOUR_NT":
                switch (DriverStation.getInstance().getAlliance()) {
                    case Red:
                        entry.setDouble(1.0);
                        break;
                    case Blue:
                        entry.setDouble(2.0);
                        break;
                    case Invalid:
                        entry.setDouble(0.0);
                        break;
                }
                break;
            case "O_ALLIANCE_LOCATION_NT":
                entry.setDouble(DriverStation.getInstance().getLocation());
                break;
            case "O_GAME_SPECIFIC_MESSAGE_NT":
                entry.setString(DriverStation.getInstance().getGameSpecificMessage());
                break;
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.FAST, value = {
            "O_MATCH_TIME_NT",
            "O_BROWNED_OUT_NT"
    })
    public void transmitter_fast(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_MATCH_TIME_NT":
                entry.setDouble(DriverStation.getInstance().getMatchTime());
                break;
            case "O_BROWNED_OUT_NT":
                entry.setBoolean(RobotController.isBrownedOut());
                break;
        }
    }
}
