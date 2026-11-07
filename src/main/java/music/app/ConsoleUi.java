package music.app;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import music.audio.effects.EffectType;
import music.command.AddEffectCommand;
import music.command.CommandHistory;
import music.command.NextCommand;
import music.command.PauseCommand;
import music.command.PlayCommand;
import music.command.RemoveEffectCommand;
import music.command.ResumeCommand;
import music.command.StopCommand;
import music.events.listeners.ListeningHistory;
import music.facade.MusicStreamingFacade;
import music.model.Track;
import music.playback.order.PlaybackOrder;
import music.playback.order.RepeatOneOrder;
import music.playback.order.SequentialOrder;
import music.playback.order.ShuffleOrder;

public class ConsoleUi {

    private final MusicStreamingFacade facade;
    private final ListeningHistory listened;
    private final CommandHistory commands = new CommandHistory();
    private volatile String status = "nothing is playing";

    public ConsoleUi(MusicStreamingFacade facade, ListeningHistory listened) {
        this.facade = facade;
        this.listened = listened;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void run() {
        System.out.println("music streaming service, type help");
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String[] parts = in.nextLine().trim().split("\\s+", 2);
            String arg = parts.length > 1 ? parts[1] : "";
            if (parts[0].equals("exit")) {
                break;
            }
            try {
                handle(parts[0], arg);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        facade.stop();
    }

    private void handle(String command, String arg) {
        switch (command) {
            case "help" -> help();
            case "list" -> print(facade.allTracks());
            case "search" -> print(facade.search(arg));
            case "play" -> commands.run(new PlayCommand(facade, arg));
            case "pause" -> commands.run(new PauseCommand(facade));
            case "resume" -> commands.run(new ResumeCommand(facade));
            case "stop" -> commands.run(new StopCommand(facade));
            case "next" -> commands.run(new NextCommand(facade));
            case "add" -> commands.run(new AddEffectCommand(facade, effect(arg)));
            case "remove" -> commands.run(new RemoveEffectCommand(facade, effect(arg)));
            case "effects" -> System.out.println(facade.activeEffects());
            case "undo" -> System.out.println(commands.undo() ? "undone" : "nothing to undo");
            case "queue" -> queue(arg);
            case "order" -> facade.setPlaybackOrder(order(arg));
            case "now" -> System.out.println(status);
            case "history" -> print(listened.tracks());
            case "" -> {
            }
            default -> System.out.println("unknown command, type help");
        }
    }

    private void queue(String trackId) {
        if (trackId.isEmpty()) {
            print(facade.queue());
        } else {
            facade.enqueue(trackId);
        }
    }

    private static EffectType effect(String name) {
        try {
            return EffectType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("effects: volume_boost, fade_in, echo, bass_boost");
        }
    }

    private static PlaybackOrder order(String name) {
        return switch (name) {
            case "sequential" -> new SequentialOrder();
            case "shuffle" -> new ShuffleOrder(new Random());
            case "repeat" -> new RepeatOneOrder();
            default -> throw new IllegalArgumentException("orders: sequential, shuffle, repeat");
        };
    }

    private static void print(List<Track> tracks) {
        if (tracks.isEmpty()) {
            System.out.println("empty");
        }
        for (Track track : tracks) {
            System.out.printf("%s  %s - %s (%s) %d:%02d%n", track.id(), track.title(), track.artist(),
                    track.album(), track.duration().toMinutes(), track.duration().toSecondsPart());
        }
    }

    private static void help() {
        System.out.println("""
                list | search <text>
                play <id> | pause | resume | stop | next
                queue [<id>] | order sequential|shuffle|repeat
                add <effect> | remove <effect> | effects | undo
                now | history | exit
                effects: volume_boost, fade_in, echo, bass_boost""");
    }
}
