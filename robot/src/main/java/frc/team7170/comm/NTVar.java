package frc.team7170.comm;

import java.util.function.Consumer;
import java.util.logging.Logger;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableValue;


/**
 * Convenience class for accessing variable tied to entry in network table.
 */
public class NTVar<T> {

    private final static Logger LOGGER = Logger.getLogger(NTVar.class.getName());

    private final NetworkTableEntry entry;
    private int listener_handle;
    private final boolean setable;

    /**
     * @param val Value to store in entry; must be one of valid types specified in NetworkTableType
     * @param key Key to store the value at in the given (sub)table
     * @param table (Sub)table to store key-value pair in.
     */
    public NTVar(T val, String key, String table) {
        this(val, key, table, true);
    }

    /**
     * @param val Value to store in entry; must be one of valid types specified in NetworkTableType
     * @param key Key to store the value at in the given (sub)table
     * @param table (Sub)table to store key-value pair in.
     * @param setable Whether or not the value of this var is editable.
     */
    public NTVar(T val, String key, String table, boolean setable) {
        entry = Communication.register_entry(key, table);
        entry.setValue(val);
        this.setable = setable;
    }

    @SuppressWarnings("unchecked")
    public T get() {
        try {
            return (T) entry.getValue().getValue();
        } catch (ClassCastException e) {
            // This should never happen unless the type is changed outside of this interface because
            // NetworkTableEntry.setValue() throws an exception if the initial value type is illegal
            // and set() only allows for setting to the predefined type anyway.
            LOGGER.severe("Cast to specified NTVar generic type failed.");
            e.printStackTrace();
            return null;
        }
    }

    public void set(T val) {
        if (setable) {
            entry.setValue(val);
        }
    }

    /**
     * Set a listener function to be called whenever the entry is updated.
     * Does NOT call the given function with the EntryNotification object, simply passes the new value instead.
     * @param func Must return void and accept one NetworkTableValue object (the new value in the entry)
     */
    public void set_listener(Consumer<NetworkTableValue> func) {
        listener_handle = entry.addListener((entry_notification) -> func.accept(entry_notification.value), EntryListenerFlags.kUpdate);
    }

    public void remove_listener() {
        entry.removeListener(listener_handle);
    }
}
