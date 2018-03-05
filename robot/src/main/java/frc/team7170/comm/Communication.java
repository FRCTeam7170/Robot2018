package frc.team7170.comm;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableEntry;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
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
    private HashSet<Runnable> transmitters = new HashSet<>();
    private HashMap<String, HashSet<Runnable>> receivers = new HashMap<>();

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
    protected void update() {}

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
                if (meth.getReturnType() != NTPacket.class ||
                        meth.getParameterCount() != 1 ||
                        meth.getParameterTypes()[0] != String.class) {
                    throw new RuntimeException("Transmitter method in communicator does not feature proper signature.");
                }
                // TODO Prohibit transmitter with key already created
                if (transmitter.poll_rate() == TransmitFrequency.STATIC) {
                    for (String key : transmitter.value()) {
                        // TODO
                    }
                } else if (transmitter.poll_rate() == TransmitFrequency.VOLATILE) {
                    for (String key : transmitter.value()) {
                        transmitters.add(() -> {
                            try {
                                meth.invoke(communicator, "O_"+key);  // TODO?
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } else {
                    for (String key : transmitter.value()) {
                        // TODO
                    }
                }
            }
            if (receiver != null) {
                if (meth.getReturnType() != void.class ||
                        meth.getParameterCount() != 1 ||
                        meth.getParameterTypes()[0] != NTPacket.class) {
                    throw new RuntimeException("Receiver method in communicator does not feature proper signature.");
                }
                // TODO
            }
        }
    }
}
