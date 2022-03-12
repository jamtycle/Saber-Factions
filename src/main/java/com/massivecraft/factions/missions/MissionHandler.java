package com.massivecraft.factions.missions;

import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.FactionPlayersManagerBase;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.util.CC;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.List;
import java.util.stream.Collectors;

public class MissionHandler implements Listener {

    /**
     * @author Driftay
     */

    private FactionsPlugin plugin;

    public MissionHandler(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player)) {
            return;
        }
        IFactionPlayer fPlayer = FactionPlayersManagerBase.getInstance().getByPlayer((Player) event.getOwner());
        if (fPlayer == null) {
            return;
        }
        List<Mission> missions = fPlayer.getFaction().getMissions().values().stream().filter(mission -> mission.getType().equalsIgnoreCase("tame")).collect(Collectors.toList());
        for (Mission mission2 : missions) {
            ConfigurationSection section = plugin.getFileManager().getMissions().getConfig().getConfigurationSection("Missions").getConfigurationSection(mission2.getName());
            if (!event.getEntityType().toString().equals(section.getConfigurationSection("Mission").getString("EntityType")) && !section.getConfigurationSection("Mission").getString("EntityType").equalsIgnoreCase("ALL")) {
                continue;
            }
            mission2.incrementProgress();
            checkIfDone(fPlayer, mission2, section);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() == null || event.getEntity().getKiller() == null) {
            return;
        }
        IFactionPlayer fPlayer = FactionPlayersManagerBase.getInstance().getByPlayer(event.getEntity().getKiller());
        if (fPlayer == null) {
            return;
        }
        List<Mission> missions = fPlayer.getFaction().getMissions().values().stream().filter(mission -> mission.getType().equalsIgnoreCase("kill")).collect(Collectors.toList());
        for (Mission mission2 : missions) {
            ConfigurationSection section = plugin.getFileManager().getMissions().getConfig().getConfigurationSection("Missions").getConfigurationSection(mission2.getName());
            if (!event.getEntityType().toString().equals(section.getConfigurationSection("Mission").getString("EntityType"))) {
                continue;
            }
            mission2.incrementProgress();
            checkIfDone(fPlayer, mission2, section);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        IFactionPlayer fPlayer = FactionPlayersManagerBase.getInstance().getByPlayer(event.getPlayer());
        if (fPlayer == null) {
            return;
        }
        List<Mission> missions = fPlayer.getFaction().getMissions().values().stream().filter(mission -> mission.getType().equalsIgnoreCase("mine")).collect(Collectors.toList());
        for (Mission mission2 : missions) {
            ConfigurationSection section = plugin.getFileManager().getMissions().getConfig().getConfigurationSection("Missions").getConfigurationSection(mission2.getName());
            if (!event.getBlock().getType().toString().equals(section.getConfigurationSection("Mission").getString("Material"))) {
                continue;
            }
            mission2.incrementProgress();
            checkIfDone(fPlayer, mission2, section);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        IFactionPlayer fPlayer = FactionPlayersManagerBase.getInstance().getByPlayer(event.getPlayer());
        if (fPlayer == null) {
            return;
        }
        List<Mission> missions = fPlayer.getFaction().getMissions().values().stream().filter(mission -> mission.getType().equalsIgnoreCase("place")).collect(Collectors.toList());
        for (Mission mission2 : missions) {
            ConfigurationSection section = plugin.getFileManager().getMissions().getConfig().getConfigurationSection("Missions").getConfigurationSection(mission2.getName());
            if (!event.getBlock().getType().toString().equals(section.getConfigurationSection("Mission").getString("Material"))) {
                continue;
            }
            mission2.incrementProgress();
            checkIfDone(fPlayer, mission2, section);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        IFactionPlayer fPlayer = FactionPlayersManagerBase.getInstance().getByPlayer(event.getPlayer());
        if (fPlayer == null) {
            return;
        }
        List<Mission> missions = fPlayer.getFaction().getMissions().values().stream().filter(mission -> mission.getType().equalsIgnoreCase("fish")).collect(Collectors.toList());
        for (Mission mission2 : missions) {
            ConfigurationSection section = plugin.getFileManager().getMissions().getConfig().getConfigurationSection("Missions").getConfigurationSection(mission2.getName());
            mission2.incrementProgress();
            checkIfDone(fPlayer, mission2, section);
        }
    }

    @EventHandler
    public void onPlayerEnchant(EnchantItemEvent e) {
        IFactionPlayer fPlayer = FactionPlayersManagerBase.getInstance().getByPlayer(e.getEnchanter());
        if (fPlayer == null) {
            return;
        }
        List<Mission> missions = fPlayer.getFaction().getMissions().values().stream().filter(mission -> mission.getType().equalsIgnoreCase("enchant")).collect(Collectors.toList());
        for (Mission mission2 : missions) {
            ConfigurationSection section = plugin.getFileManager().getMissions().getConfig().getConfigurationSection("Missions").getConfigurationSection(mission2.getName());
            mission2.incrementProgress();
            checkIfDone(fPlayer, mission2, section);
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent e) {
        IFactionPlayer fPlayer = FactionPlayersManagerBase.getInstance().getByPlayer(e.getPlayer());
        if (fPlayer == null) {
            return;
        }
        List<Mission> missions = fPlayer.getFaction().getMissions().values().stream().filter(mission -> mission.getType().equalsIgnoreCase("consume")).collect(Collectors.toList());
        for (Mission mission2 : missions) {
            ConfigurationSection section = plugin.getFileManager().getMissions().getConfig().getConfigurationSection("Missions").getConfigurationSection(mission2.getName());

            if (!e.getItem().toString().contains(section.getConfigurationSection("Mission").getString("Item")) && !section.getConfigurationSection("Mission").getString("Item").equalsIgnoreCase("ALL")) {
                continue;
            }
            mission2.incrementProgress();
            checkIfDone(fPlayer, mission2, section);
        }
    }

    private void checkIfDone(IFactionPlayer fPlayer, Mission mission, ConfigurationSection section) {
        if (mission.getProgress() < section.getConfigurationSection("Mission").getLong("Amount")) {
            return;
        }
        for (String command : section.getConfigurationSection("Reward").getStringList("Commands")) {
            FactionsPlugin.getInstance().getServer().dispatchCommand(FactionsPlugin.getInstance().getServer().getConsoleSender(), command.replace("%faction%", fPlayer.getFaction().getTag()).replace("%player%", fPlayer.getPlayer().getName()));
        }
        fPlayer.getFaction().getMissions().remove(mission.getName());
        fPlayer.getFaction().msg(TL.MISSION_MISSION_FINISHED, CC.translate(section.getString("Name")));
        fPlayer.getFaction().getCompletedMissions().add(mission.getName());
    }
}
