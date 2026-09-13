/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_243
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 *  net.minecraft.class_742
 *  net.minecraft.class_746
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.killeffect.client;

import com.killeffect.client.BBModel;
import com.killeffect.client.EffectCatalog;
import com.killeffect.client.EffectManager;
import com.killeffect.client.EffectRenderer;
import com.killeffect.client.EffectSelectScreen;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_742;
import net.minecraft.class_746;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
public class KilleffectClient
implements ClientModInitializer {
    public static final String MOD_ID = "killeffect";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"killeffect");
    public static final EffectManager EFFECTS = new EffectManager();
    public static String selectedEffectId = "shark_attack";
    private static class_304 debugKey;
    private static class_304 guiKey;
    private static final Map<Integer, Long> lastDeath;
    private static final long DEATH_COOLDOWN_MS = 3000L;
    private static final class_304.class_11900 CATEGORY;

    public static void onPlayerDeadId(int entityId, class_243 deathPos) {
        long now = System.currentTimeMillis();
        Long last = lastDeath.get(entityId);
        if (last != null && now - last < 3000L) {
            return;
        }
        lastDeath.put(entityId, now);
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null) {
            return;
        }
        KilleffectClient.spawnEffect(deathPos, client.field_1724);
    }

    public static boolean hasActiveEffect() {
        return !EFFECTS.active().isEmpty();
    }

    public static boolean shouldHideDeath(int entityId) {
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null) {
            return false;
        }
        for (class_742 player : client.field_1687.method_18456()) {
            if (player.method_5628() != entityId || !player.method_29504() && player.field_6213 <= 0) continue;
            return KilleffectClient.hasActiveEffect();
        }
        return false;
    }

    public void onInitializeClient() {
        EffectCatalog.registerAll();
        EffectRenderer.register();
        try {
            BBModel shark = EffectCatalog.get("shark_attack").model();
            LOGGER.info("Loaded model '{}': {} cubes, {} roots, {} textures, {} animations", new Object[]{shark.name, shark.cubes.size(), shark.roots.size(), shark.textures.size(), shark.animations.size()});
        }
        catch (Exception e) {
            LOGGER.error("Failed to load shark_attack model", (Throwable)e);
        }
        debugKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.killeffect.debug_spawn", class_3675.class_307.field_1668, 75, CATEGORY));
        guiKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.killeffect.open_gui", class_3675.class_307.field_1668, 74, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        LOGGER.info("Kill Effect mod initialized ({} effects registered)", (Object)EffectCatalog.all().size());
    }

    private void tick(class_310 client) {
        if (client.field_1687 == null || client.field_1724 == null) {
            return;
        }
        while (guiKey.method_1436()) {
            client.method_1507((class_437)new EffectSelectScreen());
        }
        for (class_742 player : client.field_1687.method_18456()) {
            if (!player.method_29504() && !(player.method_6032() <= 0.0f)) continue;
            KilleffectClient.onPlayerDeadId(player.method_5628(), player.method_73189());
        }
        while (debugKey.method_1436()) {
            this.spawnDebug(client);
        }
    }

    public static void spawnEffect(class_243 deathPos, class_746 localPlayer) {
        EffectCatalog.Effect effect = EffectCatalog.get(selectedEffectId);
        if (effect == null) {
            effect = EffectCatalog.get("shark_attack");
        }
        if (effect == null) {
            return;
        }
        double dx = localPlayer.method_23317() - deathPos.field_1352;
        double dz = localPlayer.method_23321() - deathPos.field_1350;
        float yawToPlayer = (float)Math.toDegrees(Math.atan2(dx, dz));
        EFFECTS.spawn(effect, deathPos, yawToPlayer);
    }

    private void spawnDebug(class_310 client) {
        EffectCatalog.Effect effect = EffectCatalog.get(selectedEffectId);
        if (effect == null) {
            effect = EffectCatalog.get("shark_attack");
        }
        if (effect == null) {
            return;
        }
        class_243 pos = client.field_1724.method_73189();
        float yaw = client.field_1724.method_36454();
        double rad = Math.toRadians(yaw);
        pos = pos.method_1031(-Math.sin(rad) * 5.0, 0.0, Math.cos(rad) * 5.0);
        double dx = client.field_1724.method_23317() - pos.field_1352;
        double dz = client.field_1724.method_23321() - pos.field_1350;
        float yawToPlayer = (float)Math.toDegrees(Math.atan2(dx, dz));
        EFFECTS.spawn(effect, pos, yawToPlayer);
        LOGGER.info("Debug: spawned '{}' at {}", (Object)effect.id(), (Object)pos);
    }

    static {
        lastDeath = new HashMap<Integer, Long>();
        CATEGORY = class_304.class_11900.method_74698((class_2960)class_2960.method_60655((String)MOD_ID, (String)"category"));
    }
}

