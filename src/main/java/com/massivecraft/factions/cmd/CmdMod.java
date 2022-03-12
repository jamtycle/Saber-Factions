package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;

public class CmdMod extends FCommand {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public CmdMod() {
        super();
        this.aliases.addAll(Aliases.mod);

        this.optionalArgs.put("player name", "name");

        this.requirements = new CommandRequirements.Builder(Permission.MOD)
                .playerOnly()
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        IFactionPlayer you = context.argAsBestFPlayerMatch(0);
        if (you == null) {
            FancyMessage msg = new FancyMessage(TL.COMMAND_MOD_CANDIDATES.toString()).color(ChatColor.GOLD);
            for (IFactionPlayer player : context.faction.getFPlayersWhereRole(Role.NORMAL)) {
                String s = player.getName();
                msg.then(s + " ").color(ChatColor.WHITE).tooltip(TL.COMMAND_MOD_CLICKTOPROMOTE + s).command("/" + Conf.baseCommandAliases.get(0) + " mod " + s);
            }

            context.sendFancyMessage(msg);
            return;
        }

        boolean permAny = Permission.MOD_ANY.has(context.sender, false);
        IFaction targetFaction = you.getFaction();
        if (targetFaction != context.faction && !permAny) {
            context.msg(TL.COMMAND_MOD_NOTMEMBER, you.describeTo(context.fPlayer, true));
            return;
        }

        if (you.isAlt()) {
            return;
        }

        if (context.fPlayer != null && !context.fPlayer.getRole().isAtLeast(Role.COLEADER) && !permAny) {
            context.msg(TL.COMMAND_MOD_NOTADMIN);
            return;
        }

        if (you == context.fPlayer && !permAny) {
            context.msg(TL.COMMAND_MOD_SELF);
            return;
        }

        if (you.getRole() == Role.LEADER) {
            context.msg(TL.COMMAND_MOD_TARGETISADMIN);
            return;
        }

        if (you.getRole() == Role.MODERATOR) {
            // Revoke
            setRole(you, Role.NORMAL);
            targetFaction.msg(TL.COMMAND_MOD_REVOKED, you.describeTo(targetFaction, true));
            context.msg(TL.COMMAND_MOD_REVOKES, you.describeTo(context.fPlayer, true));
        } else {
            // Give
            setRole(you, Role.MODERATOR);
            targetFaction.msg(TL.COMMAND_MOD_PROMOTED, you.describeTo(targetFaction, true));
            context.msg(TL.COMMAND_MOD_PROMOTES, you.describeTo(context.fPlayer, true));

        }
    }

    private void setRole(IFactionPlayer fp, Role r) {
        FactionsPlugin.getInstance().getServer().getScheduler().runTask(FactionsPlugin.instance, () -> fp.setRole(r));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MOD_DESCRIPTION;
    }

}
