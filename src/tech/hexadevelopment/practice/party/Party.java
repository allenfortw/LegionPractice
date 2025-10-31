package tech.hexadevelopment.practice.party;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.PartyCreatedEvent;
import tech.hexadevelopment.practice.events.PartyDisbandEvent;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.scoreboard.ScoreboardManager;
import tech.hexadevelopment.practice.utils.PlayerUtil;

public class Party {


	static List<String> emptyDiscords = new ArrayList<String>();

	private String owner;
	private Party opponent;
	private LegionPractice plugin;
	private HashSet<String> invited = new HashSet<String>();
	private HashSet<String> members = new HashSet<String>();
	private Fight fight;
	private PartySettings settings = new PartySettings();
	private BukkitTask broadcastTask;
	boolean customDiscord;
	String discord;
	
	
	public static void loadPartyDiscords() {
		emptyDiscords = LegionPractice.getInstance().getConfig().getStringList("party.discord-invites");
	}

	public Party(Player p, LegionPractice plugin) {
		this.plugin = plugin;
		this.owner = p.getName();
		getMembers().add(p.getName());
		PartyCreatedEvent event = new PartyCreatedEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		saveParty(p);
		plugin.arenaPvP.giveSpawnItems(p);
		if(ScoreboardManager.isEnabled()) {
			ScoreboardManager.getScoreboardManager().updateScoreboard(p);
		}
	}

	public Party getOpponent() {
		return opponent;
	}

	public void setOpponent(Party opponent) {
		this.opponent = opponent;
	}

	public HashSet<String> getInvited() {
		return invited;
	}

	public void setInvited(HashSet<String> invited) {
		this.invited = invited;
	}

	public boolean isInFight() {
		return fight != null;
	}
	
	public String getDiscord() {
		return discord;
	}

	public void setFight(Fight fight) {
		this.fight = fight;
	}

	public Fight getFight() {
		return fight;
	}

	public String getOwner() {
		return owner;
	}

	public HashSet<String> getMembers() {
		return members;
	}

	public void setMembers(HashSet<String> members) {
		this.members = members;
	}

	public BattleKit getKit() {
		if(fight != null) return fight.getKit();
		return null;
	}

	public Arena getArena() {
		if(fight != null) return fight.getArena();
		return null;
	}

	public PartySettings getSettings() {
		return settings;
	}

	public BukkitTask getBroadcastTask() {
		return broadcastTask;
	}

	public void setBroadcastTask(BukkitTask task){
		this.broadcastTask = task;
	}

	public void setOwner(String newOwner) {
		Player now = Bukkit.getPlayer(newOwner);
		if(!getMembers().contains(now.getName())) {
			getMembers().add(now.getName());
		}
		if(broadcastTask != null) {
			broadcastTask.cancel();
			broadcastTask = null;
		}
		owner = newOwner;
	}

	public void disbandParty() {
		PartyDisbandEvent event = new PartyDisbandEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		for(String name : members) {
			Player member = PlayerUtil.getPlayer(name);
			if(member != null) {
				if(isInFight()) {
					Fight fight = Fight.getCurrentFight(member, plugin);
					if(fight != null && !LegionPractice.disabling) {
						if(fight instanceof PartyVsParty) {
							((PartyVsParty)fight).forceEnd(this);
						}
						else {
							fight.forceEnd(ChatColor.RED + "The fight was forced to end because the party has been disbanded!");
						}
					}
				}
				member.removeMetadata(plugin.META_IN_PARTY, plugin);
				if(Fight.getCurrentFight(member, plugin) == null) {
					plugin.arenaPvP.giveSpawnItems(member);
				}
				if(ScoreboardManager.isEnabled()) {
					ScoreboardManager.getScoreboardManager().updateScoreboard(member);
				}
			}
		}
		if(!LegionPractice.disabling && discord != null && !customDiscord) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					emptyDiscords.add(discord);			
				}
			}.runTaskLater(plugin, 20*60*10);
		}
		members.clear();
	}

	public void saveParty(Metadatable p) {
		p.setMetadata(plugin.META_IN_PARTY, new FixedMetadataValue(plugin, this));
	}

	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for(String s : members) {
			Player p = Bukkit.getPlayer(s);
			if(p != null) {
				players.add(p);
			}
		}
		return players;
	}

	public void partyMessage(String msg) {
		if(getMembers() != null && !getMembers().isEmpty()) {
			for(String name : getMembers()) {
				Player pl = Bukkit.getPlayer(name);
				if(pl != null) {
					pl.sendMessage(msg);
				}
			}
		}
	}

	public static Party getParty(Player member) {
		if(member == null) return null;
		LegionPractice plugin = LegionPractice.getInstance();
		if(member.hasMetadata(plugin.META_IN_PARTY)) {
			MetadataValue m = plugin.getMetadata(member, plugin.META_IN_PARTY);
			if(m != null && m.value() != null && m.value() instanceof Party) {
				return (Party) m.value();
			}
		}
		return null;
	}

}
