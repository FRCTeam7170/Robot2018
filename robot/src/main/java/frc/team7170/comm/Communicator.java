package frc.team7170.comm;


public interface Communicator {
    default void register_comm() {
        Communication.get_instance().register_communicator(this);
    }
}
