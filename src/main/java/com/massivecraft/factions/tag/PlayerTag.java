package com.massivecraft.factions.tag;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.mysql.FactionPlayer;
import com.massivecraft.factions.zcore.util.TL;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.function.Function;

public enum PlayerTag implements Tag {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */
    GROUP("{group}", (fp) -> {
        if (fp.isOnline()) {
            return FactionsPlugin.getInstance().getPrimaryGroup(fp.getPlayer());
        } else {
            return "";
        }
    }),
    ROLE("{player-role}", FactionPlayer::getRolePrefix),
    PLAYER_KILLS("{player-kills}", (fp) -> String.valueOf(fp.getKills())),
//    PLAYER_DEATHS("{player-deaths}", (fp) -> String.valueOf(fp.getDeaths())),
    PLAYER_NAME("{name}", FactionPlayer::getPlayer_name),
    TOTAL_ONLINE_VISIBLE("total-online-visible", (fp) -> {
        if (fp == null) {
            return String.valueOf(Bukkit.getOnlinePlayers().size());
        }
        int count = 0;
        Player me = fp.getPlayer();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (me.canSee(player)) {
                count++;
            }
        }
        return Integer.toString(count);
    }),
    ;

    private final String tag;
    private final Function<FactionPlayer, String> function;

    PlayerTag(String tag, Function<FactionPlayer, String> function) {
        this.tag = tag;
        this.function = function;
    }

    public static String parse(String text, FactionPlayer player) {
        for (PlayerTag tag : VALUES) {
            text = tag.replace(text, player);
        }
        return text;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public boolean foundInString(String test) {
        return test != null && test.contains(this.tag);
    }

    public String replace(String text, FactionPlayer player) {
        if (!this.foundInString(text)) {
            return text;
        }
        String result = this.function.apply(player);
        return result == null ? null : text.replace(this.tag, result);
    }

    public static final PlayerTag[] VALUES = PlayerTag.values();

}
