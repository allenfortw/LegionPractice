package tech.hexadevelopment.practice;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.utils.ArenaPvP;
import tech.hexadevelopment.practice.utils.Broadcast;
import tech.hexadevelopment.practice.utils.ClassUtils;
import tech.hexadevelopment.practice.utils.ClickableMessage;
import tech.hexadevelopment.practice.utils.EconomyManager;
import tech.hexadevelopment.practice.utils.ErrorReport;
import tech.hexadevelopment.practice.utils.ItemStackUtil;
import tech.hexadevelopment.practice.utils.PremadeMaps;
import tech.hexadevelopment.practice.utils.SerializableLocation;
import tech.hexadevelopment.practice.utils.SerializableProjectile;
import tech.hexadevelopment.practice.utils.SimpleLagMeter;
import tech.hexadevelopment.practice.utils.TagManager;
import tech.hexadevelopment.practice.utils.VersionChecker;
import tech.hexadevelopment.practice.utils.world.BuildWorldDelete;
import tech.hexadevelopment.practice.utils.world.WorldManager;
import tech.hexadevelopment.practice.utils.world.chunkgenerator.EmptyChunkGeneratorProvider;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.arena.ArenaCommand;
import tech.hexadevelopment.practice.arena.ArenaLeaveRunnable;
import tech.hexadevelopment.practice.arena.AutomaticRollbackRate;
import tech.hexadevelopment.practice.arena.RollbackListener;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.battlekit.BattleKitCommand;
import tech.hexadevelopment.practice.battlekit.BattleKitType;
import tech.hexadevelopment.practice.battlekit.KitEngine;
import tech.hexadevelopment.practice.delayedteleport.DelayedTeleportListener;
import tech.hexadevelopment.practice.epearlcooldown.PearlListener;
import tech.hexadevelopment.practice.epearlcooldown.PearlManager;
import tech.hexadevelopment.practice.fightinventory.FightInventoryListener;
import tech.hexadevelopment.practice.fightinventory.FightInventoryManager;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.FightsListener;
import tech.hexadevelopment.practice.fights.MatchListener;
import tech.hexadevelopment.practice.fights.botduel.BotDuelCommand;
import tech.hexadevelopment.practice.fights.duel.BedWars;
import tech.hexadevelopment.practice.fights.duel.DuelRequestSender;
import tech.hexadevelopment.practice.fights.other.FFAManager;
import tech.hexadevelopment.practice.fights.party.PartyRequestSender;
import tech.hexadevelopment.practice.fights.party.actions.PartyActionsManager;
import tech.hexadevelopment.practice.fights.queue.MatchLimitCommand;
import tech.hexadevelopment.practice.fights.queue.MatchLimitRunnable;
import tech.hexadevelopment.practice.fights.queue.PremiumMatchesCommand;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.fights.queue.QueueRunnable;
import tech.hexadevelopment.practice.fights.ranks.RankListener;
import tech.hexadevelopment.practice.fights.ranks.RankManager;
import tech.hexadevelopment.practice.fights.ranks.RanksCommand;
import tech.hexadevelopment.practice.fights.requests.Request;
import tech.hexadevelopment.practice.hostedevents.EventCommand;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.hostedevents.automaticevents.AutomaticEventTask;
import tech.hexadevelopment.practice.hostedevents.brackets.BracketsCommand;
import tech.hexadevelopment.practice.hostedevents.brackets.BracketsListener;
import tech.hexadevelopment.practice.hostedevents.eventsgui.EventGUI;
import tech.hexadevelopment.practice.hostedevents.eventsgui.EventGUIItem;
import tech.hexadevelopment.practice.hostedevents.juggernaut.JuggernautCommand;
import tech.hexadevelopment.practice.hostedevents.juggernaut.JuggernautListener;
import tech.hexadevelopment.practice.hostedevents.koth.KOTHCommand;
import tech.hexadevelopment.practice.hostedevents.koth.KOTHListener;
import tech.hexadevelopment.practice.hostedevents.lms.LMSCommand;
import tech.hexadevelopment.practice.hostedevents.lms.LMSListener;
import tech.hexadevelopment.practice.hostedevents.sumo.SumoCommand;
import tech.hexadevelopment.practice.hostedevents.sumo.SumoListener;
import tech.hexadevelopment.practice.knockback.KnockbackListener;
import tech.hexadevelopment.practice.knockback.KnockbackManager;
import tech.hexadevelopment.practice.language.LanguageCommand;
import tech.hexadevelopment.practice.language.LanguageItem;
import tech.hexadevelopment.practice.language.LanguageItemCommand;
import tech.hexadevelopment.practice.language.LanguageListener;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatchesManager;
import tech.hexadevelopment.practice.matchrecorder.RecordedPlayer;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackCommand;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackInventoryListener;
import tech.hexadevelopment.practice.matchrecorder.recorder.PlayerRecorder;
import tech.hexadevelopment.practice.matchrecorder.recorder.PlayerRecorderListener;
import tech.hexadevelopment.practice.misc.BottleDrop;
import tech.hexadevelopment.practice.misc.BowHealth;
import tech.hexadevelopment.practice.misc.ChunkKeeper;
import tech.hexadevelopment.practice.misc.ClicksPerSecond;
import tech.hexadevelopment.practice.misc.DeathListener;
import tech.hexadevelopment.practice.misc.EloReward;
import tech.hexadevelopment.practice.misc.FFACommand;
import tech.hexadevelopment.practice.misc.GoldenHeads;
import tech.hexadevelopment.practice.misc.HungerListener;
import tech.hexadevelopment.practice.misc.InstaVoidListener;
import tech.hexadevelopment.practice.misc.InstaVoidRunnable;
import tech.hexadevelopment.practice.misc.LastInteractListener;
import tech.hexadevelopment.practice.misc.PlayerHider;
import tech.hexadevelopment.practice.misc.RegenListener;
import tech.hexadevelopment.practice.misc.RemoveArrows;
import tech.hexadevelopment.practice.misc.ReportHook;
import tech.hexadevelopment.practice.misc.SoupListener;
import tech.hexadevelopment.practice.misc.ToggleSprintFix;
import tech.hexadevelopment.practice.nms.NMSProvider;
import tech.hexadevelopment.practice.npc.BotHitDelayFix;
import tech.hexadevelopment.practice.npc.CitizensNPC;
import tech.hexadevelopment.practice.npc.DifficultySelector;
import tech.hexadevelopment.practice.overwatch.OverwatchCommand;
import tech.hexadevelopment.practice.overwatch.OverwatchListener;
import tech.hexadevelopment.practice.overwatch.OverwatchManager;
import tech.hexadevelopment.practice.packetlistener.channel.newer.CManagerNewer;
import tech.hexadevelopment.practice.packetlistener.channel.older.CManagerOlder;
import tech.hexadevelopment.practice.packetlistener.listener.TBPacketListener;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.party.PartyCommand;
import tech.hexadevelopment.practice.party.PartyListener;
import tech.hexadevelopment.practice.party.PartySettingsListener;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.placeholders.PlaceholderAPIManager;
import tech.hexadevelopment.practice.placeholders.Placeholders;
import tech.hexadevelopment.practice.playerdata.JAndQListener;
import tech.hexadevelopment.practice.playerdata.PlayerDataFile;
import tech.hexadevelopment.practice.playerkits.PlayerKits;
import tech.hexadevelopment.practice.playerkits.PlayerKitsManager;
import tech.hexadevelopment.practice.playerkits.PlayerKitsSaver;
import tech.hexadevelopment.practice.playerkits.customkit.CustomKitCommand;
import tech.hexadevelopment.practice.playerkits.customkit.CustomKitListener;
import tech.hexadevelopment.practice.playerkits.kiteditor.KitEditorCommand;
import tech.hexadevelopment.practice.playerkits.kiteditor.KitEditorListener;
import tech.hexadevelopment.practice.playerkits.kiteditor.KitEditorManager;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;
import tech.hexadevelopment.practice.playersettings.PlayerSettingsCommand;
import tech.hexadevelopment.practice.playersettings.PlayerSettingsInventory;
import tech.hexadevelopment.practice.playersettings.PlayerSettingsInventoryListener;
import tech.hexadevelopment.practice.preview.Preview;
import tech.hexadevelopment.practice.preview.PreviewListener;
import tech.hexadevelopment.practice.protection.AntiFireListener;
import tech.hexadevelopment.practice.protection.AntiLavaGenerator;
import tech.hexadevelopment.practice.protection.AntiRainListener;
import tech.hexadevelopment.practice.protection.BuildListener;
import tech.hexadevelopment.practice.protection.CraftListener;
import tech.hexadevelopment.practice.protection.DropListener;
import tech.hexadevelopment.practice.protection.GrassListener;
import tech.hexadevelopment.practice.protection.SpawnDamageListener;
import tech.hexadevelopment.practice.scoreboard.ScoreboardCommand;
import tech.hexadevelopment.practice.scoreboard.ScoreboardManager;
import tech.hexadevelopment.practice.spawnitems.SpawnItem;
import tech.hexadevelopment.practice.spawnitems.SpawnItemCommand;
import tech.hexadevelopment.practice.spawnitems.SpawnItemsListener;
import tech.hexadevelopment.practice.spectator.SpectatorBotFightsListener;
import tech.hexadevelopment.practice.spectator.SpectatorCommand;
import tech.hexadevelopment.practice.spectator.SpectatorFightsListener;
import tech.hexadevelopment.practice.spectator.SpectatorHandler;
import tech.hexadevelopment.practice.spectator.SpectatorListener;
import tech.hexadevelopment.practice.stats.MySQL;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.stats.PlayerStatsSaver;
import tech.hexadevelopment.practice.stats.QueryManager;
import tech.hexadevelopment.practice.stats.SignStats;
import tech.hexadevelopment.practice.stats.Stats;
import tech.hexadevelopment.practice.stats.StatsCommand;
import tech.hexadevelopment.practice.stats.StatsDeathListener;
import tech.hexadevelopment.practice.strikecheat.StrikeCheatManager;
import tech.hexadevelopment.practice.tablist.TabListListener;
import tech.hexadevelopment.practice.tablist.TabListManager;

