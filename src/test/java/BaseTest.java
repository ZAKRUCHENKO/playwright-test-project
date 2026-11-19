import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {
    // Не статические поля (отдельные для каждого тестового класса)
    protected Playwright playwright;
    protected Browser browser;
    protected Page page;

    @BeforeAll
    public static void setupClass() { /* Общая инициализация */ }

    @BeforeEach
    public void setupTest() {
        playwright = Playwright.create();
        // HEADLESS=true нужен для запуска в CI (GitHub Actions); локально по умолчанию headed
        boolean headless = Boolean.parseBoolean(System.getenv().getOrDefault("HEADLESS", "false"));
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(headless)
        );
        page = browser.newPage();
    }

    @AfterEach
    public void tearDownTest() {
        if (page != null) page.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}