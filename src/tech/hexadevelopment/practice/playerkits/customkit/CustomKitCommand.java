package tech.hexadevelopment.practice.playerkits.customkit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.LegionPractice;

/**
 * Custom kit command handler class.
 * @author Toppe5
 * @since 0.1
 */
public class CustomKitCommand implements CommandExecutor{
	
	
	private LegionPractice plugin;
	
	/**
	 * 
	 * @param plugin LegionPractice plugin
	 */
	public CustomKitCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			if(PermissionsManager.hasPermission(sender, Permission.ADMIN) && args.length > 0 && args[0].equalsIgnoreCase("items")) {
				Inventory inv = Bukkit.createInventory(null, 54, "Edit Custom Kit Items");
				if(plugin.getConfig().get("custom-kit.items") != null) {
					for(Object o : plugin.getConfig().getList("custom-kit.items")) {
						if(o != null && o instanceof ItemStack) {
							inv.addItem((ItemStack) o);
						}
					}
				}
				((Player) sender).openInventory(inv);
				return true;
			}
			plugin.getPlayerKitsHandler().openCustomKit((Player) sender);
		}
		return true;
	}
}
