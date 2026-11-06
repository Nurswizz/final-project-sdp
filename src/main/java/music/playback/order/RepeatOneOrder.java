package music.playback.order;

import java.util.OptionalInt;

public class RepeatOneOrder implements PlaybackOrder {

    @Override
    public OptionalInt next(int current, int size) {
        return size == 0 ? OptionalInt.empty() : OptionalInt.of(Math.max(current, 0));
    }
}
