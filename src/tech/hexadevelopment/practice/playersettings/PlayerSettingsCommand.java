package tech.hexadevelopment.practice.playersettings;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerSettingsCommand implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] arg3) {
		if(sender instanceof Player) {
			PlayerSettingsInventory.settingsInventory.openInventory((Player) sender);
		}
		return true;
	}

}
