package frc.team7170.comm;

import edu.wpi.first.networktables.*;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;
import frc.team7170.util.TimedTask;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
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
 *      an "R_..." prefix specifies a remote procedure call (RPC) entry;
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
 * Any methods of a subscribed module that are remote procedure call (RPC) methods must be annotated with
 * {@link RPCCaller}. This accepts a list of strings, each corresponding to a {@link NetworkTableEntry} key to be
 * associated with the method in question's RPC. This method must accept one {@link RpcAnswer} parameter and return
 * void, lest an exception will be thrown during robot initialization. This method will be called whenever the entry's
 * {@link NetworkTableEntry#callRpc(byte[])} method is called. Note this way of identifying a method as RPC with the
 * {@link RPCCaller} annotation is essentially for convenience as the same effect could easily be achieved with a
 * {@link Transmitter} with a {@link TransmitFrequency#STATIC} poll rate that manually registers a given method as an
 * RPC method via {@link NetworkTableInstance#createRpc(NetworkTableEntry, Consumer)}.
 *
 * As a side note, consider annotating each transmitter, receiver, or RPC method with {@link SuppressWarnings} and
 * "unused" because the method should not be called without reflection. That is, {@code @SuppressWarnings("unused")}.
 * Also, all transmitter, receiver, or RPC methods must be public in order for {@link Method#invoke(Object, Object...)}
 * to not fail.
 *
 * @see TransmitFrequency
 * @see Transmitter
 * @see Receiver
 * @see RPCCaller
 * @see Communicator
 */
public class Communication extends Module implements Communicator {

    private final static Logger LOGGER = Logger.getLogger(Communication.class.getName());

    private static Communication instance = new Communication();  // Singleton
    public static Communication get_instance() {
        return instance;
    }
    private Communication() {
        LOGGER.info("Initializing communication system.");

        // Setup listener for sender whitelist from dashboard
        nt_inst.getTable(Tables.IN.get()).getEntry(rectify_key(RobotMap.Communication.DB_to_send_key, 2)).addListener((event) -> {
            senders.clear();
            try {
                senders.addAll(Arrays.asList(event.value.getStringArray()));
            } catch (ClassCastException e) {
                LOGGER.severe("DB senders list entry updated but it is not a string array!");
                e.printStackTrace();
            }
        }, EntryListenerFlags.kUpdate);

        Dispatcher.get_instance().register_module(this);
        register_comm();
    }

    private NetworkTableInstance nt_inst = NetworkTableInstance.getDefault();
    /**
     * This maps each entry key to the {@link Runnable} transmitter that it corresponds to, or null if the transmitter's
     * poll rate is {@link TransmitFrequency#STATIC} or the method is a {@link RPCCaller}.
     */
    private HashMap<String, Runnable> transmitters = new HashMap<>();
    /**
     * This set contains the keys of all entries to send over to the dashboard and is updated by the dashboard whenever
     * a key is to be stopped being sent or when a new key is to start being sent. I.e this is a whitelist for which
     * keys to actually update and send to the dashboard.
     */
    private HashSet<String> senders = new HashSet<>();

    /**
     * Enum of all (sub)table paths for convenience ease of modification.
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
    protected void update() {
        for (String key : senders) {
            Runnable r = transmitters.get(key);
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
        return "Communication System";
    }

    /**
     * This method is can be called from {@link Communicator#register_comm()} (preferable) or manually to register
     * a module as containing {@link Transmitter}, {@link Receiver}, or {@link RPCCaller} method. This allows this
     * method to search the {@link Communicator} for these annotated methods using reflection and handle them
     * appropriately.
     * @param communicator The {@link Communicator} to register.
     */
    public void register_communicator(Communicator communicator) {
        // Loop through the communicator's methods
        for (Method meth : communicator.getClass().getDeclaredMethods()) {
            // Attempt to identify each type of the annotations
            Transmitter transmitter = meth.getDeclaredAnnotation(Transmitter.class);
            Receiver receiver = meth.getDeclaredAnnotation(Receiver.class);
            RPCCaller rpccaller = meth.getDeclaredAnnotation(RPCCaller.class);

            // State variable to store whether a method has been found to have a annotation on it already.
            // This is used to enforce that only one of the three annotations be used per method in a communicator
            boolean anno_used = false;

            if (transmitter != null) {  // If the method is annotated with Transmitter
                LOGGER.fine("Transmitter "+meth.getName()+" found in "+communicator.getClass().getSimpleName()+".");
                anno_used = true;
                // Check the signature of the method and throw an exception if it isn't correct
                if (meth.getReturnType() != void.class ||
                        meth.getParameterCount() != 1 ||
                        meth.getParameterTypes()[0] != NetworkTableEntry.class) {
                    throw new RuntimeException("Transmitter method in communicator does not feature proper signature.");
                }
                // Special cases for when the transmitter's poll rate is static or volatile
                if (transmitter.poll_rate() == TransmitFrequency.STATIC) {
                    for (String key : transmitter.value()) {
                        key = rectify_key(key, 1);
                        if (transmitters.containsKey(key)) {
                            // Throw an exception if a transmitter for this key has already been mapped
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
                            // Throw an exception if a transmitter for this key has already been mapped
                            throw new RuntimeException("Multiple transmitters/rpc caller for same key registered.");
                        }
                        // Map a Runnable in transmitters without any delay for a volatile poll rate
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
                            // Throw an exception if a transmitter for this key has already been mapped
                            throw new RuntimeException("Multiple transmitters/rpc caller for same key registered.");
                        }
                        // Map a Runnable in transmitters with a delay for a non-volatile poll rate
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
                LOGGER.fine("Receiver "+meth.getName()+" found in "+communicator.getClass().getSimpleName()+".");
                if (anno_used) {
                    // Throw an exception if transmitter already exists on this method
                    throw new RuntimeException("Method in communicator declared as transmitter AND/OR receiver AND/OR rpc caller.");
                }
                anno_used = true;
                // Check the signature of the method and throw an exception if it isn't correct
                if (meth.getReturnType() != void.class ||
                        meth.getParameterCount() != 1 ||
                        meth.getParameterTypes()[0] != EntryNotification.class) {
                    throw new RuntimeException("Receiver method in communicator does not feature proper signature.");
                }
                for (String key : receiver.value()) {
                    key = rectify_key(key, 2);
                    // Add a listener to the entry key which invokes the method whenever the entry is remotely updated
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
                LOGGER.fine("RPCCaller "+meth.getName()+" found in "+communicator.getClass().getSimpleName()+".");
                if (anno_used) {
                    // Throw an exception if a transmitter or receiver already exists on this method
                    throw new RuntimeException("Method in communicator declared as transmitter AND/OR receiver AND/OR rpc caller.");
                }
                // Check the signature of the method and throw an exception if it isn't correct
                if (meth.getReturnType() != void.class ||
                        meth.getParameterCount() != 1 ||
                        meth.getParameterTypes()[0] != RpcAnswer.class) {
                    throw new RuntimeException("RPCCaller method in communicator does not feature proper signature.");
                }
                for (String key : rpccaller.value()) {
                    key = rectify_key(key, 0);
                    if (transmitters.containsKey(key)) {
                        // Throw an error if a transmitter for this key has already been mapped
                        throw new RuntimeException("Multiple transmitters/rpc caller for same key registered.");
                    }
                    transmitters.put(key, null);  // Populate map to indicate that a transmitter with this key exists
                    // Create an RPC on the Network Tables instance for the entry key which invokes the method
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

    /**
     * Fix a key to follow the naming scheme described in this class's docstring.
     * @param key The key to fix.
     * @param type The type of the key. This specifies the prefix according to:
     *             0 = "R_..."
     *             1 = "O_..."
     *             2 (or any other integer) = "I_..."
     * @return The rectified key.
     */
    public static String rectify_key(String key, int type) {
        if (type == 0 && !key.startsWith("R_")) {
            key = "R_".concat(key);
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

    /**
     * Strips the given key of any prefixes or affixes using regex and returns the root only.
     * @param key The key to parse.
     * @return The key stripped of pre/affixes.
     */
    public static String get_key_root(String key) {
        return key.replaceFirst("^R_|^O_|^I_", "").replaceFirst("_M\\$|_S\\$", "");
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.SLOW, value = {
            "O_COMMUNICATION_ENABLED_S"
    })
    public void transmitter_slow(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_COMMUNICATION_ENABLED_S":
                entry.setBoolean(get_enabled());
                break;
        }
    }

    @SuppressWarnings("unused")
    @RPCCaller("R_COMMUNICATION_ENABLE")
    public void rpccaller_enable(RpcAnswer rpc) {
        if (rpc.params.getBytes()[0] == 1) {
            LOGGER.info("Communication enabled via RPC.");
            set_enabled(true);
            rpc.postResponse(new byte[] {1});  // Success
        } else if (rpc.params.getBytes()[0] == 0) {
            LOGGER.info("Communication disabled via RPC.");
            set_enabled(false);
            rpc.postResponse(new byte[] {1});  // Success
        }
        rpc.postResponse(new byte[] {0});  // Failure
    }
}
