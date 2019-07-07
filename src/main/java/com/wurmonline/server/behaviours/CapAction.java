package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.questions.CapQuestion;
import org.gotti.wurmunlimited.modsupport.actions.*;

import java.util.List;

public class CapAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final short actionId;

    public CapAction() {
        actionId = (short)ModActions.getNextActionId();

        ActionEntry actionEntry = new ActionEntryBuilder(actionId, "Set Cap", "setting cap").build();
        CapActions.capAction = actionEntry;

        ModActions.registerAction(actionEntry);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Creature target) {
        return CapActions.getBehavioursFor(performer, subject, target);
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Creature target, short num, float counter) {
        if (CapActions.canCap(performer, source, target)) {
            new CapQuestion(performer, target).sendQuestion();
            return true;
        }
        return false;
    }

    @Override
    public short getActionId() {
        return actionId;
    }
}
