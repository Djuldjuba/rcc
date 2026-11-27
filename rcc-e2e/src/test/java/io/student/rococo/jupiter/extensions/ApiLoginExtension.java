package io.student.rococo.jupiter.extensions;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Step;
import io.student.rococo.api.core.ThreadSafeCookieStore;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.annotation.ApiLogin;
import io.student.rococo.jupiter.annotation.Token;
import io.student.rococo.model.UserJson;
import io.student.rococo.service.api.AuthApiClient;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.openqa.selenium.Cookie;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ApiLoginExtension implements BeforeEachCallback, ParameterResolver {

    private static final Config CFG = Config.getInstance();
    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(ApiLoginExtension.class);

    private final AuthApiClient authApiClient = new AuthApiClient();
    private final boolean setupBrowser;

    public ApiLoginExtension() {
        this.setupBrowser = true;
    }

    private ApiLoginExtension(boolean setupBrowser) {
        this.setupBrowser = setupBrowser;
    }

    public static ApiLoginExtension restApiLoginExtension() {
        return new ApiLoginExtension(false);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), ApiLogin.class)
                .ifPresent(apiLogin -> {
                    final String username;
                    final String password;
                    final UserJson userToLogin;

                    Optional<UserJson> userFromUserExtension = UserExtension.createdUser();

                    if (!apiLogin.username().isEmpty()) {
                        username = apiLogin.username();
                        password = apiLogin.password().isEmpty()
                                ? UserExtension.DEFAULT_PASSWORD
                                : apiLogin.password();
                        userToLogin = new UserJson(null, username, null, null, null);
                        UserExtension.setUser(userToLogin);
                        context.getStore(UserExtension.NAMESPACE)
                                .put(context.getUniqueId() + "_password", password);

                    } else if (userFromUserExtension.isPresent()) {
                        userToLogin = userFromUserExtension.get();
                        username = userToLogin.username();
                        String pwdFromStore = context.getStore(UserExtension.NAMESPACE)
                                .get(context.getUniqueId() + "_password", String.class);
                        password = pwdFromStore != null ? pwdFromStore : UserExtension.DEFAULT_PASSWORD;

                    } else {
                        throw new IllegalStateException(
                                "No user found. Use @User or specify username/password in @ApiLogin");
                    }

                    final String token = authApiClient.login(username, password);
                    setToken(token);

                    if (setupBrowser) {
                        setupBrowserSession(token);
                    }
                });
    }

    @Step("Авторизоваться в браузере под пользователем")
    private void setupBrowserSession(String token) {
        Selenide.open(CFG.frontUrl());

        Selenide.localStorage().setItem("id_token", token);
        Selenide.localStorage().setItem("rococo-theme", "dark");

        String jsessionId = ThreadSafeCookieStore.INSTANCE.cookieValue("JSESSIONID");
        if (jsessionId != null) {
            WebDriverRunner.getWebDriver().manage().addCookie(
                    new Cookie("JSESSIONID", jsessionId)
            );
        }

        Selenide.refresh();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().isAssignableFrom(String.class)
                && AnnotationSupport.isAnnotated(parameterContext.getParameter(), Token.class);
    }

    @Override
    public String resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return getToken();
    }

    public static void setToken(String token) {
        try {
            ExtensionContext context = TestMethodContextExtension.context();
            context.getStore(NAMESPACE).put("token", token);
        } catch (Exception e) {
            System.setProperty("rococo.test.token", token);
        }
    }

    public static String getToken() {
        try {
            ExtensionContext context = TestMethodContextExtension.context();
            return context.getStore(NAMESPACE).get("token", String.class);
        } catch (Exception e) {
            return System.getProperty("rococo.test.token");
        }
    }

    public static void setCode(String code) {
        try {
            ExtensionContext context = TestMethodContextExtension.context();
            context.getStore(NAMESPACE).put("code", code);
        } catch (Exception e) {
            System.setProperty("rococo.test.code", code);
        }
    }

    public static String getCode() {
        try {
            ExtensionContext context = TestMethodContextExtension.context();
            return context.getStore(NAMESPACE).get("code", String.class);
        } catch (Exception e) {
            return System.getProperty("rococo.test.code");
        }
    }
}