package rcc.jupiter.annotation;

import org.junit.jupiter.api.extension.ExtendWith;
import rcc.jupiter.extensions.MuseumExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(MuseumExtension.class)
public @interface Museum {
    String title() default "";
    String description() default "";
    String city() default "";
    String countryName() default "";
    String photo() default "";
}
