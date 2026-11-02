package music.model;

import java.util.List;

public record Lyrics(String trackId, List<LyricLine> lines) {

    public Lyrics {
        lines = List.copyOf(lines);
    }
}
