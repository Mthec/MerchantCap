package mod.wurmunlimited.npcs;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import mod.wurmunlimited.WurmObjectsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MerchantCapDatabaseTests extends DatabaseTest {
    private WurmObjectsFactory factory;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        factory = new WurmObjectsFactory();
        db = DriverManager.getConnection(MerchantCapDatabase.dbString);
    }

    @Test
    void testTablesCreated() throws SQLException {
        assertTrue(db.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='caps';").executeQuery().next());
        assertTrue(db.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='player_spending';").executeQuery().next());
    }

    @Test
    void testGetCapFor() throws SQLException {
        Creature merchant = factory.createNewCreature(CreatureTemplateIds.SALESMAN_CID);
        long cap = 100;

        PreparedStatement ps = db.prepareStatement("INSERT INTO caps VALUES(?, ?)");
        ps.setLong(1, merchant.getWurmId());
        ps.setLong(2, cap);
        ps.execute();

        assertEquals(cap, MerchantCapDatabase.getCapFor(merchant));
    }

    @Test
    void testSetCapFor() throws SQLException {
        Creature merchant = factory.createNewCreature(CreatureTemplateIds.SALESMAN_CID);
        long cap = 100;

        MerchantCapDatabase.setCapFor(merchant, cap);

        PreparedStatement ps = db.prepareStatement("SELECT (COUNT(*) > 0) FROM caps WHERE id=? AND cap=?");
        ps.setLong(1, merchant.getWurmId());
        ps.setLong(2, cap);
        ResultSet rs = ps.executeQuery();

        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }

    @Test
    void testRemoveCapFor() throws SQLException {
        Creature merchant = factory.createNewCreature(CreatureTemplateIds.SALESMAN_CID);
        long cap = 100;

        MerchantCapDatabase.setCapFor(merchant, cap);
        MerchantCapDatabase.setCapFor(merchant, 0);

        PreparedStatement ps = db.prepareStatement("SELECT (COUNT(*) > 0) FROM caps WHERE id=?");
        ps.setLong(1, merchant.getWurmId());
        ResultSet rs = ps.executeQuery();

        assertTrue(rs.next());
        assertFalse(rs.getBoolean(1));
    }

    @Test
    void testGetPlayerSpent() throws SQLException {
        Creature player = factory.createNewPlayer();
        Creature merchant = factory.createNewCreature(CreatureTemplateIds.SALESMAN_CID);
        long spent = 100;

        PreparedStatement ps = db.prepareStatement("INSERT INTO player_spending (playerid, merchantid, spent) VALUES(?, ?, ?)");
        ps.setLong(1, player.getWurmId());
        ps.setLong(2, merchant.getWurmId());
        ps.setLong(3, spent);
        ps.execute();

        assertEquals(spent, MerchantCapDatabase.getPlayerSpent(player, merchant));
    }

    @Test
    void testSetPlayerSpent() throws SQLException {
        Creature player = factory.createNewPlayer();
        Creature merchant = factory.createNewCreature(CreatureTemplateIds.SALESMAN_CID);
        long spent = 100;

        MerchantCapDatabase.setPlayerSpent(player, merchant, spent);

        PreparedStatement ps = db.prepareStatement("SELECT (COUNT(*) > 0) FROM player_spending WHERE playerid=? AND merchantid=?");
        ps.setLong(1, player.getWurmId());
        ps.setLong(2, merchant.getWurmId());
        ResultSet rs = ps.executeQuery();

        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertEquals(spent, MerchantCapDatabase.getPlayerSpent(player, merchant));
    }

    @Test
    void testClearHistoryFor() throws SQLException, MerchantCapMod.MerchantCapDatabaseException {
        Creature merchant = factory.createNewCreature(CreatureTemplateIds.SALESMAN_CID);

        for (int i = 0; i < 10; ++i) {
            MerchantCapMod.addToPlayerSpent(factory.createNewPlayer(), merchant, 10);
        }

        assertDoesNotThrow(() -> MerchantCapMod.clearHistoryFor(merchant));

        PreparedStatement ps = db.prepareStatement("SELECT (COUNT(*) > 0) FROM player_spending WHERE merchantid=?");
        ps.setLong(1, merchant.getWurmId());
        ResultSet rs = ps.executeQuery();

        assertTrue(rs.next());
        assertFalse(rs.getBoolean(1));
    }
}
