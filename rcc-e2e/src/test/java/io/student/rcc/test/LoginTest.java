package io.student.rcc.test;

import com.codeborne.selenide.Selenide;
import io.student.rcc.config.Config;
import io.student.rcc.jupiter.annotation.User;
import io.student.rcc.jupiter.extensions.BrowserExtension;
import io.student.rcc.model.UserJson;
import io.student.rcc.page.MainPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.student.rcc.utils.RandomUserDataUtils.getRandomUserName;

@ExtendWith(BrowserExtension.class)
public class LoginTest {

    private static final Config CFG = Config.getInstance();
    private static final String PASSWORD = "pass";

    @Test
    @User
    void mainPageShouldBeDisplayedAfterSuccessLogin(UserJson user) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(user.username(), PASSWORD)
                .checkSuccessLogin()
                .clickPaintings()
                .clickAddPainting()
                .closeModal()
                .clickArtists()
                .clickAddArtists()
                .closeModal()
                .clickMuseums()
                .clickAddMuseum()
                .closeModal();
    }

    @Test
    @User
    void shouldNotRegisterUserWithExistingUsername(UserJson user) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .clickRegistrationButton()
                .unsuccessRegistrationUser(user.username(), PASSWORD)
                .checkErrorUsername(user.username());
    }

    @Test
    void shouldShowErrorIfPasswordAndConfirmPasswordAreNotEqual() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .clickRegistrationButton()
                .setUsername(getRandomUserName())
                .setPassword(PASSWORD)
                .setConfirmPassword(PASSWORD + "1")
                .clickRegisterUser()
                .checkErrorPassword();
    }

    @Test
    void shouldRegisterNewUser() {

        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .clickRegistrationButton()
                .successRegistrationUser(getRandomUserName(), PASSWORD);
    }

    @Test
    @User
    void userShouldStayOnLoginPageAfterLoginWithBadCredentials(UserJson user) {

        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .unsuccessLogin(user.username(), PASSWORD + "1");
    }
}
