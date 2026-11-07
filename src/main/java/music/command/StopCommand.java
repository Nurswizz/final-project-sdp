package music.command;

import music.facade.MusicStreamingFacade;

public class StopCommand implements Command {

    private final MusicStreamingFacade facade;

    public StopCommand(MusicStreamingFacade facade) {
        this.facade = facade;
    }

    @Override
    public void execute() {
        facade.stop();
    }
}
