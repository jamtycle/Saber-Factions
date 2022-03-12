package com.massivecraft.factions;

import com.massivecraft.factions.iface.IRelationParticipator;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import mkremins.fanciful.FancyMessage;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;


/**
 * Logged in players always have exactly one FPlayer instance. Logged out players may or may not have an FPlayer
 * instance. They will always have one if they are part of a faction. This is because only players with a faction are
 * saved to disk (in order to not waste disk space).
 * The FPlayer is linked to a minecraft player using the player name.
 * The same instance is always returned for the same player. This means you can use the == operator. No .equals method
 * necessary.
 */

/* *
Bruno Ramirez:
This interface is not necessary if there is going to be only one "Child" Classes extending from it.
This kind of interface would make sense ONLY if there was more than one type of players ex:
    - JSONPlayer - Using JSON serialization system.
    - MYSQLPlayer - Using a DB system.
Since this is not the case and this projects aims only for MYSQL system, I'm dropping this interface
    and making the Player just extend from RelationParticipator, and it's probable that I drop that as well.
* */

public interface IFactionPlayer extends IRelationParticipator {

    void setNotificationsEnabled(boolean notifications);

    boolean hasNotificationsEnabled();

    /**
     * Determine if a player has enemies nearby based on the enemy check task in CmdFly
     * NOTE: THIS VALUE IS ONLY UPDATED WHEN A USER IS USING FLY
     *
     * @return enemiesNearby as a boolean
     */
    boolean getHasEnemiesNearby();

    /**
     * Set if this FPlayer has an enemy nearby
     *
     * @param b enemiesNearby
     */
    void setEnemiesNearby(Boolean b);

    /**
     * Get if a player has setup their Discord before
     *
     * @return if the player setup Discord as a boolean
     */
    boolean discordSetup();

    /**
     * Get the players Discord user ID
     *
     * @return players Discord user ID as a String
     */
    String discordUserID();

    /**
     * Set the players Boolean defining if the player has setup their Discord
     *
     * @param b Boolean for discordSetup to be defined to
     */
    void setDiscordSetup(Boolean b);

    /**
     * Set the players Discord user ID
     *
     * @param s String for their user ID to be set to
     */
    void setDiscordUserID(String s);

    /**
     * Get the players Discord user (If the player has not setup Discord it will return null!)
     *
     * @return User from players set Discord User ID
     */
    User discordUser();

    /**
     * Used to check if this player should be served titles
     *
     * @return if this FPlayer has titles enabled as a boolean
     */
    boolean getHasTitlesEnabled();

    /**
     * Used to set if player should be served titles
     *
     * @param b Boolean to titlesEnabled to
     */
    void setTitlesEnabled(Boolean b);

    /**
     * Used to determine if a player is in their faction's chest
     *
     * @return if player is in their faction's as a boolean
     */
    boolean isInFactionsChest();

    /**
     * Set if the player is inside of their faction's chest
     */
    void setInFactionsChest(boolean b);

    /**
     * Sets the kills and deaths of a player.
     */
    void login();

    /**
     * Caches the kills and deaths of a player.
     */
    void logout();

    /**
     * gets the faction of a FPlayer.
     *
     * @return Faction of the FPlayer.
     */
    IFaction getFaction();

    /**
     * Sets the faction of the FPlayer
     *
     * @param faction faction to set.
     */
    void setFaction(IFaction faction, boolean alt);

    /**
     * Gets the faction ID of the player.
     *
     * @return FactionsID string
     */
    String getFactionId();

    /**
     * Check if a player has a faction
     *
     * @return boolean
     */
    boolean getHasFaction();

    void setMonitorJoins(boolean monitor);

    boolean isMonitoringJoins();

    Role getRole();

    void setRole(Role role);

    double getPowerBoost();

    void setPowerBoost(double powerBoost);

    boolean isVanished();

    ChatMode getChatMode();

    void setChatMode(ChatMode chatMode);

    boolean isIgnoreAllianceChat();

