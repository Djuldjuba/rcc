package io.student.rococo.jupiter.extensions;

import io.student.rococo.jupiter.TestDataPresets;
import io.student.rococo.jupiter.annotation.Artist;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.model.ArtistJson;
import io.student.rococo.service.ArtistClient;
import io.student.rococo.service.impl.ArtistDbClient;
import io.student.rococo.utils.RandomDataUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ArtistExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(ArtistExtension.class);

    private static final String CREATED_ARTISTS_KEY = "created_artists";

    private final ArtistClient artistClient = new ArtistDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), Artist.class)
                .ifPresent(annotation -> createArtist(context, annotation));

        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnnotation -> {
                    for (Artist artistAnnotation : userAnnotation.artists()) {
                        createArtist(context, artistAnnotation);
                    }
                });
    }

    private void createArtist(ExtensionContext context, Artist annotation) {
        String name;
        String biography;
        String photoPath;

        if (!annotation.preset().isBlank()) {
            switch (annotation.preset().toLowerCase()) {
                case TestDataPresets.LEVITAN -> {
                    name = TestDataPresets.LEVITAN_ARTIST_NAME;
                    biography = TestDataPresets.LEVITAN_ARTIST_BIOGRAPHY;
                    photoPath = TestDataPresets.LEVITAN_ARTIST_PHOTO;
                }
                case TestDataPresets.LOUVRE -> {
                    name = TestDataPresets.LOUVRE_ARTIST_NAME;
                    biography = TestDataPresets.LOUVRE_ARTIST_BIOGRAPHY;
                    photoPath = TestDataPresets.LOUVRE_ARTIST_PHOTO;
                }
                default -> throw new IllegalArgumentException(
                        "Unknown artist preset: '" + annotation.preset() + "'. " +
                                "Available: " + TestDataPresets.LEVITAN + ", " + TestDataPresets.LOUVRE);
            }
        } else {
            name = annotation.name().isEmpty()
                    ? "Artist_" + RandomDataUtils.randomUserName()
                    : annotation.name();

            biography = annotation.biography().isEmpty()
                    ? "Biography for " + name
                    : annotation.biography();

            photoPath = annotation.photo();
        }

        deleteIfExistsByName(name);

        String photo = null;
        if (photoPath != null && !photoPath.isBlank()) {
            photo = loadPhotoAsBase64(photoPath);
        }

        ArtistJson artist = new ArtistJson(
                null,
                name,
                biography,
                photo
        );

        ArtistJson createdArtist = artistClient.createArtist(artist);

        context.getStore(NAMESPACE).put(context.getUniqueId(), createdArtist);
        TestDataExtension.addArtist(context, createdArtist);

        @SuppressWarnings("unchecked")
        List<ArtistJson> created = context.getStore(NAMESPACE)
                .getOrDefault(context.getUniqueId() + "_" + CREATED_ARTISTS_KEY, List.class, new ArrayList<>());
        created.add(createdArtist);
        context.getStore(NAMESPACE).put(context.getUniqueId() + "_" + CREATED_ARTISTS_KEY, created);
    }

    private void deleteIfExistsByName(String name) {
        try {
            artistClient.findArtistByName(name).ifPresent(artist -> {
                try {
                    artistClient.deleteArtist(artist.id());
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        @SuppressWarnings("unchecked")
        List<ArtistJson> createdArtists = context.getStore(NAMESPACE)
                .remove(context.getUniqueId() + "_" + CREATED_ARTISTS_KEY, List.class);

        if (createdArtists != null) {
            for (ArtistJson createdArtist : createdArtists) {
                if (createdArtist != null && createdArtist.id() != null) {
                    try {
                        artistClient.deleteArtist(createdArtist.id());
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        context.getStore(NAMESPACE).remove(context.getUniqueId());
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        if (!parameterContext.getParameter().getType().equals(ArtistJson.class)) {
            return false;
        }

        if (AnnotationSupport.isAnnotated(extensionContext.getRequiredTestMethod(), Artist.class)) {
            return true;
        }

        return AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), User.class)
                .map(user -> user.artists().length > 0)
                .orElse(false);
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext,
                                             ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE)
                .get(extensionContext.getUniqueId(), ArtistJson.class);
    }

    private static String loadPhotoAsBase64(String classpathPath) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(classpathPath)) {

            if (is == null) {
                throw new IllegalArgumentException("Фото не найдено: " + classpathPath);
            }

            byte[] bytes = is.readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            String mimeType = classpathPath.toLowerCase().endsWith(".png")
                    ? "image/png"
                    : "image/jpeg";

            return "data:" + mimeType + ";base64," + base64;

        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить фото: " + classpathPath, e);
        }
    }
}