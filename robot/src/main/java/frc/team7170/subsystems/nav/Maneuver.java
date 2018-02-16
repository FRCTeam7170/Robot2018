package frc.team7170.subsystems.nav;


abstract class Maneuver {

    boolean running = false;

    abstract void run();

    abstract void update();

    abstract boolean finished();
}
