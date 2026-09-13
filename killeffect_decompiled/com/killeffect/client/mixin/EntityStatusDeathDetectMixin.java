/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_2663
 *  net.minecraft.class_310
 *  net.minecraft.class_634
 *  net.minecraft.class_742
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.killeffect.client.mixin;

import com.killeffect.client.KilleffectClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2663;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_742;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_634.class})
public abstract class EntityStatusDeathDetectMixin {
    @Inject(method={"method_11148"}, at={@At(value="HEAD")})
    private void killeffect$onEntityStatus(class_2663 packet, CallbackInfo ci) {
        if (packet.method_11470() != 3) {
            return;
        }
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null || client.field_1724 == null) {
            return;
        }
        class_1297 entity = packet.method_11469((class_1937)client.field_1687);
        if (entity instanceof class_742) {
            class_742 player = (class_742)entity;
            KilleffectClient.onPlayerDeadId(player.method_5628(), player.method_73189());
        }
    }
}

