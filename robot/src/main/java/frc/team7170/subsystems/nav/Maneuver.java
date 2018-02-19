package frc.team7170.subsystems.nav;


public abstract class Maneuver {

    boolean running = false;

    abstract void run();

    abstract void update();

    abstract boolean finished();
}
