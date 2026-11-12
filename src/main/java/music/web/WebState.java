package music.web;

import music.events.PlaybackEvent;
import music.events.PlaybackListener;
import music.model.Track;

public class WebState implements PlaybackListener {

    private Track track;
    private long positionMillis;
    private boolean playing;
    private boolean paused;
    private String lyric = "";

    @Override
    public synchronized void onEvent(PlaybackEvent event) {
        switch (event.type()) {
            case TRACK_STARTED -> {
                track = event.track();
                positionMillis = 0;
                playing = true;
                paused = false;
                lyric = "";
            }
            case POSITION_CHANGED -> positionMillis = event.position().toMillis();
            case TRACK_PAUSED -> paused = true;
            case TRACK_RESUMED -> paused = false;
            case TRACK_STOPPED, TRACK_FINISHED -> {
                playing = false;
                paused = false;
            }
            default -> {
            }
        }
    }

    public synchronized void showLyric(String line) {
        lyric = line;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(track, positionMillis, playing, paused, lyric);
    }

    public record Snapshot(Track track, long positionMillis, boolean playing, boolean paused, String lyric) {
    }
}
