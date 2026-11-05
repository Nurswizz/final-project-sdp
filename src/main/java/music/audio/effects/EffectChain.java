package music.audio.effects;

import java.util.ArrayList;
import java.util.List;
import music.audio.AudioStream;

public class EffectChain {

    private final EffectFactory factory;
    private final List<EffectType> effects = new ArrayList<>();

    public EffectChain(EffectFactory factory) {
        this.factory = factory;
    }

    public void add(EffectType type) {
        if (!effects.contains(type)) {
            effects.add(type);
        }
    }

    public void remove(EffectType type) {
        effects.remove(type);
    }

    public List<EffectType> active() {
        return List.copyOf(effects);
    }

    public AudioStream wrap(AudioStream raw) {
        AudioStream stream = raw;
        for (EffectType type : effects) {
            stream = factory.create(type, stream);
        }
        return stream;
    }
}
