package com.massivecraft.factions.zcore.frame.fupgrades;

import com.cryptomorin.xseries.XMaterial;
import com.massivecraft.factions.*;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.material.Crops;

import java.util.concurrent.ThreadLocalRandom;

public class UpgradesListener implements Listener {

    /**
     * @author Illyria Team
     */

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer == null) return;

        // TODO: Announce death.

//        FLocation floc = new FLocation(e.getEntity().getLocation());
//        Faction faction = Board.getInstance().getFactionAt(floc);
//        if (!faction.isWilderness()) {
//
//        }
    }

    @EventHandler
    public void onCropGrow(BlockGrowEvent e) {
        FLocation floc = new FLocation(e.getBlock().getLocation());
        IFaction factionAtLoc = Board.getInstance().getFactionAt(floc);
        if (!factionAtLoc.isWilderness()) {
            int level = factionAtLoc.getUpgrade(UpgradeType.CROP);
            int chance = FactionsPlugin.getInstance().getFileManager().getUpgrades().getConfig().getInt("fupgrades.MainMenu.Crops.Crop-Boost.level-" + level);
            if (level == 0 || chance == 0) return;

            int randomNum = ThreadLocalRandom.current().nextInt(1, 101);
            if (randomNum <= chance) this.growCrop(e);
        }
    }

    private void growCrop(BlockGrowEvent e) {
        if (e.getBlock().getType().equals(XMaterial.WHEAT.parseMaterial())) {
            e.setCancelled(true);
            Crops c = new Crops(CropState.RIPE);
            BlockState bs = e.getBlock().getState();
            bs.setData(c);
            bs.update();
        }
        Block below = e.getBlock().getLocation().subtract(0.0, 1.0, 0.0).getBlock();
        if (below.getType() == XMaterial.SUGAR_CANE.parseMaterial()) {
            Block above = e.getBlock().getLocation().add(0.0, 1.0, 0.0).getBlock();
            if (above.getType() == Material.AIR && above.getLocation().add(0.0, -2.0, 0.0).getBlock().getType() != Material.AIR) {
                above.setType(XMaterial.SUGAR_CANE.parseMaterial());
            }
        } else if (below.getType() == Material.CACTUS) {
            Block above = e.getBlock().getLocation().add(0.0, 1.0, 0.0).getBlock();
            if (above.getType() == Material.AIR && above.getLocation().add(0.0, -2.0, 0.0).getBlock().getType() != Material.AIR) {
                above.setType(Material.CACTUS);
            }
        }
    }
}
