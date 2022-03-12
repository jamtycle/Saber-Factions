package com.massivecraft.factions.mysql;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.mysql.abstracts.DBConnection;

import java.sql.Date;
import java.sql.ResultSet;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Season extends DBConnection {

    protected static Season season = getSeason();

    // region Variables

    private int id_season;
    private String season_name;
    private Date begin_date;
    private Date end_date;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm:ss");
    LocalDateTime now = LocalDateTime.now();

    // endregion

    public Season(FactionsPlugin _plugin) {
        super(_plugin);

        ResultSet faction_info = this.getResultSet("GET_CURRENT_SEASON(?)", dtf.format(now));
        if (faction_info == null) return;

        if (!BuildCLass(faction_info)) {
            plugin.getLogger().info("Couldn't build Season");
        }

        // TODO: Check dates between current date and season begin_date and end_date
    }

    // region Getters

    public static Season getSeasonInstance() {
        return season;
    }

    private static Season getSeason() {
        return new Season(FactionsPlugin.getInstance());
    }

    public int getId_season() {
        return id_season;
    }

    public String getSeason_name() {
        return season_name;
    }

    public Date getBegin_date() {
        return begin_date;
    }

    public Date getEnd_date() {
        return end_date;
    }

    // endregion
}
