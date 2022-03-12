package com.massivecraft.factions;

import com.massivecraft.factions.zcore.persist.json.JSONFactions;

import java.util.ArrayList;
import java.util.Set;

public abstract class Factions {
    protected static Factions instance = getFactionsImpl();

    public static Factions getInstance() {
        return instance;
    }

    private static Factions getFactionsImpl() {
        if (Conf.backEnd == Conf.Backend.JSON) {
            return new JSONFactions();
        }
        return null;
    }

    public abstract IFaction getFactionById(String id);

    public abstract IFaction getFactionById(int id);

    public abstract IFaction getByTag(String str);

    public abstract IFaction getBestTagMatch(String start);

    public abstract boolean isTagTaken(String str);

    public abstract boolean isValidFactionId(String id);

    public abstract IFaction createFaction();

    public abstract void removeFaction(int id);

    public abstract Set<String> getFactionTags();

    public abstract ArrayList<IFaction> getAllFactions();

    @Deprecated
    public abstract IFaction getNone();

    public abstract IFaction getWilderness();

    public abstract IFaction getSafeZone();

    public abstract IFaction getWarZone();

    public abstract void forceSave();

    public abstract void forceSave(boolean sync);

    public abstract void load();
}
