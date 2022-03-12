package com.massivecraft.factions.event;

import com.massivecraft.factions.IFactionPlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FPlayerStoppedFlying extends FactionPlayerEvent {

    /**
     * @author Illyria Team
     */

    private static final HandlerList handlers = new HandlerList();
    private IFactionPlayer fPlayer;


    public FPlayerStoppedFlying(IFactionPlayer fPlayer) {
        super(fPlayer.getFaction(), fPlayer);
        this.fPlayer = fPlayer;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public IFactionPlayer getfPlayer() {
        return fPlayer;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
}
