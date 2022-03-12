package com.massivecraft.factions.event;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.mysql.FactionPlayer;
import org.bukkit.event.Cancellable;

public class FPlayerLeaveEvent extends FactionPlayerEvent implements Cancellable {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    boolean cancelled = false;
    private PlayerLeaveReason reason;

    public FPlayerLeaveEvent(FactionPlayer p, IFaction f, PlayerLeaveReason r) {
        super(f, p);
        reason = r;
    }

    /**
     * Get the reason the player left the faction.
     *
     * @return reason player left the faction.
     */
    public PlayerLeaveReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        // Don't let them cancel factions disbanding.
        cancelled = reason != PlayerLeaveReason.DISBAND && reason != PlayerLeaveReason.RESET && c;
    }

    public enum PlayerLeaveReason {
        KICKED, DISBAND, RESET, JOINOTHER, LEAVE, BANNED
    }
}