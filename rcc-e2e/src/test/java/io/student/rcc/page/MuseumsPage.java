package io.student.rcc.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;

public class MuseumsPage extends BasePage {
    private final SelenideElement addMuseumBtn = $x("//button[.='Добавить музей']");
    private final SelenideElement modalFormAddMuseum = $x("//form");
    private final SelenideElement closeModalBtn = $x("//button[.='Закрыть']");

    public MuseumsPage clickAddMuseum() {
        addMuseumBtn.click();
        modalFormAddMuseum.shouldBe(visible);
        return new MuseumsPage();
    }

    public MuseumsPage closeModal() {
        closeModalBtn.click();
        return this;
    }
}
