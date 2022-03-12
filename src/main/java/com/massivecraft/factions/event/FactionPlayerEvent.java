package com.massivecraft.factions.event;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.mysql.Faction;
import com.massivecraft.factions.mysql.FactionPlayer;

/**
 * Represents an event involving a Faction and a FPlayer.
 */
public class FactionPlayerEvent extends FactionEvent {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    protected final FactionPlayer faction_player;

    public FactionPlayerEvent(Faction _faction, FactionPlayer _player) {
        super(_faction);
        this.faction_player = _player;
    }

    public FactionPlayer getfPlayer() {
        return this.faction_player;
    }
}
