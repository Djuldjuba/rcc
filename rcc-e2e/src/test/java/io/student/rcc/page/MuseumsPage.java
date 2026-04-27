package io.student.rcc.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;

public class MuseumsPage extends BasePage {
    private final SelenideElement addMuseumBtn = $x("//button[.='Добавить музей']");
    private final SelenideElement modalFormAddMuseum =
            $x(".//button[.='Добавить музей']/ancestor::*[contains(@class, 'overflow-hidden')]//form");
    private final SelenideElement closeModalBtn = $x("//button[.='Закрыть']");

    public MuseumsPage clickAddMuseum() {
        addMuseumBtn.click();
        return this;
    }

    public MuseumsPage shouldVisibleMuseumModal() {
        modalFormAddMuseum.shouldBe(visible);
        return this;
    }

    public MuseumsPage closeModal() {
        closeModalBtn.click();
        return this;
    }
}
