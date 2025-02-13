package com.massivecraft.factions.scoreboards.sidebar;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.scoreboards.FSidebarProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class FDefaultSidebar extends FSidebarProvider {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    @Override
    public String getTitle(IFactionPlayer fplayer) {
        return replaceTags(fplayer, FactionsPlugin.getInstance().getConfig().getString("scoreboard.default-title", "{name}"));
    }

    @Override
    public List<String> getLines(IFactionPlayer fplayer) {
        if (fplayer.getHasFaction()) {
            return getOutput(fplayer, "scoreboard.default");
        } else if (FactionsPlugin.getInstance().getConfig().getBoolean("scoreboard.factionless-enabled", false)) {
            return getOutput(fplayer, "scoreboard.factionless");
        }
        return getOutput(fplayer, "scoreboard.default"); // no faction, factionless-board disabled
    }

    public List<String> getOutput(IFactionPlayer fplayer, String list) {
        List<String> lines = FactionsPlugin.getInstance().getConfig().getStringList(list);

        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>(0);
        }

        lines = new ArrayList<>(lines);

        ListIterator<String> it = lines.listIterator();
        while (it.hasNext()) {
            it.set(replaceTags(fplayer, it.next()));
        }
        return lines;
    }
}