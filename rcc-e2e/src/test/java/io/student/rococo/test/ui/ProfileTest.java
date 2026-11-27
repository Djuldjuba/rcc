package io.student.rococo.test.ui;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.ApiLogin;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.page.MainPage;
import io.student.rococo.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProfileTest {

    private static final Config CFG = Config.getInstance();

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка редактирования профиля")
    void positiveCheckEditProfile(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickProfileIconBtn()
                .shouldDisplayTitleModal()
                .shouldDisplayAvatarInitials()
                .checkNickname(testData.user().username())
                .addFileAvatar("files/profile/avatar.png")
                .writeName("Алексей")
                .writeSurname("Носков")
                .clickUpdateProfile()
                .checkNotification("Профиль обновлен")
                .clickProfileIconBtn()
                .shouldDisplayAvatar()
                .checkWrittenName("Алексей")
                .checkWrittenSurname("Носков");
    }

    @Test
    @User
    @DisplayName("Проверка закрытия модального окна профиля")
    void сheckClodeProfileModal(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .checkThatOpenMainPage()
                .clickProfileIconBtn()
                .clickCloseButton()
                .checkModalProfileIsNotDisplayed();
    }

    @Test
    @User
    @DisplayName("Проверка успешного выхода из профиля")
    void logout(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .checkThatOpenMainPage()
                .clickProfileIconBtn()
                .clickExitButton()
                .checkLoginButtonIdDisplayed();
    }

    @Test
    @User
    @DisplayName("Проверка ошибок контроля в окне профиля")
    void сheckErrorsInProfileModal(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .checkThatOpenMainPage()
                .clickProfileIconBtn()
                .writeName(RandomDataUtils.randomMaxLengthName(256))
                .writeSurname(RandomDataUtils.randomMaxLengthName(256))
                .clickUpdateProfile()
                .checkErrorUnderNameInput()
                .checkErrorUnderSurnameInput();
    }
}
