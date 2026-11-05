package music.audio.effects;

import music.audio.AudioStream;

public class FadeInEffect extends AudioEffect {

    private final long length;
    private long done;

    public FadeInEffect(AudioStream inner) {
        super(inner);
        length = inner.sampleRate() * 3L;
    }

    @Override
    protected float apply(float sample) {
        float gain = Math.min(1f, (float) done++ / length);
        return sample * gain;
    }
}
