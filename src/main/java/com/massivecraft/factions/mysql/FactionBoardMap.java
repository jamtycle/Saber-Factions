package com.massivecraft.factions.mysql;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FactionPlayersManagerBase;
import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.zcore.util.TL;

import java.util.Collection;
import java.util.HashMap;

public class FactionBoardMap extends HashMap<FLocation, Integer> {
    private static final long serialVersionUID = -6689617828610585368L;

    Multimap<Integer, FLocation> factionToLandMap = HashMultimap.create();

    @Override
    public Integer put(FLocation faction_location, Integer faction_id) {
        Integer previousValue = super.put(faction_location, faction_id);
        if (previousValue != null) {
            factionToLandMap.remove(previousValue, faction_location);
        }

        factionToLandMap.put(faction_id, faction_location);
        return previousValue;
    }

    @Override
    public Integer remove(Object key) {
        Integer result = super.remove(key);
        if (result != null) {
            FLocation faction_location = (FLocation) key;
            factionToLandMap.remove(result, faction_location);
        }

        return result;
    }

    @Override
    public void clear() {
        super.clear();
        factionToLandMap.clear();
    }

    public int getOwnedLandCount(Integer factionId) {
        return factionToLandMap.get(factionId).size();
    }

    public void removeFaction(Integer factionId) {
        Collection<FLocation> fLocations = factionToLandMap.removeAll(factionId);
        for (FactionPlayer player : FactionPlayersManagerBase.getInstance().getOnlinePlayers()) {
            for (FLocation locations : fLocations) {
                super.remove(locations);
            }
        }
    }
}