package music.audio;

public interface AudioOutput {

    void open(int sampleRate);

    void write(float[] samples, int count);

    void close();
}
