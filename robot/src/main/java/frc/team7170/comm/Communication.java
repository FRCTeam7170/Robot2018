package frc.team7170.comm;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableEntry;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.util.TimedTask;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Logger;


/**
 * Handles all Network Tables stuff.
 */
public class Communication extends Module {

    private final static Logger LOGGER = Logger.getLogger(Communication.class.getName());

    private static Communication instance = new Communication();  // Singleton
    public static Communication get_instance() {
        return instance;
    }
    private Communication() {
        Dispatcher.get_instance().register_module(this);
    }

    private NetworkTableInstance nt_inst;
    private HashMap<String, Runnable> transmitters = new HashMap<>();

    /**
     * Enum of all (sub)table paths for convenience.
     */
    public enum Tables {
        OUT ("/out"),
        IN ("/in");

        private final String path;

        Tables(String path) {
            this.path = path;
        }

        public String get() {
            return path;
        }
    }

    @Override
    protected void init() {
        LOGGER.info("Initializing communication system.");
        nt_inst = NetworkTableInstance.getDefault();
    }

    @Override
    protected void update() {
        for (Runnable r : transmitters.values()) {
            if (r != null) {
                r.run();
            }
        }
    }

    // TODO: Disable callbacks on disable?
    @Override
    protected void enabled() {}

    @Override
    protected void disabled() {}

    @Override
    public String toString() {
        return "Communication module.";
    }

    public void register_communicator(Communicator communicator) {
        for (Method meth : communicator.getClass().getDeclaredMethods()) {
            Transmitter transmitter = meth.getDeclaredAnnotation(Transmitter.class);
            Receiver receiver = meth.getDeclaredAnnotation(Receiver.class);
            if (transmitter != null && receiver != null) {
                throw new RuntimeException("Method in communicator declared as transmitter AND receiver.");
            }
            if (transmitter != null) {
                if (meth.getReturnType() != void.class ||
                        meth.getParameterCount() != 1 ||
                        meth.getParameterTypes()[0] != NetworkTableEntry.class) {
                    throw new RuntimeException("Transmitter method in communicator does not feature proper signature.");
                }
                if (transmitter.poll_rate() == TransmitFrequency.STATIC) {
                    for (String key : transmitter.value()) {
                        key = rectify_key(key, true);
                        if (transmitters.containsKey(key)) {
                            throw new RuntimeException("Multiple transmitters for same key registered.");
                        }
                        transmitters.put(key, null);  // Populate map to indicate that a transmitter with this key exists
                        try {
                            meth.invoke(communicator, nt_inst.getTable(Tables.OUT.get()).getEntry(key));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (transmitter.poll_rate() == TransmitFrequency.VOLATILE) {
                    for (String key : transmitter.value()) {
                        final String k = rectify_key(key, true);  // Make key final for use in lambda
                        if (transmitters.containsKey(key)) {
                            throw new RuntimeException("Multiple transmitters for same key registered.");
                        }
                        transmitters.put(key, () -> {
                            try {
                                meth.invoke(communicator, nt_inst.getTable(Tables.OUT.get()).getEntry(k));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } else {  // One of specific delays in milliseconds
                    for (String key : transmitter.value()) {
                        final String k = rectify_key(key, true);  // Make key final for use in lambda
                        if (transmitters.containsKey(key)) {
                            throw new RuntimeException("Multiple transmitters for same key registered.");
                        }
                        transmitters.put(key, new TimedTask(() -> {
                            try {
                                meth.invoke(communicator, nt_inst.getTable(Tables.OUT.get()).getEntry(k));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }, transmitter.poll_rate_ms() == -1 ? transmitter.poll_rate().freq : transmitter.poll_rate_ms()));
                    }
                }
            }
            if (receiver != null) {
                if (meth.getReturnType() != void.class ||
                        meth.getParameterCount() != 1 ||
                        meth.getParameterTypes()[0] != EntryNotification.class) {
                    throw new RuntimeException("Receiver method in communicator does not feature proper signature.");
                }
                for (String key : receiver.value()) {
                    key = rectify_key(key, false);
                    nt_inst.getTable(Tables.IN.get()).getEntry(key).addListener((event) -> {
                        try {
                            meth.invoke(communicator, event);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, EntryListenerFlags.kUpdate);
                }
            }
        }
    }

    private String rectify_key(String key, boolean output) {
        if (output) {
            if (!key.startsWith("O_")) {
                key = "O_".concat(key);
            }
            if (!key.endsWith("_M") || !key.endsWith("_S")) {
                key = key.concat("_S");  // Keys default to being static
            }
        } else if (!key.startsWith("I_")) {
            key = "I_".concat(key);
        }
        return key;
    }
}
