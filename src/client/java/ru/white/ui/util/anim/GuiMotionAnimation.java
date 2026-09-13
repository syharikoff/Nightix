package ru.white.ui.util.anim;

public final class GuiMotionAnimation {
    private static final long DURATION_NS = 500000000L;
    private static final double OPEN_START_SCALE = 1.25;
    private static final double REST_SCALE = 1.0;
    private static final double CLOSE_END_SCALE = 0.75;
    private static final float SCALE_MIN = 0.8F;
    private static final float SCALE_RANGE = 0.2F;
    private static final float MAX_BLUR_RADIUS = 9.0F;
    private static final Ease CUBIC_OUT = d -> 1.0 - Math.pow(1.0 - d, 3.0);
    private static final Ease CUBIC_IN = d -> Math.pow(d, 3.0);
    private static final Ease QUINT_OUT = d -> 1.0 - Math.pow(1.0 - d, 5.0);
    private static final Ease BACK_IN = d -> 2.70158 * Math.pow(d, 3.0) - 1.70158 * Math.pow(d, 2.0);
    private long startNs = System.nanoTime() - 500000000L;
    private double alphaFrom;
    private double alphaTo;
    private Ease alphaEase = CUBIC_OUT;
    private double scaleFrom = 1.25;
    private double scaleTo = 1.0;
    private Ease scaleEase = QUINT_OUT;
    private boolean closing;
    private float frameAlpha;
    private float frameScale = 1.0F;
    private float frameBlurRadius;
    private double frameProgress = 1.0;

    public float scale() {
        return this.frameScale;
    }

    public float alpha() {
        return this.frameAlpha;
    }

    private static float clamp01(float f) {
        return Math.max(0.0F, Math.min(1.0F, f));
    }

    public float blurRadius() {
        return this.frameBlurRadius;
    }

    public void snapOpen() {
        this.closing = false;
        this.alphaFrom = 1.0;
        this.alphaTo = 1.0;
        this.scaleFrom = 1.0;
        this.scaleTo = 1.0;
        this.startNs = System.nanoTime() - 500000000L;
        this.updateFrame();
    }

    public void snapClosed() {
        this.closing = false;
        this.alphaFrom = 0.0;
        this.alphaTo = 0.0;
        this.scaleFrom = 0.75;
        this.scaleTo = 0.75;
        this.startNs = System.nanoTime() - 500000000L;
        this.updateFrame();
    }

    public boolean isClosing() {
        return this.closing;
    }

    private double currentScaleRaw() {
        return this.scaleFrom + (this.scaleTo - this.scaleFrom) * this.scaleEase.apply(this.progress());
    }

    private double currentAlpha() {
        return this.alphaFrom + (this.alphaTo - this.alphaFrom) * this.alphaEase.apply(this.progress());
    }

    public void resumeOpening() {
        double a = this.currentAlpha();
        double s = this.currentScaleRaw();
        this.closing = false;
        this.startNs = System.nanoTime();
        this.alphaFrom = a;
        this.alphaTo = 1.0;
        this.alphaEase = CUBIC_OUT;
        this.scaleFrom = s;
        this.scaleTo = 1.0;
        this.scaleEase = QUINT_OUT;
        this.updateFrame();
    }

    public void startClosing() {
        if (!this.closing) {
            double a = this.currentAlpha();
            double s = this.currentScaleRaw();
            this.closing = true;
            this.startNs = System.nanoTime();
            this.alphaFrom = a;
            this.alphaTo = 0.0;
            this.alphaEase = CUBIC_IN;
            this.scaleFrom = s;
            this.scaleTo = 0.75;
            this.scaleEase = BACK_IN;
            this.updateFrame();
        }
    }

    public boolean isCloseFinished() {
        return this.closing && this.frameProgress >= 1.0;
    }

    public boolean isAnimating() {
        return this.frameProgress < 1.0;
    }

    public void startOpening() {
        this.closing = false;
        this.startNs = System.nanoTime();
        this.alphaFrom = 0.0;
        this.alphaTo = 1.0;
        this.alphaEase = CUBIC_OUT;
        this.scaleFrom = 1.25;
        this.scaleTo = 1.0;
        this.scaleEase = QUINT_OUT;
        this.updateFrame();
    }

    public float closeProgress() {
        return this.closing ? (float) this.frameProgress : 0.0F;
    }

    public void updateFrame() {
        this.frameProgress = this.progress();
        double a = this.alphaFrom + (this.alphaTo - this.alphaFrom) * this.alphaEase.apply(this.frameProgress);
        double s = this.scaleFrom + (this.scaleTo - this.scaleFrom) * this.scaleEase.apply(this.frameProgress);
        this.frameAlpha = clamp01((float) a);
        this.frameScale = 0.8F + (float) s * 0.2F;
        this.frameBlurRadius = 9.0F * (1.0F - this.frameAlpha);
    }

    public boolean canInteract() {
        return !this.closing && this.frameAlpha >= 0.9F;
    }

    private double progress() {
        long l = System.nanoTime() - this.startNs;
        if (l >= 500000000L) {
            return 1.0;
        } else {
            return l <= 0L ? 0.0 : l / 5.0E8;
        }
    }

    public interface Ease {
        double apply(double progress);
    }
}