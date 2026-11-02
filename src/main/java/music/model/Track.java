package music.model;

import java.time.Duration;

public record Track(String id, String title, String artist, String album, Duration duration) {
}
