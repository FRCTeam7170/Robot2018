package frc.team7170.subsystems.nav;

import frc.team7170.subsystems.Drive;


abstract class Maneuver {

    boolean running = false;

    abstract void run();

    abstract void update();

    abstract void correct();

    abstract boolean finished();

    void set_arcade(double x, double y) {
        Drive.set_arcade(x, y, false);
    }
}
