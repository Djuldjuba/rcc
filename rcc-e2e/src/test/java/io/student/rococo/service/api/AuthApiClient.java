package io.student.rococo.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Step;
import io.student.rococo.api.AuthApi;
import io.student.rococo.api.core.ThreadSafeCookieStore;
import io.student.rococo.config.Config;
import io.student.rococo.jupiter.extensions.ApiLoginExtension;
import io.student.rococo.utils.OAuthUtils;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthApiClient extends RestClient {

    private static final Config CFG = Config.getInstance();
    private final AuthApi authApi;

    private static final Pattern CSRF_INPUT_PATTERN = Pattern.compile(
            "name=\"_csrf\"\\s+value=\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CSRF_INPUT_PATTERN_ALT = Pattern.compile(
            "value=\"([^\"]+)\"\\s+name=\"_csrf\"",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CSRF_META_PATTERN = Pattern.compile(
            "name=\"_csrf\"\\s+content=\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );

    public AuthApiClient() {
        super(CFG.rococoAuthUrl(), false);
        this.authApi = create(AuthApi.class);
    }

    @Step("Получить JWT токен для пользователя '{username}' через API")
    public String login(String username, String password) {
        try {
            ThreadSafeCookieStore.INSTANCE.clear();

            final String codeVerifier = OAuthUtils.generateCodeVerifier();
            final String codeChallenge = OAuthUtils.generateCodeChallenge(codeVerifier);
            final String redirectUri = CFG.frontUrl().endsWith("/")
                    ? CFG.frontUrl() + "authorized"
                    : CFG.frontUrl() + "/authorized";
            final String clientId = CFG.clientId();

            // 1. OAuth2 authorize → 302 Location: /login
            Response<Void> authorizeResponse = authApi.authorize(
                    "code",
                    clientId,
                    "openid",
                    redirectUri,
                    codeChallenge,
                    "S256"
            ).execute();

            String authorizeLocation = authorizeResponse.headers().get("Location");

            // 2. GET login page (HTML) — отсюда берём CSRF
            Response<ResponseBody> loginPageResponse;
            if (authorizeLocation != null && !authorizeLocation.isBlank()) {
                String loginUrl = toAbsoluteUrl(authorizeLocation);
                loginPageResponse = authApi.followRedirectWithBody(loginUrl).execute();
            } else {
                loginPageResponse = authApi.getLoginPage().execute();
            }

            String html = "";
            if (loginPageResponse.body() != null) {
                html = loginPageResponse.body().string();
            }

            // 3. CSRF: сначала cookie, потом HTML
            String csrfToken = ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN");
            if (csrfToken == null || csrfToken.isEmpty()) {
                csrfToken = extractCsrfFromHtml(html);
            }

            if (csrfToken == null || csrfToken.isEmpty()) {
                throw new RuntimeException("CSRF token not found (neither in cookie nor in HTML)");
            }

            // 4. POST login
            Response<Void> loginResponse = authApi.login(username, password, csrfToken).execute();

            // 5. Следуем по редиректам, пока не найдём code=
            String location = loginResponse.headers().get("Location");
            String code = null;
            int maxRedirects = 8;

            while (location != null && maxRedirects-- > 0) {
                if (location.contains("code=")) {
                    code = OAuthUtils.extractCodeFromUrl(location);
                    break;
                }

                String nextUrl = toAbsoluteUrl(location);
                Response<Void> redirectResponse = authApi.followRedirect(nextUrl).execute();
                location = redirectResponse.headers().get("Location");
            }

            if (code == null || code.isEmpty()) {
                throw new RuntimeException("Authorization code not found. Last Location=" + location);
            }

            ApiLoginExtension.setCode(code);

            // 6. Exchange code → tokens
            Response<JsonNode> tokenResponse = authApi.token(
                    clientId,
                    redirectUri,
                    "authorization_code",
                    code,
                    codeVerifier
            ).execute();

            if (!tokenResponse.isSuccessful()) {
                String errorBody = tokenResponse.errorBody() != null
                        ? tokenResponse.errorBody().string()
                        : "null";
                throw new RuntimeException("Token request failed: " + tokenResponse.code()
                        + " - " + tokenResponse.message() + ", body: " + errorBody);
            }

            JsonNode body = tokenResponse.body();
            if (body == null || !body.has("id_token")) {
                throw new RuntimeException("id_token not found in token response: " + body);
            }

            return body.get("id_token").asText();

        } catch (Exception e) {
            throw new RuntimeException("API login failed for user: " + username, e);
        }
    }

    private String toAbsoluteUrl(String location) {
        if (location == null) {
            return null;
        }
        if (location.startsWith("http")) {
            return location;
        }
        String base = CFG.rococoAuthUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return location.startsWith("/") ? base + location : base + "/" + location;
    }

    private String extractCsrfFromHtml(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }

        Matcher m = CSRF_INPUT_PATTERN.matcher(html);
        if (m.find()) {
            return m.group(1);
        }

        m = CSRF_INPUT_PATTERN_ALT.matcher(html);
        if (m.find()) {
            return m.group(1);
        }

        m = CSRF_META_PATTERN.matcher(html);
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }
}