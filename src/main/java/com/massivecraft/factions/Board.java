package com.massivecraft.factions;

import com.massivecraft.factions.mysql.Faction;
import com.massivecraft.factions.zcore.persist.json.JSONBoard;
import org.bukkit.World;

import java.util.Set;


public abstract class Board {
    protected static Board instance = getBoardImpl();

    private static Board getBoardImpl() {
        switch (Conf.backEnd) {
            case JSON:
                return new JSONBoard();
        }
        return null;
    }

    public static Board getInstance() {
        return instance;
    }

    //----------------------------------------------//
    // Get and Set
    //----------------------------------------------//
    public abstract Integer getIdAt(FLocation flocation);

    public abstract Faction getFactionAt(FLocation flocation);

    public abstract void setIdAt(String id, FLocation flocation);

    public abstract void setFactionAt(Faction faction, FLocation flocation);

    public abstract void removeAt(FLocation flocation);

    public abstract Set<FLocation> getAllClaims(String factionId);

    public abstract Set<FLocation> getAllClaims(Faction faction);

    // not to be confused with claims, ownership referring to further member-specific ownership of a claim
    public abstract void clearOwnershipAt(FLocation flocation);

    public abstract void unclaimAll(String factionId);

    public abstract void unclaimAllInWorld(String factionId, World world);

    // Is this coord NOT completely surrounded by coords claimed by the same faction?
    // Simpler: Is there any nearby coord with a faction other than the faction here?
    public abstract boolean isBorderLocation(FLocation flocation);

    // Is this coord connected to any coord claimed by the specified faction?
    public abstract boolean isConnectedLocation(FLocation flocation, Faction faction);

    public abstract boolean hasFactionWithin(FLocation flocation, Faction faction, int radius);

    //----------------------------------------------//
    // Cleaner. Remove orphaned foreign keys
    //----------------------------------------------//

    public abstract void clean();

    //----------------------------------------------//
    // Coord count
    //----------------------------------------------//

    public abstract int getFactionCoordCount(Integer factionId);

    public abstract int getFactionCoordCount(Faction faction);

    public abstract int getFactionCoordCountInWorld(Faction faction, String worldName);


    public abstract void forceSave();

    public abstract void forceSave(boolean sync);

    public abstract boolean load();
}
