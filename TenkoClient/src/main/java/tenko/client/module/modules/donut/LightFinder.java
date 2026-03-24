package tenko.client.module.modules.donut;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.WorldChunk;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;
import tenko.client.util.RenderUtil;
import java.awt.Color;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class LightFinder extends Module {
    public final Setting<Double>  scanRange = addSetting("Scan Range",    6.0, 1.0, 20.0);
    public final Setting<Double>  minLight  = addSetting("Min Light",     7.0, 1.0, 15.0);
    public final Setting<Double>  minCount  = addSetting("Min Lit",       4.0, 1.0, 30.0);
    public final Setting<Boolean> deepOnly  = addSetting("Deepslate Only", true);
    public final Setting<Boolean> showLines = addSetting("Show Lines",    true);

    private final AtomicReference<List<LightCluster>> results = new AtomicReference<>(Collections.emptyList());
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Tenko-LightFinder"); t.setDaemon(true); t.setPriority(3); return t;
    });
    private ScheduledFuture<?> task;

    public LightFinder() { super("LightFinder", "Finds artificially lit areas underground", Category.DONUT); }

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
            int range = scanRange.get().intValue(), minLvl = minLight.get().intValue(), minCnt = minCount.get().intValue();
            ChunkPos pc = mc.player.getChunkPos();
            int yMax = deepOnly.get() ? 0 : 64, yMin = mc.world.getBottomY();
            Map<BlockPos, List<BlockPos>> cells = new HashMap<>();
            for (int cx=-range; cx<=range; cx++) for (int cz=-range; cz<=range; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(pc.x+cx, pc.z+cz);
                if (chunk == null) continue;
                for (int x=0; x<16; x++) for (int z=0; z<16; z++) for (int y=yMin; y<=yMax; y++) {
                    BlockPos pos = new BlockPos(chunk.getPos().getStartX()+x, y, chunk.getPos().getStartZ()+z);
                    Block b = mc.world.getBlockState(pos).getBlock();
                    if (b==Blocks.AIR || b==Blocks.CAVE_AIR) {
                        int lvl = mc.world.getLightLevel(LightType.BLOCK, pos);
                        if (lvl >= minLvl) {
                            BlockPos cell = new BlockPos((pos.getX()>>4)<<4, (pos.getY()>>4)<<4, (pos.getZ()>>4)<<4);
                            cells.computeIfAbsent(cell, k -> new ArrayList<>()).add(pos);
                        }
                    }
                }
            }
            List<LightCluster> found = new ArrayList<>();
            for (Map.Entry<BlockPos, List<BlockPos>> e : cells.entrySet()) {
                if (e.getValue().size() >= minCnt) {
                    double ax=0, ay=0, az=0;
                    for (BlockPos p : e.getValue()) { ax+=p.getX(); ay+=p.getY(); az+=p.getZ(); }
                    int n = e.getValue().size();
                    found.add(new LightCluster(new Vec3d(ax/n, ay/n, az/n), n));
                }
            }
            found.sort(Comparator.comparingInt((LightCluster c) -> c.count).reversed());
            results.set(Collections.unmodifiableList(found));
        } catch (Exception ignored) {}
    }

    private void render(Events.RenderWorldEvent e) {
        if (mc.player == null) return;
        for (LightCluster c : results.get()) {
            Color col = new Color(200, 160, 255, 180);
            if (showLines.get()) RenderUtil.drawLineToPos(e.matrices, mc.player, c.center, col);
            RenderUtil.drawLabel(e.matrices, c.center, "§dLightCluster §7(" + c.count + ")", e.tickDelta);
        }
    }

    public static class LightCluster {
        public final Vec3d center; public final int count;
        public LightCluster(Vec3d c, int n) { center=c; count=n; }
    }
}
