package tech.hexadevelopment.practice.hostedevents.koth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.PvPEventEndEvent;
import tech.hexadevelopment.practice.events.PvPEventStartEvent;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.utils.Countdown;
import tech.hexadevelopment.practice.utils.LocationUtil;

public class KOTH implements PvPEvent{


	private KOTHTeam team1 = new KOTHTeam();
	private KOTHTeam team2 = new KOTHTeam();
	public KOTHTeam capperTeam;
	public BattleKit kit;
	private boolean started;
	private Location spawn1;
	private Location spawn2;
	private Location corner1;
	private Location corner2;
	public int timer;
	public int counter;
	public UUID capper;
	private BukkitTask task;
	public long startedTime;
	private Fight kothFight;
	private Arena arena;
	private LegionPractice plugin;

	public KOTH(BattleKit kit, Location spawn1, Location spawn2, Location corner1, Location corner2, int timer, LegionPractice plugin) {
		this.plugin = plugin;
		this.spawn1 = spawn1;
		this.spawn2 = spawn2;
		this.kit = kit;
		this.timer = timer;
		if(timer < 1) timer = 1;
		this.corner1 = corner1;
		this.corner2 = corner2;
		this.counter = timer;
		for(Arena arena : LegionPractice.getInstance().arenas) {
			if(arena.getName().equalsIgnoreCase("koth")) {
				this.arena = arena;
			}
		}
	}

	@Override
	public void start() {
		PvPEventStartEvent event = new PvPEventStartEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		started = true;
		if(arena != null ) {
			kothFight = new Fight() {

				@Override
				public void start() {}

				@Override
				public SavedFight saveFight(List<UUID> winners, List<UUID> losers, List<FightInventory> winnersInventories,
						List<FightInventory> losersInventories, UUID playbackUUID) {
					return null;
				}

				@Override
				public boolean hasEnded() {
					return !KOTH.this.hasStarted();
				}

				@Override
				public void handleDeath(Player p) {}

				@Override
				public void forceEnd(String reason) {}

				@Override
				public boolean canStart() {
					return false;
				}

				@Override
				public boolean allowSpectating() {
					return false;
				}

				@Override
				public BattleKit getKit() {
					return kit;
				}
			};
			arena.setUsing(true, kothFight);
		}
		startedTime = System.currentTimeMillis();
		for(UUID uuid : KOTHCommand.joined) {
			Player p = Bukkit.getPlayer(uuid);
			if(p != null && Party.getParty(p) == null && Fight.getCurrentFight(p, LegionPractice.getInstance()) == null) {
				join(p);
			}
		}
		KOTHCommand.joined.clear();
		try{
		task = new BukkitRunnable() {

			@Override
			public void run() {
				if(capper == null) {
					getNewCapper();
				}
				Player capperPlayer = Bukkit.getPlayer(capper);
				if(capper != null && capperPlayer != null) {
					if(isInRegion(capperPlayer)) {
						counter--;
						if(counter == 0) {
							for(Player pl : Bukkit.getOnlinePlayers()) {
								pl.sendMessage(LegionPractice.getInstance().translateMessage(pl, "koth-winner").replace("<team>", getTeamName(capperTeam)));
							}
							PvPEventEndEvent event = new PvPEventEndEvent(KOTH.this, capperPlayer);
							Bukkit.getPluginManager().callEvent(event);
							stop();
						}
					}
					else {
						counter = timer;
						capper = null;
						capperTeam = null;
						getNewCapper();
					}
				}
			}
		}.runTaskTimer(LegionPractice.getInstance(), 20, 20);
		}catch(Exception e) {
			e.printStackTrace();
			Bukkit.broadcastMessage(ChatColor.RED + "An error occurred in the KOTH event...");
			stop();
		}
	}

	private boolean isInRegion(Player mem) {
		return mem != null && !mem.isDead() && LocationUtil.isInregion(mem.getLocation(), corner1, corner2);
	}

