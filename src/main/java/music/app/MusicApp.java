package music.app;

import music.audio.SpeakerOutput;
import music.audio.effects.EffectChain;
import music.audio.effects.EffectFactory;
import music.events.PlaybackEventPublisher;
import music.events.PlaybackEventType;
import music.events.listeners.ListeningHistory;
import music.events.listeners.LyricsDisplay;
import music.events.listeners.NowPlayingBar;
import music.facade.MusicStreamingFacade;
import music.library.AudioLibrary;
import music.library.LrcParser;
import music.library.LyricsRepository;
import music.library.TrackCatalog;
import music.playback.Player;
import music.playback.order.PlaybackQueue;

public class MusicApp {

    public static void main(String[] args) {
        PlaybackEventPublisher events = new PlaybackEventPublisher();
        MusicStreamingFacade facade = new MusicStreamingFacade(
                TrackCatalog.fromResource("/tracks/tracks.csv"),
                new LyricsRepository(new LrcParser()),
                new AudioLibrary(),
                new EffectChain(new EffectFactory()),
                new PlaybackQueue(),
                new Player(new SpeakerOutput(), events),
                events);

        ListeningHistory history = new ListeningHistory();
        ConsoleUi ui = new ConsoleUi(facade, history);
        LyricsDisplay lyrics = new LyricsDisplay(facade::lyricsOf, line -> System.out.println("   ~ " + line));

        facade.subscribe(PlaybackEventType.TRACK_STARTED, lyrics);
        facade.subscribe(PlaybackEventType.POSITION_CHANGED, lyrics);
        facade.subscribe(PlaybackEventType.POSITION_CHANGED, new NowPlayingBar(ui::setStatus));
        facade.subscribe(PlaybackEventType.TRACK_FINISHED, history);
        facade.subscribe(PlaybackEventType.TRACK_STARTED,
                e -> System.out.println(">> now playing: " + e.track().title() + " - " + e.track().artist()));
        facade.subscribe(PlaybackEventType.TRACK_FINISHED, e -> System.out.println(">> finished: " + e.track().title()));
        facade.subscribe(PlaybackEventType.TRACK_PAUSED, e -> System.out.println(">> paused"));
        facade.subscribe(PlaybackEventType.TRACK_RESUMED, e -> System.out.println(">> resumed"));
        facade.subscribe(PlaybackEventType.EFFECTS_CHANGED, e -> System.out.println(">> effects: " + facade.activeEffects()));

        ui.run();
    }
}
