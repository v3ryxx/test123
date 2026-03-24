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

public class TunnelBaseFinder extends Module {
    public final Setting<Double>  scanRange = addSetting("Scan Range",      6.0, 1.0, 20.0);
    public final Setting<Double>  minLen    = addSetting("Min Tunnel Len",  5.0, 2.0, 30.0);
    public final Setting<Boolean> deepOnly  = addSetting("Deepslate Only",  true);
    public final Setting<Boolean> showLines = addSetting("Show Lines",       true);

    private final AtomicReference<List<TunnelResult>> results = new AtomicReference<>(Collections.emptyList());
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Tenko-TunnelFinder"); t.setDaemon(true); t.setPriority(3); return t;
    });
    private ScheduledFuture<?> task;

    public TunnelBaseFinder() { super("TunnelBaseFinder", "Finds player tunnels below deepslate", Category.DONUT); }

    @Override public void onEnable() {
        results.set(Collections.emptyList());
        task = exec.scheduleAtFixedRate(this::scan, 0, 3, TimeUnit.SECONDS);
        TenkoClient.eventBus.subscribe(Events.RenderWorldEvent.class, e -> render(e));
    }
    @Override public void onDisable() {
        if (task != null) { task.cancel(false); task = null; }
        results.set(Collections.emptyList());
    }

    private void scan() {
        if (mc.world==null || mc.player==null) return;
        try {
            int range = scanRange.get().intValue(), ml = minLen.get().intValue();
            ChunkPos pc = mc.player.getChunkPos();
            int yMax = deepOnly.get() ? 0 : 64, yMin = mc.world.getBottomY();
            List<TunnelResult> found = new ArrayList<>();

            for (int cx=-range; cx<=range; cx++) for (int cz=-range; cz<=range; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(pc.x+cx, pc.z+cz);
                if (chunk == null) continue;
                for (int y=yMin; y<=yMax; y++) for (int z=0; z<16; z++) {
                    int run=0, startX=-1;
                    for (int x=0; x<16; x++) {
                        BlockPos pos = new BlockPos(chunk.getPos().getStartX()+x, y, chunk.getPos().getStartZ()+z);
                        Block b  = mc.world.getBlockState(pos).getBlock();
                        Block ab = mc.world.getBlockState(pos.up()).getBlock();
                        boolean air = (b==Blocks.AIR||b==Blocks.CAVE_AIR) && (ab==Blocks.AIR||ab==Blocks.CAVE_AIR);
                        if (air) { if (run==0) startX=x; run++; }
                        else {
                            if (run >= ml) {
                                Vec3d cen = new Vec3d(chunk.getPos().getStartX()+startX+run/2.0, y+1, chunk.getPos().getStartZ()+z);
                                found.add(new TunnelResult(cen, run, y));
                            }
                            run=0; startX=-1;
                        }
                    }
                }
            }
            results.set(Collections.unmodifiableList(dedup(found, 8.0)));
        } catch (Exception ignored) {}
    }

    private List<TunnelResult> dedup(List<TunnelResult> list, double d) {
        List<TunnelResult> out = new ArrayList<>();
        for (TunnelResult r : list) {
            boolean c = false;
            for (TunnelResult o : out) if (r.center.distanceTo(o.center) < d) { c=true; break; }
            if (!c) out.add(r);
        }
        return out;
    }

    private void render(Events.RenderWorldEvent e) {
        if (mc.player == null) return;
        for (TunnelResult t : results.get()) {
            Color col = new Color(180, 80, 255, 180);
            if (showLines.get()) RenderUtil.drawLineToPos(e.matrices, mc.player, t.center, col);
            RenderUtil.drawLabel(e.matrices, t.center, "§dTunnel §7y="+t.y+" len="+t.length, e.tickDelta);
        }
    }

    public static class TunnelResult {
        public final Vec3d center; public final int length, y;
        public TunnelResult(Vec3d c, int l, int y) { center=c; length=l; this.y=y; }
    }
}
