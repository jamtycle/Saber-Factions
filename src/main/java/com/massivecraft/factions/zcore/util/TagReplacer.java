package com.massivecraft.factions.zcore.util;

import com.massivecraft.factions.*;
import com.massivecraft.factions.mysql.Faction;
import com.massivecraft.factions.mysql.FactionPlayer;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.util.timer.TimerManager;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Link between config and in-game messages<br> Changes based on faction / player<br> Interfaces the config lists with
 * {} variables to plugin
 */
public enum TagReplacer {

    /**
     * Fancy variables, used by f show
     */
    NEUTRAL_LIST(TagType.FANCY, "{neutral-list}"),
    ALLIES_LIST(TagType.FANCY, "{allies-list}"),
    ONLINE_LIST(TagType.FANCY, "{online-list}"),
    ENEMIES_LIST(TagType.FANCY, "{enemies-list}"),
    TRUCES_LIST(TagType.FANCY, "{truces-list}"),
    OFFLINE_LIST(TagType.FANCY, "{offline-list}"),
    ALTS(TagType.FANCY, "{alts}"),

    /**
     * Player variables, require a player
     */
    PLAYER_GROUP(TagType.PLAYER, "{group}"),
    LAST_SEEN(TagType.PLAYER, "{lastSeen}"),
    PLAYER_BALANCE(TagType.PLAYER, "{balance}"),
    PLAYER_POWER(TagType.PLAYER, "{player-power}"),
    PLAYER_MAXPOWER(TagType.PLAYER, "{player-maxpower}"),
    PLAYER_KILLS(TagType.PLAYER, "{player-kills}"),
    PLAYER_DEATHS(TagType.PLAYER, "{player-deaths}"),

    /**
     * Faction variables, require at least a player
     */
    CHUNKS(TagType.FACTION, "{chunks}"),
    HEADER(TagType.FACTION, "{header}"),
    POWER(TagType.FACTION, "{power}"),
    MAX_POWER(TagType.FACTION, "{maxPower}"),
    POWER_BOOST(TagType.FACTION, "{power-boost}"),
    LEADER(TagType.FACTION, "{leader}"),
    JOINING(TagType.FACTION, "{joining}"),
    FACTION(TagType.FACTION, "{faction}"),
    PLAYER_NAME(TagType.FACTION, "{name}"),
    HOME_WORLD(TagType.FACTION, "{world}"),
    RAIDABLE(TagType.FACTION, "{raidable}"),
    PEACEFUL(TagType.FACTION, "{peaceful}"),
    PERMANENT(TagType.FACTION, "permanent"), // no braces needed
    TIME_LEFT(TagType.FACTION, "{time-left}"),
    LAND_VALUE(TagType.FACTION, "{land-value}"),
    DESCRIPTION(TagType.FACTION, "{description}"),
    CREATE_DATE(TagType.FACTION, "{create-date}"),
    LAND_REFUND(TagType.FACTION, "{land-refund}"),
    BANK_BALANCE(TagType.FACTION, "{faction-balance}"),
    ALLIES_COUNT(TagType.FACTION, "{allies}"),
    ENEMIES_COUNT(TagType.FACTION, "{enemies}"),
    TRUCES_COUNT(TagType.FACTION, "{truces}"),
    ALT_COUNT(TagType.FACTION, "{alt-count}"),
    ONLINE_COUNT(TagType.FACTION, "{online}"),
    OFFLINE_COUNT(TagType.FACTION, "{offline}"),
    FACTION_SIZE(TagType.FACTION, "{members}"),
    FACTION_KILLS(TagType.FACTION, "{faction-kills}"),
    FACTION_DEATHS(TagType.FACTION, "{faction-deaths}"),
    FACTION_BANCOUNT(TagType.FACTION, "{faction-bancount}"),
    FACTION_STRIKES(TagType.FACTION, "{strikes}"),


    /**
     * General variables, require no faction or player
     */
    GRACE_TIMER(TagType.GENERAL, "{grace-time}"),
    MAX_ALLIES(TagType.GENERAL, "{max-allies}"),
    MAX_ALTS(TagType.GENERAL, "{max-alts}"),
    MAX_ENEMIES(TagType.GENERAL, "{max-enemies}"),
    MAX_TRUCES(TagType.GENERAL, "{max-truces}"),
    FACTIONLESS(TagType.GENERAL, "{factionless}"),
    TOTAL_ONLINE(TagType.GENERAL, "{total-online}");

    private TagType type;
    private String tag;

    TagReplacer(TagType type, String tag) {
        this.type = type;
        this.tag = tag;
    }

