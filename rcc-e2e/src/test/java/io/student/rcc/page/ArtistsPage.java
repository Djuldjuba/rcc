package io.student.rcc.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class ArtistsPage extends BasePage {
    private final SelenideElement addArtistBtn = $(byText("Добавить художника"));
    private final SelenideElement modalFormAddArtist =
            addArtistBtn.$x("ancestor::*[contains(@class, 'overflow-hidden')]//form");
    private final SelenideElement closeModalBtn = $x("//button[.='Закрыть']");

    public ArtistsPage clickAddArtist() {
        addArtistBtn.click();
        return this;
    }

    public ArtistsPage shouldDisplayArtistModal() {
        modalFormAddArtist.shouldBe(visible);
        return this;
    }

    public ArtistsPage closeModal() {
        closeModalBtn.click();
        return this;
    }
}
