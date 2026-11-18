package io.student.rococo.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class RegisterPage {

    private final SelenideElement usernameInput = $(".form #username");
    private final SelenideElement passwordInput = $(".form #password");
    private final SelenideElement submitPasswordBtn = $(".form #passwordSubmit");
    private final SelenideElement registerBtn = $(".form__submit");
    private final SelenideElement usernameErrorText = $(".form__error.error__username");
    private final SelenideElement passwordErrorText = $(".form__error.error__password");
    private final SelenideElement welcomeTitle = $("p.form__subheader");
    private final SelenideElement loginBtn = $("a.form__link");

    @Step("Ввести имя пользователя: '{username}'")
    public RegisterPage setUsername(String username) {
        usernameInput.setValue(username);
        return this;
    }

    @Step("Ввести пароль")
    public RegisterPage setPassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Step("Ввести пароль повторно в поле 'Повторите пароль'")
    public RegisterPage setConfirmPassword(String password) {
        submitPasswordBtn.setValue(password);
        return this;
    }

    @Step("Нажать на кнопку 'Зарегистрироваться'")
    public RegisterPage clickRegisterUser() {
        registerBtn.click();
        return this;
    }

    @Step("Проверить отображение надписи Добро пожаловать в Rococo")
    public RegisterPage checkWelcomeTitle() {
        welcomeTitle.shouldHave(text("Добро пожаловать в Rococo"));
        return this;
    }

    @Step("Нажать на кнопку Зарегистрироваться")
    public MainPage clickRegisterBtn() {
        registerBtn.click();
        return new MainPage();
    }

    @Step("Нажать на кнопку Войти")
    public MainPage clickLoginBtn() {
        loginBtn.click();
        return new MainPage();
    }

    @Step("Проверить ошибку контроля под полем 'Имя пользователя'")
    public RegisterPage checkErrorUsername(String username) {
        usernameErrorText.shouldHave(text("Username `" + username + "` already exists"));
        return this;
    }

    @Step("Проверить ошибку контроля под полем 'Пароль'")
    public RegisterPage checkErrorPassword() {
        passwordErrorText.shouldHave(text("Passwords should be equal"));
        return this;
    }
}
