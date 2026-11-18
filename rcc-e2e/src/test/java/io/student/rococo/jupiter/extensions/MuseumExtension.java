package io.student.rococo.jupiter.extensions;

import io.student.rococo.jupiter.TestDataPresets;
import io.student.rococo.jupiter.annotation.Museum;
import io.student.rococo.jupiter.annotation.User;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

public class MuseumExtension implements BeforeEachCallback, ParameterResolver {

    private static final Logger LOG = LoggerFactory.getLogger(MuseumExtension.class);

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(MuseumExtension.class);

    private static final String DEFAULT_PHOTO_PATH = "files/museums/default_museum_photo.jpg";
    private static final Base64.Encoder encoder = Base64.getEncoder();

    private final MuseumDbClient museumClient = new MuseumDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                Museum.class
        ).ifPresent(annotation -> createMuseum(context, annotation));

        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(userAnnotation -> {
            Museum[] museums = userAnnotation.museums();
            if (museums.length > 0) {
                for (Museum museumAnnotation : museums) {
                    createMuseum(context, museumAnnotation);
                }
                LOG.info("Created {} museums for test: {}", museums.length, context.getDisplayName());
            }
        });
    }

    private void createMuseum(ExtensionContext context, Museum annotation) {
        try {
            String title;
            String description;
            String city;
            String countryName;
            String photoPath;

            if (!annotation.preset().isBlank()) {
                switch (annotation.preset().toLowerCase()) {
                    case TestDataPresets.LEVITAN -> {
                        title = TestDataPresets.LEVITAN_MUSEUM_TITLE;
                        description = TestDataPresets.LEVITAN_MUSEUM_DESCRIPTION;
                        city = TestDataPresets.LEVITAN_MUSEUM_CITY;
                        countryName = TestDataPresets.LEVITAN_MUSEUM_COUNTRY;
                        photoPath = TestDataPresets.LEVITAN_MUSEUM_PHOTO;
                    }
                    case TestDataPresets.LOUVRE -> {
                        title = TestDataPresets.LOUVRE_MUSEUM_TITLE;
                        description = TestDataPresets.LOUVRE_MUSEUM_DESCRIPTION;
                        city = TestDataPresets.LOUVRE_MUSEUM_CITY;
                        countryName = TestDataPresets.LOUVRE_MUSEUM_COUNTRY;
                        photoPath = TestDataPresets.LOUVRE_MUSEUM_PHOTO;
                    }
                    default -> throw new IllegalArgumentException(
                            "Unknown museum preset: '" + annotation.preset() + "'. " +
                                    "Available: " + TestDataPresets.LEVITAN + ", " + TestDataPresets.LOUVRE);
                }
            } else {
                title = annotation.title().isEmpty()
                        ? "Museum_" + RandomDataUtils.randomUserName()
                        : annotation.title();
                description = annotation.description().isEmpty()
                        ? "Description for " + title
                        : annotation.description();
                city = annotation.city().isEmpty()
                        ? RandomDataUtils.randomCity()
                        : annotation.city();
                countryName = annotation.countryName().isEmpty() ? null : annotation.countryName();
                photoPath = annotation.photo();
            }

            deleteIfExistsByTitle(title);

            CountryJson country;
            if (countryName != null && !countryName.isBlank()) {
                country = museumClient.findCountryByName(countryName)
                        .orElseThrow(() -> new RuntimeException(
                                "Страна '" + countryName + "' не найдена в базе данных"
                        ));
            } else {
                List<CountryJson> allCountries = museumClient.getAllCountries();
                if (allCountries.isEmpty()) {
                    throw new RuntimeException("Нет доступных стран в базе данных");
                }
                country = allCountries.get(RandomDataUtils.randomInt(0, allCountries.size() - 1));
            }

            String photo = null;
            if (photoPath != null && !photoPath.isEmpty()) {
                photo = loadPhotoFromResource(photoPath);
            }

            if (photo == null) {
                photo = loadDefaultPhoto();
            }

            MuseumJson museum = new MuseumJson(
                    null,
                    title,
                    description,
                    city,
                    photo,
                    country
            );

            MuseumJson createdMuseum = museumClient.createMuseum(museum);

            context.getStore(NAMESPACE).put(context.getUniqueId(), createdMuseum);
            TestDataExtension.addMuseum(context, createdMuseum);

            LOG.info("Museum '{}' created with country '{}'", title, country.name());

        } catch (Exception e) {
            throw new RuntimeException("Failed to create museum for test", e);
        }
    }

    private void deleteIfExistsByTitle(String title) {
        try {
            boolean deleted = museumClient.deleteMuseumByTitle(title);
            if (deleted) {
                LOG.info("Deleted existing museum: {}", title);
            }
        } catch (Exception e) {
            LOG.warn("deleteMuseumByTitle failed for '{}': {}", title, e.getMessage());
        }
    }

    private String loadPhotoFromResource(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return null;
        }

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                LOG.warn("Photo resource not found: {}", resourcePath);
                return null;
            }

            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                LOG.warn("Failed to read image from: {}", resourcePath);
                return null;
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                String format = getImageFormat(resourcePath);
                ImageIO.write(image, format, baos);
                byte[] bytes = baos.toByteArray();

                String base64 = encoder.encodeToString(bytes);
                String mimeType = getMimeType(resourcePath);
                return "data:" + mimeType + ";base64," + base64;
            }

        } catch (IOException e) {
            LOG.error("Failed to load photo: {}", resourcePath, e);
            return null;
        }
    }

    private String getImageFormat(String filePath) {
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "jpg";
        } else if (filePath.endsWith(".png")) {
            return "png";
        } else if (filePath.endsWith(".gif")) {
            return "gif";
        } else if (filePath.endsWith(".webp")) {
            return "webp";
        }
        return "jpg";
    }

    private String getMimeType(String filePath) {
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.endsWith(".png")) {
            return "image/png";
        } else if (filePath.endsWith(".gif")) {
            return "image/gif";
        } else if (filePath.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private String loadDefaultPhoto() {
        String photo = loadPhotoFromResource(DEFAULT_PHOTO_PATH);
        if (photo == null) {
            LOG.warn("Default photo not found at: {}", DEFAULT_PHOTO_PATH);
        }
        return photo;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> paramType = parameterContext.getParameter().getType();
        return paramType.equals(MuseumJson.class) &&
                (AnnotationSupport.isAnnotated(extensionContext.getRequiredTestMethod(), Museum.class) ||
                        AnnotationSupport.isAnnotated(extensionContext.getRequiredTestMethod(), User.class));
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext,
                                             ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE)
                .get(extensionContext.getUniqueId(), MuseumJson.class);
    }
}