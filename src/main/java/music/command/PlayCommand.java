package music.command;

import music.facade.MusicStreamingFacade;

public class PlayCommand implements Command {

    private final MusicStreamingFacade facade;
    private final String trackId;

    public PlayCommand(MusicStreamingFacade facade, String trackId) {
        this.facade = facade;
        this.trackId = trackId;
    }

    @Override
    public void execute() {
        facade.play(trackId);
    }
}
