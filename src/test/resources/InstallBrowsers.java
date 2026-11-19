protected static Playwright playwright;
protected static Browser browser;

@BeforeAll
public static void launchBrowser() { // JUnit 5: @BeforeAll + static
    playwright = Playwright.create();
    browser = playwright.chromium().launch();
}

@AfterAll
public static void closeBrowser() {
    /* ... */ }

@BeforeEach
public void createPage() {
    Page page = browser.newPage(); // Новая страница на тест
}
