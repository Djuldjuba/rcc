package io.student.rcc.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;

public class PaintingsPage extends BasePage {

    private final SelenideElement addPaintingBtn = $x("//button[.='Добавить картину']");
    private final SelenideElement modalFormAddPainting =
            $x(".//button[.='Добавить картину']/ancestor::*[contains(@class, 'overflow-hidden')]//form");
    private final SelenideElement closeModalBtn = $x("//button[.='Закрыть']");

    public PaintingsPage clickAddPainting() {
        addPaintingBtn.click();
        return this;
    }

    public PaintingsPage shouldVisiblePaintingModal() {
        modalFormAddPainting.shouldBe(visible);
        return this;
    }

    public PaintingsPage closeModal() {
        closeModalBtn.click();
        return this;
    }
}
