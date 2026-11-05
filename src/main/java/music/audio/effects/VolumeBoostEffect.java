package music.audio.effects;

import music.audio.AudioStream;

public class VolumeBoostEffect extends AudioEffect {

    public VolumeBoostEffect(AudioStream inner) {
        super(inner);
    }

    @Override
    protected float apply(float sample) {
        return clip(sample * 1.8f);
    }
}
