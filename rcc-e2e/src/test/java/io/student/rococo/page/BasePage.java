package io.student.rococo.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import io.student.rococo.config.Config;
import io.student.rococo.page.component.Header;
import io.student.rococo.page.component.ModalForm;
import io.student.rococo.page.component.SearchField;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

public abstract class BasePage<T extends BasePage<?>> {

    protected static final Config CFG = Config.getInstance();

    protected final Header header = new Header();
    protected final SearchField searchField = new SearchField();
    protected final ModalForm modal = new ModalForm();

    private final SelenideElement notification = $("div.text-base");
    private final SelenideElement notificationCloseBtn = $("[aria-label='Dismiss toast']");
    protected final SelenideElement content = $("#page-content");

    @Step("Нажать на таб в хедере 'Картины'")
    public PaintingPage clickPaintingsTab() {
        return header.clickPaintingsTab();
    }

    @Step("Нажать на таб в хедере 'Художники'")
    public ArtistPage clickArtistsTab() {
        return header.clickArtistsTab();
    }

    @Step("Нажать на таб в хедере 'Музеи'")
    public MuseumPage clickMuseumsTab() {
        return header.clickMuseumsTab();
    }

    @Step("Нажать на иконку Rococo")
    public MainPage clickOnRococoIcon() {
        return header.clickOnRococoIcon();
    }

    @Step("Нажать на кнопку 'Войти'")
    public LoginPage clickLoginButton() {
        return header.clickLoginButton();
    }

    @Step("Проверить отображение кнопки 'Войти'")
    public LoginPage checkLoginButtonIdDisplayed() {
        return header.checkLoginButtonIsDisplayed();
    }

    @Step("Нажать на иконку профиля")
    public ProfilePage clickProfileIconBtn() {
        return header.clickProfileIconBtn();
    }

    @Step("Нажать на свитчер смены темы")
    public MainPage switchTheme() {
        return header.switchTheme();
    }

    @SuppressWarnings("unchecked")
    @Step("Проверка уведомления")
    public T checkNotification(String text) {
        notification.shouldHave(text(text));
        notificationCloseBtn.click();
        return (T) this;
    }

    @Step("Выполнить поиск художника по запросу: '{value}'")
    public ArtistPage searchArtist(String value) {
        return searchField.searchArtist(value);
    }

    @Step("Выполнить поиск картины по запросу: '{value}'")
    public PaintingPage searchPainting(String value) {
        return searchField.searchPainting(value);
    }

    @Step("Выполнить поиск музея по запросу: '{value}'")
    public MuseumPage searchMuseum(String value) {
        return searchField.searchMuseum(value);
    }

    @Step("Проверка фото профиля в хедере")
    public MainPage checkPhotoInHeader(BufferedImage expected) throws IOException {
        return header.checkPhotoInHeader(expected);
    }
}