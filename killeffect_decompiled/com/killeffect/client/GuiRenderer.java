/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.opengl.GlStateManager
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL15
 *  org.lwjgl.opengl.GL20
 *  org.lwjgl.opengl.GL30
 *  org.lwjgl.system.MemoryUtil
 */
package com.killeffect.client;

import com.killeffect.client.KilleffectClient;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public final class GuiRenderer {
    private static final String VERT_SRC = "#version 150 core\nin vec2 aPos;\nin vec2 aUV;\nuniform mat4 uProjection;\nout vec2 vUV;\nvoid main() {\n    gl_Position = uProjection * vec4(aPos, 0.0, 1.0);\n    vUV = aUV;\n}\n";
    private static final String FRAG_SRC = "#version 150 core\nin vec2 vUV;\nuniform vec4  uColor;\nuniform vec4  uColor2;\nuniform float uRadius;\nuniform vec2  uSize;\nuniform float uAlpha;\nout vec4 fragColor;\n\nfloat roundedBox(vec2 p, vec2 halfSize, float r) {\n    vec2 q = abs(p) - halfSize + r;\n    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;\n}\n\nvoid main() {\n    vec2 p       = (vUV - 0.5) * uSize;\n    float d      = roundedBox(p, uSize * 0.5, uRadius);\n    float aa     = length(vec2(dFdx(d), dFdy(d)));\n    float shape  = 1.0 - smoothstep(-aa, aa, d);\n\n    vec4 col = mix(uColor, uColor2, vUV.y);\n    fragColor = vec4(col.rgb, col.a * shape * uAlpha);\n}\n";
    private int program = -1;
    private int vao = -1;
    private int vbo = -1;
    private int locProjection;
    private int locColor;
    private int locColor2;
    private int locRadius;
    private int locSize;
    private int locAlpha;
    private boolean initialised = false;

    public void init() {
        if (this.initialised) {
            return;
        }
        this.program = GuiRenderer.buildProgram(VERT_SRC, FRAG_SRC);
        this.locProjection = GL20.glGetUniformLocation((int)this.program, (CharSequence)"uProjection");
        this.locColor = GL20.glGetUniformLocation((int)this.program, (CharSequence)"uColor");
        this.locColor2 = GL20.glGetUniformLocation((int)this.program, (CharSequence)"uColor2");
        this.locRadius = GL20.glGetUniformLocation((int)this.program, (CharSequence)"uRadius");
        this.locSize = GL20.glGetUniformLocation((int)this.program, (CharSequence)"uSize");
        this.locAlpha = GL20.glGetUniformLocation((int)this.program, (CharSequence)"uAlpha");
        this.vao = GL30.glGenVertexArrays();
        this.vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray((int)this.vao);
        GL15.glBindBuffer((int)34962, (int)this.vbo);
        GL15.glBufferData((int)34962, (long)64L, (int)35048);
        GL20.glEnableVertexAttribArray((int)0);
        GL20.glVertexAttribPointer((int)0, (int)2, (int)5126, (boolean)false, (int)16, (long)0L);
        GL20.glEnableVertexAttribArray((int)1);
        GL20.glVertexAttribPointer((int)1, (int)2, (int)5126, (boolean)false, (int)16, (long)8L);
        GL30.glBindVertexArray((int)0);
        GL15.glBindBuffer((int)34962, (int)0);
        this.initialised = true;
    }

    public void destroy() {
        if (!this.initialised) {
            return;
        }
        GL20.glDeleteProgram((int)this.program);
        GL15.glDeleteBuffers((int)this.vbo);
        GL30.glDeleteVertexArrays((int)this.vao);
        this.initialised = false;
    }

    public void beginFrame(int fbWidth, int fbHeight) {
        this.init();
        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        GL11.glDisable((int)2929);
        GL20.glUseProgram((int)this.program);
        float[] proj = GuiRenderer.ortho(0.0f, fbWidth, fbHeight, 0.0f, -1.0f, 1.0f);
        GL20.glUniformMatrix4fv((int)this.locProjection, (boolean)false, (float[])proj);
    }

    public void endFrame() {
        GL20.glUseProgram((int)0);
        GL30.glBindVertexArray((int)0);
        GL11.glEnable((int)2929);
        GlStateManager._disableBlend();
    }

    public void drawRect(float x, float y, float w, float h, float radius, float r, float g, float b, float a, float r2, float g2, float b2, float a2, float alpha) {
        float[] verts = new float[]{x, y, 0.0f, 0.0f, x + w, y, 1.0f, 0.0f, x, y + h, 0.0f, 1.0f, x + w, y + h, 1.0f, 1.0f};
        FloatBuffer buf = MemoryUtil.memAllocFloat((int)verts.length);
        buf.put(verts).flip();
        GL15.glBindBuffer((int)34962, (int)this.vbo);
        GL15.glBufferSubData((int)34962, (long)0L, (FloatBuffer)buf);
        GL15.glBindBuffer((int)34962, (int)0);
        MemoryUtil.memFree((Buffer)buf);
        GL20.glUniform4f((int)this.locColor, (float)r, (float)g, (float)b, (float)a);
        GL20.glUniform4f((int)this.locColor2, (float)r2, (float)g2, (float)b2, (float)a2);
        GL20.glUniform1f((int)this.locRadius, (float)radius);
        GL20.glUniform2f((int)this.locSize, (float)w, (float)h);
        GL20.glUniform1f((int)this.locAlpha, (float)alpha);
        GL30.glBindVertexArray((int)this.vao);
        GL11.glDrawArrays((int)5, (int)0, (int)4);
        GL30.glBindVertexArray((int)0);
    }

    public void drawRect(float x, float y, float w, float h, float radius, float r, float g, float b, float a, float alpha) {
        this.drawRect(x, y, w, h, radius, r, g, b, a, r, g, b, a, alpha);
    }

    private static int buildProgram(String vertSrc, String fragSrc) {
        int vert = GuiRenderer.compileShader(35633, vertSrc);
        int frag = GuiRenderer.compileShader(35632, fragSrc);
        int prog = GL20.glCreateProgram();
        GL20.glAttachShader((int)prog, (int)vert);
        GL20.glAttachShader((int)prog, (int)frag);
        GL20.glBindAttribLocation((int)prog, (int)0, (CharSequence)"aPos");
        GL20.glBindAttribLocation((int)prog, (int)1, (CharSequence)"aUV");
        GL20.glLinkProgram((int)prog);
        if (GL20.glGetProgrami((int)prog, (int)35714) == 0) {
            KilleffectClient.LOGGER.error("GUI shader link error: {}", (Object)GL20.glGetProgramInfoLog((int)prog));
        }
        GL20.glDeleteShader((int)vert);
        GL20.glDeleteShader((int)frag);
        return prog;
    }

    private static int compileShader(int type, String src) {
        int id = GL20.glCreateShader((int)type);
        GL20.glShaderSource((int)id, (CharSequence)src);
        GL20.glCompileShader((int)id);
        if (GL20.glGetShaderi((int)id, (int)35713) == 0) {
            KilleffectClient.LOGGER.error("GUI shader compile error ({}): {}", (Object)(type == 35633 ? "vert" : "frag"), (Object)GL20.glGetShaderInfoLog((int)id));
        }
        return id;
    }

    private static float[] ortho(float l, float r, float b, float t, float near, float far) {
        return new float[]{2.0f / (r - l), 0.0f, 0.0f, 0.0f, 0.0f, 2.0f / (t - b), 0.0f, 0.0f, 0.0f, 0.0f, -2.0f / (far - near), 0.0f, -(r + l) / (r - l), -(t + b) / (t - b), -(far + near) / (far - near), 1.0f};
    }
}

