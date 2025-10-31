package tech.hexadevelopment.practice.fights.botduel;

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

import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.battlekit.BattleKitType;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.npc.DifficultySelector;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.preview.Preview;
import tech.hexadevelopment.practice.utils.VersionChecker;
import tech.hexadevelopment.practice.LegionPractice;

/**
 * Handles the duel requests.
 * Also a listener class.
 * @author Toppe5
 * @since 0.1
 */
public class BotDuelCommand implements CommandExecutor, Listener {

	private static boolean disabled;
	
	private LegionPractice plugin;

	public BotDuelCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onClick(InventoryClickEvent e){
		if(!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		if(e.getInventory().getHolder() != null && e.getInventory().getHolder() instanceof BotDuelHolder) {
			if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("inventory-title")))) {
				if(e.getClickedInventory() == null || e.getClickedInventory().equals(p.getInventory())) return;
				if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
					if(disabled) {
						p.sendMessage(ChatColor.RED + "PvP Bot has not been published yet!");
						return;
					}
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
					if(c != null && e.getCurrentItem().getItemMeta().hasDisplayName() && c.getName().equals(e.getCurrentItem().getItemMeta().getDisplayName())) {
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
					BotDuel botDuel = new BotDuel(plugin, p.getName(), kit);
					DifficultySelector.openSelector(p, botDuel);
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(disabled) {
				p.sendMessage(ChatColor.RED + "PvP Bot has not been published yet!");
				return true;
			}
			String site = VersionChecker.s;
			if(!site.equals("http://LegionPractice.ga/") || site.length() != 25
					|| LegionPractice.getInstance().arenaPvP.z().length() < 3) {
				return true;
			}
			if(p.hasMetadata(plugin.IN_FIGHT) || PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
				p.sendMessage(plugin.translateMessage(p, "you-can-not-duel-now"));
				return true;
			}
			else if(Party.getParty(p) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
				return true;
			}
			else if(!plugin.citizens) {
				p.sendMessage(ChatColor.RED + "Bot fight feature is disabled because Citizens plugin is missing!");
				return true;
			}
			plugin.arenaPvP.openKitSelector(p, new BotDuelHolder(), BattleKitType.BOT_FIGHT);
		}
		return true;
	}
	
	public static void disableBot() {
		disabled = false;
	}
}
