package com.massivecraft.factions.event;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FPlayerEnteredFactionEvent extends FactionPlayerEvent {

    /**
     * @author Illyria Team
     */

    private static final HandlerList handlers = new HandlerList();
    private IFactionPlayer fPlayer;
    private IFaction factionTo;
    private IFaction factionFrom;

    public FPlayerEnteredFactionEvent(IFaction factionTo, IFaction factionFrom, IFactionPlayer fPlayer) {
        super(fPlayer.getFaction(), fPlayer);
        this.factionFrom = factionFrom;
        this.factionTo = factionTo;
        this.fPlayer = fPlayer;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public IFactionPlayer getfPlayer() {
        return fPlayer;
    }

    public IFaction getFactionTo() {
        return factionTo;
    }

    public IFaction getFactionFrom() {
        return factionFrom;
    }

}
