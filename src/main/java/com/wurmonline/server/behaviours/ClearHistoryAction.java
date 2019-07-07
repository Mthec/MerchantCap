package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.questions.ClearHistoryQuestion;
import mod.wurmunlimited.npcs.MerchantCapMod;
import org.gotti.wurmunlimited.modsupport.actions.*;

import java.util.List;

public class ClearHistoryAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final short actionId;

    public ClearHistoryAction() {
        actionId = (short)ModActions.getNextActionId();

        ActionEntry actionEntry = new ActionEntryBuilder(actionId, "Clear History", "clearing history").build();
        CapActions.clearHistoryAction = actionEntry;

        ModActions.registerAction(actionEntry);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Creature target) {
        return null;
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Creature target, short num, float counter) {
        if (CapActions.canCap(performer, source, target)) {
            new ClearHistoryQuestion(performer, target).sendQuestion();
            return true;
        }
        return false;
    }

    @Override
    public short getActionId() {
        return actionId;
    }
}
