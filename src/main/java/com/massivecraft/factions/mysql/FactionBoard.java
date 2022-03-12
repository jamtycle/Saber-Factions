package com.massivecraft.factions.mysql;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.*;
import com.massivecraft.factions.mysql.abstracts.DBConnection;
import com.massivecraft.factions.util.Logger;
import com.massivecraft.factions.zcore.util.DiscUtil;
import com.massivecraft.factions.zcore.util.TL;
import com.massivecraft.factions.zcore.util.TagReplacer;
import com.massivecraft.factions.zcore.util.TagUtil;
import mkremins.fanciful.FancyMessage;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.*;

public class FactionBoard extends DBConnection {

    public FactionBoardMap faction_location_ids = new FactionBoardMap();
    protected static FactionBoard instance = getInstance();

    private static FactionBoard getInstance() {
        if (Conf.backEnd == Conf.Backend.MYSQL) {
            return new FactionBoard(FactionsPlugin.getInstance());
        }
        return null;
    }

    public FactionBoard(FactionsPlugin _plugin) {
        super(_plugin);
    }

    //----------------------------------------------//
    // Get and Set
    //----------------------------------------------//
    public Integer getIdAt(FLocation flocation) {
        if (!faction_location_ids.containsKey(flocation)) {
            return 0;
        }

        return faction_location_ids.get(flocation);
    }

    public void setIdAt(Integer id, FLocation flocation) {
        clearOwnershipAt(flocation);

        if (id.equals(0)) {
            removeAt(flocation);
        }

        faction_location_ids.put(flocation, id);
    }

    public Faction getFactionAt(FLocation flocation) {
        // Meh, not convinced but, will work.
        if (FactionsManager.instance == null) return null;
        return FactionsManager.instance.getFactionById(getIdAt(flocation));
    }

    public void setFactionAt(Faction faction, FLocation flocation) {
        setIdAt(faction.getId_faction(), flocation);
    }

    public void removeAt(FLocation flocation) {
//        Faction faction = getFactionAt(flocation);
//        for (Entity entity : flocation.getChunk().getEntities()) {
//            if (entity instanceof Player) {
//                FactionPlayer fPlayer = FactionPlayersManagerBase.getInstance().getByPlayer((Player) entity);
//            }
//        }
        clearOwnershipAt(flocation);
        faction_location_ids.remove(flocation);
    }

    public Set<FLocation> getAllClaims(Integer factionId) {
        Set<FLocation> locs = new HashSet<>();
        for (Map.Entry<FLocation, Integer> entry : faction_location_ids.entrySet()) {
            if (entry.getValue().equals(factionId)) {
                locs.add(entry.getKey());
            }
        }
        return locs;
    }

    public Set<FLocation> getAllClaims(Faction faction) {
        return getAllClaims(faction.getId_faction());
    }

    // not to be confused with claims, ownership referring to further member-specific ownership of a claim
    public void clearOwnershipAt(FLocation flocation) {
        Faction faction = getFactionAt(flocation);
        if (faction != null && faction.isNormal()) {
            faction.clearClaimOwnership(flocation);
        }
    }

    public void unclaimAll(Integer factionId) {
        if (FactionsManager.instance == null) return;

        Faction faction = FactionsManager.instance.getFactionById(factionId);
        if (faction != null && faction.isNormal()) {
            faction.clearAllClaimOwnership();
//            faction.clearSpawnerChunks();
        }
        clean(factionId);
    }

    public void unclaimAllInWorld(Integer factionId, World world) {
        for (FLocation loc : getAllClaims(factionId)) {
            if (loc.getWorldName().equals(world.getName())) {
                removeAt(loc);
            }
        }
    }

    public void clean(Integer factionId) {
        faction_location_ids.removeFaction(factionId);
    }

