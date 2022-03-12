package com.massivecraft.factions.event;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.struct.Role;
import org.bukkit.event.Cancellable;

/**
 * Represents {@link Role} change of a factions player
 *
 * @see IFactionPlayer#getRole()
 */
public class FPlayerRoleChangeEvent extends FactionPlayerEvent implements Cancellable {

    /**
     * @author Illyria Team
     */

    private final Role from;
    private boolean cancelled;
    private Role to;

    public FPlayerRoleChangeEvent(IFaction faction, IFactionPlayer fPlayer, Role from, Role to) {
        super(faction, fPlayer);
        this.from = from;
        this.to = to;
    }

    public FPlayerRoleChangeEvent(IFaction faction, IFactionPlayer fPlayer, Role to) {
        this(faction, fPlayer, fPlayer.getRole(), to);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Role getFrom() {
        return from;
    }

    public Role getTo() {
        return to;
    }

    public void setTo(Role to) {
        this.to = to;
    }

}
