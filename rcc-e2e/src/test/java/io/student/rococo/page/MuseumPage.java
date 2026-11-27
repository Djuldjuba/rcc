package io.student.rococo.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
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
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MuseumPage extends BasePage<MuseumPage> {

    public static final String URL = CFG.frontUrl() + "/museum";

    // main
    private final SelenideElement titleMuseums = content.$("h2");
    private final SelenideElement addMuseumBtn = content.$(".items-center button.btn");
    private final ElementsCollection museumNames = content.$$("li div.text-center.mt-2");
    private final ElementsCollection cityAndCountryNames = content.$$("li div.text-center:not(.mt-2)");
    private final ElementsCollection museumsPhotosList = content.$$("li img");

    // no museums
    private final SelenideElement textNoMuseums = content.$("p");
    private final SelenideElement addMuseumWhenNoMuseums = content.$("section button.btn");

    // modal form — специфичные поля
    private final SelenideElement museumNameInput = modal.field("[name='title']");
    private final SelenideElement nameFieldMuseumNameInput = museumNameInput.parent().$("span");
    private final SelenideElement countrySelect = modal.field("select");
    private final ElementsCollection countersSelect = countrySelect.$$("option");
    private final SelenideElement nameFieldCountryNameInput = modal.field("select").parent().$("span");
    private final SelenideElement cityNameInput = modal.field("[name='city']");
    private final SelenideElement nameFieldCityNameInput = cityNameInput.parent().$("span");
    private final SelenideElement museumPhotoInput = modal.field("[name='photo']");
    private final SelenideElement nameFieldMuseumPhotoInput = museumPhotoInput.parent().$("span");
    private final SelenideElement museumDescriptionInput = modal.field("[name='description']");
    private final SelenideElement nameFieldMuseumDescriptionInput = museumDescriptionInput.parent().$("span");

    // museum card
    private final SelenideElement museumName = content.$("header");
    private final SelenideElement countryAndCityName = museumName.parent().$("div");
    private final SelenideElement editMuseum = content.$("[data-testid='edit-museum']");
    private final SelenideElement descriptionMuseum = content.$("div:not([class]) div:not([class])");
    private final SelenideElement photoMuseumInCard = content.$("img");
    private final SelenideElement errorUnderNameMuseumInput = modal.field("[name='title'] + .text-error-400");
    private final SelenideElement errorUnderNameCityInput = modal.field("[name='city'] + .text-error-400");
    private final SelenideElement errorUnderDescriptionMuseumInput = modal.field("[name='description'] + .text-error-400");

    // if museums not found
    private final SelenideElement textMuseumsNotFound1line = content.$("section p", 0);
    private final SelenideElement textMuseumsNotFound2line = content.$("section p", 1);

    @Step("Ввести название музея {museumName}")
    public MuseumPage inputMuseumName(String museumName) {
        museumNameInput.clear();
        museumNameInput.setValue(museumName);
        return this;
    }

    @Step("Проверить название поля 'Название музея'")
    public MuseumPage checkNameFieldInputMuseumName() {
        nameFieldMuseumNameInput.shouldHave(text("Название музея"));
        return this;
    }

    @Step("В списке стран выбрать страну '{name}'")
    public MuseumPage chooseCounter(String name) {
        countrySelect.click();
        for (int i = 0; i < 20; i++) {
            SelenideElement option = countersSelect.filterBy(text(name)).first();
            if (option.exists()) {
                option.click();
                return this;
            }
            Selenide.executeJavaScript("arguments[0].scrollTop = arguments[0].scrollHeight", countrySelect.getWrappedElement());
            sleep(100);
        }
        return this;
    }

    @Step("Проверить название поля 'Укажите страну'")
    public MuseumPage checkNameFieldCountersSelect() {
        nameFieldCountryNameInput.shouldHave(text("Укажите страну"));
        return this;
    }

    @Step("Ввести город {museumName}")
    public MuseumPage inputCityName(String cityName) {
        cityNameInput.clear();
        cityNameInput.setValue(cityName);
        return this;
    }

    @Step("Проверить название поля 'Укажите город'")
    public MuseumPage checkNameFieldCityName() {
        nameFieldCityNameInput.shouldHave(text("Укажите город"));
        return this;
    }

    @Step("Загрузить изображение музея")
    public MuseumPage inputMuseumPhoto(String filePath) {
        museumPhotoInput.uploadFromClasspath(filePath);
        return this;
    }

    @Step("Проверить название поля 'Изображение музея'")
    public MuseumPage checkNameFieldMuseumPhoto() {
        nameFieldMuseumPhotoInput.shouldHave(text("Изображение музея"));
        return this;
    }

    @Step("Ввести описание музея {museumDescription}")
    public MuseumPage inputMuseumDescription(String museumDescription) {
        museumDescriptionInput.clear();
        museumDescriptionInput.setValue(museumDescription);
        return this;
    }

    @Step("Проверить название поля 'О музее'")
    public MuseumPage checkNameFieldMuseumDescription() {
        nameFieldMuseumDescriptionInput.shouldHave(text("О музее"));
        return this;
    }

    @Step("Закрыть модальное окно 'Добавить музей'")
    public MuseumPage closeModal() {
        modal.close();
        return this;
    }

    @Step("В модальном окне нажать на кнопку 'Добавить'")
    public MuseumPage addMuseum() {
        modal.submit();
        return this;
    }

    @Step("Проверить название музея {name} в карточке")
    public MuseumPage checkMuseumNameInCard(String name) {
        museumName.shouldHave(text(name));
        return this;
    }

    @Step("Проверить страну {country} и город {city}, в котором находится музей'")
    public MuseumPage checkCountryAndCityInCard(String country, String city) {
        String expectedText = country + ", " + city;
        countryAndCityName.shouldHave(text(expectedText));
        return this;
    }

    @Step("Нажать на кнопку 'Редактировать'")
    public MuseumPage clickEditMuseumInCard() {
        editMuseum.click();
        return this;
    }

    @Step("Проверить, что описание музея верно отображается")
    public MuseumPage checkDescriptionMuseumInCard(String description) {
        descriptionMuseum.shouldHave(text(description));
        return this;
    }

    @Step("Проверка отображения фотографии музея в карточке")
    @Nonnull
    public MuseumPage checkPhotoInCard(BufferedImage expected) throws IOException {
        Selenide.sleep(1000);
        BufferedImage actualImage = ImageIO.read(Objects.requireNonNull(photoMuseumInCard.screenshot()));
        assertFalse(
                new ScreenDiffResult(
                        actualImage, expected
                ),
                ScreenShotTestExtension.ASSERT_SCREEN_MESSAGE
        );
        return this;
    }

    @Step("Проверить заголовок 'Музеи'")
    public MuseumPage checkTitleMuseums() {
        titleMuseums.shouldHave(text("Музеи"));
        return this;
    }

    @Step("Нажать на кнопку 'Добавить музей'")
    public MuseumPage clickAddMuseum() {
        addMuseumBtn.click();
        return this;
    }

    @Step("Проверка что кнопка 'Добавить музей' не отображается")
    public MuseumPage checkBtnAddMuseumIsNotDisplayed() {
        addMuseumBtn.shouldNotBe(visible);
        return this;
    }

    @Step("Проверить, что корректное отображается название музея")
    public MuseumPage checkNameMuseumIsDisplayed(String name) {
        museumNames.findBy(text(name)).shouldBe(visible);
        return this;
    }

    @Step("Нажать на музей '{name}'")
    public MuseumPage clickOnMuseum(String name) {
        SelenideElement museum = museumNames.findBy(text(name)).shouldBe(visible);
        museum.scrollTo();
        Selenide.executeJavaScript("arguments[0].click();", museum);
        return this;
    }

    @Step("Нажать на музей {name}")
    public MuseumPage checkCityAndCountryMuseumIsDisplayed(String city, String countryName) {
        String expectedText = city + ", " + countryName;
        cityAndCountryNames.findBy(text(expectedText)).shouldBe(visible);
        return this;
    }

    @Step("Проверка отображаения ошибки контроля под полем 'Название музея'")
    public MuseumPage checkErrorUnderMuseumNameField(String errorText) {
        errorUnderNameMuseumInput.shouldBe(visible).shouldHave(text(errorText));
        return this;
    }

    @Step("Проверка отображения ошибки контроля под полем ввода 'Укажите город'")
    public MuseumPage checkErrorUnderNameCityField(String errorText) {
        errorUnderNameCityInput.shouldBe(visible).shouldHave(text(errorText));
        return this;
    }

    @Step("Проверка отображаения ошибки контроля под полем ввода 'О музее'")
    public MuseumPage checkErrorUnderDescriptionField(String errorText) {
        errorUnderDescriptionMuseumInput.shouldBe(visible).shouldHave(text(errorText));
        return this;
    }

    @Step("Проверить текст на странице когда нет ни одного музея")
    public MuseumPage checkTextWhenNoMuseums() {
        textNoMuseums.shouldHave(text("Пока что список музеев пуст. Чтобы пополнить коллекцию, добавьте новый музей"));
        return this;
    }

    @Step("Нажать на кнопку 'Добавить музей' посередине страницы Музеи, когда нет ни одного музея")
    public MuseumPage clickAddMuseumBtnWhenNoMuseums() {
        addMuseumWhenNoMuseums.shouldBe(visible).click();
        return this;
    }

    @Step("Проверить что музей {name} присутствует на странице и вернуть его аватар")
    public SelenideElement getPhotoMuseum(String name) {
        for (int i = 0; i < museumNames.size(); i++) {
            if (museumNames.get(i).getText().equals(name)) {
                SelenideElement avatar = museumsPhotosList.get(i);
                avatar.shouldBe(visible);
                return avatar;
            }
        }
        throw new AssertionError("Музей с названием '" + name + "' не найден");
    }

    @Step("Проверка отображения фотографии музея на вкладке 'Музеи'")
    @Nonnull
    public MuseumPage checkPhotoMuseum(BufferedImage expected, String name) throws IOException {
        sleep(1000);
        BufferedImage actualImage = ImageIO.read(Objects.requireNonNull(getPhotoMuseum(name).screenshot()));
        assertFalse(
                new ScreenDiffResult(
                        actualImage, expected
                ),
                ScreenShotTestExtension.ASSERT_SCREEN_MESSAGE
        );
        return this;
    }

    @Step("Проверка текста 'Музеи не найдены'")
    public MuseumPage checkTextIfMuseumsNotFound() {
        textMuseumsNotFound1line.shouldHave(text("Музеи не найдены"));
        textMuseumsNotFound2line.shouldHave(text("Для указанного вами фильтра мы не смогли не найти ни одного музея"));
        return this;
    }

    @Step("Проверить что модальное окно 'Добавить музей' закрыто")
    public MuseumPage checkThatModalFormIsClose() {
        modal.checkClosed();
        return this;
    }
}