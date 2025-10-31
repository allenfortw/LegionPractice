package tech.hexadevelopment.practice.language;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;

public class LanguageItemCommand implements CommandExecutor{


	private LegionPractice plugin;

	public LanguageItemCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length < 2) {
				p.sendMessage(ChatColor.GRAY + "/langitem create <language> <slot>");
				p.sendMessage(ChatColor.GRAY + "/langitem delete <slot>");
			}
			else if(args.length == 3){
				if(args[0].equalsIgnoreCase("create")) {
					if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
						p.sendMessage(ChatColor.RED + "Hold something in your hand.");
					}
					else {
						int i;
						try{
							i = Integer.parseInt(args[2]);
						}catch(NumberFormatException e) {
							p.sendMessage(ChatColor.RED + "That's not a number!");
							return true;
						}
						if(i > 54 || i < 1) {
							p.sendMessage(ChatColor.RED + "The slot must be between 1 and 54");
							return true;
						}
						LanguageItem guiItem = new LanguageItem(p.getItemInHand(), args[1].replace("_", " "), i-1);
						plugin.languageItems.add(guiItem);
						p.sendMessage(ChatColor.BLUE + "Created a new language item.");
					}
				}
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("delete")) {
				int i;
				try{
					i = Integer.parseInt(args[1])-1;
				}catch(NumberFormatException e) {
					p.sendMessage(ChatColor.RED + "That's not a number!");
					return true;
				}
				Iterator<LanguageItem> it = plugin.languageItems.iterator();
				while(it.hasNext()) {
					LanguageItem g = it.next();
					if(g.getSlot() == i) {
						plugin.languageItems.remove(g);
						p.sendMessage(ChatColor.BLUE + "Deleted an old language item. (slot " + args[1] + ")");
						break;
					}
				}
			}
		}
		return true;
	}
}
