package tech.hexadevelopment.practice.scoreboard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;

public class ScoreboardCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			PlayerSettings.getPlayerSettings(p).setScoreboardDisabled(!PlayerSettings.getPlayerSettings(p).isScoreboardDisabled());
			ScoreboardManager.getScoreboardManager().updateScoreboard(p);
			if(PlayerSettings.getPlayerSettings(p).isScoreboardDisabled()) {
				p.sendMessage(LegionPractice.getInstance().getPrefix() + "Scoreboard disabled!");
			}
			else {
				p.sendMessage(LegionPractice.getInstance().getPrefix() + "Scoreboard enabled!");
			}
		}
		return true;
	}


}
