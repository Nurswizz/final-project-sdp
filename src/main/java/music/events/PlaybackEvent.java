package music.events;

import music.model.Track;

import java.time.Duration;

public record PlaybackEvent(PlaybackEventType type, Track track, Duration position) {
}
