package music.audio;

public class SilentOutput implements AudioOutput {

    @Override
    public void open(int sampleRate) {
    }

    @Override
    public void write(float[] samples, int count) {
    }

    @Override
    public void close() {
    }
}
