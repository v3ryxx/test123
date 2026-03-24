package tenko.client.module.modules.donut;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;
import tenko.client.util.RenderUtil;
import java.awt.Color;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkFinder extends Module {
    public final Setting<Double>  scanRange  = addSetting("Scan Range",      8.0,  1.0, 32.0);
    public final Setting<Boolean> deepOnly   = addSetting("Below Deepslate", true);
    public final Setting<Boolean> showLines  = addSetting("Show Lines",       true);
    public final Setting<Boolean> showLabels = addSetting("Show Labels",      true);
    public final Setting<Double>  minScore   = addSetting("Min Score",        5.0,  1.0, 30.0);

    private final AtomicReference<List<SusChunk>> results = new AtomicReference<>(Collections.emptyList());
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Tenko-ChunkFinder"); t.setDaemon(true); t.setPriority(3); return t;
    });
    private ScheduledFuture<?> task;

    private static final Set<Block> PLAYER_BLOCKS = Set.of(
        Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST, Blocks.BARREL,
        Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
        Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.ENCHANTING_TABLE,
        Blocks.TORCH, Blocks.WALL_TORCH, Blocks.LANTERN, Blocks.SOUL_LANTERN,
        Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
        Blocks.BEACON, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN,
        Blocks.NETHER_PORTAL, Blocks.BED, Blocks.LADDER,
        Blocks.FARMLAND, Blocks.DIRT_PATH, Blocks.COARSE_DIRT
    );

    public ChunkFinder() { super("ChunkFinder", "Detects bases below deepslate", Category.DONUT); }

    @Override public void onEnable() {
        results.set(Collections.emptyList());
        task = exec.scheduleAtFixedRate(this::scan, 0, 2, TimeUnit.SECONDS);
        TenkoClient.eventBus.subscribe(Events.RenderWorldEvent.class, e -> render(e));
    }
    @Override public void onDisable() {
        if (task != null) { task.cancel(false); task = null; }
        results.set(Collections.emptyList());
    }

    private void scan() {
        if (mc.world==null || mc.player==null) return;
        try {
            int range = scanRange.get().intValue();
            ChunkPos pc = mc.player.getChunkPos();
            int yMax = deepOnly.get() ? 0 : 64;
            int yMin = mc.world.getBottomY();
            List<SusChunk> found = new ArrayList<>();

            for (int cx=-range; cx<=range; cx++) for (int cz=-range; cz<=range; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(pc.x+cx, pc.z+cz);
                if (chunk == null) continue;
                int score=0, count=0;
                for (int x=0; x<16; x++) for (int z=0; z<16; z++) for (int y=yMin; y<=yMax; y++) {
                    BlockPos pos = new BlockPos(chunk.getPos().getStartX()+x, y, chunk.getPos().getStartZ()+z);
                    Block b = mc.world.getBlockState(pos).getBlock();
                    if (PLAYER_BLOCKS.contains(b)) { score+=blockScore(b); count++; }
                }
                if (score >= minScore.get().intValue()) {
                    Vec3d center = new Vec3d(chunk.getPos().getCenterX(), yMin+10, chunk.getPos().getCenterZ());
                    found.add(new SusChunk(chunk.getPos(), center, score, count));
                }
            }
            found.sort(Comparator.comparingInt((SusChunk c) -> c.score).reversed());
            results.set(Collections.unmodifiableList(found));
        } catch (Exception ignored) {}
    }

    private int blockScore(Block b) {
        if (b==Blocks.BEACON) return 10;
        if (b==Blocks.CHEST||b==Blocks.TRAPPED_CHEST||b==Blocks.ENDER_CHEST) return 4;
        if (b==Blocks.ENCHANTING_TABLE||b==Blocks.ANVIL||b==Blocks.BLAST_FURNACE) return 3;
        if (b==Blocks.OBSIDIAN||b==Blocks.CRYING_OBSIDIAN||b==Blocks.NETHER_PORTAL) return 3;
        if (b==Blocks.HOPPER||b==Blocks.BARREL||b==Blocks.DROPPER) return 2;
        return 1;
    }

    private void render(Events.RenderWorldEvent e) {
        if (mc.player == null) return;
        for (SusChunk c : results.get()) {
            float t = Math.min(1f, c.score/30f);
            // Tenko colors: purple → magenta gradient
            Color col = new Color((int)(180 + t*75), (int)(60 - t*60), (int)(255 - t*60), 180);
            Vec3d min = new Vec3d(c.chunkPos.getStartX(), mc.world.getBottomY(), c.chunkPos.getStartZ());
            Vec3d max = new Vec3d(c.chunkPos.getEndX(), mc.world.getBottomY()+20, c.chunkPos.getEndZ());
            RenderUtil.drawChunkBox(e.matrices, min, max, col);
            if (showLines.get())  RenderUtil.drawLineToPos(e.matrices, mc.player, c.center, col);
            if (showLabels.get()) RenderUtil.drawLabel(e.matrices, c.center, "§dBase? §7Score:"+c.score+" ("+c.blockCount+")", e.tickDelta);
        }
    }

    public List<SusChunk> getResults() { return results.get(); }

    public static class SusChunk {
        public final ChunkPos chunkPos; public final Vec3d center;
        public final int score, blockCount;
        public SusChunk(ChunkPos cp, Vec3d cen, int s, int bc) { chunkPos=cp; center=cen; score=s; blockCount=bc; }
    }
}
