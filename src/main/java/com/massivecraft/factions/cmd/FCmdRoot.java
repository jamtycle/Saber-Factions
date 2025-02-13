package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.cmd.chest.CmdChest;
import com.massivecraft.factions.cmd.claim.*;
import com.massivecraft.factions.cmd.grace.CmdGrace;
import com.massivecraft.factions.cmd.relational.CmdRelationAlly;
import com.massivecraft.factions.cmd.relational.CmdRelationEnemy;
import com.massivecraft.factions.cmd.relational.CmdRelationNeutral;
import com.massivecraft.factions.cmd.relational.CmdRelationTruce;
import com.massivecraft.factions.cmd.reserve.CmdReserve;
import com.massivecraft.factions.cmd.roles.CmdDemote;
import com.massivecraft.factions.cmd.roles.CmdPromote;
import com.massivecraft.factions.discord.CmdInviteBot;
import com.massivecraft.factions.discord.CmdSetGuild;
import com.massivecraft.factions.missions.CmdMissions;
import com.massivecraft.factions.util.Logger;
import com.massivecraft.factions.zcore.util.TL;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class FCmdRoot extends FCommand implements CommandExecutor {

    /**
     * @author FactionsUUID Team - Modified By CmdrKittens
     */

    public static FCmdRoot instance;
    public BrigadierManager brigadierManager;
    public CmdAdmin cmdAdmin = new CmdAdmin();
    public CmdBypass cmdBypass = new CmdBypass();
    public CmdChat cmdChat = new CmdChat();
    public CmdClaim cmdClaim = new CmdClaim();
    public CmdConfig cmdConfig = new CmdConfig();
    public CmdCreate cmdCreate = new CmdCreate();

    public CmdDescription cmdDescription = new CmdDescription();
    public CmdFocus cmdFocus = new CmdFocus();
    public CmdGrace cmdGrace = new CmdGrace();
    public CmdHelp cmdHelp = new CmdHelp();
    public CmdInvite cmdInvite = new CmdInvite();
    public CmdJoin cmdJoin = new CmdJoin();
    public CmdKick cmdKick = new CmdKick();
    public CmdLeave cmdLeave = new CmdLeave();
    public CmdList cmdList = new CmdList();
    public CmdMod cmdMod = new CmdMod();
    public CmdOpen cmdOpen = new CmdOpen();
    public CmdOwner cmdOwner = new CmdOwner();
    public CmdOwnerList cmdOwnerList = new CmdOwnerList();
    public CmdPeaceful cmdPeaceful = new CmdPeaceful();
    public CmdPermanent cmdPermanent = new CmdPermanent();
    public CmdPermanentPower cmdPermanentPower = new CmdPermanentPower();
    public CmdPowerBoost cmdPowerBoost = new CmdPowerBoost();
    public CmdPower cmdPower = new CmdPower();
    public CmdRelationAlly cmdRelationAlly = new CmdRelationAlly();
    public CmdRelationEnemy cmdRelationEnemy = new CmdRelationEnemy();
    public CmdRelationNeutral cmdRelationNeutral = new CmdRelationNeutral();
    public CmdRelationTruce cmdRelationTruce = new CmdRelationTruce();
    public CmdReload cmdReload = new CmdReload();
    public CmdSaveAll cmdSaveAll = new CmdSaveAll();
    public CmdShow cmdShow = new CmdShow();
    public CmdStatus cmdStatus = new CmdStatus();
    public CmdStealth cmdStealth = new CmdStealth();
    public CmdStuck cmdStuck = new CmdStuck();
    public CmdTag cmdTag = new CmdTag();
    public CmdTitle cmdTitle = new CmdTitle();
    public CmdPlayerTitleToggle cmdPlayerTitleToggle = new CmdPlayerTitleToggle();
    public CmdToggleAllianceChat cmdToggleAllianceChat = new CmdToggleAllianceChat();
    public CmdVersion cmdVersion = new CmdVersion();
    public CmdSB cmdSB = new CmdSB();
    public CmdShowInvites cmdShowInvites = new CmdShowInvites();
    public CmdAnnounce cmdAnnounce = new CmdAnnounce();
    public CmdSeeChunk cmdSeeChunk = new CmdSeeChunk();
    public CmdModifyPower cmdModifyPower = new CmdModifyPower();
    public CmdLogins cmdLogins = new CmdLogins();
    public CmdClaimLine cmdClaimLine = new CmdClaimLine();
    public CmdTop cmdTop = new CmdTop();
    public CmdPerm cmdPerm = new CmdPerm();
    public CmdPromote cmdPromote = new CmdPromote();
    public CmdDemote cmdDemote = new CmdDemote();
    public CmdSetDefaultRole cmdSetDefaultRole = new CmdSetDefaultRole();
    public CmdRules cmdRules = new CmdRules();
    public CmdCheckpoint cmdCheckpoint = new CmdCheckpoint();
    public CmdNear cmdNear = new CmdNear();
    public CmdUpgrades cmdUpgrades = new CmdUpgrades();
    public CmdVault cmdVault = new CmdVault();
    public CmdGetVault cmdGetVault = new CmdGetVault();
    public CmdColeader cmdColeader = new CmdColeader();
    public CmdBanner cmdBanner = new CmdBanner();
    public CmdKillHolograms cmdKillHolograms = new CmdKillHolograms();
    //public CmdInspect cmdInspect = new CmdInspect();
    public CmdCoords cmdCoords = new CmdCoords();
    public CmdShowClaims cmdShowClaims = new CmdShowClaims();
    public CmdLowPower cmdLowPower = new CmdLowPower();
    public CmdChest cmdChest = new CmdChest();
    public CmdSetBanner cmdSetBanner = new CmdSetBanner();
    public CmdCorner cmdCorner = new CmdCorner();
    public CmdInventorySee cmdInventorySee = new CmdInventorySee();
    public CmdFGlobal cmdFGlobal = new CmdFGlobal();
    public CmdViewChest cmdViewChest = new CmdViewChest();
    //public CmdLogout cmdLogout = new CmdLogout();
    public CmdMissions cmdMissions = new CmdMissions();
    public CmdStrikes cmdStrikes = new CmdStrikes();
    public CmdSetDiscord cmdSetDiscord = new CmdSetDiscord();
    public CmdSeeDiscord cmdSeeDiscord = new CmdSeeDiscord();
    public CmdInviteBot cmdInviteBot = new CmdInviteBot();
    public CmdSetGuild cmdSetGuild = new CmdSetGuild();
    public CmdDiscord cmdDiscord = new CmdDiscord();
    public CmdDebug cmdDebug = new CmdDebug();
    public CmdLookup cmdLookup = new CmdLookup();
    public CmdReserve cmdReserve = new CmdReserve();
    public CmdClaimFill cmdClaimFill = new CmdClaimFill();
    public CmdNotifications cmdNotifications = new CmdNotifications();
    public CmdFriendlyFire cmdFriendlyFire = new CmdFriendlyFire();
    public CmdSetPower cmdSetPower = new CmdSetPower();
    public CmdSpawnerChunk cmdSpawnerChunk = new CmdSpawnerChunk();
    public CmdCornerList cmdCornerList = new CmdCornerList();


    //Variables to know if we already setup certain sub commands
    public Boolean discordEnabled = false;
    public Boolean checkEnabled = false;
    public Boolean missionsEnabled = false;
    public Boolean fShopEnabled = false;
    public Boolean invSeeEnabled = false;
    public Boolean fPointsEnabled = false;
    public Boolean fAltsEnabled = false;
    public Boolean fGraceEnabled = false;
    public Boolean fFocusEnabled = false;
    public Boolean fFlyEnabled = false;
    public Boolean coreProtectEnabled = false;
    public Boolean internalFTOPEnabled = false;
    public Boolean fWildEnabled = false;
    public Boolean fAuditEnabled = false;
    public Boolean fStrikes = false;

    public FCmdRoot() {
        super();
        instance = this;

        if (CommodoreProvider.isSupported()) brigadierManager = new BrigadierManager();


        this.aliases.addAll(Conf.baseCommandAliases);
        this.aliases.removeAll(Collections.<String>singletonList(null));

        this.setHelpShort("The faction base command");
        this.helpLong.add(FactionsPlugin.getInstance().txt.parseTags("<i>This command contains all faction stuff."));

        if (CommodoreProvider.isSupported()) brigadierManager = new BrigadierManager();

        this.addSubCommand(this.cmdAdmin);
        this.addSubCommand(this.cmdBypass);
        this.addSubCommand(this.cmdChat);
        this.addSubCommand(this.cmdToggleAllianceChat);
        this.addSubCommand(this.cmdClaim);
        this.addSubCommand(this.cmdConfig);
        this.addSubCommand(this.cmdCreate);
        this.addSubCommand(this.cmdDescription);
        this.addSubCommand(this.cmdHelp);
        this.addSubCommand(this.cmdInvite);
        this.addSubCommand(this.cmdJoin);
        this.addSubCommand(this.cmdKick);
        this.addSubCommand(this.cmdLeave);
        this.addSubCommand(this.cmdList);
        this.addSubCommand(this.cmdMod);
        this.addSubCommand(this.cmdOpen);
        this.addSubCommand(this.cmdOwner);
        this.addSubCommand(this.cmdOwnerList);
        this.addSubCommand(this.cmdPeaceful);
        this.addSubCommand(this.cmdPermanent);
        this.addSubCommand(this.cmdPermanentPower);
        this.addSubCommand(this.cmdPower);
        this.addSubCommand(this.cmdPowerBoost);
        this.addSubCommand(this.cmdRelationAlly);
        this.addSubCommand(this.cmdRelationEnemy);
        this.addSubCommand(this.cmdRelationNeutral);
        this.addSubCommand(this.cmdRelationTruce);
        this.addSubCommand(this.cmdReload);
        this.addSubCommand(this.cmdSaveAll);
        this.addSubCommand(this.cmdShow);
        this.addSubCommand(this.cmdStatus);
        this.addSubCommand(this.cmdStealth);
        this.addSubCommand(this.cmdStuck);
        //this.addSubCommand(this.cmdLogout);
        this.addSubCommand(this.cmdTag);
        this.addSubCommand(this.cmdTitle);
        this.addSubCommand(this.cmdPlayerTitleToggle);
        this.addSubCommand(this.cmdVersion);
        this.addSubCommand(this.cmdSB);
        this.addSubCommand(this.cmdShowInvites);
        this.addSubCommand(this.cmdAnnounce);
        this.addSubCommand(this.cmdSeeChunk);
        this.addSubCommand(this.cmdModifyPower);
        this.addSubCommand(this.cmdLogins);
        this.addSubCommand(this.cmdClaimFill);
        this.addSubCommand(this.cmdClaimLine);
        this.addSubCommand(this.cmdPerm);
        this.addSubCommand(this.cmdPromote);
        this.addSubCommand(this.cmdDebug);
        this.addSubCommand(this.cmdDemote);
        this.addSubCommand(this.cmdSetDefaultRole);
        this.addSubCommand(this.cmdRules);
        this.addSubCommand(this.cmdCheckpoint);
        this.addSubCommand(this.cmdNear);
        this.addSubCommand(this.cmdUpgrades);
        this.addSubCommand(this.cmdVault);
        this.addSubCommand(this.cmdGetVault);
        this.addSubCommand(this.cmdColeader);
        this.addSubCommand(this.cmdBanner);
        this.addSubCommand(this.cmdKillHolograms);
        this.addSubCommand(this.cmdCoords);
        this.addSubCommand(this.cmdShowClaims);
        this.addSubCommand(this.cmdLowPower);
        this.addSubCommand(this.cmdChest);
        this.addSubCommand(this.cmdSetBanner);
        this.addSubCommand(this.cmdCorner);
        this.addSubCommand(this.cmdCornerList);
        this.addSubCommand(this.cmdFGlobal);
        this.addSubCommand(this.cmdViewChest);
        this.addSubCommand(this.cmdLookup);
        this.addSubCommand(this.cmdNotifications);
        this.addSubCommand(this.cmdFriendlyFire);
        this.addSubCommand(this.cmdSetPower);
        addVariableCommands();
        if (CommodoreProvider.isSupported()) brigadierManager.build();
    }

    /**
     * Add sub commands to the root if they are enabled
     */
    public void addVariableCommands() {
        //Discord
        if (FactionsPlugin.getInstance().getFileManager().getDiscord().fetchBoolean("Discord.useDiscordSystem") && !discordEnabled) {
            this.addSubCommand(this.cmdInviteBot);
            this.addSubCommand(this.cmdSetGuild);
            this.addSubCommand(this.cmdSetDiscord);
            this.addSubCommand(this.cmdSeeDiscord);
            this.addSubCommand(this.cmdDiscord);
            discordEnabled = true;
        }
        //Reserve
        if (Conf.useReserveSystem) {
            this.addSubCommand(this.cmdReserve);
        }

        //CoreProtect
        //if (Bukkit.getServer().getPluginManager().getPlugin("CoreProtect") != null && !coreProtectEnabled) {
        //    FactionsPlugin.getInstance().log("Found CoreProtect, enabling Inspect");
        //    this.addSubCommand(this.cmdInspect);
        //    coreProtectEnabled = true;
        //} else {
        //    FactionsPlugin.getInstance().log("CoreProtect not found, disabling Inspect");
        //}
        //FTOP
        if ((Bukkit.getServer().getPluginManager().getPlugin("FactionsTop") != null || Bukkit.getServer().getPluginManager().getPlugin("SavageFTOP") != null || Bukkit.getServer().getPluginManager().getPlugin("SaberFTOP") != null) && !internalFTOPEnabled) {
            Logger.print( "Found FactionsTop plugin. Disabling our own /f top command.", Logger.PrefixType.DEFAULT);
        } else {
            Logger.print( "Internal Factions Top Being Used. NOTE: Very Basic", Logger.PrefixType.DEFAULT);
            this.addSubCommand(this.cmdTop);
            internalFTOPEnabled = true;
        }

        if (Conf.useStrikeSystem) {
            this.addSubCommand(this.cmdStrikes);
            fStrikes = true;
        }

        if (Conf.userSpawnerChunkSystem) {
            this.addSubCommand(this.cmdSpawnerChunk);
        }

        if (FactionsPlugin.getInstance().getFileManager().getMissions().getConfig().getBoolean("Missions-Enabled", false) && !missionsEnabled) {
            this.addSubCommand(this.cmdMissions);
            missionsEnabled = true;
        }
        if (FactionsPlugin.getInstance().getConfig().getBoolean("f-inventory-see.Enabled", false) && !invSeeEnabled) {
            this.addSubCommand(this.cmdInventorySee);
            invSeeEnabled = true;
        }
        if (FactionsPlugin.getInstance().getConfig().getBoolean("f-grace.Enabled", false) && !fGraceEnabled) {
            this.addSubCommand(this.cmdGrace);
            fGraceEnabled = true;
        }
        if (FactionsPlugin.getInstance().getConfig().getBoolean("ffocus.Enabled") && !fFocusEnabled) {
            addSubCommand(this.cmdFocus);
            fFocusEnabled = true;
        }
    }

    public void rebuild() {
        if (CommodoreProvider.isSupported()) brigadierManager.build();
    }

    @Override
    public void perform(CommandContext context) {
        context.commandChain.add(this);
        this.cmdHelp.execute(context);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.execute(new CommandContext(sender, new ArrayList<>(Arrays.asList(args)), label));
        return true;
    }

    @Override
    public void addSubCommand(FCommand subCommand) {
        super.addSubCommand(subCommand);
        // People were getting NPE's as somehow CommodoreProvider#isSupported returned true on legacy versions.
        if (CommodoreProvider.isSupported()) {
            brigadierManager.addSubCommand(subCommand);
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.GENERIC_PLACEHOLDER;
    }

}
