package io.student.rococo.data.mapper.extractor;

import io.student.rococo.data.entity.ArtistEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArtistResultSetExtractor implements ResultSetExtractor<List<ArtistEntity>> {

    public static final ArtistResultSetExtractor instance = new ArtistResultSetExtractor();

    private ArtistResultSetExtractor() {
    }

    @Override
    public List<ArtistEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, ArtistEntity> artistMap = new LinkedHashMap<>();

        while (rs.next()) {
            UUID artistId = UUID.fromString(rs.getString("artist_id"));
            ArtistEntity artist = artistMap.get(artistId);

            if (artist == null) {
                artist = new ArtistEntity();
                artist.setId(artistId);
                artist.setName(rs.getString("name"));
                artist.setBiography(rs.getString("biography"));
                artist.setPhoto(rs.getBytes("photo"));
                artistMap.put(artistId, artist);
            }
        }

        return new ArrayList<>(artistMap.values());
    }
}