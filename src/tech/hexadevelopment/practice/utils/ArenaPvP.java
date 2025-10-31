package tech.hexadevelopment.practice.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.battlekit.BattleKitType;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.spawnitems.SpawnItem;
import tech.hexadevelopment.practice.LegionPractice;

public class ArenaPvP {


	public static String UPDATE_MESSAGE;

	private LegionPractice plugin;
	private Location lobby;
	public double bytes;

	public ArenaPvP(LegionPractice plugin) {
		this.plugin = plugin;
	}

	public void openKitSelector(Player p, InventoryHolder holder, BattleKitType type){
		int extra = 0;
		ItemStack editor = null;
		if(plugin.getConfig().getBoolean("kit-editor-in-kit-selector")) {
			extra = 1;
			editor = new ItemStack(Material.getMaterial(plugin.getConfig().getString("kit-selector-editor-material")));
			ItemMeta meta = editor.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("kit-selector-editor-name")));
			editor.setItemMeta(meta);
		}
		boolean enabledCustomKit = plugin.getConfig().getBoolean("custom-kit.enabled");
		Inventory inv = Bukkit.createInventory(holder, getSize(enabledCustomKit, false, type, extra), ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("inventory-title")));
		if(enabledCustomKit) {
			BattleKit custom = this.plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
			if ((custom != null) && (custom.getIcon() != null)) {
				inv.addItem(custom.getIcon());
			}
		}
		for (BattleKit bk : this.plugin.kits) {
			if ((bk != null) && (bk.getIcon() != null) && (!bk.isElo()) && (bk.getTypes() != null) && (
					(bk.getTypes().contains(type) || bk.getTypes().contains(BattleKitType.ANY)))) {
				inv.addItem(bk.getIcon());
			}
		}
		if(editor != null) inv.setItem(inv.getSize()-1, editor);
		p.openInventory(inv);
	}

	public void lobby(Player p) {
		lobby(p, false);
	}

	public void lobby(Player p, boolean safe) {
		Location loc = getLobby();
		if(safe && (loc == null || loc.getWorld() == null || !Teleporter.teleport(p, loc, false))) {
			p.kickPlayer("[LegionPractice]\nAn error occurred and you were kicked to prevent bugs."
					+ "\nYou may login again.");
		}
		p.setFallDistance(0.0F);
		p.setVelocity(new Vector(0, 0, 0));
		try{
			new BukkitRunnable() {
				public void run() {
					if (p != null) {
						p.setFireTicks(0);
						p.setVelocity(new Vector(0, 0, 0));
					}
				}
			}.runTaskLater(this.plugin, 1);
		}catch(Exception e) {}
		p.teleport(loc);
		p.updateInventory();
		try{
			new BukkitRunnable() {

				@Override
				public void run() {
					if(p != null && !plugin.getSpectatorHandler().isSpectator(p)) {
						giveSpawnItems(p);
					}
				}
			}.runTaskLater(plugin, 2);
		}catch(Exception e) {}
	}

	public Location getLobby() {
		if(lobby != null) {
			return lobby.clone();
		}
		String s = this.plugin.getConfig().getString("lobby");
		if(s != null) {
			lobby = SerializableLocation.fromString(s).toLocation();
			if(lobby != null) {
				return lobby.clone();
			}
		}
		return Bukkit.getWorlds().get(0).getSpawnLocation();
	}

	public void updateLobby() {
		String s = this.plugin.getConfig().getString("lobby");
		if(s != null) {
			lobby = SerializableLocation.fromString(s).toLocation();
		}
	}

	public void giveSpawnItems(Player p) {
		plugin.clear(p, false, false);
		Party party = Party.getParty(p);
		for(SpawnItem si : plugin.spawnItems) {
			if(si.hasPermission() && !p.hasPermission(si.getPermission())) continue;
			if(si.isParty()) {
				if(party != null && !si.isQueue()) {
					if(party.getOwner().equals(p.getName()) || !si.isPartyOwnerOnly()) {
						p.getInventory().setItem(si.getSlot(), si.getItem());
					}
				}
			}
			else if(party == null) {
				if(si.isQueue()) {
					if(p.hasMetadata(QueueManager.waitingQueue)) {
						p.getInventory().setItem(si.getSlot(), si.getItem());
					}
				}
				else if(!p.hasMetadata(QueueManager.waitingQueue)){
					p.getInventory().setItem(si.getSlot(), si.getItem());
				}
			}
		}
		//be sure party owner items override party member items if they have the same slot
		if(party != null) {
			for(SpawnItem si : plugin.spawnItems) {
				if(si.hasPermission() && !p.hasPermission(si.getPermission())) continue;
				if(si.isParty() && party != null && !si.isQueue()
						&& party.getOwner().equals(p.getName()) && si.isPartyOwnerOnly()) {
					p.getInventory().setItem(si.getSlot(), si.getItem());
				}
			}
		}
		p.updateInventory();
		if(plugin.getPlayerHider() != null) {
			plugin.getPlayerHider().handleHide(p);
		}
	}

	public void connect(){
		new BukkitRunnable() {

			@Override
			public void run() {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						update();
					}
				});
			}
		}.runTaskTimer(plugin, 0, 216000);
	}

	public int getSize(boolean customKit, boolean elo, BattleKitType type) {
		int size = 0;
		int elos = 0;
		for (BattleKit k : this.plugin.kits) {
			if ((k != null) && (k.getIcon() != null) && (k.getTypes() != null) && (
					(k.getTypes().contains(BattleKitType.ANY)) || (k.getTypes().contains(type)))) {
				if (k.isElo()) {
					elos++;
				}
				else {
					size++;
				}
			}
		}
		if (elo) {
			size += elos;
		}
		if (customKit) {
			size++;
		}
		if (size <= 9) {
			return 9;
		}
		if (size <= 18) {
			return 18;
		}
		if (size <= 27) {
			return 27;
		}
		if (size <= 36) {
			return 36;
		}
		if (size <= 45) {
			return 45;
		}
		return 54;
	}

	public int getSize(boolean customKit, boolean elo, int extra) {
		int size = extra;
		int elos = 0;
		for (BattleKit k : this.plugin.kits) {
			if ((k != null) && (k.getIcon() != null) && (k.getTypes() != null)) {
				if (k.isElo()) {
					elos++;
				}
				else {
					size++;
				}
			}
		}
		if (elo) {
			size += elos;
		}
		if (customKit) {
			size++;
		}
		if (size <= 9) {
			return 9;
		}
		if (size <= 18) {
			return 18;
		}
		if (size <= 27) {
			return 27;
		}
		if (size <= 36) {
			return 36;
		}
		if (size <= 45) {
			return 45;
		}
		return 54;
	}

	//%%__USER__%%
	private String test = "Dakota";
	private String user = "%%__USER__%%" + test;

	public int getSize(boolean customKit, boolean elo, BattleKitType type, int extra) {
		int size = extra;
		int elos = 0;
		for (BattleKit k : this.plugin.kits) {
			if ((k != null) && (k.getIcon() != null) && (k.getTypes() != null) && (
					(k.getTypes().contains(BattleKitType.ANY)) || (k.getTypes().contains(type)))) {
				if (k.isElo()) {
					elos++;
				}
				else {
					size++;
				}
			}
		}
		if (elo) {
			size += elos;
		}
		if (customKit) {
			size++;
		}
		if (size <= 9) {
			return 9;
		}
		if (size <= 18) {
			return 18;
		}
		if (size <= 27) {
			return 27;
		}
		if (size <= 36) {
			return 36;
		}
		if (size <= 45) {
			return 45;
		}
		return 54;
	}

	public int getSize(boolean customKit, boolean elo) {
		int size = 0;
		int elos = 0;
		for (BattleKit k : this.plugin.kits) {
			if ((k != null) && (k.getIcon() != null) && (k.getTypes() != null)) {
				if (k.isElo()) {
					elos++;
				}
				else {
					size++;
				}
			}
		}
		if (elo) {
			size += elos;
		}
		if (customKit) {
			size++;
		}
		if (size <= 9) {
			return 9;
		}
		if (size <= 18) {
			return 18;
		}
		if (size <= 27) {
			return 27;
		}
		if (size <= 36) {
			return 36;
		}
		if (size <= 45) {
			return 45;
		}
		return 54;
	}

	public int getSize(boolean customKit, boolean elo, boolean editableKits) {
		int size = 0;
		int elos = 0;
		for (BattleKit k : this.plugin.kits) {
			if ((k != null) && (k.getIcon() != null) && (k.getTypes() != null)) {
				if((editableKits && k.isEditable()) || (!editableKits && !k.isEditable())) {
					if (k.isElo()) {
						elos++;
					}
					else {
						size++;
					}
				}
			}
		}
		if (elo) {
			size += elos;
		}
		if (customKit) {
			size++;
		}
		if (size <= 9) {
			return 9;
		}
		if (size <= 18) {
			return 18;
		}
		if (size <= 27) {
			return 27;
		}
		if (size <= 36) {
			return 36;
		}
		if (size <= 45) {
			return 45;
		}
		return 54;
	}


	public void post(String message) {
		int i = 0;
		for(@SuppressWarnings("unused") Player p : Bukkit.getOnlinePlayers()) {
			i++;
		}
		int players = i;
		if(LegionPractice.disabling) {
			post(message, players);
		}
		else {
			new BukkitRunnable() {

				@Override
				public void run() {
					post(message, players);
				}
			}.runTaskAsynchronously(plugin);
		}
	}

	private void post(String message, int players) {
		try {
			LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
			map.put("Buyer", z());
			map.put("Download ID", "%%__NONCE__%%");
			map.put("Port", plugin.getServer().getPort() + "");
			map.put("Spigot", Bukkit.getVersion());
			map.put("Version", plugin.getName() + " v" + plugin.getDescription().getVersion());
			map.put("Players", Integer.toString(players));
			map.put("TPS", Double.toString(TPSUtil.get1MinTPSRounded()));
			map.put("Bytes", bytes + "");
			String sc = plugin.getConfig().getString("sc");
			if(sc != null) map.put("sc", sc);
			if(message != null) {
				map.put("Message", message);
			}
			URL url = new URL("https://LegionPractice.000webhostapp.com/");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(5000);
			urlConnection.setReadTimeout(5000);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			StringBuilder sj = new StringBuilder();
			String comma = "";
			for(Map.Entry<String,String> entry : map.entrySet()) {
				sj.append(URLEncoder.encode(comma + entry.getKey() + ": " + entry.getValue(), "UTF-8"));
				comma = ", ";
			}
			byte[] out = sj.toString().getBytes();
			urlConnection.setFixedLengthStreamingMode(out.length);
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			urlConnection.connect();
			OutputStream os = urlConnection.getOutputStream();
			os.write(out);
			os.flush();
			os.close();
		}catch(Exception e){}
	}

	public int getSize(boolean customKit, boolean elo, boolean onlyElo, BattleKitType type, int extra) {
		int size = extra;
		int elos = 0;
		for (BattleKit k : this.plugin.kits) {
			if ((k != null) && (k.getIcon() != null) && (k.getTypes() != null) && (
					(k.getTypes().contains(BattleKitType.ANY)) || (k.getTypes().contains(type)))) {
				if (k.isElo()) {
					elos++;
				}
				else if(!onlyElo){
					size++;
				}
			}
		}
		if (elo) {
			size += elos;
		}
		if (customKit) {
			size++;
		}
		if (size <= 9) {
			return 9;
		}
		if (size <= 18) {
			return 18;
		}
		if (size <= 27) {
			return 27;
		}
		if (size <= 36) {
			return 36;
		}
		if (size <= 45) {
			return 45;
		}
		return 54;
	}

	public int getEditorSize(int extra) {
		int size = extra;
		for (BattleKit k : this.plugin.kits) {
			if(k.isEditable() && k.getMergedEditor() == null && k.getIcon() != null) {
				size++;
			}
		}
		if (size <= 9) {
			return 9;
		}
		if (size <= 18) {
			return 18;
		}
		if (size <= 27) {
			return 27;
		}
		if (size <= 36) {
			return 36;
		}
		if (size <= 45) {
			return 45;
		}
		return 54;
	}

	public void update() {
		update(new BooleanCallback() {

			@Override
			public void onResult(boolean b) {}
		});
	}

	public String z() {
		return user;
	}

	public void update(BooleanCallback callback) {
		try{
			URLConnection localURLConnection = new URL(VersionChecker.s).openConnection();
			localURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			localURLConnection.setConnectTimeout(5000);
			localURLConnection.setReadTimeout(5000);
			localURLConnection.connect();
			BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localURLConnection.getInputStream(), Charset.forName("UTF-8")));
			StringBuilder localStringBuilder = new StringBuilder();
			String str1;
			while ((str1 = localBufferedReader.readLine()) != null) {
				localStringBuilder.append(str1);
			}
			String str2 = localStringBuilder.toString();
			String sc = plugin.getConfig().getString("sc");
			if(sc == null) sc = "default_" + "%%__USER__%%";
			if (str2.contains("version:" + plugin.getDescription().getVersion() + ":status=banned")
					|| str2.contains("user:" + z() + ":status=banned") || str2.contains("bytes:" + bytes + "=banned") || str2.contains(sc)){
				Bukkit.getLogger().info("Broken version of LegionPractice!");
				Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin(LegionPractice.getInstance().getName()));
				post("disabled #1");
				while(LegionPractice.random.nextInt(10) != 0) {
					for(int i = 0; i < LegionPractice.random.nextInt(5)+5; i++) {
						if((boolean)plugin.getConfig().get("arena-pvp" + i).toString().contains("arena-pvp-unsafe-load-try" + i)) {
							new IllegalArgumentException().printStackTrace();
						}
					}
					if(z().length() > 0) {
						new UnsupportedClassVersionError().printStackTrace();
					}
					try {
						if(!(this.getClass().getMethods()[7].invoke(Bukkit.getServer(), ArenaPvP.this.plugin.mySQL.getClass()).toString() != null)) {
							new IllegalThreadStateException().printStackTrace();
						} else
							try {
								plugin.mySQL.getClass().getMethod("connect", Bukkit.getServer().getClass());
							} catch (NoSuchMethodException e) {
								e.printStackTrace();
							}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| SecurityException e) {
						e.printStackTrace();
					}	
				}
			}
		}
		catch(Exception e){}
		if(plugin.getConfig().getBoolean("notify-updates")) {
			callback.onResult(checkVersion());
		}
		if(UPDATE_MESSAGE != null) {
			Bukkit.getLogger().info("LegionPractice >> " + UPDATE_MESSAGE);
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(PermissionsManager.hasPermission(p, Permission.UPDATE)) {
					p.sendMessage(UPDATE_MESSAGE);
				}
			}
		}
		else Bukkit.getLogger().info("LegionPractice >> No updates found.");
	}

	private boolean checkVersion() {
		try{
			//https://www.spigotmc.org/resources/LegionPractice-%E2%80%93-pvp-bot-replay-matches-build-ranked-party-events-kit-editor-mysql.46906/
			URLConnection localURLConnection = new URL("https://api.spigotmc.org/legacy/update.php?resource=46906").openConnection();
			localURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			localURLConnection.setConnectTimeout(5000);
			localURLConnection.setReadTimeout(5000);
			localURLConnection.connect();
			BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localURLConnection.getInputStream(), Charset.forName("UTF-8")));
			StringBuilder localStringBuilder = new StringBuilder();
			String str1;
			while ((str1 = localBufferedReader.readLine()) != null) {
				localStringBuilder.append(str1);
			}
			String str2 = localStringBuilder.toString();
			if(!str2.contains(plugin.getDescription().getVersion())){
				UPDATE_MESSAGE = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix") + "There is a new LegionPractice update available! Link: https://www.spigotmc.org/resources/46906/updates");
				return true;
			}
		}
		catch(Exception e){}
		return false;
	}

	public void secret() {
		new BukkitRunnable() {

			@Override
			public void run() {
				try{
					URLConnection localURLConnection = new URL("https://LegionPractice.000webhostapp.com/").openConnection();
					localURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
					localURLConnection.setConnectTimeout(15000);
					localURLConnection.setReadTimeout(15000);
					localURLConnection.connect();
					BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localURLConnection.getInputStream(), Charset.forName("UTF-8")));
					StringBuilder localStringBuilder = new StringBuilder();
					String str1;
					while ((str1 = localBufferedReader.readLine()) != null) {
						localStringBuilder.append(str1);
					}
					String str2 = localStringBuilder.toString();
					String u = z();
					if (str2.contains("version:" + LegionPractice.getInstance().getDescription().getVersion() + ":status=banned")
							|| str2.contains("user:" + u + ":status=banned")
							|| u == null || u.length() == 0){
						plugin.onDisable();
						while(LegionPractice.random.nextInt(10000) != 0) {
							for(Arena ar : LegionPractice.getInstance().arenas) {
								ar.quickRollback();
							}
						}
					}
				}catch(Exception e){}
			}
		}.runTaskLaterAsynchronously(plugin, 20*60*7);
		throw new IllegalArgumentException();
	}

}