package com.massivecraft.factions.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.massivecraft.factions.*;
import com.massivecraft.factions.cmd.CmdFGlobal;
import com.massivecraft.factions.cmd.CmdSeeChunk;
import com.massivecraft.factions.cmd.logout.LogoutHandler;
import com.massivecraft.factions.discord.Discord;
import com.massivecraft.factions.event.FPlayerEnteredFactionEvent;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.integration.LunarAPI;
import com.massivecraft.factions.scoreboards.FScoreboard;
import com.massivecraft.factions.scoreboards.FTeamWrapper;
import com.massivecraft.factions.scoreboards.sidebar.FDefaultSidebar;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.*;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.massivecraft.factions.zcore.frame.FactionGUI;
import com.massivecraft.factions.zcore.persist.MemoryFPlayer;
import com.massivecraft.factions.zcore.util.TL;
import com.massivecraft.factions.zcore.util.TextUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;


public class FactionsPlayerListener implements Listener {

    public static Set<FLocation> corners;
    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    HashMap<Player, Boolean> fallMap = new HashMap<>();
    // Holds the next time a player can have a map shown.
    private HashMap<UUID, Long> showTimes = new HashMap<>();

    public FactionsPlayerListener() {
        for (Player player : FactionsPlugin.getInstance().getServer().getOnlinePlayers()) initPlayer(player);
        if (FactionsPlugin.getInstance().version != 7) loadCorners();
    }

    public static void loadCorners() {
        FactionsPlayerListener.corners = new HashSet<>();
        for (World world : FactionsPlugin.getInstance().getServer().getWorlds()) {
            WorldBorder border = world.getWorldBorder();
            if (border != null) {
                int cornerCoord = (int) ((border.getSize() - 1.0) / 2.0);
                FactionsPlayerListener.corners.add(new FLocation(world.getName(), FLocation.blockToChunk(cornerCoord), FLocation.blockToChunk(cornerCoord)));
                FactionsPlayerListener.corners.add(new FLocation(world.getName(), FLocation.blockToChunk(cornerCoord), FLocation.blockToChunk(-cornerCoord)));
                FactionsPlayerListener.corners.add(new FLocation(world.getName(), FLocation.blockToChunk(-cornerCoord), FLocation.blockToChunk(cornerCoord)));
                FactionsPlayerListener.corners.add(new FLocation(world.getName(), FLocation.blockToChunk(-cornerCoord), FLocation.blockToChunk(-cornerCoord)));
            }
        }
    }

    public static Boolean isSystemFaction(IFaction faction) {
        return faction.isSafeZone() ||
                faction.isWarZone() ||
                faction.isWilderness();
    }

