package chess;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/** Guards the Forge update checker against accidental metadata removal. */
class TestUpdateCheckerMetadata {
    @Test
    void processedModMetadataEnablesCanonicalUpdateFeed() throws IOException {
        try (InputStream resource = getClass().getClassLoader()
                .getResourceAsStream("META-INF/mods.toml")) {
            assertNotNull(resource, "Processed META-INF/mods.toml must be on the test classpath");
            String content = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(content.contains(
                    "updateJSONURL=\"https://raw.githubusercontent.com/ramou/ChessMod/1.20.1/project/mod/update.json\""),
                    "The Forge update checker must use the canonical shared feed URL");
            assertTrue(!content.contains("${mod_update_url}"),
                    "The update feed property must be expanded during resource processing");
        }
    }
}
