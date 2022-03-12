package com.massivecraft.factions.struct;

import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.command.CommandSender;

public enum Permission {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */


    ADMIN("admin"),
    ADMIN_ANY("admin.any"),
    ANNOUNCE("announce"),
    BOOSTERS("boosters"),
    BYPASS("bypass"),
    CHAT("chat"),
    CLAIM("claim"),
    CLAIMAT("claimat"),
    CLAIM_FILL("claimfill"),
    CLAIM_LINE("claim.line"),
    CLAIM_RADIUS("claim.radius"),
    CONFIG("config"),
    CREATE("create"),
    CORNER("corner"),
    CORNER_LIST("corner.list"),
    DEBUG("debug"),
    DEFAULTRANK("defaultrank"),
    DEINVITE("deinvite"),
    DELHOME("delhome"),
    DESCRIPTION("description"),
    DISCORD("discord"),
    FOCUS("focus"),
    FRIENDLYFIRE("friendlyfire"),
    GIVEBOOSTER("givebooster"),
    GLOBALCHAT("globalchat"),
    GRACE("grace"),
    GRACETOGGLE("gracetoggle"),
    HELP("help"),
    INVITE("invite"),
    INVSEE("invsee"),
    JOIN("join"),
    JOIN_ANY("join.any"),
    JOIN_OTHERS("join.others"),
    KICK("kick"),
    KICK_ANY("kick.any"),
    LEAVE("leave"),
    LIST("list"),
    LOGOUT("logout"),
    LOOKUP("lookup"),
    MOD("mod"),
    NOTIFICATIONS("notifications"),
    COLEADER("coleader"),
    MOD_ANY("mod.any"),
    COLEADER_ANY("coleader.any"),
    MISSIONS("missions"),
    MODIFY_POWER("modifypower"),
    MONITOR_LOGINS("monitorlogins"),
    NEAR("near"),
    OPEN("open"),
    OWNER("owner"),
    OWNERLIST("ownerlist"),
    RESERVE("reserve"),
    SET_GUILD("setguild"),
    SET_PEACEFUL("setpeaceful"),
    SET_PERMANENT("setpermanent"),
    SET_PERMANENTPOWER("setpermanentpower"),
    SHOW_INVITES("showinvites"),
    PERMISSIONS("permissions"),
    POWERBOOST("powerboost"),
    POWER("power"),
    POWER_ANY("power.any"),
    PROMOTE("promote"),
    RELATION("relation"),
    RELOAD("reload"),
    ROSTER("roster"),
    SAVE("save"),
    SPAM("spam"),
    SETDISCORD("setdiscord"),
    SETPOWER("setpower"),
    SETSTRIKES("setstrikes"),
    SHOW("show"),
    SPAWNER_CHUNKS("spawnerchunks"),
    STATUS("status"),
    STEALTH("stealth"),
    STUCK("stuck"),
    TAG("tag"),
    TOGGLE_TITLES("toggletitles"),
    TITLE("title"),
    TITLE_COLOR("title.color"),
    TOGGLE_ALLIANCE_CHAT("togglealliancechat"),
    VERSION("version"),
    SCOREBOARD("scoreboard"),
    SEECHUNK("seechunk"),
    SHOP("shop"),
    TOP("top"),
    VIEWCHEST("viewchest"),
    VAULT("vault"),
    GETVAULT("getvault"),
    SETMAXVAULTS("setmaxvaults"),
    RULES("rules"),
    CHECKPOINT("checkpoint"),
    UPGRADES("upgrades"),
    BANNER("banner"),
    KILLHOLOS("killholos"),
    INSPECT("inspect"),
    COORD("coords"),
    SHOWCLAIMS("showclaims"),
    CHUNKBUSTER_GIVE("corex.chunkbuster"),
    CHEST("chest");

    public final String node;

    Permission(final String node) {
        this.node = "factions." + node;
    }

    public boolean has(CommandSender sender, boolean informSenderIfNot) {
        return FactionsPlugin.getInstance().perm.has(sender, this.node, informSenderIfNot);
    }

    public boolean has(CommandSender sender) {
        return has(sender, false);
    }
}
