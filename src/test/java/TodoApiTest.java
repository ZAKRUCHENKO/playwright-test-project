import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * API-тест эндпоинта https://jsonplaceholder.typicode.com/todos/1
 *
 * Сценарий:
 *  1. Выполнить GET-запрос напрямую через API (без браузера).
 *  2. Проверить статус ответа: assertEquals(200, response.status()).
 *  3. Распарсить JSON тела ответа через Jackson (ObjectMapper).
 *  4. Проверить структуру ответа: наличие полей userId, id, title, completed и их типы.
 */
public class TodoApiTest {
    Playwright playwright;
    APIRequestContext requestContext;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        requestContext = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL("https://jsonplaceholder.typicode.com")
        );
    }

    @Test
    @Epic("Playwright API")
    @Feature("JSONPlaceholder")
    @Story("GET /todos/1: структура и значения")
    @Severity(SeverityLevel.CRITICAL)
    void testTodoApi() throws Exception {
        // 1. GET-запрос напрямую через API — браузер не открывается
        APIResponse response = requestContext.get("/todos/1");

        // 2. Проверка статуса ответа
        assertEquals(200, response.status(), "GET /todos/1 должен вернуть HTTP 200");

        // 3. Парсинг JSON из тела ответа
        JsonNode todo = objectMapper.readTree(response.text());
        System.out.println("Тело ответа: " + response.text());

        // 4. Проверка структуры: все 4 поля на месте и имеют правильные типы
        assertTrue(todo.has("userId") && todo.get("userId").isInt(),
                "Поле userId должно существовать и быть числом");
        assertTrue(todo.has("id") && todo.get("id").isInt(),
                "Поле id должно существовать и быть числом");
        assertTrue(todo.has("title") && todo.get("title").isTextual(),
                "Поле title должно существовать и быть строкой");
        assertTrue(todo.has("completed") && todo.get("completed").isBoolean(),
                "Поле completed должно существовать и быть булевым");

        // Проверка значений по каноничному ответу /todos/1
        assertEquals(1, todo.get("id").asInt(), "id должен быть равен 1");
        assertEquals(1, todo.get("userId").asInt(), "userId должен быть равен 1");
        assertFalse(todo.get("completed").asBoolean(), "completed должен быть false");
        assertFalse(todo.get("title").asText().isBlank(), "title не должен быть пустым");
    }

    @AfterEach
    void tearDown() {
        requestContext.dispose();
        playwright.close();
    }
}
