package rcc.jupiter.extensions;

import rcc.jupiter.annotation.User;
import rcc.model.UserJson;
import rcc.service.UsersDbClient;

import rcc.utils.RandomDataUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

public class UserExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(UserExtension.class);

    private final UsersDbClient usersDbClient = new UsersDbClient();
    private static final String DEFAULT_PASSWORD = "pass";

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

            UserJson user = usersDbClient.createUser(username, password);

            context.getStore(NAMESPACE).put(context.getUniqueId(), user);
            context.getStore(NAMESPACE).put(context.getUniqueId() + "_password", password);

            TestDataExtension.addUser(context, user, password);
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

    public static String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }
}