    void setIgnoreAllianceChat(boolean ignore);

    boolean showScoreboard();

    void setShowScoreboard(boolean show);

    // FIELD: account
    String getAccountId();

    void resetFactionData(boolean doSpoutUpdate);

    void resetFactionData();

    long getLastLoginTime();

    void setLastLoginTime(long lastLoginTime);

//    boolean hasLoginPvpDisabled();

    FLocation getLastStoodAt();

    void setLastStoodAt(FLocation flocation);

    String getTitle();

    void setTitle(CommandSender sender, String title);

    String getName();

    String getTag();

    // Base concatenations:

    String getNameAndSomething(String something);

    String getNameAndTitle();

    String getNameAndTag();

    // Colored concatenations:
    // These are used in information messages

    String getNameAndTitle(IFaction faction);

    String getNameAndTitle(IFactionPlayer fplayer);

    // Chat Tag:
    // These are injected into the format of global chat messages.

    String getChatTag();

    // Colored Chat Tag
    String getChatTag(IFaction faction);

    String getChatTag(IFactionPlayer fplayer);

    int getKills();

    int getDeaths();

    //Fplayer specific friendly fire.
    boolean hasFriendlyFire();

    void setFriendlyFire(boolean status);

    //inspect Stuff

    boolean isInspectMode();

    void setInspectMode(boolean status);

    // -------------------------------
    // Relation and relation colors
    // -------------------------------

    @Override
    String describeTo(IRelationParticipator that, boolean ucfirst);

    @Override
    String describeTo(IRelationParticipator that);

    @Override
    Relation getRelationTo(IRelationParticipator rp);

    @Override
    Relation getRelationTo(IRelationParticipator rp, boolean ignorePeaceful);

    Relation getRelationToLocation();

    @Override
    ChatColor getColorTo(IRelationParticipator rp);


    String getRolePrefix();

    //----------------------------------------------//
    // Health
    //----------------------------------------------//
    void heal(int amnt);

    //----------------------------------------------//
    // Power
    //----------------------------------------------//
    double getPower();

    void alterPower(double delta);

    double getPowerMax();

    double getPowerMin();

    int getPowerRounded();

    void setPowerRounded(int power);

    int getPowerMaxRounded();

    int getPowerMinRounded();

    long getMillisPassed();

    long getLastPowerUpdateTime();

    void updatePower();

    void onDeath();

    //----------------------------------------------//
    // Territory
    //----------------------------------------------//
    boolean isInOwnTerritory();

    boolean isInOthersTerritory();

    boolean isInAllyTerritory();

    boolean isInNeutralTerritory();

    boolean isInEnemyTerritory();

    void sendFactionHereMessage(IFaction from);

    // -------------------------------
    // Actions
    // -------------------------------

    void leave();

    boolean canClaimForFaction(IFaction forFaction);

    boolean canClaimForFactionAtLocation(IFaction forFaction, Location location, boolean notifyFailure);

    boolean canClaimForFactionAtLocation(IFaction forFaction, FLocation location, boolean notifyFailure);

    boolean attemptClaim(IFaction forFaction, Location location, boolean notifyFailure);

    boolean attemptClaim(IFaction forFaction, FLocation location, boolean notifyFailure);

    boolean isInVault();

    void setInVault(boolean status);

    void msg(String str, Object... args);

    String getId();

    void setId(String id);

    Player getPlayer();

    boolean isOnline();

    void sendMessage(String message);

    void sendMessage(List<String> messages);

    void sendFancyMessage(FancyMessage message);

    void sendFancyMessage(List<FancyMessage> message);

    boolean isOnlineAndVisibleTo(Player me);

    void remove();

    boolean isOffline();

    boolean isSeeingChunk();

    void setSeeingChunk(boolean seeingChunk);

    boolean isEnteringPassword();

    void checkIfNearbyEnemies();

    int getCooldown(String cmd);

    void setCooldown(String cmd, long cooldown);

    boolean isCooldownEnded(String cmd);
}