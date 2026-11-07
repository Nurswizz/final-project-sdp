package music.command;

import music.facade.MusicStreamingFacade;

public class PauseCommand implements UndoableCommand {

    private final MusicStreamingFacade facade;

    public PauseCommand(MusicStreamingFacade facade) {
        this.facade = facade;
    }

    @Override
    public void execute() {
        facade.pause();
    }

    @Override
    public void undo() {
        facade.resume();
    }
}
