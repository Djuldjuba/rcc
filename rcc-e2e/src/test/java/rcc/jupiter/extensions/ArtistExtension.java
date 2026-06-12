package rcc.jupiter.extensions;

import rcc.jupiter.annotation.Artist;
import rcc.model.ArtistJson;
import rcc.service.ArtistClient;
import rcc.service.ArtistDbClient;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import rcc.utils.RandomDataUtils;

public class ArtistExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(ArtistExtension.class);

    private final ArtistClient artistClient = new ArtistDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                Artist.class
        ).ifPresent(annotation -> {
            String name = annotation.name().isEmpty()
                    ? "Artist_" + RandomDataUtils.randomUserName()
                    : annotation.name();
            String biography = annotation.biography().isEmpty()
                    ? "Biography for " + name
                    : annotation.biography();

            ArtistJson artist = new ArtistJson(
                    null,
                    name,
                    biography,
                    null
            );

            ArtistJson createdArtist = artistClient.createArtist(artist);

            context.getStore(NAMESPACE).put(context.getUniqueId(), createdArtist);

            TestDataExtension.addArtist(context, createdArtist);
        });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> paramType = parameterContext.getParameter().getType();
        return paramType.equals(ArtistJson.class) &&
                AnnotationSupport.isAnnotated(extensionContext.getRequiredTestMethod(), Artist.class);
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext,
                                             ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId(), ArtistJson.class);
    }
}