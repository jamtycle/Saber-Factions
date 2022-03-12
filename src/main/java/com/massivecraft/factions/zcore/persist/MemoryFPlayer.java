package com.massivecraft.factions.zcore.persist;

import com.massivecraft.factions.*;
import com.massivecraft.factions.discord.Discord;
import com.massivecraft.factions.discord.DiscordUser;
import com.massivecraft.factions.event.*;
import com.massivecraft.factions.event.FactionDisbandEvent.PlayerDisbandReason;
import com.massivecraft.factions.iface.IRelationParticipator;
import com.massivecraft.factions.integration.Essentials;
import com.massivecraft.factions.integration.Worldguard;
import com.massivecraft.factions.scoreboards.FScoreboard;
import com.massivecraft.factions.scoreboards.sidebar.FInfoSidebar;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.Logger;
import com.massivecraft.factions.util.RelationUtil;
import com.massivecraft.factions.util.WarmUpUtil;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import mkremins.fanciful.FancyMessage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * Logged in players always have exactly one FPlayer instance. Logged out players may or may not have an FPlayer
 * instance. They will always have one if they are part of a faction. This is because only players with a faction are
 * saved to disk (in order to not waste disk space).
 * The FPlayer is linked to a minecraft player using the player name.
 * The same instance is always returned for the same player. This means you can use the == operator. No .equals method
 * necessary.
 */

public abstract class MemoryFPlayer extends DiscordUser implements IFactionPlayer {

    // region Variables
    public boolean enemiesNearby = false;
    public boolean inChest = false;
    public boolean inVault = false;
    protected HashMap<String, Long> commandCooldown = new HashMap<>();
    protected String factionId;
    protected Role role;
    protected String title;
    protected double power;
    protected double powerBoost;
    protected long lastPowerUpdateTime;
    protected long millisPassed;
    protected long lastLoginTime;
    protected ChatMode chatMode;
    protected boolean ignoreAllianceChat = false;
    protected String id;
    protected String name;
    protected boolean monitorJoins;
    protected boolean showScoreboard = true;
    protected WarmUpUtil.Warmup warmup;
    protected int warmupTask;
    protected int kills, deaths;
    protected boolean enteringPassword = false;
    protected transient FLocation lastStoodAt = new FLocation(); // Where did this player stand the last time we checked?
    protected boolean notificationsEnabled;
    protected boolean titlesEnabled = true;
    protected boolean seeingChunk = false;
    boolean inspectMode = false;
    boolean friendlyFire = false;
    // endregion

    // region Constructors

    public MemoryFPlayer(String id) {
        this.id = id;
        this.resetFactionData(false);
        this.power = Conf.powerPlayerStarting;
        this.lastPowerUpdateTime = System.currentTimeMillis();
        this.lastLoginTime = System.currentTimeMillis();
        this.notificationsEnabled = true;
        this.powerBoost = 0.0;
        this.getKills();
        this.getDeaths();
        this.showScoreboard = FactionsPlugin.getInstance().getConfig().getBoolean("scoreboard.default-enabled", false);
        this.notificationsEnabled = true;

        if (!Conf.newPlayerStartingFactionID.equals("0") && Factions.getInstance().isValidFactionId(Conf.newPlayerStartingFactionID)) {
            this.factionId = Conf.newPlayerStartingFactionID;
        }
    }

    public MemoryFPlayer(MemoryFPlayer other) {
        this.factionId = other.factionId;
        this.id = other.id;
        this.power = other.power;
        this.lastLoginTime = other.lastLoginTime;
        this.powerBoost = other.powerBoost;
        this.role = other.role;
        this.title = other.title;
        this.chatMode = other.chatMode;
        this.lastStoodAt = other.lastStoodAt;
        this.getKills();
        this.getDeaths();
        this.notificationsEnabled = other.notificationsEnabled;
        this.showScoreboard = FactionsPlugin.getInstance().getConfig().getBoolean("scoreboard.default-enabled", true);
        this.notificationsEnabled = true;
    }

    // endregion

