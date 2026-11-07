package music.command;

import music.audio.effects.EffectType;
import music.facade.MusicStreamingFacade;

public class AddEffectCommand implements UndoableCommand {

    private final MusicStreamingFacade facade;
    private final EffectType effect;
    private boolean added;

    public AddEffectCommand(MusicStreamingFacade facade, EffectType effect) {
        this.facade = facade;
        this.effect = effect;
    }

    @Override
    public void execute() {
        added = !facade.activeEffects().contains(effect);
        facade.addEffect(effect);
    }

    @Override
    public void undo() {
        if (added) {
            facade.removeEffect(effect);
        }
    }
}
