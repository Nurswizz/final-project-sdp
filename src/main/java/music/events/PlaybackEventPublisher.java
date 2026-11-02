package music.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlaybackEventPublisher implements PlaybackSubject {

    private final Map<PlaybackEventType, CopyOnWriteArrayList<PlaybackListener>> listeners = new ConcurrentHashMap<>();

    @Override
    public void subscribe(PlaybackEventType type, PlaybackListener listener) {
        listenersOf(type).addIfAbsent(listener);
    }

    @Override
    public void unsubscribe(PlaybackEventType type, PlaybackListener listener) {
        listenersOf(type).remove(listener);
    }

    public void publish(PlaybackEvent event) {
        for (PlaybackListener listener : listenersOf(event.type())) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException e) {
                // A broken listener must not stop playback or the other listeners.
                System.err.println("Listener failed on " + event.type() + ": " + e);
            }
        }
    }

    private CopyOnWriteArrayList<PlaybackListener> listenersOf(PlaybackEventType type) {
        return listeners.computeIfAbsent(type, t -> new CopyOnWriteArrayList<>());
    }
}