    // region Overrides

    @Override
    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
    }

    @Override
    public boolean hasNotificationsEnabled() {
        return this.notificationsEnabled;
    }

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

    @Override
    public String getChatTag(IFactionPlayer fplayer) {
        return this.getHasFaction() ? this.getRelationTo(fplayer).getColor() + getChatTag() : "";
    }

    @Override
    public void checkIfNearbyEnemies() {
        Player me = this.getPlayer();

        if (me == null) return;
        if (me.hasPermission("factions.fly.bypassnearbyenemycheck")) return;
        int radius = Conf.stealthFlyCheckRadius;
        for (Entity e : me.getNearbyEntities(radius, 255, radius)) {
            if (e instanceof Player) {
                Player eplayer = (((Player) e).getPlayer());
                if (eplayer == null) continue;
                if (eplayer.hasMetadata("NPC")) continue;
                IFactionPlayer efplayer = FactionPlayersManagerBase.getInstance().getByPlayer(eplayer);
                if (efplayer == null) continue;
                if (!me.canSee(eplayer) || efplayer.isVanished()) continue;
//                && !efplayer.isStealthEnabled() for assasin roles
                if (this.getRelationTo(efplayer).equals(Relation.ENEMY)) {
                    this.enemiesNearby = true;
                    return;
                }
            }
        }
        this.enemiesNearby = false;
    }

    @Override
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

    @Override
    public boolean isInspectMode() {
        return inspectMode;
    }

    @Override
    public void setInspectMode(boolean status) {
        inspectMode = status;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean showScoreboard() {
        return this.showScoreboard;
    }

    @Override
    public void setShowScoreboard(boolean show) {
        this.showScoreboard = show;
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
        IFaction factionHere = Board.getInstance().getFactionAt(new FLocation(this));
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

    public void onDeath() {
        this.updatePower();
        this.alterPower(-Conf.powerPerDeath);
        if (getHasFaction()) getFaction().setLastDeath(System.currentTimeMillis());
    }

    // endregion

    // region Utils/Logic/Misc

    public void login() {
        this.kills = getPlayer().getStatistic(Statistic.PLAYER_KILLS);
        this.deaths = getPlayer().getStatistic(Statistic.DEATHS);
    }

    public void logout() {
        this.kills = getPlayer().getStatistic(Statistic.PLAYER_KILLS);
        this.deaths = getPlayer().getStatistic(Statistic.DEATHS);
    }

    // -------------------------------------------- //
    // Getters And Setters
    // -------------------------------------------- //
    public void resetFactionData(boolean doSpoutUpdate) {
        // clean up any territory ownership in old faction, if there is one
        if (factionId != null && Factions.getInstance().isValidFactionId(this.getFactionId())) {
            IFaction currentFaction = this.getFaction();
            //Discord
            try {
                if (Discord.useDiscord && this.discordSetup() && Discord.isInMainGuild(this.discordUser()) && Discord.mainGuild != null) {
                    Member m = Discord.mainGuild.retrieveMember(this.discordUser()).complete();
                    if (FactionsPlugin.getInstance().getFileManager().getDiscord().fetchBoolean("Discord.Guild.leaderRoles") && this.role == Role.LEADER && Discord.leader != null)
                        Discord.mainGuild.removeRoleFromMember(m, Discord.leader).queue();
                    if (FactionsPlugin.getInstance().getFileManager().getDiscord().fetchBoolean("Discord.Guild.factionRoles"))
                        Discord.mainGuild.removeRoleFromMember(m, Objects.requireNonNull(Discord.createFactionRole(this.getFaction().getTag()))).queue();
                    if (FactionsPlugin.getInstance().getFileManager().getDiscord().fetchBoolean("Discord.Guild.factionDiscordTags"))
                        Discord.resetNick(this);
                }
            } catch (HierarchyException e) {
                Logger.print(e.getMessage(), Logger.PrefixType.FAILED);
            }
            //End Discord
            currentFaction.removeFPlayer(this);
            if (currentFaction.isNormal()) currentFaction.clearClaimOwnership(this);
        }

        this.factionId = "0"; // The default neutral faction
        this.chatMode = ChatMode.PUBLIC;
        this.role = Role.NORMAL;
        this.title = "";
    }

    public void resetFactionData() {
        this.resetFactionData(true);
    }

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
        } else if (getHasFaction() && getFaction().isPowerFrozen()) {
            return; // Don't let power regen if faction power is frozen.
        }

        long now = System.currentTimeMillis();
        long millisPassed = now - this.lastPowerUpdateTime;
        this.lastPowerUpdateTime = now;

        Player thisPlayer = this.getPlayer();
        if (thisPlayer != null && thisPlayer.isDead()) {
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

    public void sendFactionHereMessage(IFaction from) {
        IFaction toShow = Board.getInstance().getFactionAt(getLastStoodAt());

        if ((Conf.worldsNoClaiming.contains(getLastStoodAt().getWorldName()) && !Conf.useWorldConfigurationsAsWhitelist) || (!Conf.worldsNoClaiming.contains(getLastStoodAt().getWorldName()) && Conf.useWorldConfigurationsAsWhitelist))
            return;

        if (showInfoBoard(toShow)) {
            FScoreboard.get(this).setTemporarySidebar(new FInfoSidebar(toShow));
        }
        if (FactionsPlugin.getInstance().getConfig().getBoolean("scoreboard.also-send-chat", true))
            this.sendMessage(FactionsPlugin.getInstance().txt.parse(TL.FACTION_LEAVE.format(from.getTag(this), toShow.getTag(this))));
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
    public boolean showInfoBoard(IFaction toShow) {
        return showScoreboard && !toShow.isWarZone() && !toShow.isWilderness() && !toShow.isSafeZone() && FactionsPlugin.getInstance().getConfig().contains("scoreboard.finfo") && FactionsPlugin.getInstance().getConfig().getBoolean("scoreboard.finfo-enabled", false) && FScoreboard.get(this) != null;
    }

    public void leave() {
        IFaction myFaction = this.getFaction();

        if (myFaction == null) {
            resetFactionData();
            return;
        }

        boolean perm = myFaction.isPermanent();

        if (!perm && this.getRole() == Role.LEADER && myFaction.getFPlayers().size() > 1) {
            msg(TL.LEAVE_PASSADMIN);
            return;
        }

        if (!Conf.canLeaveWithNegativePower && this.getPower() < 0) {
            msg(TL.LEAVE_NEGATIVEPOWER);
            return;
        }

        // if economy is enabled and they're not on the bypass list, make sure they can pay
        FPlayerLeaveEvent leaveEvent = new FPlayerLeaveEvent(this, myFaction, FPlayerLeaveEvent.PlayerLeaveReason.LEAVE);
        Bukkit.getServer().getPluginManager().callEvent(leaveEvent);
        if (leaveEvent.isCancelled()) return;


        if (myFaction.isNormal()) {
            for (IFactionPlayer fplayer : myFaction.getFPlayersWhereOnline(true))
                FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> fplayer.msg(TL.LEAVE_LEFT, this.describeTo(fplayer, true), myFaction.describeTo(fplayer)));
            if (Conf.logFactionLeave)
                FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> Logger.print(TL.LEAVE_LEFT.format(this.getName(), myFaction.getTag()), Logger.PrefixType.DEFAULT));
        }
        myFaction.removeAnnouncements(this);
        this.resetFactionData();
//        FactionsPlugin.instance.logFactionEvent(myFaction, FLogType.INVITES, this.getName(), CC.Red + "left", "the faction");
        if (myFaction.isNormal() && !perm && myFaction.getFPlayers().isEmpty()) {
            // Remove this faction

            if (FactionsPlugin.getInstance().getConfig().getBoolean("faction-disband-broadcast")) {

                String message = TL.LEAVE_DISBANDED.toString()
                        .replace("{claims}", myFaction.getAllClaims().size() + "");

                for (IFactionPlayer fplayer : FactionPlayersManagerBase.getInstance().getOnlinePlayers())
                    fplayer.msg(message, myFaction.describeTo(fplayer, true));
            }


            FactionDisbandEvent disbandEvent = new FactionDisbandEvent(getPlayer(), myFaction.getId(), PlayerDisbandReason.LEAVE);
            Bukkit.getPluginManager().callEvent(disbandEvent);

            Factions.getInstance().removeFaction(myFaction.getId());
            if (Conf.logFactionDisband)
                FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance,
                        () -> Logger.print(TL.LEAVE_DISBANDEDLOG.format(myFaction.getTag(), myFaction.getId(),
                                this.getName()).replace("{claims}", myFaction.getAllClaims().size() + ""), Logger.PrefixType.DEFAULT));
        }
    }

    public boolean canClaimForFaction(IFaction forFaction) {
        return !forFaction.isWilderness() && (forFaction == this.getFaction() && this.getRole().isAtLeast(Role.MODERATOR)) || (forFaction.isSafeZone()) || forFaction.isWarZone();
    }

    public boolean canClaimForFactionAtLocation(IFaction forFaction, Location location, boolean notifyFailure) {
        return canClaimForFactionAtLocation(forFaction, new FLocation(location), notifyFailure);
    }

    public boolean canClaimForFactionAtLocation(IFaction forFaction, FLocation flocation, boolean notifyFailure) {
        FactionsPlugin plugin = FactionsPlugin.getInstance();
        String error = null;
        IFaction myFaction = getFaction();
        IFaction currentFaction = Board.getInstance().getFactionAt(flocation);
        int ownedLand = forFaction.getLandRounded();
        int factionBuffer = plugin.getConfig().getInt("hcf.buffer-zone", 0);
        int worldBuffer = plugin.getConfig().getInt("world-border.buffer", 0);

        if (Conf.worldGuardChecking && Worldguard.getInstance().checkForRegionsInChunk(flocation)) {
            // Checks for WorldGuard regions in the chunk attempting to be claimed
            error = plugin.txt.parse(TL.CLAIM_PROTECTED.toString());
        } else if (flocation.isOutsideWorldBorder(worldBuffer)) {
            error = plugin.txt.parse(TL.CLAIM_OUTSIDEWORLDBORDER.toString());
        } else if ((Conf.worldsNoClaiming.contains(flocation.getWorldName()) && !Conf.useWorldConfigurationsAsWhitelist) || (!Conf.worldsNoClaiming.contains(flocation.getWorldName()) && Conf.useWorldConfigurationsAsWhitelist)) {
            error = plugin.txt.parse(TL.CLAIM_DISABLED.toString());
        } else if (forFaction.isSafeZone()) {
            return true;
        } else if (forFaction.isWarZone()) {
            return true;
        } else if (currentFaction.getAccess(this, PermissableAction.TERRITORY) == Access.ALLOW && forFaction != currentFaction) {
            return true;
        } else if (myFaction != forFaction) {
            error = plugin.txt.parse(TL.CLAIM_CANTCLAIM.toString(), forFaction.describeTo(this));
        } else if (forFaction == currentFaction) {
            error = plugin.txt.parse(TL.CLAIM_ALREADYOWN.toString(), forFaction.describeTo(this, true));
        } else if (forFaction.getFPlayers().size() < Conf.claimsRequireMinFactionMembers) {
            error = plugin.txt.parse(TL.CLAIM_MEMBERS.toString(), Conf.claimsRequireMinFactionMembers);
        } else if (currentFaction.isSafeZone()) {
            error = plugin.txt.parse(TL.CLAIM_SAFEZONE.toString());
        } else if (currentFaction.isWarZone()) {
            error = plugin.txt.parse(TL.CLAIM_WARZONE.toString());
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
            if (myFaction.isPeaceful()) {
                error = plugin.txt.parse(TL.CLAIM_PEACEFUL.toString(), currentFaction.getTag(this));
            } else if (currentFaction.isPeaceful()) {
                error = plugin.txt.parse(TL.CLAIM_PEACEFULTARGET.toString(), currentFaction.getTag(this));
            } else if (!currentFaction.hasLandInflation()) {
                // TODO more messages WARN current faction most importantly
                error = plugin.txt.parse(TL.CLAIM_THISISSPARTA.toString(), currentFaction.getTag(this));
            } else if (currentFaction.hasLandInflation() && !plugin.getConfig().getBoolean("hcf.allow-overclaim", true)) {
                // deny over claim when it normally would be allowed.
                error = plugin.txt.parse(TL.CLAIM_OVERCLAIM_DISABLED.toString());
            } else if (!Board.getInstance().isBorderLocation(flocation)) {
                error = plugin.txt.parse(TL.CLAIM_BORDER.toString());
            }
        }
        // TODO: Add more else if statements.

        if (notifyFailure && error != null) {
            msg(error);
        }
        return error == null;
    }

    public boolean attemptClaim(IFaction forFaction, Location location, boolean notifyFailure) {
        return attemptClaim(forFaction, new FLocation(location), notifyFailure);
    }

    public boolean shouldBeSaved() {
        return this.getHasFaction() || (this.getPowerRounded() != this.getPowerMaxRounded() && this.getPowerRounded() != (int) Math.round(Conf.powerPlayerStarting));
    }

    public abstract void remove();

    public boolean hasFriendlyFire() {
        return friendlyFire;
    }

    public boolean attemptClaim(IFaction forFaction, FLocation flocation, boolean notifyFailure) {
        // notifyFailure is false if called by auto-claim; no need to notify on every failure for it
        // return value is false on failure, true on success

        IFaction currentFaction = Board.getInstance().getFactionAt(flocation);
        int ownedLand = forFaction.getLandRounded();

        if (!this.canClaimForFactionAtLocation(forFaction, flocation, notifyFailure)) {
            return false;
        }


        if (forFaction.getClaimOwnership().containsKey(flocation) && !forFaction.isPlayerInOwnerList(this, flocation)) {
            this.msg(TL.GENERIC_FPERM_OWNER_NOPERMISSION, "claim");
            return false;
        }

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
            Set<IFactionPlayer> informTheseFPlayers = new HashSet<>();
            informTheseFPlayers.add(this);
            informTheseFPlayers.addAll(forFaction.getFPlayersWhereOnline(true));
            for (IFactionPlayer fp : informTheseFPlayers) {
                FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> fp.msg(TL.CLAIM_CLAIMED, this.describeTo(fp, true), forFaction.describeTo(fp), currentFaction.describeTo(fp)));
            }
        }

        Board.getInstance().setFactionAt(forFaction, flocation);

        if (Conf.logLandClaims) {
            FactionsPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FactionsPlugin.instance, () -> Logger.printArgs(TL.CLAIM_CLAIMEDLOG.toString(), Logger.PrefixType.DEFAULT, this.getName(), flocation.getCoordString(), forFaction.getTag()));
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

    // region Getters

    public int getCooldown(String cmd) {
        int seconds = 0;
        if (this.getPlayer().isOp())
            return 0;
        if (commandCooldown.containsKey(cmd))
            seconds = (int) ((this.commandCooldown.get(cmd) - System.currentTimeMillis()) / 1000);
        return seconds;
    }

    public IFaction getFaction() {
        if (this.factionId == null) {
            this.factionId = "0";
        }
        return Factions.getInstance().getFactionById(this.factionId);
    }

    public String getFactionId() {
        return this.factionId;
    }

    public Role getRole() {
        // Hack to fix null roles..
        if (role == null) this.role = Role.NORMAL;
        return this.role;
    }

    public double getPowerBoost() {
        return this.powerBoost;
    }

    public ChatMode getChatMode() {
        if (this.factionId.equals("0") || !Conf.factionOnlyChat) this.chatMode = ChatMode.PUBLIC;
        return chatMode;
    }

    public String getAccountId() {
        return this.getId();
    }

    public void setRole(Role role) {
        if (this.role == role) return;
        FPlayerRoleChangeEvent event = new FPlayerRoleChangeEvent(getFaction(), this, role);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            try {
                if (Discord.useDiscord && this.discordSetup() && Discord.isInMainGuild(this.discordUser()) && Discord.mainGuild != null) {
                    Member m = Discord.mainGuild.retrieveMember(this.discordUser()).complete();
                    if (FactionsPlugin.getInstance().getFileManager().getDiscord().fetchBoolean("Discord.Guild.leaderRoles") && this.role == Role.LEADER && event.getTo() != Role.LEADER) {
                        Discord.mainGuild.removeRoleFromMember(m, Discord.mainGuild.getRoleById(FactionsPlugin.getInstance().getFileManager().getDiscord().fetchString("Discord.Guild.leaderRoleID"))).queue();
                    }
                    if (FactionsPlugin.getInstance().getFileManager().getDiscord().fetchBoolean("Discord.Guild.leaderRoles") && event.getTo() == Role.LEADER) {
                        Discord.mainGuild.addRoleToMember(m, Discord.mainGuild.getRoleById(FactionsPlugin.getInstance().getFileManager().getDiscord().fetchString("Discord.Guild.leaderRoleID"))).queue();
                    }
                    this.role = event.getTo();
                    if (FactionsPlugin.getInstance().getFileManager().getDiscord().fetchBoolean("Discord.Guild.factionDiscordTags")) {
                        Discord.mainGuild.modifyNickname(m, Discord.getNicknameString(this)).queue();
                    }
                } else {
                    this.role = event.getTo();
                }
            } catch (HierarchyException e) {
                Logger.print(e.getMessage(), Logger.PrefixType.FAILED);
            }
        }
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public FLocation getLastStoodAt() {
        return this.lastStoodAt;
    }

    public String getTitle() {
        return this.getHasFaction() ? title : TL.NOFACTION_PREFIX.toString();
    }

    public String getName() {
        if (this.name == null) {
            // Older versions of FactionsUUID don't save the name,
            // so `name` will be null the first time it's retrieved
            // after updating
            OfflinePlayer offline = Bukkit.getOfflinePlayer(UUID.fromString(getId()));
            this.name = offline.getName() != null ? offline.getName() : getId();
        }
        return name;
    }

    public String getTag() {
        return this.getHasFaction() ? this.getFaction().getTag() : "";
    }

    // Colored concatenations:
    // These are used in information messages

    public String getNameAndSomething(String something) {
        String ret = this.role.getPrefix();
        if (something.length() > 0) ret += something + " ";
        ret += this.getName();
        return ret;
    }

    public String getNameAndTitle() {
        return this.getNameAndSomething(this.getTitle());
    }

    // Chat Tag:
    // These are injected into the format of global chat messages.

    public String getNameAndTag() {
        return this.getNameAndSomething(this.getTag());
    }

    public String getNameAndTitle(IFaction faction) {
        return this.getColorTo(faction) + this.getNameAndTitle();
    }

    public String getNameAndTitle(MemoryFPlayer fplayer) {
        return this.getColorTo(fplayer) + this.getNameAndTitle();
    }

    public String getChatTag() {
        return this.getHasFaction() ? String.format(Conf.chatTagFormat, this.getRole().getPrefix() + this.getTag()) : TL.NOFACTION_PREFIX.toString();
    }

    // Colored Chat Tag
    public String getChatTag(IFaction faction) {
        return this.getHasFaction() ? this.getRelationTo(faction).getColor() + getChatTag() : "";
    }

    // -------------------------------
    // Relation and relation colors
    // -------------------------------

    public String getChatTag(MemoryFPlayer fplayer) {
        return this.getHasFaction() ? this.getColorTo(fplayer) + getChatTag() : "";
    }

    public int getKills() {
        return isOnline() ? getPlayer().getStatistic(Statistic.PLAYER_KILLS) : this.kills;
    }

    public int getDeaths() {
        return isOnline() ? getPlayer().getStatistic(Statistic.DEATHS) : this.deaths;
    }

    public Relation getRelationToLocation() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this);
    }

    public double getPower() {
        this.updatePower();
        return this.power;
    }

    public double getPowerMax() {
        return Conf.powerPlayerMax + this.powerBoost;
    }

    public double getPowerMin() {
        return Conf.powerPlayerMin + this.powerBoost;
    }

    public int getPowerRounded() {
        return (int) Math.round(this.getPower());
    }

    public int getPowerMaxRounded() {
        return (int) Math.round(this.getPowerMax());
    }

    public int getPowerMinRounded() {
        return (int) Math.round(this.getPowerMin());
    }

    public long getMillisPassed() {
        return this.millisPassed;
    }

    public long getLastPowerUpdateTime() {
        return this.lastPowerUpdateTime;
    }

    public String getNameAndTitle(IFactionPlayer fplayer) {
        return this.getColorTo(fplayer) + this.getNameAndTitle();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(UUID.fromString(this.getId()));
    }

    public boolean getHasEnemiesNearby() {
        return this.enemiesNearby;
    }

    public boolean getHasTitlesEnabled() {
        return this.titlesEnabled;
    }

    public boolean getHasFaction() {
        return !factionId.equals("0");
    }

    // endregion

    // region Setters

    public void setCooldown(String cmd, long cooldown) {
        if (this.getPlayer().isOp())
            return;

        this.commandCooldown.put(cmd, cooldown);
    }

    public void setFaction(IFaction faction, boolean alt) {
        IFaction oldFaction = this.getFaction();
        if (oldFaction != null) {
            oldFaction.removeFPlayer(this);
        }
        if (alt) faction.addAltPlayer(this);
        else faction.addFPlayer(this);
        this.factionId = faction.getId();
    }

    public void setEnemiesNearby(Boolean b) {
        this.enemiesNearby = b;
    }

    public void setTitlesEnabled(Boolean b) {
        this.titlesEnabled = b;
    }

    public void setMonitorJoins(boolean monitor) {
        this.monitorJoins = monitor;
    }

    public void setPowerBoost(double powerBoost) {
        this.powerBoost = powerBoost;
    }

    public void setChatMode(ChatMode chatMode) {
        this.chatMode = chatMode;
    }

    public void setIgnoreAllianceChat(boolean ignore) {
        this.ignoreAllianceChat = ignore;
    }

    public void setLastLoginTime(long lastLoginTime) {
//        losePowerFromBeingOffline();
        this.lastLoginTime = lastLoginTime;
        this.lastPowerUpdateTime = lastLoginTime;
//        if (Conf.noPVPDamageToOthersForXSecondsAfterLogin > 0) this.loginPvpDisabled = true;
    }

    public void setInFactionsChest(boolean b) {
        inChest = b;
    }

    public void setInVault(boolean status) {
        inVault = status;
    }

    public void setSeeingChunk(boolean seeingChunk) {
        this.seeingChunk = seeingChunk;
    }

    public void setPowerRounded(int power) {
        this.power = power;
    }

    //----------------------------------------------//
    // Title, Name, Faction Tag and Chat
    //----------------------------------------------//

    // Base:
//    public boolean hasLoginPvpDisabled() {
//        if (!loginPvpDisabled) return false;
//        if (this.lastLoginTime + (Conf.noPVPDamageToOthersForXSecondsAfterLogin * 1000L) < System.currentTimeMillis()) {
//            this.loginPvpDisabled = false;
//            return false;
//        }
//        return true;
//    }
    public void setLastStoodAt(FLocation flocation) {
        this.lastStoodAt = flocation;
    }

    public void setTitle(CommandSender sender, String title) {
        // Check if the setter has it.
        if (sender.hasPermission(Permission.TITLE_COLOR.node))
            title = ChatColor.translateAlternateColorCodes('&', title);
        this.title = title;
    }

    // Base concatenations:
    public void setName(String name) {
        this.name = name;
    }

    public void setFriendlyFire(boolean status) {
        friendlyFire = status;
    }

    // endregion
}