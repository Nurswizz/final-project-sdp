package music.audio.effects;

import music.audio.AudioStream;

public abstract class AudioEffect implements AudioStream {

    protected final AudioStream inner;

    protected AudioEffect(AudioStream inner) {
        this.inner = inner;
    }

    @Override
    public int sampleRate() {
        return inner.sampleRate();
    }

    @Override
    public int read(float[] buffer) {
        int count = inner.read(buffer);
        for (int i = 0; i < count; i++) {
            buffer[i] = apply(buffer[i]);
        }
        return count;
    }

    protected abstract float apply(float sample);

    protected static float clip(float value) {
        return Math.max(-1f, Math.min(1f, value));
    }
}
