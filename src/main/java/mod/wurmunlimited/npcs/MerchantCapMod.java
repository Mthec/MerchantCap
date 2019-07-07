package mod.wurmunlimited.npcs;

import com.wurmonline.server.behaviours.CapAction;
import com.wurmonline.server.behaviours.ClearHistoryAction;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.TradeHandler;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.items.TradingWindow;
import com.wurmonline.shared.exceptions.WurmException;
import javassist.NotFoundException;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.logging.Logger;

public class MerchantCapMod implements WurmServerMod, Initable, ServerStartedListener {
    private static final Logger logger = Logger.getLogger(MerchantCapMod.class.getName());

    public static class MerchantCapDatabaseException extends WurmException {

        MerchantCapDatabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static void setCapFor(Creature merchant, long cap) throws MerchantCapDatabaseException {
        try {
            MerchantCapDatabase.setCapFor(merchant, cap);
        } catch (SQLException e) {
            throw new MerchantCapDatabaseException("Failed to set cap for " + merchant.getName() + ".", e);
        }
    }

    public static long getCapFor(Creature merchant) {
        try {
            return MerchantCapDatabase.getCapFor(merchant);
        } catch (SQLException e) {
            logger.warning("Failed to get cap for " + merchant.getName() + ", returning 0.");
            e.printStackTrace();
        }

        return 0;
    }

    public static void addToPlayerSpent(Creature player, Creature merchant, long spent) throws MerchantCapDatabaseException {
        try {
            MerchantCapDatabase.setPlayerSpent(player, merchant, spent);
        } catch (SQLException e) {
            throw new MerchantCapDatabaseException("Failed to set player_spending for " + merchant.getName() + ".", e);
        }
    }

    public static long getPlayerSpent(Creature player, Creature merchant) {
        try {
            return MerchantCapDatabase.getPlayerSpent(player, merchant);
        } catch (SQLException e) {
            logger.warning("Failed to get " + player.getName() + " spending for " + merchant.getName() + ", returning 0.");
            e.printStackTrace();
        }

        return 0;
    }

    public static void clearHistoryFor(Creature merchant) throws MerchantCapDatabaseException {
        try {
            MerchantCapDatabase.removeHistoryFor(merchant);
        } catch (SQLException e) {
            logger.warning("Error when deleting merchant history for " + merchant.getName() + ", reason follows:");
            e.printStackTrace();
            throw new MerchantCapDatabaseException("Error when deleting merchant history.", e);
        }
    }

    @Override
    public void init() {
        HookManager manager = HookManager.getInstance();

        manager.registerHook("com.wurmonline.server.creatures.TradeHandler",
                "addItemsToTrade",
                "()V",
                () -> this::addItemsToTrade);

        manager.registerHook("com.wurmonline.server.items.Trade",
                "makeTrade",
                "()Z",
                () -> this::makeTrade);

        try {
            if (manager.getClassPool().getCtClass("com.wurmonline.server.creatures.BuyerHandler") != null) {
                manager.registerHook("com.wurmonline.server.creatures.BuyerHandler",
                        "addItemsToTrade",
                        "()V",
                        () -> this::addItemsToTrade);

                manager.registerHook("com.wurmonline.server.items.BuyerTrade",
                        "makeBuyerTrade",
                        "()Z",
                        () -> this::makeTrade);
            }
            logger.info("Buyer found.");
        } catch (NotFoundException ignored) {
            logger.info("Buyer not found");
        }
    }

    @Override
    public void onServerStarted() {
        ModActions.registerAction(new CapAction());
        ModActions.registerAction(new ClearHistoryAction());
    }

    Object addItemsToTrade(Object o, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        try {
            Trade trade = ReflectionUtil.getPrivateField(o, o.getClass().getDeclaredField("trade"));
            Creature player = trade.creatureOne;
            Creature merchant = trade.creatureTwo;
            if (merchant.getShop().isPersonal() && merchant.getShop().getOwnerId() != player.getWurmId()) {
                long merchantCap = getCapFor(merchant);
                if (merchantCap != -1) {
                    if (getPlayerSpent(player, merchant) >= merchantCap) {
                        merchant.getTrade().end(merchant, false);
                        player.getCommunicator().sendNormalServerMessage(merchant.getName() + " says 'Oh I'm sorry, but I have reached my trade cap with you.'");
                        return null;
                    }
                }
            }
        } catch (NoSuchFieldException e) {
            logger.warning("Could not get merchant, reason follows:");
            e.printStackTrace();
        }
        return method.invoke(o, args);
    }

    Object makeTrade(Object o, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Trade trade = (Trade)o;
        Creature player = trade.creatureOne;
        Creature merchant = trade.creatureTwo;
        if (merchant.isNpcTrader() && merchant.getShop().isPersonal() && merchant.getShop().getOwnerId() != player.getWurmId()) {
            long merchantCap = getCapFor(merchant);
            if (merchantCap != -1) {
                long shopDiff = 0;
                TradeHandler handler = merchant.getTradeHandler();
                TradingWindow merchantWindow = trade.getTradingWindow(3);
                TradingWindow playerWindow = trade.getTradingWindow(4);
                for (Item i : merchantWindow.getAllItems()) {
                    if (!i.isCoin())
                        shopDiff += handler.getTraderSellPriceForItem(i, merchantWindow);
                }

                for (Item i : playerWindow.getAllItems()) {
                    if (!i.isCoin())
                        shopDiff += handler.getTraderBuyPriceForItem(i);
                }

                if (getPlayerSpent(player, merchant) + shopDiff > merchantCap) {
                    player.getCommunicator().sendNormalServerMessage(merchant.getName() + " says 'I cannot make that trade as it would exceed my trade cap with you.'");
                    return false;
                }

                boolean toReturn = (boolean)method.invoke(o, args);
                if (toReturn) {
                    try {
                        addToPlayerSpent(player, merchant, shopDiff);
                        player.getCommunicator().sendNormalServerMessage(merchant.getName() + " says 'There is " + new Change(getCapFor(merchant) - getPlayerSpent(player, merchant)).getChangeShortString() + " left until you reach my trade cap.'");
                    } catch (MerchantCapDatabaseException e) {
                        e.printStackTrace();
                    }
                }
                return toReturn;
            }
        }

        return method.invoke(o, args);
    }
}
