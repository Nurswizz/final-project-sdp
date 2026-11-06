package music.library;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import music.model.LyricLine;
import music.model.Lyrics;

public class LrcParser {

    private static final Pattern LINE = Pattern.compile("\\[(\\d+):(\\d+(?:\\.\\d+)?)]\\s*(.*)");

    public Lyrics parse(String trackId, List<String> lines) {
        List<LyricLine> result = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = LINE.matcher(line.trim());
            if (matcher.matches()) {
                long millis = Long.parseLong(matcher.group(1)) * 60_000
                        + Math.round(Double.parseDouble(matcher.group(2)) * 1000);
                result.add(new LyricLine(Duration.ofMillis(millis), matcher.group(3)));
            }
        }
        result.sort(Comparator.comparing(LyricLine::at));
        return new Lyrics(trackId, result);
    }
}