    // Is this coord NOT completely surrounded by coords claimed by the same faction?
    // Simpler: Is there any nearby coord with a faction other than the faction here?
    public boolean isBorderLocation(FLocation flocation) {
        Faction faction = getFactionAt(flocation);
        FLocation a = flocation.getRelative(1, 0);
        FLocation b = flocation.getRelative(-1, 0);
        FLocation c = flocation.getRelative(0, 1);
        FLocation d = flocation.getRelative(0, -1);
        return faction != getFactionAt(a) || faction != getFactionAt(b) || faction != getFactionAt(c) || faction != getFactionAt(d);
    }

    // Is this coord connected to any coord claimed by the specified faction?
    public boolean isConnectedLocation(FLocation flocation, Faction faction) {
        FLocation a = flocation.getRelative(1, 0);
        FLocation b = flocation.getRelative(-1, 0);
        FLocation c = flocation.getRelative(0, 1);
        FLocation d = flocation.getRelative(0, -1);
        return faction == getFactionAt(a) || faction == getFactionAt(b) || faction == getFactionAt(c) || faction == getFactionAt(d);
    }

    /**
     * Checks if there is another faction within a given radius other than Wilderness. Used for HCF feature that
     * requires a 'buffer' between factions.
     *
     * @param flocation - center location.
     * @param faction   - faction checking for.
     * @param radius    - chunk radius to check.
     * @return true if another Faction is within the radius, otherwise false.
     */
    public boolean hasFactionWithin(FLocation flocation, Faction faction, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                FLocation relative = flocation.getRelative(x, z);
                Faction other = getFactionAt(relative);

                if (other.isNormal() && other != faction) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clean() {
        if (FactionsManager.instance == null) return;

        Iterator<Map.Entry<FLocation, Integer>> iterator = faction_location_ids.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<FLocation, Integer> entry = iterator.next();
            if (!FactionsManager.instance.isValidFactionId(entry.getValue())) {
                Logger.print("Board cleaner removed " + entry.getValue() + " from " + entry.getKey(), Logger.PrefixType.DEFAULT);
                iterator.remove();
            }
        }
    }

    //----------------------------------------------//
    // Cleaner. Remove orphaned foreign keys
    //----------------------------------------------//

    public int getFactionCoordCount(Integer factionId) {
        return faction_location_ids.getOwnedLandCount(factionId);
    }

    //----------------------------------------------//
    // Coord count
    //----------------------------------------------//

    public int getFactionCoordCount(Faction faction) {
        return getFactionCoordCount(faction.getId_faction());
    }

    public int getFactionCoordCountInWorld(Faction faction, String worldName) {
        Integer factionId = faction.getId_faction();
        int ret = 0;
        for (Map.Entry<FLocation, Integer> entry : faction_location_ids.entrySet()) {
            if (entry.getValue().equals(factionId) && entry.getKey().getWorldName().equals(worldName)) {
                ret += 1;
            }
        }
        return ret;
    }

    //----------------------------------------------//
    // Map generation
    //----------------------------------------------//

    private List<String> oneLineToolTip(Faction faction, FactionPlayer to) {
        return Collections.singletonList(faction.describeTo(to));
    }

