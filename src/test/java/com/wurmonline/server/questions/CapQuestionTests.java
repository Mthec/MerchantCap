package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.MonetaryConstants;
import mod.wurmunlimited.WurmObjectsFactory;
import mod.wurmunlimited.npcs.DatabaseTest;
import mod.wurmunlimited.npcs.MerchantCapMod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static mod.wurmunlimited.Assert.receivedMessageContaining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CapQuestionTests extends DatabaseTest {
    private Creature player;
    private Creature merchant;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        WurmObjectsFactory factory = new WurmObjectsFactory();
        player = factory.createNewPlayer();
        merchant = factory.createNewMerchant(factory.createNewPlayer());
    }

    private void askQuestion(Properties answers) {
        CapQuestion question = new CapQuestion(player, merchant);
        //question.sendQuestion();
        question.answer(answers);
    }

    @Test
    void testIronsEntry() {
        long cap = 96;
        Properties properties = new Properties();
        properties.setProperty("set", "true");
        properties.setProperty("capI", String.valueOf(cap));
        askQuestion(properties);

        assertEquals(cap, MerchantCapMod.getCapFor(merchant));
    }

    @Test
    void testCopperEntry() {
        long cap = 96;
        Properties properties = new Properties();
        properties.setProperty("set", "true");
        properties.setProperty("capC", String.valueOf(cap));
        askQuestion(properties);

        assertEquals(cap * MonetaryConstants.COIN_COPPER, MerchantCapMod.getCapFor(merchant));
    }

    @Test
    void testSilverEntry() {
        long cap = 96;
        Properties properties = new Properties();
        properties.setProperty("set", "true");
        properties.setProperty("capS", String.valueOf(cap));
        askQuestion(properties);

        assertEquals(cap * MonetaryConstants.COIN_SILVER, MerchantCapMod.getCapFor(merchant));
    }

    @Test
    void testGoldEntry() {
        long cap = 96;
        Properties properties = new Properties();
        properties.setProperty("set", "true");
        properties.setProperty("capG", String.valueOf(cap));
        askQuestion(properties);

        assertEquals(cap * MonetaryConstants.COIN_GOLD, MerchantCapMod.getCapFor(merchant));
    }

    @Test
    void testAllEntry() {
        long cap = 123456789;
        Change change = new Change(cap);
        Properties properties = new Properties();
        properties.setProperty("set", "true");
        properties.setProperty("capG", String.valueOf(change.goldCoins));
        properties.setProperty("capS", String.valueOf(change.silverCoins));
        properties.setProperty("capC", String.valueOf(change.copperCoins));
        properties.setProperty("capI", String.valueOf(change.ironCoins));
        askQuestion(properties);

        assertEquals(cap, MerchantCapMod.getCapFor(merchant));
    }

    @Test
    void testEntryTooLong() {
        long cap = 123456789;
        Properties properties = new Properties();
        properties.setProperty("set", "true");
        properties.setProperty("capI", String.valueOf(cap));
        askQuestion(properties);

        assertThat(player, receivedMessageContaining("Something went wrong"));
        assertNotEquals(-1, MerchantCapMod.getCapFor(merchant));
    }

    @Test
    void testEntryNotANumber() {
        Properties properties = new Properties();
        properties.setProperty("set", "true");
        properties.setProperty("capI", "ab");
        askQuestion(properties);

        assertThat(player, receivedMessageContaining("not a valid value"));
        assertNotEquals(-1, MerchantCapMod.getCapFor(merchant));
    }

    @Test
    void testEntryNegative() {
        long cap = -96;
        Properties properties = new Properties();
        properties.setProperty("set", "true");
        properties.setProperty("capG", String.valueOf(cap));
        askQuestion(properties);

        assertThat(player, receivedMessageContaining("must be a positive"));
        assertNotEquals(-1, MerchantCapMod.getCapFor(merchant));
    }

    @Test
    void testRemoveCap() throws MerchantCapMod.MerchantCapDatabaseException {
        long cap = 100;
        MerchantCapMod.setCapFor(merchant, cap);

        Properties properties = new Properties();
        properties.setProperty("remove", "true");
        askQuestion(properties);

        assertThat(player, receivedMessageContaining("remove the trade cap"));
        assertEquals(-1, MerchantCapMod.getCapFor(merchant));
    }
}
