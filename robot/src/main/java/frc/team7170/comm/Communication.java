package frc.team7170.comm;

import edu.wpi.first.networktables.*;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.util.TimedTask;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Logger;


/**
 * Handles all Network Tables stuff, namely communication with the dashboard. Modules that wish to transmit or receive
 * Network Tables data must implement {@link Communicator} and then call {@link Communicator#register_comm()} to
 * subscribe. This class uses reflection during robot initialization to identify each method of a subscribed module
 * that participates in communication (each communicating method must be annotated, as explained below).
 *
 * Any methods of a subscribed module that transmit data must be annotated with {@link Transmitter}. This accepts a list
 * of strings ({@link Transmitter#value()}) which each correspond to a {@link NetworkTableEntry} key that the module in
 * question is responsible for transmitting. Only one module may be assigned to transmit data for a specific key, but
 * each method may be the transmitter for more than one key. The transmitter annotation also defines the
 * {@link Transmitter#poll_rate()} and {@link Transmitter#poll_rate_ms()} fields, which specify the rate at which the
 * communication main loop ({@link Communication#update()}) should poll each transmitting method for new data to send
 * over the network. The {@link Transmitter#poll_rate()} field accepts a type from the {@link TransmitFrequency} enum,
 * which specifies various update speeds, three of which are simply delays in milliseconds, but the two others are
 * particularly important: {@link TransmitFrequency#VOLATILE} makes a given transmitter update every iteration of the
 * main loop and {@link TransmitFrequency#STATIC} makes a given transmitter update only once (on robot initialization).
 * The other field, {@link Transmitter#poll_rate_ms()}, allows the user to specify a certain integer delay in
 * milliseconds, which will always override {@link Transmitter#poll_rate()} if it is specified to be some integer
 * greater than 0. It is recommended to use the slowest appropriate speed to minimize bandwidth usage. Each transmitter
 * must accept one {@link NetworkTableEntry} parameter and return void. Failure to conform to this signature will result
 * in an exception during robot initialization. During each update cycle, the transmitting method is expected to mutate
 * the passed entry appropriately. If the transmitting method in question mutates more than one entry, then it is
 * recommended that {@link NetworkTableEntry#getName()} be used to determine the entry's key and then operate
 * accordingly. Note that a specific naming system for each entry's key exists:
 *      an "O_..." prefix specifies a one-way robot-to-dashboard entry;
 *      an "I_..." prefix specifies a one-way dashboard-to-robot entry;
 *      an "..._M" affix specifies that an entry be mutable by the dashboard (that is, the corresponding entry key
 *          with an "I_..." prefix may actually exist and hence a receiver may be assigned to listen for it);
 *      an "..._S" affix specifies that an entry be static (not mutable by the dashboard).
 * Any text in between these pre/affixes can be used as wished to further differentiate each key. Note that the user
 * does not have to manually apply these pre/affixes; one may simply provide a key with just the root to an annotation's
 * value and they will be automatically fixed via {@link Communication#rectify_key(String, int)}. Do be wary, however,
 * that the entries passed to each communicating method will be named in accordance with this scheme, and, hence,
 * identifying each entry must take this into account. Use {@link Communication#get_key_root(String)} when identifying
 * entries to get only the root of the key. Also, note that {@link Communication#rectify_key(String, int)} defaults to
 * appending a key with the static identifier, so mutable keys must be manually specified.
 *
 * Any methods of a subscribed module that receives data must be annotated with {@link Receiver} which accepts a list
 * of strings, each corresponding to a {@link NetworkTableEntry} key that the module in question wishes to receive
 * updates for. Many receivers may be specified for any given key, unlike {@link Transmitter}. Almost all the rules
 * described above for transmitters also apply to receivers, excluding the poll rates, because receivers are called
 * whenever a entry from the dashboard (prefixed with "I_...") is updated. Also, receivers are expected to accept one
 * {@link EntryNotification} parameter and return void, lest a {@link RuntimeException} will be thrown during robot
 * initialization. {@link EntryNotification#name} can be used to identify which entry an event refers to if the
 * receiver receives multiple entry keys.
 *
 * @see TransmitFrequency
 * @see Transmitter
 * @see Receiver
 * @see RPCCaller
 * @see Communicator
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
            if (r != null) {  // We map null to a key for static or rpc transmitters, hence we must null check
                r.run();
            }
        }
    }

    @Override
    protected void enabled() {
        // TODO: Re-enable callbacks ?
    }

    @Override
    protected void disabled() {
        // TODO: Disable callbacks ?
    }

    @Override
    public String toString() {
        return "Communication module.";
    }

    public void register_communicator(Communicator communicator) {
        for (Method meth : communicator.getClass().getDeclaredMethods()) {
            Transmitter transmitter = meth.getDeclaredAnnotation(Transmitter.class);
            Receiver receiver = meth.getDeclaredAnnotation(Receiver.class);
            RPCCaller rpccaller = meth.getDeclaredAnnotation(RPCCaller.class);
            boolean anno_used = false;

            if (transmitter != null) {
                anno_used = true;
                if (meth.getReturnType() != void.class ||
                        meth.getParameterCount() != 1 ||
                        meth.getParameterTypes()[0] != NetworkTableEntry.class) {
                    throw new RuntimeException("Transmitter method in communicator does not feature proper signature.");
                }
                if (transmitter.poll_rate() == TransmitFrequency.STATIC) {
                    for (String key : transmitter.value()) {
                        key = rectify_key(key, 1);
                        if (transmitters.containsKey(key)) {
                            throw new RuntimeException("Multiple transmitters/rpc caller for same key registered.");
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
                        final String k = rectify_key(key, 1);  // Make key final for use in lambda
                        if (transmitters.containsKey(k)) {
                            throw new RuntimeException("Multiple transmitters/rpc caller for same key registered.");
                        }
                        transmitters.put(k, () -> {
                            try {
                                meth.invoke(communicator, nt_inst.getTable(Tables.OUT.get()).getEntry(k));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } else {  // One of specific delays in milliseconds
                    for (String key : transmitter.value()) {
                        final String k = rectify_key(key, 1);  // Make key final for use in lambda
                        if (transmitters.containsKey(k)) {
                            throw new RuntimeException("Multiple transmitters/rpc caller for same key registered.");
                        }
                        transmitters.put(k, new TimedTask(() -> {
                            try {
                                meth.invoke(communicator, nt_inst.getTable(Tables.OUT.get()).getEntry(k));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }, transmitter.poll_rate_ms() > 0 ? transmitter.poll_rate().freq : transmitter.poll_rate_ms()));
                    }
                }
            }

            if (receiver != null) {
                if (anno_used) {
                    throw new RuntimeException("Method in communicator declared as transmitter AND/OR receiver AND/OR rpc caller.");
                }
                anno_used = true;
                if (meth.getReturnType() != void.class ||
                        meth.getParameterCount() != 1 ||
                        meth.getParameterTypes()[0] != EntryNotification.class) {
                    throw new RuntimeException("Receiver method in communicator does not feature proper signature.");
                }
                for (String key : receiver.value()) {
                    key = rectify_key(key, 2);
                    nt_inst.getTable(Tables.IN.get()).getEntry(key).addListener((event) -> {
                        try {
                            meth.invoke(communicator, event);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, EntryListenerFlags.kUpdate);
                }
            }

            if (rpccaller != null) {
                if (anno_used) {
                    throw new RuntimeException("Method in communicator declared as transmitter AND/OR receiver AND/OR rpc caller.");
                }
                if (meth.getReturnType() != void.class ||
                        meth.getParameterCount() != 1 ||
                        meth.getParameterTypes()[0] != RpcAnswer.class) {
                    throw new RuntimeException("RPCCaller method in communicator does not feature proper signature.");
                }
                for (String key : rpccaller.value()) {
                    key = rectify_key(key, 0);
                    if (transmitters.containsKey(key)) {
                        throw new RuntimeException("Multiple transmitters/rpc caller for same key registered.");
                    }
                    transmitters.put(key, null);  // Populate map to indicate that a transmitter with this key exists
                    nt_inst.createRpc(nt_inst.getTable(Tables.OUT.get()).getEntry(key), (rpca) -> {
                        try {
                            meth.invoke(communicator, rpca);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    public String rectify_key(String key, int type) {
        if (type == 0) {
            if (!key.startsWith("R_")) {
                key = "R_".concat(key);
            }
        } else if (type == 1) {
            if (!key.startsWith("O_")) {
                key = "O_".concat(key);
            }
            if (!key.endsWith("_M") || !key.endsWith("_S")) {
                key = key.concat("_S");  // Output keys default to being static
            }
        } else if (!key.startsWith("I_")) {
            key = "I_".concat(key);
        }
        return key;
    }

    public String get_key_root(String key) {
        return "TODO";
    }
}
