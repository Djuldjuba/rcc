package io.student.rococo.test.ui;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.ApiLogin;
import io.student.rococo.jupiter.annotation.Museum;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.page.MainPage;
import io.student.rococo.service.impl.MuseumDbClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import static io.student.rococo.jupiter.TestDataPresets.*;
import static io.student.rococo.utils.RandomDataUtils.randomLongText;

@ResourceLock("museum")
@Execution(ExecutionMode.SAME_THREAD)
public class MuseumsTest {

    private static final Config CFG = Config.getInstance();
    private final MuseumDbClient museumDbClient = new MuseumDbClient();

    @AfterEach
    void cleanUp() {
        museumDbClient.deleteAllMuseums();
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка создания музея")
    void createMuseumUi() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickMuseumsTab()
                .checkTitleMuseums()
                .clickAddMuseum()
                .checkNameFieldInputMuseumName()
                .checkNameFieldCountersSelect()
                .checkNameFieldCityName()
                .checkNameFieldMuseumPhoto()
                .checkNameFieldMuseumDescription()
                .chooseCounter(LEVITAN_MUSEUM_COUNTRY)
                .inputMuseumName(LEVITAN_MUSEUM_TITLE)
                .inputCityName(LEVITAN_MUSEUM_CITY)
                .inputMuseumPhoto(LEVITAN_MUSEUM_PHOTO)
                .inputMuseumDescription(LEVITAN_MUSEUM_DESCRIPTION)
                .addMuseum()
                .checkNotification("Добавлен музей: " + LEVITAN_MUSEUM_TITLE)
                .checkNameMuseumIsDisplayed(LEVITAN_MUSEUM_TITLE)
                .checkCityAndCountryMuseumIsDisplayed(LEVITAN_MUSEUM_CITY, LEVITAN_MUSEUM_COUNTRY);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка создания музея по кнопке посередине страницы когда нет ни одного музея")
    void createMuseumUiIfNoMuseumsBtn() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickMuseumsTab()
                .checkTitleMuseums()
                .clickAddMuseumBtnWhenNoMuseums()
                .chooseCounter(LEVITAN_MUSEUM_COUNTRY)
                .inputMuseumName(LEVITAN_MUSEUM_TITLE)
                .inputCityName(LEVITAN_MUSEUM_CITY)
                .inputMuseumPhoto(LEVITAN_MUSEUM_PHOTO)
                .inputMuseumDescription(LEVITAN_MUSEUM_DESCRIPTION)
                .addMuseum()
                .checkNotification("Добавлен музей: " + LEVITAN_MUSEUM_TITLE)
                .checkNameMuseumIsDisplayed(LEVITAN_MUSEUM_TITLE)
                .checkCityAndCountryMuseumIsDisplayed(LEVITAN_MUSEUM_CITY, LEVITAN_MUSEUM_COUNTRY);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка страницы если не создан ни один музей")
    void checkPageNoMuseums() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickMuseumsTab()
                .checkTextWhenNoMuseums();
    }

    @Test
    @User(museums = @Museum(preset = "levitan"))
    @ApiLogin
    @DisplayName("Проверка карточки музея")
    void checkMuseumCard(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickMuseumsTab()
                .checkNameMuseumIsDisplayed(testData.museum().title())
                .checkCityAndCountryMuseumIsDisplayed(testData.museum().city(), testData.museum().country().name())
                .clickOnMuseum(testData.museum().title())
                .checkMuseumNameInCard(testData.museum().title())
                .checkCountryAndCityInCard(testData.museum().country().name(), testData.museum().city())
                .checkDescriptionMuseumInCard(testData.museum().description());
    }

    @Test
    @User(museums = @Museum(preset = "levitan"))
    @ApiLogin
    @DisplayName("Проверка редактирования музея")
    void editMuseumUi(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickMuseumsTab()
                .clickOnMuseum(testData.museum().title())
                .clickEditMuseumInCard()
                .inputCityName(LEVITAN_MUSEUM_CITY_EDIT)
                .inputMuseumPhoto(LEVITAN_MUSEUM_PHOTO_EDIT)
                .chooseCounter(LEVITAN_MUSEUM_COUNTRY_EDIT)
                .inputMuseumDescription(LEVITAN_MUSEUM_DESCRIPTION_EDIT)
                .addMuseum()
                .checkNotification("Обновлен музей: " + testData.museum().title())
                .checkMuseumNameInCard(testData.museum().title())
                .checkCountryAndCityInCard(LEVITAN_MUSEUM_COUNTRY_EDIT, LEVITAN_MUSEUM_CITY_EDIT)
                .checkDescriptionMuseumInCard(LEVITAN_MUSEUM_DESCRIPTION_EDIT);
    }

    @Test
    @User
    @DisplayName("Проверка ошибок контроля при создании музея")
    void createMuseumCheckErrorUnderTextFields(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), testData.password())
                .checkThatOpenMainPage()
                .clickMuseumsTab()
                .clickAddMuseum()
                .inputMuseumName(randomLongText(256))
                .inputCityName(randomLongText(256))
                .inputMuseumPhoto(LEVITAN_MUSEUM_PHOTO)
                .inputMuseumDescription(randomLongText(2001))
                .chooseCounter(LEVITAN_MUSEUM_COUNTRY)
                .addMuseum()
                .checkErrorUnderNameCityField("Город не может быть длиннее 255 символов")
                .checkErrorUnderMuseumNameField("Название не может быть длиннее 255 символов")
                .checkErrorUnderDescriptionField("Описание не может быть длиннее 2000 символов")
                .inputMuseumName(randomLongText(2))
                .inputCityName(randomLongText(2))
                .inputMuseumDescription(randomLongText(2))
                .addMuseum()
                .checkErrorUnderNameCityField("Город не может быть короче 3 символов")
                .checkErrorUnderMuseumNameField("Название не может быть короче 3 символов")
                .checkErrorUnderDescriptionField("Описание не может быть короче 10 символов");
    }

    @Test
    @User
    @DisplayName("Проверка что создание музея недоступно для неавторизированного пользователя")
    void checkUnavailableCreateMuseumUnauthorizedUser() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickMuseumsTab()
                .checkBtnAddMuseumIsNotDisplayed();
    }

    @Test
    @User(museums = @Museum(preset = "levitan"))
    @ApiLogin
    @DisplayName("Проверка успешного поиска Музея с помощью строки поиска")
    void searchArtist(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickMuseumsTab()
                .searchMuseum(testData.museum().title())
                .checkNameMuseumIsDisplayed(testData.museum().title());
    }

    @Test
    @User(museums = @Museum(preset = "levitan"))
    @ApiLogin
    @DisplayName("Проверка поиска Музея с помощью строки поиска, когда ни один музей не будет найден")
    void searchArtistNegative(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickMuseumsTab()
                .searchMuseum(testData.museum().title() + "123")
                .checkTextIfMuseumsNotFound();
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Проверка закрытия модального окна добавления музея")
    void checkClodeModalFromAddArtist() {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickMuseumsTab()
                .clickAddMuseum()
                .closeModal()
                .checkThatModalFormIsClose();
    }
}
