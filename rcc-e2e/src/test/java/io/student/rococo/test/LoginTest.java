package io.student.rococo.test;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.jupiter.extensions.BrowserExtension;
import io.student.rococo.jupiter.extensions.MuseumExtension;
import io.student.rococo.jupiter.extensions.TestDataExtension;
import io.student.rococo.jupiter.extensions.UserExtension;
import io.student.rococo.model.UserJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.student.rococo.page.MainPage;

import static io.student.rococo.jupiter.extensions.UserExtension.getDefaultPassword;

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
