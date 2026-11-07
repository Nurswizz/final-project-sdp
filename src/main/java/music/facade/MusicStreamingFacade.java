package music.facade;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import music.audio.AudioStream;
import music.audio.effects.EffectChain;
import music.audio.effects.EffectType;
import music.events.PlaybackEventPublisher;
import music.events.PlaybackEventType;
import music.events.PlaybackListener;
import music.library.AudioLibrary;
import music.library.LyricsRepository;
import music.library.TrackCatalog;
import music.model.Lyrics;
import music.model.Track;
import music.playback.Player;
import music.playback.order.PlaybackOrder;
import music.playback.order.PlaybackQueue;

public class MusicStreamingFacade {

    private final TrackCatalog catalog;
    private final LyricsRepository lyrics;
    private final AudioLibrary library;
    private final EffectChain effects;
    private final PlaybackQueue queue;
    private final Player player;
    private final PlaybackEventPublisher events;
    private final ExecutorService autoNext = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "auto-next");
        thread.setDaemon(true);
        return thread;
    });
    private AudioStream raw;

    public MusicStreamingFacade(TrackCatalog catalog, LyricsRepository lyrics, AudioLibrary library,
                                EffectChain effects, PlaybackQueue queue, Player player, PlaybackEventPublisher events) {
        this.catalog = catalog;
        this.lyrics = lyrics;
        this.library = library;
        this.effects = effects;
        this.queue = queue;
        this.player = player;
        this.events = events;
        events.subscribe(PlaybackEventType.TRACK_FINISHED, event -> autoNext.execute(this::next));
    }

    public List<Track> allTracks() {
        return catalog.all();
    }

    public List<Track> search(String query) {
        return catalog.search(query);
    }

    public synchronized void play(String trackId) {
        Track track = find(trackId);
        queue.select(track);
        start(track);
    }

    public synchronized void pause() {
        player.pause();
    }

    public synchronized void resume() {
        player.resume();
    }

    public synchronized void stop() {
        player.stop();
    }

    public synchronized void next() {
        queue.next().ifPresentOrElse(this::start, player::stop);
    }

    public synchronized void enqueue(String trackId) {
        queue.add(find(trackId));
    }

    public synchronized List<Track> queue() {
        return queue.tracks();
    }

    public synchronized void setPlaybackOrder(PlaybackOrder order) {
        queue.setOrder(order);
    }

    public synchronized void addEffect(EffectType effect) {
        effects.add(effect);
        rewrap();
    }

    public synchronized void removeEffect(EffectType effect) {
        effects.remove(effect);
        rewrap();
    }

    public synchronized List<EffectType> activeEffects() {
        return effects.active();
    }

    public Optional<Lyrics> lyricsOf(String trackId) {
        return lyrics.find(trackId);
    }

    public void subscribe(PlaybackEventType type, PlaybackListener listener) {
        events.subscribe(type, listener);
    }

    public void unsubscribe(PlaybackEventType type, PlaybackListener listener) {
        events.unsubscribe(type, listener);
    }

    private Track find(String trackId) {
        return catalog.find(trackId).orElseThrow(() -> new IllegalArgumentException("unknown track: " + trackId));
    }

    private void start(Track track) {
        raw = library.open(track);
        player.play(track, effects.wrap(raw));
    }

    private void rewrap() {
        if (player.isPlaying()) {
            player.switchStream(effects.wrap(raw));
        }
    }
}
