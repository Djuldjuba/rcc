package rcc.jupiter;

import rcc.model.ArtistJson;
import rcc.model.MuseumJson;
import rcc.model.PaintingJson;
import rcc.model.UserJson;

public record TestData(
        UserJson user,
        MuseumJson museum,
        ArtistJson artist,
        PaintingJson painting,
        String password
) {}
