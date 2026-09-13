/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1074
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package com.killeffect.client;

import com.killeffect.client.KilleffectClient;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1074;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;

@Environment(value=EnvType.CLIENT)
public class EffectSelectScreen
extends class_437 {
    private static final List<String> EFFECT_IDS = List.of("shark_attack", "ice_shatter", "glass_shatter", "knockout_ko", "bubble_burst", "rust_decay", "quicksand", "spectral_fade", "stone_crumble", "sand_dissolve", "water_evaporation", "sound_wave_disperse", "digital_disintegration", "tentacle_grasp", "plantfood_feasting", "tertis_smash", "light_absorption", "astral_projection", "arcade_gameover", "dissolve_into_ash", "hellfire_burn", "angelic_bless", "colorful_explosion", "feather_scatter", "hologram_flicker_out", "liquid_meltdown", "imposter_instinct", "kfx_abstracted", "kfx_acidic_corrosion", "kfx_clockwork_disassembly", "kfx_frost_infection", "kfx_graffiti_spray", "kfx_ink_blots", "kfx_magnetic_resonance", "kfx_origami_fold", "kfx_pure_form");
    private static final int PANEL_W = 210;
    private static final int BTN_H = 22;
    private static final int BTN_GAP = 3;
    private static final int PAD = 10;
    private static final int TITLE_H = 36;
    private static final int SCROLL_W = 6;
    private static final int VISIBLE = 10;
    private float scrollOffset = 0.0f;
    private float scrollSmooth = 0.0f;
    private boolean draggingScrollbar = false;
    private int dragStartY;
    private float dragStartScroll;
    private final float[] hover = new float[EFFECT_IDS.size()];
    private long lastNanos = 0L;
    private int panelX;
    private int panelY;
    private int panelH;
    private int listH;

    public EffectSelectScreen() {
        super((class_2561)class_2561.method_43473());
    }

    public boolean method_25421() {
        return false;
    }

    private int visibleCount() {
        return Math.min(10, EFFECT_IDS.size());
    }

    private int maxScroll() {
        return Math.max(0, EFFECT_IDS.size() - this.visibleCount());
    }

    private void layout(int sw, int sh) {
        this.listH = this.visibleCount() * 25 - 3;
        this.panelH = 46 + this.listH + 10;
        this.panelX = (sw - 210) / 2;
        this.panelY = (sh - this.panelH) / 2;
    }

    private int listX() {
        return this.panelX + 10;
    }

    private int listW() {
        return 181;
    }

    private int listTop() {
        return this.panelY + 36 + 10;
    }

    private int btnScreenY(int i) {
        return this.listTop() + (int)(((float)i - this.scrollSmooth) * 25.0f);
    }

    private boolean btnVisible(int i) {
        int y = this.btnScreenY(i);
        return y + 22 > this.listTop() && y < this.listTop() + this.listH;
    }

    private int scrollBarX() {
        return this.panelX + 210 - 10 - 6;
    }

    private int scrollBarTopY() {
        return this.listTop();
    }

    private int scrollBarH() {
        return this.listH;
    }

    private int thumbH() {
        if (this.maxScroll() == 0) {
            return this.scrollBarH();
        }
        return Math.max(20, this.scrollBarH() * this.visibleCount() / EFFECT_IDS.size());
    }

    private int thumbY() {
        if (this.maxScroll() == 0) {
            return this.scrollBarTopY();
        }
        return this.scrollBarTopY() + (int)(this.scrollSmooth / (float)this.maxScroll() * (float)(this.scrollBarH() - this.thumbH()));
    }

    private static String effectName(String id) {
        return class_1074.method_4662((String)("killeffect.effect." + id), (Object[])new Object[0]);
    }

    public boolean method_25401(double mx, double my, double hScroll, double vScroll) {
        this.scrollOffset = (float)Math.max(0.0, Math.min((double)this.maxScroll(), (double)this.scrollOffset - vScroll));
        return true;
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        int ix = (int)click.comp_4798();
        int iy = (int)click.comp_4799();
        if (ix >= this.scrollBarX() && ix <= this.scrollBarX() + 6 && iy >= this.thumbY() && iy <= this.thumbY() + this.thumbH()) {
            this.draggingScrollbar = true;
            this.dragStartY = iy;
            this.dragStartScroll = this.scrollOffset;
            return true;
        }
        if (ix < this.panelX || ix > this.panelX + 210 || iy < this.panelY || iy > this.panelY + this.panelH) {
            this.method_25419();
            return true;
        }
        for (int i = 0; i < EFFECT_IDS.size(); ++i) {
            if (!this.btnVisible(i)) continue;
            int by = this.btnScreenY(i);
            if (ix < this.listX() || ix > this.listX() + this.listW() || iy < by || iy > by + 22) continue;
            KilleffectClient.selectedEffectId = EFFECT_IDS.get(i);
            this.method_25419();
            return true;
        }
        return true;
    }

    public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
        if (this.draggingScrollbar && this.maxScroll() > 0) {
            float ratio = (float)(click.comp_4799() - (double)this.dragStartY) / (float)(this.scrollBarH() - this.thumbH());
            this.scrollOffset = Math.max(0.0f, Math.min((float)this.maxScroll(), this.dragStartScroll + ratio * (float)this.maxScroll()));
        }
        return super.method_25403(click, offsetX, offsetY);
    }

    public boolean method_25406(class_11909 click) {
        this.draggingScrollbar = false;
        return super.method_25406(click);
    }

    public void method_25394(class_332 ctx, int mx, int my, float delta) {
        int i;
        long now = System.nanoTime();
        float dt = this.lastNanos == 0L ? 0.0f : (float)(now - this.lastNanos) / 1.0E9f;
        this.lastNanos = now;
        this.layout(this.field_22789, this.field_22790);
        this.scrollSmooth += (this.scrollOffset - this.scrollSmooth) * Math.min(1.0f, dt * 16.0f);
        for (i = 0; i < EFFECT_IDS.size(); ++i) {
            int by = this.btnScreenY(i);
            boolean hov = this.btnVisible(i) && mx >= this.listX() && mx <= this.listX() + this.listW() && my >= by && my <= by + 22;
            int n = i;
            this.hover[n] = this.hover[n] + ((hov ? 1.0f : 0.0f) - this.hover[i]) * Math.min(1.0f, dt * 14.0f);
        }
        ctx.method_25294(0, 0, this.field_22789, this.field_22790, -2013265920);
        ctx.method_25294(this.panelX + 3, this.panelY + 3, this.panelX + 3 + 210, this.panelY + 3 + this.panelH, 0x55000000);
        ctx.method_25294(this.panelX - 1, this.panelY - 1, this.panelX + 210 + 1, this.panelY + this.panelH + 1, -11915152);
        ctx.method_25294(this.panelX, this.panelY, this.panelX + 210, this.panelY + this.panelH, -15462370);
        ctx.method_25294(this.panelX, this.panelY, this.panelX + 210, this.panelY + 36, -14018491);
        ctx.method_25294(this.panelX, this.panelY + 36 - 1, this.panelX + 210, this.panelY + 36, -8956468);
        ctx.method_25294(this.listX(), this.listTop(), this.listX() + this.listW(), this.listTop() + this.listH, -15922666);
        for (i = 0; i < EFFECT_IDS.size(); ++i) {
            if (!this.btnVisible(i)) continue;
            boolean sel = EFFECT_IDS.get(i).equals(KilleffectClient.selectedEffectId);
            float h = this.hover[i];
            int bx = this.listX();
            int by = this.btnScreenY(i);
            int bw = this.listW();
            int bg = sel ? EffectSelectScreen.lerpColor(-14018475, -12769920, h) : EffectSelectScreen.lerpColor(-15067867, -14410443, h);
            ctx.method_25294(bx, by, bx + bw, by + 22, bg);
            int bc = sel ? -7842322 : (h > 0.1f ? -11189112 : -14016704);
            ctx.method_25294(bx, by, bx + bw, by + 1, bc);
            ctx.method_25294(bx, by + 22 - 1, bx + bw, by + 22, bc);
            ctx.method_25294(bx, by, bx + 1, by + 22, bc);
            ctx.method_25294(bx + bw - 1, by, bx + bw, by + 22, bc);
            if (!sel) continue;
            ctx.method_25294(bx, by + 2, bx + 3, by + 22 - 2, -5605377);
        }
        ctx.method_25296(this.listX(), this.listTop(), this.listX() + this.listW(), this.listTop() + 8, -15922666, 854550);
        ctx.method_25296(this.listX(), this.listTop() + this.listH - 8, this.listX() + this.listW(), this.listTop() + this.listH, 854550, -15922666);
        if (this.maxScroll() > 0) {
            int sbx = this.scrollBarX();
            ctx.method_25294(sbx, this.scrollBarTopY(), sbx + 6, this.scrollBarTopY() + this.scrollBarH(), -15067856);
            int ty = this.thumbY();
            ctx.method_25294(sbx, ty, sbx + 6, ty + this.thumbH(), -10074966);
            ctx.method_25294(sbx, ty, sbx + 6, ty + 1, -6723858);
        }
        class_327 tr = class_310.method_1551().field_1772;
        String title = class_1074.method_4662((String)"category.killeffect", (Object[])new Object[0]).toUpperCase();
        int n = this.panelX + (210 - tr.method_1727(title)) / 2;
        Objects.requireNonNull(tr);
        ctx.method_25303(tr, title, n, this.panelY + (18 - 9), -1122817);
        Object sub = "\u00bb " + EffectSelectScreen.effectName(KilleffectClient.selectedEffectId);
        sub = EffectSelectScreen.clamp(tr, (String)sub, 190);
        ctx.method_25303(tr, (String)sub, this.panelX + (210 - tr.method_1727((String)sub)) / 2, this.panelY + 18 + 1, -7837014);
        ctx.method_44379(this.listX(), this.listTop(), this.listX() + this.listW(), this.listTop() + this.listH);
        for (int i2 = 0; i2 < EFFECT_IDS.size(); ++i2) {
            if (!this.btnVisible(i2)) continue;
            boolean sel = EFFECT_IDS.get(i2).equals(KilleffectClient.selectedEffectId);
            int bx = this.listX();
            int by = this.btnScreenY(i2);
            int bw = this.listW();
            int labelColor = sel ? -2249985 : (this.hover[i2] > 0.5f ? -1 : -3355444);
            String name = EffectSelectScreen.clamp(tr, EffectSelectScreen.effectName(EFFECT_IDS.get(i2)), bw - 10 - (sel ? tr.method_1727("ACTIVE") + 6 : 0));
            Objects.requireNonNull(tr);
            ctx.method_25303(tr, name, bx + 8, by + (22 - 9) / 2, labelColor);
            if (!sel) continue;
            String badge = "ACTIVE";
            int n2 = bx + bw - 4 - tr.method_1727(badge);
            Objects.requireNonNull(tr);
            ctx.method_25303(tr, badge, n2, by + (22 - 9) / 2, -6723841);
        }
        ctx.method_44380();
    }

    private static int lerpColor(int c1, int c2, float t) {
        int a1 = c1 >> 24 & 0xFF;
        int r1 = c1 >> 16 & 0xFF;
        int g1 = c1 >> 8 & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = c2 >> 24 & 0xFF;
        int r2 = c2 >> 16 & 0xFF;
        int g2 = c2 >> 8 & 0xFF;
        int b2 = c2 & 0xFF;
        return (int)((float)a1 + (float)(a2 - a1) * t) << 24 | (int)((float)r1 + (float)(r2 - r1) * t) << 16 | (int)((float)g1 + (float)(g2 - g1) * t) << 8 | (int)((float)b1 + (float)(b2 - b1) * t);
    }

    private static String clamp(class_327 tr, String text, int maxW) {
        if (tr.method_1727(text) <= maxW) {
            return text;
        }
        while (!text.isEmpty() && tr.method_1727(text + "\u2026") > maxW) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "\u2026";
    }
}

