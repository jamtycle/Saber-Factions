package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;

public class CmdCheckpoint extends FCommand {

    /**
     * @author Illyria Team
     */

    public CmdCheckpoint() {
        super();
        this.aliases.addAll(Aliases.checkpoint);

        this.optionalArgs.put("set", "");

        this.requirements = new CommandRequirements.Builder(Permission.CHECKPOINT).playerOnly().memberOnly().build();
    }

    @Override
    public void perform(CommandContext context) {
        if (!FactionsPlugin.getInstance().getConfig().getBoolean("checkpoints.Enabled")) {
            context.msg(TL.COMMAND_CHECKPOINT_DISABLED);
            return;
        }
        if (context.args.size() == 1 && context.args.get(0).equalsIgnoreCase("set")) {
            if (context.fPlayer.getRole() == Role.LEADER) {
                FLocation myLocation = new FLocation(context.player.getLocation());
                IFaction myLocFaction = Board.getInstance().getFactionAt(myLocation);
                if (myLocFaction == Factions.getInstance().getWilderness() || myLocFaction == context.faction) {
                    context.faction.setCheckpoint(context.player.getLocation());
                    context.msg(TL.COMMAND_CHECKPOINT_SET);
                    return;
                }
            } else {
                context.msg(TL.COMMAND_CHECKPOINT_INVALIDLOCATION);
                return;
            }
        }

        if (context.faction.getCheckpoint() == null) {
            context.msg(TL.COMMAND_CHECKPOINT_NOT_SET);
            return;
        }
        FLocation checkLocation = new FLocation(context.faction.getCheckpoint());
        IFaction checkfaction = Board.getInstance().getFactionAt(checkLocation);

        if (checkfaction.getId().equals(Factions.getInstance().getWilderness().getId()) || checkfaction.getId().equals(context.faction.getId())) {
            context.msg(TL.COMMAND_CHECKPOINT_GO);
        } else {
            context.msg(TL.COMMAND_CHECKPOINT_CLAIMED);
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_CHECKPOINT_DESCRIPTION;
    }
}
