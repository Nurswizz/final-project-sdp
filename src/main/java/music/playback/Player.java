package music.playback;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import music.audio.AudioOutput;
import music.audio.AudioStream;
import music.events.PlaybackEvent;
import music.events.PlaybackEventPublisher;
import music.events.PlaybackEventType;
import music.model.Track;

public class Player {

    private static final int BUFFER_SIZE = 1024;
    private static final long EVENT_STEP_MILLIS = 100;

    private final AudioOutput output;
    private final PlaybackEventPublisher events;
    private final AtomicBoolean running = new AtomicBoolean();
    private volatile Track track;
    private volatile AudioStream stream;
    private volatile boolean paused;
    private volatile long playedMillis;
    private Thread thread;

    public Player(AudioOutput output, PlaybackEventPublisher events) {
        this.output = output;
        this.events = events;
    }

    public synchronized void play(Track track, AudioStream stream) {
        stop();
        this.track = track;
        this.stream = stream;
        paused = false;
        playedMillis = 0;
        running.set(true);
        publish(PlaybackEventType.TRACK_STARTED);
        thread = new Thread(this::loop, "player");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void pause() {
        if (running.get() && !paused) {
            paused = true;
            publish(PlaybackEventType.TRACK_PAUSED);
        }
    }

    public synchronized void resume() {
        if (running.get() && paused) {
            paused = false;
            publish(PlaybackEventType.TRACK_RESUMED);
        }
    }

    public synchronized void stop() {
        boolean wasRunning = running.getAndSet(false);
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (wasRunning) {
            publish(PlaybackEventType.TRACK_STOPPED);
        }
    }

    public synchronized void switchStream(AudioStream stream) {
        if (running.get()) {
            this.stream = stream;
            publish(PlaybackEventType.EFFECTS_CHANGED);
        }
    }

    public boolean isPlaying() {
        return running.get();
    }

    public Duration position() {
        return Duration.ofMillis(playedMillis);
    }

    private void loop() {
        float[] buffer = new float[BUFFER_SIZE];
        output.open(stream.sampleRate());
        long samples = 0;
        long nextEvent = 0;
        while (running.get()) {
            if (paused) {
                sleep();
                continue;
            }
            AudioStream current = stream;
            int count = current.read(buffer);
            if (count < 0) {
                break;
            }
            output.write(buffer, count);
            samples += count;
            playedMillis = samples * 1000 / current.sampleRate();
            if (playedMillis >= nextEvent) {
                publish(PlaybackEventType.POSITION_CHANGED);
                nextEvent += EVENT_STEP_MILLIS;
            }
        }
        output.close();
        if (running.compareAndSet(true, false)) {
            publish(PlaybackEventType.TRACK_FINISHED);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void publish(PlaybackEventType type) {
        events.publish(new PlaybackEvent(type, track, position()));
    }
}
