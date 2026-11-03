package music.events.listeners;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import music.events.PlaybackEvent;
import music.events.PlaybackEventType;
import music.events.PlaybackListener;
import music.model.Track;

public class ListeningHistory implements PlaybackListener {

    private final List<Track> finished = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(PlaybackEvent event) {
        if (event.type() == PlaybackEventType.TRACK_FINISHED) {
            finished.add(event.track());
        }
    }

    public List<Track> tracks() {
        return List.copyOf(finished);
    }
}
