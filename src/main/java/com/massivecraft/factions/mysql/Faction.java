package com.massivecraft.factions.mysql;

import com.massivecraft.factions.*;
import com.massivecraft.factions.iface.IRelationParticipator;
import com.massivecraft.factions.missions.Mission;
import com.massivecraft.factions.mysql.abstracts.DBConnection;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.RelationUtil;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.Permissable;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Faction extends DBConnection implements IRelationParticipator {

    // region Variables

    private int id_faction;
    private String faction_name;
    // Probably change this to an int lately
    private String faction_god;
    private int id_player_faction_king;
    private int faction_kills;
    private int faction_karma;
    private int faction_power;

    private String faction_tag;
    private String description;

    // inherit from MemoryFaction

    protected transient Set<FactionPlayer> faction_players = new HashSet<>();

    protected Map<Integer, Relation> relationWish = new HashMap<>();
    protected Map<FLocation, Set<String>> claimOwnership = new ConcurrentHashMap<>();
    protected Map<Permissable, Map<PermissableAction, Access>> permissions = new HashMap<>();

    protected Set<String> invites = new HashSet<>();
    protected HashMap<String, List<String>> announcements = new HashMap<>();

    protected Role defaultRole;

    Inventory chest;

    private Map<String, Mission> missions = new ConcurrentHashMap<>();
    private List<String> completedMissions;

    private String factionChatChannelId;

    private String notifyFormat;

    // endregion

    public Faction(FactionsPlugin _plugin, int _id_faction) {
        super(_plugin);

        ResultSet faction_info = this.getResultSet("GET_FACTION(?)", _id_faction);
        if (faction_info == null) return;

        if (!BuildCLass(faction_info)) {
            plugin.getLogger().info("Faction with ID: " + _id_faction + " could not be build.");
        }
    }

    public Faction(FactionsPlugin _plugin, Map<String, Object> _values) {
        super(_plugin);

        if (!BuildCLass(_values)) {
            plugin.getLogger().info("Faction with ID: " + _values.get(0) + " could not be build.");
        }
    }

    // region IRelationParticipator Overrides
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
    public void msg(String message, Object... args) {
        message = FactionsPlugin.getInstance().txt.parse(message, args);
        for (FactionPlayer fplayer : this.getFPlayersWhereOnline(true)) fplayer.sendMessage(message);
    }

    @Override
    public void msg(TL translation, Object... args) {
        msg(translation.toString(), args);
    }

    public Relation getRelationWish(IFaction otherFaction) {
        if (this.relationWish.containsKey(otherFaction.getId())) {
            return this.relationWish.get(otherFaction.getId());
        }
        return Relation.fromString(FactionsPlugin.getInstance().getConfig().getString("default-relation", "neutral")); // Always default to old behavior.
    }

    public void setRelationWish(Faction otherFaction, Relation relation) {
        if (this.relationWish.containsKey(otherFaction.getId_faction()) && relation.equals(Relation.NEUTRAL)) {
            this.relationWish.remove(otherFaction.getId_faction());
        } else {
            this.relationWish.put(otherFaction.getId_faction(), relation);
        }
    }

    public int getRelationCount(Relation relation) {
        int count = 0;
        for (IFaction faction : Factions.getInstance().getAllFactions()) {
            if (faction.getRelationTo(this) == relation) {
                count++;
            }
        }
        return count;
    }

    // endregion

    // region Validators

    // TODO: Rename this method to isPlayableFaction
    public boolean isNormal() {
        return true;
//        return !(this.isWilderness() || this.isSafeZone() || this.isWarZone());
    }

    // endregion

    // region Claims

    public void clearAllClaimOwnership() {
        claimOwnership.clear();
    }

    public void clearClaimOwnership(FLocation loc) {
        claimOwnership.remove(loc);
    }

    public void clearClaimOwnership(FactionPlayer player) {
        if (this.isNormal()) return;
        Set<String> ownerData;

        for (Map.Entry<FLocation, Set<String>> entry : claimOwnership.entrySet()) {
            ownerData = entry.getValue();
            if (ownerData == null) continue;
            ownerData.removeIf(s -> s.equals(player.getPlayer_UUID()));
            if (ownerData.isEmpty()) claimOwnership.remove(entry.getKey());
        }
    }

    // endregion

    // region Tags in reference to others

    public String getTag(String prefix) {
        return prefix + this.faction_tag;
    }

    public String getTag(IFaction otherFaction) {
        if (otherFaction == null) {
            return getFaction_tag();
        }
        return this.getTag(this.getColorTo(otherFaction).toString());
    }

    public String getTag(FactionPlayer otherFplayer) {
        if (otherFplayer == null) {
            return getFaction_tag();
        }
        return this.getTag(this.getColorTo(otherFplayer).toString());
    }

    // endregion

    // region Getter & Setters

    public int getId_faction() {
        return id_faction;
    }

    public void setId_faction(int id_faction) {
        this.id_faction = id_faction;
    }

    public String getFaction_name() {
        return faction_name;
    }

    public void setFaction_name(String faction_name) {
        this.faction_name = faction_name;
    }

    public String getFaction_god() {
        return faction_god;
    }

    public void setFaction_god(String faction_god) {
        this.faction_god = faction_god;
    }

    public int getId_player_faction_king() {
        return id_player_faction_king;
    }

    public void setId_player_faction_king(int id_player_faction_king) {
        this.id_player_faction_king = id_player_faction_king;
    }

    public int getFaction_kills() {
        return faction_kills;
    }

    public void setFaction_kills(int faction_kills) {
        this.faction_kills = faction_kills;
    }

    public int getFaction_karma() {
        return faction_karma;
    }

    public void setFaction_karma(int faction_karma) {
        this.faction_karma = faction_karma;
    }

    public int getFaction_power() {
        return faction_power;
    }

    public void setFaction_power(int faction_power) {
        this.faction_power = faction_power;
    }

    // **************************** inherit from MemoryFaction ****************************

    public Access getAccess(Permissable permissable, PermissableAction permissableAction) {
        if (permissable == null || permissableAction == null) {
            return Access.UNDEFINED;
        }

        Map<PermissableAction, Access> accessMap = permissions.get(permissable);
        if (accessMap != null && accessMap.containsKey(permissableAction)) {
            return accessMap.get(permissableAction);
        }

        return Access.UNDEFINED;
    }

    /**
     * Get the Access of a player. Will use player's Role if they are a faction member. Otherwise, uses their Relation.
     *
     * @param player
     * @param permissableAction
     * @return
     */
    public Access getAccess(FactionPlayer player, PermissableAction permissableAction) {
        if (player == null || permissableAction == null) return Access.UNDEFINED;
        if (player.getFaction() == this && player.getRole() == Role.LEADER) return Access.ALLOW;

        Permissable perm = player.getFaction() == this ? player.getRole() : player.getFaction().getRelationTo(this);

        Map<PermissableAction, Access> accessMap = permissions.get(perm);
        if (accessMap != null && accessMap.containsKey(permissableAction)) return accessMap.get(permissableAction);

        return Access.UNDEFINED;
    }

    /**
     * Read only map of Permissions.
     *
     * @return
     */
    public Map<Permissable, Map<PermissableAction, Access>> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    public Role getDefaultRole() {
        return this.defaultRole;
    }

    public int getPowerRounded() {
        return (int) Math.round(this.getFaction_power());
    }

    public int getLandRounded() {
        return Board.getInstance().getFactionCoordCount(this);
    }

    public int getLandRoundedInWorld(String worldName) {
        return Board.getInstance().getFactionCoordCountInWorld(this, worldName);
    }

    public int getSize() {
        return faction_players.size();
    }

    public FactionPlayer[] getFaction_Players() {
        // uhmmm idk, if you need to get the players but also don't want to modify them... then
        // why do you need it in first place? Possible ReadOnly List bad usage here.
        // return a shallow copy of the FPlayer list, to prevent tampering and
        // concurrency issues
        return (FactionPlayer[]) faction_players.toArray();
    }

    public FactionPlayer[] getFPlayersWhereOnline(boolean _online) {
        // Much better.
        return (FactionPlayer[]) faction_players.stream().filter(x -> x.isOnline() == _online).toArray();

//        Set<FactionPlayer> ret = new HashSet<>();
//
//        for (FactionPlayer fplayer : fplayers)
//            if (fplayer.isOnline() == online) ret.add(fplayer);
//
//        return ret;
    }

    public FactionPlayer[] getFPlayersWhereOnline(boolean _online, FactionPlayer _viewer) {

        if (!this.isNormal()) return new FactionPlayer[0];

        return (FactionPlayer[]) faction_players.stream().filter(x -> {
            if (x.isOnline() == _online) {
                if (_online && _viewer.getPlayer().canSee(x.getPlayer())) {
                    return true;
                } else return !_online;
            } else return false;
        }).toArray();


//        Set<FactionPlayer> ret = new HashSet<>();
////        if (!this.isNormal()) return ret;
//        for (FactionPlayer viewed : fplayers) {
//            // Add if their online status is what we want
//            if (viewed.isOnline() == online) {
//                // If we want online, check to see if we are able to see this player
//                // This checks if they are in vanish.
//                if (online
//                        && viewed.getPlayer() != null
//                        && viewer.getPlayer() != null
//                        && viewer.getPlayer().canSee(viewed.getPlayer())) {
//                    ret.add(viewed);
//                    // If we want offline, just add them.
//                    // Prob a better way to do this but idk.
//                } else if (!online) {
//                    ret.add(viewed);
//                }
//            }
//        }
//        return ret;
    }


    public FactionPlayer getFPlayerAdmin() {
        // If there are a PlayerManager maybe is a good idea to actually CALL
        // that manager and bring the player from there.

        if (!this.isNormal()) return null;

        Optional<FactionPlayer> admin = faction_players.stream().filter(x -> x.getId_faction() == id_player_faction_king).findFirst();
        return admin.orElse(null);
    }

    public FactionPlayer getFPlayerLeader() {
        return getFPlayerAdmin();
    }

    public FactionPlayer[] getFPlayersWhereRole(Role _role) {

        if (!this.isNormal()) return new FactionPlayer[0];

        return (FactionPlayer[]) faction_players.stream().filter(x -> x.getRole() == _role).toArray();
//
//        for (FactionPlayer fplayer : fplayers)
//            if (fplayer.getRole() == role) ret.add(fplayer);
//        return ret;
    }

    public Player[] getOnlinePlayers() {

        return (Player[]) plugin.getServer().getOnlinePlayers().stream()
                .map(x -> FactionPlayersManagerBase.getInstance().getByPlayer(x))
                .filter(x -> x.getFaction() == this).toArray();


//        ArrayList<Player> ret = new ArrayList<>();
//
//        for (Player player : FactionsPlugin.getInstance().getServer().getOnlinePlayers()) {
//            FactionPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
//            if (fplayer.getFaction() == this && !fplayer.isAlt()) {
//                ret.add(player);
//            }
//        }
//        return ret;
    }

    public Map<FLocation, Set<String>> getClaimOwnership() {
        return claimOwnership;
    }

    public Map<String, Mission> getMissions() {
        return this.missions;
    }

    public List<String> getCompletedMissions() {
        return this.completedMissions;
    }

    public int getCountOfClaimsWithOwners() {
        return claimOwnership.isEmpty() ? 0 : claimOwnership.size();
    }

    public Set<String> getOwnerList(FLocation loc) {
        return claimOwnership.get(loc);
    }

    public String getOwnerListString(FLocation loc) {
        Set<String> ownerData = claimOwnership.get(loc);
        if (ownerData == null || ownerData.isEmpty()) return "";
        StringBuilder ownerList = new StringBuilder();

        for (String anOwnerData : ownerData) {
            if (ownerList.length() > 0) ownerList.append(", ");
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(anOwnerData));
            ownerList.append(offlinePlayer != null ? offlinePlayer.getName() : TL.GENERIC_NULLPLAYER.toString());
        }
        return ownerList.toString();
    }

    public Set<FLocation> getAllClaims() {
        return Board.getInstance().getAllClaims(this);
    }

    public String getFaction_tag() {
        return faction_tag;
    }

    public void setFaction_tag(String faction_tag) {
        this.faction_tag = faction_tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // endregion
}
