package io.student.rococo.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;

import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class MuseumPage extends BasePage {

    private final SelenideElement addMuseumBtn = $x("//button[.='Добавить музей']");
    private final SelenideElement modalFormAddMuseum =
            $x(".//button[.='Добавить музей']/ancestor::*[contains(@class, 'overflow-hidden')]//form");
    private final SelenideElement closeModalBtn = $x("//button[.='Закрыть']");
    private final ElementsCollection museumNames = $$("div.mt-2");
    private final ElementsCollection cityAndCountryNames = $$("div.mt-2 + div");
    private final SelenideElement titleMuseum = $("header.card-header");
    private final SelenideElement addressMuseum = $("#page-content div.text-center");
    private final SelenideElement editMuseum = $("[data-testid=\"edit-museum\"]");
    private final SelenideElement descriptionMuseum = $("#page-content div:nth-child(4)");
    private final SelenideElement nameMuseumInput = $("[name=\"title\"]");
    private final SelenideElement errorUnderNameMuseumInput = $("[name='title'] + .text-error-400");
    private final ElementsCollection countersSelect = $$("option");
    private final SelenideElement nameCityInput = $("[name=\"city\"]");
    private final SelenideElement errorUnderNameCityInput = $("[name=\"city\"] + .text-error-400");
    private final SelenideElement photoMuseumInput = $("[name=\"photo\"]");
    private final SelenideElement descriptionMuseumInput = $("[name=\"description\"]");
    private final SelenideElement errorUnderDescriptionMuseumInput = $("[name=\"description\"] + .text-error-400");
    private final SelenideElement closeBtn = $("button.variant-ringed");
    private final SelenideElement addBtn = $("button.variant-filled-primary");

    @Step("Нажать на кнопку 'Добавить музей'")
    public MuseumPage clickAddMuseum() {
        addMuseumBtn.click();
        return this;
    }

    @Step("Проверить, что открылось модальное окно 'Добавить музей'")
    public MuseumPage shouldVisibleMuseumModal() {
        modalFormAddMuseum.shouldBe(visible);
        return this;
    }

    @Step("Закрыть модальное окно 'Добавить музей'")
    public MuseumPage closeModal() {
        closeModalBtn.click();
        return this;
    }

    @Step("Проверить, что корректное отображается название музея")
    public MuseumPage checkNameMuseumIsDisplayed(String name) {
        museumNames.findBy(text(name)).shouldBe(visible);
        return this;
    }

    @Step("Нажать на музей '{name}'")
    public MuseumPage clickOnMuseum(String name) {
        museumNames.findBy(text(name)).shouldBe(visible).click();
        return this;
    }

    @Step("Нажать на музей {name}")
    public MuseumPage checkCityAndCountryMuseumIsDisplayed(String city, String countryName) {
        String expectedText = city + ", " + countryName;
        cityAndCountryNames.findBy(text(expectedText)).shouldBe(visible);
        return this;
    }

    @Step("Проверить, что название музея '{museumName}' верно отображается")
    public MuseumPage checkTitleMuseum(String museumName) {
        titleMuseum.shouldHave(text(museumName));
        return this;
    }

    @Step("Проверить, что адрес музея '{address}' верно отображается")
    public MuseumPage checkAddressMuseum(String address) {
        addressMuseum.shouldHave(text(address));
        return this;
    }

    @Step("Нажать на кнопку 'Редактировать'")
    public MuseumPage clickEditMuseum() {
        editMuseum.click();
        return this;
    }

    @Step("Проверить, что описание музея верно отображается")
    public MuseumPage checkDescriptionMuseum(String description) {
        descriptionMuseum.shouldHave(text(description));
        return this;
    }

    @Step("Ввести название музея '{name}' в поле 'Название музея'")
    public MuseumPage writeNameMuseum(String name) {
        nameMuseumInput.setValue(name);
        return this;
    }

    @Step("Проверка отображаения ошибки контроля под полем 'Название музея'")
    public MuseumPage checkErrorUnderMuseumNameField() {
        errorUnderNameMuseumInput.shouldBe(visible).shouldHave(text("Название не может быть длиннее 255 символов"));
        return this;
    }

    @Step("В списке стран выбрать страну '{name}'")
    public MuseumPage chooseCounter(String name) {
        countersSelect.first().click();

        int attempts = 0;
        while (attempts < 100) {
            SelenideElement element = countersSelect.findBy(text(name));
            if (element.exists() && element.isDisplayed()) {
                element.click();
                return this;
            }

            actions().sendKeys(Keys.ARROW_DOWN).perform();
            attempts++;
        }
        return this;
    }

    @Step("Ввести в поле 'Укажите город' название города '{name}'")
    public MuseumPage writeNameCity(String name) {
        nameCityInput.setValue(name);
        return this;
    }

    @Step("Проверка отображаения ошибки контроля под полем ввода 'Укажите город'")
    public MuseumPage checkErrorUnderNameCityField() {
        errorUnderNameCityInput.shouldBe(visible).shouldHave(text("Город не может быть длиннее 255 символов"));
        return this;
    }

    @Step("Добавить фото музея")
    public MuseumPage addMuseumPhoto() {
        photoMuseumInput.uploadFromClasspath("files/museum.jpg");
        return this;
    }

    @Step("Написать описание музея в поле 'О музее'")
    public MuseumPage writeDescriptionMuseum(String description) {
        descriptionMuseumInput.doubleClick().clear();
        descriptionMuseumInput.setValue(description);
        return this;
    }

    @Step("Проверка отображаения ошибки контроля под полем ввода 'О музее'")
    public MuseumPage checkErrorUnderDescriptionField() {
        errorUnderDescriptionMuseumInput.shouldBe(visible).shouldHave(text("Описание не может быть длиннее 2000 символов"));
        return this;
    }

    @Step("Закрыть модальное окно 'Добавить музей'")
    public MuseumPage closeAddMuseum() {
        closeBtn.click();
        return this;
    }

    @Step("Нажать на кнопку 'Добавить'")
    public MuseumPage addMuseum() {
        sleep(10000);
        addBtn.click();
        return this;
    }

    @Step("Нажать на кнопку 'Сохранить'")
    public MuseumPage updateMuseum() {
        addBtn.click();
        return this;
    }
}
