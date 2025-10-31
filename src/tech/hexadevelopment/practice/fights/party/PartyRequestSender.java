package tech.hexadevelopment.practice.fights.party;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.battlekit.BattleKitType;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.party.actions.PartyAction;
import tech.hexadevelopment.practice.fights.party.actions.PartyActionsManager;
import tech.hexadevelopment.practice.fights.party.holders.PartyFFAHolder;
import tech.hexadevelopment.practice.fights.party.holders.PartySplitHolder;
import tech.hexadevelopment.practice.fights.party.holders.PartyVsBotsHolder;
import tech.hexadevelopment.practice.fights.party.holders.PartyVsPartyHolder;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyFFA;
import tech.hexadevelopment.practice.fights.party.partyfights.PartySplit;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsBots;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.fights.requests.PartyVsPartyRequest;
import tech.hexadevelopment.practice.fights.requests.Request;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.npc.DifficultySelector;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.preview.Preview;
import tech.hexadevelopment.practice.utils.ClickableMessage;

public class PartyRequestSender implements Listener {

	private LegionPractice plugin;
	public PartyRequestSender(LegionPractice plugin) {
		this.plugin = plugin;
	}

	public static void handleCommand(Player p, String[] args, LegionPractice plugin) {
		Party rParty = Party.getParty(p);
		if(rParty == null) return;
		if(rParty.isInFight() || PvPEvent.isInEvent(p)) {
			p.sendMessage(plugin.translateMessage(p, "can-not-do-party-commands"));
			return;
		}
		if(rParty != null && args.length > 0) {
			for(String s : rParty.getMembers()) {
				Player mem = Bukkit.getPlayer(s);
				if(mem == null) rParty.getMembers().remove(s);
				else if(Fight.getCurrentFight(mem, plugin) != null || PvPEvent.isInEvent(mem)) {
					p.sendMessage(ChatColor.RED + "Can't start! The player " + s + " is in a duel!");
					return;
				}
			}
			if(args.length == 1) {
				open(p, plugin);
				return;
			}
			else if(args.length > 1) {
				Player target = Bukkit.getPlayer(args[1]);
				if(target != null) {
					Party targetParty = Party.getParty(target);
					if(targetParty != null && p.hasMetadata(Request.REQUESTS) && targetParty.getOwner().equals(target.getName())) {
						Party tarParty = Party.getParty(target);
						if(rParty.isInFight() || tarParty.isInFight()) {
							p.sendMessage(plugin.translateMessage(p, "in-fight"));
							return;
						}
						HashSet<PartyVsPartyRequest> requests = Request.getPartyRequestsForPlayer(p);	
						for(PartyVsPartyRequest req : requests) {
							PartyVsParty pvp = (PartyVsParty) req.getFight();
							if(pvp.p2.equals(req.getDueled()) && pvp.p1.equals(req.getDueler()) && !req.hasExpired()) {
								if(pvp.canStart()) {
									pvp.start();
									return;
								}
								else {
									Arena.sendNoArenas(p);
									return;
								}
							}
						}
						p.sendMessage(plugin.translateMessage(p, "party-has-not-dueled"));
						return;
					}
				}
				open(p, plugin);
			}
		}
	}

