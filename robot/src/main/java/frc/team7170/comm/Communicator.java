package frc.team7170.comm;


/**
 * Interface to indicate that a module contains transmitters, receivers, or RPC callers (i.e. methods annotated with
 * {@link Transmitter}, {@link Receiver}, and {@link RPCCaller}). Also contains the convenience default method
 * {@link Communicator#register_comm()} to register the communicator with the {@link Communication} module. It is
 * recommended that this method be called in the constructor of the module in question.
 */
public interface Communicator {
    default void register_comm() {
        // TODO: TEMP -- comm disabled
        //Communication.get_instance().register_communicator(this);
    }
}
