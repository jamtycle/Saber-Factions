package com.massivecraft.factions.event;

import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.mysql.Faction;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event involving a Faction.
 */
public class FactionEvent extends Event {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    private static final HandlerList handlers = new HandlerList();
    private final Faction faction;

    public FactionEvent(Faction _faction){
        faction = _faction;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the Faction involved in the event.
     *
     * @return faction involved in the event.
     */
//    @Deprecated
//    public IFaction getFaction() {
//        return null;
////        return this.faction;
//    }

    public Faction getFaction(){
        return faction;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
