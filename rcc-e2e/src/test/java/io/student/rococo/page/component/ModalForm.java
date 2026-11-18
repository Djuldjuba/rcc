package io.student.rococo.page.component;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

public class ModalForm extends BaseComponent<ModalForm> {

    private final SelenideElement title = self.$("header");
    private final SelenideElement description = self.$("article");
    private final SelenideElement closeBtn = self.$("button[type='button']");
    private final SelenideElement submitBtn = self.$("button[type='submit'], button.variant-filled-primary");

    public ModalForm() {
        super($("[data-testid='modal-component']"));
    }

    @Step("Проверить заголовок модального окна: '{expectedTitle}'")
    public ModalForm checkTitle(String expectedTitle) {
        title.shouldHave(Condition.text(expectedTitle));
        return this;
    }

    @Step("Проверить описание модального окна: '{expectedDescription}'")
    public ModalForm checkDescription(String expectedDescription) {
        description.shouldHave(Condition.text(expectedDescription));
        return this;
    }

    @Step("Закрыть модальное окно")
    public ModalForm close() {
        closeBtn.click();
        return this;
    }

    @Step("Нажать 'Добавить' в модальном окне")
    public ModalForm submit() {
        submitBtn.click();
        return this;
    }

    @Step("Проверить, что модальное окно закрыто")
    public ModalForm checkClosed() {
        closeBtn.shouldNotBe(Condition.visible);
        return this;
    }

    public SelenideElement field(String cssSelector) {
        return self.$(cssSelector);
    }
}