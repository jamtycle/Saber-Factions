package com.massivecraft.factions.iface;

import com.massivecraft.factions.struct.Relation;
import org.bukkit.ChatColor;
import com.massivecraft.factions.zcore.util.TL;

/* *
Bruno Ramirez:

Same as the IFactionPlayer (ex FPlayer) This interface is not really necessary in this scenario.
We can use the class itself, and it will have the same behaviour.

If this interface is used on Factions maybe useful, otherwise, it is a waste of time and disk space.
* */

public interface IRelationParticipator {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    String describeTo(IRelationParticipator that);

    String describeTo(IRelationParticipator that, boolean ucfirst);

    Relation getRelationTo(IRelationParticipator that);

    Relation getRelationTo(IRelationParticipator that, boolean ignorePeaceful);

    ChatColor getColorTo(IRelationParticipator to);

//    String getAccountId();

    void msg(String str, Object... args);

    void msg(TL translation, Object... args);
}
