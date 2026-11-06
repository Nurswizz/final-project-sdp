package music.playback.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import music.model.Track;
import org.junit.jupiter.api.Test;

class PlaybackQueueTest {

    private static final Track A = new Track("a", "A", "X", "Y", Duration.ofSeconds(1));
    private static final Track B = new Track("b", "B", "X", "Y", Duration.ofSeconds(1));

    private final PlaybackQueue queue = new PlaybackQueue();

    @Test
    void sequentialPlaysInOrderAndEnds() {
        queue.add(A);
        queue.add(B);

        assertEquals(Optional.of(A), queue.next());
        assertEquals(Optional.of(B), queue.next());
        assertEquals(Optional.empty(), queue.next());
    }

    @Test
    void repeatOneKeepsTheSameTrack() {
        queue.add(A);
        queue.add(B);
        queue.select(B);
        queue.setOrder(new RepeatOneOrder());

        assertEquals(Optional.of(B), queue.next());
        assertEquals(Optional.of(B), queue.next());
    }

    @Test
    void shuffleAlwaysGivesATrackOfTheQueue() {
        queue.add(A);
        queue.add(B);
        queue.setOrder(new ShuffleOrder(new Random(1)));

        for (int i = 0; i < 10; i++) {
            assertTrue(queue.next().isPresent());
        }
    }

    @Test
    void selectAddsUnknownTrack() {
        queue.select(A);

        assertEquals(1, queue.tracks().size());
        assertEquals(Optional.empty(), queue.next());
    }

    @Test
    void emptyQueueGivesNothing() {
        assertEquals(Optional.empty(), queue.next());
        queue.setOrder(new RepeatOneOrder());
        assertEquals(Optional.empty(), queue.next());
    }
}
