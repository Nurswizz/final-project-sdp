package music.audio;

public class RealTimeSilentOutput implements AudioOutput {

    private int sampleRate;

    @Override
    public void open(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public void write(float[] samples, int count) {
        try {
            Thread.sleep(count * 1000L / sampleRate);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
    }
}
