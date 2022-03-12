package com.massivecraft.factions.event;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.mysql.Faction;
import com.massivecraft.factions.mysql.FactionPlayer;
import org.bukkit.event.Cancellable;

/**
 * Event called when a player regenerate power.
 */
public class PowerRegenEvent extends FactionPlayerEvent implements Cancellable {

    private boolean cancelled = false;
    private double delta;

    public PowerRegenEvent(IFaction f, IFactionPlayer p, double delta) {
        super(f, p);
        this.delta = delta;
    }

    public PowerRegenEvent(Faction _faction, FactionPlayer _player, double _delta) {
        super(_faction, _player);
        this.delta = _delta;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
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