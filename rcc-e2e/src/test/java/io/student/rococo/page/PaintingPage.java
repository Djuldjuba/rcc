package io.student.rococo.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import io.student.rococo.page.component.SearchField;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class PaintingPage extends BasePage {

    private final SelenideElement addPaintingBtn = $x("//button[.='Добавить картину']");
    private final SelenideElement modalFormAddPainting =
            $x(".//button[.='Добавить картину']/ancestor::*[contains(@class, 'overflow-hidden')]//form");
    private final SelenideElement closeModalBtn = $x("//button[.='Закрыть']");
    private final SelenideElement titlePainting = $("header.card-header");
    private final SelenideElement artistName = $("div.text-center");
    private final SelenideElement editPainting = $("[data-testid=\"edit-painting\"]");
    private final SelenideElement descriptionPainting = $x("//div[@class='m-4']");

    @Step("Нажать на кнопку 'Добавить картину'")
    public PaintingPage clickAddPainting() {
        addPaintingBtn.click();
        return this;
    }

    @Step("Проверить отображения модального окна 'Добавить картину'")
    public PaintingPage shouldVisiblePaintingModal() {
        modalFormAddPainting.shouldBe(visible);
        return this;
    }

    @Step("Закрыть модальное окно 'Добавить картину'")
    public PaintingPage closeModal() {
        closeModalBtn.click();
        return this;
    }

    @Step("Проверить, что название картины '{name}' верно отображается")
    public PaintingPage checkTitlePainting(String museumName) {
        titlePainting.shouldHave(text(museumName));
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
}
