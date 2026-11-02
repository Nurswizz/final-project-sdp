package music.events;

@FunctionalInterface
public interface PlaybackListener {

    void onEvent(PlaybackEvent event);
}
