/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_243
 */
package com.killeffect.client;

import com.killeffect.client.EffectCatalog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;

@Environment(value=EnvType.CLIENT)
public final class EffectManager {
    private final List<ActiveEffect> active = new ArrayList<ActiveEffect>();

    public void spawn(EffectCatalog.Effect effect, class_243 position, float spawnYaw) {
        this.active.add(new ActiveEffect(effect, position, System.currentTimeMillis(), spawnYaw));
    }

    public List<ActiveEffect> active() {
        Iterator<ActiveEffect> it = this.active.iterator();
        while (it.hasNext()) {
            if (!it.next().isFinished()) continue;
            it.remove();
        }
        return this.active;
    }

    @Environment(value=EnvType.CLIENT)
    public record ActiveEffect(EffectCatalog.Effect effect, class_243 position, long startMillis, float spawnYaw) {
        public float timeSeconds() {
            return (float)(System.currentTimeMillis() - this.startMillis) / 1000.0f;
        }

        public boolean isFinished() {
            return (double)this.timeSeconds() > this.effect.duration();
        }
    }
}

