package io.student.rococo.jupiter.extensions;

import io.student.rococo.jupiter.TestData;
import io.student.rococo.jupiter.TestDataPresets;
import io.student.rococo.jupiter.annotation.Painting;
import io.student.rococo.jupiter.annotation.User;
import io.student.rococo.model.ArtistJson;
import io.student.rococo.model.MuseumJson;
import io.student.rococo.model.PaintingJson;
import io.student.rococo.service.PaintingClient;
import io.student.rococo.service.impl.PaintingDbClient;
import io.student.rococo.utils.RandomDataUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PaintingExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(PaintingExtension.class);

    private static final String CREATED_PAINTINGS_KEY = "created_paintings";

    private final PaintingClient paintingClient = new PaintingDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), Painting.class)
                .ifPresent(annotation -> createPainting(context, annotation));

        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnnotation -> {
                    for (Painting paintingAnnotation : userAnnotation.paintings()) {
                        createPainting(context, paintingAnnotation);
                    }
                });
    }

    private void createPainting(ExtensionContext context, Painting annotation) {
        try {
            TestData testData = context.getStore(TestDataExtension.NAMESPACE)
                    .get(context.getUniqueId(), TestData.class);

            ArtistJson artist = testData.artist();
            if (artist == null) {
                throw new RuntimeException(
                        "Artist must be created before painting. " +
                                "Add @Artist annotation or artists = @Artist(...) inside @User.");
            }

            MuseumJson museum = testData.museum();
            if (museum == null) {
                throw new RuntimeException(
                        "Museum must be created before painting. " +
                                "Add @Museum annotation or museums = @Museum(...) inside @User.");
            }

            String title;
            String description;
            String contentPath;

            if (!annotation.preset().isBlank()) {
                switch (annotation.preset().toLowerCase()) {
                    case TestDataPresets.LEVITAN -> {
                        title = TestDataPresets.LEVITAN_PAINTING_TITLE;
                        description = TestDataPresets.LEVITAN_PAINTING_DESCRIPTION;
                        contentPath = TestDataPresets.LEVITAN_PAINTING_PHOTO;
                    }
                    case TestDataPresets.LOUVRE -> {
                        title = TestDataPresets.LOUVRE_PAINTING_TITLE;
                        description = TestDataPresets.LOUVRE_PAINTING_DESCRIPTION;
                        contentPath = TestDataPresets.LOUVRE_PAINTING_PHOTO;
                    }
                    default -> throw new IllegalArgumentException(
                            "Unknown painting preset: '" + annotation.preset() + "'. " +
                                    "Available: " + TestDataPresets.LEVITAN + ", " + TestDataPresets.LOUVRE);
                }
            } else {
                title = annotation.title().isEmpty()
                        ? "Painting_" + RandomDataUtils.randomUserName()
                        : annotation.title();

                description = annotation.description().isEmpty()
                        ? "Description for " + title
                        : annotation.description();

                contentPath = annotation.content();
            }

            deleteIfExistsByTitle(title);

            String content = null;
            if (contentPath != null && !contentPath.isBlank()) {
                content = loadPhotoAsBase64(contentPath);
            }

            PaintingJson painting = new PaintingJson(
                    null,
                    title,
                    description,
                    content,
                    artist,
                    museum
            );

            PaintingJson createdPainting = paintingClient.createPainting(painting);

            context.getStore(NAMESPACE).put(context.getUniqueId(), createdPainting);
            TestDataExtension.addPainting(context, createdPainting);

            @SuppressWarnings("unchecked")
            List<PaintingJson> created = context.getStore(NAMESPACE)
                    .getOrDefault(context.getUniqueId() + "_" + CREATED_PAINTINGS_KEY, List.class, new ArrayList<>());
            created.add(createdPainting);
            context.getStore(NAMESPACE).put(context.getUniqueId() + "_" + CREATED_PAINTINGS_KEY, created);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create painting for test", e);
        }
    }

    private void deleteIfExistsByTitle(String title) {
        try {
            paintingClient.findPaintingByTitle(title).ifPresent(painting -> {
                try {
                    paintingClient.deletePainting(painting.id());
                } catch (Exception e) {
                    System.err.println("[PaintingExtension] Failed to delete existing painting '" + title + "': " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[PaintingExtension] findPaintingByTitle failed: " + e.getMessage());
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        @SuppressWarnings("unchecked")
        List<PaintingJson> createdPaintings = context.getStore(NAMESPACE)
                .remove(context.getUniqueId() + "_" + CREATED_PAINTINGS_KEY, List.class);

        if (createdPaintings != null) {
            for (PaintingJson createdPainting : createdPaintings) {
                if (createdPainting != null && createdPainting.id() != null) {
                    try {
                        paintingClient.deletePainting(createdPainting.id());
                    } catch (Exception e) {
                        System.err.println("[PaintingExtension] Failed to delete painting "
                                + createdPainting.id() + ": " + e.getMessage());
                    }
                }
            }
        }

        context.getStore(NAMESPACE).remove(context.getUniqueId());
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        if (!parameterContext.getParameter().getType().equals(PaintingJson.class)) {
            return false;
        }

        if (AnnotationSupport.isAnnotated(extensionContext.getRequiredTestMethod(), Painting.class)) {
            return true;
        }

        return AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), User.class)
                .map(user -> user.paintings().length > 0)
                .orElse(false);
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext,
                                             ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE)
                .get(extensionContext.getUniqueId(), PaintingJson.class);
    }

    private static String loadPhotoAsBase64(String classpathPath) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(classpathPath)) {

            if (is == null) {
                throw new IllegalArgumentException("Фото не найдено по пути: " + classpathPath);
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