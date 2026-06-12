package rcc.jupiter.annotation;

import rcc.jupiter.extensions.UserExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(UserExtension.class)
public @interface User {
    String username() default "";
    String password() default "pass";
}