/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.killeffect.client;

import com.killeffect.client.BBModel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class EffectCatalog {
    private static final Map<String, Effect> EFFECTS = new LinkedHashMap<String, Effect>();

    private EffectCatalog() {
    }

    public static void register(String id, String modelId, String namespace, float scale, double duration) {
        BBModel model = BBModel.load(modelId, namespace, "models/effects");
        EFFECTS.put(id, new Effect(id, modelId, namespace, scale, duration, model));
    }

    public static Effect get(String id) {
        return EFFECTS.get(id);
    }

    public static List<Effect> all() {
        return List.copyOf(EFFECTS.values());
    }

    public static void registerAll() {
        String ns = "killeffect";
        EffectCatalog.register("shark_attack", "shark_attack", ns, 1.0f, 3.0);
        EffectCatalog.register("ice_shatter", "ice_shatter", ns, 1.0f, 3.0);
        EffectCatalog.register("glass_shatter", "glass_shatter", ns, 1.0f, 3.0);
        EffectCatalog.register("rust_decay", "rust_decay", ns, 1.0f, 3.0);
        EffectCatalog.register("quicksand", "quicksand", ns, 1.0f, 3.0);
        EffectCatalog.register("knockout_ko", "knockout_ko", ns, 1.0f, 3.0);
        EffectCatalog.register("bubble_burst", "bubble_burst", ns, 1.0f, 3.0);
        EffectCatalog.register("spectral_fade", "spectral_fade", ns, 1.0f, 3.0);
        EffectCatalog.register("energy_dissipation", "energy_dissipation", ns, 1.0f, 3.0);
        EffectCatalog.register("stone_crumble", "stone_crumble", ns, 1.0f, 3.0);
        EffectCatalog.register("sand_dissolve", "sand_dissolve", ns, 1.0f, 3.0);
        EffectCatalog.register("water_evaporation", "water_evaporation", ns, 1.0f, 3.0);
        EffectCatalog.register("sound_wave_disperse", "sound_wave_disperse", ns, 1.0f, 3.0);
        EffectCatalog.register("digital_disintegration", "digital_disintegration", ns, 1.0f, 3.0);
        EffectCatalog.register("tentacle_grasp", "tentacle_grasp", ns, 1.0f, 3.0);
        EffectCatalog.register("plantfood_feasting", "plantfood_feasting", ns, 1.0f, 3.0);
        EffectCatalog.register("tertis_smash", "tertis_smash", ns, 1.0f, 3.0);
        EffectCatalog.register("light_absorption", "light_absorption", ns, 1.0f, 3.0);
        EffectCatalog.register("astral_projection", "astral_projection", ns, 1.0f, 3.0);
        EffectCatalog.register("arcade_gameover", "arcade_gameover", ns, 1.0f, 3.0);
        EffectCatalog.register("angelic_bless", "angelic_bless", ns, 1.0f, 3.0);
        EffectCatalog.register("liquid_meltdown", "liquid_meltdown", ns, 1.0f, 3.0);
        EffectCatalog.register("hellfire_burn", "hellfire_burn", ns, 1.0f, 3.0);
        EffectCatalog.register("imposter_instinct", "imposter_instinct", ns, 1.0f, 3.0);
        EffectCatalog.register("colorful_explosion", "colorful_explosion", ns, 1.0f, 3.0);
        EffectCatalog.register("feather_scatter", "feather_scatter", ns, 1.0f, 3.0);
        EffectCatalog.register("nature_reclaim", "nature_reclaim", ns, 1.0f, 3.0);
        EffectCatalog.register("dissolve_into_ash", "dissolve_into_ash", ns, 1.0f, 3.0);
        EffectCatalog.register("hologram_flicker_out", "hologram_flicker_out", ns, 1.0f, 3.0);
        EffectCatalog.register("liquid_meltdown", "liquid_meltdown", ns, 1.0f, 3.0);
        EffectCatalog.register("imposter_instinct", "imposter_instinct", ns, 1.0f, 3.0);
        EffectCatalog.register("kfx_abstracted", "kfx_abstracted", ns, 1.0f, 3.0);
        EffectCatalog.register("kfx_acidic_corrosion", "kfx_acidic_corrosion", ns, 1.0f, 3.0);
        EffectCatalog.register("kfx_clockwork_disassembly", "kfx_clockwork_disassembly", ns, 1.0f, 3.0);
        EffectCatalog.register("kfx_frost_infection", "kfx_frost_infection", ns, 1.0f, 3.0);
        EffectCatalog.register("kfx_graffiti_spray", "kfx_graffiti_spray", ns, 1.0f, 3.0);
        EffectCatalog.register("kfx_ink_blots", "kfx_ink_blots", ns, 1.0f, 3.0);
        EffectCatalog.register("kfx_magnetic_resonance", "kfx_magnetic_resonance", ns, 1.0f, 3.0);
        EffectCatalog.register("kfx_origami_fold", "kfx_origami_fold", ns, 1.0f, 3.0);
        EffectCatalog.register("kfx_pure_form", "kfx_pure_form", ns, 1.0f, 3.0);
    }

    @Environment(value=EnvType.CLIENT)
    public record Effect(String id, String modelId, String namespace, float scale, double duration, BBModel model) {
    }
}

