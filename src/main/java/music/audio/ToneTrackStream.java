package music.audio;

import java.time.Duration;

public class ToneTrackStream implements AudioStream {

    private static final int SAMPLE_RATE = 22050;
    private static final int SAMPLES_PER_NOTE = SAMPLE_RATE / 3;

    private final double[] notes;
    private final long totalSamples;
    private long position;

    public ToneTrackStream(double[] notes, Duration duration) {
        this.notes = notes;
        this.totalSamples = duration.toMillis() * SAMPLE_RATE / 1000;
    }

    @Override
    public int sampleRate() {
        return SAMPLE_RATE;
    }

    @Override
    public int read(float[] buffer) {
        if (position >= totalSamples) {
            return -1;
        }
        int count = (int) Math.min(buffer.length, totalSamples - position);
        for (int i = 0; i < count; i++) {
            double note = notes[(int) (position / SAMPLES_PER_NOTE % notes.length)];
            double envelope = Math.sin(Math.PI * (position % SAMPLES_PER_NOTE) / SAMPLES_PER_NOTE);
            buffer[i] = (float) (0.3 * envelope * Math.sin(2 * Math.PI * note * position / SAMPLE_RATE));
            position++;
        }
        return count;
    }
}
