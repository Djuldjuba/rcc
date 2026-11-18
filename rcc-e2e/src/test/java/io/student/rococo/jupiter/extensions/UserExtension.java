package io.student.rococo.jupiter.extensions;

import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.model.UserJson;
import io.student.rococo.service.impl.UsersDbClient;
import io.student.rococo.utils.RandomDataUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class UserExtension implements BeforeEachCallback, ParameterResolver {

    private static final Logger LOG = LoggerFactory.getLogger(UserExtension.class);

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(UserExtension.class);

    private final UsersDbClient usersDbClient = new UsersDbClient();
    static final String DEFAULT_PASSWORD = "pass";

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(annotation -> {
            String username = annotation.username().isEmpty()
                    ? RandomDataUtils.randomUserName()
                    : annotation.username();
            String password = annotation.password().isEmpty()
                    ? DEFAULT_PASSWORD
                    : annotation.password();

            UserJson user = usersDbClient.findByUsername(username)
                    .orElseGet(() -> usersDbClient.createUser(username, password));

            context.getStore(NAMESPACE).put(context.getUniqueId(), user);
            context.getStore(NAMESPACE).put(context.getUniqueId() + "_password", password);

            TestDataExtension.addUser(context, user, password);

            LOG.info("User '{}' created/setup for test: {}", username, context.getDisplayName());
        });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> paramType = parameterContext.getParameter().getType();
        return paramType.equals(UserJson.class) &&
                AnnotationSupport.isAnnotated(extensionContext.getRequiredTestMethod(), User.class);
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext,
                                             ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId(), UserJson.class);
    }

    public static Optional<UserJson> createdUser() {
        ExtensionContext context = TestMethodContextExtension.context();
        if (context == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                context.getStore(NAMESPACE).get(context.getUniqueId(), UserJson.class)
        );
    }

    public static void setUser(UserJson testUser) {
        ExtensionContext context = TestMethodContextExtension.context();
        context.getStore(NAMESPACE).put(context.getUniqueId(), testUser);
    }
}