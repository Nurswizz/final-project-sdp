package music.command;

import music.facade.MusicStreamingFacade;

public class NextCommand implements Command {

    private final MusicStreamingFacade facade;

    public NextCommand(MusicStreamingFacade facade) {
        this.facade = facade;
    }

    @Override
    public void execute() {
        facade.next();
    }
}
