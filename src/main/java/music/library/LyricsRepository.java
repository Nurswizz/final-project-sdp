package music.library;

import java.util.Optional;
import music.model.Lyrics;

public class LyricsRepository {

    private final LrcParser parser;

    public LyricsRepository(LrcParser parser) {
        this.parser = parser;
    }

    public Optional<Lyrics> find(String trackId) {
        return Resources.lines("/lyrics/" + trackId + ".lrc").map(lines -> parser.parse(trackId, lines));
    }
}
