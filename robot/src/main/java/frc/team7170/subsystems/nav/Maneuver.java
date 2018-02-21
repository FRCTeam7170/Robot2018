package frc.team7170.subsystems.nav;


public abstract class Maneuver {

    boolean running = false;

    abstract void run();

    abstract void update();

    abstract boolean finished();

    /**
     * Combine many maneuvers into one maneuver.
     * WARNING: Do not combine maneuvers of conflicting types. For example, two turns. They will "fight"
     *     each other for completion.
     * @param mans array (varargs) of maneuvers
     * @return Meshed Maneuver object.
     */
    public static Maneuver mesh(Maneuver ...mans) {
        return new Maneuver() {
            @Override
            void run() {
                running = true;
                for (Maneuver man:mans) {
                    man.run();
                }
            }

            @Override
            void update() {
                for (Maneuver man:mans) {
                    man.update();
                }
            }

            @Override
            boolean finished() {
                for (Maneuver man:mans) {
                    if (!man.finished()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
}
