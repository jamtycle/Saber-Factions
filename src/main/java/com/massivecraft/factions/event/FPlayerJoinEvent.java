package com.massivecraft.factions.event;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import org.bukkit.event.Cancellable;

/**
 * Event called when an FPlayer joins a Faction.
 */
public class FPlayerJoinEvent extends FactionPlayerEvent implements Cancellable {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    PlayerJoinReason reason;
    boolean cancelled = false;

    public FPlayerJoinEvent(IFactionPlayer fp, IFaction f, PlayerJoinReason r) {
        super(f, fp);
        reason = r;
    }

    /**
     * Get the reason the player joined the faction.
     *
     * @return reason player joined the faction.
     */
    public PlayerJoinReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        cancelled = c;
    }

    public enum PlayerJoinReason {
        CREATE, LEADER, COMMAND
    }
}