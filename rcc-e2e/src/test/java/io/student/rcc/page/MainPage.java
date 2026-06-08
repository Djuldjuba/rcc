package io.student.rcc.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class MainPage extends BasePage {

    private final SelenideElement mainTitle = $("#page h1");
    private final SelenideElement avatarIcon = $("[data-testid='avatar']");

    public MainPage checkThatUserIsAuthorized() {
        avatarIcon.shouldBe(visible);
        mainTitle.shouldHave(text("Ваши любимые картины и художники всегда рядом"));
        return this;
    }
}
