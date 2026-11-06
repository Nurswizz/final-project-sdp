package music.library;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import music.model.Track;

public class TrackCatalog {

    private final List<Track> tracks;

    public TrackCatalog(List<Track> tracks) {
        this.tracks = List.copyOf(tracks);
    }

    public static TrackCatalog fromResource(String path) {
        List<String> lines = Resources.lines(path).orElseThrow(() -> new IllegalStateException("no resource " + path));
        return new TrackCatalog(lines.stream().filter(line -> !line.isBlank()).map(TrackCatalog::parse).toList());
    }

    private static Track parse(String line) {
        String[] parts = line.split(";");
        return new Track(parts[0], parts[1], parts[2], parts[3], Duration.ofSeconds(Long.parseLong(parts[4])));
    }

    public List<Track> all() {
        return tracks;
    }

    public Optional<Track> find(String id) {
        return tracks.stream().filter(track -> track.id().equals(id)).findFirst();
    }

    public List<Track> search(String query) {
        String text = query.toLowerCase();
        return tracks.stream()
                .filter(track -> track.title().toLowerCase().contains(text) || track.artist().toLowerCase().contains(text))
                .toList();
    }
}
