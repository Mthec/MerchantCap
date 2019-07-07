package mod.wurmunlimited.npcs;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DatabaseTest {
    protected Connection db;

    @BeforeEach
    protected void setUp() throws Exception {
        db = DriverManager.getConnection(MerchantCapDatabase.dbString);
        ReflectionUtil.callPrivateMethod(null, MerchantCapDatabase.class.getDeclaredMethod("init", Connection.class), db);
    }

    private static void cleanUp() {
        File file = new File("merchantcap.db");
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    @BeforeAll
    static void reset() {
        cleanUp();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (db != null)
            db.close();
        cleanUp();
    }
}
