package tenko.client.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tenko.client.TenkoClient;
import tenko.client.module.modules.combat.Reach;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "getBlockInteractionRange", at = @At("RETURN"), cancellable = true)
    private void onBlockRange(CallbackInfoReturnable<Double> cir) {
        Reach r = TenkoClient.moduleManager.getModule(Reach.class);
        if (r != null && r.isEnabled()) cir.setReturnValue(cir.getReturnValue() + r.extra.get());
    }

    @Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void onEntityRange(CallbackInfoReturnable<Double> cir) {
        Reach r = TenkoClient.moduleManager.getModule(Reach.class);
        if (r != null && r.isEnabled()) cir.setReturnValue(cir.getReturnValue() + r.extra.get());
    }
}
