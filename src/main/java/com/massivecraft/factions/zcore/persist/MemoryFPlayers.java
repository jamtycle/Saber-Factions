package com.massivecraft.factions.zcore.persist;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.FactionPlayersManagerBase;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class MemoryFPlayers extends FactionPlayersManagerBase {
    public Map<String, IFactionPlayer> fPlayers = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

    public void clean() {
        for (IFactionPlayer fplayer : this.fPlayers.values()) {
            if (!Factions.getInstance().isValidFactionId(fplayer.getFactionId())) {
                Logger.print("Reset faction data (invalid faction:" + fplayer.getFactionId() + ") for player " + fplayer.getName(), Logger.PrefixType.DEFAULT);
                fplayer.resetFactionData(false);
            }
        }
    }

    public Collection<IFactionPlayer> getOnlinePlayers() {
        Set<IFactionPlayer> entities = new HashSet<>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            entities.add(this.getByPlayer(player));
        }
        return entities;
    }

    @Override
    public IFactionPlayer getByPlayer(Player player) {
        return getById(player.getUniqueId().toString());
    }

    @Override
    public List<IFactionPlayer> getAllFPlayers() {
        return new ArrayList<>(fPlayers.values());
    }

    @Override
    public abstract void forceSave();

    public abstract void load();

    @Override
    public IFactionPlayer getByOfflinePlayer(OfflinePlayer player) {
        return getById(player.getUniqueId().toString());
    }

    @Override
    public IFactionPlayer getById(String id) {
        IFactionPlayer player = fPlayers.get(id);
        if (player == null) {
            player = generateFPlayer(id);
        }
        return player;
    }

    public abstract IFactionPlayer generateFPlayer(String id);

    public abstract void convertFrom(MemoryFPlayers old);
}