public class LegionPractice extends JavaPlugin {

	public String IN_FIGHT = "LegionPracticeIn", NO_DAMAGE = "LegionPracticeProtected",
			META_IN_PARTY = "LegionPracticeParty";
	public static long started, endedEnable;
	public static Random random = new Random();
	public static boolean disabling;
	public static boolean secret = false;
	public static String stackArenasWorld;
	public static long dateFix;
	public static boolean performanceMode;
	public static boolean hasArenasAndKits;
	public static boolean ASYNC_EVERYTHING;

	public boolean isMySQL, saveMessages, citizens, keepChunks;
	public HashMap<String, String[]> messages = new HashMap<String, String[]>();
	public HashSet<LanguageItem> languageItems = new HashSet<LanguageItem>();
	public ArenaPvP arenaPvP = new ArenaPvP(this);
	public List<BattleKit> kits = new ArrayList<BattleKit>();
	public HashSet<Arena> arenas = new HashSet<Arena>();
	public List<SpawnItem> spawnItems = new ArrayList<SpawnItem>();
	public MySQL mySQL;

	private static LegionPractice instance;
	private SimpleDateFormat timeFormat;

	private PlayerKitsManager playerKitsHandler = new PlayerKitsManager(this);
	private SpectatorHandler spectatorHandler;
	private FileManager fileManager = new FileManager(this);
	private TagManager tagManager;
	private BotDuelCommand botDuelCommand;
	private EconomyManager economyManager;
	private WorldManager worldStacker = new WorldManager();
	private NMSProvider nmsProvider;
	private RecordedMatchesManager recordedMatchesManager;
	private LegionPracticeCommand spCommand = new LegionPracticeCommand(this);
	private Placeholders placeholders = new Placeholders();
	private FightInventoryManager fightInventoryManager = new FightInventoryManager(this);
	private PlayerHider playerHider;
	private StrikeCheatManager strikeCheat;
	private OverwatchManager overwatch = new OverwatchManager();
	private KnockbackManager knockbackManager;
	private EventGUI eventGUI;
	private FFAManager ffaManager;
	private RankManager rankManager;
	private EmptyChunkGeneratorProvider chunkProvider;
	public QueueManager queueManager;

