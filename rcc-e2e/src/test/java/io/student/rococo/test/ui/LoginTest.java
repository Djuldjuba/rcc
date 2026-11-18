package io.student.rococo.test.ui;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.student.rococo.page.MainPage;

public class LoginTest {

    private static final Config CFG = Config.getInstance();

    @Test
    @User
    @DisplayName("Проверка успешного логина")
    void login(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .checkThatOpenMainPage();
    }

    @Test
    @DisplayName("Проверка успешной регистрации")
    void successfulRegistered() {
        String username = RandomDataUtils.randomUserName();
        String password = "12345";
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .clickRegistrationButton()
                .setUsername(username)
                .setPassword(password)
                .setConfirmPassword(password)
                .clickRegisterUser()
                .checkWelcomeTitle()
                .clickRegisterBtn()
                .clickLoginButton()
                .login(username, password)
                .checkThatOpenMainPage();
    }

    @Test
    @User
    @DisplayName("Проверка неуспешного логина")
    void unsuccessfulLogin(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .unsuccessLogin(testData.user().username(), testData.password() + "123")
                .checkUnsuccessLogin();
    }

    @Test
    @DisplayName("Проверка ошибки регистрации если пароли не совпадают")
    void unsuccessfulRegisteredIfPasswordsNotMatched() {
        String username = RandomDataUtils.randomUserName();
        String password = "12345";
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .clickRegistrationButton()
                .setUsername(username)
                .setPassword(password)
                .setConfirmPassword(password + "1")
                .clickRegisterUser()
                .checkErrorPassword();
    }

    @Test
    @User
    @DisplayName("Проверка ошибки регистрации если пользователь с таким именем уже существует")
    void unsuccessfulRegisteredIfUsernameAlreadyExists(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .clickRegistrationButton()
                .setUsername(testData.user().username())
                .setPassword(testData.password())
                .setConfirmPassword(testData.password())
                .clickRegisterUser()
                .checkErrorUsername(testData.user().username());
    }

    @Test
    @User
    @DisplayName("Проверка логина при нажатии на кнопку Войти из формы регистрации")
    void loginFromRegisteredForm(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .clickRegistrationButton()
                .clickLoginBtn()
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .checkThatOpenMainPage();
    }

    @Test
    @User
    @DisplayName("Проверка кнопки скрытия пароля")
    void checkVisibilityPassword(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .inputPassword(testData.password())
                .checkPasswordBecameHidden()
                .clickVisibilityPasswordBtn()
                .checkPasswordBecameVisible()
                .clickVisibilityPasswordBtn()
                .checkPasswordBecameHidden();
    }
}
