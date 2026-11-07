package music.command;

import music.facade.MusicStreamingFacade;

public class ResumeCommand implements Command {

    private final MusicStreamingFacade facade;

    public ResumeCommand(MusicStreamingFacade facade) {
        this.facade = facade;
    }

    @Override
    public void execute() {
        facade.resume();
    }
}
