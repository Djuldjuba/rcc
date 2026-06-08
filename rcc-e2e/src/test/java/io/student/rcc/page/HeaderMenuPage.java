package io.student.rcc.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$x;

public class HeaderMenuPage {

    private final SelenideElement loginBtn = $x("//button[.='Войти']");
    private final SelenideElement paintingsBtn = $x("//*[@data-testid='app-bar']//a[.='Картины']");
    private final SelenideElement artistsBtn = $x("//*[@data-testid='app-bar']//a[.='Художники']");
    private final SelenideElement museumsBtn = $x("//*[@data-testid='app-bar']//a[.='Музеи']");

    public LoginPage clickLoginButton() {
        loginBtn.click();
        return new LoginPage();
    }

    public PaintingsPage clickPaintings() {
        paintingsBtn.click();
        return new PaintingsPage();
    }

    public ArtistsPage clickArtists() {
        artistsBtn.click();
        return new ArtistsPage();
    }

    public MuseumsPage clickMuseums() {
        museumsBtn.click();
        return new MuseumsPage();
    }
}