	private void getNewCapper() {
		List<Player> inRegion = new ArrayList<Player>();
		for(UUID uuid : team1.getMembers()) {
			Player mem = Bukkit.getPlayer(uuid);
			if(mem != null) {
				if(isInRegion(mem)){
					inRegion.add(mem);
				}
			}
		}
		for(UUID uuid : team2.getMembers()) {
			Player mem = Bukkit.getPlayer(uuid);
			if(mem != null) {
				if(isInRegion(mem)){
					inRegion.add(mem);
				}
			}
		}
		if(inRegion.size() > 0) {
			int r = 0;
			if(inRegion.size() > 1) {
				r = LegionPractice.random.nextInt(inRegion.size());
			}
			for(Player pl : inRegion) {
				if(r == 0) {
					capper = pl.getUniqueId();
					if(team1.getMembers().contains(pl.getUniqueId())) {
						capperTeam = team1;
					}
					else if(team2.getMembers().contains(pl.getUniqueId())) {
						capperTeam = team2;
					}
					return;
				}
				r--;
			}
		}
	}

	@Override
	public void stop() {
		started = false;
		if(arena != null) {
			arena.setUsing(false, null);
			arena.rollbackArena(kit.getBlocks());
		}
		KOTHCommand.open = false;
		for(UUID uuid : team1.getMembers()) {
			Player mem = Bukkit.getPlayer(uuid);
			if(mem != null) {
				plugin.clear(mem, false, true);
				try{
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							plugin.clear(mem, true, true);
							if(plugin.getSpectatorHandler().isSpectator(mem)) {
								plugin.getSpectatorHandler().removeSpectator(mem, true);
							}
						}
					}, 20*plugin.getConfig().getInt("wait-before-teleport"));
				}catch(Exception e) {
					plugin.clear(mem, true, true);
					if(plugin.getSpectatorHandler().isSpectator(mem)) {
						plugin.getSpectatorHandler().removeSpectator(mem, true);
					}
				}
				if(plugin.getTagManager().COLORED_TAGS) {
					plugin.getTagManager().removeFromTeams(mem);
				}
			}
		}

		for(UUID uuid : team2.getMembers()) {
			Player mem = Bukkit.getPlayer(uuid);
			if(mem != null) {
				plugin.clear(mem, false, true);
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						plugin.clear(mem, true, true);
						if(plugin.getSpectatorHandler().isSpectator(mem)) {
							plugin.getSpectatorHandler().removeSpectator(mem, true);
						}
					}
				}, 20*plugin.getConfig().getInt("wait-before-teleport"));
				if(plugin.getTagManager().COLORED_TAGS) {
					plugin.getTagManager().removeFromTeams(mem);
				}
			}
		}
		for(Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendMessage(plugin.translateMessage(pl, "event-stopped"));
		}
		if(task != null) task.cancel();
		KOTHCommand.koth = null;
	}

	@Override
	public boolean hasStarted() {
		return started;
	}


	public void join(Player p) {
		plugin.clear(p, false, true);
		boolean tags = plugin.getConfig().getBoolean("enable-colored-names");
		if(team1.getMembers().size() > team2.getMembers().size()) {
			team2.getMembers().add(p.getUniqueId());
			p.teleport(spawn2);
			if(tags) {
				plugin.getTagManager().setTag(p, "team2");
			}
		}
		else {
			team1.getMembers().add(p.getUniqueId());
			p.teleport(spawn1);
			if(tags) {
				plugin.getTagManager().setTag(p, "team1");
			}
		}
		Countdown.startCountdown(Arrays.asList(p.getName()));
		kit.giveKit(p);
		//Fight.setCurrentFight(p, kothFight, plugin);
	}

	public KOTHTeam getTeam1() {
		return team1;
	}

	public KOTHTeam getTeam2() {
		return team2;
	}

	public class KOTHTeam {

		private HashSet<UUID> members = new HashSet<UUID>();


		public HashSet<UUID> getMembers() {
			Iterator<UUID> itr = members.iterator();
			if(itr.hasNext()) {
				UUID uuid = itr.next();
				if(Bukkit.getPlayer(uuid) == null) {
					itr.remove();
				}
			}
			return members;
		}
	}

	public String getTeamName(KOTHTeam team) {
		return ChatColor.translateAlternateColorCodes('&', team == team1 ?
				LegionPractice.getInstance().getConfig().getString("koth.team1") : LegionPractice.getInstance().getConfig().getString("koth.team2"));
	}
}
