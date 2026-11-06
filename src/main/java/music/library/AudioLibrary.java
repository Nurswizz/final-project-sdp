package music.library;

import java.util.Random;
import music.audio.AudioStream;
import music.audio.ToneTrackStream;
import music.model.Track;

public class AudioLibrary {

    private static final double[] SCALE = {261.63, 293.66, 329.63, 392.00, 440.00, 523.25};
    private static final int NOTES = 16;

    public AudioStream open(Track track) {
        Random random = new Random(track.id().hashCode());
        double[] notes = new double[NOTES];
        for (int i = 0; i < NOTES; i++) {
            notes[i] = SCALE[random.nextInt(SCALE.length)];
        }
        return new ToneTrackStream(notes, track.duration());
    }
}
