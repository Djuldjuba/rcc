package io.student.rcc.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class LoginPage {

    private final SelenideElement usernameInput = $x("//*[@name='username']");
    private final SelenideElement passwordInput = $x("//*[@name='password']");
    private final SelenideElement submitBtn = $x("//*[@class='form__submit']");
    private final SelenideElement registerBtn = $x("//a[@href='/register']");
    private final SelenideElement errorLogin = $x("//*[contains(@class, 'login__error')]");

    public MainPage login(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        submitBtn.click();
        return new MainPage();
    }

    public LoginPage unsuccessLogin(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        submitBtn.click();
        errorLogin.shouldHave(text("Неверные учетные данные пользователя"));
        return this;
    }

    public RegisterPage clickRegistrationButton() {
        registerBtn.click();
        return new RegisterPage();
    }
}
