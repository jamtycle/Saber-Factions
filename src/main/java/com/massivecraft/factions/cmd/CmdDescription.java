package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.FactionPlayersManagerBase;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import com.massivecraft.factions.zcore.util.TextUtil;

public class CmdDescription extends FCommand {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public CmdDescription() {
        super();
        this.aliases.addAll(Aliases.description);

        this.requiredArgs.add("desc");

        this.requirements = new CommandRequirements.Builder(Permission.DESCRIPTION)
                .playerOnly()
                .memberOnly()
                .noErrorOnManyArgs()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> {

            // since "&" color tags seem to work even through plain old FPlayer.sendMessage() for some reason, we need to break those up
            // And replace all the % because it messes with string formatting and this is easy way around that.
            String desc = TextUtil.implode(context.args, " ").replaceAll("%", "").replaceAll("(&([a-f0-9klmnor]))", "& $2");
            context.faction.setDescription(desc);
            if (!Conf.broadcastDescriptionChanges) {
                context.msg(TL.COMMAND_DESCRIPTION_CHANGED, context.faction.describeTo(context.fPlayer));
                context.sendMessage(context.faction.getDescription());
                return;
            }

            // Broadcast the description to everyone
            for (IFactionPlayer fplayer : FactionPlayersManagerBase.getInstance().getOnlinePlayers()) {
                fplayer.msg(TL.COMMAND_DESCRIPTION_CHANGES, context.faction.describeTo(fplayer));
                fplayer.sendMessage(context.faction.getDescription());  // players can inject "&" or "`" or "<i>" or whatever in their description; &k is particularly interesting looking
            }
        });
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_DESCRIPTION_DESCRIPTION;
    }

}