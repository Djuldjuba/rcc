package io.student.rococo.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import io.student.rococo.jupiter.extensions.ScreenShotTestExtension;
import io.student.rococo.utils.ScreenDiffResult;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ProfilePage extends BasePage<ProfilePage> {

    //modalForm
    private final SelenideElement modal = $(".modal");
    private final SelenideElement titleModal = modal.$("header");
    private final SelenideElement avatarInitials = modal.$(".avatar-initials");
    private final SelenideElement avatarIcon = modal.$(".avatar-image");
    private final SelenideElement chooseFileInput = modal.$("input[name=\"content\"]");
    private final SelenideElement nameInput = modal.$("input[name=\"firstname\"]");
    private final SelenideElement checkErrorUnderNameInput = nameInput.parent().$(".text-error-400");
    private final SelenideElement surnameInput = modal.$("input[name=\"surname\"]");
    private final SelenideElement checkErrorSurnameInput = surnameInput.parent().$(".text-error-400");
    private final SelenideElement closeBtn = modal.$("button.variant-ringed");
    private final SelenideElement updateProfileBtn = modal.$("button.variant-filled-primary");
    private final SelenideElement exitBtn = modal.$("button.variant-ghost");
    private final SelenideElement nickname = modal.$("h4");

    @Step("Проверить, что модальное окно не отображается")
    public ProfilePage checkModalProfileIsNotDisplayed() {
        modal.shouldNotBe(exist);
        return this;
    }

    @Step("Проверить, что отображается заголовок модального окна")
    public ProfilePage shouldDisplayTitleModal() {
        titleModal.shouldHave(text("Профиль"));
        return this;
    }

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

    @Step("Проверка введенного имени")
    public ProfilePage checkWrittenName(String name) {
        nameInput.shouldHave(value(name));
        return this;
    }

    @Step("Проверка ошибки контроля под полем Имя")
    public ProfilePage checkErrorUnderNameInput() {
        checkErrorUnderNameInput.shouldHave(text("Имя не может быть длиннее 255 символов"));
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

    @Step("Проверка введенной фамилии")
    public ProfilePage checkWrittenSurname(String surname) {
        surnameInput.shouldHave(value(surname));
        return this;
    }

    @Step("Проверка ошибки контроля под полем Фамилия")
    public ProfilePage checkErrorUnderSurnameInput() {
        checkErrorSurnameInput.shouldHave(text("Фамилия не может быть длиннее 255 символов"));
        return this;
    }

    @Step("Нажать на кнопку 'Обновить профиль'")
    public ProfilePage clickUpdateProfile() {
        updateProfileBtn.click();
        return new ProfilePage();
    }

    @Step("Проверка фото в профиле")
    @Nonnull
    public ProfilePage checkPhotoInProfile(BufferedImage expected) throws IOException {
        avatarIcon.shouldBe(visible);
        sleep(1000);
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
