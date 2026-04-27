package io.student.rcc.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;

public class ArtistsPage extends BasePage {
    private final SelenideElement addArtistsBtn = $x("//button[.='Добавить художника']");
    private final SelenideElement modalFormAddArtists =
            $x(".//button[.='Добавить художника']/ancestor::*[contains(@class, 'overflow-hidden')]//form");
    private final SelenideElement closeModalBtn = $x("//button[.='Закрыть']");

    public ArtistsPage clickAddArtists() {
        addArtistsBtn.click();
        return this;
    }

    public ArtistsPage shouldVisibleArtistsModal() {
        modalFormAddArtists.shouldBe(visible);
        return this;
    }

    public ArtistsPage closeModal() {
        closeModalBtn.click();
        return this;
    }
}
