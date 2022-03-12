package com.massivecraft.factions.scoreboards.sidebar;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.mysql.Faction;
import com.massivecraft.factions.mysql.FactionPlayer;
import com.massivecraft.factions.scoreboards.FSidebarProvider;

import java.util.List;
import java.util.ListIterator;

public class FInfoSidebar extends FSidebarProvider {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    private final Faction faction;

    public FInfoSidebar(Faction _faction) {
        faction = _faction;
    }

    @Override
    public String getTitle(FactionPlayer _player) {
        return faction.getRelationTo(_player).getColor() + faction.getFaction_tag();
    }

    @Override
    public List<String> getLines(FactionPlayer fplayer) {
        List<String> lines = FactionsPlugin.getInstance().getConfig().getStringList("scoreboard.finfo");

        ListIterator<String> it = lines.listIterator();
        while (it.hasNext()) {
            String next = it.next();
            if (next == null) {
                it.remove();
                continue;
            }
            String replaced = replaceTags(faction, fplayer, next);
            if (replaced == null) {
                it.remove();
            } else {
                it.set(replaced);
            }
        }
        return lines;
    }
}