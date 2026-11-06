package music.playback.order;

import java.util.OptionalInt;

public class SequentialOrder implements PlaybackOrder {

    @Override
    public OptionalInt next(int current, int size) {
        return current + 1 < size ? OptionalInt.of(current + 1) : OptionalInt.empty();
    }
}
