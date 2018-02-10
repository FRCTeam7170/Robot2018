package frc.team7170.subsystems.comm;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableEntry;

import java.util.logging.Logger;


/**
 * Handles all Network Tables stuff. See NTVar
 */
public class Communication {

    private final static Logger LOGGER = Logger.getLogger(Communication.class.getName());

    /**
     * Enum of all (sub)table paths for convenience.
     */
    public enum tables {
        DASHBOARD ("/dashboard"),
        VISION ("/vision");

        private final String path;

        tables(String path) {
            this.path = path;
        }

        public String get() {
            return path;
        }
    }

    private static final NetworkTableInstance nt_inst = NetworkTableInstance.getDefault();

    static NetworkTableEntry register_entry(String key, String table) {
        LOGGER.info("Entry with key "+key+" created in table "+table+".");
        return nt_inst.getTable(table).getEntry(key);
    }
}
