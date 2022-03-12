package com.massivecraft.factions.mysql;

import com.massivecraft.factions.*;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.event.PowerRegenEvent;
import com.massivecraft.factions.iface.IRelationParticipator;
import com.massivecraft.factions.integration.Essentials;
import com.massivecraft.factions.integration.Worldguard;
import com.massivecraft.factions.mysql.abstracts.DBConnection;
import com.massivecraft.factions.scoreboards.FScoreboard;
import com.massivecraft.factions.scoreboards.sidebar.FInfoSidebar;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.Logger;
import com.massivecraft.factions.util.RelationUtil;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FactionPlayer extends DBConnection implements IRelationParticipator {

    // region Variables

    // DB Fields
    private int id_player;
    private int id_season;
    private int id_faction;
    private String player_UUID;
    private String player_name;
    private String discord_tag;
    private int kills;
    private String ip_public_4;
    private String ip_public_6;

    protected Role role;

    protected String title; // faction_name + concat.all(roles)
    //always be true
    protected boolean titlesEnabled = true;

    // Inherit from MemoryFPlayer
    protected ChatMode chatMode;

    protected boolean ignoreAllianceChat = false;
    public boolean enemiesNearby = false;
    public boolean inChest = false;
    public boolean inVault = false;
    protected boolean showScoreboard = true;
    protected boolean enteringPassword = false;
    protected boolean seeingChunk = false;
    boolean inspectMode = false;
    boolean friendlyFire = false;

    protected double max_power;
    protected double power;
    protected double powerBoost;
    protected long lastPowerUpdateTime;
    protected long millisPassed;
    protected long lastLoginTime;
    protected boolean monitorJoins;
    protected boolean notificationsEnabled;

    protected transient FLocation lastStoodAt = new FLocation(); // Where did this player stand the last time we checked?

    protected HashMap<String, Long> commandCooldown = new HashMap<>();

    // endregion

    public FactionPlayer(FactionsPlugin _plugin, String UUID) {
        super(_plugin);

        ResultSet player_info = this.getResultSet("GET_PLAYER(?)", UUID);
        if (player_info == null) return;

        if (!BuildCLass(player_info)) {
            plugin.getLogger().info("Player with UUID " + UUID + " could not be build.");
        }
    }

    public FactionPlayer(FactionsPlugin _plugin, Map<String, Object> _values) {
        super(_plugin);

        if (!BuildCLass(_values)) {
            plugin.getLogger().info("Player with UUID " + _values.get(0) + " could not be build.");
        }
    }

    // region Overrides
    @Override
    public String describeTo(IRelationParticipator that, boolean ucfirst) {
        return RelationUtil.describeThatToMe(this, that, ucfirst);
    }

    @Override
    public String describeTo(IRelationParticipator that) {
        return RelationUtil.describeThatToMe(this, that);
    }

    @Override
    public Relation getRelationTo(IRelationParticipator rp) {
        return RelationUtil.getRelationTo(this, rp);
    }

    @Override
    public Relation getRelationTo(IRelationParticipator rp, boolean ignorePeaceful) {
        return RelationUtil.getRelationTo(this, rp, ignorePeaceful);
    }

    @Override
    public ChatColor getColorTo(IRelationParticipator rp) {
        return RelationUtil.getColorOfThatToMe(this, rp);
    }

    public String getChatTag(FactionPlayer fplayer) {
        // TODO: Here is important to change the Faction.id() to Faction.name()
        return this.hasFaction() ?
                this.getRelationTo(fplayer).getColor() + "[" + this.getFaction().getId_faction() + " - " + role.getPrefix() + "]"
                : "";
    }

    public void checkIfNearbyEnemies() {
        Player me = this.getPlayer();

        int radius = Conf.stealthFlyCheckRadius;
        for (Entity e : me.getNearbyEntities(radius, 255, radius)) {
            if (!(e instanceof Player)) continue;

            Player eplayer = (((Player) e).getPlayer());
            if (eplayer == null) continue;
            if (eplayer.hasMetadata("NPC")) continue;

            FactionPlayer efplayer = FactionPlayersManagerBase.getInstance().getByPlayer(eplayer);
            if (efplayer == null) continue;
            if (!me.canSee(eplayer) || efplayer.isVanished()) continue;
//                && !efplayer.isStealthEnabled() for assasin roles
            if (this.getRelationTo(efplayer).equals(Relation.ENEMY)) {
                this.enemiesNearby = true;
                return;
            }
        }

        this.enemiesNearby = false;
    }

    public String getRolePrefix() {

        switch (getRole()) {
            case RECRUIT:
                return Conf.prefixRecruit;
            case NORMAL:
                return Conf.prefixNormal;
            case MODERATOR:
                return Conf.prefixMod;
            case COLEADER:
                return Conf.prefixCoLeader;
            case LEADER:
                return Conf.prefixLeader;
        }
        return null;
    }

    public boolean isInspectMode() {
        return inspectMode;
    }

    public void setInspectMode(boolean status) {
        inspectMode = status;
    }

    public boolean showScoreboard() {
        return this.showScoreboard;
    }

    public void setShowScoreboard(boolean show) {
        this.showScoreboard = show;
    }
    // endregion

    // region Faction Relations

    public boolean hasFaction() {
        return id_faction > 0;
    }

    // endregion

    // region Validators

    public boolean isCooldownEnded(String cmd) {
        if (this.getPlayer().isOp())
            return true;
        if (!commandCooldown.containsKey(cmd))
            return true;
        else return commandCooldown.containsKey(cmd) && commandCooldown.get(cmd) <= System.currentTimeMillis();
    }

    public boolean isMonitoringJoins() {
        return this.monitorJoins;
    }

    public boolean isVanished() {
        return Essentials.isVanished(getPlayer());
    }

    public boolean isIgnoreAllianceChat() {
        return ignoreAllianceChat;
    }

    //----------------------------------------------//
    // Territory
    //----------------------------------------------//
    public boolean isInOwnTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)) == this.getFaction();
    }

    public boolean isInOthersTerritory() {
        Faction factionHere = Board.getInstance().getFactionAt(new FLocation(this));
        return factionHere != null && factionHere.isNormal() && factionHere != this.getFaction();
    }

    public boolean isInAllyTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this).isAlly();
    }

    public boolean isInNeutralTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this).isNeutral();
    }

    public boolean isInEnemyTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this).isEnemy();
    }

    public boolean isOnline() {
        return this.getPlayer() != null;
    }

    // make sure target player should be able to detect that this player is online
    public boolean isOnlineAndVisibleTo(Player player) {
        Player target = this.getPlayer();
        return target != null && player.canSee(target);
    }

    public boolean isOffline() {
        return !isOnline();
    }

    public boolean isInFactionsChest() {
        return inChest;
    }

    public boolean isInVault() {
        return inVault;
    }

    public boolean isSeeingChunk() {
        return seeingChunk;
    }

    public boolean isEnteringPassword() {
        return enteringPassword;
    }

    // endregion

    // region on Actions

    // Not sure if this methods is going to be used. Most likely not.
    public void onDeath() {
//        this.updatePower();
//        this.alterPower(-Conf.powerPerDeath);
//        if (hasFaction()) getFaction().setLastDeath(System.currentTimeMillis());
    }

    // endregionerror = ""

    // region Utils/Logic/Misc

    public void login() {
        this.kills = getPlayer().getStatistic(Statistic.PLAYER_KILLS);
    }

    public void logout() {
        this.kills = getPlayer().getStatistic(Statistic.PLAYER_KILLS);
    }

    // I don't want to players have the chance of resetting their factions.
