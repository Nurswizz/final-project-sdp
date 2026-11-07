package music.command;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandHistory {

    private final Deque<UndoableCommand> done = new ArrayDeque<>();

    public void run(Command command) {
        command.execute();
        if (command instanceof UndoableCommand undoable) {
            done.push(undoable);
        }
    }

    public boolean undo() {
        UndoableCommand last = done.poll();
        if (last == null) {
            return false;
        }
        last.undo();
        return true;
    }
}
