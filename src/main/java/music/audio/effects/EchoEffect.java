package music.audio.effects;

import music.audio.AudioStream;

public class EchoEffect extends AudioEffect {

    private final float[] delay;
    private int index;

    public EchoEffect(AudioStream inner) {
        super(inner);
        delay = new float[inner.sampleRate() / 4];
    }

    @Override
    protected float apply(float sample) {
        float out = sample + delay[index] * 0.5f;
        delay[index] = out;
        index = (index + 1) % delay.length;
        return clip(out);
    }
}