    @SuppressWarnings("unused")
    private List<String> getToolTip(Faction faction, FactionPlayer to) {
        List<String> ret = new ArrayList<>();
        List<String> show = FactionsPlugin.getInstance().getConfig().getStringList("map");

        if (!faction.isNormal()) {
            String tag = faction.getTag(to);
            // send header and that's all
            String header = show.get(0);
            if (TagReplacer.HEADER.contains(header)) {
                ret.add(FactionsPlugin.getInstance().txt.titleize(tag));
            } else {
                ret.add(FactionsPlugin.getInstance().txt.parse(TagReplacer.FACTION.replace(header, tag)));
            }
            return ret; // we only show header for non-normal factions
        }

        for (String raw : show) {
            // Hack to get rid of the extra underscores in title normally used to center tag
            if (raw.contains("{header}")) {
                raw = raw.replace("{header}", faction.getTag(to));
            }

            String parsed = TagUtil.parsePlain(faction, to, raw); // use relations
            if (parsed == null) {
                continue; // Due to minimal f show.
            }

            if (TagUtil.hasFancy(parsed)) {
                List<FancyMessage> fancy = TagUtil.parseFancy(faction, to, parsed);
                if (fancy != null) {
                    for (FancyMessage msg : fancy) {
                        ret.add((FactionsPlugin.getInstance().txt.parse(msg.toOldMessageFormat())));
                    }
                }
                continue;
            }

            if (!parsed.contains("{notFrozen}") && !parsed.contains("{notPermanent}")) {
                if (parsed.contains("{ig}")) {
                    // replaces all variables with no home TL
                    parsed = parsed.substring(0, parsed.indexOf("{ig}")) + TL.COMMAND_SHOW_NOHOME;
                }
                if (parsed.contains("%")) {
                    parsed = parsed.replaceAll("%", ""); // Just in case it got in there before we disallowed it.
                }
                ret.add(FactionsPlugin.getInstance().txt.parse(parsed));
            }
        }

        return ret;
    }

    // region SaveRelated Methods

    public void convertFrom(FactionBoard old) {
        this.faction_location_ids = old.faction_location_ids;
        forceSave();
        FactionBoard.instance = this;
    }

    public Map<String, Map<String, Integer>> dumpAsSaveFormat() {
        Map<String, Map<String, Integer>> worldCoordIds = new HashMap<>();

        String worldName, coords;
        Integer id;

        for (Map.Entry<FLocation, Integer> entry : faction_location_ids.entrySet()) {
            worldName = entry.getKey().getWorldName();
            coords = entry.getKey().getCoordString();
            id = entry.getValue();
            if (!worldCoordIds.containsKey(worldName)) {
                worldCoordIds.put(worldName, new TreeMap<>());
            }

            worldCoordIds.get(worldName).put(coords, id);
        }

        return worldCoordIds;
    }

    public void loadFromSaveFormat(Map<String, Map<String, Integer>> worldCoordIds) {
        faction_location_ids.clear();

        String worldName;
        String[] coords;
        int x, z;
        Integer factionId;

        for (Map.Entry<String, Map<String, Integer>> entry : worldCoordIds.entrySet()) {
            worldName = entry.getKey();
            for (Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                coords = entry2.getKey().trim().split("[,\\s]+");
                x = Integer.parseInt(coords[0]);
                z = Integer.parseInt(coords[1]);
                factionId = entry2.getValue();
                faction_location_ids.put(new FLocation(worldName, x, z), factionId);
            }
        }
    }

    // TODO: Implement the save method correctly
    public void forceSave() {
        forceSave(true);
    }

    public void forceSave(boolean sync) {
//        DiscUtil.writeCatch(file, FactionsPlugin.getInstance().gson.toJson(dumpAsSaveFormat()), sync);
    }

    // TODO: Implement this logic to the constructor.
    public boolean load() {
        return true;
//        Logger.print("Loading board from disk", Logger.PrefixType.DEFAULT);
//
//        if (!file.exists()) {
//            Logger.print("No board to load from disk. Creating new file.", Logger.PrefixType.DEFAULT);
//            forceSave();
//            return true;
//        }
//
//        try {
//            Type type = new TypeToken<Map<String, Map<String, String>>>() {
//            }.getType();
//            Map<String, Map<String, String>> worldCoordIds = FactionsPlugin.getInstance().gson.fromJson(DiscUtil.read(file), type);
//            loadFromSaveFormat(worldCoordIds);
//            Logger.print("Loaded " + flocationIds.size() + " board locations", Logger.PrefixType.DEFAULT);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Logger.print("Failed to load the board from disk.", Logger.PrefixType.FAILED);
//            return false;
//        }
//
//        return true;
    }

    // endregion

}