    /**
     * Returns a list of all the variables we can use for this type<br>
     *
     * @param type the type we want
     * @return a list of all the variables with this type
     */
    protected static List<TagReplacer> getByType(TagType type) {
        List<TagReplacer> tagReplacers = new ArrayList<>();
        for (TagReplacer tagReplacer : TagReplacer.values()) {
            if (type == TagType.FANCY) {
                if (tagReplacer.type == TagType.FANCY) {
                    tagReplacers.add(tagReplacer);
                }
            } else if (tagReplacer.type.id >= type.id) {
                tagReplacers.add(tagReplacer);
            }
        }
        return tagReplacers;
    }

    /**
     * Protected access to this generic server related variable
     *
     * @return value for this generic server related variable<br>
     */
    protected String getValue() {
        switch (this) {
            case GRACE_TIMER:
                return String.valueOf(TimerManager.getRemaining(FactionsPlugin.getInstance().getTimerManager().graceTimer.getRemaining(), true));
            case TOTAL_ONLINE:
                return String.valueOf(Bukkit.getOnlinePlayers().size());
            case FACTIONLESS:
                return String.valueOf(FactionPlayersManagerBase.getInstance().getAllFPlayers().stream().filter(p -> !p.hasFaction()).count());
            case MAX_ALLIES:
                if (FactionsPlugin.getInstance().getConfig().getBoolean("max-relations.enabled", true)) {
                    return String.valueOf(FactionsPlugin.getInstance().getConfig().getInt("max-relations.ally", 10));
                }
                return TL.GENERIC_INFINITY.toString();
            case MAX_ALTS:
                if (FactionsPlugin.getInstance().getConfig().getBoolean("f-alts.Enabled")) {
                    return String.valueOf(Conf.factionAltMemberLimit);
                }
                return TL.GENERIC_INFINITY.toString();
            case MAX_ENEMIES:
                if (FactionsPlugin.getInstance().getConfig().getBoolean("max-relations.enabled", true)) {
                    return String.valueOf(FactionsPlugin.getInstance().getConfig().getInt("max-relations.enemy", 10));
                }
                return TL.GENERIC_INFINITY.toString();
            case MAX_TRUCES:
                if (FactionsPlugin.getInstance().getConfig().getBoolean("max-relations.enabled", true)) {
                    return String.valueOf(FactionsPlugin.getInstance().getConfig().getInt("max-relations.truce", 10));
                }
                return TL.GENERIC_INFINITY.toString();
            default:
        }
        return null;
    }

