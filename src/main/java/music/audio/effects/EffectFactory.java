package music.audio.effects;

import music.audio.AudioStream;

public class EffectFactory {

    public AudioStream create(EffectType type, AudioStream inner) {
        return switch (type) {
            case VOLUME_BOOST -> new VolumeBoostEffect(inner);
            case FADE_IN -> new FadeInEffect(inner);
            case ECHO -> new EchoEffect(inner);
            case BASS_BOOST -> new BassBoostEffect(inner);
        };
    }
}
