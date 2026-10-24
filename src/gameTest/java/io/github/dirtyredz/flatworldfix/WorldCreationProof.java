package io.github.dirtyredz.flatworldfix;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Runs only in the named dedicated-server proof, never in the release jar. */
@Mod.EventBusSubscriber(modid = FlatWorldFix.MOD_ID)
public final class WorldCreationProof {
    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        if (System.getProperty("flatworldfix.proof") == null) return;
        try {
            System.out.println("FLATWORLDFIX_PROOF generator="
                    + event.getServer().overworld().getChunkSource().getGenerator().getClass().getName());
            if (!Boolean.getBoolean("flatworldfix.control")) {
                MergeProof.run(event.getServer());
            }
            java.nio.file.Files.writeString(java.nio.file.Path.of("proof-result.txt"), "PASS\n");
        } catch (Exception | AssertionError error) {
            error.printStackTrace();
            try {
                java.nio.file.Files.writeString(java.nio.file.Path.of("proof-result.txt"), "FAIL " + error + "\n");
            } catch (java.io.IOException writeError) {
                throw new java.io.UncheckedIOException(writeError);
            }
        } finally {
            // Normal shutdown saves level.dat, which the external verifier reads afterwards.
            event.getServer().halt(false);
        }
    }
}