	private static void open(Player p, LegionPractice plugin) {
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.inventory-title")));
		int counter = 0;
		String inMatch = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.in-match-party"));
		String notInMatch = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.not-in-match-party"));
		String m = plugin.getConfig().getString("party.member");
		String memberFormat = m == null ? null : ChatColor.translateAlternateColorCodes('&', m);
		int limit = plugin.getConfig().getInt("party.show-members-limit");
		for(Player pl : Bukkit.getOnlinePlayers()) {
			Party party = Party.getParty(pl);
			if(party != null && party.getOwner().equals(pl.getName())) {
				ItemStack is = new ItemStack(Material.SKULL_ITEM);
				ItemMeta meta = is.getItemMeta();
				List<String> lore = new ArrayList<String>();
				if(memberFormat != null) {
					int memberCounter = 0;
					for(String name : party.getMembers()) {
						Player member = Bukkit.getPlayer(name);
						if(member != null) {
							memberCounter++;
							lore.add(memberFormat.replace("<name>", member.getName()));
							if(memberCounter == limit && party.getMembers().size()-1 > memberCounter) {
								String limitReached = plugin.getConfig().getString("party.show-members-limit-reached");
								if(limitReached != null) {
									lore.add(ChatColor.translateAlternateColorCodes('&', limitReached.replace("<more_members>", (party.getMembers().size()-counter) + "")));
								}
								break;
							}
						}
					}
				}
				meta.setLore(lore);
				if(pl.getName().equals(p.getName())) {
					is.setDurability((short) 1);
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.your-party")).replace("<player>", pl.getName()));
				}
				else if(party.isInFight()) {
					is.setDurability((short) 0);
					meta.setDisplayName(inMatch.replace("<name>", pl.getName()));
				}
				else {
					is.setDurability((short) 3);
					meta.setDisplayName(notInMatch.replace("<name>", pl.getName()));
				}
				is.setItemMeta(meta);
				inv.addItem(is);
				counter++;
				if(counter == 44) break;
			}
		}
		for(PartyAction pa : PartyActionsManager.getPartyActionsManager().getPartyActions()) {
			inv.setItem(pa.getSlot(), pa.getIcon());
		}
		p.openInventory(inv);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
			Player p = (Player) e.getWhoClicked();
			if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.inventory-title")))) {
				e.setCancelled(true);
				Party party = Party.getParty(p);
				if(party != null) {
					if(!party.getOwner().equals(p.getName())) {
						p.sendMessage(plugin.translateMessage(p, "not-own-party"));
						return;
					}
					if(!party.getMembers().isEmpty()) {
						if(e.getCurrentItem().getType().equals(Material.SKULL_ITEM) && e.getCurrentItem().getDurability() == 3
								&& e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasLore()) {
							String mFormat = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.member"));
							String[] prefixSuffix = mFormat.split("<name>");
							String b = e.getCurrentItem().getItemMeta().getLore().get(0);
							if(prefixSuffix.length > 0) {
								b = b.replace(prefixSuffix[0], "");
							}
							else if(prefixSuffix.length > 1) {
								b = b.replace(prefixSuffix[1], "");
							}
							Player tar = Bukkit.getPlayer(b);
							if(tar == null || Party.getParty(tar) == null) {
								p.closeInventory();
								return;
							}
							else {
								Party oParty = Party.getParty(tar);
								Player target = Bukkit.getPlayer(oParty.getOwner());
								if(target != null && oParty != null && oParty.getOwner().equals(target.getName()) && !oParty.isInFight()) {
									plugin.arenaPvP.openKitSelector(p, new PartyVsPartyHolder(party, oParty), BattleKitType.PARTY_VS_PARTY);
								}
								else {
									p.sendMessage(plugin.translateMessage(p, "can-not-duel-that-party"));
									p.closeInventory();
								}	
							}
						}
						else {
							if(e.getCurrentItem().getItemMeta().hasDisplayName()) {
								PartyAction partyAction = PartyActionsManager.getPartyActionsManager().byIcon(e.getCurrentItem());
								if(partyAction != null) {
									partyAction.start(party);
								}
							}
						}
					}
				}
			}
			else if(e.getInventory().getName().equals(ChatColor.GREEN + "Open Parties")) {
				e.setCancelled(true);
				if(Party.getParty(p) != null) {
					p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
					p.closeInventory();
					return;
				}
				if(PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
					p.sendMessage(plugin.translateMessage(p, "can-not-do-party-commands"));
					return;
				}
				String mFormat = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.member"));
				String[] prefixSuffix = mFormat.split("<name>");
				String b = e.getCurrentItem().getItemMeta().getLore().get(0);
				if(prefixSuffix.length > 0) {
					b = b.replace(prefixSuffix[0], "");
				}
				else if(prefixSuffix.length > 1) {
					b = b.replace(prefixSuffix[1], "");
				}
				Player tar = Bukkit.getPlayer(b);
				if(tar == null || Party.getParty(tar) == null) {
					p.closeInventory();
					return;
				}
				p.chat("/party join " + b);
			}
			if(e.getInventory().getHolder() == null) return;
			if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("inventory-title")))) {
				e.setCancelled(true);
				if(e.getCurrentItem().getItemMeta().hasDisplayName()) {
					if(e.getSlot() == e.getInventory().getSize()-1) {
						if(plugin.getConfig().getBoolean("kit-editor-in-kit-selector")) {
							plugin.getPlayerKitsHandler().openKitEditorSelector(p);
							return;
						}
					}
				}
				if(e.getInventory().getHolder() instanceof PartyFFAHolder) {
					PartyFFAHolder h = (PartyFFAHolder) e.getInventory().getHolder();
					if(h.getParty().getOwner().equals(p.getName())) {
						BattleKit kit = BattleKit.getKit(p, e.getCurrentItem(), false);
						BattleKit c = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
						if(c != null && e.getCurrentItem().equals(c.getIcon())) {
							kit = c;
						}
						else if(kit == null) {
							p.sendMessage(ChatColor.RED + "Error: invalid kit.. please try another kit and contact admins!");
							return;
						}
						if(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
							if(plugin.getConfig().getBoolean("preview.shift-click-preview")) {
								Preview.preview(p, kit, plugin);
								return;
							}
						}
						Party party = Party.getParty(p);
						if(party.getMembers().size() < 2) {
							p.sendMessage(plugin.translateMessage(p, "not-enough-players"));
							p.closeInventory();
							return;
						}
						PartyFFA ffa = new PartyFFA(plugin, party, kit);
						if(ffa.canStart()) {
							ffa.start();
							p.closeInventory();
						}
						else {
							Arena.sendNoArenas(p);
						}
					}
				}
				else if(e.getInventory().getHolder() instanceof PartySplitHolder) {
					PartySplitHolder h = (PartySplitHolder) e.getInventory().getHolder();
					if(h.getParty().getOwner().equals(p.getName())) {
						BattleKit kit = BattleKit.getKit(p, e.getCurrentItem(), false);
						BattleKit c = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
						if(c != null && e.getCurrentItem().equals(c.getIcon())) {
							kit = c;
						}
						else if(kit == null) {
							p.sendMessage(ChatColor.RED + "Error: invalid kit.. please try another kit and contact admins!");
							return;
						}
						if(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
							if(plugin.getConfig().getBoolean("preview.shift-click-preview")) {
								Preview.preview(p, kit, plugin);
								return;
							}
						}
						Party party = h.getParty();
						if(party.getMembers().size() < 2) {
							p.sendMessage(plugin.translateMessage(p, "not-enough-players"));
							p.closeInventory();
							return;
						}
						PartySplit split = new PartySplit(plugin, party, kit);
						if(split.canStart()) {
							split.start();
							p.closeInventory();
						}
						else {
							Arena.sendNoArenas(p);
						}
					}
				}
				else if(e.getInventory().getHolder() instanceof PartyVsBotsHolder) {
					e.setCancelled(true);
					PartyVsBotsHolder h = (PartyVsBotsHolder) e.getInventory().getHolder();
					if(!h.getParty().getOwner().equals(p.getName())) return;
					BattleKit kit = BattleKit.getKit(p, e.getCurrentItem(), false);
					BattleKit c = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
					if(c != null && e.getCurrentItem().equals(c.getIcon())) {
						kit = c;
					}
					else if(kit == null) {
						p.sendMessage(ChatColor.RED + "Error: invalid kit.. please try another kit and contact admins!");
						return;
					}
					if(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
						if(plugin.getConfig().getBoolean("preview.shift-click-preview")) {
							Preview.preview(p, kit, plugin);
							return;
						}
					}
					PartyVsBots split = new PartyVsBots(plugin, h.getParty(), kit);
					DifficultySelector.openSelector(p, split);
				}
				else if(e.getInventory().getHolder() instanceof PartyVsPartyHolder) {
					e.setCancelled(true);
					PartyVsPartyHolder h = (PartyVsPartyHolder) e.getInventory().getHolder();
					if(!h.getParty().getOwner().equals(p.getName())) return;
					Player target = Bukkit.getPlayer(h.getParty2().getOwner());
					Party targetParty = Party.getParty(target);
					Party party = Party.getParty(p);
					if(targetParty != null && !targetParty.isInFight() && !party.isInFight()) {
						BattleKit kit = BattleKit.getKit(p, e.getCurrentItem(), false);
						BattleKit c = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
						if(c != null && e.getCurrentItem().equals(c.getIcon())) {
							kit = c;
						}
						if(kit == null) {
							p.sendMessage(ChatColor.RED + "Error: invalid kit, please try other kits and contact admins!");
							return;
						}
						PartyVsParty pvp = new PartyVsParty(plugin, party, targetParty, kit);
						Request.addPartyRequest(target, new PartyVsPartyRequest(party, targetParty, pvp));
						if(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
							if(plugin.getConfig().getBoolean("preview.shift-click-preview")) {
								Preview.preview(p, c, plugin);
								return;
							}
						}
						if(kit.equals(c)) {
							if(plugin.getConfig().getBoolean("clickable-messages")) {
								UUID uuid = UUID.randomUUID();
								Preview.requestKits.put(uuid, c);
								String msg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview-custom-kit"));
								for(String s : targetParty.getMembers()) {
									Player mem = Bukkit.getPlayer(s);
									if(mem != null) {
										ClickableMessage.sendMessage(mem, msg, "/previewkit " + uuid.toString());	
									}
								}
								for(String s : targetParty.getMembers()) {
									Player mem = Bukkit.getPlayer(s);
									if(mem != null) {
										mem.sendMessage(plugin.translateMessage(mem, "party-wants-duel-with-custom-kit").replace("<player>", p.getName()).replace("<kit>", c.getFancyName()).replace("<combo>", c.isCombo() + "").replace("<horse>", c.isHorse() + "").replace("<bow>", c.isOnlyBow() + ""));
									}
								}
							}
						}
						else {
							for(String s : targetParty.getMembers()) {
								Player mem = Bukkit.getPlayer(s);
								if(mem != null) {
									mem.sendMessage(plugin.translateMessage(target, "party-wants-duel").replace("<player>", p.getName()).replace("<kit>", kit.getFancyName()).replace("<combo>", kit.isCombo() + "").replace("<horse>", kit.isHorse() + "").replace("<bow>", kit.isOnlyBow() + ""));
								}
							}
						}
						p.closeInventory();
						p.sendMessage(plugin.translateMessage(p, "request-sent"));
						if(plugin.getConfig().getBoolean("clickable-messages")) {
							ClickableMessage.sendMessage(target, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("clickable-message")), "/LegionPractice:party match " + p.getName());
						}
					}
					else p.sendMessage(plugin.translateMessage(p, "in-fight"));
					return;
				}
			}
		}
	}
}
