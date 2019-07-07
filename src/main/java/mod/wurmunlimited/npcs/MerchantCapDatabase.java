package mod.wurmunlimited.npcs;

import com.wurmonline.server.creatures.Creature;

import java.sql.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

class MerchantCapDatabase {
    private static final Logger logger = Logger.getLogger(MerchantCapDatabase.class.getName());
    static String dbString = "jdbc:sqlite:merchantcap.db";
    private static boolean created = false;

    private interface Execute {

        void run(Connection conn) throws SQLException;
    }

    static long getCapFor(Creature merchant) throws SQLException {
        AtomicLong cap = new AtomicLong(-1L);

        execute(conn -> {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM caps WHERE id=?");
            ps.setLong(1, merchant.getWurmId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                cap.set(rs.getLong("cap"));
            }
        });

        return cap.get();
    }

    static void setCapFor(Creature merchant, long cap) throws SQLException {
        if (cap == 0) {
            execute(conn -> {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM caps WHERE id=?");
                ps.setLong(1, merchant.getWurmId());
                ps.execute();
            });
        } else {
            execute(conn -> {
                PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO caps (id, cap) VALUES(?, ?)");
                ps.setLong(1, merchant.getWurmId());
                ps.setLong(2, cap);
                ps.execute();
            });
        }
    }

    static long getPlayerSpent(Creature player, Creature merchant) throws SQLException {
        AtomicLong spent = new AtomicLong(0L);

        execute(conn -> {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM player_spending WHERE playerid=? AND merchantid=?");
            ps.setLong(1, player.getWurmId());
            ps.setLong(2, merchant.getWurmId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                spent.set(rs.getLong("spent"));
            }
        });

        return spent.get();
    }

    static void setPlayerSpent(Creature player, Creature merchant, long spent) throws SQLException {
        execute(conn -> {
            PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO player_spending (playerid, merchantid) VALUES(?, ?)");
            ps.setLong(1, player.getWurmId());
            ps.setLong(2, merchant.getWurmId());
            ps.execute();

            PreparedStatement ps2 = conn.prepareStatement("UPDATE player_spending SET spent=spent + ? WHERE playerid=? AND merchantid=?");
            ps2.setLong(1, spent);
            ps2.setLong(2, player.getWurmId());
            ps2.setLong(3, merchant.getWurmId());
            ps2.execute();
        });
    }

    static void removeHistoryFor(Creature merchant) throws SQLException {
        execute(conn -> {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM player_spending WHERE merchantid=?");
            ps.setLong(1, merchant.getWurmId());
            ps.execute();
        });
    }

    private static void execute(Execute execute) throws SQLException {
        Connection merchants = null;
        try {
            merchants = DriverManager.getConnection(dbString);
            if (!created) {
                init(merchants);
            }
            execute.run(merchants);
        } finally {
            try {
                if (merchants != null)
                    merchants.close();
            } catch (SQLException e1) {
                logger.warning("Could not connect to database.");
                e1.printStackTrace();
            }
        }
    }

    private static void init(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS caps (" +
                                                                  "id INTEGER UNIQUE," +
                                                                  "cap INTEGER" +
                                                                  ");");
        PreparedStatement ps2 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS player_spending (" +
                                                                  "playerid INTEGER," +
                                                                  "merchantid INTEGER," +
                                                                  "spent INTEGER NOT NULL DEFAULT 0," +
                                                                  "UNIQUE(playerid, merchantid) ON CONFLICT REPLACE" +
                                                                  ");");
        ps.execute();
        ps2.execute();
        created = true;
    }
}
