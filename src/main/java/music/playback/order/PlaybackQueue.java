package music.playback.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import music.model.Track;

public class PlaybackQueue {

    private final List<Track> tracks = new ArrayList<>();
    private PlaybackOrder order = new SequentialOrder();
    private int current = -1;

    public void add(Track track) {
        tracks.add(track);
    }

    public void select(Track track) {
        int index = tracks.indexOf(track);
        if (index < 0) {
            tracks.add(track);
            index = tracks.size() - 1;
        }
        current = index;
    }

    public Optional<Track> next() {
        OptionalInt index = order.next(current, tracks.size());
        if (index.isEmpty()) {
            return Optional.empty();
        }
        current = index.getAsInt();
        return Optional.of(tracks.get(current));
    }

    public void setOrder(PlaybackOrder order) {
        this.order = order;
    }

    public List<Track> tracks() {
        return List.copyOf(tracks);
    }
}