//    public void resetFactionData(boolean doSpoutUpdate) {
//        // clean up any territory ownership in old faction, if there is one
//        if (id_faction != null && Factions.getInstance().isValidFactionId(this.getFactionId())) {
//            Faction currentFaction = this.getFaction();
//            //Discord
//            try {
//                if (Discord.useDiscord && this.discordSetup() && Discord.isInMainGuild(this.discordUser()) && Discord.mainGuild != null) {
//                    Member m = Discord.mainGuild.retrieveMember(this.discordUser()).complete();
//                    if (FactionsPlugin.getInstance().getFileManager().getDiscord().fetchBoolean("Discord.Guild.leaderRoles") && this.role == Role.LEADER && Discord.leader != null)
//                        Discord.mainGuild.removeRoleFromMember(m, Discord.leader).queue();
//                    if (FactionsPlugin.getInstance().getFileManager().getDiscord().fetchBoolean("Discord.Guild.factionRoles"))
//                        Discord.mainGuild.removeRoleFromMember(m, Objects.requireNonNull(Discord.createFactionRole(this.getFaction().getTag()))).queue();
//                    if (FactionsPlugin.getInstance().getFileManager().getDiscord().fetchBoolean("Discord.Guild.factionDiscordTags"))
//                        Discord.resetNick(this);
//                }
//            } catch (HierarchyException e) {
//                Logger.print(e.getMessage(), Logger.PrefixType.FAILED);
//            }
//            //End Discord
//            currentFaction.removeFPlayer(this);
//            if (currentFaction.isNormal()) currentFaction.clearClaimOwnership(this);
//        }
//
//        this.id_faction = "0"; // The default neutral faction
//        this.chatMode = ChatMode.PUBLIC;
//        this.role = Role.NORMAL;
//        this.title = "";
//    }

