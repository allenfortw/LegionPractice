package tech.hexadevelopment.practice.spawnitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tech.hexadevelopment.practice.LegionPractice;

public class SpawnItemCommand implements CommandExecutor{



	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(args.length > 1 && args[0].equalsIgnoreCase("give")){
			Player tar = Bukkit.getPlayer(args[1]);
			if(tar == null) {
				sender.sendMessage(ChatColor.RED + "The player is not online!");
			}
			else {
				sender.sendMessage(ChatColor.YELLOW + "Giving spawnitems...");
				LegionPractice.getInstance().arenaPvP.giveSpawnItems(tar);
			}
		}
		else if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length == 0) {
				p.sendMessage(ChatColor.GOLD + "/spawnitem create <name> <command> - (Hold the item in your hand in the slot)");
				p.sendMessage(ChatColor.GOLD + "/spawnitem move <name> - (Moves the spawnitem to the slot of your hand)");
				p.sendMessage(ChatColor.GOLD + "/spawnitem party <name>");
				p.sendMessage(ChatColor.GOLD + "/spawnitem partyOwner <name>");
				p.sendMessage(ChatColor.GOLD + "/spawnitem permission <name> <permission>");
				p.sendMessage(ChatColor.GOLD + "/spawnitem rename <name> <new item name>");
				p.sendMessage(ChatColor.GOLD + "/spawnitem material <name> - (Hold an item in your hand)");
				p.sendMessage(ChatColor.GOLD + "/spawnitem queue <name>");
				p.sendMessage(ChatColor.GOLD + "/spawnitem delete <name>");
				p.sendMessage(ChatColor.GOLD + "/spawnitem give <player>");
				p.sendMessage(ChatColor.GOLD + "/spawnitem list");
			}
			else if(args[0].equalsIgnoreCase("delete")) {
				if(args.length > 1) {
					SpawnItem found = null;
					for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
						if(si.getName().equals(args[1].toLowerCase())) {
							found = si;
						}
					}
					if(found == null) {
						p.sendMessage(ChatColor.RED + "Sorry, couldn't find that spawn item!");
					}
					else {
						LegionPractice.getInstance().spawnItems.remove(found);
						LegionPractice.getInstance().getFileManager().saveSpawnItems();
						p.sendMessage(ChatColor.YELLOW + "Deleted a spawn item!");
					}
				}
				else p.sendMessage(ChatColor.GOLD + "/spawnitem delete <name>");
			}
			else if(args[0].equalsIgnoreCase("party")) {
				if(args.length > 1) {
					boolean found = false;
					for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
						if(si.getName().equals(args[1].toLowerCase())) {
							found = true;
							si.setParty(!si.isParty());
							if(si.isParty()) {
								p.sendMessage(ChatColor.GOLD + si.getName() + " is now a party spawn item");
							}
							else {
								p.sendMessage(ChatColor.GOLD + si.getName() + " is no longer a party spawn item");
							}
						}
					}
					if(!found) {
						p.sendMessage(ChatColor.RED + "Sorry, couldn't find that spawn item!");
					}
					else {
						LegionPractice.getInstance().getFileManager().saveSpawnItems();
					}
				}
				else p.sendMessage(ChatColor.GOLD + "/spawnitem party <name>");
			}
			else if(args[0].equalsIgnoreCase("permission")) {
				if(args.length > 2) {
					boolean found = false;
					for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
						if(si.getName().equals(args[1].toLowerCase())) {
							ItemStack item = si.getItem();
							if(item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
								found = true;
								si.setPermission(args[2]);
								p.sendMessage(ChatColor.GREEN + "Permission (" + si.getPermission() + ") set for " + si.getName());
							}
						}
					}
					if(!found) {
						p.sendMessage(ChatColor.RED + "Sorry, couldn't find that spawn item!");
					}
					else {
						LegionPractice.getInstance().getFileManager().saveSpawnItems();
					}
				}
				else p.sendMessage(ChatColor.GOLD + "/spawnitem permission <name> <permission>");
			}
			else if(args[0].equalsIgnoreCase("rename")) {
				if(args.length > 2) {
					boolean found = false;
					for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
						if(si.getName().equals(args[1].toLowerCase())) {
							ItemStack item = si.getItem();
							if(item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
								found = true;
								ItemMeta meta = item.getItemMeta();
								meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[2].replace("_", " ")));
								item.setItemMeta(meta);
								p.sendMessage(ChatColor.GREEN + "Renamed a spawnitem. New name: " + ChatColor.RESET + meta.getDisplayName());
							}
						}
					}
					if(!found) {
						p.sendMessage(ChatColor.RED + "Sorry, couldn't find that spawn item!");
					}
					else {
						LegionPractice.getInstance().getFileManager().saveSpawnItems();
					}
				}
				else p.sendMessage(ChatColor.GOLD + "/spawnitem rename <name> <new item name>");
			}
			else if(args[0].equalsIgnoreCase("move")) {
				if(args.length > 1) {
					boolean found = false;
					for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
						if(si.getName().equals(args[1].toLowerCase())) {
							found = true;
							si.setSlot(p.getInventory().getHeldItemSlot());
							p.sendMessage(ChatColor.GOLD + si.getName() + " has been moved!");
						}
					}
					if(!found) {
						p.sendMessage(ChatColor.RED + "Sorry, couldn't find that spawn item!");
					}
					else {
						LegionPractice.getInstance().getFileManager().saveSpawnItems();
					}
				}
				else p.sendMessage(ChatColor.GOLD + "/spawnitem party <name>");
			}
			else if(args[0].equalsIgnoreCase("partyowner")) {
				if(args.length > 1) {
					boolean found = false;
					for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
						if(si.getName().equals(args[1].toLowerCase())) {
							found = true;
							si.setPartyOwnerOnly(!si.isPartyOwnerOnly());
							if(si.isPartyOwnerOnly()) {
								p.sendMessage(ChatColor.GOLD + si.getName() + " is now a party owner spawn item");
							}
							else {
								p.sendMessage(ChatColor.GOLD + si.getName() + " is no longer a party owner spawn item");
							}
						}
					}
					if(!found) {
						p.sendMessage(ChatColor.RED + "Sorry, couldn't find that spawn item!");
					}
					else {
						LegionPractice.getInstance().getFileManager().saveSpawnItems();
					}
				}
				else p.sendMessage(ChatColor.GOLD + "/spawnitem partyowner <name>");
			}
			else if(args[0].equalsIgnoreCase("queue")) {
				if(args.length > 1) {
					boolean found = false;
					for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
						if(si.getName().equals(args[1].toLowerCase())) {
							found = true;
							si.setQueue(!si.isQueue());
							if(si.isQueue()) {
								p.sendMessage(ChatColor.GOLD + si.getName() + " is now a queue item");
							}
							else {
								p.sendMessage(ChatColor.GOLD + si.getName() + " is no longer a queue item");
							}
						}
					}
					if(!found) {
						p.sendMessage(ChatColor.RED + "Sorry, couldn't find that spawn item!");
					}
					else {
						LegionPractice.getInstance().getFileManager().saveSpawnItems();
					}
				}
				else p.sendMessage(ChatColor.GOLD + "/spawnitem queue <name>");
			}
			else if(args[0].equalsIgnoreCase("list")) {
				if(LegionPractice.getInstance().spawnItems.size() == 0) {
					p.sendMessage(ChatColor.RED + "There are no spawn items yet.");
				}
				else {
					for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
						p.sendMessage(ChatColor.GOLD + "--------" + si.getName() + "--------");
						p.sendMessage(ChatColor.YELLOW + "Command: " + si.getCommand()
						+ ", Slot: " + si.getSlot() + ", Item: " + si.getItem().getType().toString() + ", Party: " + si.isParty()
						+ ", Party Owner Only: " + si.isPartyOwnerOnly() + ", Queue: " + si.isQueue());						
					}
				}
			}
			else if(args[0].equalsIgnoreCase("material")) {
				if(args.length > 1) {
					if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
						p.sendMessage(ChatColor.RED + "Hold something in your hand.");
						return true;
					}
					boolean found = false;
					for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
						if(si.getName().equals(args[1].toLowerCase())) {
							found = true;
							if(si.getItem() == null) si.setItem(p.getItemInHand());
							else si.getItem().setType(p.getItemInHand().getType());
							p.sendMessage(ChatColor.GREEN + "New material set: " + si.getItem().getType());
						}
					}
					if(!found) {
						p.sendMessage(ChatColor.RED + "Sorry, couldn't find that spawn item!");
					}
					else {
						LegionPractice.getInstance().getFileManager().saveSpawnItems();
					}
				}
				else p.sendMessage(ChatColor.GOLD + "/spawnitem material <name>");
			}
			else if(args[0].equalsIgnoreCase("create")) {
				if(args.length > 2) {
					if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
						p.sendMessage(ChatColor.RED + "Hold something in your hand.");
						return true;
					}
					for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
						if(si.getName().equalsIgnoreCase(args[1])) {
							p.sendMessage(ChatColor.RED + "That spawnitem already exists!");
							return true;
						}
					}
					String command = args[2].replace("_", " ");
					SpawnItem si = new SpawnItem(p, args[1].toLowerCase(), command);
					LegionPractice.getInstance().spawnItems.add(si);
					p.sendMessage(ChatColor.YELLOW + "Created a new spawn item.");
					LegionPractice.getInstance().getFileManager().saveSpawnItems();
				}
				else {
					p.sendMessage(ChatColor.GOLD + "/spawnitem create <name> <command>");
				}
			}
		}
		return true;
	}

}
