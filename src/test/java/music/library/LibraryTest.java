package music.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import music.model.LyricLine;
import music.model.Track;
import org.junit.jupiter.api.Test;

class LibraryTest {

    @Test
    void catalogLoadsTracksFromResource() {
        TrackCatalog catalog = TrackCatalog.fromResource("/tracks/tracks.csv");

        assertEquals(4, catalog.all().size());
        assertEquals("Morning Lights", catalog.find("t1").orElseThrow().title());
    }

    @Test
    void searchFindsByTitleOrArtistIgnoringCase() {
        TrackCatalog catalog = TrackCatalog.fromResource("/tracks/tracks.csv");

        assertEquals(List.of("t2"), catalog.search("rain").stream().map(Track::id).toList());
        assertEquals(List.of("t1", "t3"), catalog.search("ARU").stream().map(Track::id).toList());
    }

    @Test
    void parserReadsSortsAndSkipsBadLines() {
        List<LyricLine> lines = new LrcParser().parse("t", List.of(
                "[00:10.50] second",
                "no time here",
                "[01:02.00] third",
                "[00:01.00] first")).lines();

        assertEquals(List.of(
                new LyricLine(Duration.ofMillis(1000), "first"),
                new LyricLine(Duration.ofMillis(10500), "second"),
                new LyricLine(Duration.ofMillis(62000), "third")), lines);
    }

    @Test
    void repositoryFindsLyricsOnlyForTracksThatHaveThem() {
        LyricsRepository repository = new LyricsRepository(new LrcParser());

        assertTrue(repository.find("t1").isPresent());
        assertTrue(repository.find("missing").isEmpty());
    }
}
