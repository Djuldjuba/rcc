package io.student.rococo.data.mapper.extractor;

import io.student.rococo.data.entity.ArtistEntity;
import io.student.rococo.data.entity.MuseumEntity;
import io.student.rococo.data.entity.PaintingEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PaintingResultSetExtractor implements ResultSetExtractor<List<PaintingEntity>> {

    public static final PaintingResultSetExtractor instance = new PaintingResultSetExtractor();

    private PaintingResultSetExtractor() {
    }

    @Override
    public List<PaintingEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, PaintingEntity> paintingMap = new LinkedHashMap<>();

        while (rs.next()) {
            UUID paintingId = UUID.fromString(rs.getString("painting_id"));
            PaintingEntity painting = paintingMap.get(paintingId);

            if (painting == null) {
                painting = new PaintingEntity();
                painting.setId(paintingId);
                painting.setTitle(rs.getString("title"));
                painting.setDescription(rs.getString("description"));
                painting.setContent(rs.getBytes("content"));

                String artistId = rs.getString("artist_id");
                if (artistId != null && !rs.wasNull()) {
                    ArtistEntity artist = new ArtistEntity();
                    artist.setId(UUID.fromString(artistId));
                    artist.setName(rs.getString("artist_name"));
                    artist.setBiography(rs.getString("artist_biography"));
                    artist.setPhoto(rs.getBytes("artist_photo"));
                    painting.setArtist(artist);
                }

                String museumId = rs.getString("museum_id");
                if (museumId != null && !rs.wasNull()) {
                    MuseumEntity museum = new MuseumEntity();
                    museum.setId(UUID.fromString(museumId));
                    museum.setTitle(rs.getString("museum_title"));
                    museum.setDescription(rs.getString("museum_description"));
                    museum.setCity(rs.getString("museum_city"));
                    museum.setPhoto(rs.getBytes("museum_photo"));
                    painting.setMuseum(museum);
                }

                paintingMap.put(paintingId, painting);
            }
        }

        return new ArrayList<>(paintingMap.values());
    }
}
