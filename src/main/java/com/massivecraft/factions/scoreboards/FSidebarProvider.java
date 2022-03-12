package com.massivecraft.factions.scoreboards;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.mysql.Faction;
import com.massivecraft.factions.mysql.FactionPlayer;
import com.massivecraft.factions.tag.Tag;
import com.massivecraft.factions.zcore.util.TL;

import java.util.List;

public abstract class FSidebarProvider {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public abstract String getTitle(FactionPlayer _player);

    public abstract List<String> getLines(FactionPlayer _player);

    public String replaceTags(FactionPlayer _player, String _replace_with) {
        _replace_with = Tag.parsePlaceholders(_player.getPlayer(), _replace_with);

        return qualityAssure(Tag.parsePlain(_player, _replace_with));
    }

    public String replaceTags(Faction _faction, FactionPlayer _player, String _replace_with) {
        // Run through Placeholder API first
        _replace_with = Tag.parsePlaceholders(_player.getPlayer(), _replace_with);

        return qualityAssure(Tag.parsePlain(_faction, _player, _replace_with));
    }

    private String qualityAssure(String line) {
        if (line.contains("{notFrozen}") || line.contains("{notPermanent}")) {
            return "n/a"; // we dont support support these error variables in scoreboards
        }
        if (line.contains("{ig}")) {
            // since you can't really fit a whole "Faction Home: world, x, y, z" on one line
            // we assume it's broken up into two lines, so returning our tl will suffice.
            return TL.COMMAND_SHOW_NOHOME.toString();
        }
        return FactionsPlugin.getInstance().txt.parse(line); // finally add color :)
    }
}