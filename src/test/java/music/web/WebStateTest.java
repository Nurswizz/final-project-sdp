package music.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import music.events.PlaybackEvent;
import music.events.PlaybackEventType;
import music.model.Track;
import org.junit.jupiter.api.Test;

class WebStateTest {

    private final Track track = new Track("t1", "Morning Lights", "Aru Band", "First Steps", Duration.ofSeconds(24));
    private final WebState state = new WebState();

    private void send(PlaybackEventType type, long millis) {
        state.onEvent(new PlaybackEvent(type, track, Duration.ofMillis(millis)));
    }

    @Test
    void followsTheTrackFromStartToFinish() {
        send(PlaybackEventType.TRACK_STARTED, 0);
        send(PlaybackEventType.POSITION_CHANGED, 5200);
        state.showLyric("coffee and a quiet street");

        WebState.Snapshot playing = state.snapshot();
        assertEquals(track, playing.track());
        assertEquals(5200, playing.positionMillis());
        assertTrue(playing.playing());
        assertEquals("coffee and a quiet street", playing.lyric());

        send(PlaybackEventType.TRACK_PAUSED, 5200);
        assertTrue(state.snapshot().paused());
        send(PlaybackEventType.TRACK_RESUMED, 5200);
        assertFalse(state.snapshot().paused());

        send(PlaybackEventType.TRACK_FINISHED, 24000);
        assertFalse(state.snapshot().playing());
    }

    @Test
    void newTrackClearsTheOldLyricLine() {
        send(PlaybackEventType.TRACK_STARTED, 0);
        state.showLyric("old line");

        send(PlaybackEventType.TRACK_STARTED, 0);

        assertEquals("", state.snapshot().lyric());
    }
}