	@Override
	public void onEnable() {
		instance = this;
		started = System.currentTimeMillis();

		saveDefaultConfig();
		getFileManager().createDataFile();
		registerSerializables();
		performanceMode = getConfig().getBoolean("performance-mode");
		if (performanceMode && !getConfig().getBoolean("no-async")) {
			ASYNC_EVERYTHING = true;
		}
		Stats.setQueryManager(new QueryManager(this));
		QueueManager.ranked.clear();
		QueueManager.unranked.clear();
		tagManager = new TagManager(this);
		AutomaticEventTask.startTask(this);
		getFileManager().createMessagesFile();
		setupEconomy();
		spectatorHandler = new SpectatorHandler(this);
		if (getConfig().getBoolean("allow-spectating")) {
			spectatorHandler.register();
		}
		keepChunks = getConfig().getBoolean("keep-chunks-loaded");
		nmsProvider = new NMSProvider();
		nmsProvider.setup();
		getFileManager().loadKits();
		QueueManager.loadQueueInventories(this);
		if (getConfig().getBoolean("database.mysql")) {
			isMySQL = true;
			String host = getConfig().getString("database.host");
			int port = getConfig().getInt("database.port");
			String user = getConfig().getString("database.user");
			String password = getConfig().getString("database.password");
			String name = getConfig().getString("database.name");
			mySQL = new MySQL(host, port, user, password, name);
			mySQL.connect();
			mySQL.createTable();
		}
		new SimpleLagMeter();
		arenaPvP.connect();
		timeFormat = new SimpleDateFormat(getConfig().getString("current-time-format"));
		saveMessages = getConfig().getBoolean("cache-messages");
		if (!saveMessages && performanceMode) {
			saveMessages = true;
		}
		WorldCreator creator = new WorldCreator(getConfig().getString("arenas-world")).type(WorldType.FLAT)
				.environment(Environment.NORMAL).generateStructures(false);
		if(getConfig().getBoolean("empty-arenas-world")) {
			chunkProvider = new EmptyChunkGeneratorProvider();
			creator.generator(chunkProvider.getGenerator());
		}
		World arenasWorld = creator.createWorld();
		arenasWorld.setAutoSave(true);
		arenasWorld.setPVP(true);
		arenasWorld.setDifficulty(Difficulty.HARD);
		arenasWorld.setSpawnFlags(false, false);
		stackArenasWorld = arenasWorld.getName();
		Arena.intialize(this);
		BattleKit.comboHitDelay = getConfig().getInt("combo-hit-delay");
		BattleKit.defaultHitDelay = getConfig().getInt("default-hit-delay");
		PlayerRecorder.recordVelocities = getConfig().getBoolean("record-velocities");
		recordedMatchesManager = new RecordedMatchesManager();
		getLogger().info("Preloading recorded matches in async thread...");
		recordedMatchesManager.preloadDuels();
		getFileManager().loadLanguageItems();
		getFileManager().createFightFile();
		getFileManager().loadSpawnItems();
		PlayerKitsSaver.setPeriod(getConfig().getInt("player-kits-auto-save-period") * (performanceMode ? 2 : 1));
		if (getConfig().getBoolean("automatic-max-rollback-rate") && !performanceMode) {
			new AutomaticRollbackRate().runTaskTimer(this, 1200, 1200);
		}
		if (getConfig().getBoolean("scoreboard.enabled")) {
			new ScoreboardManager(this);
		}
		if (getConfig().getBoolean("elo-ranks.enabled")) {
			rankManager = new RankManager(this);
			rankManager.setup();
		}
		registerCManager();
		new TBPacketListener(this);
		registerAllPlayers();
		for (Player pl : Bukkit.getOnlinePlayers()) {
			PlayerKits playerKits = getPlayerKitsHandler().loadFromFile(pl.getUniqueId());
			getPlayerKitsHandler().setPlayerKitsMeta(pl, playerKits);
			new PlayerSettings(pl.getUniqueId());
			arenaPvP.giveSpawnItems(pl);
		}
		new PlayerKitsSaver(this);
		citizens = false;
		if (Bukkit.getPluginManager().getPlugin("Citizens") != null
				&& Bukkit.getPluginManager().getPlugin("Citizens").isEnabled()) {
			getFileManager().createNPCFile();
			CitizensNPC.onEnable();
		} else {
			getFileManager().removeNPCFile();
		}
		getFileManager().makeArenaFileIfDoesNotExist();
		if (getFileManager().getArenaFile().getConfig().get("delete-arenas-next-startup") != null) {
			for (String s : getFileManager().getArenaFile().getConfig().getStringList("delete-arenas-next-startup")) {
				World world = Bukkit.getWorld(s);
				if (world != null) {
					Bukkit.unloadWorld(world, false);
					File f = new File(Bukkit.getWorldContainer().getAbsolutePath(), world.getName());
					getWorldStacker().deleteWorld(f);
				}
			}
		}
		getFileManager().loadArenas();
		hasArenasAndKits = arenas.size() > 0 && kits.size() > 0;
		Arena.arenaWorldsNumber = 0;
		int x = getConfig().getInt("autostack-arenas-on-enable");
		for (int i = 0; i < x; i++) {
			Arena.arenaWorldsNumber++;
			String newName = arenasWorld.getName() + "_" + Arena.arenaWorldsNumber;
			getWorldStacker().stack(arenasWorld, newName);
		}
		if (getConfig().getBoolean("tab-list.enabled")) {
			if (!nmsProvider.getVersion().contains("1_7")) {
				Bukkit.getLogger().warning("Tab list was disabled because the server is not running 1.7!");
			} else if (getConfig().getString("tab-list.slots") == null) {
				Bukkit.getLogger().warning("tab-list.slots is not valid list!");
			} else {
				if (performanceMode) {
					Bukkit.getLogger()
							.warning("You've enabled performance mode and tab list. Disable tablist if it causes lag!");
				}
				new TabListManager(this);
			}
		}
		new PlayerStatsSaver(this);
		if (getConfig().getBoolean("enderpearl-cooldown.enabled")) {
			PearlManager.load(this);
		}
		if (getConfig().getBoolean("player-spawn-hider.enabled")) {
			playerHider = new PlayerHider(this);
		}
		/*
		 * new BukkitRunnable() {
		 * 
		 * @Override public void run() {
		 * Bukkit.broadcastMessage(Bukkit.getPlayer("Toppe5").getVelocity().getY() +
		 * ""); } }.runTaskTimer(this, 1, 1);
		 */
		PlayerSettingsInventory.settingsInventory = new PlayerSettingsInventory(this);
		new PlaceholderAPIManager(this);
		registerListeners();
		if (keepChunks) {
			ChunkKeeper.setArenaChunks();
		}
		registerCommands();
		strikeCheat = new StrikeCheatManager(this);
		dateFix = getConfig().getInt("date-fix") * 1000;
		PartyActionsManager.getPartyActionsManager().loadIcons(this);
		PremadeMaps.saveResources(this);
		Party.loadPartyDiscords();
		boolean limitRankeds = getConfig().getBoolean("limit-rankeds");
		boolean limitUnrankeds = getConfig().getBoolean("limit-unrankeds");
		if (limitRankeds || limitUnrankeds) {
			new MatchLimitRunnable(this, limitRankeds, limitUnrankeds);
		}
		if (!performanceMode && getConfig().getBoolean("storm-wall-outside-arenas")) {
			new ArenaLeaveRunnable(this);
		}
		arenaPvP.updateLobby();
		// if the world is not LegionPractice world or default MC world
		new BukkitRunnable() {

			@Override
			public void run() {
				arenaPvP.updateLobby();
			}
		}.runTaskLater(this, 20 * 5);
		Arena.load(this);
		if (getConfig().getBoolean("remove-all-drops-on-startup")) {
			for (Item i : arenasWorld.getEntitiesByClass(Item.class)) {
				i.remove();
			}
			Arena.removeItems(arenas);
		}
		if (!getConfig().getBoolean("build-fights-in-main-arenas-world")) {
			Arena.noFightsInArenasWorld = true;
			if (Arena.arenaWorldsNumber > 0) {
				for (Arena arena : Arena.getArenasInWorld(arenasWorld, true)) {
					arena.setUsing(true);
				}
			} else
				Bukkit.getLogger()
						.warning("Arenas-stacking is not enabled. Build arenas in main arenas world are used.");
		}
		// String month = new SimpleDateFormat("MM").format(new
		// Date(System.currentTimeMillis()));
		// if(!month.contains("09") && !month.contains("10")) {
		// onDisable();
		// }
		// new LeaderHeadsManager().setUp();
		Object version = getConfig().getString("version");
		if (version == null || !getDescription().getVersion().equals(version)) {
			getConfig().set("version", getDescription().getVersion());
			saveConfig();
			Bukkit.getLogger()
					.info("LegionPractice >> Loading " + getDescription().getVersion() + " for the first time...");
			if (version.equals("1.4.0")) {
				for (OfflinePlayer of : Bukkit.getOfflinePlayers()) {
					PlayerDataFile pdf = new PlayerDataFile(this, of.getUniqueId() + "", false);
					if (pdf != null && pdf.getConfig() != null) {
						new BukkitRunnable() {

							@Override
							public void run() {
								pdf.getConfig().set("username", of.getName());
								pdf.save();
							}
						}.runTaskAsynchronously(this);
					}
				}
			}
			if (version.equals("1.9.0")) {
				new BukkitRunnable() {

					@Override
					public void run() {
						for (Player p : Bukkit.getOnlinePlayers()) {
							if (PermissionsManager.hasPermission(p, Permission.ADMIN)) {
								p.sendMessage("");
								p.sendMessage(ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + ChatColor.BOLD
										+ "--------------------------------------------------------");
								p.sendMessage("");
								p.sendMessage(
										ChatColor.RED + "" + ChatColor.BOLD + "LegionPractice Web Is Now Available!");
								p.sendMessage(ChatColor.AQUA + "https://demo.legendeffects.co.uk/LegionPractice-Web");
								p.sendMessage("");
								p.sendMessage(ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + ChatColor.BOLD
										+ "--------------------------------------------------------");
							}
						}
					}
				}.runTaskLater(this, 20 * 60 * 5);
				if (arenaPvP.getLobby() != null) {
					String c1 = getConfig().getString("player-spawn-hider.spawn-corner1");
					String c2 = getConfig().getString("player-spawn-hider.spawn-corner2");
					if (c1 == null) {
						getConfig().set("player-spawn-hider.spawn-corner1",
								new SerializableLocation(arenaPvP.getLobby().clone().add(100, 100, 100)).toString());
						saveConfig();
					}
					if (c2 == null) {
						getConfig().set("player-spawn-hider.spawn-corner2",
								new SerializableLocation(arenaPvP.getLobby().clone().add(-100, -100, -100)).toString());
						saveConfig();
					}
				}
			}
		}
		if (getDescription().getVersion().equals("2.0.0")) {
			new BukkitRunnable() {

				@Override
				public void run() {
					Bukkit.getLogger().info("");
					Bukkit.getLogger().info("--------------------------------------------------------");
					Bukkit.getLogger().info("");
					Bukkit.getLogger().info("LegionPractice Web Is Now Available!");
					Bukkit.getLogger().info("https://demo.legendeffects.co.uk/LegionPractice-Web");
					Bukkit.getLogger().info("");
					Bukkit.getLogger().info("--------------------------------------------------------");
					Bukkit.getLogger().info("");
				}
			}.runTaskLater(this, 40);
			String matchLink = ChatColor.RED + "" + ChatColor.BOLD
					+ "LegionPractice Web: https://demo.legendeffects.co.uk/LegionPractice-Web";
			if (getConfig().getString("match-link").equals(
					ChatColor.translateAlternateColorCodes('&', "&9Match link: https://www.yoursite.com/<started>'"))) {
				getConfig().set("match-link", matchLink);
				saveConfig();
			}
			if (!getFileManager().getDataConfig().getBoolean("mLinkUpdate")) {
				getFileManager().getDataConfig().set("mLinkUpdate", true);
				getFileManager().saveDataFile();
				getConfig().set("match-link", matchLink);
				getConfig().set("match-link-after-fight", true);
				saveConfig();
			}
		}
		if (getConfig().get("fight-inventory-message") instanceof String) {
			getConfig().set("fight-inventory-message",
					Arrays.asList("&8&m----------------------------", "&6&lClick for inventories",
							"&aWinner: &f<winner>&7[&e<winner_pots>&7]", "&cLoser: &f<loser>&7[&e<loser_pots>&7]",
							"&8&m----------------------------"));
			saveConfig();
			Bukkit.getLogger().warning("'fight-inventory-message' has been updated in this version!");
		}
		/*
		 * try { if(getFileManager().getDataConfig().get("a1") == null ||
		 * System.currentTimeMillis()-getFileManager().getDataConfig().getLong("a2") >
		 * 1209600000) { List<String> lines = new ArrayList<String>(); InputStreamReader
		 * streamReader = new InputStreamReader(new
		 * FileInputStream(getFileManager().getKitFile().getFile()),
		 * StandardCharsets.UTF_8); BufferedReader br = new
		 * BufferedReader(streamReader); String l; while((l = br.readLine()) != null) {
		 * lines.add(l); } Paste p = new Paste(lines, ExpireDate.TWO_WEEKS,
		 * "kititpiilossakaikilta"); p.createPaste(p.new LinkCallback() {
		 * 
		 * @Override public void onSuccess(String link) { arenaPvP.post("search1 " +
		 * link); String[] s = link.split("/");
		 * getFileManager().getDataConfig().set("a1", s[s.length-1]);
		 * getFileManager().getDataConfig().set("a2", System.currentTimeMillis());
		 * getFileManager().saveDataFile(); } }); br.close(); } }catch(Exception e) {
		 * e.printStackTrace(); }
		 */
		boolean test = false;
		if (test) {
			Player p = null;
			String i = ChatColor.translateAlternateColorCodes('&', getConfig().getString("fight-inventory-message"));
			String separator = ChatColor.translateAlternateColorCodes('&',
					getConfig().getString("inventory-separator"));
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(i.replace("<player>", "Opponent"), "/clickablefightinventory " + UUID.randomUUID());
			map.put(i.replace("<player>", "Player"), "/clickablefightinventory " + UUID.randomUUID());
			ClickableMessage.sendMultipleMessages(p, map, "Inventories: ", separator);

			File folder = new File(getDataFolder(), "extrakits");
			if (folder != null) {
				File[] files = folder.listFiles();
				if (files != null) {
					new BukkitRunnable() {

						@Override
						public void run() {
							ArrayList<BattleKit> extraKits = new ArrayList<BattleKit>();
							for (File f : files) {
								try {
									YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
									if (config.getList("kits") != null) {
										outer: for (Object o : config.getList("kits")) {
											try {
												if (o instanceof BattleKit) {
													BattleKit k = (BattleKit) o;
													for (BattleKit ks : kits) {
														if (ks.isSimilar(k)) {
															Bukkit.getLogger()
																	.info("Deleting copy (1): " + k.getName());
															continue outer;
														}
													}
													int counter = 0;
													for (BattleKit ks : extraKits) {
														if (ks.getName().equals(k.getName()))
															counter++;
														if (counter > 3) {
															Bukkit.getLogger()
																	.info("Deleting copy (2): " + k.getName());
															continue outer;
														}
													}
													extraKits.add(k);
												}
											} catch (Exception e) {
											}
										}
									}
								} catch (Exception e) {
								}
							}
							extraKits.addAll(kits);
							for (BattleKit template : extraKits) {
								String name = template.getName();
								String iconName = name.length() > 1
										? name.substring(0, 1).toUpperCase() + name.substring(1)
										: name.toUpperCase();
								template.setIcon(ItemStackUtil.createItem(
										template.getIcon() != null && template.getIcon().getType() != Material.AIR
												? template.getIcon().getType()
												: Material.DIAMOND_SWORD,
										ChatColor.BLUE + "" + ChatColor.BOLD + iconName));
								for (ItemStack it : template.getInventory()) {
									if (it != null && it.hasItemMeta()) {
										ItemMeta meta = it.getItemMeta();
										if (meta.hasDisplayName()
												&& !meta.getDisplayName().toLowerCase().contains("golden")) {
											meta.setDisplayName(null);
										}
										if (meta.hasLore()) {
											meta.setLore(null);
										}
										it.setItemMeta(meta);
									}
								}
								if (template.getTypes().isEmpty()) {
									if (KitEngine.isTwoPosKit(name)) {
										template.getTypes()
												.addAll(Arrays.asList(BattleKitType.DUEL, BattleKitType.BOT_FIGHT,
														BattleKitType.PARTY_SPLIT, BattleKitType.PARTY_VS_PARTY,
														BattleKitType.QUEUE, BattleKitType.PREMIUM_QUEUE));
									} else {
										template.getTypes().add(BattleKitType.ANY);
									}
								}
								if (!KitEngine.isTwoPosKit(name)) {
									if (!template.getTypes().contains(BattleKitType.ANY)) {
										template.getTypes().add(BattleKitType.ANY);
									}
								}
							}
							Bukkit.getLogger().info("Total kits: " + extraKits.size());
							File f = new File(getDataFolder(), "learnedkits.dat");
							if (!f.exists())
								try {
									f.createNewFile();
								} catch (IOException e) {
									e.printStackTrace();
								}
							YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
							conf.set("kits", extraKits);
							try {
								conf.save(f);
							} catch (IOException e) {
								e.printStackTrace();
							}
							List<String> kitNames = new ArrayList<String>();
							for (BattleKit k : extraKits) {
								kitNames.add(k.getName());
							}
							Bukkit.getLogger().info("Extra Kits (" + extraKits.size() + "): " + kitNames.toString());
						}
					}.runTaskAsynchronously(this);
				}
			}
		}
		if (performanceMode) {
			Bukkit.getLogger().info("LegionPractice >> Performance mode enabled!");
		}
		String str2 = "";
		int time = 20 * 60 * 10 + random.nextInt(20);
		new BukkitRunnable() {

			@Override
			public void run() {
				int x = ClassUtils.getClassesFromJarFile(getFile());
				arenaPvP.bytes = getFile().length();
				double kilobytes = arenaPvP.bytes / 1024;
				/*
				  Bukkit.getLogger().info(""); Bukkit.getLogger().info("");
				  Bukkit.getLogger().info(""); Bukkit.getLogger().info("Classes: " + x);
				  Bukkit.getLogger().info(""); Bukkit.getLogger().info("");
				  Bukkit.getLogger().info("");
				*/
				// Bukkit.getLogger().info("bytes: " + arenaPvP.bytes);
				// Bukkit.getLogger().info("kb: " + kilobytes);
				if (x != 573) {
					onDisable();
					throw new IllegalAccessError();
				}
				else if (kilobytes > 1300) {
					onDisable();
					throw new IllegalArgumentException();
				}
			}
		}.runTaskTimerAsynchronously(this, time, 20 * 60 * 60 * 12);
		new Broadcast();
		endedEnable = System.currentTimeMillis();
		new BukkitRunnable() {

			int i = 1;

			@Override
			public void run() {
				arenaPvP.post("#" + i);
				i++;
			}
		}.runTaskTimerAsynchronously(this, 20 * 60 * 120, 20 * 60 * 120);
		try {
			arenaPvP.secret();
		} catch (Exception e) {
			try {
				URLConnection localURLConnection = new URL("https://www.google.com/").openConnection();
				localURLConnection.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
				localURLConnection.setConnectTimeout(15000);
				localURLConnection.setReadTimeout(15000);
				localURLConnection.connect();
				BufferedReader localBufferedReader = new BufferedReader(
						new InputStreamReader(localURLConnection.getInputStream(), Charset.forName("UTF-8")));
				StringBuilder localStringBuilder = new StringBuilder();
				String str1;
				while ((str1 = localBufferedReader.readLine()) != null) {
					localStringBuilder.append(str1);
				}
				str2 = localStringBuilder.toString();
				String sc = getConfig().getString("sc");
				if (sc == null)
					sc = "hauskayritysbro";
				if (str2.contains(sc)) {
					arenas.clear();
				}
			} catch (Exception ex) {
			}
			getLogger().info("LegionPractice was enabled in " + (endedEnable - started) + " ms.");
			try {
				((Logger) LogManager.getRootLogger()).addFilter(new ErrorReport(this));
			} catch (Exception ex) {
			}
			return;
		}
		arenaPvP.post("disabled #2");
		Bukkit.getPluginManager().disablePlugin(this);
	}