//    public void resetFactionData() {
//        this.resetFactionData(true);
//    }

    //----------------------------------------------//
    // Health
    //----------------------------------------------//
    public void heal(int amnt) {
        Player player = this.getPlayer();
        if (player == null) return;
        player.setHealth(player.getHealth() + amnt);
    }

    //----------------------------------------------//
    // Power
    //----------------------------------------------//

    public void alterPower(double delta) {
        this.power += delta;
        if (this.power > this.getPowerMax())
            this.power = this.getPowerMax();
        else if (this.power < this.getPowerMin())
            this.power = this.getPowerMin();
    }

    public void updatePower() {
        if (this.isOffline()) {
            if (!Conf.powerRegenOffline) {
                return;
            }
        } else if (hasFaction()) {
            return; // Don't let power regen if faction power is frozen.
        }

        long now = System.currentTimeMillis();
        long millisPassed = now - this.lastPowerUpdateTime;
        this.lastPowerUpdateTime = now;

        Player thisPlayer = this.getPlayer();
        if (thisPlayer.isDead()) {
            return;  // don't let dead players regain power until they respawn
        }

        double delta = millisPassed * Conf.powerPerMinute / 60000; // millisPerMinute : 60 * 1000
        if (Bukkit.getPluginManager().getPlugin("FactionsPlugin") != null) {
            Bukkit.getScheduler().runTask(FactionsPlugin.getInstance(), () -> {
                PowerRegenEvent powerRegenEvent = new PowerRegenEvent(getFaction(), this, delta);
                Bukkit.getServer().getPluginManager().callEvent(powerRegenEvent);
                if (!powerRegenEvent.isCancelled()) {
                    this.alterPower(powerRegenEvent.getDelta());
                }
            });
        } else {
            this.alterPower(delta);
        }
    }

    // Not sure what this method does
    public void sendFactionHereMessage(Faction from) {
        Faction toShow = Board.getInstance().getFactionAt(lastStoodAt);

        if ((Conf.worldsNoClaiming.contains(lastStoodAt.getWorldName()) && !Conf.useWorldConfigurationsAsWhitelist) || (!Conf.worldsNoClaiming.contains(lastStoodAt.getWorldName()) && Conf.useWorldConfigurationsAsWhitelist))
            return;

        if (showInfoBoard(toShow)) {
            FScoreboard.get(this).setTemporarySidebar(new FInfoSidebar(toShow));
        }
        if (FactionsPlugin.getInstance().getConfig().getBoolean("scoreboard.also-send-chat", true))
            this.sendMessage(FactionsPlugin.getInstance().txt.parse(TL.FACTION_LEAVE.format(from.getFaction_tag(), toShow.getFaction_tag())));
    }

    // -------------------------------
    // Actions
    // -------------------------------

    /**
     * Check if the scoreboard should be shown. Simple method to be used by above method.
     *
     * @param toShow Faction to be shown.
     * @return true if should show, otherwise false.
     */
    public boolean showInfoBoard(Faction toShow) {
        //&& !toShow.isWarZone() && !toShow.isWilderness() && !toShow.isSafeZone()
        return showScoreboard && FactionsPlugin.getInstance().getConfig().contains("scoreboard.finfo") && FactionsPlugin.getInstance().getConfig().getBoolean("scoreboard.finfo-enabled", false) && FScoreboard.get(this) != null;
    }

    // Not sure if I want players to leave freely from their factions.
    public void leave() {
//        Faction myFaction = this.getFaction();
//        if (myFaction == null) return;

//        if (!perm && this.getRole() == Role.LEADER && myFaction.getFPlayers().size() > 1) {
//            msg(TL.LEAVE_PASSADMIN);
//            return;
//        }
//
//        if (!Conf.canLeaveWithNegativePower && this.power < 0) {
//            msg(TL.LEAVE_NEGATIVEPOWER);
//            return;
//        }
//
//        FPlayerLeaveEvent leaveEvent = new FPlayerLeaveEvent(this, myFaction, FPlayerLeaveEvent.PlayerLeaveReason.LEAVE);
//        Bukkit.getServer().getPluginManager().callEvent(leaveEvent);
//        if (leaveEvent.isCancelled()) return;
//
//
//        if (myFaction.isNormal()) {
//            for (FactionPlayer fplayer : myFaction.getFPlayersWhereOnline(true))
//                FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> fplayer.msg(TL.LEAVE_LEFT, this.describeTo(fplayer, true), myFaction.describeTo(fplayer)));
//            if (Conf.logFactionLeave)
//                FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> Logger.print(TL.LEAVE_LEFT.format(this.player_name, myFaction.getTag()), Logger.PrefixType.DEFAULT));
//        }
//        myFaction.removeAnnouncements(this);
////        FactionsPlugin.instance.logFactionEvent(myFaction, FLogType.INVITES, this.getName(), CC.Red + "left", "the faction");
//        if (myFaction.isNormal() && !perm && myFaction.getFPlayers().isEmpty()) {
//            // Remove this faction
//
//            if (FactionsPlugin.getInstance().getConfig().getBoolean("faction-disband-broadcast")) {
//
//                String message = TL.LEAVE_DISBANDED.toString()
//                        .replace("{claims}", myFaction.getAllClaims().size() + "");
//
//                for (FactionPlayer fplayer : FactionPlayersManagerBase.getInstance().getOnlinePlayers())
//                    fplayer.msg(message, myFaction.describeTo(fplayer, true));
//            }
//
//
//            FactionDisbandEvent disbandEvent = new FactionDisbandEvent(getPlayer(), myFaction.getId(), FactionDisbandEvent.PlayerDisbandReason.LEAVE);
//            Bukkit.getPluginManager().callEvent(disbandEvent);
//
//            Factions.getInstance().removeFaction(myFaction.getId());
//            if (Conf.logFactionDisband)
//                FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance,
//                        () -> Logger.print(TL.LEAVE_DISBANDEDLOG.format(myFaction.getTag(), myFaction.getId(),
//                                this.player_name).replace("{claims}", myFaction.getAllClaims().size() + ""), Logger.PrefixType.DEFAULT));
//        }
    }

    public boolean canClaimForFaction(Faction forFaction) {
        return forFaction.isNormal() && (forFaction == this.getFaction() && this.getRole().isAtLeast(Role.MODERATOR));
    }

    public boolean canClaimForFactionAtLocation(Faction forFaction, Location location, boolean notifyFailure) {
        return canClaimForFactionAtLocation(forFaction, new FLocation(location), notifyFailure);
    }

    public boolean canClaimForFactionAtLocation(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        FactionsPlugin plugin = FactionsPlugin.getInstance();
        String error = null;
        Faction myFaction = getFaction();
        Faction currentFaction = Board.getInstance().getFactionAt(flocation);
        int ownedLand = forFaction.getLandRounded();
        int factionBuffer = plugin.getConfig().getInt("hcf.buffer-zone", 0);
        int worldBuffer = plugin.getConfig().getInt("world-border.buffer", 0);

        // TODO: I mean, this kind of if's can't be right, right? right?
        // DAMN THIS IF IS INSANE, no no definitely not staying.
        if (Conf.worldGuardChecking && Worldguard.getInstance().checkForRegionsInChunk(flocation)) {
            // Checks for WorldGuard regions in the chunk attempting to be claimed
            error = plugin.txt.parse(TL.CLAIM_PROTECTED.toString());
        } else if (flocation.isOutsideWorldBorder(worldBuffer)) {
            error = plugin.txt.parse(TL.CLAIM_OUTSIDEWORLDBORDER.toString());
        } else if ((Conf.worldsNoClaiming.contains(flocation.getWorldName()) && !Conf.useWorldConfigurationsAsWhitelist) || (!Conf.worldsNoClaiming.contains(flocation.getWorldName()) && Conf.useWorldConfigurationsAsWhitelist)) {
            error = plugin.txt.parse(TL.CLAIM_DISABLED.toString());
        } else if (currentFaction.getAccess(this, PermissableAction.TERRITORY) == Access.ALLOW && forFaction != currentFaction) {
            return true;
        } else if (myFaction != forFaction) {
            error = plugin.txt.parse(TL.CLAIM_CANTCLAIM.toString(), forFaction.describeTo(this));
        } else if (forFaction == currentFaction) {
            error = plugin.txt.parse(TL.CLAIM_ALREADYOWN.toString(), forFaction.describeTo(this, true));
        } else if (forFaction.getFaction_Players().length < Conf.claimsRequireMinFactionMembers) {
            error = plugin.txt.parse(TL.CLAIM_MEMBERS.toString(), Conf.claimsRequireMinFactionMembers);
        } else if (plugin.getConfig().getBoolean("hcf.allow-overclaim", true) && ownedLand >= forFaction.getPowerRounded()) {
            error = plugin.txt.parse(TL.CLAIM_POWER.toString());
        } else if (Conf.claimedLandsMax != 0 && ownedLand >= Conf.claimedLandsMax && forFaction.isNormal()) {
            error = plugin.txt.parse(TL.CLAIM_LIMIT.toString());
        } else if (currentFaction.getRelationTo(forFaction) == Relation.ALLY) {
            error = plugin.txt.parse(TL.CLAIM_ALLY.toString());
        } else if (Conf.claimsMustBeConnected && myFaction.getLandRoundedInWorld(flocation.getWorldName()) > 0 && !Board.getInstance().isConnectedLocation(flocation, myFaction) && (!Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction || !currentFaction.isNormal())) {
            if (Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction) {
                error = plugin.txt.parse(TL.CLAIM_CONTIGIOUS.toString());
            } else {
                error = plugin.txt.parse(TL.CLAIM_FACTIONCONTIGUOUS.toString());
            }
        } else if (factionBuffer > 0 && Board.getInstance().hasFactionWithin(flocation, myFaction, factionBuffer)) {
            error = plugin.txt.parse(TL.CLAIM_TOOCLOSETOOTHERFACTION.format(factionBuffer));
        } else if (flocation.isOutsideWorldBorder(worldBuffer)) {
            if (worldBuffer > 0) {
                error = plugin.txt.parse(TL.CLAIM_OUTSIDEBORDERBUFFER.format(worldBuffer));
            } else {
                error = plugin.txt.parse(TL.CLAIM_OUTSIDEWORLDBORDER.toString());
            }
        } else if (currentFaction.isNormal()) {
            error = "This terrain is already taken.";
//            if (myFaction.isPeaceful()) {
//                error = plugin.txt.parse(TL.CLAIM_PEACEFUL.toString(), currentFaction.getTag(this));
//            } else if (currentFaction.isPeaceful()) {
//                error = plugin.txt.parse(TL.CLAIM_PEACEFULTARGET.toString(), currentFaction.getTag(this));
//            } else if (!currentFaction.hasLandInflation()) {
//                // TODO more messages WARN current faction most importantly
//                error = plugin.txt.parse(TL.CLAIM_THISISSPARTA.toString(), currentFaction.getTag(this));
//            } else if (currentFaction.hasLandInflation() && !plugin.getConfig().getBoolean("hcf.allow-overclaim", true)) {
//                // deny over claim when it normally would be allowed.
//                error = plugin.txt.parse(TL.CLAIM_OVERCLAIM_DISABLED.toString());
//            } else if (!Board.getInstance().isBorderLocation(flocation)) {
//                error = plugin.txt.parse(TL.CLAIM_BORDER.toString());
//            }
        }
        // TODO: Add more else if statements.

        if (notifyFailure && error != null) {
            msg(error);
        }
        return error == null;
    }

    public boolean attemptClaim(Faction forFaction, Location location, boolean notifyFailure) {
        return attemptClaim(forFaction, new FLocation(location), notifyFailure);
    }

    public boolean shouldBeSaved() {
        return this.hasFaction();
        //|| (this.power != this.getPowerMax() && this.power != (int) Math.round(Conf.powerPlayerStarting));
    }

    public void remove() {
        // JSONFPlayers is supposed to be a Player Manager Class.
        // This class is responsible to handle the events between factions and players.
//        ((JSONFPlayers) FPlayers.getInstance()).fPlayers.remove(getId());
    }

    public boolean hasFriendlyFire() {
        return friendlyFire;
    }

    public boolean attemptClaim(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        // notifyFailure is false if called by auto-claim; no need to notify on every failure for it
        // return value is false on failure, true on success

        Faction currentFaction = Board.getInstance().getFactionAt(flocation);
        int ownedLand = forFaction.getLandRounded();

        if (!this.canClaimForFactionAtLocation(forFaction, flocation, notifyFailure)) {
            return false;
        }


        // TODO: debug if this is important
//        if (forFaction.getClaimOwnership().containsKey(flocation) && !forFaction.isPlayerInOwnerList(this, flocation)) {
//            this.msg(TL.GENERIC_FPERM_OWNER_NOPERMISSION, "claim");
//            return false;
//        }

        if (Conf.worldGuardChecking && Worldguard.getInstance().checkForRegionsInChunk(flocation)) {
            this.msg(TL.GENERIC_WORLDGUARD);
            return false;
        }

        LandClaimEvent claimEvent = new LandClaimEvent(flocation, forFaction, this);
        Bukkit.getPluginManager().callEvent(claimEvent);

        if (claimEvent.isCancelled()) {
            return false;
        }

        // announce success
        if (!FactionsPlugin.cachedRadiusClaim) {
            Set<FactionPlayer> informTheseFPlayers = new HashSet<>();
            informTheseFPlayers.add(this);
            informTheseFPlayers.addAll(Arrays.asList(forFaction.getFPlayersWhereOnline(true)));
            for (FactionPlayer fp : informTheseFPlayers) {
                FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> fp.msg(TL.CLAIM_CLAIMED, this.describeTo(fp, true), forFaction.describeTo(fp), currentFaction.describeTo(fp)));
            }
        }

        Board.getInstance().setFactionAt(forFaction, flocation);

        if (Conf.logLandClaims) {
            FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> Logger.printArgs(TL.CLAIM_CLAIMEDLOG.toString(), Logger.PrefixType.DEFAULT, this.player_name, flocation.getCoordString(), forFaction.getFaction_tag()));
        }

        return true;
    }

