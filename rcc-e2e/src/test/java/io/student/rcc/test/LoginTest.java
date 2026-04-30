package io.student.rcc.test;

import com.codeborne.selenide.Selenide;
import io.student.rcc.config.Config;
import io.student.rcc.jupiter.annotation.User;
import io.student.rcc.jupiter.extensions.BrowserExtension;
import io.student.rcc.jupiter.extensions.UserExtension;
import io.student.rcc.model.UserJson;
import io.student.rcc.page.MainPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.student.rcc.jupiter.extensions.UserExtension.getDefaultPassword;
import static io.student.rcc.utils.RandomUserDataUtils.getRandomUserName;

@ExtendWith(BrowserExtension.class)
@ExtendWith(UserExtension.class)
public class LoginTest {

    private static final Config CFG = Config.getInstance();

    @Test
    @User
    void addPaintingModalShouldBeAvailable(UserJson userJson) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(userJson.username(), getDefaultPassword())
                .checkThatUserIsAuthorized()
                .clickPaintings()
                .clickAddPainting()
                .shouldVisiblePaintingModal()
                .closeModal();
    }

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

    @Test
    @User
    void addArtistModalShouldBeAvailable(UserJson userJson) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(userJson.username(), getDefaultPassword())
                .checkThatUserIsAuthorized()
                .clickArtists()
                .clickAddArtist()
                .shouldDisplayArtistModal()
                .closeModal();
    }

    @Test
    @User
    void shouldNotRegisterUserWithExistingUsername(UserJson userJson) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .clickRegistrationButton()
                .unsuccessRegistrationUser(userJson.username(), getDefaultPassword())
                .checkErrorUsername(userJson.username());
    }

    @Test
    @User(enabled = false)
    void shouldShowErrorIfPasswordAndConfirmPasswordAreNotEqual() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .clickRegistrationButton()
                .setUsername(getRandomUserName())
                .setPassword(getDefaultPassword())
                .setConfirmPassword(getDefaultPassword() + "1")
                .clickRegisterUser()
                .checkErrorPassword();
    }

    @Test
    @User(enabled = false)
    void shouldRegisterNewUser() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .clickRegistrationButton()
                .successRegistrationUser(getRandomUserName(), getDefaultPassword());
    }

    @Test
    @User
    void userShouldStayOnLoginPageAfterLoginWithBadCredentials(UserJson userJson) {

        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .unsuccessLogin(userJson.username(), getDefaultPassword() + "1")
                .clickSubmitButton()
                .checkUnsuccessLogin();
    }
}
