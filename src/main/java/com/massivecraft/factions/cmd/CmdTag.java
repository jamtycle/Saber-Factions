package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.discord.Discord;
import com.massivecraft.factions.event.FactionRenameEvent;
import com.massivecraft.factions.scoreboards.FTeamWrapper;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.Cooldown;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class CmdTag extends FCommand {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public CmdTag() {
        this.aliases.addAll(Aliases.tag);

        this.requiredArgs.add("faction tag");

        this.requirements = new CommandRequirements.Builder(Permission.TAG)
                .withRole(Role.COLEADER)
                .playerOnly()
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {

        FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> {


            String tag = context.argAsString(0);

            // TODO does not first test cover selfcase?
            if (Factions.getInstance().isTagTaken(tag) && !MiscUtil.getComparisonString(tag).equals(context.faction.getComparisonTag())) {
                context.msg(TL.COMMAND_TAG_TAKEN);
                return;
            }

            ArrayList<String> errors = MiscUtil.validateTag(tag);
            if (errors.size() > 0) {
                context.sendMessage(errors);
                return;
            }

            if (Cooldown.isOnCooldown(context.player, "tagCooldown") && !context.fPlayer.isAdminBypassing()) {
                context.msg(TL.COMMAND_COOLDOWN);
                return;
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(FactionsPlugin.getInstance(), () -> {
                // trigger the faction rename event (cancellable)
                FactionRenameEvent renameEvent = new FactionRenameEvent(context.fPlayer, tag);
                Bukkit.getServer().getPluginManager().callEvent(renameEvent);
                if (renameEvent.isCancelled()) {
                    return;
                }

                String oldtag = context.faction.getTag();
                context.faction.setTag(tag);

                Discord.changeFactionTag(context.faction, oldtag);


                // Inform
                for (IFactionPlayer fplayer : FactionPlayersManagerBase.getInstance().getOnlinePlayers()) {
                    if (fplayer.getFactionId().equals(context.faction.getId())) {
                        fplayer.msg(TL.COMMAND_TAG_FACTION, context.fPlayer.describeTo(context.faction, true), context.faction.getTag(context.faction));
                        Cooldown.setCooldown(fplayer.getPlayer(), "tagCooldown", FactionsPlugin.getInstance().getConfig().getInt("fcooldowns.f-tag"));
                        continue;
                    }
                    // Broadcast the tag change (if applicable)
                    if (Conf.broadcastTagChanges) {
                        IFaction faction = fplayer.getFaction();
                        fplayer.msg(TL.COMMAND_TAG_CHANGED, context.fPlayer.getColorTo(faction) + oldtag, context.faction.getTag(faction));
                    }
                }
                FTeamWrapper.updatePrefixes(context.faction);
            });
        });
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TAG_DESCRIPTION;
    }

}