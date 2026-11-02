package music.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import music.model.Track;
import org.junit.jupiter.api.Test;

class PlaybackEventPublisherTest {

    private static final Track TRACK = new Track("t1", "Title", "Artist", "Album", Duration.ofMinutes(3));

    private final PlaybackEventPublisher publisher = new PlaybackEventPublisher();
    private final List<PlaybackEvent> received = new ArrayList<>();

    private static PlaybackEvent event(PlaybackEventType type) {
        return new PlaybackEvent(type, TRACK, Duration.ZERO);
    }

    @Test
    void deliversEventOnlyToListenersOfItsType() {
        publisher.subscribe(PlaybackEventType.TRACK_STARTED, received::add);

        publisher.publish(event(PlaybackEventType.TRACK_FINISHED));
        publisher.publish(event(PlaybackEventType.TRACK_STARTED));

        assertEquals(List.of(event(PlaybackEventType.TRACK_STARTED)), received);
    }

    @Test
    void unsubscribedListenerGetsNothing() {
        PlaybackListener listener = received::add;
        publisher.subscribe(PlaybackEventType.TRACK_STARTED, listener);
        publisher.unsubscribe(PlaybackEventType.TRACK_STARTED, listener);

        publisher.publish(event(PlaybackEventType.TRACK_STARTED));

        assertEquals(List.of(), received);
    }

    @Test
    void subscribingTwiceDeliversOnce() {
        PlaybackListener listener = received::add;
        publisher.subscribe(PlaybackEventType.TRACK_STARTED, listener);
        publisher.subscribe(PlaybackEventType.TRACK_STARTED, listener);

        publisher.publish(event(PlaybackEventType.TRACK_STARTED));

        assertEquals(1, received.size());
    }

    @Test
    void failingListenerDoesNotStopTheOthers() {
        publisher.subscribe(PlaybackEventType.TRACK_STARTED, e -> {
            throw new IllegalStateException("broken");
        });
        publisher.subscribe(PlaybackEventType.TRACK_STARTED, received::add);

        publisher.publish(event(PlaybackEventType.TRACK_STARTED));

        assertEquals(1, received.size());
    }

    @Test
    void listenerCanUnsubscribeItselfWhileHandlingAnEvent() {
        PlaybackListener[] self = new PlaybackListener[1];
        self[0] = e -> {
            received.add(e);
            publisher.unsubscribe(PlaybackEventType.TRACK_STARTED, self[0]);
        };
        publisher.subscribe(PlaybackEventType.TRACK_STARTED, self[0]);

        publisher.publish(event(PlaybackEventType.TRACK_STARTED));
        publisher.publish(event(PlaybackEventType.TRACK_STARTED));

        assertEquals(1, received.size());
    }
}
