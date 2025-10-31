package tech.hexadevelopment.practice.party;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.party.PartyRequestSender;
import tech.hexadevelopment.practice.fights.party.actions.PartyActionsManager;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;
import tech.hexadevelopment.practice.utils.ClickableMessage;

public class PartyCommand implements CommandExecutor {


	private String cooldown = "LegionPracticePartyInviteCooldown";
	public static String chat = "LegionPracticePartyChat";
	public static String invite = "LegionPracticePartyInvite";
	private LegionPractice plugin;


	public PartyCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	private boolean canInviteAgain(Player p) {
		long l = plugin.getConfig().getLong("party-invite-cooldown");
		if(p.hasMetadata(cooldown)) {
			MetadataValue m = plugin.getMetadata(p, cooldown);
			if(m != null && m.value() != null && m.asLong()+l > System.currentTimeMillis()) {
				return false;
			}
		}
		p.setMetadata(cooldown, new FixedMetadataValue(plugin, System.currentTimeMillis()));
		return true;
	}
	
	private String getInviteTime(Player p) {
		long l = plugin.getConfig().getLong("party-invite-cooldown");
		if(p.hasMetadata(cooldown)) {
			MetadataValue m = plugin.getMetadata(p, cooldown);
			if(m != null && m.value() != null) {
				int s = (int) ((m.asLong()+l-System.currentTimeMillis())/1000);
				return s + "s ";
			}
		}
		return "0s";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length == 0 || (args.length > 0 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")))) {
				String lang = PlayerSettings.getPlayerSettings(p.getUniqueId()).getLanguage();
				if(lang == null || plugin.getFileManager().getMessagesConfig().getStringList(lang + ".party-help") == null) {
					lang = plugin.getConfig().getString("default-language");
				}
				List<String> list = plugin.getFileManager().getMessagesConfig().getStringList(lang + ".party-help");
				if(list.isEmpty()) list = plugin.getFileManager().getMessagesConfig().getStringList(plugin.getConfig().getString("default-language") + ".party-help");
				if(list.isEmpty()) list = plugin.getFileManager().getMessagesConfig().getStringList("english.party-help");
				for(String s : list) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
				}
				return true;
			}
			if(args[0].equalsIgnoreCase("admin")) {
				if(!PermissionsManager.hasPermission(p, Permission.ADMIN)) return true;
				Party party = Party.getParty(p);
				if(party != null) {
					Player target = Bukkit.getPlayer(party.getOwner());
					party.setOwner(p.getName());
					for(String s : party.getMembers()) {
						Player mem = Bukkit.getPlayer(s);
						if(mem != null) {
							mem.sendMessage(plugin.translateMessage(mem, "party-promoted").replace("<player>", p.getName()));
						}
					}
					if(Fight.getCurrentFight(target, plugin) == null) {
						plugin.arenaPvP.giveSpawnItems(target);
					}
					if(Fight.getCurrentFight(p, plugin) == null) {
						plugin.arenaPvP.giveSpawnItems(p);
					}
				}
				else p.sendMessage(plugin.translateMessage(p, "not-own-party"));
			}
			else if(args[0].equalsIgnoreCase("list")) {
				if(Party.getParty(p) != null) {
					p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
					p.closeInventory();
					return true;
				}
				if(PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
					p.sendMessage(plugin.translateMessage(p, "can-not-do-party-commands"));
					return true;
				}
				Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Open Parties");
				String inMatch = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.in-match-party"));
				String notInMatch = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.not-in-match-party"));
				String m = plugin.getConfig().getString("party.member");
				String memberFormat = m == null ? null : ChatColor.translateAlternateColorCodes('&', m);
				int limit = plugin.getConfig().getInt("party.show-members-limit");
				List<Party> parties = new ArrayList<Party>();
				for(Player pl : Bukkit.getOnlinePlayers()) {
					Party party = Party.getParty(pl);
					if(party != null && !parties.contains(party) && (party.getSettings().isPublicParty() || party.getSettings().isOpenParty())) {
						int counter = 0;
						ItemStack is = new ItemStack(Material.SKULL_ITEM);
						ItemMeta meta = is.getItemMeta();
						List<String> lore = new ArrayList<String>();
						parties.add(party);
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
						if(party.isInFight()) {
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
						if(counter == 53) break;
					}
				}
				p.openInventory(inv);
			}
			else if(args[0].equalsIgnoreCase("ffa")) {
				Party party = Party.getParty(p);
				if(party == null) {
					p.sendMessage(plugin.translateMessage(p, "not-own-party"));
				}
				else {
					PartyActionsManager.getPartyActionsManager().byName("partyffa").start(party);
				}
			}
			else if(args[0].toLowerCase().contains("bots")) {
				Party party = Party.getParty(p);
				if(party == null) {
					p.sendMessage(plugin.translateMessage(p, "not-own-party"));
				}
				else {
					PartyActionsManager.getPartyActionsManager().byName("partybots").start(party);
				}
			}
			else if(args[0].equalsIgnoreCase("discord")) {
				Party party = Party.getParty(p);
				if(party == null) {
					p.sendMessage(plugin.translateMessage(p, "not-own-party"));
				}
				else {
					if(party.discord == null && !Party.emptyDiscords.isEmpty()) {
						if(Party.emptyDiscords.size() == 1) {
							party.discord = Party.emptyDiscords.get(0);
							Party.emptyDiscords.remove(0);
						}
						else {
							party.discord = Party.emptyDiscords.get(LegionPractice.random.nextInt(Party.emptyDiscords.size()));
							Party.emptyDiscords.remove(0);
						}
					}
					p.sendMessage(ChatColor.GREEN + "Party Discord: " + ChatColor.YELLOW + (party.getDiscord() == null ? "no empty channels" : party.getDiscord()));
				}
			}
			else if(args[0].equalsIgnoreCase("setdiscord") && args.length > 1) {
				Party party = Party.getParty(p);
				if(party == null || !party.getOwner().equals(p.getName())) {
					p.sendMessage(plugin.translateMessage(p, "not-own-party"));
				}
				else {
					party.customDiscord = true;
					party.discord = args[1];
					p.sendMessage(ChatColor.GREEN + "Party Discord set: " + ChatColor.YELLOW + (party.getDiscord() == null ? "no empty channels" : party.getDiscord()));
				}
			}
			else if(args[0].toLowerCase().contains("playback") || args[0].toLowerCase().contains("replay")) {
				Party party = Party.getParty(p);
				if(party == null) {
					p.sendMessage(plugin.translateMessage(p, "not-own-party"));
				}
				else {
					PartyActionsManager.getPartyActionsManager().byName("partyplayback").start(party);
				}
			}
			else if(args[0].equalsIgnoreCase("tdm") || args[0].equalsIgnoreCase("split") || args[0].equalsIgnoreCase("partysplit")) {
				Party party = Party.getParty(p);
				if(party == null) {
					p.sendMessage(plugin.translateMessage(p, "not-own-party"));
				}
				else {
					PartyActionsManager.getPartyActionsManager().byName("partysplit").start(party);
				}
			}
			else if(args[0].equalsIgnoreCase("force")) {
				if(!PermissionsManager.hasPermission(p, Permission.ADMIN)) return true;
				if(Party.getParty(p) != null) {
					p.sendMessage(plugin.translateMessage(p, "in-party"));
					return true;
				}
				if(Bukkit.getPlayer(args[1]) == null) {
					p.sendMessage(plugin.translateMessage(p, "not-online"));
					return true;
				}
				if(PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
					p.sendMessage(plugin.translateMessage(p, "can-not-do-party-commands"));
					return true;
				}
				else {
					QueueManager.leaveQueue(p, true);
					Player pl = Bukkit.getPlayer(args[1]);
					if(Party.getParty(pl) == null) {
						p.sendMessage(plugin.translateMessage(p,"does-not-have-party"));
						return true;
					}
					Party party = Party.getParty(pl);
					if(party == null) {
						p.sendMessage(plugin.translateMessage(p,"does-not-have-party"));
						return true;
					}
					party.getMembers().add(p.getName());
					p.setMetadata(plugin.META_IN_PARTY, new FixedMetadataValue(plugin, party));
					for(String s : party.getMembers()) {
						Player mem = Bukkit.getPlayer(s);
						mem.sendMessage(plugin.translateMessage(mem, "party-joined").replace("<player>", p.getName()));
					}
				}
			}
			else if(args[0].equalsIgnoreCase("leave")) {
				Party party = Party.getParty(p);
				if(party != null) {
					if(party.getOwner().equals(p.getName())) {
						p.sendMessage(plugin.translateMessage(p, "disband-to-leave"));
						return true;
					}
					for(String s : party.getMembers()) {
						Player mem = Bukkit.getPlayer(s);
						if(mem != null) {
							mem.sendMessage(plugin.translateMessage(mem, "left-party").replace("<player>", p.getName()));
						}
					}
					if(party.isInFight()) {
						Fight fight = Fight.getCurrentFight(p, plugin);
						if(fight != null) {
							fight.handleDeath(p);
							plugin.arenaPvP.lobby(p);
						}
					}
					if(Fight.getCurrentFight(p, plugin) == null && !plugin.getSpectatorHandler().isSpectator(p)) {
						party.getMembers().remove(p.getName());
						p.removeMetadata(plugin.META_IN_PARTY, plugin);
						plugin.arenaPvP.lobby(p);
					}
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase("settings") || args[0].equalsIgnoreCase("public")
					|| args[0].equalsIgnoreCase("private") || args[0].equalsIgnoreCase("limit")) {
				Party party = Party.getParty(p);
				if(party == null) return true;
				if(!party.getOwner().equals(p.getName())) {
					p.sendMessage(plugin.translateMessage(p, "not-own-party"));
					return true;
				}
				p.openInventory(party.getSettings().getSettingsMenu());
			}
			else if(args[0].equalsIgnoreCase("info")) {
				if(args.length == 2){
					Player tar = Bukkit.getPlayer(args[1]);
					if(tar == null) {
						p.sendMessage(plugin.translateMessage(p, "not-online"));
						return true;
					}
					if(Party.getParty(tar) == null) {
						p.sendMessage(plugin.translateMessage(p, "does-not-have-party"));
						return true;
					}
					Party party = Party.getParty(tar);
					if(party == null) {
						p.sendMessage(plugin.translateMessage(p, "does-not-have-party"));
						return true;
					}
					String size = Integer.toString(party.getMembers().size());
					String owner = party.getOwner();
					String members = "";
					String comma = "";
					for(String s : party.getMembers()) {
						members += comma + s;
						comma = ", ";
					}
					for(String s : plugin.getConfig().getStringList("party.info-command")) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', s).replace("<size>", size)
								.replace("<members>", members).replace("<leader>", owner).replace("<owner>", owner));
					}
					return true;
				}
				Party party = Party.getParty(p);
				if(party == null) {
					p.sendMessage(plugin.translateMessage(p, "does-not-have-party"));
					return true;
				}
				if(party.getMembers() == null && party.getMembers().isEmpty()) return true;
				String size = Integer.toString(party.getMembers().size());
				String owner = party.getOwner();
				String members = "";
				String comma = "";
				for(String s : party.getMembers()) {
					members += comma + s;
					comma = ", ";
				}
				for(String s : plugin.getConfig().getStringList("party.info-command")) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', s).replace("<size>", size)
							.replace("<members>", members).replace("<leader>", owner).replace("<owner>", owner));
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase("chat")) {
				if(Party.getParty(p) != null) {
					if(p.hasMetadata(chat)) {
						p.removeMetadata(chat, plugin);
						p.sendMessage(plugin.translateMessage(p, "party-chat-leave"));
					}
					else {
						p.setMetadata(chat, new FixedMetadataValue(plugin, true));
						p.sendMessage(plugin.translateMessage(p, "party-chat-join"));
					}
				}
			}
			else if(args[0].equalsIgnoreCase("invite") && args.length > 1) {
				Party party = Party.getParty(p);
				if(party != null && party.getOwner().equals(p.getName())) {
					Player mate = Bukkit.getPlayer(args[1]);
					if(mate != null) {
						if(mate.getName().equals(p.getName())) return true;
						if(party.getMembers().contains(mate.getName()) || party.getInvited().contains(mate.getName())) {
							p.sendMessage(plugin.translateMessage(p, "alread-invited"));
						}
						else {
							if(canInviteAgain(p)) {
								p.sendMessage(plugin.translateMessage(p, "sent-invite"));
								sendInvite(mate, p);
								party.getInvited().add(mate.getName());
							}
							else p.sendMessage(plugin.translateMessage(p, "cooldown-message").replace("<time>", getInviteTime(p)));
						}
					}
				}
				else p.sendMessage(plugin.translateMessage(p, "not-own-party"));
			}
			else if(args[0].equalsIgnoreCase("promote") && args.length > 1) {
				Party party = Party.getParty(p);
				if(party != null && party.getOwner().equals(p.getName())) {
					Player target = Bukkit.getPlayer(args[1]);
					if(target != null) {
						if(!party.getMembers().contains(target.getName())) {
							p.sendMessage(plugin.translateMessage(p, "not-in-your-party"));
							return true;
						}
						if(p.getName().equals(target.getName())) return true;
						party.setOwner(target.getName());
						for(String s : party.getMembers()) {
							Player mem = Bukkit.getPlayer(s);
							if(mem != null) {
								mem.sendMessage(plugin.translateMessage(mem, "party-promoted").replace("<player>", target.getName()));
							}
						}
						if(Fight.getCurrentFight(target, plugin) == null) {
							plugin.arenaPvP.giveSpawnItems(target);
						}
						if(Fight.getCurrentFight(p, plugin) == null) {
							plugin.arenaPvP.giveSpawnItems(p);
						}
					}
					else p.sendMessage(plugin.translateMessage(p, "not-online"));
				}
				else p.sendMessage(plugin.translateMessage(p, "not-own-party"));
			}
			/*
			 if(p.hasMetadata(plugin.META_IN_PARTY)) {
				if(Party.getParty(p).isInFight()) {
					p.sendMessage(plugin.translateMessage(p, "can-not-do-party-commands"));
					return true;
				}
			}
			 */
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("create")) {
					if(Party.getParty(p) != null) {
						p.sendMessage(plugin.translateMessage(p, "in-party"));
					}
					else if(PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
						p.sendMessage(plugin.translateMessage(p, "can-not-do-party-commands"));
					}
					else {
						if(plugin.getConfig().getBoolean("party.create.costs")) {
							double d = plugin.getConfig().getDouble("party.create.amount");
							if(plugin.getEconomyManager().hasMoney(p, d)) {
								plugin.getEconomyManager().withdraw(p, d);
								createParty(p);
							}
							else p.sendMessage(plugin.translateMessage(p, "no-enough-money").replace("<needed>", d + ""));
						}
						else createParty(p);
					}
				}
				else if(args[0].equalsIgnoreCase("match") || args[0].equalsIgnoreCase("duel") || args[0].equalsIgnoreCase("fight")) {
					if(PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
						p.sendMessage(plugin.translateMessage(p, "can-not-do-party-commands"));
						return true;
					}
					PartyRequestSender.handleCommand(p, args, plugin);
				}
				else if(args[0].equalsIgnoreCase("join") && args.length > 1) {
					if(Party.getParty(p) != null) {
						p.sendMessage(plugin.translateMessage(p, "in-party"));
						return true;
					}
					if(Bukkit.getPlayer(args[1]) == null) {
						p.sendMessage(plugin.translateMessage(p, "not-online"));
						return true;
					}
					if(PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
						p.sendMessage(plugin.translateMessage(p, "can-not-do-party-commands"));
						return true;
					}
					else {
						QueueManager.leaveQueue(p, true);
						Player pl = Bukkit.getPlayer(args[1]);
						Party party = Party.getParty(pl);
						if(party == null) {
							p.sendMessage(plugin.translateMessage(p,"does-not-have-party"));
							return true;
						}
						if(party.getInvited().contains(p.getName()) || party.getSettings().isOpenParty() || party.getSettings().isPublicParty()) {
							if(party.getSettings().getMaxPlayerLimit() > party.getMembers().size()) {
								party.getMembers().add(p.getName());
								p.setMetadata(plugin.META_IN_PARTY, new FixedMetadataValue(plugin, party));
								party.getInvited().remove(p.getName());
								for(String s : party.getMembers()) {
									Player mem = Bukkit.getPlayer(s);
									mem.sendMessage(plugin.translateMessage(mem, "party-joined").replace("<player>", p.getName()));
								}
								plugin.arenaPvP.giveSpawnItems(p);
								if(party.getFight() != null) {
									plugin.getSpectatorHandler().addSpectator(p, party.getFight());
									p.teleport(party.getFight().getArena().getCenter());
								}
							}
							else p.sendMessage(plugin.translateMessage(p, "party-full"));
						}
						else p.sendMessage(plugin.translateMessage(p, "has-not-invited"));
					}
				}
				else if(args[0].equalsIgnoreCase("kick") && args.length > 1) {
					Party party = Party.getParty(p);
					if(party != null && party.getOwner().equals(p.getName())) {
						Player target = Bukkit.getPlayer(args[1]);
						if(target != null) {
							if(!party.getMembers().contains(target.getName())) {
								p.sendMessage(plugin.translateMessage(p, "not-in-your-party"));
								return true;
							}
							if(target.getName().equals(p.getName())) return true;
							for(String s : party.getMembers()) {
								Player mem = Bukkit.getPlayer(s);
								if(mem != null) {
									mem.sendMessage(plugin.translateMessage(mem, "was-kicked").replace("<player>", target.getName()));
								}
							}
							if(party.isInFight()) {
								Fight fight = Fight.getCurrentFight(target, plugin);
								if(fight != null) {
									fight.handleDeath(target);
									plugin.arenaPvP.lobby(target);
								}
							}
							party.getMembers().remove(target.getName());
							target.removeMetadata(plugin.META_IN_PARTY, plugin);
							if(Fight.getCurrentFight(target, plugin) == null && !plugin.getSpectatorHandler().isSpectator(target)) {
								plugin.arenaPvP.lobby(target);
							}
							else {
								new BukkitRunnable() {
									
									@Override
									public void run() {
										if(target != null) {
											plugin.getSpectatorHandler().removeSpectator(target, true);
											plugin.arenaPvP.lobby(target);
										}
									}
								}.runTaskLater(plugin, 5);
							}
						}
						else p.sendMessage(plugin.translateMessage(p, "not-online"));
					}
					else p.sendMessage(plugin.translateMessage(p, "not-own-party"));
				}
				else if(args[0].equalsIgnoreCase("disband")) {
					Party party = Party.getParty(p);
					if(party != null && party.getOwner().equals(p.getName())) {
						for(String s : party.getMembers()) {
							Player mem = Bukkit.getPlayer(s);
							if(mem != null) {
								mem.sendMessage(plugin.translateMessage(mem, "party-was-deleted"));
							}
						}
						party.disbandParty();
					}
					else p.sendMessage(plugin.translateMessage(p, "not-own-party"));
				}
			}
		}
		return true;
	}

	private void createParty(Player p) {
		QueueManager.leaveQueue(p, true);
		new Party(p, plugin);
		p.sendMessage(plugin.translateMessage(p, "party-created"));
	}

	private void sendInvite(Player target, Player inviter) {
		target.sendMessage(plugin.translateMessage(target, "want-to-invite").replace("<player>", inviter.getName()));
		target.setMetadata(invite, new FixedMetadataValue(plugin, inviter.getName()));
		if(!plugin.getConfig().getBoolean("clickable-messages")) return;
		ClickableMessage.sendMessage(target, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("clickable-message")), "/LegionPractice:party join " + inviter.getName());
	}
}
