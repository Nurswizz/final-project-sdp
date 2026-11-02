package music.events;

public interface PlaybackSubject {

    void subscribe(PlaybackEventType type, PlaybackListener listener);

    void unsubscribe(PlaybackEventType type, PlaybackListener listener);
}
