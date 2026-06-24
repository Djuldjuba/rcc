package io.student.rococo.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class RegisterPage {

    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement submitPasswordBtn = $("#passwordSubmit");
    private final SelenideElement registerBtn = $(".form__submit");
    private final SelenideElement usernameErrorText = $(".form__error.error__username");
    private final SelenideElement passwordErrorText = $(".form__error.error__password");
    private final SelenideElement welcomeTitle = $("p.form__subheader");


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

    @Step("Выполнить успешную регистрацию")
    public MainPage successRegistrationUser(String username, String password) {
        setUsername(username);
        setPassword(password);
        setConfirmPassword(password);
        registerBtn.click();
        welcomeTitle.shouldBe(visible);
        return new MainPage();
    }

    @Step("Выполнить неуспешную регистрацию")
    public RegisterPage unsuccessRegistrationUser(String username, String password) {
        setUsername(username);
        setPassword(password);
        setConfirmPassword(password);
        clickRegisterUser();
        return this;
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
