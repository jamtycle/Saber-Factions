package com.massivecraft.factions.cmd;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;
import com.massivecraft.factions.zcore.util.TextUtil;

public class CmdTitle extends FCommand {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public CmdTitle() {
        this.aliases.addAll(Aliases.title);
        this.requiredArgs.add("player name");
        this.optionalArgs.put("title", "");

        this.requirements = new CommandRequirements.Builder(Permission.TITLE)
                .memberOnly()
                .withRole(Role.MODERATOR)
                .playerOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        IFactionPlayer you = context.argAsBestFPlayerMatch(0);
        if (you == null) {
            return;
        }

        context.args.remove(0);
        String title = TextUtil.implode(context.args, " ");

        title = title.replaceAll(",", "");

        if (!context.canIAdministerYou(context.fPlayer, you)) {
            return;
        }

        you.setTitle(context.sender, title);

        // Inform
        context.faction.msg(TL.COMMAND_TITLE_CHANGED, context.fPlayer.describeTo(context.faction, true), you.describeTo(context.faction, true));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TITLE_DESCRIPTION;
    }

}