package com.massivecraft.factions.cmd;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.FactionPlayersManagerBase;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;

public class CmdAdmin extends FCommand {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public CmdAdmin() {
        super();
        this.aliases.addAll(Aliases.admin);

        this.requiredArgs.add("player");

        this.requirements = new CommandRequirements.Builder(Permission.ADMIN).build();
    }

    @Override
    public void perform(CommandContext context) {
        if (context.player == null) {
            context.msg(TL.GENERIC_PLAYERONLY);
            return;
        }
        // Allows admins bypass this.
        if (!context.fPlayer.isAdminBypassing() && !context.fPlayer.getRole().equals(Role.LEADER)) {
            context.msg(TL.COMMAND_ADMIN_NOTADMIN);
            return;
        }
        IFactionPlayer fyou = context.argAsBestFPlayerMatch(0);
        if (fyou == null) {
            return;
        }

        boolean permAny = Permission.ADMIN_ANY.has(context.sender, false);
        IFaction targetFaction = fyou.getFaction();

        if (targetFaction != context.faction && !permAny) {
            context.msg(TL.COMMAND_ADMIN_NOTMEMBER, fyou.describeTo(context.fPlayer, true));
            return;
        }

        if (fyou == context.fPlayer && !permAny) {
            context.msg(TL.COMMAND_ADMIN_TARGETSELF);
            return;
        }

        if (fyou.isAlt()) {
            return;
        }

        // only perform a FPlayerJoinEvent when newLeader isn't actually in the faction
        if (fyou.getFaction() != targetFaction) {
            FPlayerJoinEvent event = new FPlayerJoinEvent(FactionPlayersManagerBase.getInstance().getByPlayer(context.player), targetFaction, FPlayerJoinEvent.PlayerJoinReason.LEADER);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }

        IFactionPlayer admin = targetFaction.getFPlayerAdmin();

        // if target player is currently admin, demote and replace him
        if (fyou == admin) {
            promoteNewLeader(targetFaction);
            context.msg(TL.COMMAND_ADMIN_DEMOTES, fyou.describeTo(context.fPlayer, true));
            fyou.msg(TL.COMMAND_ADMIN_DEMOTED, context.player == null ? TL.GENERIC_SERVERADMIN.toString() : context.fPlayer.describeTo(fyou, true));
            return;
        }

        // promote target player, and demote existing admin if one exists
        if (admin != null) {
            setRole(admin, Role.COLEADER);
        }
        setRole(fyou, Role.LEADER);
        context.msg(TL.COMMAND_ADMIN_PROMOTES, fyou.describeTo(context.fPlayer, true));

        // Inform all players
        if (FactionsPlugin.instance.getConfig().getBoolean("faction-leader-broadcast")) {
            for (IFactionPlayer fplayer : FactionPlayersManagerBase.getInstance().getOnlinePlayers()) {
                fplayer.msg(TL.COMMAND_ADMIN_PROMOTED, context.player == null ? TL.GENERIC_SERVERADMIN.toString() : context.fPlayer.describeTo(fplayer, true), fyou.describeTo(fplayer), targetFaction.describeTo(fplayer));
            }
        }
        
    }

    private void setRole(IFactionPlayer fp, Role r) {
        FactionsPlugin.getInstance().getServer().getScheduler().runTask(FactionsPlugin.instance, () -> fp.setRole(r));
    }

    private void promoteNewLeader(IFaction f) {
        FactionsPlugin.getInstance().getServer().getScheduler().runTask(FactionsPlugin.instance, (Runnable) f::promoteNewLeader);
    }

    public TL getUsageTranslation() {
        return TL.COMMAND_ADMIN_DESCRIPTION;
    }

}