package io.student.rococo.jupiter;

import io.student.rococo.model.ArtistJson;
import io.student.rococo.model.MuseumJson;
import io.student.rococo.model.PaintingJson;
import io.student.rococo.model.UserJson;

import java.util.List;

public record TestData(
        UserJson user,
        List<MuseumJson> museums,
        List<ArtistJson> artists,
        List<PaintingJson> paintings,
        String password
) {
    public MuseumJson museum() {
        return museums != null && !museums.isEmpty() ? museums.get(0) : null;
    }

    public ArtistJson artist() {
        return artists != null && !artists.isEmpty() ? artists.get(0) : null;
    }

    public PaintingJson painting() {
        return paintings != null && !paintings.isEmpty() ? paintings.get(0) : null;
    }
}