package com.massivecraft.factions;

import com.massivecraft.factions.event.FactionDisbandEvent.PlayerDisbandReason;
import com.massivecraft.factions.iface.IRelationParticipator;
import com.massivecraft.factions.missions.Mission;
import com.massivecraft.factions.mysql.FactionPlayer;
import com.massivecraft.factions.struct.BanInfo;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.FastChunk;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.Permissable;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.massivecraft.factions.zcore.frame.fupgrades.UpgradeType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public interface IFaction extends IRelationParticipator {

    String getMemberRoleId();

    void setMemberRoleId(String roleId);

    String getGuildId();

    void setGuildId(String id);

    String getWallNotifyChannelId();

    void setWallNotifyChannelId(String channelId);

    String getBufferNotifyChannelId();

    void setBufferNotifyChannelId(String channelId);

    String getNotifyFormat();

    void setNotifyFormat(String format);

    String getFactionChatChannelId();

    void setFactionChatChannelId(String channelId);

    String getDiscord();

    void setDiscord(String link);

    void checkPerms();

    int getWallCheckMinutes();

    void setWallCheckMinutes(int minutes);

    int getBufferCheckMinutes();

    void setBufferCheckMinutes(int minutes);

    Map<Long, String> getChecks();

    Map<UUID, Integer> getPlayerBufferCheckCount();

    Map<UUID, Integer> getPlayerWallCheckCount();

    boolean altInvited(FactionPlayer fplayer);

    Map<String, Mission> getMissions();

    List<String> getCompletedMissions();

    Set<FastChunk> getSpawnerChunks();

    void setSpawnerChunks(Set<FastChunk> fastChunks);

    void clearSpawnerChunks();

    int getSpawnerChunkCount();

    int getAllowedSpawnerChunks();

    void setAllowedSpawnerChunks(int chunks);

    boolean isProtected();

    void setProtected(boolean b);

    void deinviteAlt(FactionPlayer alt);

    void deinviteAllAlts();

    void altInvite(FactionPlayer fplayer);

    boolean addAltPlayer(FactionPlayer fplayer);

    boolean removeAltPlayer(FactionPlayer fplayer);

    Set<FactionPlayer> getAltPlayers();

    HashMap<String, List<String>> getAnnouncements();

    int getMaxVaults();

    void setMaxVaults(int value);

    void addAnnouncement(FactionPlayer fPlayer, String msg);

    void sendUnreadAnnouncements(FactionPlayer fPlayer);

    void removeAnnouncements(FactionPlayer fPlayer);

    Set<String> getInvites();

    String getFocused();

    void setFocused(String setFocused);

    String getId();

    void setId(String id);

    void invite(FactionPlayer fplayer);

    void deinvite(FactionPlayer fplayer);

    void setUpgrade(UpgradeType upgrade, int level);

    int getUpgrade(UpgradeType upgrade);

    boolean isInvited(FactionPlayer fplayer);

    void ban(FactionPlayer target, FactionPlayer banner);

    int getPoints();

    void setPoints(int points);

    int getStrikes();

    void setStrikes(int strikes);

    void unban(FactionPlayer player);

    boolean isBanned(FactionPlayer player);

    Set<BanInfo> getBannedPlayers();

    HashMap<Integer, String> getRulesMap();

    void addRule(String rule);

    void removeRule(int index);

    void clearRules();

    Location getCheckpoint();

    void setCheckpoint(Location location);

    void addTnt(int amt);

    void takeTnt(int amt);

    Location getVault();

    void setVault(Location vaultLocation);

    Inventory getChestInventory();

    void setChestSize(int chestSize);

    void setBannerPattern(ItemStack banner);

    ItemStack getBanner();

    int getTnt();

    void setTnt(int amount);

    String getRule(int index);

    boolean getOpen();

    void setOpen(boolean isOpen);

    boolean isPeaceful();

    void setPeaceful(boolean isPeaceful);

    boolean getPeacefulExplosionsEnabled();

    void setPeacefulExplosionsEnabled(boolean val);

    boolean noExplosionsInTerritory();

    boolean isPermanent();

    void setPermanent(boolean isPermanent);

    String getTag();

    void setTag(String str);

    String getTag(String prefix);

    String getTag(IFaction otherFaction);

    String getTag(FactionPlayer otherFplayer);

    String getComparisonTag();

    String getDescription();

    void setDescription(String value);

    boolean hasHome();

    Location getHome();

    void setHome(Location home);

    void deleteHome();

    long getFoundedDate();

    void setFoundedDate(long newDate);

    void confirmValidHome();

    String getAccountId();

    Integer getPermanentPower();

    void setPermanentPower(Integer permanentPower);

    boolean hasPermanentPower();

    double getPowerBoost();

    void setPowerBoost(double powerBoost);

    boolean noPvPInTerritory();

    boolean noMonstersInTerritory();

    boolean isNormal();

    boolean isSystemFaction();

    @Deprecated
    boolean isNone();

    boolean isWilderness();

    boolean isSafeZone();

    boolean isWarZone();

    boolean isPlayerFreeType();

    boolean isPowerFrozen();

    void setLastDeath(long time);

    int getKills();

    int getDeaths();

    Access getAccess(Permissable permissable, PermissableAction permissableAction);

    Access getAccess(FactionPlayer player, PermissableAction permissableAction);

    boolean setPermission(Permissable permissable, PermissableAction permissableAction, Access access);

    void resetPerms();

    void setDefaultPerms();

    void disband(Player disbander);

    void disband(Player disbander, PlayerDisbandReason reason);

    // -------------------------------
    // Relation and relation colors
    // -------------------------------

    Map<Permissable, Map<PermissableAction, Access>> getPermissions();

    @Override
    String describeTo(IRelationParticipator that, boolean ucfirst);

    @Override
    String describeTo(IRelationParticipator that);

    @Override
    Relation getRelationTo(IRelationParticipator rp);

    @Override
    Relation getRelationTo(IRelationParticipator rp, boolean ignorePeaceful);

    @Override
    ChatColor getColorTo(IRelationParticipator rp);

    Relation getRelationWish(IFaction otherFaction);

    void setRelationWish(IFaction otherFaction, Relation relation);

    int getRelationCount(Relation relation);

    // ----------------------------------------------//
    // Power
    // ----------------------------------------------//
    double getPower();

    double getPowerMax();

    int getPowerRounded();

    int getPowerMaxRounded();

    int getLandRounded();

    int getLandRoundedInWorld(String worldName);

    // -------------------------------
    // FPlayers
    // -------------------------------

    boolean hasLandInflation();

    // maintain the reference list of FPlayers in this faction
    void refreshFPlayers();

    boolean addFPlayer(FactionPlayer fplayer);

    boolean removeFPlayer(FactionPlayer fplayer);

    int getSize();

    Set<FactionPlayer> getFPlayers();

    Set<FactionPlayer> getFPlayersWhereOnline(boolean online);

    Set<FactionPlayer> getFPlayersWhereOnline(boolean online, FactionPlayer viewer);

    FactionPlayer getFPlayerAdmin();

    FactionPlayer getFPlayerLeader();

    ArrayList<FactionPlayer> getFPlayersWhereRole(Role role);

    ArrayList<Player> getOnlinePlayers();

    // slightly faster check than getOnlinePlayers() if you just want to see if
    // there are any players online
    boolean hasPlayersOnline();

    void memberLoggedOff();

    // used when current leader is about to be removed from the faction;
    // promotes new leader, or disbands faction if no other members left
    void promoteNewLeader();

    void promoteNewLeader(boolean autoLeave);

    Role getDefaultRole();

    void setDefaultRole(Role role);

    // ----------------------------------------------//
    // Messages
    // ----------------------------------------------//
    void msg(String message, Object... args);

    void sendMessage(String message);

    // ----------------------------------------------//
    // Ownership of specific claims
    // ----------------------------------------------//

    void sendMessage(List<String> messages);

    Map<FLocation, Set<String>> getClaimOwnership();

    void clearAllClaimOwnership();

    void clearClaimOwnership(FLocation loc);

    void clearClaimOwnership(FactionPlayer player);

    int getCountOfClaimsWithOwners();

    boolean doesLocationHaveOwnersSet(FLocation loc);

    boolean isPlayerInOwnerList(FactionPlayer player, FLocation loc);

    void setPlayerAsOwner(FactionPlayer player, FLocation loc);

    void removePlayerAsOwner(FactionPlayer player, FLocation loc);

    Set<String> getOwnerList(FLocation loc);

    String getOwnerListString(FLocation loc);

    boolean playerHasOwnershipRights(FactionPlayer fplayer, FLocation loc);

    // ----------------------------------------------//
    // Persistance and entity management
    // ----------------------------------------------//
    void remove();

    Set<FLocation> getAllClaims();

    // -------------------------------
    // Shields
    // -------------------------------
}
