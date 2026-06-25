package io.student.rococo.test;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.Museum;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.jupiter.extensions.ArtistExtension;
import io.student.rococo.jupiter.extensions.BrowserExtension;
import io.student.rococo.jupiter.extensions.MuseumExtension;
import io.student.rococo.jupiter.extensions.PaintingExtension;
import io.student.rococo.jupiter.extensions.TestDataExtension;
import io.student.rococo.jupiter.extensions.UserExtension;
import io.student.rococo.page.MainPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.student.rococo.jupiter.extensions.UserExtension.getDefaultPassword;
import static io.student.rococo.utils.RandomDataUtils.randomLongText;

@ExtendWith({TestDataExtension.class, UserExtension.class, MuseumExtension.class, ArtistExtension.class, PaintingExtension.class, BrowserExtension.class})
public class MuseumTest {

    private static final Config CFG = Config.getInstance();

    @Test
    @User()
    @Museum(title = "Лувр", city = "Санкт-Петербург", countryName = "Франция", description = "Самый лучший музей")
    void checkMuseumCreatedInDb(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), getDefaultPassword())
                .checkThatUserIsAuthorized()
                .clickMuseums()
                .checkNameMuseumIsDisplayed(testData.museum().title())
                .checkCityAndCountryMuseumIsDisplayed(testData.museum().city(), testData.museum().country().name());
    }

    @Test
    @User()
    @Museum(title = "Лувр", city = "Санкт-Петербург", countryName = "Франция", description = "Самый лучший музей")
    void editProfile(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), getDefaultPassword())
                .checkThatUserIsAuthorized()
                .clickProfileIconBtn()
                .addFileAvatar("files/avatar.png")
                .writeName(testData.user().firstName())
                .writeSurname(testData.user().lastName())
                .clickUpdateProfile()
                .checkThatProfileUpdated("Профиль обновлен");
    }

    @Test
    @User()
    @Museum(title = "Лувр", city = "Санкт-Петербург", countryName = "Франция", description = "Самый лучший музей")
    void editMuseum(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), getDefaultPassword())
                .checkThatUserIsAuthorized()
                .clickMuseums()
                .clickOnMuseum(testData.museum().title())
                .clickEditMuseum()
                .writeDescriptionMuseum(testData.museum().description())
                .updateMuseum()
                .checkDescriptionMuseum(testData.museum().description());
    }

    @Test
    @User()
    @Museum(title = "Лувр", city = "Санкт-Петербург", countryName = "Франция", description = "Самый лучший музей")
    void createMuseum(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), getDefaultPassword())
                .checkThatUserIsAuthorized()
                .clickMuseums()
                .clickAddMuseum()
                .writeNameMuseum(testData.museum().title())
                .writeNameCity(testData.museum().city())
                .addMuseumPhoto()
                .writeDescriptionMuseum(testData.museum().description())
                .chooseCounter(testData.museum().country().name())
                .addMuseum()
                .clickOnMuseum(testData.museum().title());
    }

    @Test
    @User()
    @Museum()
    void createMuseumCheckErrorUnderTextFields(TestData testData) {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login(testData.user().username(), getDefaultPassword())
                .checkThatUserIsAuthorized()
                .clickMuseums()
                .clickAddMuseum()
                .writeNameMuseum(randomLongText(256))
                .writeNameCity(randomLongText(256))
                .addMuseumPhoto()
                .writeDescriptionMuseum(randomLongText(2001))
                .chooseCounter(testData.museum().country().name())
                .addMuseum()
                .checkErrorUnderNameCityField()
                .checkErrorUnderMuseumNameField()
                .checkErrorUnderDescriptionField();
    }
}
