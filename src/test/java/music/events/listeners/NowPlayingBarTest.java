package music.events.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import music.events.PlaybackEvent;
import music.events.PlaybackEventType;
import music.model.Track;
import org.junit.jupiter.api.Test;

class NowPlayingBarTest {

    private static final Track TRACK = new Track("t1", "Title", "Artist", "Album", Duration.ofMinutes(3));

    private final List<String> shown = new ArrayList<>();
    private final NowPlayingBar bar = new NowPlayingBar(shown::add);

    private static PlaybackEvent position(Duration position) {
        return new PlaybackEvent(PlaybackEventType.POSITION_CHANGED, TRACK, position);
    }

    @Test
    void rendersStartMiddleAndEnd() {
        assertEquals("Title - Artist [----------] 0:00 / 3:00", NowPlayingBar.render(TRACK, Duration.ZERO));
        assertEquals("Title - Artist [#####-----] 1:30 / 3:00", NowPlayingBar.render(TRACK, Duration.ofSeconds(90)));
        assertEquals("Title - Artist [##########] 3:00 / 3:00", NowPlayingBar.render(TRACK, Duration.ofMinutes(3)));
    }

    @Test
    void positionPastTheEndStaysAtTheEnd() {
        assertEquals("Title - Artist [##########] 3:00 / 3:00", NowPlayingBar.render(TRACK, Duration.ofMinutes(4)));
    }

    @Test
    void trackWithoutDurationDoesNotFail() {
        Track empty = new Track("t2", "Empty", "Artist", "Album", Duration.ZERO);

        assertEquals("Empty - Artist [----------] 0:00 / 0:00", NowPlayingBar.render(empty, Duration.ZERO));
    }

    @Test
    void showsLineOnlyWhenItChanges() {
        bar.onEvent(position(Duration.ofMillis(1000)));
        bar.onEvent(position(Duration.ofMillis(1100)));
        bar.onEvent(position(Duration.ofMillis(2000)));

        assertEquals(List.of(
                "Title - Artist [----------] 0:01 / 3:00",
                "Title - Artist [----------] 0:02 / 3:00"), shown);
    }

    @Test
    void ignoresOtherEvents() {
        bar.onEvent(new PlaybackEvent(PlaybackEventType.TRACK_STARTED, TRACK, Duration.ZERO));

        assertEquals(List.of(), shown);
    }
}