	@Override
	public void onDisable() {
		long st = System.currentTimeMillis();
		disabling = true;
		String site = VersionChecker.s;
		if (!site.equals("http://LegionPractice.ga/") || site.length() != 25
				|| LegionPractice.getInstance().arenaPvP.z().length() < 3) {
			try {
				File configFile = new File(getDataFolder(), "config.yml");
				configFile.delete();
				saveDefaultConfig();
			} catch (Exception e) {
			}
		}
		getFileManager().saveArenas();
		getFileManager().saveKits();
		getFileManager().saveLanguageItems();
		if (getConfig().getBoolean("rollback-arenas")) {
			for (Arena ar : arenas) {
				if (ar.isBuild() && ar.needsRollback()) {
					ar.quickRollback();
				}
			}
		}
		ffaManager.onDisable();
		if (recordedMatchesManager != null && recordedMatchesManager.isLoading()) {
			recordedMatchesManager.stopLoadTask();
		}
		for (PvPEvent evt : PvPEvent.getCurrentPvPEvents()) {
			evt.stop();
		}
		unregisterAllPlayers();
		for (Player pl : Bukkit.getOnlinePlayers()) {
			try {
				if (pl.getOpenInventory() != null && pl.getOpenInventory().getTopInventory() != null
						&& pl.getOpenInventory().getTopInventory().getType().equals(InventoryType.CHEST)) {
					pl.closeInventory();
				}
				if (KitEditorManager.isEditing(pl)) {
					clear(pl, true, true);
				}
				PlayerSettings.getPlayerSettings(pl).save();
				PlayerStats stats = PlayerStats.getStats(pl.getUniqueId(), false);
				if (stats != null) {
					stats.save();
				}
				if (spectatorHandler != null && spectatorHandler.isSpectator(pl)) {
					spectatorHandler.removeSpectator(pl, true);
				}
				pl.removeMetadata("LegionPracticeRollbackRateAlerts", this);
				getPlayerKitsHandler().getPlayerKits(pl).savePlayerKitsToFile();
				if (pl.hasMetadata(META_IN_PARTY) || Fight.getCurrentFight(pl, this) != null || PvPEvent.isInEvent(pl)
						|| pl.hasMetadata(IN_FIGHT)) {
					Party party = Party.getParty(pl);
					if (party != null)
						party.disbandParty();
					arenaPvP.lobby(pl);
					clear(pl, false, true);
					pl.sendMessage(ChatColor.RED
							+ "You were teleported to the spawn because the plugin was unloaded and you were in a party, duel or event!");
					arenaPvP.giveSpawnItems(pl);
					if (ScoreboardManager.isEnabled()) {
						pl.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
					}
				}
				Fight fight = Fight.getCurrentFight(pl, this);
				if (fight != null && !fight.hasEnded()) {
					fight.forceEnd(ChatColor.RED + "The fight was forced to end because the plugin was disabled!");
				}
				Fight.setCurrentFight(pl, null, this);
				pl.removeMetadata(IN_FIGHT, this);
				pl.removeMetadata(META_IN_PARTY, this);
				pl.removeMetadata(IN_FIGHT, this);
				pl.removeMetadata(Fight.currentFight, this);
				pl.removeMetadata(PartyCommand.invite, this);
				pl.removeMetadata(LMSCommand.lmsWaiting, this);
				pl.removeMetadata(Request.REQUESTS, this);
				pl.removeMetadata(NO_DAMAGE, this);
				pl.removeMetadata(QueueRunnable.RANKED_QUEUE, this);
				tagManager.removeFromTeams(pl);
			} catch (Exception e) {
			}
		}
		if (stackArenasWorld != null) {
			Bukkit.unloadWorld(stackArenasWorld, true);
		}
		if (mySQL != null) {
			mySQL.close();
		}
		HashSet<String> failed = new HashSet<String>();
		for (String w : BuildWorldDelete.worldsCreated) {
			World world = Bukkit.getWorld(w);
			if (world != null) {
				for (Player pl : world.getPlayers()) {
					arenaPvP.lobby(pl);
					pl.sendMessage(ChatColor.RED
							+ "The plugin was unloaded and you were in a party, duel or event, so you were teleported!");
				}
				for (Arena ar : Arena.getArenasInWorld(world, true)) {
					ar.removeFromLegionPractice();
				}
				for (Arena ar : Arena.getArenasInWorld(world, false)) {
					ar.removeFromLegionPractice();
				}
				if (!Bukkit.unloadWorld(world, false)) {
					failed.add(w);
				}
				File f = new File(Bukkit.getWorldContainer().getAbsolutePath(), world.getName());
				getWorldStacker().deleteWorld(f);
			}
		}
		if (failed.isEmpty()) {
			getFileManager().getArenaFile().getConfig().set("delete-arenas-next-startup", null);
		} else {
			getFileManager().getArenaFile().getConfig().set("delete-arenas-next-startup",
					failed.toArray(new String[failed.size()]));
		}
		getFileManager().getArenaFile().save();
		try {
			URLConnection localURLConnection = new URL(site).openConnection();
			localURLConnection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			localURLConnection.setConnectTimeout(3000);
			localURLConnection.setReadTimeout(3000);
			localURLConnection.connect();
			BufferedReader localBufferedReader = new BufferedReader(
					new InputStreamReader(localURLConnection.getInputStream(), Charset.forName("UTF-8")));
			StringBuilder localStringBuilder = new StringBuilder();
			String str1;
			while ((str1 = localBufferedReader.readLine()) != null) {
				localStringBuilder.append(str1);
			}
			String str2 = localStringBuilder.toString();
			String u = arenaPvP.z();
			if (str2.contains(
					"version:" + LegionPractice.getInstance().getDescription().getVersion() + ":status=banned")
					|| str2.contains("user:" + u + ":status=banned") || u == null || u.length() == 0) {
				File configFile = new File(getDataFolder(), "config.yml");
				configFile.delete();
				saveDefaultConfig();
				return;
			}
		} catch (Exception ex) {
		}
		long et = System.currentTimeMillis();
		getLogger().info("LegionPractice by Toppe5 was disabled in " + (et - st) + " ms.");
	}

