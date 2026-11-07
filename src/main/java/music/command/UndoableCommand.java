package music.command;

public interface UndoableCommand extends Command {

    void undo();
}
