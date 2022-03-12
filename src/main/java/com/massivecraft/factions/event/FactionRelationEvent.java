package com.massivecraft.factions.event;

import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a Faction relation is called.
 */
public class FactionRelationEvent extends Event {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    private static final HandlerList handlers = new HandlerList();

    private IFaction fsender;
    private IFaction ftarget;
    private Relation foldrel;
    private Relation frel;

    public FactionRelationEvent(IFaction sender, IFaction target, Relation oldrel, Relation rel) {
        fsender = sender;
        ftarget = target;
        foldrel = oldrel;
        frel = rel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Relation getOldRelation() {
        return foldrel;
    }

    public Relation getRelation() {
        return frel;
    }

    public IFaction getFaction() {
        return fsender;
    }

    public IFaction getTargetFaction() {
        return ftarget;
    }
}
