package chess;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/** Check the resources that Forge packages into the 1.19.4 release JAR. */
class TestReleaseMetadata {
    private String resource(String name) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(name)) {
            assertNotNull(stream, "Missing processed resource: " + name);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void modMetadataDeclaresReleaseAndUpdateChecker() throws IOException {
        String metadata = resource("META-INF/mods.toml");
        assertTrue(metadata.contains("version=\"1.19.4-0.702\""), "Mod version must match Gradle");
        assertTrue(metadata.contains("loaderVersion=\"[45,)\""), "Forge 45 loader range");
        assertTrue(metadata.contains("versionRange=\"[45,)\""), "Forge 45 dependency range");
        assertTrue(metadata.contains("updateJSONURL=\"https://raw.githubusercontent.com/ramou/ChessMod/1.20.1/project/mod/update.json\""),
                   "Forge update checker must use the canonical update feed");
        assertTrue(!metadata.toLowerCase().contains("lorem ipsum"), "Remove example mod description");
        assertNotNull(getClass().getClassLoader().getResourceAsStream("chessmod.png"),
                      "The logo referenced by mods.toml must be packaged");
    }

    @Test
    void resourcePackFormatMatchesMinecraft1194() throws IOException {
        String metadata = resource("pack.mcmeta");
        assertTrue(metadata.contains("\"pack_format\": 13"), "Minecraft 1.19.4 resource-pack format");
    }
}
