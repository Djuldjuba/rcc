package io.student.rococo.page;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import io.student.rococo.jupiter.extensions.ScreenShotTestExtension;
import io.student.rococo.utils.ScreenDiffResult;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ProfilePage extends BasePage {

    private final SelenideElement avatarInitials = $(".modal .avatar-initials");
    private final SelenideElement avatarIcon = $(".modal .avatar-image");
    private final SelenideElement chooseFileInput = $("input[name=\"content\"]");
    private final SelenideElement nameInput = $("input[name=\"firstname\"]");
    private final SelenideElement surnameInput = $("input[name=\"surname\"]");
    private final SelenideElement closeBtn = $("button.variant-ringed");
    private final SelenideElement updateProfileBtn = $("button.variant-filled-primary");
    private final SelenideElement exitBtn = $("button.variant-ghost");
    private final SelenideElement nickname = $(".modal h4");

    @Step("Проверить, что отображаются инициалы аватара")
    public ProfilePage shouldDisplayAvatarInitials() {
        avatarInitials.shouldBe(visible);
        return this;
    }

    @Step("Проверить, что отображается аватар")
    public ProfilePage shouldDisplayAvatar() {
        avatarIcon.shouldBe(visible);
        return this;
    }

    @Step("Добавить изображение для аватара")
    public ProfilePage addFileAvatar(String path) {
        chooseFileInput.uploadFromClasspath(path);
        return this;
    }

    @Step("Ввести имя")
    public ProfilePage writeName(String name) {
        nameInput.setValue(name);
        return this;
    }

    @Step("Нажать на кнопку 'Закрыть'")
    public ProfilePage clickCloseButton() {
        closeBtn.click();
        return this;
    }

    @Step("Нажать на кнопку 'Выйти'")
    public ProfilePage clickExitButton() {
        exitBtn.click();
        return this;
    }

    @Step("Проверить отображение никнейма")
    public ProfilePage checkNickname(String nick) {
        nickname.shouldBe(visible).shouldHave(text("@" + nick));
        return this;
    }

    @Step("Ввести фамилию")
    public ProfilePage writeSurname(String surname) {
        surnameInput.setValue(surname);
        return this;
    }


    @Step("Нажать на кнопку 'Обновить профиль'")
    public MainPage clickUpdateProfile() {
        updateProfileBtn.click();
        return new MainPage();
    }

    @Step("Check photo")
    @Nonnull
    public ProfilePage checkPhoto(BufferedImage expected) throws IOException {
        Selenide.sleep(1000);
        BufferedImage actualImage = ImageIO.read(Objects.requireNonNull(avatarIcon.screenshot()));
        assertFalse(
                new ScreenDiffResult(
                        actualImage, expected
                ),
                ScreenShotTestExtension.ASSERT_SCREEN_MESSAGE
        );
        return this;
    }
}
