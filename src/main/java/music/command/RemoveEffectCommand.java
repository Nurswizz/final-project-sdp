package music.command;

import music.audio.effects.EffectType;
import music.facade.MusicStreamingFacade;

public class RemoveEffectCommand implements UndoableCommand {

    private final MusicStreamingFacade facade;
    private final EffectType effect;
    private boolean removed;

    public RemoveEffectCommand(MusicStreamingFacade facade, EffectType effect) {
        this.facade = facade;
        this.effect = effect;
    }

    @Override
    public void execute() {
        removed = facade.activeEffects().contains(effect);
        facade.removeEffect(effect);
    }

    @Override
    public void undo() {
        if (removed) {
            facade.addEffect(effect);
        }
    }
}
