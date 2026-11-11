package chess;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/** Smoke check that the 1.19.4 backport ships all registered quantum-board resources. */
class TestQuantumResources {
    @Test
    void quantumBoardResourcesAreOnClasspath() {
        String[] paths = {
            "/assets/chessmod/blockstates/quantum_chessboard.json",
            "/assets/chessmod/models/block/quantum_chessboard.json",
            "/assets/chessmod/models/block/quantum_chessboard_linked.json",
            "/assets/chessmod/models/item/quantum_chessboard.json",
            "/assets/chessmod/textures/block/enchantment_glint.png",
            "/assets/chessmod/textures/entity/beam_texture.png",
            "/data/chessmod/loot_tables/blocks/quantum_chessboard.json",
            "/data/chessmod/recipes/quantum_chessboard.json"
        };
        for (String resource : paths) {
            assertNotNull(TestQuantumResources.class.getResource(resource),
                    "Missing quantum-board resource: " + resource);
        }
    }
}
