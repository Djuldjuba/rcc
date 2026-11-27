package io.student.rococo.jupiter.annotation;

import io.student.rococo.jupiter.extensions.ArtistExtension;
import io.student.rococo.jupiter.extensions.MuseumExtension;
import io.student.rococo.jupiter.extensions.PaintingExtension;
import io.student.rococo.jupiter.extensions.TestDataExtension;
import io.student.rococo.jupiter.extensions.TestMethodContextExtension;
import io.student.rococo.jupiter.extensions.UserExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith({
        TestDataExtension.class,
        UserExtension.class,
        MuseumExtension.class,
        ArtistExtension.class,
        PaintingExtension.class,
        TestMethodContextExtension.class
})
public @interface User {
    String username() default "";
    String password() default "pass";

    Museum[] museums() default {};
    Artist[] artists() default {};
    Painting[] paintings() default {};
}
