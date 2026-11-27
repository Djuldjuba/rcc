package io.student.rococo.page.component;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import io.student.rococo.page.ArtistPage;
import io.student.rococo.page.MuseumPage;
import io.student.rococo.page.PaintingPage;

import static com.codeborne.selenide.Selenide.$;

public class SearchField extends BaseComponent<SearchField> {

    public SearchField() {
        super($("[type=\"search\"]"));
    }

    private final SelenideElement searchField = self;
    private final SelenideElement searchBtn = self.parent().$("[type=\"button\"]");

    @Step("Выполнить поиск картины по запросу: '{value}'")
    public PaintingPage searchPainting(String value) {
        searchField.setValue(value);
        searchBtn.click();
        return new PaintingPage();
    }

    @Step("Выполнить поиск художника по запросу: '{value}'")
    public ArtistPage searchArtist(String value) {
        searchField.setValue(value);
        searchBtn.click();
        return new ArtistPage();
    }

    @Step("Выполнить поиск музея по запросу: '{value}'")
    public MuseumPage searchMuseum(String value) {
        searchField.setValue(value);
        searchBtn.click();
        return new MuseumPage();
    }
}
