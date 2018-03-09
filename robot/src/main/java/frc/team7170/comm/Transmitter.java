package frc.team7170.comm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Transmitter {

    String[] value();  // end in "..._M" to make mutable

    TransmitFrequency poll_rate() default TransmitFrequency.MODERATE;

    int poll_rate_ms() default -1;
}
