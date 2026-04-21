package io.student.rcc.page;

import com.codeborne.selenide.SelenideElement;
import org.apache.kafka.common.security.auth.Login;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class RegisterPage {

    private final SelenideElement usernameInput = $x("//*[@id='username']");
    private final SelenideElement passwordInput = $x("//*[@id='password']");
    private final SelenideElement submitPasswordBtn = $x("//*[@id='passwordSubmit']");
    private final SelenideElement registerBtn = $x("//*[@class='form__submit']");
    private final SelenideElement usernameErrorText = $x("//*[@class='form__error error__username']");
    private final SelenideElement passwordErrorText = $x("//*[@class='form__error error__password']");
    private final SelenideElement welcomeTitle = $x("//p[@class='form__subheader']");

    public RegisterPage setUsername(String username) {
        usernameInput.setValue(username);
        return this;
    }

    public RegisterPage setPassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    public RegisterPage setConfirmPassword(String password) {
        submitPasswordBtn.setValue(password);
        return this;
    }

    public RegisterPage clickRegisterUser() {
        registerBtn.click();
        return this;
    }

    public MainPage successRegistrationUser(String username, String password) {
        setUsername(username);
        setPassword(password);
        setConfirmPassword(password);
        registerBtn.click();
        welcomeTitle.shouldBe(visible);
        return new MainPage();
    }

    public RegisterPage unsuccessRegistrationUser(String username, String password) {
        setUsername(username);
        setPassword(password);
        setConfirmPassword(password);
        clickRegisterUser();
        return this;
    }

    public RegisterPage checkErrorUsername(String username) {
        usernameErrorText.shouldHave(text("Username `" + username + "` already exists"));
        return this;
    }

    public RegisterPage checkErrorPassword() {
        passwordErrorText.shouldHave(text("Passwords should be equal"));
        return this;
    }
}
