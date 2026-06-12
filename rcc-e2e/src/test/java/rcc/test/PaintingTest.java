package rcc.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import rcc.jupiter.TestData;
import rcc.jupiter.annotation.Artist;
import rcc.jupiter.annotation.Museum;
import rcc.jupiter.annotation.Painting;
import rcc.jupiter.annotation.User;
import rcc.jupiter.extensions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({TestDataExtension.class, UserExtension.class, MuseumExtension.class, ArtistExtension.class, PaintingExtension.class})
public class PaintingTest {

    @Test
    @User(username = "barsik321", password = "pass123")
    @Museum(title = "Лувр", city = "Санкт-Петербург", countryName = "Франция")
    @Artist(name = "Да Винчи", biography = "Великий художник")
    @Painting(title = "Мона Лиза", description = "Одна из самых известных картин")
    void shouldCreateAllEntitiesAndPainting(TestData testData) {
        assertNotNull(testData.user());
        assertEquals("barsik321", testData.user().username());

        assertNotNull(testData.museum());
        assertEquals("Лувр", testData.museum().title());

        assertNotNull(testData.artist());
        assertEquals("Да Винчи", testData.artist().name());

        assertNotNull(testData.painting());
        assertEquals("Мона Лиза", testData.painting().title());
    }
}
