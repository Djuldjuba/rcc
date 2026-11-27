package io.student.rococo.jupiter.extensions;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Allure;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BrowserExtension implements
        BeforeEachCallback,
        AfterEachCallback,
        TestExecutionExceptionHandler,
        LifecycleMethodExecutionExceptionHandler {

    private static boolean configured = false;

    private static synchronized void configure() {
        if (configured) return;

        String browser = System.getProperty("browser", "chrome").toLowerCase();
        String testEnv = System.getProperty("test.env", "local");

        Configuration.browser = browser;
        Configuration.timeout = 10_000;
        Configuration.pageLoadTimeout = 15_000;
        Configuration.pageLoadStrategy = "eager";
        Configuration.screenshots = false;
        Configuration.savePageSource = false;
        
        Configuration.browserSize = "1920x1080";

        if ("docker".equalsIgnoreCase(testEnv)) {
            Configuration.remote = "http://selenoid:4444/wd/hub";

            Map<String, Object> selenoidOptions = new HashMap<>();
            selenoidOptions.put("enableVNC", true);
            selenoidOptions.put("enableVideo", false);
            selenoidOptions.put("name", "rococo-test");
            selenoidOptions.put("sessionTimeout", "5m");
            selenoidOptions.put("timeZone", "Europe/Moscow");
            selenoidOptions.put("screenResolution", "1920x1080x24");

            switch (browser) {
                case "firefox" -> {
                    Configuration.browserVersion = "125.0";
                    FirefoxOptions options = new FirefoxOptions();
                    options.addArguments("-width=1920", "-height=1080");
                    options.setCapability("selenoid:options", selenoidOptions);
                    Configuration.browserCapabilities = options;
                }
                default -> {
                    Configuration.browserVersion = "135.0";
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments(
                            "--no-sandbox",
                            "--disable-dev-shm-usage",
                            "--disable-gpu",
                            "--window-size=1920,1080",
                            "--force-device-scale-factor=1",
                            "--high-dpi-support=1"
                    );
                    options.setCapability("selenoid:options", selenoidOptions);
                    Configuration.browserCapabilities = options;
                }
            }
        } else {
            if ("chrome".equals(browser)) {
                ChromeOptions options = new ChromeOptions();
                options.addArguments(
                        "--window-size=1920,1080",
                        "--force-device-scale-factor=1",
                        "--high-dpi-support=1"
                );
                Configuration.browserCapabilities = options;
            }
        }

        configured = true;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        configure();

        SelenideLogger.addListener("Allure-selenide",
                new AllureSelenide()
                        .screenshots(false)
                        .savePageSource(false)
        );

        clearBrowserState();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        try {
            clearBrowserState();
        } catch (Exception ignored) {
        } finally {
            try {
                if (WebDriverRunner.hasWebDriverStarted()) {
                    Selenide.closeWebDriver();
                }
            } catch (Exception e) {
                System.err.println("Failed to close WebDriver: " + e.getMessage());
            }
        }
    }

    private static void clearBrowserState() {
        try {
            if (!WebDriverRunner.hasWebDriverStarted()) {
                return;
            }

            String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();
            if (currentUrl == null || currentUrl.isBlank() || "about:blank".equals(currentUrl)) {
                Selenide.clearBrowserCookies();
                return;
            }

            Selenide.clearBrowserCookies();
            Selenide.clearBrowserLocalStorage();
            Selenide.executeJavaScript("window.sessionStorage.clear();");
        } catch (Exception e) {
            // nothing
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        doScreenshot("Screen on fail");
        throw throwable;
    }

    @Override
    public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        doScreenshot("Screen on BeforeEach fail");
        throw throwable;
    }

    @Override
    public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        doScreenshot("Screen on AfterEach fail");
        throw throwable;
    }

    private static void doScreenshot(String name) {
        try {
            if (WebDriverRunner.hasWebDriverStarted()) {
                Allure.addAttachment(
                        name,
                        new ByteArrayInputStream(
                                ((TakesScreenshot) WebDriverRunner.getWebDriver())
                                        .getScreenshotAs(OutputType.BYTES)
                        )
                );

//                String pageSource = WebDriverRunner.getWebDriver().getPageSource();
//                Allure.addAttachment(name + " (HTML)", "text/html", pageSource, ".html");
//
//                Path dir = Paths.get("build", "failed-html");
//                Files.createDirectories(dir);
//                Path file = dir.resolve(name.replaceAll("[^a-zA-Z0-9-_]", "_") + ".html");
//                Files.writeString(file, pageSource);
            }
        } catch (Exception ignored) {
        }
    }
}