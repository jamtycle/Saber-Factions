package com.massivecraft.factions.zcore.fperms;

import com.cryptomorin.xseries.XMaterial;
import com.massivecraft.factions.IFactionPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.util.CC;
import com.massivecraft.factions.util.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum PermissableAction {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    BUILD("build"),
    DESTROY("destroy"),
    DOOR("door"),
    BUTTON("button"),
    LEVER("lever"),
    CONTAINER("container"),
    INVITE("invite"),
    KICK("kick"),
    ITEM("items"), // generic for most items
    TERRITORY("territory"),
    PROMOTE("promote"),
    VAULT("vault"),
    CHEST("chest");

    private String name;

    PermissableAction(String name) {
        this.name = name;
    }

    /**
     * Case insensitive check for action.
     *
     * @param check
     * @return - action
     */
    public static PermissableAction fromString(String check) {
        for (PermissableAction permissableAction : values()) {
            if (permissableAction.name().equalsIgnoreCase(check)) {
                return permissableAction;
            }
        }
        return null;
    }

    public static Map<PermissableAction, Access> fromDefaults(DefaultPermissions defaultPermissions) {
        Map<PermissableAction, Access> defaultMap = new HashMap<>();
        for (PermissableAction permissableAction : PermissableAction.values()) {
            defaultMap.put(permissableAction, defaultPermissions.getbyName(permissableAction.name) ? Access.ALLOW : Access.DENY);
        }
        return defaultMap;
    }

    public static PermissableAction fromSlot(int slot) {
        for (PermissableAction action : PermissableAction.values()) {
            if (action.getSlot() == slot) return action;
        }
        return null;
    }

    public String getDescription() {
        return CC.translate(FactionsPlugin.getInstance().getFileManager().getFperms().getConfig().getString("fperm-gui.action.Descriptions." + this.name.toLowerCase()));
    }

    public int getSlot() {
        return FactionsPlugin.getInstance().getFileManager().getFperms().getConfig().getInt("fperm-gui.action.slots." + this.name.toLowerCase());
    }

    /**
     * Get the friendly name of this action. Used for editing in commands.
     *
     * @return friendly name of the action as a String.
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return name;
    }

    public ItemStack buildAsset(IFactionPlayer fme, Permissable perm) {
        ConfigurationSection section = FactionsPlugin.getInstance().getFileManager().getFperms().getConfig().getConfigurationSection("fperm-gui.action");
        ItemStack item = XMaterial.matchXMaterial(section.getString("Materials." + this.name)).get().parseItem();
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(CC.translate(section.getString("placeholder-item.name").replace("{action}", this.name)));
        List<String> lore = section.getStringList("placeholder-item.lore");

        lore = FactionsPlugin.getInstance().replacePlaceholders(lore,
                new Placeholder("{description}", this.getDescription()),
                new Placeholder("{action-access-color}", fme.getFaction().getPermissions().get(perm).get(this).getColor()),
                new Placeholder("{action-access}", fme.getFaction().getPermissions().get(perm).get(this).getName()));

        meta.setLore(CC.translate(lore));
        item.setItemMeta(meta);
        return item;
    }

}