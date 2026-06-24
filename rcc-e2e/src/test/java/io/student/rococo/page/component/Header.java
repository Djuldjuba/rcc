package io.student.rococo.page.component;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import io.student.rococo.page.ArtistPage;
import io.student.rococo.page.LoginPage;
import io.student.rococo.page.MuseumPage;
import io.student.rococo.page.PaintingPage;
import io.student.rococo.page.ProfilePage;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class Header {

    private final SelenideElement loginBtn = $x("//button[.='Войти']");
    private final SelenideElement paintingsBtn = $x("//*[@data-testid='app-bar']//a[.='Картины']");
    private final SelenideElement artistsBtn = $x("//*[@data-testid='app-bar']//a[.='Художники']");
    private final SelenideElement museumsBtn = $x("//*[@data-testid='app-bar']//a[.='Музеи']");
    private final SelenideElement profileIconBtn = $(".avatar-image, .avatar-initials");
    private final SelenideElement lightSwitchBtn = $(".lightswitch-track");

    @Step("Нажать на кнопку 'Войти' в хедере")
    public LoginPage clickLoginButton() {
        loginBtn.click();
        return new LoginPage();
    }

    @Step("Нажать на таб 'Картины' в хедере")
    public PaintingPage clickPaintings() {
        paintingsBtn.click();
        return new PaintingPage();
    }

    @Step("Нажать на таб 'Художники' в хедере")
    public ArtistPage clickArtists() {
        artistsBtn.click();
        return new ArtistPage();
    }

    @Step("Нажать на таб 'Музеи' в хедере")
    public MuseumPage clickMuseums() {
        museumsBtn.click();
        return new MuseumPage();
    }

    @Step("Нажать на иконку профиля в хедере")
    public ProfilePage clickProfileIconBtn() {
        profileIconBtn.click();
        return new ProfilePage();
    }

    @Step("Переключить тему (светлая/темная)")
    public Header switchLight() {
        lightSwitchBtn.click();
        return this;
    }
}
