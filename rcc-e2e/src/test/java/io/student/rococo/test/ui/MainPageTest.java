package io.student.rococo.test.ui;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.page.MainPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MainPageTest {

    private static final Config CFG = Config.getInstance();

    @Test
    @User
    @DisplayName("Проверка перехода на главную страницу с помощью иконки Rococo из страницы Картины")
    void checkThatOpenedMainPageFromPaintings(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .clickPaintings()
                .checkTitlePaintings()
                .clickOnRococoIcon()
                .checkThatOpenMainPage()
                .clickPaintingsTab()
                .checkTitlePaintings()
                .clickOnRococoIcon()
                .checkThatOpenMainPage();
    }

    @Test
    @User
    @DisplayName("Проверка перехода на главную страницу с помощью иконки Rococo из страницы Художники")
    void checkThatOpenedMainPageFromArtists(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .clickArtists()
                .checkTitleArtists()
                .clickOnRococoIcon()
                .checkThatOpenMainPage()
                .clickArtistsTab()
                .checkTitleArtists()
                .clickOnRococoIcon()
                .checkThatOpenMainPage();
    }

    @Test
    @User
    @DisplayName("Проверка перехода на главную страницу с помощью иконки Rococo из страницы Музеи")
    void checkThatOpenedMainPageFromMuseums(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .clickMuseums()
                .checkTitleMuseums()
                .clickOnRococoIcon()
                .checkThatOpenMainPage()
                .clickMuseumsTab()
                .checkTitleMuseums()
                .clickOnRococoIcon()
                .checkThatOpenMainPage();
    }
}
