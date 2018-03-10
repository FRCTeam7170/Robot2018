package frc.team7170.comm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Used to specify that a method transmits information over the Network Tables network. See {@link Communication} for
 * more details.
 *
 * @see Communication
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Transmitter {

    /**
     * List of {@link edu.wpi.first.networktables.NetworkTableEntry} keys.
     */
    String[] value();

    /**
     * The rate at which to poll this method for transmitting data.
     * @see TransmitFrequency
     */
    TransmitFrequency poll_rate() default TransmitFrequency.MODERATE;

    /**
     * Used to specify a specific delay in milliseconds if the five default ones provided in {@link TransmitFrequency}
     * are not satisfactory. Any number for this greater than zero will override {@link Transmitter#poll_rate()}.
     */
    int poll_rate_ms() default -1;
}
