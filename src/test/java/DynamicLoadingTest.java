import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.assertions.LocatorAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тест страницы https://the-internet.herokuapp.com/dynamic_loading/1
 * (Example 1: Element on page that is hidden after load).
 *
 * Сценарий:
 *  1. Открыть страницу и запустить процесс загрузки (кнопка Start).
 *  2. Перехватить сетевые запросы и убедиться, что запрос к /dynamic_loading
 *     завершился успешно (HTTP 200).
 *  3. Дождаться появления текста "Hello World!".
 *  4. Сохранить трассировку выполнения (trace-success.zip) — независимо от результата.
 */
public class DynamicLoadingTest extends BaseTest {

    private static final String PAGE_URL =
            "https://the-internet.herokuapp.com/dynamic_loading/1";
    private static final Path TRACE_PATH = Path.of("trace", "trace-success.zip");
    private static final String EXPECTED_TEXT = "Hello World!";

    @Test
    void testDynamicLoading() throws IOException {
        BrowserContext context = browser.newContext();
        // herokuapp просыпается медленно — увеличиваем таймауты навигации
        context.setDefaultNavigationTimeout(60_000);
        context.setDefaultTimeout(15_000);
        page = context.newPage();

        // Перехват запросов: собираем все ответы, чей URL содержит /dynamic_loading
        List<Response> dynamicLoadingResponses = Collections.synchronizedList(new ArrayList<>());
        page.onResponse(response -> {
            if (response.url().contains("/dynamic_loading")) {
                dynamicLoadingResponses.add(response);
                System.out.printf("[network] %s %s -> HTTP %d%n",
                        response.request().resourceType(), response.url(), response.status());
            }
        });

        // Трассировка запускается до начала действий: скриншоты, DOM-снапшоты, исходники
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        try {
            page.navigate(PAGE_URL);

            // Запускаем процесс загрузки (кнопка Start)
            page.click("button");
            Locator finishText = page.locator("#finish");

            // Дожидаемся появления текста "Hello World!"
            // страница показывает его через setTimeout 5 сек после клика, даём запас
            assertThat(finishText).hasText(EXPECTED_TEXT,
                    new LocatorAssertions.HasTextOptions().setTimeout(15_000));

            // Проверяем, что запрос(ы) к /dynamic_loading завершились успешно
            assertFalse(dynamicLoadingResponses.isEmpty(),
                    "Сетевые запросы к /dynamic_loading не были перехвачены");

            String failedResponses = dynamicLoadingResponses.stream()
                    .filter(r -> r.status() != 200)
                    .map(r -> r.status() + " " + r.url())
                    .collect(Collectors.joining(", "));
            assertTrue(failedResponses.isEmpty(),
                    "Найдены неуспешные запросы к /dynamic_loading: " + failedResponses);

            System.out.println("Перехвачено запросов к /dynamic_loading: "
                    + dynamicLoadingResponses.size() + ", все со статусом 200");
        } finally {
            // Сохраняем трассировку даже если тест упал
            Files.createDirectories(TRACE_PATH.getParent());
            context.tracing().stop(new Tracing.StopOptions().setPath(TRACE_PATH));
            System.out.println("Трассировка сохранена: " + TRACE_PATH.toAbsolutePath());
        }
    }
}
