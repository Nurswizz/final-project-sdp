package music.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import music.audio.RealTimeSilentOutput;
import music.audio.effects.EffectChain;
import music.audio.effects.EffectFactory;
import music.audio.effects.EffectType;
import music.events.PlaybackEventPublisher;
import music.events.PlaybackEventType;
import music.facade.MusicStreamingFacade;
import music.library.AudioLibrary;
import music.library.LrcParser;
import music.library.LyricsRepository;
import music.library.TrackCatalog;
import music.playback.Player;
import music.playback.order.PlaybackQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class WebControllerTest {

    private final PlaybackEventPublisher events = new PlaybackEventPublisher();
    private final MusicStreamingFacade facade = new MusicStreamingFacade(
            TrackCatalog.fromResource("/tracks/tracks.csv"),
            new LyricsRepository(new LrcParser()),
            new AudioLibrary(),
            new EffectChain(new EffectFactory()),
            new PlaybackQueue(),
            new Player(new RealTimeSilentOutput(), events),
            events);
    private final WebState state = new WebState();
    private final WebController controller = new WebController(facade, state);

    WebControllerTest() {
        for (PlaybackEventType type : PlaybackEventType.values()) {
            facade.subscribe(type, state);
        }
    }

    @AfterEach
    void stopMusic() {
        facade.stop();
    }

    @Test
    void wholeLibraryIsInTheQueue() {
        assertEquals(4, facade.queue().size());
    }

    @Test
    void effectKeyTogglesAndUndoTakesItBack() {
        controller.toggleEffect(EffectType.ECHO);
        controller.toggleEffect(EffectType.BASS_BOOST);
        assertEquals(List.of(EffectType.ECHO, EffectType.BASS_BOOST), facade.activeEffects());

        controller.toggleEffect(EffectType.ECHO);
        assertEquals(List.of(EffectType.BASS_BOOST), facade.activeEffects());

        assertTrue(controller.undo());
        assertEquals(List.of(EffectType.BASS_BOOST, EffectType.ECHO), facade.activeEffects());
    }

    @Test
    void playPauseStartsPausesAndResumes() {
        controller.playPause();
        assertTrue(state.snapshot().playing());
        assertEquals("t1", state.snapshot().track().id());

        controller.playPause();
        assertTrue(state.snapshot().paused());

        controller.playPause();
        assertFalse(state.snapshot().paused());
    }

    @Test
    void previousGoesBackInTheLibraryAndWrapsAround() {
        controller.play("t1");
        controller.previous();

        assertEquals("t4", state.snapshot().track().id());
    }

    @Test
    void stateJsonHasTheTracksTheEffectsAndTheOrder() {
        controller.toggleEffect(EffectType.ECHO);
        controller.nextOrder();

        String json = controller.stateJson();

        assertTrue(json.contains("{\"id\":\"t1\",\"title\":\"Morning Lights\",\"artist\":\"Aru Band\",\"durationMillis\":24000}"));
        assertTrue(json.contains("\"effects\":[\"ECHO\"]"));
        assertTrue(json.contains("\"order\":\"shuffle\""));
        assertTrue(json.contains("\"current\":null"));
    }

    @Test
    void jsonTextIsEscaped() {
        assertEquals("\"say \\\"hi\\\"\\n\"", Json.text("say \"hi\"\n"));
    }
}
