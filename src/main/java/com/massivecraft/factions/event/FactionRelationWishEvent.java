package com.massivecraft.factions.event;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.IFaction;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.event.Cancellable;

public class FactionRelationWishEvent extends FactionPlayerEvent implements Cancellable {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    private final IFaction targetFaction;
    private final Relation currentRelation;
    private final Relation targetRelation;

    private boolean cancelled;

    public FactionRelationWishEvent(IFactionPlayer caller, IFaction sender, IFaction targetFaction, Relation currentRelation, Relation targetRelation) {
        super(sender, caller);

        this.targetFaction = targetFaction;
        this.currentRelation = currentRelation;
        this.targetRelation = targetRelation;
    }

    public IFaction getTargetFaction() {
        return targetFaction;
    }

    public Relation getCurrentRelation() {
        return currentRelation;
    }

    public Relation getTargetRelation() {
        return targetRelation;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
