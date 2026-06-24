package io.student.rococo.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import io.student.rococo.page.component.Header;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public abstract class BasePage<T extends BasePage<?>> {
    protected final Header header;
    private final SelenideElement profileUpdatedText = $("div.text-base");

    public BasePage() {
        this.header = new Header();
    }

    @Step("Нажать на таб в хедере 'Картины'")
    public PaintingPage clickPaintings() {
        return header.clickPaintings();
    }

    @Step("Нажать на таб в хедере 'Художники'")
    public ArtistPage clickArtists() {
        return header.clickArtists();
    }

    @Step("Нажать на таб в хедере 'Музеи'")
    public MuseumPage clickMuseums() {
        return header.clickMuseums();
    }

    @Step("Нажать на кнопку 'Войти'")
    public LoginPage clickLoginButton() {
        return header.clickLoginButton();
    }

    @Step("Нажать на иконку профиля")
    public ProfilePage clickProfileIconBtn() {
        return header.clickProfileIconBtn();
    }

    @Step("Нажать на свитчер смены темы")
    public Header switchLight() {
        return header.switchLight();
    }

    @Step("Проверка уведомления")
    public T checkThatProfileUpdated(String text) {
        profileUpdatedText.shouldHave(text(text));
        return (T) this;
    }
}
