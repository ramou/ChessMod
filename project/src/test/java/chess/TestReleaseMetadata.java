package chess;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/** Guard release metadata expanded by Gradle before the Forge JAR is packaged. */
class TestReleaseMetadata {
    private String resource(String name) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(name)) {
            assertNotNull(stream, "Processed resource must exist: " + name);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void processedModsTomlDeclaresVersionAndUpdateChecker() throws IOException {
        String metadata = resource("META-INF/mods.toml");
        assertTrue(metadata.contains("version=\"1.20.1-0.702\""), "Packaged mod version");
        assertTrue(metadata.contains("loaderVersion=\"[47,)\""), "Forge loader range");
        assertTrue(metadata.contains("versionRange=\"[47,)\""), "Forge dependency range");
        assertTrue(metadata.contains("updateJSONURL=\"https://raw.githubusercontent.com/ramou/ChessMod/1.20.1/project/mod/update.json\""),
                "Enabled canonical Forge update checker");
        assertTrue(!metadata.contains("${mod_"), "All mod properties should be expanded");
        assertTrue(!metadata.contains("lorem ipsum"), "Remove example metadata");
    }

    @Test
    void resourcePackMetadataMatchesMinecraft1201() throws IOException {
        String pack = resource("pack.mcmeta");
        assertTrue(pack.contains("\"pack_format\": 15"), "Minecraft 1.20.1 resource-pack format");
        assertNotNull(getClass().getClassLoader().getResourceAsStream("chessmod.png"),
                "Mod logo configured in mods.toml should be packaged");
    }
}
