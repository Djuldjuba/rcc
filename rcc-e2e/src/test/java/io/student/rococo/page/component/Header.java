package io.student.rococo.page.component;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import io.student.rococo.jupiter.extensions.ScreenShotTestExtension;
import io.student.rococo.page.ArtistPage;
import io.student.rococo.page.LoginPage;
import io.student.rococo.page.MainPage;
import io.student.rococo.page.MuseumPage;
import io.student.rococo.page.PaintingPage;
import io.student.rococo.page.ProfilePage;
import io.student.rococo.utils.ScreenDiffResult;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Header extends BaseComponent<Header> {

    public Header() {
        super($("header"));
    }

    private final SelenideElement rococoIcon = self.$("h1");
    private final SelenideElement loginBtn = self.$("button.btn");
    private final SelenideElement paintingsBtn = self.$("[href='/painting']");
    private final SelenideElement artistsBtn = self.$("[href='/artist']");
    private final SelenideElement museumsBtn = self.$("[href='/museum']");
    private final SelenideElement profileIconBtn = self.$(".avatar-image, .avatar-initials");
    private final SelenideElement lightSwitchBtn = self.$(".lightswitch-track");

    @Step("Перейти на главную страницу нажав на кнопку 'Rococo' в хедере")
    public MainPage clickOnRococoIcon() {
        rococoIcon.click();
        return new MainPage();
    }

    @Step("Нажать на кнопку 'Войти' в хедере")
    public LoginPage clickLoginButton() {
        loginBtn.click();
        return new LoginPage();
    }

    @Step("Проверить отображение кнопки Войти")
    public LoginPage checkLoginButtonIsDisplayed() {
        loginBtn.shouldBe(Condition.visible);
        return new LoginPage();
    }

    @Step("Нажать на таб 'Картины' в хедере")
    public PaintingPage clickPaintingsTab() {
        paintingsBtn.click();
        return new PaintingPage();
    }

    @Step("Нажать на таб 'Художники' в хедере")
    public ArtistPage clickArtistsTab() {
        Selenide.refresh();
        artistsBtn.click();
        return new ArtistPage();
    }

    @Step("Нажать на таб 'Музеи' в хедере")
    public MuseumPage clickMuseumsTab() {
        museumsBtn.click();
        return new MuseumPage();
    }

    @Step("Нажать на иконку профиля в хедере")
    public ProfilePage clickProfileIconBtn() {
        profileIconBtn.click();
        return new ProfilePage();
    }

    @Step("Переключить тему (светлая/темная)")
    public MainPage switchTheme() {
        lightSwitchBtn.click();
        Selenide.refresh();
        sleep(7000);
        return new MainPage();
    }

    @Step("Проверка фото профиля в хедере")
    @Nonnull
    public MainPage checkPhotoInHeader(BufferedImage expected) throws IOException {
        BufferedImage actualImage = ImageIO.read(Objects.requireNonNull(profileIconBtn.screenshot()));
        assertFalse(
                new ScreenDiffResult(
                        actualImage, expected
                ),
                ScreenShotTestExtension.ASSERT_SCREEN_MESSAGE
        );
        return new MainPage();
    }
}
