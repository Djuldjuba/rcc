package io.student.rococo.test.ui;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.*;
import io.student.rococo.page.MainPage;
import io.student.rococo.service.impl.ArtistDbClient;
import io.student.rococo.service.impl.MuseumDbClient;
import io.student.rococo.service.impl.PaintingDbClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static io.student.rococo.jupiter.TestDataPresets.*;
import static io.student.rococo.utils.RandomDataUtils.randomMaxLengthName;

@ResourceLock("artist")
@ResourceLock("museum")
@ResourceLock("painting")
@Execution(ExecutionMode.SAME_THREAD)
public class PaintingsTests {

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
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan")
    )
    @ApiLogin
    @ScreenShotTest(value = "files/paintings/nad_vechnym_pokoem_in_paintings_page.jpg", rewriteExpected = false)
    @DisplayName("Проверка добавления картины")
    void checkAddPainting(TestData testData, BufferedImage expectedPainting) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickPaintingsTab()
                .checkTitlePaintings()
                .clickAddPainting()
                .checkNameFieldPaintingName()
                .checkNameFieldInputPhoto()
                .checkNameFieldArtistSelect()
                .checkNameFieldDescriptionPainting()
                .checkNameFieldMuseumsSelect()
                .inputPaintingName(LEVITAN_PAINTING_TITLE)
                .inputPhotoPainting(LEVITAN_PAINTING_PHOTO)
                .selectArtist(testData.artist().name())
                .selectMuseum(testData.museum().title())
                .inputDescriptionPainting(LEVITAN_PAINTING_DESCRIPTION)
                .clickAddBtn()
                .checkNotification("Добавлена картины: " + LEVITAN_PAINTING_TITLE)
                .checkPaintingIsDisplayed(LEVITAN_PAINTING_TITLE)
                .checkPaintingPhoto(expectedPainting, LEVITAN_PAINTING_TITLE);
    }

    @Test
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan")
    )
    @ApiLogin
    @ScreenShotTest(value = "files/paintings/nad_vechnym_pokoem_in_paintings_page.jpg", rewriteExpected = false)
    @DisplayName("Проверка создания картины по кнопке посередине страницы когда нет ни одной картины")
    void checkAddPaintingIfNoPaintingsBtn(TestData testData, BufferedImage expectedPainting) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickPaintingsTab()
                .checkTextWhenNoPaintings()
                .clickAddPaintingBtnWhenNoPaintings()
                .inputPaintingName(LEVITAN_PAINTING_TITLE)
                .inputPhotoPainting(LEVITAN_PAINTING_PHOTO)
                .selectArtist(testData.artist().name())
                .selectMuseum(testData.museum().title())
                .inputDescriptionPainting(LEVITAN_PAINTING_DESCRIPTION)
                .clickAddBtn()
                .checkNotification("Добавлена картины: " + LEVITAN_PAINTING_TITLE)
                .checkPaintingIsDisplayed(LEVITAN_PAINTING_TITLE)
                .checkPaintingPhoto(expectedPainting, LEVITAN_PAINTING_TITLE);
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
                .checkTitle(testData.painting().title())
                .checkNameArtist(testData.artist().name())
                .checkDescriptionPainting(testData.painting().description())
                .checkPaintingImage(expectedPainting);
    }

    @Test
    @User(
            museums = {
                    @Museum(preset = "levitan"),
                    @Museum(preset = "louvre")
            },
            artists = {
                    @Artist(preset = "levitan"),
                    @Artist(preset = "louvre")
            },
            paintings = {
                    @Painting(preset = "levitan"),
                    @Painting(preset = "louvre")
            }
    )
    @ApiLogin
    @ScreenShotTest(value = "files/paintings/mona_lisa_in_card.jpg", rewriteExpected = false)
    @DisplayName("Проверка добавления картины")
    void checkEditPainting(TestData testData, BufferedImage expectedPainting) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickPaintingsTab()
                .clickOnPainting(testData.painting().title())
                .clickEditPainting()
                .inputPaintingName(LOUVRE_PAINTING_TITLE)
                .inputPhotoPainting(LOUVRE_PAINTING_PHOTO)
                .selectArtist(LOUVRE_ARTIST_NAME)
                .selectMuseum(LOUVRE_MUSEUM_TITLE)
                .inputDescriptionPainting(LOUVRE_ARTIST_BIOGRAPHY)
                .clickAddBtn()
                .checkNotification("Обновлена картина: " + LOUVRE_PAINTING_TITLE)
                .checkTitle(LOUVRE_PAINTING_TITLE)
                .checkNameArtist(LOUVRE_ARTIST_NAME)
                .checkDescriptionPainting(LOUVRE_ARTIST_BIOGRAPHY)
                .checkPaintingImage(expectedPainting);
    }

    @Test
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan"),
            paintings = @Painting(preset = "levitan")
    )
    @ApiLogin
    @ScreenShotTest(value = "files/paintings/nad_vechnym_pokoem_in_paintings_page.jpg")
    @DisplayName("Проверка успешного поиска")
    void searchPainting(TestData testData, BufferedImage expectedPainting) throws IOException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickPaintingsTab()
                .searchPainting(testData.painting().title())
                .checkPaintingIsDisplayed(testData.painting().title())
                .checkPaintingPhoto(expectedPainting, testData.painting().title());
    }

    @Test
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan"),
            paintings = @Painting(preset = "levitan")
    )
    @ApiLogin
    @DisplayName("Проверка поиска Картины с помощью строки поиска, когда ни одной картины не будет найдено")
    void searchPaintingNegative() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickPaintingsTab()
                .searchPainting(LEVITAN_PAINTING_TITLE + "123")
                .checkTextIfArtistsNotFound();
    }

    @Test
    @User
    @DisplayName("Проверка невозможности добавления картины неавторизованному пользователю")
    void unauthorizedUserCannotAddPainting() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickPaintingsTab()
                .checkAddPaintingIsNotDisplayed();
    }

    @Test
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan")
    )
    @ApiLogin
    @DisplayName("Проверка ошибок контроля при создании новой картины")
    void addNewPaintingWithIncorrectFields(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickPaintingsTab()
                .clickAddPainting()
                .inputPaintingName(randomMaxLengthName(1))
                .inputPhotoPainting(LEVITAN_PAINTING_PHOTO)
                .selectArtist(testData.artist().name())
                .selectMuseum(testData.museum().title())
                .inputDescriptionPainting(randomMaxLengthName(3))
                .clickAddBtn()
                .checkErrorUnderInputName("Название не может быть короче 3 символов")
                .checkErrorUnderDescriptionName("Описание не может быть короче 10 символов")
                .inputPaintingName(randomMaxLengthName(300))
                .inputDescriptionPainting(randomMaxLengthName(2100))
                .clickAddBtn()
                .checkErrorUnderInputName("Название не может быть длиннее 255 символов")
                .checkErrorUnderDescriptionName("Описание не может быть длиннее 2000 символов");
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка закрытия модального окна добавления картины")
    void checkClodeModalFromAddPainting() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickPaintingsTab()
                .clickAddPainting()
                .clickClodeBtn()
                .checkThatModalFormIsClose();
    }
}