    public static boolean playerCanUseItemHere(Player player, Location location, Material material, boolean justCheck, PermissableAction permissableAction) {
        String name = player.getName();
        if (Conf.playersWhoBypassAllProtection.contains(name)) {
            return true;
        }

        IFactionPlayer me = FactionPlayersManagerBase.getInstance().getByPlayer(player);

        FLocation loc = new FLocation(location);
        IFaction otherFaction = Board.getInstance().getFactionAt(loc);
        IFaction myFaction = me.getFaction();
        Relation rel = myFaction.getRelationTo(otherFaction);

        // Also cancel if player doesn't have ownership rights for this claim
        if (Conf.ownedAreasEnabled && myFaction == otherFaction && !myFaction.playerHasOwnershipRights(me, loc)) {
            if (!justCheck) {
                me.msg(TL.ACTIONS_OWNEDTERRITORYDENY.toString().replace("{owners}", myFaction.getOwnerListString(loc)));
            }
            return false;
        }

        //if (me.getFaction() == otherFaction) return true;

        if (FactionsPlugin.getInstance().getConfig().getBoolean("hcf.raidable", false) && otherFaction.getLandRounded() > otherFaction.getPowerRounded()) {
            return true;
        }

        if (otherFaction.hasPlayersOnline()) {
            if (!Conf.territoryDenyUsageMaterials.contains(material)) {
                return true; // Item isn't one we're preventing for online factions.
            }
        } else {
            if (!Conf.territoryDenyUsageMaterialsWhenOffline.contains(material)) {
                return true; // Item isn't one we're preventing for offline factions.
            }
        }

        if (otherFaction.isWilderness()) {
            if (!Conf.wildernessDenyUsage || ((Conf.worldsNoWildernessProtection.contains(location.getWorld().getName()) && !Conf.useWorldConfigurationsAsWhitelist) || (!Conf.worldsNoWildernessProtection.contains(location.getWorld().getName()) && Conf.useWorldConfigurationsAsWhitelist))) {
                return true; // This is not faction territory. Use whatever you like here.
            }

            if (!justCheck) {
                me.msg(TL.PLAYER_USE_WILDERNESS, TextUtil.getMaterialName(material));
            }

            return false;
        }

        // Cancel if we are not in our own territory
        if (rel.confDenyUseage()) {
            if (!justCheck) {
                me.msg(TL.PLAYER_USE_TERRITORY, TextUtil.getMaterialName(material), otherFaction.getTag(myFaction));
            }
            return false;
        }

        Access access = otherFaction.getAccess(me, permissableAction);
        return CheckPlayerAccess(player, me, loc, otherFaction, access, permissableAction);
    }

    public static boolean canPlayerUseBlock(Player player, Block block, boolean justCheck) {
        if (Conf.playersWhoBypassAllProtection.contains(player.getName()))
            return true;

        IFactionPlayer me = FactionPlayersManagerBase.getInstance().getByPlayer(player);
        Material material = block.getType();

        // Dupe fix.
        FLocation loc = new FLocation(block);
        IFaction otherFaction = Board.getInstance().getFactionAt(loc);
        IFaction myFaction = me.getFaction();

        // no door/chest/whatever protection in wilderness, war zones, or safe zones
        if (otherFaction.isSystemFaction()) return true;
        if (myFaction.isWilderness()) {
            if (block.getType().name().contains("PLATE")) {
                if (!Cooldown.isOnCooldown(player, "plateMessage")) {
                    Cooldown.setCooldown(player, "plateMessage", 3);
                } else {
                    return false;
                }
            }

            me.msg(TL.GENERIC_ACTION_NOPERMISSION, block.getType().toString().replace("_", " "));
            return false;
        }

        if (FactionsPlugin.getInstance().getConfig().getBoolean("hcf.raidable", false) && otherFaction.getLandRounded() > otherFaction.getPowerRounded())
            return true;

        if (otherFaction.getId().equals(myFaction.getId()) && me.getRole() == Role.LEADER) return true;
        PermissableAction action = GetPermissionFromUsableBlock(block);
        if (action == null) return false;
        // We only care about some material types.
        /// Who was the idiot?
        if (otherFaction.hasPlayersOnline()) {
            if (Conf.territoryProtectedMaterials.contains(material)) {
                return false;
            }
        } else {
            if (Conf.territoryProtectedMaterialsWhenOffline.contains(material)) {
                return false;
            }
        }

        // Move up access check to check for exceptions
        if (!otherFaction.getId().equals(myFaction.getId())) { // If the faction target is not my own
            return CheckPlayerAccess(player, me, loc, otherFaction, otherFaction.getAccess(me, action), action);
        } else if (otherFaction.getId().equals(myFaction.getId())) {
            return CheckPlayerAccess(player, me, loc, myFaction, myFaction.getAccess(me, action), action);
        }
        return CheckPlayerAccess(player, me, loc, myFaction, otherFaction.getAccess(me, action), action);
    }