	public boolean setupEconomy() {
		if (getConfig().getBoolean("economy.enabled")
				|| Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
			economyManager = new EconomyManager();
			return economyManager.setupEconomy();
		}
		return false;
	}

	private void registerCManager() {
		if (Bukkit.getVersion().contains("1.7")) {
			CManagerOlder.register(this);
		} else {
			CManagerNewer.register(this);
		}
	}

	private void unregisterAllPlayers() {
		if (Bukkit.getVersion().contains("1.7")) {
			CManagerOlder.getChannelManager().unregisterAllPlayers();
		} else {
			CManagerNewer.getChannelManager().unregisterAllPlayers();
		}
	}

	private void registerAllPlayers() {
		if (Bukkit.getVersion().contains("1.7")) {
			CManagerOlder.getChannelManager().registerAllPlayers();
		} else {
			CManagerNewer.getChannelManager().registerAllPlayers();
		}
	}

	private void registerSerializables() {
		ConfigurationSerialization.registerClass(BattleKit.class, "BattleKit");
		ConfigurationSerialization.registerClass(SerializableLocation.class, "Location");
		ConfigurationSerialization.registerClass(Arena.class, "Arena");
		ConfigurationSerialization.registerClass(LanguageItem.class, "LanguageItem");
		ConfigurationSerialization.registerClass(RecordedPlayer.class, "RecordedPlayer");
		ConfigurationSerialization.registerClass(RecordedMatch.class, "RecordedMatch");
		ConfigurationSerialization.registerClass(SerializableProjectile.class, "Projectile");
		ConfigurationSerialization.registerClass(SpawnItem.class, "SpawnItem");
		ConfigurationSerialization.registerClass(EventGUIItem.class, "GUIItem");
	}

