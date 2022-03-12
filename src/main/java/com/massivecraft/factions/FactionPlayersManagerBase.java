package com.massivecraft.factions;

import com.massivecraft.factions.mysql.FactionPlayer;
import com.massivecraft.factions.mysql.FactionPlayersManager;
import com.massivecraft.factions.zcore.persist.json.JSONFPlayers;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

public abstract class FactionPlayersManagerBase {
    protected static FactionPlayersManagerBase instance = getFPlayersImpl();

    public static FactionPlayersManagerBase getInstance() {
        return instance;
    }

    private static FactionPlayersManagerBase getFPlayersImpl() {
//        if (Conf.backEnd == Conf.Backend.JSON) {
//            return new JSONFPlayers();
//        }
        if (Conf.backEnd == Conf.Backend.MYSQL) {
            return new FactionPlayersManager();
        }
        return null;
    }

    public abstract void clean();

    public abstract FactionPlayer[] getOnlinePlayers();

    public abstract FactionPlayer getByPlayer(Player player);

    public abstract Collection<FactionPlayer> getAllFPlayers();

    public abstract void forceSave();

    public abstract void forceSave(boolean sync);

    public abstract FactionPlayer getByOfflinePlayer(OfflinePlayer player);

    public abstract FactionPlayer getById(String string);

    public abstract void load();
}
