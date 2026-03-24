package tenko.client.module.modules.visual;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import tenko.client.module.Module;

public class Xray extends Module {
    public Xray() { super("Xray", "See ores through blocks", Category.VISUAL); }

    @Override public void onEnable()  { if (mc.worldRenderer != null) mc.worldRenderer.reload(); }
    @Override public void onDisable() { if (mc.worldRenderer != null) mc.worldRenderer.reload(); }

    public boolean shouldRender(Block b) {
        if (!isEnabled()) return true;
        String id = Registries.BLOCK.getId(b).getPath();
        return id.contains("ore") || id.contains("ancient_debris") ||
               b==Blocks.CHEST || b==Blocks.TRAPPED_CHEST || b==Blocks.ENDER_CHEST ||
               b==Blocks.BEDROCK || b==Blocks.LAVA || b==Blocks.SPAWNER;
    }
}
