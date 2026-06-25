package io.student.rococo.test;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.ScreenShotTest;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.jupiter.extensions.ArtistExtension;
import io.student.rococo.jupiter.extensions.BrowserExtension;
import io.student.rococo.jupiter.extensions.MuseumExtension;
import io.student.rococo.jupiter.extensions.PaintingExtension;
import io.student.rococo.jupiter.extensions.TestDataExtension;
import io.student.rococo.jupiter.extensions.TestMethodContextExtension;
import io.student.rococo.jupiter.extensions.UserExtension;
import io.student.rococo.page.MainPage;
import org.junit.jupiter.api.extension.ExtendWith;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static io.student.rococo.jupiter.extensions.UserExtension.getDefaultPassword;

@ExtendWith({TestDataExtension.class, UserExtension.class, MuseumExtension.class, ArtistExtension.class,
        PaintingExtension.class, TestMethodContextExtension.class, BrowserExtension.class})
public class ProfileTest {

    private static final Config CFG = Config.getInstance();

    @User()
    @ScreenShotTest(value = "files/avatar.png", rewriteExpected = false)
    void checkPhoto(TestData testData, BufferedImage expectedAvatar) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), getDefaultPassword())
                .checkThatUserIsAuthorized()
                .clickProfileIconBtn()
                .addFileAvatar("files/avatar.png")
                .clickUpdateProfile()
                .clickProfileIconBtn()
                .checkPhoto(expectedAvatar);
    }
}
