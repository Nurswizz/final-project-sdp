package music.events.listeners;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import music.events.PlaybackEvent;
import music.events.PlaybackListener;
import music.model.LyricLine;
import music.model.Lyrics;

public class LyricsDisplay implements PlaybackListener {

    private final Function<String, Optional<Lyrics>> lyricsOfTrack;
    private final Consumer<String> output;
    private List<LyricLine> lines = List.of();
    private LyricLine lastShown;

    public LyricsDisplay(Function<String, Optional<Lyrics>> lyricsOfTrack, Consumer<String> output) {
        this.lyricsOfTrack = lyricsOfTrack;
        this.output = output;
    }

    @Override
    public synchronized void onEvent(PlaybackEvent event) {
        switch (event.type()) {
            case TRACK_STARTED -> {
                lines = lyricsOfTrack.apply(event.track().id()).map(Lyrics::lines).orElse(List.of());
                lastShown = null;
            }
            case POSITION_CHANGED -> showLineAt(event.position());
            default -> {
            }
        }
    }

    private void showLineAt(Duration position) {
        lines.stream()
                .filter(line -> line.at().compareTo(position) <= 0)
                .max(Comparator.comparing(LyricLine::at))
                .filter(line -> !line.equals(lastShown))
                .ifPresent(line -> {
                    lastShown = line;
                    output.accept(line.text());
                });
    }
}
