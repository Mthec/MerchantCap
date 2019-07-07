package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import mod.wurmunlimited.npcs.MerchantCapMod;
import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;

import java.util.ArrayList;
import java.util.List;

class CapActions {
    static ActionEntry capAction;
    static ActionEntry clearHistoryAction;

    static List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Creature target) {
        if (canCap(performer, subject, target)) {
            List<ActionEntry> entries = new ArrayList<>();
            entries.add(capAction);

            if (MerchantCapMod.getCapFor(target) > 0) {
                entries.add(clearHistoryAction);
            }

            entries.add(0, new ActionEntry((short)-(entries.size()), "Cap Trade", "capping trade"));
            return entries;
        }

        return null;
    }

    static boolean canCap(Creature performer, Item subject, Creature target) {
        return (performer.getPower() >= 2 && target.isNpcTrader() && target.getShop().isPersonal() &&
                 (subject.getTemplateId() == ItemList.wandGM || subject.getTemplateId() == ItemList.wandDeity));
    }
}
