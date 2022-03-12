package com.massivecraft.factions.discord;

import net.dv8tion.jda.api.entities.User;

public abstract class DiscordUser {

    public boolean discordSetup = false;
    public String discordUserID = "";

    public boolean discordSetup() {
        return this.discordSetup;
    }

    public String discordUserID() {
        return this.discordUserID;
    }

    public User discordUser() {
        return Discord.jda.getUserById(this.discordUserID);
    }

    public void setDiscordSetup(Boolean b) {
        this.discordSetup = b;
    }

    public void setDiscordUserID(String s) {
        this.discordUserID = s;
    }
}
