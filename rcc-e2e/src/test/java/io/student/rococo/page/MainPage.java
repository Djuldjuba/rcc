package io.student.rococo.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class MainPage extends BasePage {

    private final SelenideElement mainTitle = $("#page h1");
    private final SelenideElement paintings = $("#page-content [href='/painting']");
    private final SelenideElement artists = $("#page-content [href='/artist']");
    private final SelenideElement museums = $("#page-content [href='/museum']");

    @Step("Проверка что выполнен успешный вход")
    public MainPage checkThatUserIsAuthorized() {
        mainTitle.shouldHave(text("Ваши любимые картины и художники всегда рядом"));
        return this;
    }

    @Step("Нажать на плашку 'Картины'")
    public PaintingPage clickPaintings() {
        paintings.click();
        return new PaintingPage();
    }

    @Step("Нажать на плашку 'Художники'")
    public ArtistPage clickArtists() {
        artists.click();
        return new ArtistPage();
    }

    @Step("Нажать на плашку 'Музеи'")
    public MuseumPage clickMuseums() {
        museums.click();
        return new MuseumPage();
    }
}
