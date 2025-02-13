package com.massivecraft.factions.cmd;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.FactionPlayersManagerBase;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.Cooldown;
import com.massivecraft.factions.zcore.util.TL;

public class CmdOpen extends FCommand {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public CmdOpen() {
        super();
        this.aliases.addAll(Aliases.open);
        this.optionalArgs.put("yes/no", "flip");

        this.requirements = new CommandRequirements.Builder(Permission.OPEN)
                .withRole(Role.COLEADER)
                .playerOnly()
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {

        if (Cooldown.isOnCooldown(context.fPlayer.getPlayer(), "openCooldown") && !context.fPlayer.isAdminBypassing()) {
            context.msg(TL.COMMAND_COOLDOWN);
            return;
        }

        context.faction.setOpen(!context.faction.getOpen());

        String open = context.faction.getOpen() ? TL.COMMAND_OPEN_OPEN.toString() : TL.COMMAND_OPEN_CLOSED.toString();

        // Inform
        for (IFactionPlayer fplayer : FactionPlayersManagerBase.getInstance().getOnlinePlayers()) {
            if (fplayer.getFactionId().equals(context.faction.getId())) {
                fplayer.msg(TL.COMMAND_OPEN_CHANGES, context.fPlayer.getName(), open);
                Cooldown.setCooldown(fplayer.getPlayer(), "openCooldown", FactionsPlugin.getInstance().getConfig().getInt("fcooldowns.f-open"));
                continue;
            }
            if (!FactionsPlugin.getInstance().getConfig().getBoolean("faction-open-broadcast")) return;
            fplayer.msg(TL.COMMAND_OPEN_CHANGED, context.faction.getTag(fplayer.getFaction()), open);
        }
        if (!FactionsPlugin.getInstance().getConfig().getBoolean("faction-open-broadcast")) {
            for (IFactionPlayer fPlayer : context.faction.getFPlayersWhereOnline(true)) {
                fPlayer.msg(TL.COMMAND_OPEN_CHANGED, context.faction.getTag(fPlayer.getFaction()), open);
            }
        }

    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_OPEN_DESCRIPTION;
    }

}
