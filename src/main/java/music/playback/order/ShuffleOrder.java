package music.playback.order;

import java.util.OptionalInt;
import java.util.Random;

public class ShuffleOrder implements PlaybackOrder {

    private final Random random;

    public ShuffleOrder(Random random) {
        this.random = random;
    }

    @Override
    public OptionalInt next(int current, int size) {
        return size == 0 ? OptionalInt.empty() : OptionalInt.of(random.nextInt(size));
    }
}