    /* *
    This method is in serious observation, I don't know what it does, but I don't like it.
    * */
    public static boolean preventCommand(String fullCmd, Player player) {
        if ((Conf.territoryNeutralDenyCommands.isEmpty() && Conf.territoryEnemyDenyCommands.isEmpty() && Conf.permanentFactionMemberDenyCommands.isEmpty() && Conf.warzoneDenyCommands.isEmpty())) {
            return false;
        }

        fullCmd = fullCmd.toLowerCase();

        IFactionPlayer me = FactionPlayersManagerBase.getInstance().getByPlayer(player);

        String shortCmd;  // command without the slash at the beginning
        if (fullCmd.startsWith("/")) {
            shortCmd = fullCmd.substring(1);
        } else {
            shortCmd = fullCmd;
            fullCmd = "/" + fullCmd;
        }

        if (me.getHasFaction() &&
                !Conf.permanentFactionMemberDenyCommands.isEmpty() &&
                me.getFaction().isPermanent() &&
                isCommandInList(fullCmd, shortCmd, Conf.permanentFactionMemberDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_PERMANENT, fullCmd);
            return true;
        }

        IFaction at = Board.getInstance().getFactionAt(new FLocation(player.getLocation()));
        if (at.isWilderness() && !Conf.wildernessDenyCommands.isEmpty() && isCommandInList(fullCmd, shortCmd, Conf.wildernessDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_WILDERNESS, fullCmd);
            return true;
        }

        Relation rel = at.getRelationTo(me);
        if (at.isNormal() && rel.isAlly() && !Conf.territoryAllyDenyCommands.isEmpty() && isCommandInList(fullCmd, shortCmd, Conf.territoryAllyDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_ALLY, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isNeutral() && !Conf.territoryNeutralDenyCommands.isEmpty() && isCommandInList(fullCmd, shortCmd, Conf.territoryNeutralDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_NEUTRAL, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isEnemy() && !Conf.territoryEnemyDenyCommands.isEmpty() && isCommandInList(fullCmd, shortCmd, Conf.territoryEnemyDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_ENEMY, fullCmd);
            return true;
        }

        if (at.isWarZone() && !Conf.warzoneDenyCommands.isEmpty()  && isCommandInList(fullCmd, shortCmd, Conf.warzoneDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_WARZONE, fullCmd);
            return true;
        }

        return false;
    }

    private static boolean isCommandInList(String fullCmd, String shortCmd, Iterator<String> iter) {
        String cmdCheck;
        while (iter.hasNext()) {
            cmdCheck = iter.next();
            if (cmdCheck == null) {
                iter.remove();
                continue;
            }

            cmdCheck = cmdCheck.toLowerCase();
            if (fullCmd.startsWith(cmdCheck) || shortCmd.startsWith(cmdCheck)) {
                return true;
            }
        }
        return false;
    }

    private static boolean CheckPlayerAccess(Player player, IFactionPlayer me, FLocation loc, IFaction factionToCheck, Access access, PermissableAction action) {
        if (access != null) {
            boolean landOwned = (factionToCheck.doesLocationHaveOwnersSet(loc) && !factionToCheck.getOwnerList(loc).isEmpty());
            if ((landOwned && factionToCheck.getOwnerListString(loc).contains(player.getName())) || (me.getRole() == Role.LEADER && me.getFactionId().equals(factionToCheck.getId()))) {
                return true;
            } else if (landOwned && !factionToCheck.getOwnerListString(loc).contains(player.getName())) {
                me.msg(TL.ACTIONS_OWNEDTERRITORYDENY.toString().replace("{owners}", factionToCheck.getOwnerListString(loc)));
                return false;
            } else if (!landOwned && access == Access.ALLOW) {
                return true;
            } else {
                me.msg(TL.PLAYER_USE_TERRITORY, action, factionToCheck.getTag(me.getFaction()));
                return false;
            }
        }

        // Approves any permission check if the player in question is a leader AND owns the faction.
        if (me.getRole().equals(Role.LEADER) && me.getFaction().equals(factionToCheck)) return true;
        if (factionToCheck != null) {
            me.msg(TL.PLAYER_USE_TERRITORY, action, factionToCheck.getTag(me.getFaction()));
        }
        return false;
    }

    /// <summary>
    /// This will try to resolve a permission action based on the item material, if it's not usable, will return null
    /// </summary>
    private static PermissableAction GetPermissionFromUsableBlock(Block block) {
        return GetPermissionFromUsableBlock(block.getType());
    }

    private static PermissableAction GetPermissionFromUsableBlock(Material material) {
        if (material.name().contains("_BUTTON")
                || material.name().contains("COMPARATOR")
                || material.name().contains("PRESSURE")
                || material.name().contains("REPEATER")
                || material.name().contains("DIODE")) return PermissableAction.BUTTON;
        if (material.name().contains("_DOOR")
                || material.name().contains("_TRAPDOOR")
                || material.name().contains("_FENCE_GATE")
                || material.name().startsWith("FENCE_GATE")) return PermissableAction.DOOR;
        if (material.name().contains("SHULKER_BOX")
                || material.name().equals("FLOWER_POT")
                || material.name().startsWith("POTTED_")
                || material.name().endsWith("ANVIL")
                || material.name().startsWith("CHEST_MINECART")
                || material.name().endsWith("CHEST")
                || material.name().endsWith("JUKEBOX")
                || material.name().endsWith("CAULDRON")
                || material.name().endsWith("FURNACE")
                || material.name().endsWith("HOPPER")
                || material.name().endsWith("BEACON")
                || material.name().startsWith("TRAPPED_CHEST")
                || material.name().equalsIgnoreCase("ENCHANTING_TABLE")
                || material.name().equalsIgnoreCase("ENCHANTMENT_TABLE")
                || material.name().endsWith("BREWING_STAND")
                || material.name().equalsIgnoreCase("BARREL")) return PermissableAction.CONTAINER;
        if (material.name().endsWith("LEVER")) return PermissableAction.LEVER;
        switch (material) {
            case DISPENSER:
            case DROPPER:
                return PermissableAction.CONTAINER;
            default:
                return null;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        initPlayer(event.getPlayer());
    }

    private void initPlayer(Player player) {
        // Make sure that all online players do have a fplayer.
        IFactionPlayer me = FactionPlayersManagerBase.getInstance().getByPlayer(player);
        ((MemoryFPlayer) me).setName(player.getName());

        // Update the lastLoginTime for this fplayer
        me.setLastLoginTime(System.currentTimeMillis());

        // Store player's current FLocation and notify them where they are
        me.setLastStoodAt(new FLocation(player.getLocation()));

        me.login(); // set kills / deaths

        Bukkit.getScheduler().runTaskLater(FactionsPlugin.instance, () -> {
            if (me.isOnline()) me.getFaction().sendUnreadAnnouncements(me);
        }, 33L);

        if (FactionsPlugin.instance.getConfig().getBoolean("scoreboard.default-enabled", false)) {
            FScoreboard.init(me);
            FScoreboard.get(me).setDefaultSidebar(new FDefaultSidebar());
            FScoreboard.get(me).setSidebarVisibility(me.showScoreboard());
        }

        IFaction myFaction = me.getFaction();
        if (!myFaction.isWilderness()) {
            for (IFactionPlayer other : myFaction.getFPlayersWhereOnline(true)) {
                if (other != me && other.isMonitoringJoins()) other.msg(TL.FACTION_LOGIN, me.getName());
            }
        }

        fallMap.put(me.getPlayer(), false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(FactionsPlugin.instance, () -> fallMap.remove(me.getPlayer()), 180L);

        if (LunarAPI.isLunarAPIEnabled()) {
            LunarAPI.sendHomeWaypoint(me);
        }

    }

    @EventHandler
    public void onPlayerFall(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                Player player = (Player) e.getEntity();
                if (fallMap.containsKey(player)) {
                    e.setCancelled(true);
                    fallMap.remove(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        IFactionPlayer me = FactionPlayersManagerBase.getInstance().getByPlayer(event.getPlayer());

        // and update their last login time to point to when the logged off, for auto-remove routine
        me.setLastLoginTime(System.currentTimeMillis());

        me.logout(); // cache kills / deaths

        CmdSeeChunk.seeChunkMap.remove(me.getPlayer().getName());

        // if player is waiting for fstuck teleport but leaves, remove
        if (FactionsPlugin.instance.getStuckMap().containsKey(me.getPlayer().getUniqueId())) {
            FactionPlayersManagerBase.getInstance().getByPlayer(me.getPlayer()).msg(TL.COMMAND_STUCK_CANCELLED);
            FactionsPlugin.instance.getStuckMap().remove(me.getPlayer().getUniqueId());
            FactionsPlugin.instance.getTimers().remove(me.getPlayer().getUniqueId());
        }


        IFaction myFaction = me.getFaction();
        if (!myFaction.isWilderness()) myFaction.memberLoggedOff();

        if (!myFaction.isWilderness()) {
            for (IFactionPlayer player : myFaction.getFPlayersWhereOnline(true))
                if (player != me && player.isMonitoringJoins()) player.msg(TL.FACTION_LOGOUT, me.getName());

        }

        FScoreboard.remove(me, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        IFactionPlayer me = FactionPlayersManagerBase.getInstance().getByPlayer(player);

        //Warmup is in serious problems if they don't do anything interesting.
        if (!ChunkReference.isSameBlock(event)) {
            VisualizeUtil.clear(event.getPlayer());
            if (me.isWarmingUp()) {
                me.clearWarmup();
                me.msg(TL.WARMUPS_CANCELLED);
            }
        }

        if (ChunkReference.isSameChunk(event)) return;

        // Did we change coord?
        FLocation from = me.getLastStoodAt();
        FLocation to = new FLocation(event.getTo());

        if (from.equals(to)) return;

        me.setLastStoodAt(to);

        // Did we change "host"(faction)?
        IFaction factionFrom = Board.getInstance().getFactionAt(from);
        IFaction factionTo = Board.getInstance().getFactionAt(to);
        boolean changedFaction = (factionFrom != factionTo);

        if (changedFaction) {
            Bukkit.getScheduler().runTask(FactionsPlugin.getInstance(), () -> Bukkit.getServer().getPluginManager().callEvent(new FPlayerEnteredFactionEvent(factionTo, factionFrom, me)));
            if (FactionsPlugin.getInstance().getConfig().getBoolean("Title.Show-Title")) {
                player.setMetadata("showFactionTitle", new FixedMetadataValue(FactionsPlugin.getInstance(), true));
            }
        }

        // Not sure if the "else" is map related
//        if (me.isMapAutoUpdating()) {
//            if (!showTimes.containsKey(player.getUniqueId()) || (showTimes.get(player.getUniqueId()) < System.currentTimeMillis())) {
//                me.sendFancyMessage(Board.getInstance().getMap(me, to, player.getLocation().getYaw()));
//                showTimes.put(player.getUniqueId(), System.currentTimeMillis() + FactionsPlugin.getInstance().getConfig().getInt("findfactionsexploit.cooldown"));
//            }
//        } else {
//            Faction myFaction = me.getFaction();
//            String ownersTo = myFaction.getOwnerListString(to);
//
//            if (changedFaction) {
//                me.sendFactionHereMessage(factionFrom);
//                if (Conf.ownedAreasEnabled && Conf.ownedMessageOnBorder && myFaction == factionTo && !ownersTo.isEmpty()) {
//                    me.sendMessage(TL.GENERIC_OWNERS.format(ownersTo));
//                }
//            } else if (Conf.ownedAreasEnabled && Conf.ownedMessageInsideTerritory && myFaction == factionTo && !myFaction.isWilderness()) {
//                String ownersFrom = myFaction.getOwnerListString(from);
//                if (Conf.ownedMessageByChunk || !ownersFrom.equals(ownersTo)) {
//                    if (!ownersTo.isEmpty()) {
//                        me.sendMessage(TL.GENERIC_OWNERS.format(ownersTo));
//                    } else if (!TL.GENERIC_PUBLICLAND.toString().isEmpty()) {
//                        me.sendMessage(TL.GENERIC_PUBLICLAND.toString());
//                    }
//                }
//            }
//        }
    }

    ////inspect
    //@EventHandler
    //public void onInspect(PlayerInteractEvent e) {
    //    if (e.getAction().name().contains("BLOCK")) {
    //        FPlayer fplayer = FPlayers.getInstance().getByPlayer(e.getPlayer());
    //        if (!fplayer.isInspectMode()) {
    //            return;
    //        }
    //        e.setCancelled(true);
    //        if (!fplayer.isAdminBypassing()) {
    //            if (!fplayer.hasFaction()) {
    //                fplayer.setInspectMode(false);
    //                fplayer.msg(TL.COMMAND_INSPECT_DISABLED_NOFAC);
    //                return;
    //            }
    //            if (fplayer.getFaction() != Board.getInstance().getFactionAt(new FLocation(e.getPlayer().getLocation()))) {
    //                fplayer.msg(TL.COMMAND_INSPECT_NOTINCLAIM);
    //                return;
    //            }
    //        } else {
    //            fplayer.msg(TL.COMMAND_INSPECT_BYPASS);
    //        }
    //        List<String[]> info = CoreProtect.getInstance().getAPI().blockLookup(e.getClickedBlock(), 0);
    //        if (info.size() == 0) {
    //            e.getPlayer().sendMessage(TL.COMMAND_INSPECT_NODATA.toString());
    //            return;
    //        }
    //        Player player = e.getPlayer();
    //        CoreProtectAPI coAPI = CoreProtect.getInstance().getAPI();
    //        player.sendMessage(TL.COMMAND_INSPECT_HEADER.toString().replace("{x}", e.getClickedBlock().getX() + "")
    //                .replace("{y}", e.getClickedBlock().getY() + "")
    //                .replace("{z}", e.getClickedBlock().getZ() + ""));
    //        String rowFormat = TL.COMMAND_INSPECT_ROW.toString();
    //        for (String[] strings : info) {
    //            CoreProtectAPI.ParseResult row = coAPI.parseResult(strings);
    //            player.sendMessage(rowFormat
    //                    .replace("{time}", convertTime(row.getTime()))
    //                    .replace("{action}", row.getActionString())
    //                    .replace("{player}", row.getPlayer())
    //                    .replace("{block-type}", row.getType().toString().toLowerCase()));
    //        }
    //    }
    //}

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        IFactionPlayer fme = FactionPlayersManagerBase.getInstance().getById(e.getPlayer().getUniqueId().toString());
        if (fme.isInVault()) fme.setInVault(false);
        if (fme.isInFactionsChest()) fme.setInFactionsChest(false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
            return;

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (block == null) return;


        Material type;
        if (event.getItem() != null) {
            // Convert 1.8 Material Names -> 1.14
            try {
                type = XMaterial.matchXMaterial(event.getItem().getType().toString()).get().parseMaterial();
            } catch (NullPointerException npe) {
                type = null;
            }
        } else {
            type = null;
        }

        // Creeper Egg Bypass.
        if (Conf.allowCreeperEggingChests && block.getType() == Material.CHEST && type == XMaterial.CREEPER_SPAWN_EGG.parseMaterial() && event.getPlayer().isSneaking()) {
            return;
        }


        // territoryBypasssProtectedMaterials totally bypass the protection system
        if (Conf.territoryBypassProtectedMaterials.contains(block.getType())) return;
        // Do type null checks so if XMaterial has a parsing issue and fills null as a value it will not bypass.
        // territoryCancelAndAllowItemUseMaterial bypass the protection system but only if they're not clicking on territoryDenySwitchMaterials
        // if they're clicking on territoryDenySwitchMaterials, let the protection system handle the permissions
        //if (type != null && !Conf.territoryDenySwitchMaterials.contains(block.getType())) {
        //    if (Conf.territoryCancelAndAllowItemUseMaterial.contains(event.getPlayer().getItemInHand().getType()) && !Conf.territoryDenySwitchMaterials.contains(block.getType())) {
        //        return;
        //    }
        //}

        if (GetPermissionFromUsableBlock(block.getType()) != null) {
            if (!canPlayerUseBlock(player, block, false)) {
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                return;
            }
        }

        if (type != null && !playerCanUseItemHere(player, block.getLocation(), event.getItem().getType(), false, PermissableAction.ITEM)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onInventorySee(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        if (!e.getView().getTitle().endsWith("'s Player Inventory")) return;
        e.setCancelled(true);
    }


    @EventHandler
    public void onPlayerBoneMeal(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == XMaterial.GRASS_BLOCK.parseMaterial()
                && event.hasItem() && event.getItem().getType() == XMaterial.BONE_MEAL.parseMaterial()) {
            if (!FactionsBlockListener.playerCanBuildDestroyBlock(event.getPlayer(), block.getLocation(), "build", true)) {
                IFactionPlayer me = FactionPlayersManagerBase.getInstance().getById(event.getPlayer().getUniqueId().toString());
                IFaction myFaction = me.getFaction();

                me.msg(TL.ACTIONS_NOPERMISSION.toString().replace("{faction}", myFaction.getTag(me.getFaction())).replace("{action}", "use bone meal"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        IFactionPlayer me = FactionPlayersManagerBase.getInstance().getByPlayer(event.getPlayer());

        me.getPower();  // update power, so they won't have gained any while dead

        Location home = me.getFaction().getHome();
        if (Conf.homesEnabled &&
                Conf.homesTeleportToOnDeath &&
                home != null &&
                (Conf.homesRespawnFromNoPowerLossWorlds || ((!Conf.worldsNoPowerLoss.contains(event.getPlayer().getWorld().getName()) && !Conf.useWorldConfigurationsAsWhitelist) || (Conf.worldsNoPowerLoss.contains(event.getPlayer().getWorld().getName()) && Conf.useWorldConfigurationsAsWhitelist)))) {
            event.setRespawnLocation(home);
        }
    }

    // For some reason onPlayerInteract() sometimes misses bucket events depending on distance (something like 2-3 blocks away isn't detected),
    // but these separate bucket events below always fire without fail
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Block block = event.getBlockClicked();
        Player player = event.getPlayer();

        if (!playerCanUseItemHere(player, block.getLocation(), event.getBucket(), false, PermissableAction.BUILD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Block block = event.getBlockClicked();
        Player player = event.getPlayer();

        if (!playerCanUseItemHere(player, block.getLocation(), event.getBucket(), false, PermissableAction.DESTROY)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            LogoutHandler handler = LogoutHandler.getByName(player.getName());
            if (handler.isLogoutActive(player)) {
                handler.cancelLogout(player);
                player.sendMessage(String.valueOf(TL.COMMAND_LOGOUT_DAMAGE_TAKEN));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractGUI(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getHolder() instanceof FactionGUI) {
            event.setCancelled(true);
            ((FactionGUI) event.getClickedInventory().getHolder()).onClick(event.getRawSlot(), event.getClick());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMoveGUI(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof FactionGUI) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        IFactionPlayer badGuy = FactionPlayersManagerBase.getInstance().getByPlayer(event.getPlayer());
        if (badGuy == null) return;

        // if player was banned (not just kicked), get rid of their stored info
        if (Conf.removePlayerDataWhenBanned && event.getReason().equals(Conf.removePlayerDataWhenBannedReason)) {
            if (badGuy.getRole() == Role.LEADER) badGuy.getFaction().promoteNewLeader();
            badGuy.leave();
            badGuy.remove();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    final public void onFactionJoin(FPlayerJoinEvent event) {
        FTeamWrapper.applyUpdatesLater(event.getFaction());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFactionLeave(FPlayerLeaveEvent event) {
        FTeamWrapper.applyUpdatesLater(event.getFaction());
    }

    public Set<FLocation> getCorners() {
        return corners;
    }

    @EventHandler
    public void AsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (CmdFGlobal.toggled.contains(p.getUniqueId())) {
            //they're muted, check status of Faction Chat
            if (FactionPlayersManagerBase.getInstance().getByPlayer(p).getFaction() == null) {
                //they're muted, and not in a faction, cancel and return
                e.setCancelled(true);
                return;
            } else {
                //are in a faction that's not Wilderness, SafeZone, or Warzone, check their chat status
                if (!FactionPlayersManagerBase.getInstance().getByPlayer(p).getChatMode().isAtLeast(ChatMode.ALLIANCE)) {
                    //their Faction Chat Mode is not at-least a Alliance, cancel and return
                    e.setCancelled(true);
                    return;
                }
            }
        }

        //we made it this far, since we didn't return yet, we must have sent the chat event through
        //iterate through all of recipients and check if they're muted, then remove them from the event list

        List<Player> l = new ArrayList<>(e.getRecipients());

        for (int i = l.size() - 1; i >= 0; i--) { // going backwards in the list to prevent a ConcurrentModificationException
            Player recipient = l.get(i);
            if (recipient != null) {
                if (CmdFGlobal.toggled.contains(recipient.getUniqueId())) {
                    e.getRecipients().remove(recipient);
                }
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        IFactionPlayer fPlayer = FactionPlayersManagerBase.getInstance().getByPlayer(e.getPlayer());
        if (fPlayer.isInFactionsChest()) fPlayer.setInFactionsChest(false);
    }

    @EventHandler
    public void onTab(PlayerChatTabCompleteEvent e) {
        if (!Discord.useDiscord) {
            return;
        }

        String[] msg = e.getChatMessage().split(" ");
        if (msg.length == 0 | !msg[msg.length - 1].contains("@")) {
            return;
        }
        IFactionPlayer fp = FactionPlayersManagerBase.getInstance().getByPlayer(e.getPlayer());

        if (fp == null) return;

        if (fp.getChatMode() != ChatMode.FACTION) {
            return;
        }
        IFaction f = fp.getFaction();
        if (f == null) return;
        if (f.isSystemFaction()) {
            return;
        }
        if (f.getGuildId() == null | f.getFactionChatChannelId() == null) {
            return;
        }
        if (Discord.jda.getGuildById(f.getGuildId()) == null | Discord.jda.getGuildById(f.getGuildId()).getTextChannelById(f.getFactionChatChannelId()) == null) {
            return;
        }
        TextChannel t = Discord.jda.getGuildById(f.getGuildId()).getTextChannelById(f.getFactionChatChannelId());
        String target = msg[msg.length - 1].replace("@", "");
        List<String> targets = new ArrayList<>();
        if (target.equals("")) {
            if (t != null) {
                for (Member m : t.getMembers()) {
                    targets.add("@" + m.getUser().getName() + "#" + m.getUser().getDiscriminator());
                }
            }
        } else {
            if (t != null) {
                for (Member m : t.getMembers()) {
                    if (m.getEffectiveName().contains(target) | m.getUser().getName().contains(target)) {
                        targets.add("@" + m.getUser().getName() + "#" + m.getUser().getDiscriminator());
                    }
                }
            }
        }
        e.getTabCompletions().clear();
        e.getTabCompletions().addAll(targets);
    }
}
