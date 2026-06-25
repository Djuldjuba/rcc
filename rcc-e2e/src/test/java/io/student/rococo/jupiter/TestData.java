package io.student.rococo.jupiter;

import io.student.rococo.model.ArtistJson;
import io.student.rococo.model.MuseumJson;
import io.student.rococo.model.PaintingJson;
import io.student.rococo.model.UserJson;

public record TestData(
        UserJson user,
        MuseumJson museum,
        ArtistJson artist,
        PaintingJson painting,
        String password
) {}
