package io.student.rococo.test.ui;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.ApiLogin;
import io.student.rococo.jupiter.annotation.Artist;
import io.student.rococo.jupiter.annotation.Museum;
import io.student.rococo.jupiter.annotation.Painting;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.page.MainPage;
import io.student.rococo.service.impl.ArtistDbClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import static io.student.rococo.jupiter.TestDataPresets.*;
import static io.student.rococo.utils.RandomDataUtils.randomMaxLengthName;

@ResourceLock("artist")
@Execution(ExecutionMode.SAME_THREAD)
public class ArtistsTests {

    private static final Config CFG = Config.getInstance();
    private final ArtistDbClient artistDbClient = new ArtistDbClient();

    @AfterEach
    void cleanUp() {
        artistDbClient.deleteAllArtists();
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка добавления художника")
    void addNewArtist() {
        Selenide.page(MainPage.class)
                .clickArtistsTab()
                .checkTitleArtists()
                .clickAddArtist()
                .checkTitleModalForm("Новый художник")
                .checkDescriptionModalFromAddArtist()
                .checkNameInputName()
                .checkNameInputPhoto("Изображение художника")
                .checkNameInputDescription()
                .inputName(LEVITAN_ARTIST_NAME)
                .inputPhoto(LEVITAN_ARTIST_PHOTO)
                .inputDescription(LEVITAN_ARTIST_BIOGRAPHY)
                .clickAddBtn()
                .checkNotification("Добавлен художник: " + LEVITAN_ARTIST_NAME)
                .checkArtistIsDisplayed(LEVITAN_ARTIST_NAME);
    }

    @Test
    @User(artists = @Artist(preset = "levitan"))
    @ApiLogin
    @DisplayName("Проверка успешного поиска Художника с помощью строки поиска")
    void searchArtist(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .searchArtist(testData.artist().name())
                .checkArtistIsDisplayed(testData.artist().name());
    }

    @Test
    @User(artists = @Artist(preset = "levitan"))
    @ApiLogin
    @DisplayName("Проверка поиска Художника с помощью строки поиска, когда ни один художник не будет найден")
    void searchArtistNegative(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .searchArtist(testData.artist().name() + "123")
                .checkTextIfArtistsNotFound();
    }

    @Test
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan"),
            paintings = @Painting(preset = "levitan")
    )
    @ApiLogin
    @DisplayName("Проверка карточки художника")
    void checkOpenedArtist(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .clickOnArtist(testData.artist().name())
                .checkArtistName(testData.artist().name())
                .checkArtistDescription(testData.artist().biography())
                .checkPaintingOfArtistIsDisplayed(testData.painting().title());
    }

    @Test
    @User(artists = @Artist(preset = "levitan"))
    @ApiLogin
    @DisplayName("Проверка редактирования карточки художника")
    void checkEditCardArtist(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .clickOnArtist(testData.artist().name())
                .clickEditArtist()
                .checkTitleModalForm("Редактировать художника")
                .checkNameInputPhoto("Обновить изображение художника")
                .checkNameInputName()
                .checkNameInputDescription()
                .inputPhoto(LEVITAN_ARTIST_PHOTO_EDIT)
                .inputDescription(LEVITAN_ARTIST_BIOGRAPHY_EDIT)
                .clickAddBtn()
                .checkNotification("Обновлен художник: " + LEVITAN_ARTIST_NAME)
                .checkArtistDescription(LEVITAN_ARTIST_BIOGRAPHY_EDIT);
    }

