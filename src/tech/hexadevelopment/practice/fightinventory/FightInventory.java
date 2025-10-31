package tech.hexadevelopment.practice.fightinventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackInventoryManager;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.ClickableMessage;
import tech.hexadevelopment.practice.utils.EffectUtil;
import tech.hexadevelopment.practice.utils.FormatUtils;
import tech.hexadevelopment.practice.utils.ItemStackUtil;
import tech.hexadevelopment.practice.utils.PlayerUtil;

public class FightInventory {

	private LegionPractice plugin;
	private String owner;
	private UUID ownerUUID;
	private ItemStack[] mainInv;
	private boolean dead;
	private Collection<PotionEffect> effects;
	private int food;
	private ItemStack helmet;
	private ItemStack chestplate;
	private ItemStack leggings;
	private ItemStack boots;
	private double health;
	private UUID uuid;
	private UUID playbackUUID;
	private int left;
	private long created;
	private boolean leftPotions;


	/**
	 * Create a FightInvntory with the given player's inventory, heatlh etc.
	 * This will also save the inventory
	 * @param p whose inventory, heatlh etc. are used to make a FightInventory
	 * @param plugin LegionPractice plugin
	 */
	public FightInventory(Player p, LegionPractice plugin) {
		if(p != null) {
			Damageable d = (Damageable) p;
			this.health = d.getHealth();
			this.helmet = p.getInventory().getHelmet();
			this.chestplate = p.getInventory().getChestplate();
			this.leggings = p.getInventory().getLeggings();
			this.boots = p.getInventory().getBoots();
			this.owner = p.getName();
			this.dead = p.isDead();
			this.food = p.getFoodLevel();
			this.effects = p.getActivePotionEffects();
			this.mainInv = p.getInventory().getContents();
			this.plugin = plugin;
			this.uuid = UUID.randomUUID();
			this.ownerUUID = p.getUniqueId();
			Fight fight = Fight.getCurrentFight(p, plugin);
			if(fight instanceof Duel && ((Duel) fight).getRecorder() != null && ((Duel) fight).getRecorder().getRecordedMatch() != null) {
				this.playbackUUID = ((Duel) fight).getRecorder().getRecordedMatch().getUUID();
			}
			save();
		}
	}

	public FightInventory(LegionPractice plugin) {
		this.plugin = plugin;
	}

	public FightInventory(Entity ent, String name, PlayerInventory inv, LegionPractice plugin) {
		if(ent instanceof Damageable) {
			this.health = ((Damageable) ent).getHealth();
		}
		this.helmet = inv.getHelmet();
		this.chestplate = inv.getChestplate();
		this.leggings = inv.getLeggings();
		this.boots = inv.getBoots();
		this.owner = name;
		this.dead = ent.isDead();
		this.food = 20;
		if(ent instanceof LivingEntity) {
			this.effects = ((LivingEntity) ent).getActivePotionEffects();
		}
		this.mainInv = inv.getContents();
		this.plugin = plugin;
		this.uuid = UUID.randomUUID();
		this.ownerUUID = ent.getUniqueId();
		save();
	}

	public void save() {
		created = System.currentTimeMillis();
		for(ItemStack is : mainInv) {
			if(is != null && is.getType() == Material.POTION) {
				short d = is.getDurability();
				if(d == 8197 || d == 8229 || d == 16389 || d == 16421) {
					left += is.getAmount() > 0 ? is.getAmount() : 1;
				}
			}
		}
		if(left == 0) {
			for(ItemStack is : mainInv) {
				if(is != null && is.getType() == Material.MUSHROOM_SOUP) {
					left += is.getAmount() > 0 ? is.getAmount() : 1;
				}
			}
		}
		else leftPotions = true;
		plugin.getFightInventoryManager().saveInventory(this);
	}

