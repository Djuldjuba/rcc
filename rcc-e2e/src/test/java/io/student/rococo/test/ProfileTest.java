package io.student.rococo.test;

import com.codeborne.selenide.Selenide;
import io.student.rococo.config.Config;
import io.student.rococo.page.MainPage;
import org.junit.jupiter.api.Test;

import static java.lang.Thread.sleep;

public class ProfileTest {

    private static final Config CFG = Config.getInstance();

    @Test
    void profileTest() throws InterruptedException {
        Selenide.open(CFG.frontUrl(), MainPage.class)
                .clickLoginButton()
                .login("rococo2", "12345")
                .clickProfileIconBtn()
                .checkNickname("rococo");
        sleep(10000);
    }
}
