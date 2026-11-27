package io.student.rococo.test.ui;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.ApiLogin;
import io.student.rococo.jupiter.annotation.Artist;
import io.student.rococo.jupiter.annotation.Museum;
import io.student.rococo.jupiter.annotation.Painting;
import io.student.rococo.jupiter.annotation.ScreenShotTest;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.page.MainPage;
import io.student.rococo.service.impl.ArtistDbClient;
import io.student.rococo.service.impl.MuseumDbClient;
import io.student.rococo.service.impl.PaintingDbClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ScreenShotTests {

    private static final Config CFG = Config.getInstance();
    private final PaintingDbClient paintingDbClient = new PaintingDbClient();
    private final MuseumDbClient museumDbClient = new MuseumDbClient();
    private final ArtistDbClient artistDbClient = new ArtistDbClient();

    @AfterEach
    void cleanUp() {
        paintingDbClient.deleteAllPaintings();
        museumDbClient.deleteAllMuseums();
        artistDbClient.deleteAllArtists();
    }

    @Test
    @User
    @ScreenShotTest(value = "files/mainPage/dark_mode.jpg")
    @DisplayName("Проверка отображения темной темы")
    void checkDarkMode(BufferedImage expectedAvatar) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickThemeSwitcher()
                .checkTheme(expectedAvatar);
    }

    @Test
    @User
    @ScreenShotTest(value = "files/mainPage/light_mode.jpg")
    @DisplayName("Проверка отображения светлой темы")
    void checkLightMode(BufferedImage expectedAvatar) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .checkTheme(expectedAvatar);
    }

    @Test
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan"),
            paintings = @Painting(preset = "levitan")
    )
    @ApiLogin
    @ScreenShotTest(value = "files/artists/levitan_photo_in_card.jpg")
    @DisplayName("Проверка фото карточки художника")
    void checkOpenedArtist(TestData testData, BufferedImage expectedPainting) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .clickOnArtist(testData.artist().name())
                .checkPhotoArtistInArtistCard(expectedPainting);
    }


    @Test
    @User(museums = @Museum(preset = "levitan"))
    @ApiLogin
    @ScreenShotTest(value = "files/museums/tretyakovka_in_card.jpg")
    @DisplayName("Проверка фото карточки музея")
    void checkMuseumCard(TestData testData, BufferedImage expectedPainting) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickMuseumsTab()
                .clickOnMuseum(testData.museum().title())
                .checkPhotoInCard(expectedPainting);
    }

    @Test
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan"),
            paintings = @Painting(preset = "levitan")
    )
    @ApiLogin
    @ScreenShotTest(value = "files/paintings/nad_vechnym_pokoem_in_card.jpg")
    @DisplayName("Проверка карточки картины")
    void checkPaintingCard(TestData testData,  BufferedImage expectedPainting) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickPaintingsTab()
                .clickOnPainting(testData.painting().title())
                .checkPaintingImage(expectedPainting);
    }

    @Test
    @User
    @ApiLogin
    @ScreenShotTest(value = "files/profile/avatar.png")
    @DisplayName("Проверка фото профиля профиля")
    void positiveCheckEditProfile(BufferedImage expectedAvatar) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickProfileIconBtn()
                .addFileAvatar("files/profile/avatar.png")
                .clickUpdateProfile()
                .clickProfileIconBtn()
                .checkPhotoInProfile(expectedAvatar);
    }
}
