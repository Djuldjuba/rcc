package rcc.jupiter.extensions;

import rcc.jupiter.TestData;
import rcc.jupiter.annotation.Painting;
import rcc.model.ArtistJson;
import rcc.model.CountryJson;
import rcc.model.MuseumJson;
import rcc.model.PaintingJson;
import rcc.service.MuseumClient;
import rcc.service.MuseumDbClient;
import rcc.service.PaintingClient;
import rcc.service.PaintingDbClient;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import rcc.utils.RandomDataUtils;

public class PaintingExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(PaintingExtension.class);

    private final PaintingClient paintingClient = new PaintingDbClient();
    private final MuseumClient museumClient = new MuseumDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                Painting.class
        ).ifPresent(annotation -> {
            try {
                TestData testData = context.getStore(TestDataExtension.NAMESPACE)
                        .get(context.getUniqueId(), TestData.class);

                ArtistJson artist = testData.artist();
                if (artist == null) {
                    throw new RuntimeException("Artist must be created before painting. Add @Artist annotation to the test method.");
                }

                MuseumJson museum = testData.museum();
                if (museum == null) {
                    CountryJson country = new CountryJson(null, "Россия");
                    museum = museumClient.createMuseum(new MuseumJson(
                            null,
                            "Museum_" + RandomDataUtils.randomUserName(),
                            "Default description for test museum",
                            "Test City",
                            null,
                            country
                    ));
                    TestDataExtension.addMuseum(context, museum);
                }

                String title = annotation.title().isEmpty()
                        ? "Painting_" + RandomDataUtils.randomUserName()
                        : annotation.title();
                String description = annotation.description().isEmpty()
                        ? "Description for " + title
                        : annotation.description();
                String content = annotation.content().isEmpty()
                        ? "Base64 encoded image content"
                        : annotation.content();

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

            } catch (Exception e) {
                throw new RuntimeException("Failed to create painting for test", e);
            }
        });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> paramType = parameterContext.getParameter().getType();
        return paramType.equals(PaintingJson.class) &&
                AnnotationSupport.isAnnotated(extensionContext.getRequiredTestMethod(), Painting.class);
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext,
                                             ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId(), PaintingJson.class);
    }
}