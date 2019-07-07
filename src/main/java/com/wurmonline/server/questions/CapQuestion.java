package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import mod.wurmunlimited.bml.BMLBuilder;
import mod.wurmunlimited.npcs.MerchantCapMod;

import java.util.Properties;

public class CapQuestion extends Question {
    private final Creature merchant;

    public CapQuestion(Creature responder, Creature merchant) {
        super(responder, "Cap trade", "Cap trade for this " + merchant.getName().split("_")[0] + ".", QuestionTypes.GM_MANAGEMENT, merchant.getWurmId());
        this.merchant = merchant;
    }

    private boolean wasSelected(Properties properties, String id) {
        String val = properties.getProperty(id);
        return val != null && val.equals("true");
    }

    @Override
    public void answer(Properties properties) {
        Creature responder = getResponder();
        if (wasSelected(properties, "cancel")) {
            return;
        }

        if (wasSelected(properties, "set")) {
            String g = (String)properties.getOrDefault("capG", "000");
            String s = (String)properties.getOrDefault("capS", "00");
            String c = (String)properties.getOrDefault("capC", "00");
            String i = (String)properties.getOrDefault("capI", "00");
            if (g.length() > 3 || s.length() > 2 || c.length() > 2 || i.length() > 2) {
                responder.getCommunicator().sendNormalServerMessage("Something went wrong when setting the cap.  No changes were made.");
                return;
            }

            String capString = String.format("%s%s%s%s", g, s, c, i);
            try {
                long cap = Long.parseLong(capString);
                if (cap < 0) {
                    responder.getCommunicator().sendNormalServerMessage("The cap must be a positive number.");
                    return;
                }
                try {
                    MerchantCapMod.setCapFor(merchant, cap);
                    responder.getCommunicator().sendNormalServerMessage("You set the trade cap for " + merchant.getName() + " to " + new Change(cap).getChangeShortString() + ".");
                } catch (MerchantCapMod.MerchantCapDatabaseException e) {
                    e.printStackTrace();
                    responder.getCommunicator().sendNormalServerMessage("Failed to set the trade cap for " + merchant.getName() + ".");
                }
            } catch (NumberFormatException e) {
                responder.getCommunicator().sendNormalServerMessage("\"" + capString + "\"" + " is not a valid value.");
            }
            return;
        }

        if (wasSelected(properties, "remove")) {
            try {
                MerchantCapMod.setCapFor(merchant, 0);
                responder.getCommunicator().sendNormalServerMessage("You remove the trade cap from " + merchant.getName() + ".");
            } catch (MerchantCapMod.MerchantCapDatabaseException e) {
                e.printStackTrace();
                responder.getCommunicator().sendNormalServerMessage("Failed to set the trade cap for " + merchant.getName() + ".");
            }
        }
    }

    @Override
    public void sendQuestion() {
        long merchantCap = MerchantCapMod.getCapFor(merchant);
        if (merchantCap == -1)
            merchantCap = 0;
        Change cap = new Change(merchantCap);

        String bml = new BMLBuilder(id)
                             .text("Here you can set a per player cap on the total trade value for transactions with " + merchant.getName() + ".")
                             .harray(b -> b.entry("capG", String.valueOf(cap.goldCoins), 3).spacer()
                                                  .entry("capS", String.valueOf(cap.silverCoins), 2).spacer()
                                                  .entry("capC", String.valueOf(cap.copperCoins), 2).spacer()
                                                  .entry("capI", String.valueOf(cap.ironCoins), 2).spacer())
                             .newLine()
                             .harray(b -> b.button("set", "Set cap").spacer().button("remove", "Remove Cap").spacer().button("cancel", "Cancel"))
                             .build();

        getResponder().getCommunicator().sendBml(300, 400, false, true, bml, 200, 200, 200, title);
    }
}
