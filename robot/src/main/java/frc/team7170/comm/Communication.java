package frc.team7170.comm;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableEntry;

import java.util.logging.Logger;


/**
 * Handles all Network Tables stuff. See NTVar
 */
public class Communication {

    private final static Logger LOGGER = Logger.getLogger(Communication.class.getName());

    private static NetworkTableInstance nt_inst;

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

    public static void init() {
        LOGGER.info("Initializing communication system.");
        nt_inst = NetworkTableInstance.getDefault();
    }

    static NetworkTableEntry register_entry(String key, String table) {
        LOGGER.fine("Entry with key "+key+" created in table "+table+".");
        return nt_inst.getTable(table).getEntry(key);
    }

    public static void register_transmitter(Transmitter trans) {

    }
}