    @Test
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan")
    )
    @ApiLogin
    @DisplayName("Проверка добавления картины из карточки художника")
    void checkArtistCardWithoutPaintings(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .clickOnArtist(testData.artist().name())
                .checkTextWithoutPaintings()
                .clickAddPaintingBtnWithoutPaintings()
                .inputPaintingName(LEVITAN_PAINTING_TITLE)
                .inputPhotoPainting(LEVITAN_PAINTING_PHOTO)
                .inputDescriptionPainting(LEVITAN_PAINTING_DESCRIPTION)
                .selectMuseum(testData.museum().title())
                .clickAddBtnFromArtistPage()
                .checkNotification("Добавлена картина: " + LEVITAN_PAINTING_TITLE)
                .checkPaintingOfArtistIsDisplayed(LEVITAN_PAINTING_TITLE);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка ошибок контроля при создании нового Художника")
    void addNewArtistWithIncorrectFields() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .clickAddArtist()
                .inputName(randomMaxLengthName(1))
                .inputPhoto(LEVITAN_ARTIST_PHOTO)
                .inputDescription(randomMaxLengthName(3))
                .clickAddBtn()
                .checkErrorUnderInputName("Имя не может быть короче 3 символов")
                .checkErrorUnderInputDescription("Биография не может быть короче 10 символов")
                .inputName(randomMaxLengthName(300))
                .inputDescription(randomMaxLengthName(2100))
                .clickAddBtn()
                .checkErrorUnderInputName("Имя не может быть длиннее 255 символов")
                .checkErrorUnderInputDescription("Биография не может быть длиннее 2000 символов");
    }

    @Test
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan"),
            paintings = @Painting(preset = "levitan")
    )
    @ApiLogin
    @DisplayName("Проверка информации о картине, которая открывается при переходе из карточки художника")
    void checkInfoAboutPaintingInArtistsCard(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .clickOnArtist(testData.artist().name())
                .clickPainting(testData.painting().title())
                .clickOnPainting(testData.painting().title())
                .checkNameArtist(testData.artist().name())
                .checkDescriptionPainting(testData.painting().description());
    }

    @Test
    @User(
            museums = @Museum(preset = "levitan"),
            artists = @Artist(preset = "levitan")
    )
    @ApiLogin
    @DisplayName("Проверка отображения картины, созданной в карточке художника, на вкладке Картины")
    void checkPaintingInPaintingsArtist(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .clickOnArtist(testData.artist().name())
                .clickAddPaintingBtnWithoutPaintings()
                .inputPaintingName(LEVITAN_PAINTING_TITLE)
                .inputPhotoPainting(LEVITAN_PAINTING_PHOTO)
                .inputDescriptionPainting(LEVITAN_PAINTING_DESCRIPTION)
                .selectMuseum(testData.museum().title())
                .clickAddBtnFromArtistPage()
                .checkNotification("Добавлена картина: " + LEVITAN_PAINTING_TITLE)
                .clickPaintingsTab()
                .clickOnPainting(LEVITAN_PAINTING_TITLE)
                .checkTitle(LEVITAN_PAINTING_TITLE)
                .checkNameArtist(testData.artist().name())
                .checkDescriptionPainting(LEVITAN_PAINTING_DESCRIPTION);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка страницы если нет ни одного художника")
    void checkEmptyArtists() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .checkThatPaintingListEmpty();
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка добавления художника по кнопке посередине страницы когда нет ни одного художника")
    void addNewArtistsFromBtnIfNoArtists() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .checkTitleArtists()
                .clickAddArtistIfNoArtists()
                .checkTitleModalForm("Новый художник")
                .checkDescriptionModalFromAddArtist()
                .checkNameInputName()
                .checkNameInputPhoto("Изображение художника")
                .checkNameInputDescription()
                .inputName(LEVITAN_ARTIST_NAME)
                .inputPhoto(LEVITAN_ARTIST_PHOTO)
                .inputDescription(LEVITAN_ARTIST_BIOGRAPHY)
                .clickAddBtn()
                .checkNotification("Добавлен художник: " + LEVITAN_ARTIST_NAME)
                .checkArtistIsDisplayed(LEVITAN_ARTIST_NAME);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка закрытия модального окна добавления художника")
    void checkClodeModalFromAddArtist() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickArtistsTab()
                .clickAddArtist()
                .closeModal()
                .checkThatModalFormIsClose();
    }
}
