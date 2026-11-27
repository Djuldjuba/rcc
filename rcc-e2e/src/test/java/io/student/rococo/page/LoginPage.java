package io.student.rococo.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.matchText;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage {

    private final SelenideElement usernameInput = $("[name='username']");
    private final SelenideElement passwordInput = $("[name='password']");
    private final SelenideElement submitBtn = $(".form__submit");
    private final SelenideElement registerBtn = $("a[href='/register']");
    private final SelenideElement errorLogin = $("[class*='login__error']");
    private final SelenideElement visibilityPasswordBtn = $(".form .form__password-button");

    @Step("Залогиниться под пользователем '{username}'")
    public MainPage login(String username, String password) {
        inputUsername(username);
        inputPassword(password);
        clickSubmitButton();
        return new MainPage();
    }

    @Step("Ввести имя пользователя")
    public LoginPage inputUsername(String username) {
        usernameInput.setValue(username);
        return this;
    }

    @Step("Ввести пароль")
    public LoginPage inputPassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Step("Нажать кнопку 'Войти'")
    public LoginPage clickSubmitButton() {
        submitBtn.click();
        return this;
    }

    @Step("Выполнить некоррекный логин под пользователем '{username}'")
    public LoginPage unsuccessLogin(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        clickSubmitButton();
        return this;
    }

    @Step("Проверка, что логин выполнен неуспешно")
    public LoginPage checkUnsuccessLogin() {
        errorLogin.shouldHave(matchText("(?i)(Неверные учетные данные пользователя|Bad credentials)"));
        return this;
    }

    @Step("Нажать на кнопку 'Зарегистрироваться'")
    public RegisterPage clickRegistrationButton() {
        registerBtn.click();
        return new RegisterPage();
    }

    @Step("Нажать на кнопку отображения пароля")
    public LoginPage clickVisibilityPasswordBtn() {
        visibilityPasswordBtn.click();
        return this;
    }

    @Step("Проверка, что пароль стал видимым")
    public LoginPage checkPasswordBecameVisible() {
        visibilityPasswordBtn.shouldHave(cssClass("form__password-button_active"));
        return this;
    }

    @Step("Проверка, что пароль снова скрыт")
    public LoginPage checkPasswordBecameHidden() {
        visibilityPasswordBtn.shouldNotHave(cssClass("form__password-button_active"));
        return this;
    }
}
