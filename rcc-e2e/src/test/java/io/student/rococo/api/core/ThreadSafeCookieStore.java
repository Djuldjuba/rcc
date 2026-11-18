package io.student.rococo.api.core;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ThreadSafeCookieStore implements CookieJar {
    INSTANCE;

    private final ThreadLocal<Map<String, List<Cookie>>> store =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        Map<String, List<Cookie>> cookiesMap = store.get();
        for (Cookie cookie : cookies) {
            List<Cookie> domainCookies = cookiesMap.computeIfAbsent(cookie.domain(), k -> new ArrayList<>());
            domainCookies.removeIf(c -> c.name().equals(cookie.name()));
            domainCookies.add(cookie);
        }
    }

    @Override
    public @NonNull List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        List<Cookie> result = new ArrayList<>();
        for (Map.Entry<String, List<Cookie>> entry : store.get().entrySet()) {
            String domain = entry.getKey();
            if (url.host().contains(domain) || domain.contains(url.host())) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }

    public String cookieValue(String cookieName) {
        return store.get().values().stream()
                .flatMap(List::stream)
                .filter(c -> c.name().equals(cookieName))
                .map(Cookie::value)
                .findFirst()
                .orElse(null);
    }

    public void clear() {
        store.get().clear();
    }
}