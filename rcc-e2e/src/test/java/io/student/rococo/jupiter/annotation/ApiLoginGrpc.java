package io.student.rococo.jupiter.annotation;

import io.student.rococo.jupiter.extensions.ApiLoginGrpcExtension;
import io.student.rococo.jupiter.extensions.UserExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith({
        UserExtension.class,
        ApiLoginGrpcExtension.class
})
public @interface ApiLoginGrpc {
    String username() default "";
    String password() default "";
}