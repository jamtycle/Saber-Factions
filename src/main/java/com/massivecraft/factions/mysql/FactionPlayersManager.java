package com.massivecraft.factions.mysql;

import com.massivecraft.factions.*;
import com.massivecraft.factions.mysql.abstracts.DBConnection;
import com.massivecraft.factions.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class FactionPlayersManager extends DBConnection {

    // region Variables
    public Map<String, FactionPlayer> faction_players = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
    protected static final FactionPlayersManager instance = getFactionPlayerInstance();
    private ResultSet raw_players;
    // endregion

    public FactionPlayersManager(FactionsPlugin _plugin, int _season) {
        super(_plugin);

        ResultSet faction_info = this.getResultSet("GET_PLAYERS(?)", _season);
        if (faction_info == null) return;

        raw_players = faction_info;

        try {
            while (faction_info.next()) {
                Map<String, Object> class_values = new HashMap<>();
                for (int i = 0; i < faction_info.getMetaData().getColumnCount(); i++) {
                    class_values.put(faction_info.getMetaData().getColumnName(i + 1),
                            faction_info.getObject(faction_info.getMetaData().getColumnName(i + 1)));
                }
                faction_players.put(faction_info.getString("UUID"), new FactionPlayer(plugin, class_values));
            }
        } catch (SQLException ex) {
            plugin.getLogger().info("Couldn't build Players");
            plugin.getLogger().info(ex.getMessage());
        }
    }

    // region Utils
    // Not sure this is going to be used.
    public FactionPlayer generateFPlayer(String id) {
        FactionPlayer player = new FactionPlayer(FactionsPlugin.getInstance(), id);
        this.faction_players.put(player.getPlayer_UUID(), player);
        return player;
    }

    public void forceSave() {
        final Map<String, FactionPlayer> entitiesThatShouldBeSaved = new HashMap<>();
        for (FactionPlayer entity : this.faction_players.values()) {
            if (entity.shouldBeSaved()) {
                entitiesThatShouldBeSaved.put(entity.getPlayer_UUID(), entity);
            }
        }
        saveCore(entitiesThatShouldBeSaved);
    }

    private boolean saveCore(Map<String, FactionPlayer> data) {
        try {
            for (Map.Entry<String, FactionPlayer> player : data.entrySet()) {
                this.executeNonQuery("UPDATE_PLAYER(?, ?, ?, ?, ?, ?, ?, ?, ?)", player.getValue().getDBValues());
            }
            return true;
        } catch (Exception ex) {
            plugin.getLogger().info("SQLException: " + ex.getMessage());
            return false;
        }
    }

    public void clean() {
        for (FactionPlayer fp : this.faction_players.values()) {
            // TODO: Implement this method when FactionsManager was implemented correctly
//            if (!Factions.getInstance().isValidFactionId(fp.getId_faction())) {
//                Logger.print("Reset faction data (invalid faction:" + fp.getId_faction() + ") for player " + fp.getPlayer_name(), Logger.PrefixType.DEFAULT);
////                fp.resetFactionData(false);
//            }
        }
    }

    // This function is under observation.
//    public void convertFrom(FactionPlayersManager old) {
//        this.faction_players
//                .putAll(Maps.transformValues(old.faction_players, arg0 -> new FactionPlayer(arg0)));
//        forceSave();
//        FactionPlayersManagerBase.instance = this;
//    }
    // endregion

    // region Getters
    private static FactionPlayersManager getFactionPlayerInstance() {
        Season season = Season.getSeasonInstance();
//        if (Conf.backEnd == Conf.Backend.JSON) {
//            return new JSONFPlayers();
//        }
        if (Conf.backEnd == Conf.Backend.MYSQL) {
            return new FactionPlayersManager(FactionsPlugin.getInstance(), season.getId_season());
        }
        return null;
    }

    public FactionPlayer[] getOnlinePlayers() {

        return (FactionPlayer[]) Bukkit.getServer().getOnlinePlayers().stream().map(this::getByPlayer).toArray();

//        Set<FactionPlayer> entities = new HashSet<>();
//        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
//            entities.add(this.getByPlayer(player));
//        }
//        return entities;
    }

    public FactionPlayer getByPlayer(Player player) {
        return getById(player.getUniqueId().toString());
    }

    public FactionPlayer getByOfflinePlayer(OfflinePlayer player) {
        return getById(player.getUniqueId().toString());
    }

    public FactionPlayer getById(String id) {
        FactionPlayer player = faction_players.get(id);
        if (player == null) player = generateFPlayer(id);
        return player;
    }

    public ResultSet getRaw_players() {
        return raw_players;
    }

    public List<FactionPlayer> getAllFPlayers() {
        return new ArrayList<>(faction_players.values());
    }
    // endregion
}
