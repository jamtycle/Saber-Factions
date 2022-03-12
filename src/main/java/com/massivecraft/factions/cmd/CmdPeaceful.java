package com.massivecraft.factions.cmd;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.FactionPlayersManagerBase;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class CmdPeaceful extends FCommand {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public CmdPeaceful() {
        super();
        this.aliases.addAll(Aliases.peaceful);
        this.requiredArgs.add("faction tag");

        this.requirements = new CommandRequirements.Builder(Permission.SET_PEACEFUL)
                .build();

    }

    @Override
    public void perform(CommandContext context) {
        IFaction faction = context.argAsFaction(0);
        if (faction == null) {
            return;
        }

        String change;
        if (faction.isPeaceful()) {
            change = TL.COMMAND_PEACEFUL_REVOKE.toString();
            faction.setPeaceful(false);
        } else {
            change = TL.COMMAND_PEACEFUL_GRANT.toString();
            faction.setPeaceful(true);
        }

        // Inform all players
        for (IFactionPlayer fplayer : FactionPlayersManagerBase.getInstance().getOnlinePlayers()) {
            String blame = (context.fPlayer == null ? TL.GENERIC_SERVERADMIN.toString() : context.fPlayer.describeTo(fplayer, true));
            if (fplayer.getFaction() == faction) {
                fplayer.msg(TL.COMMAND_PEACEFUL_YOURS, blame, change);
            } else {
                fplayer.msg(TL.COMMAND_PEACEFUL_OTHER, blame, change, faction.getTag(fplayer));
            }
        }

    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_PEACEFUL_DESCRIPTION;
    }

}
