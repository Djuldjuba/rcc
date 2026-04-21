package io.student.rcc.page;

public abstract class BasePage {
    protected final HeaderMenuPage headerMenuPage;

    public BasePage() {
        this.headerMenuPage = new HeaderMenuPage();
    }

    public PaintingsPage clickPaintings() {
        return headerMenuPage.clickPaintings();
    }

    public ArtistsPage clickArtists() {
        return headerMenuPage.clickArtists();
    }

    public MuseumsPage clickMuseums() {
        return headerMenuPage.clickMuseums();
    }

    public LoginPage clickLoginButton() {
        return headerMenuPage.clickLoginButton();
    }

}
