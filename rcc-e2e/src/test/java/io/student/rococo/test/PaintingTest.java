package io.student.rococo.test;

import io.student.rococo.jupiter.extensions.ArtistExtension;
import io.student.rococo.jupiter.extensions.MuseumExtension;
import io.student.rococo.jupiter.extensions.PaintingExtension;
import io.student.rococo.jupiter.extensions.TestDataExtension;
import io.student.rococo.jupiter.extensions.UserExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.annotation.Artist;
import io.student.rococo.jupiter.annotation.Museum;
import io.student.rococo.jupiter.annotation.Painting;
import io.student.rococo.jupiter.annotation.User;

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
