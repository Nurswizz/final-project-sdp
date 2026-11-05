package music.audio.effects;

import music.audio.AudioStream;

public class BassBoostEffect extends AudioEffect {

    private float low;

    public BassBoostEffect(AudioStream inner) {
        super(inner);
    }

    @Override
    protected float apply(float sample) {
        low += (sample - low) * 0.05f;
        return clip(sample + low * 1.5f);
    }
}
