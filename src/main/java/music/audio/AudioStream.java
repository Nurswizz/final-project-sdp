package music.audio;

public interface AudioStream {

    int sampleRate();

    int read(float[] buffer);
}
