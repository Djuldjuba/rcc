package io.student.rococo.page;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class ArtistPage extends BasePage {
    private final SelenideElement addArtistBtn = $(byText("Добавить художника"));
    private final SelenideElement modalFormAddArtist =
            addArtistBtn.$x("ancestor::*[contains(@class, 'overflow-hidden')]//form");
    private final SelenideElement closeModalBtn = $x("//button[.='Закрыть']");

    private final SelenideElement artistName = $("header.card-header");
    private final ElementsCollection paintingsName = $$("div.text-center");
    private final SelenideElement editArtist = $("[data-testid=\"edit-artist\"]");
    private final SelenideElement descriptionArtist = $("#page-content p");

    @Step("Нажать на кнопку 'Добавить художника'")
    public ArtistPage clickAddArtist() {
        addArtistBtn.click();
        return this;
    }

    @Step("Проверить отображаения модального окна 'Добавить художника'")
    public ArtistPage shouldDisplayArtistModal() {
        modalFormAddArtist.shouldBe(visible);
        return this;
    }

    @Step("Закрыть модальное окно 'Добавить художника'")
    public ArtistPage closeModal() {
        closeModalBtn.click();
        return this;
    }

    @Step("Проверить оторажения имени художника")
    public ArtistPage checkArtistName(String name) {
        artistName.shouldBe(visible).shouldHave(text(name));
        return this;
    }

    @Step("Проверить отображение названий картин")
    public ArtistPage checkPaintingsNames(String... expectedNames) {
        paintingsName.shouldBe(CollectionCondition.size(expectedNames.length));

        for (int i = 0; i < expectedNames.length; i++) {
            paintingsName.get(i).shouldHave(text(expectedNames[i]));
        }
        return this;
    }

    @Step("Нажать на кнопку 'Редактировать'")
    public ArtistPage clickEditArtist() {
        editArtist.click();
        return this;
    }

    @Step("Проверить оторажения описания художника")
    public ArtistPage checkArtistDescription(String text) {
        descriptionArtist.shouldBe(visible).shouldHave(text(text));
        return this;
    }
}
