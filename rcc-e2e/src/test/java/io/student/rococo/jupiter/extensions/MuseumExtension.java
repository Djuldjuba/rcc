package io.student.rococo.jupiter.extensions;

import io.student.rococo.jupiter.annotation.Museum;
import io.student.rococo.model.CountryJson;
import io.student.rococo.model.MuseumJson;
import io.student.rococo.service.impl.MuseumDbClient;
import io.student.rococo.utils.RandomDataUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

public class MuseumExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(MuseumExtension.class);

    private final MuseumDbClient museumClient = new MuseumDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                Museum.class
        ).ifPresent(annotation -> {
            try {
                String countryName = annotation.countryName().isEmpty()
                        ? "Россия"
                        : annotation.countryName();

                CountryJson country = museumClient.findCountryByName(countryName)
                        .orElseThrow(() -> new RuntimeException(
                                "Страна '" + countryName + "' не найдена в базе данных"
                        ));

                String title = annotation.title().isEmpty()
                        ? "Museum_" + RandomDataUtils.randomUserName()
                        : annotation.title();
                String description = annotation.description().isEmpty()
                        ? "Description for " + title
                        : annotation.description();
                String city = annotation.city().isEmpty()
                        ? RandomDataUtils.randomCity()
                        : annotation.city();

                MuseumJson museum = new MuseumJson(
                        null,
                        title,
                        description,
                        city,
                        annotation.photo().isEmpty() ? null : annotation.photo(),
                        country
                );

                MuseumJson createdMuseum = museumClient.createMuseum(museum);

                context.getStore(NAMESPACE).put(context.getUniqueId(), createdMuseum);
                TestDataExtension.addMuseum(context, createdMuseum);

            } catch (Exception e) {
                throw new RuntimeException("Failed to create museum for test", e);
            }
        });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> paramType = parameterContext.getParameter().getType();
        return paramType.equals(MuseumJson.class) &&
                AnnotationSupport.isAnnotated(extensionContext.getRequiredTestMethod(), Museum.class);
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext,
                                             ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId(), MuseumJson.class);
    }
}