package tenko.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.awt.Color;

public class RenderUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void drawString(MatrixStack m, String text, int x, int y, int color) {
        mc.textRenderer.drawWithShadow(m, text, x, y, color);
    }

    public static void drawRect(MatrixStack m, int x1, int y1, int x2, int y2, int color) {
        Matrix4f mat = m.peek().getPositionMatrix();
        float a=(color>>24&0xFF)/255f, r=(color>>16&0xFF)/255f, g=(color>>8&0xFF)/255f, b=(color&0xFF)/255f;
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(mat,x1,y1,0).color(r,g,b,a);
        buf.vertex(mat,x1,y2,0).color(r,g,b,a);
        buf.vertex(mat,x2,y2,0).color(r,g,b,a);
        buf.vertex(mat,x2,y1,0).color(r,g,b,a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    public static void drawOutlinedBox(MatrixStack matrices, Vec3d pos, Box box, Color color) {
        if (mc.gameRenderer == null) return;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        matrices.push();
        matrices.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        float r=color.getRed()/255f, g=color.getGreen()/255f, b=color.getBlue()/255f, a=color.getAlpha()/255f;
        Matrix4f mat = matrices.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        double x1=box.minX-pos.x, y1=box.minY-pos.y, z1=box.minZ-pos.z;
        double x2=box.maxX-pos.x, y2=box.maxY-pos.y, z2=box.maxZ-pos.z;
        line(buf,mat, x1,y1,z1, x2,y1,z1, r,g,b,a); line(buf,mat, x2,y1,z1, x2,y1,z2, r,g,b,a);
        line(buf,mat, x2,y1,z2, x1,y1,z2, r,g,b,a); line(buf,mat, x1,y1,z2, x1,y1,z1, r,g,b,a);
        line(buf,mat, x1,y2,z1, x2,y2,z1, r,g,b,a); line(buf,mat, x2,y2,z1, x2,y2,z2, r,g,b,a);
        line(buf,mat, x2,y2,z2, x1,y2,z2, r,g,b,a); line(buf,mat, x1,y2,z2, x1,y2,z1, r,g,b,a);
        line(buf,mat, x1,y1,z1, x1,y2,z1, r,g,b,a); line(buf,mat, x2,y1,z1, x2,y2,z1, r,g,b,a);
        line(buf,mat, x2,y1,z2, x2,y2,z2, r,g,b,a); line(buf,mat, x1,y1,z2, x1,y2,z2, r,g,b,a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.enableDepthTest(); RenderSystem.disableBlend();
        matrices.pop();
    }

    public static void drawChunkBox(MatrixStack matrices, Vec3d min, Vec3d max, Color color) {
        if (mc.gameRenderer == null) return;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        float r=color.getRed()/255f, g=color.getGreen()/255f, b=color.getBlue()/255f, a=color.getAlpha()/255f;
        Matrix4f mat = matrices.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        double x1=min.x,y1=min.y,z1=min.z,x2=max.x,y2=max.y,z2=max.z;
        line(buf,mat,x1,y1,z1,x2,y1,z1,r,g,b,a); line(buf,mat,x2,y1,z1,x2,y1,z2,r,g,b,a);
        line(buf,mat,x2,y1,z2,x1,y1,z2,r,g,b,a); line(buf,mat,x1,y1,z2,x1,y1,z1,r,g,b,a);
        line(buf,mat,x1,y2,z1,x2,y2,z1,r,g,b,a); line(buf,mat,x2,y2,z1,x2,y2,z2,r,g,b,a);
        line(buf,mat,x2,y2,z2,x1,y2,z2,r,g,b,a); line(buf,mat,x1,y2,z2,x1,y2,z1,r,g,b,a);
        line(buf,mat,x1,y1,z1,x1,y2,z1,r,g,b,a); line(buf,mat,x2,y1,z1,x2,y2,z1,r,g,b,a);
        line(buf,mat,x2,y1,z2,x2,y2,z2,r,g,b,a); line(buf,mat,x1,y1,z2,x1,y2,z2,r,g,b,a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.enableDepthTest(); RenderSystem.disableBlend();
        matrices.pop();
    }

    public static void drawTracer(MatrixStack matrices, Entity target, Color color, float tickDelta) {
        if (mc.gameRenderer == null || mc.player == null) return;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Vec3d to  = target.getLerpedPos(tickDelta).add(0, target.getHeight()/2, 0);
        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.5f);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        float r=color.getRed()/255f, g=color.getGreen()/255f, b=color.getBlue()/255f, a=color.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f mat = matrices.peek().getPositionMatrix();
        buf.vertex(mat,(float)cam.x,(float)(cam.y+mc.player.getEyeHeight(mc.player.getPose())),(float)cam.z).color(r,g,b,a);
        buf.vertex(mat,(float)to.x,(float)to.y,(float)to.z).color(r,g,b,a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.enableDepthTest(); RenderSystem.disableBlend();
        matrices.pop();
    }

    public static void drawLineToPos(MatrixStack matrices, Entity from, Vec3d to, Color color) {
        if (mc.gameRenderer == null) return;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Vec3d src = from.getEyePos();
        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.5f);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        float r=color.getRed()/255f, g=color.getGreen()/255f, b=color.getBlue()/255f, a=color.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f mat = matrices.peek().getPositionMatrix();
        buf.vertex(mat,(float)src.x,(float)src.y,(float)src.z).color(r,g,b,a);
        buf.vertex(mat,(float)to.x,(float)to.y,(float)to.z).color(r,g,b,a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.enableDepthTest(); RenderSystem.disableBlend();
        matrices.pop();
    }

    public static void drawLabel(MatrixStack matrices, Vec3d pos, String text, float tickDelta) {
        if (mc.gameRenderer == null || mc.player == null) return;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double dx=pos.x-cam.x, dy=pos.y-cam.y, dz=pos.z-cam.z;
        double dist = Math.sqrt(dx*dx+dy*dy+dz*dz);
        if (dist > 256) return;
        matrices.push();
        matrices.translate(dx, dy, dz);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
        float scale = (float)(dist/100.0)*0.1f;
        matrices.scale(-scale, -scale, scale);
        RenderSystem.disableDepthTest();
        mc.textRenderer.drawWithShadow(matrices, Text.literal(text), -mc.textRenderer.getWidth(text)/2f, 0, 0xFFFFFF);
        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    private static void line(BufferBuilder b, Matrix4f m,
                             double x1,double y1,double z1,
                             double x2,double y2,double z2,
                             float r,float g,float bl,float a) {
        b.vertex(m,(float)x1,(float)y1,(float)z1).color(r,g,bl,a);
        b.vertex(m,(float)x2,(float)y2,(float)z2).color(r,g,bl,a);
    }
}
