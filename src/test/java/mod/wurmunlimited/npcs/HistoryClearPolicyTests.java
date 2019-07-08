package mod.wurmunlimited.npcs;

import com.wurmonline.server.WurmCalendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HistoryClearPolicyTests {

    @BeforeEach
    void setUp() {
        WurmCalendar.currentTime = 0;
    }

    @Test
    void testDaily() {
        assertFalse(MerchantCapMod.HistoryClearPolicy.daily.isTimeToClear(0));
        for (int i = 0; i < 60 * 60 * 24; i++) {
            ++WurmCalendar.currentTime;
        }
        assertTrue(MerchantCapMod.HistoryClearPolicy.daily.isTimeToClear(0));
    }

    @Test
    void testWeekly() {
        assertFalse(MerchantCapMod.HistoryClearPolicy.weekly.isTimeToClear(0));
        for (int i = 0; i < 60 * 60 * 24 * 7; i++) {
            ++WurmCalendar.currentTime;
        }
        assertTrue(MerchantCapMod.HistoryClearPolicy.weekly.isTimeToClear(0));
    }

    @Test
    void testMonthly() {
        assertFalse(MerchantCapMod.HistoryClearPolicy.monthly.isTimeToClear(0));
        for (int i = 0; i < 60 * 60 * 24 * 28; i++) {
            ++WurmCalendar.currentTime;
        }
        assertTrue(MerchantCapMod.HistoryClearPolicy.monthly.isTimeToClear(0));
    }

    @Test
    void testYearly() {
        assertFalse(MerchantCapMod.HistoryClearPolicy.yearly.isTimeToClear(0));
        for (int i = 0; i < 60 * 60 * 24 * 365; i++) {
            ++WurmCalendar.currentTime;
        }
        assertTrue(MerchantCapMod.HistoryClearPolicy.yearly.isTimeToClear(0));
    }
}
