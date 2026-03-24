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

public class ClusterFinder extends Module {
    public final Setting<Double>  scanRange  = addSetting("Scan Range",    6.0, 1.0, 20.0);
    public final Setting<Double>  minCluster = addSetting("Min Cluster",   3.0, 1.0, 20.0);
    public final Setting<Boolean> deepOnly   = addSetting("Deepslate Only", true);
    public final Setting<Boolean> showLines  = addSetting("Show Lines",    true);

    private final AtomicReference<List<Cluster>> results = new AtomicReference<>(Collections.emptyList());
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Tenko-ClusterFinder"); t.setDaemon(true); t.setPriority(3); return t;
    });
    private ScheduledFuture<?> task;

    private static final Set<Block> VALUABLE = Set.of(
        Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.ENDER_CHEST,
        Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
        Blocks.BEACON, Blocks.ANVIL, Blocks.ENCHANTING_TABLE,
        Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
        Blocks.CRAFTING_TABLE, Blocks.GRINDSTONE, Blocks.SMITHING_TABLE,
        Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN
    );

    public ClusterFinder() { super("ClusterFinder", "Finds clusters of player blocks", Category.DONUT); }

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
            int range = scanRange.get().intValue(), min = minCluster.get().intValue();
            ChunkPos pc = mc.player.getChunkPos();
            int yMax = deepOnly.get() ? 0 : 64, yMin = mc.world.getBottomY();
            Map<BlockPos, Integer> hot = new HashMap<>();
            for (int cx=-range; cx<=range; cx++) for (int cz=-range; cz<=range; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(pc.x+cx, pc.z+cz);
                if (chunk == null) continue;
                for (int x=0; x<16; x++) for (int z=0; z<16; z++) for (int y=yMin; y<=yMax; y++) {
                    BlockPos pos = new BlockPos(chunk.getPos().getStartX()+x, y, chunk.getPos().getStartZ()+z);
                    if (VALUABLE.contains(mc.world.getBlockState(pos).getBlock())) {
                        BlockPos key = new BlockPos((pos.getX()>>3)<<3, (pos.getY()>>3)<<3, (pos.getZ()>>3)<<3);
                        hot.merge(key, 1, Integer::sum);
                    }
                }
            }
            List<Cluster> found = new ArrayList<>();
            for (Map.Entry<BlockPos, Integer> e : hot.entrySet())
                if (e.getValue() >= min) {
                    BlockPos k = e.getKey();
                    found.add(new Cluster(new Vec3d(k.getX()+4, k.getY()+4, k.getZ()+4), e.getValue()));
                }
            found.sort(Comparator.comparingInt((Cluster c) -> c.count).reversed());
            results.set(Collections.unmodifiableList(found));
        } catch (Exception ignored) {}
    }

    private void render(Events.RenderWorldEvent e) {
        if (mc.player == null) return;
        for (Cluster c : results.get()) {
            Color col = new Color(220, 80, 255, 180);
            if (showLines.get()) RenderUtil.drawLineToPos(e.matrices, mc.player, c.center, col);
            RenderUtil.drawLabel(e.matrices, c.center, "§dCluster §7x" + c.count, e.tickDelta);
        }
    }

    public static class Cluster {
        public final Vec3d center; public final int count;
        public Cluster(Vec3d c, int n) { center=c; count=n; }
    }
}
