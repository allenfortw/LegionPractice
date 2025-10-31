package tech.hexadevelopment.practice.npc;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.fights.BotFight;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsBots;
import tech.hexadevelopment.practice.npc.CitizensNPC.Difficulty;
import tech.hexadevelopment.practice.utils.ItemStackUtil;
import tech.hexadevelopment.practice.LegionPractice;

public class DifficultySelector implements Listener {


	private static LegionPractice plugin;
	private static String title;


	public DifficultySelector(LegionPractice plugin) {
		DifficultySelector.plugin = plugin;
		title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bot-difficulty.title"));
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e){
		if(!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		if(e.getInventory().getHolder() != null && e.getInventory().getHolder() instanceof CombatNPCDifficultyHolder) {
			if(e.getInventory().getName().equals(title)) {
				if(e.getClickedInventory() == null || e.getClickedInventory().equals(p.getInventory())) return;
				if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
					e.setCancelled(true);
					CombatNPCDifficultyHolder holder = (CombatNPCDifficultyHolder) e.getInventory().getHolder();
					Difficulty difficulty = Difficulty.EASY;
					int size = e.getInventory().getSize();
					int x = size == 27 ? 9 : size == 45 ? 18 : size/2;
					if(x+7 > 8) x = 0;
					if(e.getSlot() == 3+x) {
						difficulty = Difficulty.NORMAL;
					}
					else if(e.getSlot() == 5+x) {
						difficulty = Difficulty.HARD;
					}
					else if(e.getSlot() == 7+x) {
						difficulty = Difficulty.HACKER;
					}
					holder.selectDifficultyAndStart(difficulty);
					p.closeInventory();
				}
			}
		}
	}

	public static void openSelector(Player p, BotFight fight) {
		int size = plugin.getConfig().getInt("bot-difficulty.inventory-size");
		Inventory inv = Bukkit.createInventory(new CombatNPCDifficultyHolder(fight, p), size, title);
		int[] slots = new int[]{1, 3, 5, 7};
		int x = size == 27 ? 9 : size == 45 ? 18 : size/2;
		if(x+7 > 8) x = 0;
		for(Difficulty difficulty : Difficulty.values()) {
			String s = difficulty.toString().toLowerCase();
			String name = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bot-difficulty." + s + "-name"));
			byte data = (byte) plugin.getConfig().getInt("bot-difficulty." + s + "-data");
			inv.setItem(slots[difficulty.ordinal()]+x, ItemStackUtil.createItem(Material.matchMaterial(plugin.getConfig().getString("bot-difficulty." + s + "-material")), name, data));
		}
		p.openInventory(inv);
	}
}


class CombatNPCDifficultyHolder implements InventoryHolder{

	
	private BotFight botFight;
	private UUID player;
	
	
	public CombatNPCDifficultyHolder(BotFight fight, Player p) {
		this.botFight = fight;
		this.player = p.getUniqueId();
	}

	public void selectDifficultyAndStart(Difficulty difficulty) {
		botFight.setDifficulty(difficulty);
		if(botFight instanceof BotDuel) {
			if(((BotDuel) botFight).canStart()) {
				((BotDuel) botFight).start();
			}
			else {
				Player p = Bukkit.getPlayer(player);
				if(p != null) {
					Arena.sendNoArenas(p);
				}
			}
		}
		else if(botFight instanceof PartyVsBots) {
			if(((PartyVsBots) botFight).canStart()) {
				((PartyVsBots) botFight).start();
			}
			else {
				Player p = Bukkit.getPlayer(player);
				if(p != null) {
					Arena.sendNoArenas(p);
				}
			}
		}
	}

	@Override
	public Inventory getInventory() {
		return null;
	}
}
