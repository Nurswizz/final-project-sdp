package music.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import music.audio.SilentOutput;
import music.audio.effects.EffectChain;
import music.audio.effects.EffectFactory;
import music.audio.effects.EffectType;
import music.events.PlaybackEventPublisher;
import music.events.PlaybackEventType;
import music.events.listeners.ListeningHistory;
import music.events.listeners.LyricsDisplay;
import music.library.AudioLibrary;
import music.library.LrcParser;
import music.library.LyricsRepository;
import music.library.TrackCatalog;
import music.model.Track;
import music.playback.Player;
import music.playback.order.PlaybackQueue;
import org.junit.jupiter.api.Test;

class MusicStreamingFacadeTest {

    private final PlaybackEventPublisher events = new PlaybackEventPublisher();
    private final MusicStreamingFacade facade = new MusicStreamingFacade(
            TrackCatalog.fromResource("/tracks/tracks.csv"),
            new LyricsRepository(new LrcParser()),
            new AudioLibrary(),
            new EffectChain(new EffectFactory()),
            new PlaybackQueue(),
            new Player(new SilentOutput(), events),
            events);

    @Test
    void playsTheQueueToTheEndAndShowsLyrics() throws InterruptedException {
        ListeningHistory history = new ListeningHistory();
        List<String> shown = new CopyOnWriteArrayList<>();
        LyricsDisplay lyrics = new LyricsDisplay(facade::lyricsOf, shown::add);
        CountDownLatch finished = new CountDownLatch(2);
        facade.subscribe(PlaybackEventType.TRACK_STARTED, lyrics);
        facade.subscribe(PlaybackEventType.POSITION_CHANGED, lyrics);
        facade.subscribe(PlaybackEventType.TRACK_FINISHED, history);
        facade.subscribe(PlaybackEventType.TRACK_FINISHED, e -> finished.countDown());

        facade.enqueue("t1");
        facade.enqueue("t4");
        facade.next();

        assertTrue(finished.await(5, TimeUnit.SECONDS));
        assertEquals(List.of("t1", "t4"), history.tracks().stream().map(Track::id).toList());
        assertTrue(shown.contains("first light on the window"));
        assertTrue(shown.contains("one day they will reach the stars"));
    }

    @Test
    void keepsEffectsBetweenTracks() {
        facade.addEffect(EffectType.ECHO);
        facade.addEffect(EffectType.BASS_BOOST);
        facade.removeEffect(EffectType.ECHO);

        assertEquals(List.of(EffectType.BASS_BOOST), facade.activeEffects());
    }

    @Test
    void unknownTrackIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> facade.play("nope"));
    }
}
