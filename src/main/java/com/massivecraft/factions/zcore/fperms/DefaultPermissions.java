package com.massivecraft.factions.zcore.fperms;

import java.util.Objects;

public class DefaultPermissions {

    /**
     * @author Illyria Team
     */

    public boolean build;
    public boolean destroy;
    public boolean door;
    public boolean button;
    public boolean lever;
    public boolean container;
    public boolean invite;
    public boolean kick;
    public boolean items;
    public boolean territory;
    public boolean promote;
    public boolean vault;
    public boolean chest;

    public DefaultPermissions(boolean def) {
        this.build = def;
        this.destroy = def;
        this.door = def;
        this.button = def;
        this.lever = def;
        this.container = def;
        this.invite = def;
        this.kick = def;
        this.items = def;
        this.territory = def;
        this.promote = def;
        this.vault = def;
        this.chest = def;
    }

    @Deprecated
    public boolean getbyName(String name) {
        switch (name) {
            case "build":
                return this.build;
            case "destroy":
                return this.destroy;
            case "door":
                return this.door;
            case "button":
                return this.button;
            case "lever":
                return this.lever;
            case "container":
                return this.container;
            case "invite":
                return this.invite;
            case "kick":
                return this.kick;
            case "items":
                return this.items;
            case "territory":
                return this.territory;
            case "promote":
                return this.promote;
            case "vault":
                return this.vault;
            case "chest":
                return this.chest;
            default:
                return false;
        }
    }
}