	/**
	 * Building the actual inventory.
	 * It will rebuild and save the new inventory if one already exists.
	 * @param p the player viewing
	 * @return the built inventory.
	 */
	public Inventory build(Player p) {
		int slots = getSlots();
		Inventory inv = Bukkit.createInventory(new FightInventoryHolder(), slots, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("fight-inventory.inventory").replace("<player>", owner)));
		int a = plugin.getConfig().getInt("fight-inventory.item-start-slot")-1;
		int counter = 0;
		for(int x = a; x < 36+a; x++) {
			if(x >= slots) break;
			else inv.setItem(x, getMainInv()[counter]);
			counter++;
		}
		inv.setItem(getSlot("helmet"), getHelmet());
		inv.setItem(getSlot("chestplate"), getChestplate());
		inv.setItem(getSlot("leggings"), getLeggings());
		inv.setItem(getSlot("boots"), getBoots());
		if(playbackUUID != null && plugin.citizens && PermissionsManager.hasPermission(p, Permission.PLAYBACK)) {
			for(List<RecordedMatch> matches : LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().values()) {
				for(RecordedMatch match : matches) {
					if(playbackUUID == match.getUUID()) {
						inv.setItem(getSlot("playback"), PlaybackInventoryManager.buildItem(match));	
						break;
					}
				}
			}
		}
		if(isDead()) {
			ItemStack dead = new ItemStack(Material.SKULL_ITEM);
			ItemMeta meta = dead.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					plugin.getConfig().getString("fight-inventory.dead")));
			dead.setItemMeta(meta);
			inv.setItem(getSlot("health"), dead);
		}
		else {
			long health = Math.round(getHealth());
			ItemStack hp = new ItemStack(Material.MELON, (int) health);
			ItemMeta meta = hp.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					plugin.getConfig().getString("fight-inventory.health")).replace("<health>", health + ""));
			hp.setItemMeta(meta);
			inv.setItem(getSlot("health"), hp);
		}
		ItemStack food = new ItemStack(Material.COOKED_BEEF, getFood());
		ItemMeta meta = food.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString("fight-inventory.food")).replace("<food>", getFood() + ""));
		food.setItemMeta(meta);
		inv.setItem(getSlot("food"), food);

		ItemStack ef = new ItemStack(Material.POTION);
		ItemMeta efMeta = food.getItemMeta();
		efMeta.setDisplayName(ChatColor.RED + "Effects: ");
		String effects = ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString("fight-inventory.pots"));
		efMeta.setLore(Arrays.asList(effects));
		List<String> lores = new ArrayList<String>();
		for(String s : efMeta.getLore()) {
			lores.add(s);
		}
		String f = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("fight-inventory.effect"));
		for(PotionEffect e : getEffects()) {
			lores.add(FormatUtils.formatPotionEffect(f, e));
		}
		efMeta.setLore(lores);
		ef.setItemMeta(efMeta);
		inv.setItem(getSlot("pots"), ef);

		ItemStack name = new ItemStack(Material.NAME_TAG);
		ItemMeta asd = name.getItemMeta();
		asd.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("fight-inventory.name")).replace("<name>", getOwner()));
		name.setItemMeta(asd);
		inv.setItem(getSlot("name"), name);
		if(left != 0) {
			if(leftPotions) {
				ItemStack pots = new ItemStack(Material.POTION);
				ItemMeta potsMeta = pots.getItemMeta();
				pots.setDurability((short) 16421);
				pots.setAmount(left);
				potsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("fight-inventory.pots-left")).replace("<potions>", left + ""));
				pots.setItemMeta(potsMeta);
				inv.setItem(getSlot("pots-left"), pots);
			}
			else {
				ItemStack soups = new ItemStack(Material.MUSHROOM_SOUP);
				ItemMeta soupsMeta = soups.getItemMeta();
				soups.setAmount(left);
				soupsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("fight-inventory.soups-left")).replace("<soups>", left + ""));
				soups.setItemMeta(soupsMeta);
				inv.setItem(getSlot("pots-left"), soups);
			}
		}
		return inv;
	}

	public int getSlots() {
		return 9*plugin.getConfig().getInt("fight-inventory.rows");
	}

	@Override
	public String toString() {
		String s = "";
		s += owner;
		s += ":" + ownerUUID.toString();
		s += ":" + uuid.toString();
		s += ":" + health;
		s += ":" + dead;
		s += ":" + food;
		s += ":" + ItemStackUtil.itemToString(helmet);
		s += ":" + ItemStackUtil.itemToString(chestplate);
		s += ":" + ItemStackUtil.itemToString(leggings);
		s += ":" + ItemStackUtil.itemToString(boots);
		String comma = "";
		s += ":";
		for(ItemStack is : mainInv) {
			s += comma;
			s += ItemStackUtil.itemToString(is);
			comma = ", ";
		}
		if(!effects.isEmpty()) {
			comma = "";
			s += ":";
			for(PotionEffect ef : effects) {
				s += comma;
				s += EffectUtil.effectToString(ef);
				comma = ", ";
			}
		}
		return s;
	}

	public static FightInventory fromString(String fightInventory) {
		FightInventory fi = new FightInventory(LegionPractice.getInstance());
		String[] s = fightInventory.split(":");
		fi.setOwner(s[0]);
		fi.setUUID(UUID.fromString(s[1]));
		fi.setOwnerUUID(UUID.fromString(s[2]));
		fi.setHealth(Double.parseDouble(s[3]));
		fi.setDead(Boolean.parseBoolean(s[4]));
		fi.setFood(Integer.parseInt(s[5]));
		fi.setHelmet(ItemStackUtil.fromString(s[6]));
		fi.setChestplate(ItemStackUtil.fromString(s[7]));
		fi.setLeggings(ItemStackUtil.fromString(s[8]));
		fi.setBoots(ItemStackUtil.fromString(s[9]));
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(String is : s[10].split(", ")) {
			items.add(ItemStackUtil.fromString(is));
		}
		fi.setMainInv(items.toArray(new ItemStack[36]));
		fi.setEffects(new ArrayList<PotionEffect>());
		if(s.length > 12) {
			for(String ef : s[11].split(", ")) {
				fi.getEffects().add(EffectUtil.fromString(ef));
			}
		}
		return fi;
	}


	public static void inventoryMessage(Player p, HashMap<String, FightInventory> winners, HashMap<String, FightInventory> losers) {
		try {
			LegionPractice plugin = LegionPractice.getInstance();
			for(FightInventory fi : winners.values()) {
				plugin.getFightInventoryManager().saveInventory(fi);
			}
			for(FightInventory fi : winners.values()) {
				plugin.getFightInventoryManager().saveInventory(fi);
			}
			p.sendMessage(LegionPractice.getInstance().translateMessage(p, "inventory-message"));
			HashMap<String, String> map = new HashMap<String, String>();
			String losersMessage = losers.size() > 1 ? ChatColor.YELLOW + "Losers' Inventories:" : ChatColor.YELLOW + "Loser's Inventory:" ;
			int c = 0;
			boolean msg = false;
			String separator = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("inventory-separator"));
			String i = ChatColor.translateAlternateColorCodes('&',
					plugin.getConfig().getString("fight-inventory-player"));
			for(String asd : losers.keySet()) {
				c++;
				FightInventory inv = plugin.getFightInventoryManager().getFightInventory(plugin.getFightInventoryManager().getInventoryUUIDByName(asd));
				map.put(i.replace("<player>", inv.isDead() ? ChatColor.RED + asd : ChatColor.GREEN + asd), "/clickablefightinventory "
						+ inv.getUUID());
				if((c == 3 && !msg) || msg && c == 5) {
					c = 0;
					if(!msg) {
						msg = true;
						ClickableMessage.sendMultipleMessages(p, map, losersMessage, separator);
					}
					else {
						ClickableMessage.sendMultipleMessages(p, map, "", separator);
					}
					map.clear();
				}
			}
			if(c > 0) {
				ClickableMessage.sendMultipleMessages(p, map, losersMessage, separator);
			}
			map.clear();
			String winnersMessage = winners.size() > 1 ? ChatColor.YELLOW + "Winners' Inventories:" :  ChatColor.YELLOW + "Winner's Inventory:";
			boolean msg2 = false;
			c = 0;
			for(String asd : winners.keySet()) {
				c++;
				FightInventory inv = plugin.getFightInventoryManager().getFightInventory(plugin.getFightInventoryManager().getInventoryUUIDByName(asd));
				map.put(i.replace("<player>", inv.isDead() ? ChatColor.RED + asd : ChatColor.GREEN + asd), "/clickablefightinventory "
						+ inv.getUUID());
				if((c == 3 && !msg2) || msg2 && c == 5) {
					c = 0;
					if(!msg2) {
						msg2 = true;
						ClickableMessage.sendMultipleMessages(p, map, winnersMessage, separator);
					}
					else {
						ClickableMessage.sendMultipleMessages(p, map, "", separator);
					}
					map.clear();
				}
			}
			msg2 = false;
			if(c > 0) {
				ClickableMessage.sendMultipleMessages(p, map, winnersMessage, separator);
			}
		}catch(Exception e) {}
	}

	public static void inventoryMessage(Player p, Collection<FightInventory> invs) {
		try {
			LegionPractice plugin = LegionPractice.getInstance();
			for(FightInventory fi : invs) {
				plugin.getFightInventoryManager().saveInventory(fi);
			}
			HashMap<String, String> map = new HashMap<String, String>();
			String message = LegionPractice.getInstance().translateMessage(p, "inventory-message");
			int c = 0;
			boolean msg = false;
			HashMap<String, FightInventory> inventories = new HashMap<String, FightInventory>();
			for(FightInventory inv : invs) {
				if(inv.getOwner() != null) {
					inventories.put(inv.getOwner(), inv);
				}
			}
			String separator = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("inventory-separator"));
			String i = ChatColor.translateAlternateColorCodes('&',
					plugin.getConfig().getString("fight-inventory-player"));
			for(String asd : inventories.keySet()) {
				c++;
				FightInventory inv = plugin.getFightInventoryManager().getFightInventory(plugin.getFightInventoryManager().getInventoryUUIDByName(asd));
				map.put(i.replace("<player>", inv.isDead() ? ChatColor.RED + asd : ChatColor.GREEN + asd), "/clickablefightinventory "
						+ inv.getUUID());
				if((c == 3 && !msg) || msg && c == 4) {
					c = 0;
					if(!msg) {
						msg = true;
						ClickableMessage.sendMultipleMessages(p, map, message, separator);
					}
					else {
						ClickableMessage.sendMultipleMessages(p, map, "", separator);
					}
					map.clear();
				}
			}
			if(c > 0) {
				ClickableMessage.sendMultipleMessages(p, map, msg ? "" : message, separator);
			}
		}catch(Exception e) {}
	}

	public static void message(HashSet<String> party, HashSet<String> opponent) {
		try {
			LegionPractice plugin = LegionPractice.getInstance();
			for(String m : party) {
				Player mem = PlayerUtil.getPlayer(m);
				if(mem == null) continue;
				String pr = plugin.translateMessage(mem, "inventory-message");
				mem.sendMessage(pr);
			}
			HashMap<String, String> map = new HashMap<String, String>();
			String yourTeamMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("inventory-message-your"));
			int c = 0;
			String i = ChatColor.translateAlternateColorCodes('&',
					plugin.getConfig().getString("fight-inventory-player"));
			boolean msg = false;
			String separator = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("inventory-separator"));
			for(String asd : party) {
				c++;
				FightInventory inv = plugin.getFightInventoryManager().getFightInventory(plugin.getFightInventoryManager().getInventoryUUIDByName(asd));
				map.put(i.replace("<player>", inv.isDead() ? ChatColor.RED + asd : ChatColor.GREEN + asd), "/clickablefightinventory "
						+ inv.getUUID());
				if((c == 3 && !msg) || msg && c == 5) {
					c = 0;
					for(String s : party) {
						Player mem = PlayerUtil.getPlayer(s);
						if(mem != null) {
							if(!msg) {
								ClickableMessage.sendMultipleMessages(mem, map, yourTeamMessage, separator);
							}
							else {
								ClickableMessage.sendMultipleMessages(mem, map, "", separator);
							}
						}
					}
					msg = true;
					map.clear();
				}
			}
			if(c > 0) {
				for(String s : party) {
					Player mem = PlayerUtil.getPlayer(s);
					if(mem != null) {
						if(!msg) {
							ClickableMessage.sendMultipleMessages(mem, map, yourTeamMessage, separator);
						}
						else {
							ClickableMessage.sendMultipleMessages(mem, map, "", separator);
						}
					}
				}
			}
			map.clear();
			String opponentMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("inventory-message-opponent"));
			boolean msg2 = false;
			c = 0;
			for(String asd : opponent) {
				c++;
				FightInventory inv = plugin.getFightInventoryManager().getFightInventory(plugin.getFightInventoryManager().getInventoryUUIDByName(asd));
				map.put(i.replace("<player>", inv.isDead() ? ChatColor.RED + asd : ChatColor.GREEN + asd), "/clickablefightinventory "
						+ inv.getUUID());
				if((c == 3 && !msg2) || msg2 && c == 5) {
					c = 0;
					for(String s : party) {
						Player mem = PlayerUtil.getPlayer(s);
						if(mem != null) {
							if(!msg2) {
								ClickableMessage.sendMultipleMessages(mem, map, opponentMessage, separator);
							}
							else {
								ClickableMessage.sendMultipleMessages(mem, map, "", separator);
							}
						}
					}
					msg2 = true;
					map.clear();
				}
			}
			if(c > 0) {
				for(String s : party) {
					Player mem = PlayerUtil.getPlayer(s);
					if(mem != null) {
						if(!msg2) {
							ClickableMessage.sendMultipleMessages(mem, map, opponentMessage, separator);
						}
						else {
							ClickableMessage.sendMultipleMessages(mem, map, "", separator);
						}
					}
				}
			}
		}catch(Exception e) {}
	}

	public static List<LinkedHashMap<String, String>> duelMessage(String winner, String loser, FightInventory winnerInv, FightInventory loserInv) {
		List<String> messages = LegionPractice.getInstance().getConfig().getStringList("fight-inventory-message");
		List<LinkedHashMap<String, String>> maps = new ArrayList<LinkedHashMap<String, String>>();
		String noCommand = "/clickedchatwithoutaction";
		for(String s : messages) {
			LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
			String wPots = winnerInv.getPotionsLeft() == 0 ? (int)Math.ceil(winnerInv.getHealth()) + "â�¤" : winnerInv.getPotionsLeft() + "";
			String lPots = loserInv.getPotionsLeft() == 0 ? (int)Math.ceil(loserInv.getHealth()) + "â�¤" : loserInv.getPotionsLeft() + "";
			s = ChatColor.translateAlternateColorCodes('&', s)
					.replace("<winner_pots>", wPots)
					.replace("<loser_pots>", lPots);
			if(!s.contains("<loser>") && s.contains("<winner>")) {
				String command = "/clickablefightinventory "
						+ winnerInv.getUUID();
				map.put(s.replace("<winner>", winner), command);
			}
			else if(!s.contains("<winner>") && s.contains("<loser>")) {
				String command = "/clickablefightinventory "
						+ loserInv.getUUID();
				map.put(s.replace("<loser>", loser), command);
			}
			else if(!s.contains("<winner>") && !s.contains("<loser>")) {
				map.put(s, noCommand);
			}
			else {
				if(s.indexOf("<win") < s.indexOf("<los")) {
					String[] w = s.split("<win");
					if(w[0].contains("<loser>")) {
						String command = "/clickablefightinventory "
								+ loserInv.getUUID();
						map.put(w[0].replace("<loser>", loser), command);
						if(w.length > 0 && w[1].contains("ner>")) {
							w[1] = w[1].replace("ner>", "");
							command = "/clickablefightinventory "
									+ winnerInv.getUUID();
							map.put(winner, command);
						}
					}
					else if(w.length > 0 && w[1].contains("ner>")) {
						w[1] = w[1].replace("ner>", "");
						String command = "/clickablefightinventory "
								+ winnerInv.getUUID();
						map.put(w[0] + winner, command);
					}
					else map.put(w[0], noCommand);
					if(w.length > 1) {
						if(w[1].contains("<loser>")) {
							String command = "/clickablefightinventory "
									+ loserInv.getUUID();
							map.put(w[1].replace("<loser>", loser), command);
						}
						else if(w.length > 2 && w[2].contains("ner>")) {
							w[2] = w[2].replace("ner>", "");
							String command = "/clickablefightinventory "
									+ winnerInv.getUUID();
							map.put(w[1] + winner, command);
						}
						else map.put(w[1], noCommand);
						if(w.length > 2) map.put(w[2], noCommand);
					}
				}
				else {
					String[] w = s.split("<lo");
					if(w[0].contains("<winner>")) {
						String command = "/clickablefightinventory "
								+ winnerInv.getUUID();
						map.put(w[0].replace("<winner>", winner), command);
						if(w.length > 0 && w[1].contains("ser>")) {
							w[1] = w[1].replace("ser>", "");
							command = "/clickablefightinventory "
									+ loserInv.getUUID();
							map.put(loser, command);
						}
					}
					else if(w.length > 0 && w[1].contains("ser>")) {
						w[1] = w[1].replace("ser>", "");
						String command = "/clickablefightinventory "
								+ loserInv.getUUID();
						map.put(w[0] + loser, command);
					}
					else map.put(w[0], noCommand);
					if(w.length > 1) {
						if(w[1].contains("<winner>")) {
							String command = "/clickablefightinventory "
									+ winnerInv.getUUID();
							map.put(w[1].replace("<winner>", winner), command);
						}
						else if(w.length > 2 && w[2].contains("ser>")) {
							w[2] = w[2].replace("ser>", "");
							String command = "/clickablefightinventory "
									+ loserInv.getUUID();
							map.put(w[1] + loser, command);
						}
						else map.put(w[1], noCommand);
						if(w.length > 2) map.put(w[2], noCommand);
					}
				}
			}
			Player winnerPlayer = Bukkit.getPlayer(winner);
			if(winnerPlayer != null){
				ClickableMessage.sendMultipleMessages(winnerPlayer, map);
			}
			Player loserPlayer = Bukkit.getPlayer(loser);
			if(loserPlayer != null && !loser.equalsIgnoreCase(winner)) {
				ClickableMessage.sendMultipleMessages(loserPlayer, map);	
			}
			maps.add(map);
		}
		return maps;
	}
	/*
	private static String spaces(String s) {
		String result = "";
		for(int i = 0; i < s.toCharArray().length; i++) {
			result += " ";
		}
		return result;
	}
	 */
	
	public int getPotionsLeft() {
		return left;
	}
	
	/**
	 * Gets the UUID of this FightInventory.
	 * Not the player's UUID.
	 * @return UUID of this FightInventory.
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Simple method to get the slot of the given item from the config.
	 * @param s this string is used to get a slot number from the config.
	 * @return
	 */
	private int getSlot(String s) {
		int i = plugin.getConfig().getInt("fight-inventory." + s + "-slot");
		int slots = getSlots();
		if(i >= slots) return slots-1;
		return i;
	}

	/**
	 * Gets the health that is stored in this FightInventory.
	 * @return stored health in this FightInventory.
	 */
	public double getHealth(){
		return this.health;
	}

	/**
	 * Sets the health that is used in this FightInventory.
	 * @param health stored health in this FightInventory.
	 */
	public void setHealth(double health){
		this.health = health;
	}



	/**
	 * Gets the potion effects of this FightInventory.
	 * @return potion effects of this FightInventory.
	 */
	public Collection<PotionEffect> getEffects(){
		return this.effects;
	}

	/**
	 * Gets the owner of this FightInventory.
	 * @return the owner's name
	 */
	public String getOwner(){
		return this.owner;
	}

	/**
	 * Gets the player's inventory contents when this FightInventory was saved.
	 * @return an array of items in player's inventory when this FightInventory was saved
	 */
	public ItemStack[] getMainInv(){
		return this.mainInv;
	}

	/**
	 * Gets if the player was dead when this FightInventory was saved.
	 * @return true if the player was dead, otherwise false
	 */
	public boolean isDead(){
		return this.dead;
	}

	/**
	 * Gets the player's food level when this FightInventory was saved.
	 * @return the food level of the player when this FightInventory was saved.
	 */
	public int getFood(){
		return this.food;
	}

	/**
	 * Gets the helmet of the player when this FightInventory was saved.
	 * @return the itemstack in the player's helmet slot when this FightInventory was saved.
	 */
	public ItemStack getHelmet(){
		return this.helmet;
	}

	/**
	 * Gets the chestplate of the player when this FightInventory was saved.
	 * @return the itemstack in the player's chestplate slot when this FightInventory was saved.
	 */
	public ItemStack getChestplate(){
		return this.chestplate;
	}

	public boolean createdLongTimeAgo() {
		return System.currentTimeMillis()-created > 1000*60*30;
	}

	/**
	 * Gets the leggings of the player when this FightInventory was saved.
	 * @return the itemstack in the player's leggings slot when this FightInventory was saved.
	 */
	public ItemStack getLeggings() {
		return this.leggings;
	}

	/**
	 * Gets the boots of the player when this FightInventory was saved.
	 * @return the itemstack in the player's boots slot when this FightInventory was saved.
	 */
	public ItemStack getBoots(){
		return this.boots;
	}

	/**
	 * Sets the inventory contents that will be shown as the player's inventory contents in the FightInventory.
	 * @param mainInv the mainInv to set
	 */
	public void setMainInv(ItemStack[] mainInv) {
		this.mainInv = mainInv;
	}

	/**
	 * Sets if the player should be shown as dead in the FightInventory.
	 * If true the health won't be shown.
	 * @param dead the dead to set
	 */
	public void setDead(boolean dead) {
		this.dead = dead;
	}

	/**
	 * Sets the potion effects of this FightInventory.
	 * @param effects the effects to set
	 */
	public void setEffects(Collection<PotionEffect> effects) {
		this.effects = effects;
	}

	/**
	 * Sets the food level of this FightInventory.
	 * @param food the food to set
	 */
	public void setFood(int food) {
		this.food = food;
	}

	/**
	 * Sets the helmet of this FightInventory.
	 * @param helmet the helmet to set
	 */
	public void setHelmet(ItemStack helmet) {
		this.helmet = helmet;
	}

	/**
	 * Sets the chesplate of this FightInventory.
	 * @param chestplate the chestplate to set
	 */
	public void setChestplate(ItemStack chestplate) {
		this.chestplate = chestplate;
	}

	/**
	 * Sets the leggings of this FightInventory.
	 * @param leggings the leggings to set
	 */
	public void setLeggings(ItemStack leggings) {
		this.leggings = leggings;
	}

	/**
	 * Sets the boots of this FightInventory.
	 * @param boots the boots to set
	 */
	public void setBoots(ItemStack boots) {
		this.boots = boots;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the ownerUUID
	 */
	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	/**
	 * @param ownerUUID the ownerUUID to set
	 */
	public void setOwnerUUID(UUID ownerUUID) {
		this.ownerUUID = ownerUUID;
	}

}


class FightInventoryHolder implements InventoryHolder{

	@Override
	public Inventory getInventory() {
		return null;
	}
	
}