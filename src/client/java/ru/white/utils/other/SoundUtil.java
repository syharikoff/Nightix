package ru.white.utils.other;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;

public class SoundUtil {
    // Используем потокобезопасный список, чтобы избежать ошибок при удалении
    private static final CopyOnWriteArrayList<Clip> CLIPS_LIST = new CopyOnWriteArrayList<>();

    public static void playSound_wav(String location, float volume) {
        playSound_wav(location, volume, 1.0F);
    }

    /**
     * Тот же звук, но с изменённой высотой тона: pitch > 1 — выше и короче,
     * pitch < 1 — ниже и длиннее. Позволяет из пары семплов собрать целую
     * палитру звуков для интерфейса.
     */
    public static void playSound_wav(String location, float volume, float pitch) {
        // Запускаем в асинхронном режиме, чтобы не фризить игру/приложение
        CompletableFuture.runAsync(() -> {
            try {
                cleanUpClips();

                String resourcePath = "/assets/client/sound/" + location + ".wav";
                InputStream inputStream = SoundUtil.class.getResourceAsStream(resourcePath);
                if (inputStream == null) return;

                try (AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(inputStream))) {
                    Clip clip = AudioSystem.getClip();

                    if (Math.abs(pitch - 1.0F) < 0.001F) {
                        clip.open(stream);
                    } else {
                        openPitched(clip, stream, pitch);
                    }

                    // Установка громкости
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        float volumeVal = Math.max(0.0f, Math.min(1.0f, volume));
                        // Используем Math.log10 для корректного децибельного преобразования
                        float dB = (float) (Math.log10(volumeVal <= 0 ? 0.0001 : volumeVal) * 20.0);
                        volumeControl.setValue(dB);
                    }

                    // Автоматическое закрытие клипа после завершения проигрывания
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                            CLIPS_LIST.remove(clip);
                        }
                    });

                    CLIPS_LIST.add(clip);
                    clip.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Сдвиг тона: пересемплируем кадры (pitch > 1 — берём кадры реже, звук выше и короче),
     * формат при этом остаётся исходным — микшер не приходится просить о необычной частоте.
     */
    private static void openPitched(Clip clip, AudioInputStream stream, float pitch) throws Exception {
        float p = Math.max(0.5F, Math.min(2.0F, pitch));

        AudioInputStream pcm = stream;
        AudioFormat src = stream.getFormat();

        if (src.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            pcm = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, stream);
            src = pcm.getFormat();
        }

        byte[] data = pcm.readAllBytes();
        if (pcm != stream) pcm.close();

        int frameSize = src.getFrameSize();

        if (frameSize <= 0) { // формат без фиксированного кадра — играем как есть
            clip.open(src, data, 0, data.length);
            return;
        }

        int inFrames = data.length / frameSize;
        int outFrames = (int) (inFrames / p);

        if (outFrames <= 0) {
            clip.open(src, data, 0, data.length);
            return;
        }

        byte[] out = new byte[outFrames * frameSize];

        for (int i = 0; i < outFrames; i++) {
            int srcFrame = Math.min(inFrames - 1, (int) (i * p));
            System.arraycopy(data, srcFrame * frameSize, out, i * frameSize, frameSize);
        }

        clip.open(src, out, 0, out.length);
    }

    private static void cleanUpClips() {
        // Очищаем только реально закрытые или невалидные клипы
        CLIPS_LIST.removeIf(clip -> !clip.isOpen());
    }
}