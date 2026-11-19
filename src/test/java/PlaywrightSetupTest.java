import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlaywrightSetupTest extends BaseTest {

    @Test
    void testPlaywrightSetup() {
        page.navigate("https://example.com");
        String title = page.title();
        assertEquals("Example Domain", title);
    }
}
