package tech.hexadevelopment.practice.permissions;

import org.bukkit.command.CommandSender;

public class PermissionsManager {
	
	
	public static boolean hasPermission(CommandSender sender, Permission permission) {
		return sender.isOp() || sender.hasPermission(Permission.ADMIN.toString())
				|| sender.hasPermission(permission.toString()) || sender.hasPermission("strikepractice.*")
				|| (permission.toString().contains("hostevent") && sender.hasPermission("strikepractice.hostevent.*"));
	}
	
}
