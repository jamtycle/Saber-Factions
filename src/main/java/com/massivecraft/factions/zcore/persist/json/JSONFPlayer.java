package com.massivecraft.factions.zcore.persist.json;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FactionPlayersManagerBase;
import com.massivecraft.factions.zcore.persist.MemoryFPlayer;

public class JSONFPlayer extends MemoryFPlayer {

    public JSONFPlayer(MemoryFPlayer arg0) {
        super(arg0);
    }

    public JSONFPlayer(String id) {
        super(id);
    }

    @Override
    public void remove() {
        ((JSONFPlayers) FactionPlayersManagerBase.getInstance()).fPlayers.remove(getId());
    }

    public boolean shouldBeSaved() {
        return this.getHasFaction() || (this.getPowerRounded() != this.getPowerMaxRounded() && this.getPowerRounded() != (int) Math.round(Conf.powerPlayerStarting));
    }
}
