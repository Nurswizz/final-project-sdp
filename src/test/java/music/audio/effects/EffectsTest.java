package music.audio.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Arrays;
import java.util.List;
import music.audio.AudioStream;
import org.junit.jupiter.api.Test;

class EffectsTest {

    private static AudioStream constant(float value) {
        return new AudioStream() {
            @Override
            public int sampleRate() {
                return 1000;
            }

            @Override
            public int read(float[] buffer) {
                Arrays.fill(buffer, value);
                return buffer.length;
            }
        };
    }

    private static float[] read(AudioStream stream, int size) {
        float[] buffer = new float[size];
        stream.read(buffer);
        return buffer;
    }

    @Test
    void volumeBoostMakesLouderAndClips() {
        assertEquals(0.9f, read(new VolumeBoostEffect(constant(0.5f)), 1)[0], 0.001f);
        assertEquals(1f, read(new VolumeBoostEffect(constant(0.8f)), 1)[0]);
    }

    @Test
    void fadeInStartsSilent() {
        float[] samples = read(new FadeInEffect(constant(0.5f)), 10);

        assertEquals(0f, samples[0]);
        assertEquals(true, samples[9] > samples[1]);
    }

    @Test
    void echoRepeatsTheSoundLater() {
        float[] samples = read(new EchoEffect(constant(0.2f)), 300);

        assertEquals(0.2f, samples[0], 0.001f);
        assertEquals(0.3f, samples[250], 0.001f);
    }

    @Test
    void factoryCreatesMatchingDecorator() {
        EffectFactory factory = new EffectFactory();

        assertInstanceOf(VolumeBoostEffect.class, factory.create(EffectType.VOLUME_BOOST, constant(0)));
        assertInstanceOf(FadeInEffect.class, factory.create(EffectType.FADE_IN, constant(0)));
        assertInstanceOf(EchoEffect.class, factory.create(EffectType.ECHO, constant(0)));
        assertInstanceOf(BassBoostEffect.class, factory.create(EffectType.BASS_BOOST, constant(0)));
    }

    @Test
    void chainWrapsEffectsInOrderWithoutDuplicates() {
        EffectChain chain = new EffectChain(new EffectFactory());
        chain.add(EffectType.ECHO);
        chain.add(EffectType.VOLUME_BOOST);
        chain.add(EffectType.ECHO);

        assertEquals(List.of(EffectType.ECHO, EffectType.VOLUME_BOOST), chain.active());
        assertInstanceOf(VolumeBoostEffect.class, chain.wrap(constant(0)));
    }
}
