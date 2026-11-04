package music.events.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import music.events.PlaybackEvent;
import music.events.PlaybackEventType;
import music.model.LyricLine;
import music.model.Lyrics;
import music.model.Track;
import org.junit.jupiter.api.Test;

class LyricsDisplayTest {

    private static final Track WITH_LYRICS = new Track("t1", "Title", "Artist", "Album", Duration.ofMinutes(3));
    private static final Track NO_LYRICS = new Track("t2", "Other", "Artist", "Album", Duration.ofMinutes(3));
    private static final Lyrics LYRICS = new Lyrics("t1", List.of(
            new LyricLine(Duration.ofSeconds(5), "first"),
            new LyricLine(Duration.ofSeconds(10), "second"),
            new LyricLine(Duration.ofSeconds(15), "third")));

    private final List<String> shown = new ArrayList<>();
    private final LyricsDisplay display = new LyricsDisplay(
            id -> Optional.ofNullable(Map.of("t1", LYRICS).get(id)), shown::add);

    private void start(Track track) {
        display.onEvent(new PlaybackEvent(PlaybackEventType.TRACK_STARTED, track, Duration.ZERO));
    }

    private void moveTo(Track track, int seconds) {
        display.onEvent(new PlaybackEvent(PlaybackEventType.POSITION_CHANGED, track, Duration.ofSeconds(seconds)));
    }

    @Test
    void showsNothingBeforeTheFirstLine() {
        start(WITH_LYRICS);
        moveTo(WITH_LYRICS, 4);

        assertEquals(List.of(), shown);
    }

    @Test
    void showsEachLineOnceWhenItsTimeComes() {
        start(WITH_LYRICS);
        moveTo(WITH_LYRICS, 5);
        moveTo(WITH_LYRICS, 7);
        moveTo(WITH_LYRICS, 10);

        assertEquals(List.of("first", "second"), shown);
    }

    @Test
    void showsOnlyTheCurrentLineAfterAJump() {
        start(WITH_LYRICS);
        moveTo(WITH_LYRICS, 16);

        assertEquals(List.of("third"), shown);
    }

    @Test
    void replayingTheTrackShowsLinesAgain() {
        start(WITH_LYRICS);
        moveTo(WITH_LYRICS, 5);
        start(WITH_LYRICS);
        moveTo(WITH_LYRICS, 5);

        assertEquals(List.of("first", "first"), shown);
    }

    @Test
    void trackWithoutLyricsShowsNothingAndClearsThePreviousOnes() {
        start(WITH_LYRICS);
        moveTo(WITH_LYRICS, 5);
        start(NO_LYRICS);
        moveTo(NO_LYRICS, 10);

        assertEquals(List.of("first"), shown);
    }
}
