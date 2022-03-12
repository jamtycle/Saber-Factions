package com.massivecraft.factions.tag;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.mysql.FactionPlayer;
import com.massivecraft.factions.mysql.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.mysql.Faction;
import com.massivecraft.factions.mysql.FactionPlayer;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.zcore.util.TL;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum FactionTag implements Tag {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    CHUNKS("{chunks}", (fac) -> String.valueOf(fac.getLandRounded())),
    HEADER("{header}", (fac, fp) -> FactionsPlugin.getInstance().txt.titleize(fac.getFaction_tag())),
    POWER("{power}", (fac) -> String.valueOf(fac.getPowerRounded())),
    LEADER("{leader}", (fac) -> {
        FactionPlayer fAdmin = fac.getFPlayerAdmin();
        return fAdmin == null ? "Server" : fAdmin.getPlayer_name().substring(0, fAdmin.getPlayer_name().length() > 14 ? 13 : fAdmin.getPlayer_name().length());
    }),
    JOINING("{joining}", (fac) -> (TL.COMMAND_SHOW_INVITATION.toString())),
    FACTION("{faction}", Faction::getFaction_tag),
    FACTION_RELATION_COLOR("{faction-relation-color}", (fac, fp) -> fp == null ? "" : fp.getColorTo(fac).toString()),
    RAIDABLE("{raidable}", (fac) -> {
        if (FactionsPlugin.getInstance().getConfig().getBoolean("hcf.raidable", false)) {
            boolean raidable = fac.getLandRounded() >= fac.getPowerRounded();
            String str = raidable ? TL.RAIDABLE_TRUE.toString() : TL.RAIDABLE_FALSE.toString();
            if (FactionsPlugin.getInstance().getConfig().getBoolean("hcf.dtr", false)) {
                int dtr = raidable ? 0 : (int) Math.ceil(((double) (fac.getPowerRounded() - fac.getLandRounded())) / Conf.powerPerDeath);
                str += ' ' + TL.COMMAND_SHOW_DEATHS_TIL_RAIDABLE.format(dtr);
            }
            return str;
        }
        return null;
    }),
//    ANNOUNCEMENT("{announcement}", (fac) -> {
//        return String.valueOf(fac.getAnnouncements());
//    }),
//    LAND_VALUE("{land-value}", (fac) -> Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandValue(fac.getLandRounded())) : Tag.isMinimalShow() ? null : TL.ECON_OFF.format("value")),
    DESCRIPTION("{description}", Faction::getDescription),
//    LAND_REFUND("{land-refund}", (fac) -> Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandRefund(fac.getLandRounded())) : Tag.isMinimalShow() ? null : TL.ECON_OFF.format("refund")),
//    BANK_BALANCE("{faction-balance}", (fac) -> {
//        if (Econ.shouldBeUsed()) {
//            return Conf.bankEnabled ? Econ.insertCommas(Econ.getFactionBalance(fac)) : Tag.isMinimalShow() ? null : TL.ECON_OFF.format("balance");
//        }
//        return Tag.isMinimalShow() ? null : TL.ECON_OFF.format("balance");
//    }),
//    TNT_BALANCE("{tnt-balance}", (fac) -> {
//        if (FactionsPlugin.instance.getConfig().getBoolean("ftnt.Enabled")) {
//            return String.valueOf(fac.getTnt());
//        }
//        return Tag.isMinimalShow() ? null : "";
//    }),
//    TNT_MAX("{tnt-max-balance}", (fac) -> {
//        if (FactionsPlugin.instance.getConfig().getBoolean("ftnt.Enabled")) {
//            return String.valueOf(fac.getTntBankLimit());
//        }
//        return Tag.isMinimalShow() ? null : "";
//    }),
    ALLIES_COUNT("{allies}", (fac) -> String.valueOf(fac.getRelationCount(Relation.ALLY))),
    ENEMIES_COUNT("{enemies}", (fac) -> String.valueOf(fac.getRelationCount(Relation.ENEMY))),
    TRUCES_COUNT("{truces}", (fac) -> String.valueOf(fac.getRelationCount(Relation.TRUCE))),
    ONLINE_COUNT("{online}", (fac, fp) -> {
        if (fp != null && fp.isOnline()) {
            return String.valueOf(fac.getFPlayersWhereOnline(true, fp).length);
        } else {
            // Only console should ever get here.
            return String.valueOf(fac.getFPlayersWhereOnline(true).length);
        }
    }),
    OFFLINE_COUNT("offline", (fac, fp) -> {
        if (fp != null && fp.isOnline()) {
            return String.valueOf(fac.getFaction_Players().size() - fac.getFPlayersWhereOnline(true, fp).length);
        } else {
            // Only console should ever get here.
            return String.valueOf(fac.getFPlayersWhereOnline(false).length);
        }
    }),
    FACTION_SIZE("{members}", (fac) -> String.valueOf(fac.getFaction_Players().size())),
    FACTION_KILLS("{faction-kills}", (fac) -> String.valueOf(fac.getFaction_kills()))
    ;

    private final String tag;
    private final BiFunction<Faction, FactionPlayer, String> biFunction;
    private final Function<Faction, String> function;

    FactionTag(String tag, BiFunction<Faction, FactionPlayer, String> function) {
        this.tag = tag;
        this.biFunction = function;
        this.function = null;
    }

    FactionTag(String tag, Function<Faction, String> function) {
        this.tag = tag;
        this.biFunction = null;
        this.function = function;
    }

    public static String parse(String text, Faction faction, FactionPlayer player) {
        for (FactionTag tag : VALUES) {
            text = tag.replace(text, faction, player);
        }
        return text;
    }

    public static String parse(String text, Faction faction) {
        for (FactionTag tag : VALUES) {
            text = tag.replace(text, faction);
        }
        return text;
    }

    public static final FactionTag[] VALUES = FactionTag.values();


    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public boolean foundInString(String test) {
        return test != null && test.contains(this.tag);
    }

    public String replace(String text, Faction faction, FactionPlayer player) {
        if (!this.foundInString(text)) {
            return text;
        }
        String result = this.function == null ? this.biFunction.apply(faction, player) : this.function.apply(faction);
        return result == null ? null : text.replace(this.tag, result);
    }

    public String replace(String text, Faction faction) {
        return this.replace(text, faction, null);

    }
}
