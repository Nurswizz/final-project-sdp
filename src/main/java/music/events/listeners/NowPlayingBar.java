package music.events.listeners;

import java.time.Duration;
import java.util.function.Consumer;
import music.events.PlaybackEvent;
import music.events.PlaybackEventType;
import music.events.PlaybackListener;
import music.model.Track;

public class NowPlayingBar implements PlaybackListener {

    private static final int BAR_WIDTH = 10;

    private final Consumer<String> output;
    private volatile String lastShown;

    public NowPlayingBar(Consumer<String> output) {
        this.output = output;
    }

    @Override
    public void onEvent(PlaybackEvent event) {
        if (event.type() != PlaybackEventType.POSITION_CHANGED) {
            return;
        }
        // Position events come about every 100 ms, the text changes about once a second.
        String line = render(event.track(), event.position());
        if (!line.equals(lastShown)) {
            lastShown = line;
            output.accept(line);
        }
    }

    static String render(Track track, Duration position) {
        Duration total = track.duration();
        Duration shown = position.compareTo(total) > 0 ? total : position;
        int filled = total.isZero() ? 0 : (int) (shown.toMillis() * BAR_WIDTH / total.toMillis());
        return "%s - %s [%s%s] %s / %s".formatted(
                track.title(),
                track.artist(),
                "#".repeat(filled),
                "-".repeat(BAR_WIDTH - filled),
                time(shown),
                time(total));
    }

    private static String time(Duration duration) {
        return "%d:%02d".formatted(duration.toMinutes(), duration.toSecondsPart());
    }
}
