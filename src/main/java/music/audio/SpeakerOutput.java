package music.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SpeakerOutput implements AudioOutput {

    private SourceDataLine line;
    private byte[] bytes = new byte[0];

    @Override
    public void open(int sampleRate) {
        try {
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, sampleRate / 5 * 2);
            line.start();
        } catch (LineUnavailableException e) {
            throw new IllegalStateException("speakers are not available", e);
        }
    }

    @Override
    public void write(float[] samples, int count) {
        if (bytes.length < count * 2) {
            bytes = new byte[count * 2];
        }
        for (int i = 0; i < count; i++) {
            short value = (short) (samples[i] * Short.MAX_VALUE);
            bytes[2 * i] = (byte) value;
            bytes[2 * i + 1] = (byte) (value >> 8);
        }
        line.write(bytes, 0, count * 2);
    }

    @Override
    public void close() {
        line.drain();
        line.close();
    }
}
