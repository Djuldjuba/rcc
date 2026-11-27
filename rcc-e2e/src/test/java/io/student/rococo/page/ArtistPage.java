package io.student.rococo.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import io.student.rococo.jupiter.extensions.ScreenShotTestExtension;
import io.student.rococo.utils.ScreenDiffResult;
import org.junit.jupiter.api.Assertions;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.sleep;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ArtistPage extends BasePage<ArtistPage> {

    // main
    private final SelenideElement titleArtists = content.$("h2");
    private final SelenideElement addArtistBtn = content.$("button.btn");
    private final SelenideElement addArtistBtnIfNoPaintings = content.$("section button.btn");
    private final ElementsCollection artistsNamesList = content.$$("li span");
    private final ElementsCollection artistsPhotosList = content.$$("li [data-testid='avatar']");

    // open artist card
    private final SelenideElement artistName = content.$("header");
    private final SelenideElement editArtist = content.$("[data-testid='edit-artist']");
    private final SelenideElement descriptionArtist = content.$("#page-content p");
    private final ElementsCollection paintingsNamesOfArtistList = content.$$("li div");
    private final ElementsCollection paintingsPhotosOfArtistList = content.$$("li img");
    private final SelenideElement photoArtist = content.$("[data-testid=avatar]");
    private final SelenideElement emptyArtistsList = content.$("section p");

    // modal form — специфичные поля
    private final SelenideElement inputName = modal.field("input[name='name']");
    private final SelenideElement nameFieldInputName = inputName.parent().$("span");
    private final SelenideElement errorUnderInputName = inputName.parent().$(".text-error-400");
    private final SelenideElement inputPhoto = modal.field("input[name='photo']");
    private final SelenideElement nameFieldInputPhoto = inputPhoto.parent().$("span");
    private final SelenideElement inputDescription = modal.field("textarea[name='biography']");
    private final SelenideElement nameFieldInputDescription = inputDescription.parent().$("span");
    private final SelenideElement errorUnderInputDescription = inputDescription.parent().$(".text-error-400");

    // no paintings
    private final SelenideElement textWithoutPaintings = content.$(".p-4 section p");
    private final SelenideElement addPaintingBtnWithoutPaintings = content.$(".p-4 section button");

    // if artists not found
    private final SelenideElement textArtistsNotFound1line = content.$("section p", 0);
    private final SelenideElement textArtistsNotFound2line = content.$("section p", 1);

    @Step("Проверить отображение заголовка 'Художники'")
    public ArtistPage checkTitleArtists() {
        titleArtists.shouldHave(text("Художники"));
        return this;
    }

    @Step("Нажать на кнопку 'Добавить художника'")
    public ArtistPage clickAddArtist() {
        addArtistBtn.click();
        return this;
    }

    @Step("Нажать на кнопку 'Добавить художника'")
    public ArtistPage clickAddArtistIfNoArtists() {
        addArtistBtnIfNoPaintings.click();
        return this;
    }

    @Step("Проверить что художник {name} присутствует на странице")
    public ArtistPage checkArtistIsDisplayed(String name) {
        sleep(1000);
        boolean found = false;
        for (SelenideElement element : artistsNamesList) {
            if (element.getText().equals(name)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        return this;
    }

    @Step("Проверить что художник {name} присутствует на странице и вернуть его аватар")
    public SelenideElement getAvatarForArtist(String name) {
        for (int i = 0; i < artistsNamesList.size(); i++) {
            if (artistsNamesList.get(i).getText().equals(name)) {
                SelenideElement avatar = artistsPhotosList.get(i);
                avatar.shouldBe(visible);
                return avatar;
            }
        }
        throw new AssertionError("Художник с именем '" + name + "' не найден");
    }

    @Step("Проверка отображения фотографии художника на вкладке 'Художники'")
    @Nonnull
    public ArtistPage checkPhoto(BufferedImage expected, String name) throws IOException {
        sleep(1000);
        BufferedImage actualImage = ImageIO.read(Objects.requireNonNull(getAvatarForArtist(name).screenshot()));
        assertFalse(
                new ScreenDiffResult(
                        actualImage, expected
                ),
                ScreenShotTestExtension.ASSERT_SCREEN_MESSAGE
        );
        return this;
    }

    @Step("Проверить что в карточке художника картина {name} присутствует на странице")
    public ArtistPage checkPaintingOfArtistIsDisplayed(String name) {
        sleep(5000);
        boolean found = false;
        for (SelenideElement element : paintingsNamesOfArtistList) {
            if (element.getText().equals(name)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        return this;
    }

    @Step("Проверить что в карточке художника картина {name} присутствует на странице и вернуть ее изображение")
    public SelenideElement getPaintingImageOfArtist(String name) {
        for (int i = 0; i < paintingsNamesOfArtistList.size(); i++) {
            if (paintingsNamesOfArtistList.get(i).getText().equals(name)) {
                SelenideElement avatar = paintingsPhotosOfArtistList.get(i);
                avatar.shouldBe(visible);
                return avatar;
            }
        }
        throw new AssertionError("У картины с название'" + name + "' не найдено изображение или некроректно");
    }

    @Step("Проверка отображения изображения картины на вкладке 'Художники'")
    @Nonnull
    public ArtistPage checkPaintingImageInArtistCard(BufferedImage expected, String name) throws IOException {
        SelenideElement paintingName = paintingsNamesOfArtistList.findBy(text(name));

        Selenide.executeJavaScript("arguments[0].scrollIntoView({block: 'center'});", paintingName);
        sleep(300);

        SelenideElement paintingImage = getPaintingImageOfArtist(name);

        BufferedImage actualImage = ImageIO.read(Objects.requireNonNull(paintingImage.screenshot()));
        assertFalse(
                new ScreenDiffResult(actualImage, expected),
                ScreenShotTestExtension.ASSERT_SCREEN_MESSAGE
        );
        return this;
    }

    @Step("Проверка отображения фотографии художника на вкладке 'Художники'")
    @Nonnull
    public ArtistPage checkPhotoArtistInArtistCard(BufferedImage expected) throws IOException {
        BufferedImage actualImage = ImageIO.read(Objects.requireNonNull(photoArtist.screenshot()));
        assertFalse(
                new ScreenDiffResult(actualImage, expected),
                ScreenShotTestExtension.ASSERT_SCREEN_MESSAGE
        );
        return this;
    }

    @Step("Кликнуть по названию картины {name}")
    public PaintingPage clickPainting(String name) {
        for (SelenideElement element : paintingsNamesOfArtistList) {
            if (element.getText().equals(name)) {
                element.shouldBe(visible).click();
                return new PaintingPage();
            }
        }
        return new PaintingPage();
    }

    @Step("Закрыть модальное окно 'Добавить художника'")
    public ArtistPage closeModal() {
        modal.close();
        return this;
    }

    @Step("Проверить что модальное окно 'Добавить художника' закрыто")
    public ArtistPage checkThatModalFormIsClose() {
        modal.checkClosed();
        return this;
    }

    @Step("Нажать на кнопку 'Добавить'")
    public ArtistPage clickAddBtn() {
        modal.submit();
        return this;
    }

    @Step("Проверить отображение названия модального окна {name}")
    public ArtistPage checkTitleModalForm(String name) {
        modal.checkTitle(name);
        return this;
    }

    @Step("Проверить отображение описания модального окна")
    public ArtistPage checkDescriptionModalFromAddArtist() {
        modal.checkDescription("Заполните форму, чтобы добавить нового художника");
        return this;
    }

    @Step("Проверить оторажения имени художника")
    public ArtistPage checkArtistName(String name) {
        artistName.shouldBe(visible).shouldHave(text(name));
        return this;
    }

    @Step("Нажать на кнопку 'Редактировать'")
    public ArtistPage clickEditArtist() {
        editArtist.click();
        return this;
    }

    @Step("Проверка отображения описания художника")
    public ArtistPage checkArtistDescription(String text) {
        descriptionArtist.shouldBe(visible).shouldHave(text(text));
        return this;
    }

    @Step("Ввести имя {nameArtist}")
    public ArtistPage inputName(String nameArtist) {
        inputName.clear();
        inputName.setValue(nameArtist);
        return this;
    }

    @Step("Проверить название поля 'Имя'")
    public ArtistPage checkNameInputName() {
        nameFieldInputName.shouldHave(text("Имя"));
        return this;
    }

    @Step("Проверить текст ошибки под полем 'Имя'")
    public ArtistPage checkErrorUnderInputName(String errorText) {
        errorUnderInputName.shouldHave(text(errorText));
        return this;
    }

    @Step("Добавить фото")
    public ArtistPage inputPhoto(String filePath) {
        inputPhoto.uploadFromClasspath(filePath);
        return this;
    }

    @Step("Проверить название поля с добавлением фото")
    public ArtistPage checkNameInputPhoto(String name) {
        nameFieldInputPhoto.shouldHave(text(name));
        return this;
    }

    @Step("Ввести описание {description}")
    public ArtistPage inputDescription(String description) {
        inputDescription.clear();
        inputDescription.setValue(description);
        return this;
    }

    @Step("Проверить название поля 'Биография'")
    public ArtistPage checkNameInputDescription() {
        nameFieldInputDescription.shouldHave(text("Биография"));
        return this;
    }

    @Step("Проверить текст ошибки под полем 'Биография'")
    public ArtistPage checkErrorUnderInputDescription(String errorText) {
        errorUnderInputDescription.shouldHave(text(errorText));
        return this;
    }

    @Step("Проверка текста не найден ни один художник")
    public ArtistPage checkTextIfArtistsNotFound() {
        textArtistsNotFound1line.shouldHave(text("Художники не найдены"));
        textArtistsNotFound2line.shouldHave(text("Для указанного вами фильтра мы не смогли найти художников"));
        return this;
    }

    @Step("Нажать на художника {artistName}")
    public ArtistPage clickOnArtist(String artistName) {
        artistsNamesList.shouldHave(sizeGreaterThan(0));
        for (SelenideElement element : artistsNamesList) {
            if (element.getText().equals(artistName)) {
                element.click();
                return this;
            }
        }
        throw new AssertionError("Художник с именем '" + artistName + "' не найден");
    }

    @Step("Проверка текста когда у художника не добавлено ни одной картины")
    public ArtistPage checkTextWithoutPaintings() {
        textWithoutPaintings.shouldHave(text("Пока что список картин этого художника пуст."));
        return this;
    }

    @Step("Нажать на кнопку 'Добавить картину' если у художника нет ни одной картины")
    public PaintingPage clickAddPaintingBtnWithoutPaintings() {
        addPaintingBtnWithoutPaintings.shouldHave(text("Добавить картину"));
        addPaintingBtnWithoutPaintings.click();
        return new PaintingPage();
    }

    @Step("Проверка что список художников пуст")
    public ArtistPage checkThatPaintingListEmpty() {
        emptyArtistsList
                .shouldBe(visible)
                .shouldHave(text("Пока что список художников пуст. Чтобы пополнить коллекцию, добавьте нового художника"));
        return this;
    }
}