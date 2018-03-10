package frc.team7170.comm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Used to specify that a method receives information over the Network Tables network. See {@link Communication} for
 * more details.
 *
 * @see Communication
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Receiver {

    /**
     * List of {@link edu.wpi.first.networktables.NetworkTableEntry} keys.
     */
    String[] value() default "";
}
