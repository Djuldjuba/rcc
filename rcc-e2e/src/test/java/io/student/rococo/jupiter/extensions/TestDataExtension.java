package io.student.rococo.jupiter.extensions;

import io.student.rococo.jupiter.TestData;
import io.student.rococo.model.ArtistJson;
import io.student.rococo.model.MuseumJson;
import io.student.rococo.model.PaintingJson;
import io.student.rococo.model.UserJson;
import io.student.rococo.service.impl.MuseumDbClient;
import io.student.rococo.service.impl.UsersDbClient;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TestDataExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataExtension.class);

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(TestDataExtension.class);

    private final MuseumDbClient museumClient = new MuseumDbClient();
    private final UsersDbClient usersDbClient = new UsersDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        TestData testData = new TestData(
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                null
        );
        context.getStore(NAMESPACE).put(context.getUniqueId(), testData);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        TestData testData = context.getStore(NAMESPACE).get(context.getUniqueId(), TestData.class);

        if (testData != null) {
            if (testData.museums() != null && !testData.museums().isEmpty()) {
                for (MuseumJson museum : testData.museums()) {
                    if (museum != null && museum.id() != null) {
                        try {
                            museumClient.deleteMuseumById(museum.id());
                            LOG.info("Museum '{}' deleted after test", museum.title());
                        } catch (Exception e) {
                            LOG.error("Failed to delete museum '{}'", museum.title(), e);
                        }
                    }
                }
            }

            if (testData.user() != null && testData.user().username() != null) {
                try {
                    usersDbClient.deleteUserByUsername(testData.user().username());
                    LOG.info("User '{}' deleted after test", testData.user().username());
                } catch (Exception e) {
                    LOG.error("Failed to delete user '{}'", testData.user().username(), e);
                }
            }
        }

        context.getStore(NAMESPACE).remove(context.getUniqueId());
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(TestData.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId(), TestData.class);
    }

    public static void addUser(ExtensionContext context, UserJson user, String password) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        TestData current = store.get(context.getUniqueId(), TestData.class);

        if (current == null) {
            TestData updated = new TestData(user, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), password);
            store.put(context.getUniqueId(), updated);
        } else {
            TestData updated = new TestData(
                    user,
                    current.museums(),
                    current.artists(),
                    current.paintings(),
                    password
            );
            store.put(context.getUniqueId(), updated);
        }
    }

    public static void addMuseum(ExtensionContext context, MuseumJson museum) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        TestData current = store.get(context.getUniqueId(), TestData.class);

        if (current == null) {
            List<MuseumJson> museums = new ArrayList<>();
            museums.add(museum);
            TestData updated = new TestData(null, museums, new ArrayList<>(), new ArrayList<>(), null);
            store.put(context.getUniqueId(), updated);
        } else {
            List<MuseumJson> museums = new ArrayList<>(current.museums() != null ? current.museums() : List.of());
            museums.add(museum);
            TestData updated = new TestData(
                    current.user(),
                    museums,
                    current.artists(),
                    current.paintings(),
                    current.password()
            );
            store.put(context.getUniqueId(), updated);
        }
    }

    public static void addArtist(ExtensionContext context, ArtistJson artist) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        TestData current = store.get(context.getUniqueId(), TestData.class);

        if (current == null) {
            List<ArtistJson> artists = new ArrayList<>();
            artists.add(artist);
            TestData updated = new TestData(null, new ArrayList<>(), artists, new ArrayList<>(), null);
            store.put(context.getUniqueId(), updated);
        } else {
            List<ArtistJson> artists = new ArrayList<>(current.artists() != null ? current.artists() : List.of());
            artists.add(artist);
            TestData updated = new TestData(
                    current.user(),
                    current.museums(),
                    artists,
                    current.paintings(),
                    current.password()
            );
            store.put(context.getUniqueId(), updated);
        }
    }

    public static void addPainting(ExtensionContext context, PaintingJson painting) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        TestData current = store.get(context.getUniqueId(), TestData.class);

        if (current == null) {
            List<PaintingJson> paintings = new ArrayList<>();
            paintings.add(painting);
            TestData updated = new TestData(null, new ArrayList<>(), new ArrayList<>(), paintings, null);
            store.put(context.getUniqueId(), updated);
        } else {
            List<PaintingJson> paintings = new ArrayList<>(current.paintings() != null ? current.paintings() : List.of());
            paintings.add(painting);
            TestData updated = new TestData(
                    current.user(),
                    current.museums(),
                    current.artists(),
                    paintings,
                    current.password()
            );
            store.put(context.getUniqueId(), updated);
        }
    }
}