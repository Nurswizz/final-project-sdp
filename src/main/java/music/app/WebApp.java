package music.app;

import java.awt.Desktop;
import java.io.IOException;
import java.util.List;
import music.audio.AudioOutput;
import music.audio.RealTimeSilentOutput;
import music.audio.SpeakerOutput;
import music.audio.effects.EffectChain;
import music.audio.effects.EffectFactory;
import music.events.PlaybackEventPublisher;
import music.events.PlaybackEventType;
import music.events.listeners.LyricsDisplay;
import music.facade.MusicStreamingFacade;
import music.library.AudioLibrary;
import music.library.LrcParser;
import music.library.LyricsRepository;
import music.library.TrackCatalog;
import music.playback.Player;
import music.playback.order.PlaybackQueue;
import music.web.WebController;
import music.web.WebServer;
import music.web.WebState;

public class WebApp {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws IOException {
        List<String> options = List.of(args);
        AudioOutput output = options.contains("--silent") ? new RealTimeSilentOutput() : new SpeakerOutput();
        PlaybackEventPublisher events = new PlaybackEventPublisher();
        MusicStreamingFacade facade = new MusicStreamingFacade(
                TrackCatalog.fromResource("/tracks/tracks.csv"),
                new LyricsRepository(new LrcParser()),
                new AudioLibrary(),
                new EffectChain(new EffectFactory()),
                new PlaybackQueue(),
                new Player(output, events),
                events);

        WebState state = new WebState();
        LyricsDisplay lyrics = new LyricsDisplay(facade::lyricsOf, state::showLyric);
        for (PlaybackEventType type : PlaybackEventType.values()) {
            facade.subscribe(type, state);
        }
        facade.subscribe(PlaybackEventType.TRACK_STARTED, lyrics);
        facade.subscribe(PlaybackEventType.POSITION_CHANGED, lyrics);

        WebServer server = new WebServer(new WebController(facade, state), port(options));
        server.start();
        System.out.println("music player is running at " + server.address());
        if (!options.contains("--no-browser") && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(server.address());
        }
    }

    private static int port(List<String> options) {
        return options.stream()
                .filter(option -> option.startsWith("--port="))
                .map(option -> Integer.parseInt(option.substring("--port=".length())))
                .findFirst()
                .orElse(DEFAULT_PORT);
    }
}
