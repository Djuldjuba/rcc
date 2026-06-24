package io.student.rococo.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage {

    private final SelenideElement usernameInput = $("[name='username']");
    private final SelenideElement passwordInput = $("[name='password']");
    private final SelenideElement submitBtn = $(".form__submit");
    private final SelenideElement registerBtn = $("a[href='/register']");
    private final SelenideElement errorLogin = $("[class*='login__error']");

    @Step("Залогиниться под пользователем '{username}'")
    public MainPage login(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        submitBtn.click();
        return new MainPage();
    }

    @Step("Выполнить некоррекный логин под пользователем '{username}'")
    public LoginPage unsuccessLogin(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        return this;
    }

    @Step("Нажать на кнопку 'Войти'")
    public LoginPage clickSubmitButton() {
        submitBtn.click();
        return this;
    }

    @Step("Проверка, что логин выполнен неуспешно")
    public LoginPage checkUnsuccessLogin() {
        errorLogin.shouldHave(text("Неверные учетные данные пользователя"));
        return this;
    }

    @Step("Нажать на кнопку 'Зарегистрироваться'")
    public RegisterPage clickRegistrationButton() {
        registerBtn.click();
        return new RegisterPage();
    }
}