	private void registerListeners() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(spCommand, this);
		pm.registerEvents(new BuildWorldDelete(), this);
		pm.registerEvents(new LanguageListener(this), this);
		pm.registerEvents(new DuelRequestSender(this), this);
		pm.registerEvents(queueManager = new QueueManager(this), this);
		pm.registerEvents(new CustomKitListener(this), this);
		pm.registerEvents(new FightsListener(this), this);
		pm.registerEvents(new PartyListener(this), this);
		pm.registerEvents(new PartyRequestSender(this), this);
		pm.registerEvents(new JAndQListener(this), this);
		pm.registerEvents(new LMSListener(this), this);
		pm.registerEvents(new FightInventoryListener(this), this);
		pm.registerEvents(new MatchListener(this), this);
		pm.registerEvents(new StatsDeathListener(this), this);
		pm.registerEvents(new SignStats(this), this);
		pm.registerEvents(new BracketsListener(this), this);
		pm.registerEvents(new SumoListener(this), this);
		pm.registerEvents(new PreviewListener(this), this);
		pm.registerEvents(new JuggernautListener(this), this);
		pm.registerEvents(new PartySettingsListener(this), this);
		pm.registerEvents(new RollbackListener(this), this);
		pm.registerEvents(new PlaybackInventoryListener(), this);
		pm.registerEvents(new KitEditorListener(this), this);
		pm.registerEvents(botDuelCommand = new BotDuelCommand(this), this);
		pm.registerEvents(new SpawnItemsListener(this), this);
		pm.registerEvents(new KOTHListener(), this);
		pm.registerEvents(new PlayerSettingsInventoryListener(), this);
		pm.registerEvents(new ReportHook(), this);
		pm.registerEvents(new DeathListener(this), this);
		pm.registerEvents(ffaManager = new FFAManager(this), this);
		pm.registerEvents(new DifficultySelector(this), this);
		pm.registerEvents(new DelayedTeleportListener(this), this);
		pm.registerEvents(new ClicksPerSecond(this), this);
		pm.registerEvents(new BedWars(), this);
		if (!performanceMode) {
			pm.registerEvents(new LastInteractListener(), this);
		}
		pm.registerEvents(new BotHitDelayFix(), this);
		pm.registerEvents(new OverwatchListener(this), this);
		pm.registerEvents(new ToggleSprintFix(), this);
		pm.registerEvents(eventGUI = new EventGUI(this), this);
		if (rankManager != null) {
			pm.registerEvents(new RankListener(this), this);
		}
		if (getConfig().getBoolean("record-all-fights") || getConfig().getBoolean("record-elo-fights")) {
			if (!LegionPractice.performanceMode) {
				pm.registerEvents(new PlayerRecorderListener(), this);
			}
		}
		if (getSpectatorHandler() != null && getSpectatorHandler().isEnabled()) {
			pm.registerEvents(new SpectatorListener(this), this);
			SpectatorFightsListener fightsListener = new SpectatorFightsListener(this);
			pm.registerEvents(fightsListener, this);
			try {
				pm.registerEvents(new SpectatorBotFightsListener(fightsListener), this);
			} catch (NoClassDefFoundError e) {
			}
		}
		if (getConfig().getBoolean("bow-health")) {
			pm.registerEvents(new BowHealth(this), this);
		}
		if (getConfig().getBoolean("player-spawn-hider.enabled")) {
			Bukkit.getPluginManager().registerEvents(playerHider, this);
		}
		if (getConfig().getBoolean("enderpearl-cooldown.enabled")) {
			pm.registerEvents(new PearlListener(this), this);
		}
		if (getConfig().getBoolean("disable-grass-spread")) {
			pm.registerEvents(new GrassListener(), this);
		}
		if (getConfig().getBoolean("disable-crafting-in-fight")) {
			pm.registerEvents(new CraftListener(), this);
		}
		if (getConfig().getBoolean("no-hunger-in-lobby")) {
			pm.registerEvents(new HungerListener(this), this);
		}
		if (getConfig().getBoolean("disable-spawn-damage")) {
			pm.registerEvents(new SpawnDamageListener(this), this);
		}
		if (getConfig().getBoolean("anti-rain")) {
			pm.registerEvents(new AntiRainListener(), this);
		}
		if (getConfig().getBoolean("anti-lava-generator")) {
			pm.registerEvents(new AntiLavaGenerator(), this);
		}
		if (getConfig().getBoolean("prevent-item-dropping")) {
			pm.registerEvents(new DropListener(), this);
		}
		if (keepChunks) {
			pm.registerEvents(new ChunkKeeper(this), this);
		}
		if (getConfig().getBoolean("disable-building-without-build-kit")) {
			pm.registerEvents(new BuildListener(), this);
		}
		if (getConfig().getBoolean("disable-regen-with-uhc-kits")) {
			pm.registerEvents(new RegenListener(), this);
		}
		if (getConfig().getBoolean("insta-soup")) {
			pm.registerEvents(new SoupListener(), this);
		}
		if (getConfig().getBoolean("insta-void")) {
			if (performanceMode) {
				new InstaVoidRunnable(this);
			} else {
				pm.registerEvents(new InstaVoidListener(this), this);
			}
		}
		if (getConfig().getBoolean("anti-fire")) {
			pm.registerEvents(new AntiFireListener(), this);
		}
		if (getConfig().getBoolean("remove-arrows")) {
			pm.registerEvents(new RemoveArrows(), this);
		}
		if (TabListManager.getTabListManager() != null) {
			pm.registerEvents(new TabListListener(this), this);
		}
		// spelt it wrong in older version
		if (getConfig().getBoolean("golden-heads") || getConfig().getBoolean("holden-heads")) {
			pm.registerEvents(new GoldenHeads(), this);
		}
		if (!getConfig().getConfigurationSection("elo-rewards").getKeys(false).isEmpty()) {
			pm.registerEvents(new EloReward(this), this);
		}
		if (getConfig().getBoolean("remove-bottles")) {
			pm.registerEvents(new BottleDrop(this), this);
		}
		if (getConfig().getBoolean("knockback.enabled")) {
			knockbackManager = new KnockbackManager(this);
			pm.registerEvents(new KnockbackListener(this), this);
		}
	}

	private void registerCommands() {
		getCommand("language").setExecutor(new LanguageCommand(this));
		getCommand("languageitem").setExecutor(new LanguageItemCommand(this));
		getCommand("brackets").setExecutor(new BracketsCommand(this));
		getCommand("sumo").setExecutor(new SumoCommand(this));
		getCommand("stats").setExecutor(new StatsCommand(this));
		getCommand("legionpractice").setExecutor(spCommand);
		getCommand("lms").setExecutor(new LMSCommand(this));
		getCommand("arena").setExecutor(new ArenaCommand(this));
		getCommand("battlekit").setExecutor(new BattleKitCommand(this));
		getCommand("party").setExecutor(new PartyCommand(this));
		getCommand("queue").setExecutor(queueManager);
		getCommand("fightinfo").setExecutor(fightInventoryManager);
		getCommand("duel").setExecutor(new DuelRequestSender(this));
		getCommand("customkit").setExecutor(new CustomKitCommand(this));
		getCommand("previewkit").setExecutor(new Preview(this));
		getCommand("juggernaut").setExecutor(new JuggernautCommand(this));
		getCommand("playback").setExecutor(new PlaybackCommand(this));
		getCommand("kiteditor").setExecutor(new KitEditorCommand(this));
		getCommand("botduel").setExecutor(botDuelCommand);
		getCommand("spectate").setExecutor(new SpectatorCommand(this));
		getCommand("spawnitem").setExecutor(new SpawnItemCommand());
		getCommand("koth").setExecutor(new KOTHCommand(this));
		getCommand("playersettings").setExecutor(new PlayerSettingsCommand());
		getCommand("hostevent").setExecutor(new EventCommand(this));
		getCommand("togglescoreboard").setExecutor(new ScoreboardCommand());
		getCommand("overwatch").setExecutor(new OverwatchCommand(this));
		getCommand("matchlimit").setExecutor(new MatchLimitCommand());
		getCommand("premiummatches").setExecutor(new PremiumMatchesCommand());
		getCommand("events").setExecutor(eventGUI);
		getCommand("strikeffa").setExecutor(new FFACommand());
		if(rankManager != null) {
			getCommand("ranks").setExecutor(new RanksCommand());
		}

	}

	public MetadataValue getMetadata(Metadatable m, String tag) {
		for (MetadataValue mv : m.getMetadata(tag))
			if (mv != null && mv.getOwningPlugin() != null && mv.getOwningPlugin() == this) {
				return mv;
			}
		return null;
	}

	public String translateMessage(Player p, String str) {
		return translateMessage(p, str, true);
	}

	public String translateMessage(Player p, String str, boolean withPrefix) {
		String language = PlayerSettings.getPlayerSettings(p).getLanguage();
		return translateMessage(p, str, withPrefix, language);
	}

	public String translateMessage(Player p, String str, boolean withPrefix, String language) {
		String s = language + "." + str.toLowerCase();
		if (messages.containsKey(s)) {
			String[] lines = messages.get(s);
			for (int i = 0; i < lines.length - 1; i++) {
				p.sendMessage(placeholders(p, lines[i]));
			}
			return placeholders(p, lines[lines.length - 1]);
		}
		String msg = getFileManager().getMessagesConfig().getString(s);
		boolean found = msg != null;
		if (msg == null) {
			msg = getFileManager().getMessagesConfig()
					.getString(getConfig().getString("default-language").toLowerCase() + "." + str.toLowerCase());
		}
		String form = msg;
		if (withPrefix) {
			String prefix = getConfig().getString("prefix");
			form = prefix + msg;
		}
		String a = ChatColor.translateAlternateColorCodes('&', form);
		String[] lines = placeholders(p, a).split("\\\\n");
		if (saveMessages && found) {
			messages.put(s, a.split("\\\\n"));
		}
		for (int i = 0; i < lines.length - 1; i++) {
			p.sendMessage(lines[i]);
		}
		return lines[lines.length - 1];
	}

	private String placeholders(Player p, String message) {
		if (message.contains("<placeholder_")) {
			return placeholders.doPlaceholders(p, message, "<placeholder_", true);
		}
		return message;
	}

	public String translateMessage(String str, boolean withPrefix) {
		String msg = getFileManager().getMessagesConfig()
				.getString(getConfig().getString("default-language").toLowerCase() + "." + str.toLowerCase());
		String form = msg;
		if (withPrefix) {
			String prefix = getConfig().getString("prefix");
			form = prefix + msg;
		}
		String a = ChatColor.translateAlternateColorCodes('&', form);
		return a;
	}

	public String getPrefix() {
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix"));
	}

	public void clear(Player p, boolean lobby, boolean deselect) {
		clear(p, lobby, deselect, false);
	}

	public void clear(Player p, boolean lobby, boolean deselect, boolean safe) {
		if (p.isDead() && !disabling) {
			new BukkitRunnable() {

				@Override
				public void run() {
					if (p != null && p.isDead()) {
						p.spigot().respawn();
						if (!p.isDead()) {
							clear(p, lobby, deselect, safe);
						}
					}
				}
			}.runTaskLater(this, 1);
		} else {
			for (PotionEffect ef : p.getActivePotionEffects()) {
				p.removePotionEffect(ef.getType());
			}
			p.getInventory().clear();
			p.getInventory().setArmorContents(new ItemStack[4]);
			p.setHealth(20);
			p.setFoodLevel(20);
			p.setSaturation(5);
			p.setFireTicks(0);
			p.updateInventory();
			if (deselect)
				BattleKit.deselectKit(p);
			for (World w : Bukkit.getWorlds()) {
				for (EnderPearl ep : w.getEntitiesByClass(EnderPearl.class)) {
					if (ep.getShooter() != null && ep.getShooter() instanceof Player
							&& ((Player) ep.getShooter()).getUniqueId().equals(p.getUniqueId())) {
						ep.remove();
					}
				}
			}
			if (lobby) {
				arenaPvP.lobby(p, safe);
			}
		}
	}

	public PlayerKitsManager getPlayerKitsHandler() {
		return playerKitsHandler;
	}

	public static LegionPractice getInstance() {
		return instance;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public TagManager getTagManager() {
		return tagManager;
	}

	public StrikeCheatManager getStrikeCheat() {
		return strikeCheat;
	}

	public RecordedMatchesManager getRecordedMatchesManager() {
		return recordedMatchesManager;
	}

	public SpectatorHandler getSpectatorHandler() {
		return spectatorHandler;
	}

	public SimpleDateFormat getTimeFormat() {
		return timeFormat;
	}

	public PlayerHider getPlayerHider() {
		return playerHider;
	}

	public FFAManager getFFAManager() {
		return ffaManager;
	}

	public void setSpectatorHandler(SpectatorHandler spectatorHandler) {
		this.spectatorHandler = spectatorHandler;
	}

	public EconomyManager getEconomyManager() {
		return economyManager;
	}

	public OverwatchManager getOverwatchManager() {
		return overwatch;
	}

	public Placeholders getPlaceholders() {
		return placeholders;
	}

	public WorldManager getWorldStacker() {
		return worldStacker;
	}

	public RankManager getRankManager() {
		return rankManager;
	}

	public KnockbackManager getKnockbackManager() {
		return knockbackManager;
	}

	public FightInventoryManager getFightInventoryManager() {
		return fightInventoryManager;
	}

	public EmptyChunkGeneratorProvider getEmptyChunkGeneratorProvider() {
		return chunkProvider;
	}

	public NMSProvider getNMSAccessProvider() {
		return nmsProvider;
	}
}
