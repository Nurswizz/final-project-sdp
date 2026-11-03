package music.events.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;
import music.events.PlaybackEvent;
import music.events.PlaybackEventType;
import music.model.Track;
import org.junit.jupiter.api.Test;

class ListeningHistoryTest {

    private static final Track FIRST = new Track("t1", "First", "Artist", "Album", Duration.ofMinutes(3));
    private static final Track SECOND = new Track("t2", "Second", "Artist", "Album", Duration.ofMinutes(2));

    private final ListeningHistory history = new ListeningHistory();

    private static PlaybackEvent event(PlaybackEventType type, Track track) {
        return new PlaybackEvent(type, track, Duration.ZERO);
    }

    @Test
    void remembersFinishedTracksInOrder() {
        history.onEvent(event(PlaybackEventType.TRACK_FINISHED, FIRST));
        history.onEvent(event(PlaybackEventType.TRACK_FINISHED, SECOND));
        history.onEvent(event(PlaybackEventType.TRACK_FINISHED, FIRST));

        assertEquals(List.of(FIRST, SECOND, FIRST), history.tracks());
    }

    @Test
    void ignoresTracksThatDidNotFinish() {
        history.onEvent(event(PlaybackEventType.TRACK_STARTED, FIRST));
        history.onEvent(event(PlaybackEventType.TRACK_STOPPED, FIRST));

        assertEquals(List.of(), history.tracks());
    }

    @Test
    void returnedListCannotChangeTheHistory() {
        history.onEvent(event(PlaybackEventType.TRACK_FINISHED, FIRST));

        assertThrows(UnsupportedOperationException.class, () -> history.tracks().clear());
    }
}
