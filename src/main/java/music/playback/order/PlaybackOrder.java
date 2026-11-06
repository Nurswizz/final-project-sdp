package music.playback.order;

import java.util.OptionalInt;

public interface PlaybackOrder {

    OptionalInt next(int current, int size);
}
