package com.massivecraft.factions.mysql;

import com.avaje.ebean.validation.NotEmpty;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.mysql.abstracts.DBConnection;
import com.massivecraft.factions.util.MiscUtil;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FactionsManager extends DBConnection {

    // region Variables
    public final Map<Integer, Faction> factions = new ConcurrentHashMap<>();
    public static final FactionsManager instance = getFactionsInstance();
    private ResultSet raw_factions;
    // endregion

    public FactionsManager(FactionsPlugin _plugin, int _season) {
        super(_plugin);

        ResultSet faction_info = this.getResultSet("GET_FACTIONS(?)", _season);
        if (faction_info == null) return;

        raw_factions = faction_info;

        try {
            while (faction_info.next()) {
                Map<String, Object> class_values = new HashMap<>();
                for (int i = 0; i < faction_info.getMetaData().getColumnCount(); i++) {
                    class_values.put(faction_info.getMetaData().getColumnName(i + 1),
                            faction_info.getObject(faction_info.getMetaData().getColumnName(i + 1)));
                }
                factions.put(faction_info.getInt("id_faction"), new Faction(plugin, class_values));
            }
        } catch (SQLException ex) {
            plugin.getLogger().info("Couldn't build Factions");
            plugin.getLogger().info(ex.getMessage());
        }
    }

    public boolean isValidFactionId(int id) {
        return factions.containsKey(id);
    }

    private Set<String> whichKeysNeedMigration(Set<String> keys) {
        HashSet<String> list = new HashSet<>();
        for (String value : keys) {
            if (!value.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                // Not a valid UUID..
                if (value.matches("[a-zA-Z0-9_]{2,16}")) {
                    // Valid playername, we'll mark this as one for conversion
                    // to UUID
                    list.add(value);
                }
            }
        }
        return list;
    }

    public void forceSave() {
        forceSave(true);
    }

    public void forceSave(boolean sync) {
        final Map<Integer, Faction> entitiesThatShouldBeSaved = new HashMap<>();
        for (Faction entity : this.factions.values())
            entitiesThatShouldBeSaved.put(entity.getId_faction(), entity);

        saveCore(entitiesThatShouldBeSaved, sync);
    }

    private boolean saveCore(Map<Integer, Faction> entities, boolean sync) {
        return true;
//        return DiscUtil.writeCatch(entities, sync);
    }

    // region Getters
    private static FactionsManager getFactionsInstance() {
        Season season = Season.getSeasonInstance();
        if (Conf.backEnd == Conf.Backend.MYSQL) {
            return new FactionsManager(FactionsPlugin.getInstance(), season.getId_season());
        }
        return null;
    }

    public ResultSet getRaw_factions() {
        return raw_factions;
    }

    public Faction getFactionById(int id) {
        return factions.get(id);
    }

    public Faction getByTag(String str) {
        String compStr = MiscUtil.getComparisonString(str);
        for (Faction faction : factions.values()) {
            if (faction.getFaction_tag().equals(compStr)) return faction;
        }
        return null;
    }

    public Faction getBestTagMatch(String start) {
        int best = 0;
        start = start.toLowerCase();
        int minlength = start.length();
        Faction bestMatch = null;
        for (Faction faction : factions.values()) {
            String candidate = faction.getFaction_tag();
            candidate = ChatColor.stripColor(candidate);
            if (candidate.length() < minlength) continue;
            if (!candidate.toLowerCase().startsWith(start)) continue;

            // The closer to zero the better
            int lendiff = candidate.length() - minlength;
            if (lendiff == 0) return faction;

            if (lendiff < best || best == 0)
                best = lendiff;
            bestMatch = faction;
        }
        return bestMatch;
    }

    public Set<String> getFactionTags() {
        Set<String> tags = new HashSet<>();
        for (Faction faction : factions.values()) tags.add(faction.getFaction_tag());
        return tags;
    }

    public Faction getNone() {
        return factions.get(0);
    }

    public Faction getWilderness() {
        return factions.get(0);
    }

    public Faction getGodRealm() {
        return factions.get(-1);
    }

    @NotEmpty
    @NotNull
    public Faction[] getAllFactions() {
        return (Faction[]) factions.values().toArray();
    }
    // endregion
}
