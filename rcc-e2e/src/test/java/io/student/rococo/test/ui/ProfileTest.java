package io.student.rococo.test.ui;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.ScreenShotTest;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.jupiter.extensions.ArtistExtension;
import io.student.rococo.jupiter.extensions.MuseumExtension;
import io.student.rococo.jupiter.extensions.PaintingExtension;
import io.student.rococo.jupiter.extensions.TestDataExtension;
import io.student.rococo.jupiter.extensions.TestMethodContextExtension;
import io.student.rococo.jupiter.extensions.UserExtension;
import io.student.rococo.page.MainPage;
import io.student.rococo.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ProfileTest {

    private static final Config CFG = Config.getInstance();

    @Test
    @User
    @ScreenShotTest(value = "files/profile/avatar.png", rewriteExpected = false)
    @DisplayName("Проверка редактирования профиля")
    void positiveCheckEditProfile(TestData testData, BufferedImage expectedAvatar) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .checkThatOpenMainPage()
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
                .checkPhotoInProfile(expectedAvatar)
                .checkWrittenName("Алексей")
                .checkWrittenSurname("Носков");
    }

    @Test
    @User
    @ScreenShotTest(value = "files/profile/avatar_in_header.png", rewriteExpected = false)
    void сheckPhotoProfileInHeader(TestData testData, BufferedImage expectedAvatar) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .checkThatOpenMainPage()
                .clickProfileIconBtn()
                .addFileAvatar("files/profile/avatar.png")
                .clickUpdateProfile()
                .checkNotification("Профиль обновлен")
                .checkPhotoInHeader(expectedAvatar);
    }

    @Test
    @User
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
