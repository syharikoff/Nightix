/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10042
 *  net.minecraft.class_11659
 *  net.minecraft.class_12075
 *  net.minecraft.class_1309
 *  net.minecraft.class_4587
 *  net.minecraft.class_742
 *  net.minecraft.class_922
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.killeffect.client.mixin;

import com.killeffect.client.KilleffectClient;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10042;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_922.class})
public abstract class PlayerDeathHideMixin {
    @Unique
    private final Set<class_10042> killeffect$hiddenStates = new HashSet<class_10042>();

    @Inject(method={"method_62355"}, at={@At(value="TAIL")})
    private void killeffect$trackDeadPlayer(class_1309 entity, class_10042 state, float tickDelta, CallbackInfo ci) {
        if (entity instanceof class_742) {
            if (KilleffectClient.shouldHideDeath(entity.method_5628())) {
                this.killeffect$hiddenStates.add(state);
            } else {
                this.killeffect$hiddenStates.remove(state);
            }
        }
    }

    @Inject(method={"method_4054"}, at={@At(value="HEAD")}, cancellable=true)
    private void killeffect$hideVanillaDeath(class_10042 state, class_4587 matrices, class_11659 orderedRenderCommandQueue, class_12075 cameraRenderState, CallbackInfo ci) {
        if (this.killeffect$hiddenStates.contains(state)) {
            ci.cancel();
        }
    }
}

