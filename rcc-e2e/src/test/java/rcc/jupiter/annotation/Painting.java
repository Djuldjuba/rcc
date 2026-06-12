package rcc.jupiter.annotation;

import org.junit.jupiter.api.extension.ExtendWith;
import rcc.jupiter.extensions.PaintingExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(PaintingExtension.class)
public @interface Painting {
    String title() default "";
    String description() default "";
    String content() default "";
}
