package tech.hexadevelopment.practice.fights.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.battlekit.BattleKitType;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.preview.Preview;
import tech.hexadevelopment.practice.scoreboard.ScoreboardUpdater;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.strikecheat.StrikeCheatCallback;

/**
 * Queue system handler.
 * Also a listener class.
 * @author Toppe5
 * @since 0.1
 */
public class QueueManager implements Listener, CommandExecutor{

	public static String eloCooldown = "LegionPracticeEloFightsCooldown";
	public static String queueCooldown = "LegionPracticeQueueFightsCooldown";
	public static String waitingQueue = "LegionPracticeWaitingQueue";

	public static HashMap<BattleKit, String> unranked = new HashMap<BattleKit, String>();
	public static HashMap<BattleKit, List<QueueRunnable>> ranked = new HashMap<BattleKit, List<QueueRunnable>>();

	private static Inventory rankedQueue;
	private static Inventory mainQueue;
	private static Inventory premiumQueue;
	private static int loadTry;
	private LegionPractice plugin;

	/**
	 * @param plugin LegionPractice plugin.
	 */
	public QueueManager(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("leave")) {
					leaveQueue(p, false);
					if(!Fight.isInFight(p, plugin)) {
						LegionPractice.getInstance().arenaPvP.giveSpawnItems(p);
					}
					p.sendMessage(plugin.translateMessage(p, "left-queue"));
					return true;
				}
			}
			selectQueueKit(p, label);
		}
		return true;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		leaveQueue(e.getPlayer(), true);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		leaveQueue(e.getEntity(), true);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		if(item == null || e.getInventory() == null) return;
		boolean premiumQueue = e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("premium-queue-inventory-title")));
		boolean rankedQueue = premiumQueue || e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ranked-queue-inventory-title")));
		if(!rankedQueue && !e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("queue-inventory-title")))) return;
		e.setCancelled(true);
		if(!(e.getWhoClicked() instanceof Player) || item.getType().equals(Material.AIR) || e.getClickedInventory() == null) return;
		if(LegionPractice.performanceMode) e.getWhoClicked().closeInventory();
		Player p = (Player) e.getWhoClicked();
		if(e.getClickedInventory().equals(p.getInventory())) return;
		List<BattleKit> kits = BattleKit.getKits(item);
		BattleKit kit = null;
		for(BattleKit bk : kits) {
			if(rankedQueue && bk.isElo()) {
				kit = bk;
				break;
			}
			else if(!rankedQueue && !bk.isElo()) {
				kit = bk;
				break;
			}
		}
		if(kit == null && kits.size() > 0) {
			kit = kits.get(0);
		}
		if(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
			if(kit != null && plugin.getConfig().getBoolean("preview.shift-click-preview")) {
				Preview.preview(p, kit, plugin);
				return;
			}
		}
		if(!canJoin(p)) return;
		boolean leaveItem = plugin.getConfig().getBoolean("leave-queue-item");
		if(leaveItem) {
			if(item.getType().equals(Material.WOOL) && item.getItemMeta().hasDisplayName()) {
				if(e.getSlot() == e.getInventory().getSize()-1) {
					leaveQueue(p, false);
					LegionPractice.getInstance().arenaPvP.giveSpawnItems(p);
					p.sendMessage(plugin.translateMessage(p, "left-queue"));
					return;
				}
			}
		}
		if(item.getItemMeta().hasDisplayName()) {
			if(e.getSlot() == e.getInventory().getSize()-(leaveItem ? 2 : 1)) {
				if(plugin.getConfig().getBoolean("kit-editor-in-kit-selector")) {
					plugin.getPlayerKitsHandler().openKitEditorSelector(p);
					return;
				}
			}
		}
		tryToJoin(p, kit, premiumQueue);
	}

	public void tryToJoin(Player p, BattleKit kit, boolean premiumQueue) {
		int ping = plugin.getNMSAccessProvider().getAccess().getPing(p);
		if(kit == null) {
			p.sendMessage(ChatColor.RED + "Error: invalid kit... try another kit and contact admins!");
			return;
		}
		PlayerStats st;
		if(!PlayerStats.getStats().containsKey(p.getUniqueId())) {
			st = new PlayerStats(p.getUniqueId(), true);
			p.sendMessage(ChatColor.RED + "Please try again!");
			return;
		}
		st = PlayerStats.getStats(p.getUniqueId());
		if(kit.isElo() && !premiumQueue && plugin.getConfig().getBoolean("limit-rankeds") && st.getRankedsLeft() <= 0) {
			p.sendMessage(plugin.translateMessage(p, "no-rankeds-left"));
			return;
		}
		if(!kit.isElo() && !premiumQueue && plugin.getConfig().getBoolean("limit-unrankeds") && st.getUnrankedsLeft() <= 0) {
			p.sendMessage(plugin.translateMessage(p, "no-unrankeds-left"));
			return;
		}
		if(premiumQueue) {
			if(st.getPremiumMatches() <= 0) {
				p.sendMessage(plugin.translateMessage(p, "no-premium-matches-left"));
				return;
			}
		}
		if(kit.isElo() && ping > plugin.getConfig().getInt("max-ranked-queue-ping")){
			p.sendMessage(plugin.translateMessage(p, "queue-ping-limit").replace("<ping>", Integer.toString(ping)));
			return;
		}
		if(p.hasMetadata(queueCooldown) && !PermissionsManager.hasPermission(p, Permission.COOLDOWN_BYPASS)) {
			if(plugin.getConfig().getBoolean("queue-cooldown")) {
				MetadataValue m = plugin.getMetadata(p, queueCooldown);
				if(m != null && m.value() != null) {
					int x = plugin.getConfig().getInt("queue-cooldown-time")*1000;
					if(m.asLong()+x > System.currentTimeMillis()) {
						p.sendMessage(plugin.translateMessage(p, "queue-cooldown").replace("<time>", (m.asLong()+x - System.currentTimeMillis())/1000 + ""));
						p.closeInventory();
						return;
					}
				}
			}
		}
		if(kit.isElo() && p.hasMetadata(queueCooldown) && !PermissionsManager.hasPermission(p, Permission.COOLDOWN_BYPASS)) {
			if(plugin.getConfig().getBoolean("elo-queue-cooldown")) {	
				MetadataValue m = plugin.getMetadata(p, queueCooldown);
				if(m != null && m.value() != null) {
					int x = plugin.getConfig().getInt("elo-queue-cooldown-time")*1000;
					if(m.asLong()+x > System.currentTimeMillis()) {
						p.sendMessage(plugin.translateMessage(p, "elo-queue-cooldown").replace("<time>", (m.asLong()+x - System.currentTimeMillis())/1000 + ""));
						p.closeInventory();
						return;
					}
				}
			}
		}
		leaveQueue(p, false);
		if(kit.isElo()) {
			int kills = PlayerStats.getStats(p.getUniqueId()).getKills();
			int required = plugin.getConfig().getInt("ranked.kills-required");
			if(kills < required) {
				p.sendMessage(plugin.translateMessage(p, "ranked-kills-required").replace("<kills>", kills + "").replace("<needed>", (required-kills) + "").replace("<required>", required + ""));
				return;
			}
		}
		if(kit.isAnticheatProtected()) {
			if(plugin.getStrikeCheat().getCallbacks().containsKey(p.getUniqueId())) {
				p.sendMessage(plugin.translateMessage(p, "do-not-spam-things"));
				return;
			}
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix") + "Sending client request..."));
			p.closeInventory();
			BattleKit protectedKit = kit;
			plugin.getStrikeCheat().requestCheck(p, new StrikeCheatCallback() {

				@Override
				public void isLegit(boolean result) {
					if(p != null && canJoin(p)) {
						leaveQueue(p, false);
						if(result) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix") + "Client accepted!"));
							addToQueue(p, protectedKit, premiumQueue);
						}
						else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix") + "Client declined!"));
						}
					}
				}
			});
		}
		else {
			addToQueue(p, kit, premiumQueue);
		}
	}

	private boolean canJoin(Player p) {
		if(Fight.getCurrentFight(p, plugin) != null || p.hasMetadata(plugin.IN_FIGHT) || PvPEvent.isInEvent(p)) {
			p.sendMessage(plugin.translateMessage(p, "you-can-not-duel-now"));
			return false;
		}
		if(Party.getParty(p) != null) {
			p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
			return false;
		}
		return true;
	}

	private void addToQueue(Player p, BattleKit kit, boolean premium) {
		p.sendMessage(plugin.translateMessage(p, "waiting-for-duel").replace("<kit>", kit.getFancyName()));
		if(kit.isElo()) {
			QueueRunnable qr = new QueueRunnable(plugin, p, kit, premium);
			List<QueueRunnable> list;
			if(ranked.containsKey(kit)) {
				list = ranked.get(kit);
			}
			else list = new ArrayList<QueueRunnable>();
			list.add(qr);
			ranked.put(kit, list);
			p.closeInventory();
			p.setMetadata(waitingQueue, new FixedMetadataValue(plugin, System.currentTimeMillis()));
			plugin.arenaPvP.giveSpawnItems(p);
			updateGUIs(true);
		}
		else if(unranked.containsKey(kit) && Bukkit.getPlayer(unranked.get(kit)) != null) {
			String s = unranked.get(kit);
			unranked.remove(kit);
			Bukkit.getPlayer(s).removeMetadata(waitingQueue, plugin);
			plugin.arenaPvP.giveSpawnItems(Bukkit.getPlayer(s));
			p.removeMetadata(waitingQueue, plugin);
			plugin.arenaPvP.giveSpawnItems(p);
			updateGUIs(true);
			p.closeInventory();
			Duel duel = new Duel(plugin, s, p.getName(), kit);
			if(duel.canStart()) {
				duel.setQueue(true);
				duel.start();
			}
			else {
				Arena.sendNoArenas(p);
				Player tar = Bukkit.getPlayer(s);
				if(tar.isOnline()) {
					Arena.sendNoArenas(tar);
				}
			}
		}
		else {
			p.closeInventory();
			unranked.put(kit, p.getName());
			updateGUIs(true);
			p.setMetadata(waitingQueue, new FixedMetadataValue(plugin, System.currentTimeMillis()));
			plugin.arenaPvP.giveSpawnItems(p);
		}
		ScoreboardUpdater.addUpdateNextTick(p.getUniqueId());
	}

	/**
	 * Leaves the player's current queue.
	 * It won't send any messages.
	 * @param p player to remove from queue.
	 * @param close if true the player's inventory will be closed, if the player's inventory won't be closed.
	 */
	public static void leaveQueue(Player p, boolean close) {
		List<BattleKit> bks = new ArrayList<BattleKit>();
		for(BattleKit bk : unranked.keySet()) {
			if(unranked.get(bk).equals(p.getName())) {
				bks.add(bk);
			}
		}
		QueueRunnable qr = QueueRunnable.getRanked(p);
		List<QueueRunnable> list;
		for(BattleKit kit : LegionPractice.getInstance().kits) {
			if(ranked.containsKey(kit)) {
				list = ranked.get(kit);
				list.remove(qr);
				ranked.put(kit, list);
			}
		}
		if(close) p.closeInventory();
		p.removeMetadata(waitingQueue, LegionPractice.getInstance());
		p.removeMetadata(QueueRunnable.RANKED_QUEUE, LegionPractice.getInstance());
		for(BattleKit b : bks) {
			unranked.remove(b);
		}
		ScoreboardUpdater.addUpdateNextTick(p.getUniqueId());
		updateGUIs(true);
	}

	public static void updateGUIs(boolean allowSkip) {
		if(allowSkip && LegionPractice.performanceMode && LegionPractice.random.nextInt(3) != 0) return;
		LegionPractice plugin = LegionPractice.getInstance();
		String inQueue = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("in-queue"));
		String inMatch = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("in-match"));
		boolean changeAmount = plugin.getConfig().getBoolean("change-icon-amount");
		boolean separate = plugin.getConfig().getBoolean("separate-queues");
		List<BattleKit> ks = new ArrayList<BattleKit>();
		for(BattleKit k : plugin.kits) {
			if(k.getTypes().contains(BattleKitType.ANY) || k.getTypes().contains(BattleKitType.PREMIUM_QUEUE) || k.getTypes().contains(BattleKitType.QUEUE)) {
				if(k.getIcon() != null && !ks.contains(k)) {
					ks.add(k);
				}
			}
		}
		HashMap<BattleKit, Integer> matches = new HashMap<BattleKit, Integer>();
		HashMap<BattleKit, Integer> premiumMatches = new HashMap<BattleKit, Integer>();
		for(Player p : Bukkit.getOnlinePlayers()) {
			Fight fight = Fight.getCurrentFight(p, plugin);
			if(fight != null && fight instanceof Duel && ((Duel) fight).isQueue()) {
				BattleKit k = fight.getKit();
				if(k != null) {
					int m = 1;
					if(((Duel) fight).isPremiumQueue()) {
						int i = 1;
						if(premiumMatches.containsKey(k)) {
							i += premiumMatches.get(k);
						}
						premiumMatches.put(k, i);
					}
					if(matches.containsKey(k)) {
						m += matches.get(k);
					}
					matches.put(k, m);
				}
			}
		}
		int rankedCounter = 0;
		int premiumCounter = 0;
		int unrankedCounter = 0;
		for(int i = 0; i < ks.size(); i++) {
			BattleKit bk = ks.get(i);
			ItemStack icon = bk.getIcon().clone();
			ItemMeta meta = icon.getItemMeta();
			List<String> lore = new ArrayList<String>();
			int queueSize = bk.isElo() ? rankedQueueSize(bk, false) : QueueManager.unranked.containsKey(bk) && Bukkit.getPlayer(QueueManager.unranked.get(bk)) != null ? 1 : 0;
			if(changeAmount) icon.setAmount(queueSize+1);
			if(bk.isElo() && ranked.containsKey(bk)) {
				lore.add(inQueue.replace("<players>", Integer.toString(ranked.get(bk).size())));
			}
			else if(QueueManager.unranked.containsKey(bk) && Bukkit.getPlayer(QueueManager.unranked.get(bk)) != null) {
				lore.add(inQueue.replace("<players>", Integer.toString(1)));
			}
			else {
				lore.add(inQueue.replace("<players>", Integer.toString(0)));
			}
			if(matches.containsKey(bk)) {
				lore.add(inMatch.replace("<players>", Integer.toString(matches.get(bk))));
			}
			else {
				lore.add(inMatch.replace("<players>", Integer.toString(0)));
			}
			meta.setLore(lore);
			icon.setItemMeta(meta);
			if(bk.isElo() && bk.getTypes().contains(BattleKitType.PREMIUM_QUEUE)) {
				try{
					ItemStack premiumIcon = icon.clone();
					icon.setAmount(rankedQueueSize(bk, true)+1);
					premiumQueue.setItem(premiumCounter, premiumIcon);
					premiumCounter++;
				}catch(Exception e){
					loadQueueInventories(plugin);
					return;
				}
			}
			if(bk.getTypes().contains(BattleKitType.QUEUE) || bk.getTypes().contains(BattleKitType.ANY)) {
				if(separate) {
					if(bk.isElo()) {
						try{
							rankedQueue.setItem(rankedCounter, icon.clone());
						}catch(Exception e){
							loadQueueInventories(plugin);
							return;
						}
						rankedCounter++;
					}
					else {
						try{
							mainQueue.setItem(unrankedCounter, icon);	
						}catch(Exception e){
							loadQueueInventories(plugin);
							return;
						}
						unrankedCounter++;
					}
				}
				else {
					try{
						mainQueue.setItem(i, icon);
					}catch(Exception e){
						loadQueueInventories(plugin);
						return;
					}
				}
			}
		}
	}
	
	private static int rankedQueueSize(BattleKit kit, boolean premium) {
		int c = 0;
		if(ranked.containsKey(kit)) {
			List<QueueRunnable> e = ranked.get(kit);
			for(QueueRunnable r : e) {
				if(r.isPremium() == premium) {
					c++;
				}
			}
		}
		return c;
	}

	/**
	 * Open the queue kit selector.
	 * @param p to whom the inventory should be opened.
	 */
	public void selectQueueKit(Player p, String label) {
		updateGUIs(true);
		if(label.equalsIgnoreCase("ranked") && plugin.getConfig().getBoolean("separate-queues")) {
			p.openInventory(rankedQueue);
		}
		else if(label.toLowerCase().contains("premium")) {
			if(PermissionsManager.hasPermission(p, Permission.PREMIUM_QUEUE)) {
				p.openInventory(premiumQueue);
			}
			else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + plugin.getConfig().getString("premium-permission-message")));
			}
		}
		else {
			p.openInventory(mainQueue);
		}
	}

	/**
	 * Intialize the queue kit selector inventories.
	 * @param plugin LegionPractice plugin.
	 */
	public static void loadQueueInventories(LegionPractice plugin) {
		if(loadTry > 50) return;
		loadTry++;
		if(mainQueue != null) {
			for(HumanEntity viewer : mainQueue.getViewers()) {
				viewer.closeInventory();
			}
			mainQueue.clear();
		}
		if(rankedQueue != null) {
			for(HumanEntity viewer : rankedQueue.getViewers()) {
				viewer.closeInventory();
			}
			rankedQueue.clear();
		}
		if(premiumQueue != null) {
			for(HumanEntity viewer : premiumQueue.getViewers()) {
				viewer.closeInventory();
			}
			premiumQueue.clear();
		}
		boolean separate = plugin.getConfig().getBoolean("separate-queues");
		int extra = 0;
		ItemStack editor = null;
		if(plugin.getConfig().getBoolean("kit-editor-in-kit-selector")) {
			extra++;
			editor = new ItemStack(Material.getMaterial(plugin.getConfig().getString("kit-selector-editor-material")));
			ItemMeta meta = editor.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("kit-selector-editor-name")));
			editor.setItemMeta(meta);
		}
		ItemStack quit = null;
		if(plugin.getConfig().getBoolean("leave-queue-item")) {
			extra++;
			quit = new ItemStack(Material.WOOL);
			quit.setDurability((short) 14);
			ItemMeta meta = quit.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("quit-item-name")));
			quit.setItemMeta(meta);
		}
		mainQueue = Bukkit.createInventory(null, plugin.arenaPvP.getSize(false, !separate, BattleKitType.QUEUE, extra), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("queue-inventory-title")));
		if(quit != null) {
			mainQueue.setItem(mainQueue.getSize()-1, quit);
		}
		if(editor != null) {
			if(quit == null) {
				mainQueue.setItem(mainQueue.getSize()-1, editor);
			}
			else {
				mainQueue.setItem(mainQueue.getSize()-2, editor);
			}
		}
		if(separate) {
			rankedQueue = Bukkit.createInventory(null, plugin.arenaPvP.getSize(false, true, true, BattleKitType.QUEUE, extra), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ranked-queue-inventory-title")));
			if(editor != null) {
				if(quit == null) {
					rankedQueue.setItem(rankedQueue.getSize()-1, editor);
				}
				else {
					rankedQueue.setItem(rankedQueue.getSize()-2, editor);
				}
			}
			if(quit != null) {
				rankedQueue.setItem(rankedQueue.getSize()-1, quit);
			}
		}
		premiumQueue = Bukkit.createInventory(null, plugin.arenaPvP.getSize(false, true, true, BattleKitType.QUEUE, extra), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("premium-queue-inventory-title")));
		if(editor != null) {
			if(quit == null) {
				premiumQueue.setItem(premiumQueue.getSize()-1, editor);
			}
			else {
				premiumQueue.setItem(premiumQueue.getSize()-2, editor);
			}
		}
		if(quit != null) {
			premiumQueue.setItem(premiumQueue.getSize()-1, quit);
		}
		updateGUIs(false);
	}
}
