package music.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import music.audio.effects.EffectType;
import music.command.AddEffectCommand;
import music.command.CommandHistory;
import music.command.NextCommand;
import music.command.PauseCommand;
import music.command.PlayCommand;
import music.command.RemoveEffectCommand;
import music.command.ResumeCommand;
import music.facade.MusicStreamingFacade;
import music.model.Track;
import music.playback.order.PlaybackOrder;
import music.playback.order.RepeatOneOrder;
import music.playback.order.SequentialOrder;
import music.playback.order.ShuffleOrder;

public class WebController {

    private static final List<String> ORDERS = List.of("sequential", "shuffle", "repeat");

    private final MusicStreamingFacade facade;
    private final WebState state;
    private final CommandHistory commands = new CommandHistory();
    private String order = "sequential";

    public WebController(MusicStreamingFacade facade, WebState state) {
        this.facade = facade;
        this.state = state;
        for (Track track : facade.allTracks()) {
            facade.enqueue(track.id());
        }
    }

    public void play(String trackId) {
        commands.run(new PlayCommand(facade, trackId));
    }

    public void playPause() {
        WebState.Snapshot now = state.snapshot();
        if (!now.playing()) {
            play(now.track() == null ? facade.allTracks().get(0).id() : now.track().id());
        } else if (now.paused()) {
            commands.run(new ResumeCommand(facade));
        } else {
            commands.run(new PauseCommand(facade));
        }
    }

    public void next() {
        commands.run(new NextCommand(facade));
    }

    public void previous() {
        List<Track> tracks = facade.allTracks();
        Track current = state.snapshot().track();
        int index = current == null ? 0 : tracks.indexOf(current);
        play(tracks.get((index - 1 + tracks.size()) % tracks.size()).id());
    }

    public void toggleEffect(EffectType effect) {
        if (facade.activeEffects().contains(effect)) {
            commands.run(new RemoveEffectCommand(facade, effect));
        } else {
            commands.run(new AddEffectCommand(facade, effect));
        }
    }

    public void nextOrder() {
        order = ORDERS.get((ORDERS.indexOf(order) + 1) % ORDERS.size());
        facade.setPlaybackOrder(orderOf(order));
    }

    public boolean undo() {
        return commands.undo();
    }

    public String stateJson() {
        WebState.Snapshot now = state.snapshot();
        List<String> tracks = new ArrayList<>();
        for (Track track : facade.allTracks()) {
            tracks.add(trackJson(track));
        }
        List<String> effects = new ArrayList<>();
        for (EffectType effect : facade.activeEffects()) {
            effects.add(Json.text(effect.name()));
        }
        return "{\"tracks\":[" + String.join(",", tracks) + "],"
                + "\"current\":" + (now.track() == null ? "null" : trackJson(now.track())) + ","
                + "\"positionMillis\":" + now.positionMillis() + ","
                + "\"playing\":" + now.playing() + ","
                + "\"paused\":" + now.paused() + ","
                + "\"lyric\":" + Json.text(now.lyric()) + ","
                + "\"effects\":[" + String.join(",", effects) + "],"
                + "\"order\":" + Json.text(order) + "}";
    }

    private static String trackJson(Track track) {
        return "{\"id\":" + Json.text(track.id()) + ",\"title\":" + Json.text(track.title())
                + ",\"artist\":" + Json.text(track.artist()) + ",\"durationMillis\":" + track.duration().toMillis() + "}";
    }

    private static PlaybackOrder orderOf(String name) {
        return switch (name) {
            case "shuffle" -> new ShuffleOrder(new Random());
            case "repeat" -> new RepeatOneOrder();
            default -> new SequentialOrder();
        };
    }
}
