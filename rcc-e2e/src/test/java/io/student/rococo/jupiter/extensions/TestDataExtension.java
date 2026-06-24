package io.student.rococo.jupiter.extensions;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import io.student.rococo.jupiter.TestData;
import io.student.rococo.model.ArtistJson;
import io.student.rococo.model.MuseumJson;
import io.student.rococo.model.PaintingJson;
import io.student.rococo.model.UserJson;

public class TestDataExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(TestDataExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        TestData testData = new TestData(null, null, null, null, null);
        context.getStore(NAMESPACE).put(context.getUniqueId(), testData);
    }

    @Override
    public void afterEach(ExtensionContext context) {
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
        TestData current = context.getStore(NAMESPACE).get(context.getUniqueId(), TestData.class);
        TestData updated = new TestData(user, current.museum(), current.artist(), current.painting(), password);
        context.getStore(NAMESPACE).put(context.getUniqueId(), updated);
    }

    public static void addMuseum(ExtensionContext context, MuseumJson museum) {
        TestData current = context.getStore(NAMESPACE).get(context.getUniqueId(), TestData.class);
        TestData updated = new TestData(current.user(), museum, current.artist(), current.painting(), current.password());
        context.getStore(NAMESPACE).put(context.getUniqueId(), updated);
    }

    public static void addArtist(ExtensionContext context, ArtistJson artist) {
        TestData current = context.getStore(NAMESPACE).get(context.getUniqueId(), TestData.class);
        TestData updated = new TestData(current.user(), current.museum(), artist, current.painting(), current.password());
        context.getStore(NAMESPACE).put(context.getUniqueId(), updated);
    }

    public static void addPainting(ExtensionContext context, PaintingJson painting) {
        TestData current = context.getStore(NAMESPACE).get(context.getUniqueId(), TestData.class);
        TestData updated = new TestData(current.user(), current.museum(), current.artist(), painting, current.password());
        context.getStore(NAMESPACE).put(context.getUniqueId(), updated);
    }
}