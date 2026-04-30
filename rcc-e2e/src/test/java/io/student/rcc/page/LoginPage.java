package io.student.rcc.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage {

    private final SelenideElement usernameInput = $("[name='username']");
    private final SelenideElement passwordInput = $("[name='password']");
    private final SelenideElement submitBtn = $(".form__submit");
    private final SelenideElement registerBtn = $("a[href='/register']");
    private final SelenideElement errorLogin = $("[class*='login__error']");

    public MainPage login(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        submitBtn.click();
        return new MainPage();
    }

    public LoginPage unsuccessLogin(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        return this;
    }

    public LoginPage clickSubmitButton() {
        submitBtn.click();
        return this;
    }

    public LoginPage checkUnsuccessLogin() {
        errorLogin.shouldHave(text("Неверные учетные данные пользователя"));
        return this;
    }

    public RegisterPage clickRegistrationButton() {
        registerBtn.click();
        return new RegisterPage();
    }
}
