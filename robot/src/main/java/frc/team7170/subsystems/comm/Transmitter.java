package frc.team7170.subsystems.comm;


public interface Transmitter {

    // For convenience
    default void register() {
        Communication.register_transmitter(this);
    }

    // send()

    // receive()
}
