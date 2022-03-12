package com.massivecraft.factions.cmd;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class CmdPower extends FCommand {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public CmdPower() {
        super();
        this.aliases.addAll(Aliases.power_power);
        this.optionalArgs.put("player name", "you");

        this.requirements = new CommandRequirements.Builder(Permission.POWER)
                .build();

    }

    @Override
    public void perform(CommandContext context) {
        IFactionPlayer target = context.argAsBestFPlayerMatch(0, context.fPlayer);
        if (target == null) {
            return;
        }

        if (target != context.fPlayer && !Permission.POWER_ANY.has(context.sender, true)) {
            return;
        }

        double powerBoost = target.getPowerBoost();
        String boost = (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? TL.COMMAND_POWER_BONUS.toString() : TL.COMMAND_POWER_PENALTY.toString()) + powerBoost + ")";
        context.msg(TL.COMMAND_POWER_POWER, target.describeTo(context.fPlayer, true), target.getPowerRounded(), target.getPowerMaxRounded(), boost);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_POWER_DESCRIPTION;
    }

}
