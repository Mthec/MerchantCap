package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import mod.wurmunlimited.bml.BMLBuilder;
import mod.wurmunlimited.npcs.MerchantCapMod;

import java.util.Properties;

public class ClearHistoryQuestion extends Question {
    private final Creature merchant;

    public ClearHistoryQuestion(Creature responder, Creature merchant) {
        super(responder, "Clear player spending", "Clear player trade history for this " + merchant.getName().split("_")[0] + ".", QuestionTypes.GM_MANAGEMENT, merchant.getWurmId());
        this.merchant = merchant;
    }

    @Override
    public void answer(Properties properties) {
        Creature responder = getResponder();

        String val = properties.getProperty("clear");
        if (val != null && val.equals("true")) {
            try {
                MerchantCapMod.clearHistoryFor(merchant);
                responder.getCommunicator().sendNormalServerMessage(merchant.getName() + "'" + (merchant.getName().endsWith("s") ? "" : "s") + " history has been cleared.");
            } catch (MerchantCapMod.MerchantCapDatabaseException e) {
                responder.getCommunicator().sendNormalServerMessage("Failed to clear merchant history.");
            }
        }
    }

    @Override
    public void sendQuestion() {
        String bml = new BMLBuilder(id)
                             .text("Are you sure you wish to clear player transaction history for " + merchant.getName() + "?")
                             .text("This cannot be undone.").bold()
                             .newLine()
                             .harray(b -> b.button("clear", "Clear History").spacer().button("cancel", "Cancel"))
                             .build();

        getResponder().getCommunicator().sendBml(300, 400, false, true, bml, 200, 200, 200, title);
    }
}
