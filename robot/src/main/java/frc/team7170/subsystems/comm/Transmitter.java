package frc.team7170.subsystems.comm;


public interface Transmitter {

    default void register() {
        Communication.register_transmitter(this);
    }

    
}
