package io.student.rococo.page;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import io.student.rococo.jupiter.extensions.ScreenShotTestExtension;
import io.student.rococo.utils.ScreenDiffResult;
import org.junit.jupiter.api.Assertions;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PaintingPage extends BasePage<PaintingPage> {

    public static final String URL = CFG.frontUrl() + "/painting";

    // main page
    private final SelenideElement title = content.$("h2");
    private final SelenideElement addPaintingBtn = content.$(".items-center [type='button']");
    private final ElementsCollection paintingsNamesList = content.$$(".mt-2");
    private final ElementsCollection paintingsPhotosList = content.$$("a img");

    // no paintings
    private final SelenideElement textNoPaintings = content.$("p");
    private final SelenideElement addPaintingWhenNoPaintings = content.$("section button.btn");

    // modal form — специфичные поля
    private final SelenideElement paintingNameInput = modal.field("input[name='title']");
    private final SelenideElement errorUnderInputName = paintingNameInput.parent().$(".text-error-400");
    private final SelenideElement nameFieldPaintingNameInput = paintingNameInput.parent().$("span");
    private final SelenideElement paintingPhotoInput = modal.field("input[name='content']");
    private final SelenideElement nameFieldPaintingPhotoInput = paintingPhotoInput.parent().$("span");
    private final SelenideElement nameFieldArtistsSelect = modal.field("select[name='authorId']").preceding(0);
    private final SelenideElement descriptionInput = modal.field("textarea[name='description']");
    private final SelenideElement errorUnderDescriptionName = descriptionInput.parent().$(".text-error-400");
    private final SelenideElement nameFieldDescriptionInput = descriptionInput.parent().$("span");
    private final SelenideElement nameFieldMuseumsSelect = modal.field("select[name='museumId']").preceding(0);
    private final SelenideElement artistsSelect = modal.field("select[name='authorId']");
    private final SelenideElement museumsSelect = modal.field("select[name='museumId']");

    // card
    private final SelenideElement titlePainting = content.$(".card-header");
    private final SelenideElement artistName = content.$("div.text-center");
    private final SelenideElement editPainting = content.$("[data-testid=\"edit-painting\"]");
    private final SelenideElement descriptionPainting = content.$("div.m-4");
    private final SelenideElement photoPaintingInCard = content.$("img");

    // if paintings not found
    private final SelenideElement textPaintingsNotFound1line = content.$("section p", 0);
    private final SelenideElement textPaintingsNotFound2line = content.$("section p", 1);

    @Step("Проверить название страницы")
    public PaintingPage checkTitlePaintings() {
        title.shouldHave(text("Картины"));
        return this;
    }

    @Step("Ввести название картины")
    public PaintingPage inputPaintingName(String name) {
        paintingNameInput.setValue(name);
        return this;
    }

    @Step("Проверить название поля 'Название картины'")
    public PaintingPage checkNameFieldPaintingName() {
        nameFieldPaintingNameInput.shouldHave(text("Название картины"));
        return this;
    }

    @Step("Загрузить изображение картины")
    public PaintingPage inputPhotoPainting(String filePath) {
        paintingPhotoInput.uploadFromClasspath(filePath);
        return this;
    }

    @Step("Проверить название поля 'Загрузите изображение картины'")
    public PaintingPage checkNameFieldInputPhoto() {
        nameFieldPaintingPhotoInput.shouldHave(text("Загрузите изображение картины"));
        return this;
    }

    @Step("Проверить название поля 'Укажите автора картины'")
    public PaintingPage checkNameFieldArtistSelect() {
        nameFieldArtistsSelect.shouldHave(text("Укажите автора картины"));
        return this;
    }

    @Step("Ввести описание картины")
    public PaintingPage inputDescriptionPainting(String description) {
        descriptionInput.setValue(description);
        return this;
    }

    @Step("Проверить название поля 'Описание картины'")
    public PaintingPage checkNameFieldDescriptionPainting() {
        nameFieldDescriptionInput.shouldHave(text("Описание картины"));
        return this;
    }

    @Step("Выбрать в списке музей где хранится картина")
    public PaintingPage selectMuseum(String name) {
        SelenideElement option = museumsSelect.$$("option").findBy(text(name));
        option.scrollTo().click();
        return this;
    }

    @Step("Выбрать в списке автора картины")
    public PaintingPage selectArtist(String name) {
        SelenideElement option = artistsSelect.$$("option").findBy(text(name));
        option.scrollTo().click();
        return this;
    }

    @Step("Проверить название поля 'Укажите, где хранится оригинал'")
    public PaintingPage checkNameFieldMuseumsSelect() {
        nameFieldMuseumsSelect.shouldHave(text("Укажите, где хранится оригинал"));
        return this;
    }

    @Step("Нажать кнопку 'Добавить'")
    public PaintingPage clickAddBtn() {
        modal.submit();
        return this;
    }

    @Step("Нажать кнопку 'Добавить'")
    public ArtistPage clickAddBtnFromArtistPage() {
        modal.submit();
        return new ArtistPage();
    }

    @Step("Нажать кнопку 'Закрыть'")
    public PaintingPage clickClodeBtn() {
        modal.close();
        return this;
    }

    @Step("Проверить что модальное окно 'Добавить картину' закрыто")
    public PaintingPage checkThatModalFormIsClose() {
        modal.checkClosed();
        return this;
    }

    @Step("Нажать на кнопку 'Добавить картину'")
    public PaintingPage clickAddPainting() {
        addPaintingBtn.click();
        return this;
    }

    @Step("Проверить, что имя художника '{name}' верно отображается")
    public PaintingPage checkNameArtist(String name) {
        artistName.shouldHave(text(name));
        return this;
    }

    @Step("Нажать на кнопку 'Редактировать'")
    public PaintingPage clickEditPainting() {
        editPainting.click();
        return this;
    }

    @Step("Проверить, что описание картины верно отображается")
    public PaintingPage checkDescriptionPainting(String description) {
        descriptionPainting.shouldHave(text(description));
        return this;
    }

    @Step("Проверка отображения изображения картины в карточке картины")
    @Nonnull
    public PaintingPage checkPaintingImage(BufferedImage expected) throws IOException {
        Selenide.sleep(1000);
        BufferedImage actualImage = ImageIO.read(Objects.requireNonNull(photoPaintingInCard.screenshot()));
        assertFalse(
                new ScreenDiffResult(
                        actualImage, expected
                ),
                ScreenShotTestExtension.ASSERT_SCREEN_MESSAGE
        );
        return this;
    }

    @Step("Нажать на картину с названием 'paintingName'")
    public PaintingPage clickOnPainting(String paintingName) {
        SelenideElement painting = paintingsNamesList.findBy(text(paintingName));
        painting.scrollTo().click();
        return this;
    }

    @Step("Проверить что картина с названием '' отображается и ничего кроме нее не отображается")
    public PaintingPage checkPaintingIsDisplayed(String paintingName) {
        paintingsNamesList.shouldHave(CollectionCondition.size(1));
        paintingsNamesList.findBy(text(paintingName)).shouldBe(visible);
        return this;
    }

    @Step("Проверить что картина {name} присутствует на странице и вернуть её изображение")
    public SelenideElement getImageForPainting(String name) {
        for (int i = 0; i < paintingsNamesList.size(); i++) {
            if (paintingsNamesList.get(i).getText().equals(name)) {
                SelenideElement image = paintingsPhotosList.get(i);
                image.shouldBe(visible);
                return image;
            }
        }
        throw new AssertionError("Картина с названием '" + name + "' не найдена");
    }

    @Step("Проверка отображения фотографии картины на вкладке 'Картины'")
    @Nonnull
    public PaintingPage checkPaintingPhoto(BufferedImage expected, String name) throws IOException {
        Selenide.sleep(1000);
        BufferedImage actualImage = ImageIO.read(Objects.requireNonNull(getImageForPainting(name).screenshot()));
        assertFalse(
                new ScreenDiffResult(
                        actualImage, expected
                ),
                ScreenShotTestExtension.ASSERT_SCREEN_MESSAGE
        );
        return this;
    }

    @Step("Проверка текста не найдена ни одна картина")
    public PaintingPage checkTextIfArtistsNotFound() {
        textPaintingsNotFound1line.shouldHave(text("Картины не найдены"));
        textPaintingsNotFound2line.shouldHave(text("Для указанного вами фильтра мы не смогли не найти ни одной картины"));
        return this;
    }

    @Step("Проверка названия картины {paintingName} в карточке картины")
    public PaintingPage checkTitle(String paintingName) {
        titlePainting.shouldHave(text(paintingName));
        return this;
    }

    @Step("Проверка что неавторизованный пользователь не может добавить картину")
    public PaintingPage checkAddPaintingIsNotDisplayed() {
        Assertions.assertFalse(addPaintingBtn.isDisplayed());
        return this;
    }

    @Step("Проверить текст на странице когда нет ни одной картины")
    public PaintingPage checkTextWhenNoPaintings() {
        textNoPaintings.shouldHave(text("Пока что список картин пуст. Чтобы пополнить коллекцию, добавьте новую картину"));
        return this;
    }

    @Step("Нажать на кнопку 'Добавить картину' посередине страницы Картины, когда нет ни одной картины")
    public PaintingPage clickAddPaintingBtnWhenNoPaintings() {
        addPaintingWhenNoPaintings.shouldBe(visible).click();
        return this;
    }

    @Step("Проверить текст ошибки под полем 'Название картины'")
    public PaintingPage checkErrorUnderInputName(String errorText) {
        errorUnderInputName.shouldHave(text(errorText));
        return this;
    }

    @Step("Проверить текст ошибки под полем 'Описание картины'")
    public PaintingPage checkErrorUnderDescriptionName(String errorText) {
        errorUnderDescriptionName.shouldHave(text(errorText));
        return this;
    }
}