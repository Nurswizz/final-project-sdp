package music.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import music.audio.SilentOutput;
import music.audio.effects.EffectChain;
import music.audio.effects.EffectFactory;
import music.audio.effects.EffectType;
import music.events.PlaybackEventPublisher;
import music.facade.MusicStreamingFacade;
import music.library.AudioLibrary;
import music.library.LrcParser;
import music.library.LyricsRepository;
import music.library.TrackCatalog;
import music.playback.Player;
import music.playback.order.PlaybackQueue;
import org.junit.jupiter.api.Test;

class CommandHistoryTest {

    private final PlaybackEventPublisher events = new PlaybackEventPublisher();
    private final MusicStreamingFacade facade = new MusicStreamingFacade(
            TrackCatalog.fromResource("/tracks/tracks.csv"),
            new LyricsRepository(new LrcParser()),
            new AudioLibrary(),
            new EffectChain(new EffectFactory()),
            new PlaybackQueue(),
            new Player(new SilentOutput(), events),
            events);
    private final CommandHistory history = new CommandHistory();

    @Test
    void undoTakesEffectsBackInReverseOrder() {
        history.run(new AddEffectCommand(facade, EffectType.ECHO));
        history.run(new AddEffectCommand(facade, EffectType.FADE_IN));
        history.run(new RemoveEffectCommand(facade, EffectType.ECHO));

        assertEquals(List.of(EffectType.FADE_IN), facade.activeEffects());
        assertTrue(history.undo());
        assertEquals(List.of(EffectType.FADE_IN, EffectType.ECHO), facade.activeEffects());
        assertTrue(history.undo());
        assertTrue(history.undo());
        assertEquals(List.of(), facade.activeEffects());
    }

    @Test
    void addingAnActiveEffectAgainIsNotUndoneTwice() {
        history.run(new AddEffectCommand(facade, EffectType.ECHO));
        history.run(new AddEffectCommand(facade, EffectType.ECHO));
        history.undo();

        assertEquals(List.of(EffectType.ECHO), facade.activeEffects());
    }

    @Test
    void playAndStopAreNotKeptInHistory() {
        history.run(new StopCommand(facade));

        assertFalse(history.undo());
    }
}
