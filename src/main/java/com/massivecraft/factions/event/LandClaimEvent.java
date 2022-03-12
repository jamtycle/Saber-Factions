package com.massivecraft.factions.event;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.mysql.Faction;
import com.massivecraft.factions.mysql.FactionPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Event called when an FPlayer claims land for a Faction.
 */
public class LandClaimEvent extends FactionPlayerEvent implements Cancellable {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    private boolean cancelled;
    private final FLocation location;

    public LandClaimEvent(FLocation _location, Faction _faction, FactionPlayer _player) {
        super(_faction, _player);
        cancelled = false;
        location = _location;
    }

    /**
     * Get the FLocation involved in this event.
     *
     * @return the FLocation (also a chunk) involved in this event.
     */
    public FLocation getLocation() {
        return this.location;
    }

    /**
     * Get the id of the faction.
     *
     * @return id of faction as String
     * @deprecated use getFaction().getId() instead.
     */
    @Deprecated
    public int getFactionId() {
        return getFaction().getId_faction();
    }

    /**
     * Get the tag of the faction.
     *
     * @return tag of faction as String
     * @deprecated use getFaction().getTag() instead.
     */
    @Deprecated
    public String getFactionTag() {
        return getFaction().getFaction_tag();
    }

    /**
     * Get the Player involved in this event.
     *
     * @return player from FPlayer.
     * @deprecated use getfPlayer().getPlayer() instead.
     */
    @Deprecated
    public Player getPlayer() {
        return getfPlayer().getPlayer();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        this.cancelled = c;
    }
}
