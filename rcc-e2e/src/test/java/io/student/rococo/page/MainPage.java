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

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MainPage extends BasePage<MainPage> {

    private final SelenideElement mainTitle = $("#page h1");
    private final SelenideElement paintings = $("#page-content [href='/painting']");
    private final SelenideElement artists = $("#page-content [href='/artist']");
    private final SelenideElement museums = $("#page-content [href='/museum']");
    private final SelenideElement page = $("[data-theme='gold-nouveau']");
    private final SelenideElement themeSwitcher = $(".lightswitch-track");

    @Step("Проверка что выполнен успешный вход")
    public MainPage checkThatOpenMainPage() {
        mainTitle.shouldHave(text("Ваши любимые картины и художники всегда рядом"));
        return this;
    }

    @Step("Нажать на плашку 'Картины'")
    public PaintingPage clickPaintings() {
        paintings.click();
        return new PaintingPage();
    }

    @Step("Нажать на плашку 'Художники'")
    public ArtistPage clickArtists() {
        artists.click();
        return new ArtistPage();
    }

    @Step("Нажать на плашку 'Музеи'")
    public MuseumPage clickMuseums() {
        museums.click();
        return new MuseumPage();
    }

    @Step("Проверка темы")
    @Nonnull
    public MainPage checkTheme(BufferedImage expected) throws IOException {
        BufferedImage actualImage = ImageIO.read(Objects.requireNonNull(page.screenshot()));
        assertFalse(
                new ScreenDiffResult(
                        actualImage, expected
                ),
                ScreenShotTestExtension.ASSERT_SCREEN_MESSAGE
        );
        return this;
    }

    @Step("Установить тему {theme}")
    public MainPage clickThemeSwitcher() {
        themeSwitcher.click();
        sleep(5000);
        return this;
    }
}
