package frc.team7170.subsystems.nav;


abstract class Maneuver {

    boolean running = false;

    abstract void run();

    void update() {
        correct();
        if (finished()) {
            Navigation.man_complete();
        }
    }

    abstract void correct();

    abstract boolean finished();
}
