package rcc.test;

import com.codeborne.selenide.Selenide;
import rcc.config.Config;
import rcc.jupiter.TestData;
import rcc.jupiter.annotation.Museum;
import rcc.jupiter.annotation.User;
import rcc.jupiter.extensions.BrowserExtension;
import rcc.jupiter.extensions.MuseumExtension;
import rcc.jupiter.extensions.TestDataExtension;
import rcc.jupiter.extensions.UserExtension;
import rcc.model.UserJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import rcc.page.MainPage;
import rcc.service.UsersDbClient;

import static org.junit.jupiter.api.Assertions.*;
import static rcc.jupiter.extensions.UserExtension.getDefaultPassword;

@ExtendWith(TestDataExtension.class)
@ExtendWith(BrowserExtension.class)
@ExtendWith(UserExtension.class)
@ExtendWith(MuseumExtension.class)
public class LoginTest {

    private static final Config CFG = Config.getInstance();

    @Test
    @User
    void addMuseumModalShouldBeAvailable(UserJson userJson) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(userJson.username(), getDefaultPassword())
                .checkThatUserIsAuthorized()
                .clickMuseums()
                .clickAddMuseum()
                .shouldVisibleMuseumModal()
                .closeModal();
    }
}
