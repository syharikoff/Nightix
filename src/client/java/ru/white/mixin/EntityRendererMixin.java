package ru.white.mixin;


import ru.white.module.impl.render.NameTag;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void onRenderLabelIfPresent(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        NameTag esp = NameTag.get();
        if (esp == null || !esp.isEnabled()) return;
        if (state.entityType != EntityType.PLAYER || !esp.player.getValue()) return;
        if (state.displayName == null) {
            ci.cancel();
        }
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void hookNametag(T entity, CallbackInfoReturnable<Text> cir) {
        NameTag esp = NameTag.get();
        if (esp == null || !esp.isEnabled()) return;
        if (entity instanceof PlayerEntity p && esp.player.getValue()) {
            if (esp.ignoreNaked.getValue() && !NameTag.hasArmor(p)) return;
            cir.setReturnValue(null);
        }
    }
}