package ru.white.optimization;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

public class FrameSyncManager {
    private static FrameSyncManager instance;
    private int monitorHz;
    private long lastRenderTime;
    private long frameInterval;
    private boolean enabled = true;

    private FrameSyncManager() {
        this.monitorHz = detectMonitorHz();
        this.frameInterval = 1000000000L / (long) this.monitorHz;
        this.lastRenderTime = System.nanoTime();
    }

    public static FrameSyncManager getInstance() {
        if (instance == null) {
            instance = new FrameSyncManager();
        }
        return instance;
    }

    public boolean shouldRender() {
        if (!this.enabled) {
            return true;
        }
        long now = System.nanoTime();
        if (now - this.lastRenderTime >= this.frameInterval) {
            this.lastRenderTime = now;
            return true;
        }
        return false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            this.lastRenderTime = System.nanoTime();
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getMonitorHz() {
        return this.monitorHz;
    }

    private static int detectMonitorHz() {
        try {
            if (!GLFW.glfwInit()) {
                return 60;
            }
            long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
            if (primaryMonitor == 0L) {
                return 60;
            }
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(primaryMonitor);
            if (vidMode != null) {
                return vidMode.refreshRate();
            }
        } catch (Exception e) {
            // fallback
        }
        return 60;
    }
}
