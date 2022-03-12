package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.FactionPlayersManagerBase;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;

public class CmdShowInvites extends FCommand {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public CmdShowInvites() {
        super();
        aliases.addAll(Aliases.show_invites);

        this.requirements = new CommandRequirements.Builder(Permission.SHOW_INVITES)
                .playerOnly()
                .memberOnly()
                .build();

    }

    @Override
    public void perform(CommandContext context) {
        FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> {


            FancyMessage msg = new FancyMessage(TL.COMMAND_SHOWINVITES_PENDING.toString()).color(ChatColor.GOLD);
            for (String id : context.faction.getInvites()) {
                IFactionPlayer fp = FactionPlayersManagerBase.getInstance().getById(id);
                String name = fp != null ? fp.getName() : id;
                msg.then(name + " ").color(ChatColor.WHITE).tooltip(TL.COMMAND_SHOWINVITES_CLICKTOREVOKE.format(name)).command("/" + Conf.baseCommandAliases.get(0) + " deinvite " + name);
            }
            context.sendFancyMessage(msg);
        });
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SHOWINVITES_DESCRIPTION;
    }


}

