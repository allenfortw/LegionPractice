package tech.hexadevelopment.practice.fights.duel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.battlekit.BattleKitType;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.requests.DuelRequest;
import tech.hexadevelopment.practice.fights.requests.Request;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;
import tech.hexadevelopment.practice.preview.Preview;
import tech.hexadevelopment.practice.utils.ClickableMessage;
import tech.hexadevelopment.practice.utils.ItemStackUtil;
import tech.hexadevelopment.practice.utils.signgui.SignFinishCallback;
import tech.hexadevelopment.practice.utils.signgui.SignGUI;
import tech.hexadevelopment.practice.LegionPractice;

/**
 * Handles the duel requests.
 * Also a listener class.
 * @author Toppe5
 * @since 0.1
 */
public class DuelRequestSender implements CommandExecutor, Listener {

	private HashMap<UUID, Integer> bestofRounds = new HashMap<UUID, Integer>();
	private LegionPractice plugin;
	private static String cooldown = "LegionPracticeDuelCooldown";

	/**
	 * Create a new DuelRequestHandler.
	 * @param plugin LegionPractice plugin.
	 */
	public DuelRequestSender(LegionPractice plugin) {
		this.plugin = plugin;
	}

	/**
	 * Gets if the player is no longer in the duel cooldown. 
	 * @param p player to check.
	 * @return true of the player is no longer in the duel cooldown, false if he is.
	 */
	private boolean canDuelAgain(Player p) {
		long l = plugin.getConfig().getLong("request-cooldown");
		if(p.hasMetadata(cooldown)) {
			MetadataValue m = plugin.getMetadata(p, cooldown);
			if(m != null && m.value() != null && m.asLong()+l > System.currentTimeMillis()) {
				return false;
			}
		}
		p.setMetadata(cooldown, new FixedMetadataValue(plugin, System.currentTimeMillis()));
		return true;
	}
	
	private int getCooldownLeft(Player p) {
		long l = plugin.getConfig().getLong("request-cooldown");
		if(p.hasMetadata(cooldown)) {
			MetadataValue m = plugin.getMetadata(p, cooldown);
			if(m != null && m.value() != null && m.asLong()+l > System.currentTimeMillis()) {
				return (int) ((m.asLong()+l-System.currentTimeMillis())/1000);
			}
		}
		return 0;
	}