//    public String commas(final double amount) {
//        final DecimalFormat formatter = new DecimalFormat("#,###.00");
//        return formatter.format(amount);
//    }

    // endregion

    // region Messaging

    public void msg(String str, Object... args) {
        this.sendMessage(FactionsPlugin.getInstance().txt.parse(str, args));
    }

    public void msg(TL translation, Object... args) {
        this.msg(translation.toString(), args);
    }

    public void sendMessage(String msg) {
        if (msg.contains("{null}")) return; // user wants this message to not send

        if (msg.contains("/n/")) {
            for (String s : msg.split("/n/")) sendMessage(s);
            return;
        }
        Player player = this.getPlayer();
        if (player == null) return;
        player.sendMessage(msg);
    }

    public void sendMessage(List<String> msgs) {
        for (String msg : msgs) this.sendMessage(msg);
    }

    public void sendFancyMessage(FancyMessage message) {
        Player player = getPlayer();
        if (player == null || !player.isOnGround()) return;
        message.send(player);
    }

    public void sendFancyMessage(List<FancyMessage> messages) {
        Player player = getPlayer();
        if (player == null) return;
        for (FancyMessage msg : messages) msg.send(player);
    }

    // endregion

    // region Getter & Setters

    public int getId_player() {
        return id_player;
    }

    public void setId_player(int id_player) {
        this.id_player = id_player;
    }

    public int getId_season() {
        return id_season;
    }

    public void setId_season(int id_season) {
        this.id_season = id_season;
    }

    public int getId_faction() {
        return id_faction;
    }

    public void setId_faction(int id_faction) {
        this.id_faction = id_faction;
    }

    public String getPlayer_UUID() {
        return player_UUID;
    }

    public void setPlayer_UUID(String player_UUID) {
        this.player_UUID = player_UUID;
    }

    public String getPlayer_name() {
        return player_name;
    }

    public void setPlayer_name(String player_name) {
        this.player_name = player_name;
    }

    public String getDiscord_tag() {
        return discord_tag;
    }

    public void setDiscord_tag(String discord_tag) {
        this.discord_tag = discord_tag;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public String getIp_public_4() {
        return ip_public_4;
    }

    public void setIp_public_4(String ip_public_4) {
        this.ip_public_4 = ip_public_4;
    }

    public String getIp_public_6() {
        return ip_public_6;
    }

    public void setIp_public_6(String ip_public_6) {
        this.ip_public_6 = ip_public_6;
    }

    public boolean getHasEnemiesNearby() {
        return this.enemiesNearby;
    }

    public void setEnemiesNearby(Boolean b) {
        this.enemiesNearby = b;
    }

    @NotNull
    public Player getPlayer() {
        return Objects.requireNonNull(Bukkit.getPlayer(UUID.fromString(this.getPlayer_UUID())));
    }

    public Faction getFaction() {
        // TODO: Needs the implementation of FactionsManager
        if (this.id_faction <= 0) {
            assert FactionsManager.instance != null;
            return FactionsManager.instance.getWilderness();
        }
        assert FactionsManager.instance != null;
        return FactionsManager.instance.getFactionById(this.id_faction);
    }

    public Role getRole() {
        // Hack to fix null roles..
        // TODO: Change from Role to Role[]
        // TODO: There CANNOT be null Roles, but if somehow it occurs, maybe I keep the role enum.
        if (role == null) this.role = Role.NORMAL;
        return this.role;
    }

    public double getPowerMax() {
        return Conf.powerPlayerMax + this.powerBoost;
    }

    public double getPowerMin() {
        return Conf.powerPlayerMin + this.powerBoost;
    }

    public Object[] getDBValues() {
        return new Object[]{id_player, id_season, id_faction, player_UUID, player_name, discord_tag, kills, ip_public_4, ip_public_6};
    }

    public FLocation getLastStoodAt(){
        return this.lastStoodAt;
    }

    public void setLastStoodAt(FLocation flocation) {
        this.lastStoodAt = flocation;
    }

    // endregion
}