    /**
     * Gets the value for this (as in the instance this is called from) variable!
     *
     * @param _faction Target faction
     * @param _player  Target player (can be null)
     * @return the value for this enum!
     */
    protected String getValue(Faction _faction, FactionPlayer _player) {
        if (this.type == TagType.GENERAL) {
            return getValue();
        }

        boolean minimal = FactionsPlugin.getInstance().getConfig().getBoolean("minimal-show", false);

        if (_player != null) {
            switch (this) {
                case HEADER:
                    return FactionsPlugin.getInstance().txt.titleize(_faction.getTag(_player));
                case PLAYER_NAME:
                    return _player.getPlayer_name();
                case FACTION:
                    return _faction.isNormal() ? _faction.getTag(_player) : TL.GENERIC_FACTIONLESS.toString();
//                case LAST_SEEN:
//                    String humanized = DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - fp.getLastLoginTime(), true, true) + TL.COMMAND_STATUS_AGOSUFFIX;
//                    return fp.isOnline() ? ChatColor.GREEN + TL.COMMAND_STATUS_ONLINE.toString() : (System.currentTimeMillis() - fp.getLastLoginTime() < 432000000 ? ChatColor.YELLOW + humanized : ChatColor.RED + humanized);
                case PLAYER_GROUP:
                    return FactionsPlugin.getInstance().getPrimaryGroup(Bukkit.getOfflinePlayer(UUID.fromString(_player.getPlayer_UUID())));
                case PLAYER_POWER:
                    // TODO: Add Power logic.
                    return ""; /*String.valueOf(fp.getPowerMax());*/
                case PLAYER_MAXPOWER:
                    return String.valueOf(_player.getPowerMax());
                case PLAYER_KILLS:
                    return String.valueOf(_player.getKills());
                default:
                    return "";
            }
        }

        switch (this) {
            case DESCRIPTION:
                return _faction.getDescription();
            case FACTION:
                return _faction.getFaction_tag();
//            case JOINING:
//                return (_faction.getOpen() ? TL.COMMAND_SHOW_UNINVITED.toString() : TL.COMMAND_SHOW_INVITATION.toString());
//            case PEACEFUL:
//                return _faction.isPeaceful() ? Conf.colorNeutral + TL.COMMAND_SHOW_PEACEFUL.toString() : "";
//            case PERMANENT:
//                return _faction.isPermanent() ? "permanent" : "{notPermanent}";
            case CHUNKS:
                return String.valueOf(_faction.getLandRounded());
            case POWER:
                return String.valueOf(_faction.getPowerRounded());
//            case MAX_POWER:
//                return String.valueOf(_faction.getPowerMaxRounded());
//            case POWER_BOOST:
//                double powerBoost = _faction.getPowerBoost();
//                return (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? TL.COMMAND_SHOW_BONUS.toString() : TL.COMMAND_SHOW_PENALTY.toString() + powerBoost + ")");
            case LEADER:
                FactionPlayer fAdmin = _faction.getFPlayerAdmin();
                return fAdmin == null ? "Server" : fAdmin.getPlayer_name().substring(0, fAdmin.getPlayer_name().length() > 14 ? 13 : fAdmin.getPlayer_name().length());
//            case CREATE_DATE:
//                return TL.sdf.format(_faction.getFoundedDate());
            case RAIDABLE:
                boolean raid = FactionsPlugin.getInstance().getConfig().getBoolean("hcf.raidable", false) && _faction.getLandRounded() >= _faction.getPowerRounded();
                return raid ? TL.RAIDABLE_TRUE.toString() : TL.RAIDABLE_FALSE.toString();
            //case SHIELD_STATUS:
            //if(fac.isProtected() && fac.getShieldFrame() != null) return String.valueOf(TL.SHIELD_CURRENTLY_ENABLE);
            //if(fac.getShieldFrame() == null) return String.valueOf(TL.SHIELD_NOT_SET);
            //return TL.SHIELD_CURRENTLY_NOT_ENABLED.toString();
//            case LAND_VALUE:
//                return Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandValue(fac.getLandRounded())) : minimal ? null : TL.ECON_OFF.format("value");
//            case LAND_REFUND:
//                return Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandRefund(fac.getLandRounded())) : minimal ? null : TL.ECON_OFF.format("refund");
//            case BANK_BALANCE:
//                if (Econ.shouldBeUsed()) {
//                    return Conf.bankEnabled ? Econ.insertCommas(Econ.getFactionBalance(fac)) : minimal ? null : TL.ECON_OFF.format("balance");
//                }
//                return minimal ? null : TL.ECON_OFF.format("balance");
            case ALLIES_COUNT:
                return String.valueOf(_faction.getRelationCount(Relation.ALLY));
            case ENEMIES_COUNT:
                return String.valueOf(_faction.getRelationCount(Relation.ENEMY));
            case TRUCES_COUNT:
                return String.valueOf(_faction.getRelationCount(Relation.TRUCE));
            case ONLINE_COUNT:
                if (_player != null && _player.isOnline()) {
                    return String.valueOf(_faction.getFPlayersWhereOnline(true, _player).length);
                } else {
                    // Only console should ever get here.
                    return String.valueOf(_faction.getFPlayersWhereOnline(true).length);
                }
            case OFFLINE_COUNT:
                return String.valueOf(_faction.getFaction_Players().length - _faction.getOnlinePlayers().length);
            case FACTION_SIZE:
                return String.valueOf(_faction.getFaction_Players().length);
            case FACTION_KILLS:
                return String.valueOf(_faction.getFaction_kills());
//            case FACTION_DEATHS:
//                return String.valueOf(_faction.getDeaths());
//            case FACTION_BANCOUNT:
//                return String.valueOf(_faction.getBannedPlayers().size());
//            case FACTION_STRIKES:
//                return String.valueOf(_faction.getStrikes());
            default:
                return "";
        }
    }

    /**
     * @param original raw line with variables
     * @param value    what to replace var in raw line with
     * @return the string with the new value
     */
    public String replace(String original, String value) {
        return (original != null && value != null) ? original.replace(tag, value) : original;

    }

    /**
     * @param toSearch raw line with variables
     * @return if the raw line contains this enums variable
     */
    public boolean contains(String toSearch) {
        if (tag == null) {
            return false;
        }
        return toSearch.contains(tag);
    }

    /**
     * Gets the tag associated with this enum that we should replace
     *
     * @return the {....} variable that is located in config
     */
    public String getTag() {
        return this.tag;
    }

    protected enum TagType {
        FANCY(0), PLAYER(1), FACTION(2), GENERAL(3);
        public int id;

        TagType(int id) {
            this.id = id;
        }
    }
}