	@EventHandler
	public void onClick(InventoryClickEvent e){
		if(!(e.getWhoClicked() instanceof Player)) return;
		if(e.getClickedInventory() == null) return;
		Player p = (Player) e.getWhoClicked();
		if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("duel-inventory-pending")))) {
			if(e.getClickedInventory() == null || e.getClickedInventory().equals(p.getInventory())) return;
			if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
				e.setCancelled(true);
				if(e.getCurrentItem().getItemMeta().hasDisplayName() && e.getSlot() != e.getInventory().getSize()-1) {
					String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
					if(name.length() > 0) {
						if(p.getName().equals(name)) return;
						Player target = Bukkit.getPlayer(name);
						if(target == null) {
							p.sendMessage(plugin.translateMessage(p, "not-online"));
							return;
						}
						if(PvPEvent.isInEvent(target) || target.hasMetadata(plugin.IN_FIGHT) || Fight.getCurrentFight(target, plugin) != null) {
							p.sendMessage(plugin.translateMessage(p, "can-not-duel-that-player"));
							return;
						}
						else {
							HashSet<DuelRequest> reqs = Request.getDuelRequestsForPlayer(p);
							for(DuelRequest req : reqs) {
								if(!req.hasExpired() && req.getDueled().equals(p.getName())) {
									Duel duel = (Duel) req.getFight();
									if(duel.getP1().equals(target.getName()) && duel.getP2().equals(p.getName()) && duel.getKit() != null) {
										duel.handleStart();
										if(duel.canStart()){
											duel.start();
											reqs.remove(req);
											return;
										}
										Arena.sendNoArenas(p);
										return;
									}
								}
							}
						}
					}
				}
			}
		}
		if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("duel-inventory-title")))) {
			if(e.getClickedInventory() != null && e.getClickedInventory().equals(p.getInventory())) return;
			if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
				e.setCancelled(true);
			}
			if(e.getSlot() == 10) {
				Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("duel-inventory-pending")));
				HashSet<DuelRequest> reqs = Request.getDuelRequestsForPlayer(p);
				ArrayList<String> already = new ArrayList<String>();
				for(DuelRequest req : reqs) {
					if(!req.hasExpired() && req.getDueled().equals(p.getName())
							&& !already.contains(req.getDueler())) {
						Duel duel = (Duel) req.getFight();
						already.add(req.getDueler());
						ItemStack skull = ItemStackUtil.createPlayerSkull(ChatColor.GOLD + req.getDueler());
						List<String> lores = new ArrayList<String>();
						lores.add(ChatColor.YELLOW + "Kit: " + duel.getKit());
						inv.addItem(skull);
					}
				}
				inv.setItem(inv.getSize()-1, Preview.backButtonCurrentInv(p));
				p.openInventory(inv);
			}
			else if(e.getSlot() == 13) {
				SignGUI.openSignEditor(p, plugin.translateMessage(p, "type-name", false).split("/"), new SignFinishCallback() {

					@Override
					public void onFinish(String[] lines) {
						if(lines != null && lines.length > 0) {
							String name = lines[0].replace(" ", "");
							Player target = Bukkit.getPlayer(name);
							if(target != null) {
								if(p.getName().equals(target.getName())) return;
								if(target.hasMetadata(plugin.IN_FIGHT) || Fight.getCurrentFight(target, plugin) != null) {
									p.sendMessage(plugin.translateMessage(p, "can-not-duel-that-player"));
								}
								else if(PlayerSettings.getPlayerSettings(target).isDuelRequestsDisabled()) {
									p.sendMessage(plugin.translateMessage(p, "has-requests-disabled").replace("<player>", target.getName()));
								}
								else {
									Duel duel = new Duel(plugin, p.getName(), target.getName());
									plugin.arenaPvP.openKitSelector(p, new DuelHolder(p.getName(), duel), BattleKitType.DUEL);
								}
							}
						}
					}
				});
			}
			else if(e.getSlot() == 16) {
				if(PlayerSettings.getPlayerSettings(p).isDuelRequestsDisabled()) {
					PlayerSettings.getPlayerSettings(p).setDuelRequestsDisabled(false);
					p.sendMessage(plugin.translateMessage(p, "duel-requests-on"));
				}
				else {
					PlayerSettings.getPlayerSettings(p).setDuelRequestsDisabled(true);
					p.sendMessage(plugin.translateMessage(p, "duel-requests-off"));
				}
				updateToggleItem(p, e.getInventory());
			}
		}
		else if(e.getInventory().getHolder() != null && e.getInventory().getHolder() instanceof DuelHolder && ((DuelHolder) e.getInventory().getHolder()).getPlayer().equals(p.getName())) {
			if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("inventory-title")))) {
				if(e.getClickedInventory().equals(p.getInventory())) return;
				if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
					e.setCancelled(true);
					if(e.getCurrentItem().getItemMeta().hasDisplayName()) {
						if(e.getSlot() == e.getInventory().getSize()-1) {
							if(plugin.getConfig().getBoolean("kit-editor-in-kit-selector")) {
								plugin.getPlayerKitsHandler().openKitEditorSelector(p);
								return;
							}
						}
					}
					BattleKit kit = BattleKit.getKit(p, e.getCurrentItem(), false);
					BattleKit c = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
					if(c != null && e.getCurrentItem().equals(c.getIcon())) {
						kit = c;
					}
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Error: invalid kit.. please try another kit and contact admins!");
						return;
					}
					if(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
						if(plugin.getConfig().getBoolean("preview.shift-click-preview")) {
							Preview.preview(p, kit, plugin);
							return;
						}
					}
					DuelHolder h = (DuelHolder) e.getInventory().getHolder();
					Duel duel = h.getDuel();
					duel.setKit(kit);
					Player p2 = Bukkit.getPlayer(duel.getP2());
					if(!duel.getP1().equals(p.getName()) && !duel.getP2().equals(p.getName())) {
						p.closeInventory();
					}
					else if(p2 == null) {
						p.sendMessage(plugin.translateMessage(p, "not-online"));
						p.closeInventory();
					}
					else if(Party.getParty(p) != null) {
						p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
						p.closeInventory();
						return;
					}
					else if(Party.getParty(p2) != null) {
						p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party-other-player"));
						p.closeInventory();
					}
					else if(canDuelAgain(p)) {
						requestDuel(p, p2, kit);
						Request.addDuelRequest(p2, new DuelRequest(p, p2, duel));
						p.closeInventory();
					}
					else p.sendMessage(plugin.translateMessage(p, "cooldown-message").replace("<time>", getCooldownLeft(p) + ""));
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(p.hasMetadata(plugin.IN_FIGHT) || PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
				p.sendMessage(plugin.translateMessage(p, "you-can-not-duel-now"));
			}
			else if(Party.getParty(p) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
			}
			else if(args.length == 0) {
				if(plugin.getConfig().getBoolean("duel-inventory")) {
					openInventory(p, plugin);
				}
				else p.sendMessage(ChatColor.YELLOW + "/duel <player> || /duel accept <player> || /duel toggle");
				//else p.sendMessage(ChatColor.YELLOW + "/duel <player> [number, best of rounds] || /duel accept <player> || /duel toggle");
			}
			else if(args.length >= 1){
				if(args[0].equals("toggle")) {
					if(PlayerSettings.getPlayerSettings(p).isDuelRequestsDisabled()) {
						PlayerSettings.getPlayerSettings(p).setDuelRequestsDisabled(false);
						p.sendMessage(plugin.translateMessage(p, "duel-requests-on"));
					}
					else {
						PlayerSettings.getPlayerSettings(p).setDuelRequestsDisabled(true);
						p.sendMessage(plugin.translateMessage(p, "duel-requests-off"));
					}
				}
				else {
					Player target = Bukkit.getPlayer(args[0]);
					if(target != null) {
						if(p.getName().equals(target.getName())) return true;
						if(target.hasMetadata(plugin.IN_FIGHT) || Fight.getCurrentFight(target, plugin) != null) {
							p.sendMessage(plugin.translateMessage(p, "can-not-duel-that-player"));
						}
						else if(PlayerSettings.getPlayerSettings(target).isDuelRequestsDisabled()) {
							p.sendMessage(plugin.translateMessage(p, "has-requests-disabled").replace("<player>", target.getName()));
						}
						else {
							Duel duel = new Duel(plugin, p.getName(), target.getName());
							int bestof = 0;
							try{
								bestof = args.length > 1 ? Integer.parseInt(args[1]) : 0;
							}catch(Exception e) {}
							bestofRounds.put(p.getUniqueId(), bestof);
							duel.setBestOf(new BestOf(bestof));
							plugin.arenaPvP.openKitSelector(p, new DuelHolder(p.getName(), duel), BattleKitType.DUEL);
						}
					}
				}
			}
			if(args.length >= 2 && args[0].equalsIgnoreCase("accept")){
				if(p.getName().equals(args[1])) return true;
				Player target = Bukkit.getPlayer(args[1]);
				if(target == null) {
					p.sendMessage(plugin.translateMessage(p, "not-online"));
					return true;
				}
				if(PvPEvent.isInEvent(target) || target.hasMetadata(plugin.IN_FIGHT) || Fight.getCurrentFight(target, plugin) != null) {
					p.sendMessage(plugin.translateMessage(p, "can-not-duel-that-player"));
					return true;
				}
				else {
					HashSet<DuelRequest> reqs = Request.getDuelRequestsForPlayer(p);
					for(DuelRequest req : reqs) {
						if(!req.hasExpired() && req.getDueled().equals(p.getName())) {
							Duel duel = (Duel) req.getFight();
							if(duel.getP1().equals(target.getName()) && duel.getP2().equals(p.getName()) && duel.getKit() != null) {
								duel.handleStart();
								if(duel.canStart()){
									duel.start();
									reqs.remove(req);
									return true;
								}
								Arena.sendNoArenas(p);
								return true;
							}
						}
					}
				}
				p.sendMessage(plugin.translateMessage(p, "has-not-dueled"));
			}	
		}
		return true;
	}

	public static void openInventory(Player p, LegionPractice plugin) {
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("duel-inventory-title")));
		inv.setItem(10, ItemStackUtil.createItem(Material.BOOK, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("duel-inventory-pending"))));
		inv.setItem(13, ItemStackUtil.createItem(Material.SIGN, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("duel-inventory-duel"))));
		if(PlayerSettings.getPlayerSettings(p).isDuelRequestsDisabled()) {
			inv.setItem(16, ItemStackUtil.createItem(Material.REDSTONE_TORCH_ON, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("duel-inventory-toggle-disabled"))));
		}
		else {
			inv.setItem(16, ItemStackUtil.createItem(Material.TORCH, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("duel-inventory-toggle-enabled"))));
		}
		inv.setItem(inv.getSize()-1, Preview.backButtonCurrentInv(p));
		p.openInventory(inv);
	}

	private void updateToggleItem(Player p, Inventory inv) {
		if(PlayerSettings.getPlayerSettings(p).isDuelRequestsDisabled()) {
			inv.setItem(16, ItemStackUtil.createItem(Material.REDSTONE_TORCH_ON, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("duel-inventory-toggle-disabled"))));
		}
		else {
			inv.setItem(16, ItemStackUtil.createItem(Material.TORCH, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("duel-inventory-toggle-enabled"))));
		}
	}

	/**
	 * Sends the duel request.
	 * @param p who will sends the request.
	 * @param target the player who will get the request.
	 * @param kit the BattleKit that will able to be previewed.
	 */
	private boolean requestDuel(Player p, Player target, BattleKit kit) {
		if(p == null || target == null || kit == null) return false;
		p.sendMessage(plugin.translateMessage(p, "request-sent").replace("<player>", target.getName()).replace("<sender>", p.getName()));
		if(plugin.getConfig().getList("replaces-kits") == null || plugin.getConfig().getList("replaces-kits").isEmpty()) {
			BattleKit c = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
			if(c != null && c.getName().equals(kit.getName())) {
				if(plugin.getConfig().getBoolean("clickable-messages")) {
					UUID uuid = UUID.randomUUID();
					Preview.requestKits.put(uuid, c);
					String msg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview-custom-kit"));
					ClickableMessage.sendMessage(target, msg, "/previewkit " + uuid.toString());
				}
				target.sendMessage(plugin.translateMessage(target, "wants-duel-with-custom-kit").replace("<player>", p.getName()).replace("<kit>", kit.getFancyName()).replace("<combo>", kit.isCombo() + "").replace("<horse>", kit.isHorse() + "").replace("<bow>", kit.isOnlyBow() + ""));
			}
			else target.sendMessage(plugin.translateMessage(target, "wants-duel").replace("<player>", p.getName()).replace("<kit>", kit.getFancyName()).replace("<combo>", kit.isCombo() + "").replace("<horse>", kit.isHorse() + "").replace("<bow>", kit.isOnlyBow() + ""));
			int x = bestofRounds.getOrDefault(p.getUniqueId(), 0);
			if(x > 1 || kit.getBestOf() > 1) {
				if(x == 0) x = kit.getBestOf();
				target.sendMessage(plugin.translateMessage(target, "bestof-duel-request").replace("<rounds>", Integer.toString(x)));
				p.sendMessage(plugin.translateMessage(p, "bestof-duel-request").replace("<rounds>", Integer.toString(x)));
			}
			if(plugin.getConfig().getBoolean("clickable-messages")) {
				ClickableMessage.sendMessage(target, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("clickable-message")), "/duel accept " + p.getName());
				return true;
			}
		}
		else target.sendMessage(plugin.translateMessage(target, "wants-duel").replace("<player>", p.getName()).replace("<kit>", kit.getFancyName()).replace("<combo>", kit.isCombo() + "").replace("<horse>", kit.isHorse() + "").replace("<bow>", kit.isOnlyBow() + ""));
		int x = bestofRounds.getOrDefault(p.getUniqueId(), 0);
		if(x > 1 || kit.getBestOf() > 1) {
			if(x == 0) x = kit.getBestOf();
			target.sendMessage(plugin.translateMessage(target, "bestof-duel-request").replace("<rounds>", Integer.toString(x)));
			p.sendMessage(plugin.translateMessage(p, "bestof-duel-request").replace("<rounds>", Integer.toString(x)));
		}
		if(plugin.getConfig().getBoolean("clickable-messages")) {
			ClickableMessage.sendMessage(target, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("clickable-message")), "/duel accept " + p.getName());
		}
		return true;
	}
}
