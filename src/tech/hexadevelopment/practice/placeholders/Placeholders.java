package tech.hexadevelopment.practice.placeholders;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.epearlcooldown.PearlListener;
import tech.hexadevelopment.practice.epearlcooldown.PearlManager;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.duel.BestOf;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.fights.other.FFAFight;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyFFA;
import tech.hexadevelopment.practice.fights.party.partyfights.PartySplit;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsBots;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.fights.queue.QueueRunnable;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.hostedevents.automaticevents.AutomaticEventTask;
import tech.hexadevelopment.practice.hostedevents.automaticevents.AutomaticEventType;
import tech.hexadevelopment.practice.hostedevents.brackets.Brackets;
import tech.hexadevelopment.practice.hostedevents.juggernaut.Juggernaut;
import tech.hexadevelopment.practice.hostedevents.koth.KOTH;
import tech.hexadevelopment.practice.hostedevents.lms.LMS;
import tech.hexadevelopment.practice.hostedevents.sumo.Sumo;
import tech.hexadevelopment.practice.misc.ClicksPerSecond;
import tech.hexadevelopment.practice.misc.ReportHook;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.utils.TPSUtil;
import tech.hexadevelopment.practice.utils.TimeUtil;

public class Placeholders {



	private HashMap<String, String> commonPlaceholders = new HashMap<String, String>();
	private long commonsUpdated;


	/**
	 * Supports PlaceholderAPI
	 * @param p
	 * @param text
	 * @param prefix
	 * @param common
	 * @return
	 */
	public String doPlaceholders(Player p, String text, String prefix, boolean common) {
		if(common) {
			text = doCommonPlaceholders(p, text, prefix);
		}
		LegionPractice plugin = LegionPractice.getInstance();
		Fight fight = Fight.getCurrentFight(p, plugin);
		if(text.contains("<" + prefix + "ping>")) text = StringUtils.replace(text, "<" + prefix + "ping>", getPingString(p, plugin), 1);
		UUID uuid = p.getUniqueId();
		PlayerStats stats = PlayerStats.getStats(uuid);
		text = StringUtils.replace(text, "<" + prefix + "kills>", stats.getKills() + "");
		text = StringUtils.replace(text, "<" + prefix + "deaths>", stats.getDeaths() + "");
		text = StringUtils.replace(text, "<" + prefix + "brackets_wins>", stats.getBracketsWins() + "");
		text = StringUtils.replace(text, "<" + prefix + "lms_wins>", stats.getLMSWins() + "");
		text = StringUtils.replace(text, "<" + prefix + "event_wins>", (stats.getLMSWins()+stats.getBracketsWins()) + "");
		text = StringUtils.replace(text, "<" + prefix + "party_vs_party_wins>", stats.getPartyVsPartyWins() + "");
		text = StringUtils.replace(text, "<" + prefix + "player>", p.getName());
		text = StringUtils.replace(text, "<" + prefix + "custom_name>", p.getCustomName());
		text = StringUtils.replace(text, "<" + prefix + "display_name>", p.getDisplayName());
		text = StringUtils.replace(text, "<" + prefix + "world>", p.getWorld().getName());
		text = StringUtils.replace(text, "<" + prefix + "premiummatches>", stats.getPremiumMatches() + "");
		text = StringUtils.replace(text, "<" + prefix + "cps>", ClicksPerSecond.getCPS(p) + "");
		String unlimited = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("unlimited"));
		String rankedsLeft = stats.getRankedsLeft() > 1000 ? unlimited : Integer.toString(stats.getRankedsLeft());
		String unrankedsLeft = stats.getUnrankedsLeft() > 1000 ? unlimited : Integer.toString(stats.getUnrankedsLeft());
		text = StringUtils.replace(text, "<" + prefix + "rankeds_left>", rankedsLeft);
		text = StringUtils.replace(text, "<" + prefix + "unrankeds_left>", unrankedsLeft);
		if(text.contains("<" + prefix + "elo_rank") && plugin.getRankManager() != null) {
			text = StringUtils.replace(text, "<" + prefix + "elo_rank>", plugin.getRankManager().getRank(p).getName());
		}
		if(text.contains("<" + prefix + "limits_update>")) {
			long millis = 86400000-(System.currentTimeMillis()-stats.getLimitsUpdated()+LegionPractice.dateFix);
			String limitsUpdate = new SimpleDateFormat("HH:mm").format(new Date(millis));
			text = StringUtils.replace(text, "<" + prefix + "limits_update>", limitsUpdate);
		}
		text = StringUtils.replace(text, "<" + prefix + "global_elo>", stats.getGlobalElo() + "");
		for(Entry<String, Long> e : stats.getCooldowns().entrySet()) {
			text = StringUtils.replace(text, "<" + prefix + "is_cooldown_" + e.getKey() + ">", stats.isOnCooldown(e.getKey()) + "");
			text = StringUtils.replace(text, "<" + prefix + "cooldown_" + e.getKey() + ">", stats.getCooldownString(e.getKey()));
		}
		for(String s : PlayerStats.COOLDOWNS) {
			text = StringUtils.replace(text, "<" + prefix + "is_cooldown_" + s + ">", "false");
		}
		for(BattleKit kit : plugin.kits) {
			if(kit.isElo()) {
				text = StringUtils.replace(text, "<" + prefix + "elo_" + kit.getName() + ">", stats.getElo(kit) + "");
			}
		}
		Party party = Party.getParty(p);
		text = StringUtils.replace(text, "<" + prefix + "is_party>", (party != null) + "");
		if(party != null) {
			text = StringUtils.replace(text, "<" + prefix + "party_members>", party.getMembers().size() + "");
			text = StringUtils.replace(text, "<" + prefix + "party_owner>", party.getOwner());
		}
		else {
			text = StringUtils.replace(text, "<" + prefix + "party_members>", "0");
			text = StringUtils.replace(text, "<" + prefix + "party_owner>", "None");
		}
		boolean ep = false;
		if(p.hasMetadata(PearlListener.COOLDOWN_META) && text.contains("ender")) {
			MetadataValue m = plugin.getMetadata(p, PearlListener.COOLDOWN_META);
			if(m != null && m.value() != null) {
				double i = PearlManager.getCooldown()*1000;
				double l = m.asLong();
				double c = System.currentTimeMillis();
				if(l+i > c) {
					ep = true;
					double x = (l+i-c)/1000;
					text = StringUtils.replace(text, "<" + prefix + "enderpearl_cooldown>", PearlManager.getFormat().format(x));
				}
			}
			text = StringUtils.replace(text, "<" + prefix + "enderpearl_cooldown>", "--");
		}
		text = StringUtils.replace(text, "<" + prefix + "is_enderpearl_cooldown>", ep + "");
		if(fight == null && party != null) {
			fight = party.getFight();
		}
		PvPEvent event = PvPEvent.getEvent(p);
		if(event != null) {
			if(event instanceof Sumo) {
				Sumo sumo = (Sumo) event;
				if(sumo.getP1() != null) {
					text = StringUtils.replace(text, "<" + prefix + "current_fight_player1>", sumo.getP1());
					Player p1 = Bukkit.getPlayer(sumo.getP1());
					if(p1 != null) {
						text = StringUtils.replace(text, "<" + prefix + "player1_cps>", ClicksPerSecond.getCPS(p1) + "");
						text = StringUtils.replace(text, "<" + prefix + "player1_ping>", getPingString(p1, plugin));
					}
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "current_fight_player1>", "--");
					text = StringUtils.replace(text, "<" + prefix + "player1_ping>", "--");
					text = StringUtils.replace(text, "<" + prefix + "player1_cps>", "--");
				}
				if(sumo.getP2() != null) {
					text = StringUtils.replace(text, "<" + prefix + "current_fight_player2>", sumo.getP2());
					Player p2 = Bukkit.getPlayer(sumo.getP2());
					if(p2 != null) {
						text = StringUtils.replace(text, "<" + prefix + "player2_ping>", getPingString(p2, plugin));
						text = StringUtils.replace(text, "<" + prefix + "player2_cps>", ClicksPerSecond.getCPS(p2) + "");
					}
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "current_fight_player2>", "--");
					text = StringUtils.replace(text, "<" + prefix + "player2_ping>", "--");
					text = StringUtils.replace(text, "<" + prefix + "player2_cps>", "--");
				}
				if(event.hasStarted()) {
					if(text.contains("<" + prefix + "total_duration>")) text = StringUtils.replace(text, "<" + prefix + "total_duration>", new SimpleDateFormat("mm:ss").format(new Date(System.currentTimeMillis()-sumo.getStartTime()+LegionPractice.dateFix)));
				}
				else text = StringUtils.replace(text, "<" + prefix + "total_duration>", "--:--");
				if(sumo.getDuel() != null) {
					if(text.contains("<" + prefix + "duration>")) text = StringUtils.replace(text, "<" + prefix + "duration>", new SimpleDateFormat("mm:ss").format(new Date(sumo.getDuel().getDuration()+LegionPractice.dateFix)));
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "duration>", "--:--");
				}
				if(sumo.getArena() != null) {
					text = StringUtils.replace(text, "<" + prefix + "arena>", sumo.getArena().getName());	
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "arena>", "--");	
				}
				text = StringUtils.replace(text, "<" + prefix + "sumo_started>", sumo.hasStarted() + "");
				text = StringUtils.replace(text, "<" + prefix + "estimated_time_left>", sumo.getEstimatedTimeLeft());
				text = StringUtils.replace(text, "<" + prefix + "players_left>", sumo.getPlayers().size() + "");
				text = StringUtils.replace(text, "<" + prefix + "total_players>", sumo.totalPlayers + "");
				if(sumo.getKit() != null) {
					text = StringUtils.replace(text, "<" + prefix + "raw_kit>", sumo.getKit().getName());
					text = StringUtils.replace(text, "<" + prefix + "kit>", sumo.getKit().getFancyName());
					text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", ChatColor.stripColor(sumo.getKit().getFancyName()));
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "raw_kit>", "--");
					text = StringUtils.replace(text, "<" + prefix + "kit>", "--");
					text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", "--");

				}
				text = StringUtils.replace(text, "<" + prefix + "alive>", Integer.toString(sumo.getPlayers().size()));
			}
			else if(event instanceof Brackets) {
				Brackets brackets = (Brackets) event;
				text = StringUtils.replace(text, "<" + prefix + "brackets_started>", brackets.hasStarted() + "");
				if(brackets.getP1() != null) {
					text = StringUtils.replace(text, "<" + prefix + "current_fight_player1>", brackets.getP1());
					Player p1 = Bukkit.getPlayer(brackets.getP1());
					if(p1 != null) {
						text = StringUtils.replace(text, "<" + prefix + "player1_cps>", ClicksPerSecond.getCPS(p1) + "");
						text = StringUtils.replace(text, "<" + prefix + "player1_ping>", getPingString(p1, plugin));
					}
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "current_fight_player1>", "--");
					text = StringUtils.replace(text, "<" + prefix + "player1_ping>", "--");
					text = StringUtils.replace(text, "<" + prefix + "player1_cps>", "--");
				}
				if(brackets.getP2() != null) {
					text = StringUtils.replace(text, "<" + prefix + "current_fight_player2>", brackets.getP2());
					Player p2 = Bukkit.getPlayer(brackets.getP2());
					if(p2 != null) {
						text = StringUtils.replace(text, "<" + prefix + "player2_ping>", getPingString(p2, plugin));
						text = StringUtils.replace(text, "<" + prefix + "player2_cps>", ClicksPerSecond.getCPS(p2) + "");
					}
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "current_fight_player2>", "--");
					text = StringUtils.replace(text, "<" + prefix + "player2_ping>", "--");
					text = StringUtils.replace(text, "<" + prefix + "player2_cps>", "--");
				}
				if(event.hasStarted()) {
					if(text.contains("<" + prefix + "total_duration>")) text = StringUtils.replace(text, "<" + prefix + "total_duration>", new SimpleDateFormat("mm:ss").format(new Date(System.currentTimeMillis()-brackets.getStartTime()+LegionPractice.dateFix)));
				}
				else text = StringUtils.replace(text, "<" + prefix + "total_duration>", "--:--");
				if(brackets.getDuel() != null) {
					if(text.contains("<" + prefix + "duration>")) text = StringUtils.replace(text, "<" + prefix + "duration>", new SimpleDateFormat("mm:ss").format(new Date(brackets.getDuel().getDuration()+LegionPractice.dateFix)));
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "duration>", "--:--");
				}
				if(brackets.getArena() != null) {
					text = StringUtils.replace(text, "<" + prefix + "arena>", brackets.getArena().getName());	
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "arena>", "--");	
				}
				text = StringUtils.replace(text, "<" + prefix + "estimated_time_left>", brackets.getEstimatedTimeLeft());
				text = StringUtils.replace(text, "<" + prefix + "players_left>", brackets.getPlayers().size() + "");
				text = StringUtils.replace(text, "<" + prefix + "total_players>", brackets.totalPlayers + "");
				if(brackets.getKit() != null) {
					text = StringUtils.replace(text, "<" + prefix + "raw_kit>", brackets.getKit().getName());
					text = StringUtils.replace(text, "<" + prefix + "kit>", brackets.getKit().getFancyName());
					text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", ChatColor.stripColor(brackets.getKit().getFancyName()));
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "raw_kit>", "--");
					text = StringUtils.replace(text, "<" + prefix + "kit>", "--");
					text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", "--");

				}
				text = StringUtils.replace(text, "<" + prefix + "alive>", Integer.toString(brackets.getPlayers().size()));
			}
			else if(event instanceof KOTH) {
				KOTH koth = (KOTH) event;
				text = StringUtils.replace(text, "<" + prefix + "koth_started>", koth.hasStarted() + "");
				if(text.contains("<" + prefix + "total_duration>")) text = StringUtils.replace(text, "<" + prefix + "total_duration>", event.hasStarted() ? new SimpleDateFormat("mm:ss").format(new Date(System.currentTimeMillis()-koth.startedTime+LegionPractice.dateFix)) : "00:00");
				if(text.contains("<" + prefix + "timer>")) text = StringUtils.replace(text, "<" + prefix + "timer>", event.hasStarted() ? new SimpleDateFormat("mm:ss").format(new Date((koth.counter*1000)+LegionPractice.dateFix)) : "00:00");
				if(text.contains("<" + prefix + "capper>")) text = StringUtils.replace(text, "<" + prefix + "capper>", (koth.capper == null || Bukkit.getPlayer(koth.capper) == null)  ? "no one" : Bukkit.getPlayer(koth.capper).getName());
				if(text.contains("<" + prefix + "capper_team>")) text = StringUtils.replace(text, "<" + prefix + "capper_team>", koth.capperTeam == null  ? "none" : koth.getTeamName(koth.capperTeam));
			}
			else if(event instanceof Juggernaut) {
				Juggernaut jug = (Juggernaut) event;
				text = StringUtils.replace(text, "<" + prefix + "juggernaut_started>", jug.hasStarted() + "");
				if(text.contains("<" + prefix + "total_duration>")) text = StringUtils.replace(text, "<" + prefix + "total_duration>", event.hasStarted() ? new SimpleDateFormat("mm:ss").format(new Date(System.currentTimeMillis()-jug.getStartTime()+LegionPractice.dateFix)) : "00:00");
				text = StringUtils.replace(text, "<" + prefix + "juggernaut>", jug.getJuggernaut() != null ? jug.getJuggernaut() : "no one");
			}
			else if(event instanceof LMS) {
				LMS lms = (LMS) event;
				text = StringUtils.replace(text, "<" + prefix + "lms_started>", lms.hasStarted() + "");
				if(text.contains("<" + prefix + "total_duration>")) text = StringUtils.replace(text, "<" + prefix + "total_duration>", event.hasStarted() ? new SimpleDateFormat("mm:ss").format(new Date(System.currentTimeMillis()-lms.getStartTime()+LegionPractice.dateFix)) : "00:00");
				text = StringUtils.replace(text, "<" + prefix + "alive>", lms.getAlive() != null ? lms.getAlive().size() + "" : "0");
				text = StringUtils.replace(text, "<" + prefix + "lms_players>", lms.getPlayers() != null ? lms.getPlayers().size() + "" : "0");
			}
		}
		if(fight != null) {
			if(fight.getArena() != null) {
				text = StringUtils.replace(text, "<" + prefix + "raw_arena>", fight.getArena().getName());
				text = StringUtils.replace(text, "<" + prefix + "arena>", fight.getArena().getDisplayName());
			}
			else {
				text = StringUtils.replace(text, "<" + prefix + "raw_arena>", "--");
				text = StringUtils.replace(text, "<" + prefix + "arena>", "--");
			}
			if(fight.getKit() != null) {
				text = StringUtils.replace(text, "<" + prefix + "raw_kit>", fight.getKit().getName());
				text = StringUtils.replace(text, "<" + prefix + "ranked>", fight.getKit().isElo() + "");
				text = StringUtils.replace(text, "<" + prefix + "kit>", fight.getKit().getFancyName());
				text = StringUtils.replace(text, "<" + prefix + "build>", fight.getKit().isBuild() + "");
				text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", ChatColor.stripColor(fight.getKit().getFancyName()));
			}
			else {
				text = StringUtils.replace(text, "<" + prefix + "raw_kit>", "--");
				text = StringUtils.replace(text, "<" + prefix + "ranked>", "--");
				text = StringUtils.replace(text, "<" + prefix + "kit>", "--");
				text = StringUtils.replace(text, "<" + prefix + "build>", "--");
				text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", "--");
			}
			if(text.contains("<" + prefix + "duration>")) text = StringUtils.replace(text, "<" + prefix + "duration>", new SimpleDateFormat("mm:ss").format(new Date(fight.getDuration()+LegionPractice.dateFix)));
			if(fight instanceof Duel) {
				Duel duel = (Duel) fight;
				String opponent = duel.getP1();
				if(opponent.equals(p.getName())) {
					opponent = duel.getP2();
				}
				text = StringUtils.replace(text, "<" + prefix + "opponent>", opponent);
				Player op = Bukkit.getPlayer(opponent);
				if(op != null) {
					text = StringUtils.replace(text, "<" + prefix + "opponent_cps>", ClicksPerSecond.getCPS(op) + "");
					text = StringUtils.replace(text, "<" + prefix + "opponent_ping>", getPingString(op, plugin));
					if(fight.getKit() != null) {
						if(text.contains("<" + prefix + "opponent_elo")) {
							PlayerStats opponentStats = PlayerStats.getStats(op.getUniqueId(), false, false);
							String opponentElo = opponentStats.getGlobalElo() + "";
							text = StringUtils.replace(text, "<" + prefix + "opponent_elo>", opponentElo);
						}
					}
				}
				else text = StringUtils.replace(text, "<" + prefix + "opponent_ping>", 0 + "");
				BestOf bo = duel.getBestOf();
				if(bo != null && bo.getRounds() > 1) {
					text = StringUtils.replace(text, "<" + prefix + "is_bestof>", "true");
					text = StringUtils.replace(text, "<" + prefix + "total_rounds>", bo.getRounds() + "");
					text = StringUtils.replace(text, "<" + prefix + "round>", bo.getCurrentRound() + "");
					text = StringUtils.replace(text, "<" + prefix + "own_wins>", bo.getWins(p) + "");
					text = StringUtils.replace(text, "<" + prefix + "opponent_wins>", op != null ? bo.getWins(op) + "" : "--");
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "is_bestof>", "false");
					text = StringUtils.replace(text, "<" + prefix + "total_rounds>", "");
					text = StringUtils.replace(text, "<" + prefix + "round>", "1");
					text = StringUtils.replace(text, "<" + prefix + "own_wins>", "0");
					text = StringUtils.replace(text, "<" + prefix + "opponent_wins>", "0");
				}
			}
			else if(fight instanceof BotDuel) {
				BotDuel botDuel = (BotDuel) fight;
				String opponent = botDuel.getP1();
				if(opponent.equals(p.getName())) {
					opponent = botDuel.getP2().getNPC().getName();
				}
				text = StringUtils.replace(text, "<" + prefix + "opponent>", opponent);
				text = StringUtils.replace(text, "<" + prefix + "opponent_cps>", "[display=false]");
				text = StringUtils.replace(text, "<" + prefix + "opponent_ping>", 0 + "");
				text = StringUtils.replace(text, "<" + prefix + "opponent_elo>", "[display=false]");
				BestOf bo = botDuel.getBestOf();
				if(bo != null && bo.getRounds() > 1) {
					text = StringUtils.replace(text, "<" + prefix + "is_bestof>", "true");
					text = StringUtils.replace(text, "<" + prefix + "total_rounds>", bo.getRounds() + "");
					text = StringUtils.replace(text, "<" + prefix + "round>", bo.getCurrentRound() + "");
					text = StringUtils.replace(text, "<" + prefix + "own_wins>", bo.getWins(p) + "");
					text = StringUtils.replace(text, "<" + prefix + "opponent_wins>", bo.getBotWins(p) + "");
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "is_bestof>", "false");
					text = StringUtils.replace(text, "<" + prefix + "total_rounds>", "");
					text = StringUtils.replace(text, "<" + prefix + "round>", "1");
					text = StringUtils.replace(text, "<" + prefix + "own_wins>", "0");
					text = StringUtils.replace(text, "<" + prefix + "opponent_wins>", "0");
				}
			}
			else if(fight instanceof PartyVsBots) {
				PartyVsBots bot = (PartyVsBots) fight;
				text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", bot.getBotsTeam().size() + "");
				text = StringUtils.replace(text, "<" + prefix + "own_team_members>", bot.getPlayersTeam().size() + "");
				text = StringUtils.replace(text, "<" + prefix + "own_team_left>", bot.getPlayersAlive().size() + "");
				text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", bot.getBotsAlive().size() + "");
			}
			else if(fight instanceof PartyFFA) {
				PartyFFA ffa = (PartyFFA) fight;
				text = StringUtils.replace(text, "<" + prefix + "own_team_left>", ffa.getAlive().size() + "");
				text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", ffa.getAlive().size() + "");
			}
			else if(fight instanceof PartySplit) {
				PartySplit split = (PartySplit) fight;
				if(split.getTeam1().contains(p.getName())) {
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", split.getTeam2().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "own_team_members>", split.getTeam1().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "own_team_left>", split.getAlive1().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", split.getAlive2().size() + "");
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", split.getTeam1().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "own_team_members>", split.getTeam2().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "own_team_left>", split.getAlive2().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", split.getAlive1().size() + "");
				}
			}
			else if(fight instanceof PartyVsParty) {
				PartyVsParty pvp = (PartyVsParty) fight;
				if(party.equals(pvp.getParty1())) {
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", pvp.getParty2().getMembers().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_owner>", pvp.getParty2().getOwner());
					text = StringUtils.replace(text, "<" + prefix + "own_team_left>", pvp.getPartyAlive1().size() + "");

					text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", pvp.getPartyAlive2().size() + "");
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", pvp.getParty1().getMembers().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_owner>", pvp.getParty1().getOwner());
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", pvp.getPartyAlive1().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "own_team_left>", pvp.getPartyAlive2().size() + "");
				}
			}
			else if(fight instanceof FFAFight) {
				FFAFight ffa = (FFAFight) fight;
				text = StringUtils.replace(text, "<" + prefix + "ffa_players>", ffa.getPlayers().size() + "");
				String s = new SimpleDateFormat(plugin.getConfig().getString("current-time-format")).format(plugin.getFFAManager().nextReset());
				text = StringUtils.replace(text, "<" + prefix + "ffa_rollback>", s);
				text = StringUtils.replace(text, "<" + prefix + "is_ffa>", "true");
			}
			text = StringUtils.replace(text, "<" + prefix + "is_ffa>", "false");
		}
		else if(p.hasMetadata(QueueManager.waitingQueue)) {
			QueueRunnable ranked = QueueRunnable.getRanked(p);
			if(ranked != null) {
				text = StringUtils.replace(text, "<" + prefix + "search_range1>", ranked.isAnyone() ? "-" : (ranked.getCurrentElo()-ranked.getRange()) + "");
				text = StringUtils.replace(text, "<" + prefix + "search_range2>", ranked.isAnyone() ? "-" : (ranked.getCurrentElo()+ranked.getRange()) + "");
				text = StringUtils.replace(text, "<" + prefix + "kit>", ranked.getKit().getFancyName());
				text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", ChatColor.stripColor(ranked.getKit().getFancyName()));
				text = StringUtils.replace(text, "<" + prefix + "queuetype>", ranked.getKit().isElo() ? "Ranked" : "Unranked");
				text = StringUtils.replace(text, "<" + prefix + "ranked>", "true");
			}
			else {
				text = StringUtils.replace(text, "<" + prefix + "queuetype>", "Unranked");
				text = StringUtils.replace(text, "<" + prefix + "search_range1>", "-");
				text = StringUtils.replace(text, "<" + prefix + "search_range2>", "-");
				text = StringUtils.replace(text, "<" + prefix + "ranked>", "false");
				for(Entry<BattleKit, String> s : QueueManager.unranked.entrySet()) {
					if(s.getValue() != null && s.getValue().equals(p.getName())) {
						text = StringUtils.replace(text, "<" + prefix + "kit>", s.getKey().getFancyName());
						text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", ChatColor.stripColor(s.getKey().getFancyName()));
					}
				}
				text = StringUtils.replace(text, "<" + prefix + "kit>", "unknown");
				text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", "unknown");
			}
			if(text.contains("<" + prefix + "wait_t")) {
				MetadataValue mv = plugin.getMetadata(p, QueueManager.waitingQueue);
				if(mv != null && mv.value() != null && mv.asLong() > 0) {
					text = StringUtils.replace(text, "<" + prefix + "wait_time>", new SimpleDateFormat("mm:ss").format(new Date(System.currentTimeMillis()-mv.asLong()+LegionPractice.dateFix)));
				}
				else text = StringUtils.replace(text, "<" + prefix + "wait_time>", "--:--");
			}
			else text = StringUtils.replace(text, "<" + prefix + "wait_time>", "--:--");
		}
		else if(plugin.getSpectatorHandler().isSpectator(p)) {
			fight = plugin.getSpectatorHandler().getSpectatingFight().getOrDefault(p.getUniqueId(), null);
			if(fight != null) {
				if(fight.getArena() != null) {
					text = StringUtils.replace(text, "<" + prefix + "raw_arena>", fight.getArena().getName());
					text = StringUtils.replace(text, "<" + prefix + "arena>", fight.getArena().getDisplayName());
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "raw_arena>", "--");
					text = StringUtils.replace(text, "<" + prefix + "arena>", "--");
				}
				if(fight.getKit() != null) {
					text = StringUtils.replace(text, "<" + prefix + "raw_kit>", fight.getKit().getName());
					text = StringUtils.replace(text, "<" + prefix + "ranked>", fight.getKit().isElo() + "");
					text = StringUtils.replace(text, "<" + prefix + "kit>", fight.getKit().getFancyName());
					text = StringUtils.replace(text, "<" + prefix + "build>", fight.getKit().isBuild() + "");
					text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", ChatColor.stripColor(fight.getKit().getFancyName()));
				}
				else {
					text = StringUtils.replace(text, "<" + prefix + "raw_kit>", "--");
					text = StringUtils.replace(text, "<" + prefix + "ranked>", "--");
					text = StringUtils.replace(text, "<" + prefix + "kit>", "--");
					text = StringUtils.replace(text, "<" + prefix + "build>", "--");
					text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", "--");
				}
				if(text.contains("<" + prefix + "duration>")) text = StringUtils.replace(text, "<" + prefix + "duration>", new SimpleDateFormat("mm:ss").format(new Date(fight.getDuration()+LegionPractice.dateFix)));
				if(fight instanceof Duel) {
					Duel duel = (Duel) fight;
					String opponent = duel.getP1();
					if(opponent.equals(p.getName())) {
						opponent = duel.getP2();
					}
					/*
					text = StringUtils.replace(text, "<" + prefix + "opponent>", opponent);
					Player op = Bukkit.getPlayer(opponent);
					if(op != null) {
						text = StringUtils.replace(text, "<" + prefix + "opponent_cps>", ClicksPerSecond.getCPS(op) + "");
						text = StringUtils.replace(text, "<" + prefix + "opponent_ping>", getPingString(op, plugin));
						if(fight.getKit() != null) {
							if(text.contains("<" + prefix + "opponent_elo")) {
								PlayerStats opponentStats = PlayerStats.getStats(op.getUniqueId(), false, false);
								String opponentElo = opponentStats.getGlobalElo() + "";
								text = StringUtils.replace(text, "<" + prefix + "opponent_elo>", opponentElo);
							}
						}
					}
					else text = StringUtils.replace(text, "<" + prefix + "opponent_ping>", 0 + "");
					 */
					BestOf bo = duel.getBestOf();
					if(bo != null && bo.getRounds() > 1) {
						text = StringUtils.replace(text, "<" + prefix + "is_bestof>", "true");
						text = StringUtils.replace(text, "<" + prefix + "total_rounds>", bo.getRounds() + "");
						text = StringUtils.replace(text, "<" + prefix + "round>", bo.getCurrentRound() + "");
						//text = StringUtils.replace(text, "<" + prefix + "own_wins>", bo.getWins(p) + "");
						//text = StringUtils.replace(text, "<" + prefix + "opponent_wins>", op != null ? bo.getWins(op) + "" : "--");
					}
					else {
						text = StringUtils.replace(text, "<" + prefix + "is_bestof>", "false");
						text = StringUtils.replace(text, "<" + prefix + "total_rounds>", "[display=false]");
						text = StringUtils.replace(text, "<" + prefix + "round>", "[display=false]");
						/*
						text = StringUtils.replace(text, "<" + prefix + "own_wins>", "[display=false]");
						text = StringUtils.replace(text, "<" + prefix + "opponent_wins>", "[display=false]");
						 */
					}
				}
				else if(fight instanceof BotDuel) {
					BotDuel botDuel = (BotDuel) fight;
					String opponent = botDuel.getP1();
					if(opponent.equals(p.getName())) {
						opponent = botDuel.getP2().getNPC().getName();
					}
					/*
					text = StringUtils.replace(text, "<" + prefix + "opponent>", opponent);
					text = StringUtils.replace(text, "<" + prefix + "opponent_cps>", "--");
					text = StringUtils.replace(text, "<" + prefix + "opponent_ping>", 0 + "");
					text = StringUtils.replace(text, "<" + prefix + "opponent_elo>", "--");
					 */
					BestOf bo = botDuel.getBestOf();
					if(bo != null && bo.getRounds() > 1) {
						text = StringUtils.replace(text, "<" + prefix + "is_bestof>", "true");
						text = StringUtils.replace(text, "<" + prefix + "total_rounds>", bo.getRounds() + "");
						text = StringUtils.replace(text, "<" + prefix + "round>", bo.getCurrentRound() + "");
						//text = StringUtils.replace(text, "<" + prefix + "own_wins>", bo.getWins(p) + "");
						//text = StringUtils.replace(text, "<" + prefix + "opponent_wins>", bo.getBotWins(p) + "");
					}
					else {
						text = StringUtils.replace(text, "<" + prefix + "is_bestof>", "false");
						text = StringUtils.replace(text, "<" + prefix + "total_rounds>", "[display=false]");
						text = StringUtils.replace(text, "<" + prefix + "round>", "[display=false]");
						/*
						text = StringUtils.replace(text, "<" + prefix + "own_wins>", "[display=false]");
						text = StringUtils.replace(text, "<" + prefix + "opponent_wins>", "[display=false]");
						 */
					}
				}
				else if(fight instanceof PartyVsBots) {
					PartyVsBots bot = (PartyVsBots) fight;
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", bot.getBotsTeam().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "own_team_members>", bot.getPlayersTeam().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "own_team_left>", bot.getPlayersAlive().size() + "");
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", bot.getBotsAlive().size() + "");
				}
				else if(fight instanceof PartyFFA) {
					PartyFFA ffa = (PartyFFA) fight;
					text = StringUtils.replace(text, "<" + prefix + "own_team_left>", "[display=false]");
					text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", ffa.getAlive().size() + "");
				}
				else if(fight instanceof PartySplit) {
					PartySplit split = (PartySplit) fight;
					if(split.getTeam1().contains(p.getName())) {
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", split.getTeam2().size() + "");
						text = StringUtils.replace(text, "<" + prefix + "own_team_members>", split.getTeam1().size() + "");
						text = StringUtils.replace(text, "<" + prefix + "own_team_left>", split.getAlive1().size() + "");
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", split.getAlive2().size() + "");
					}
					else if(split.getTeam2().contains(p.getName())){
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", split.getTeam1().size() + "");
						text = StringUtils.replace(text, "<" + prefix + "own_team_members>", split.getTeam2().size() + "");
						text = StringUtils.replace(text, "<" + prefix + "own_team_left>", split.getAlive2().size() + "");
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", split.getAlive1().size() + "");
					}
					else {
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", "[display=false]");
						text = StringUtils.replace(text, "<" + prefix + "own_team_members>", "[display=false]");
						text = StringUtils.replace(text, "<" + prefix + "own_team_left>", "[display=false]");
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", "[display=false]");
					}
				}
				else if(fight instanceof PartyVsParty) {
					PartyVsParty pvp = (PartyVsParty) fight;
					if(party == null) {
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", "[display=false]");
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", "[display=false]");
						text = StringUtils.replace(text, "<" + prefix + "own_team_left>", "[display=false]");						
					}
					else if(party.equals(pvp.getParty1())) {
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", pvp.getParty2().getMembers().size() + "");
						text = StringUtils.replace(text, "<" + prefix + "own_team_left>", pvp.getPartyAlive1().size() + "");
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", pvp.getPartyAlive2().size() + "");
					}
					else {
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", pvp.getParty1().getMembers().size() + "");
						text = StringUtils.replace(text, "<" + prefix + "enemy_team_left>", pvp.getPartyAlive1().size() + "");
						text = StringUtils.replace(text, "<" + prefix + "own_team_left>", pvp.getPartyAlive2().size() + "");
					}
				}
				else if(fight instanceof FFAFight) {
					FFAFight ffa = (FFAFight) fight;
					text = StringUtils.replace(text, "<" + prefix + "ffa_players>", ffa.getPlayers().size() + "");
					String s = new SimpleDateFormat(plugin.getConfig().getString("current-time-format")).format(plugin.getFFAManager().nextReset());
					text = StringUtils.replace(text, "<" + prefix + "ffa_rollback>", s);
					text = StringUtils.replace(text, "<" + prefix + "is_ffa>", "true");
				}
				text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", "[display=false]");
				text = StringUtils.replace(text, "<" + prefix + "own_team_left>", "[display=false]");
				text = StringUtils.replace(text, "<" + prefix + "enemy_team_members>", "[display=false]");
				text = StringUtils.replace(text, "<" + prefix + "own_team_members>", "[display=false]");
				text = StringUtils.replace(text, "<" + prefix + "is_ffa>", "false");
				text = StringUtils.replace(text, "<" + prefix + "is_bestof>", "false");
			}
		}
		else if(p.hasMetadata(QueueManager.waitingQueue)) {
			QueueRunnable ranked = QueueRunnable.getRanked(p);
			if(ranked != null) {
				text = StringUtils.replace(text, "<" + prefix + "search_range1>", ranked.isAnyone() ? "-" : (ranked.getCurrentElo()-ranked.getRange()) + "");
				text = StringUtils.replace(text, "<" + prefix + "search_range2>", ranked.isAnyone() ? "-" : (ranked.getCurrentElo()+ranked.getRange()) + "");
				text = StringUtils.replace(text, "<" + prefix + "kit>", ranked.getKit().getFancyName());
				text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", ChatColor.stripColor(ranked.getKit().getFancyName()));
				text = StringUtils.replace(text, "<" + prefix + "queuetype>", ranked.getKit().isElo() ? "Ranked" : "Unranked");
				text = StringUtils.replace(text, "<" + prefix + "ranked>", "true");
			}
			else {
				text = StringUtils.replace(text, "<" + prefix + "queuetype>", "Unranked");
				text = StringUtils.replace(text, "<" + prefix + "search_range1>", "-");
				text = StringUtils.replace(text, "<" + prefix + "search_range2>", "-");
				text = StringUtils.replace(text, "<" + prefix + "ranked>", "false");
				for(Entry<BattleKit, String> s : QueueManager.unranked.entrySet()) {
					if(s.getValue() != null && s.getValue().equals(p.getName())) {
						text = StringUtils.replace(text, "<" + prefix + "kit>", s.getKey().getFancyName());
						text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", ChatColor.stripColor(s.getKey().getFancyName()));
					}
				}
				text = StringUtils.replace(text, "<" + prefix + "kit>", "unknown");
				text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", "unknown");
			}
			if(text.contains("<" + prefix + "wait_time")) {
				MetadataValue mv = plugin.getMetadata(p, QueueManager.waitingQueue);
				if(mv != null && mv.value() != null && mv.asLong() > 0) {
					text = StringUtils.replace(text, "<" + prefix + "wait_time>", new SimpleDateFormat("mm:ss").format(new Date(System.currentTimeMillis()-mv.asLong()+LegionPractice.dateFix)));
				}
				else text = StringUtils.replace(text, "<" + prefix + "wait_time>", "--:--");
			}
			else text = StringUtils.replace(text, "<" + prefix + "wait_time>", "--:--");
		}
		text = StringUtils.replace(text, "<" + prefix + "raw_arena>", "None");
		text = StringUtils.replace(text, "<" + prefix + "arena>", "None");
		text = StringUtils.replace(text, "<" + prefix + "raw_kit>", "None");
		text = StringUtils.replace(text, "<" + prefix + "kit>", "None");
		text = StringUtils.replace(text, "<" + prefix + "no_colors_kit>", "None");
		Damageable dam = (Damageable) p;
		text = StringUtils.replace(text, "<" + prefix + "health>", (int)dam.getHealth() + "");
		text = StringUtils.replace(text, "<" + prefix + "is_ffa>", "false");
		text = StringUtils.replace(text, "<" + prefix + "is_bestof>", "false");
		return PlaceholderAPIManager.getManager().handlePlaceHolders(p, text);
	}

	public String doCommonPlaceholders(Player p, String text, String prefix) {
		if(System.currentTimeMillis()-commonsUpdated > (LegionPractice.performanceMode ? 10000 : 5000)) {
			HashMap<String, String> common = getCommonPlaceHolders(prefix);
			commonPlaceholders = common;
		}
		return doPlaceholders(p, commonPlaceholders, text);
	}

	private String getPingString(Player p, LegionPractice plugin) {
		int ping = plugin.getNMSAccessProvider().getAccess().getPing(p);
		if(ping >= 10000 || ping < 0) return "n/a";
		return ping + "";
	}

	public HashMap<String, String> getCommonPlaceHolders(String prefix) {
		LegionPractice plugin = LegionPractice.getInstance();
		HashMap<String, AutomaticEventType> nextEvents = AutomaticEventTask.getNextEvents();
		String nextTime = null;
		int nextTimeValue = Integer.MAX_VALUE;
		AutomaticEventType nextEvent = null;
		Date currentTime = new Date(TimeUtil.getCurrentDisplayTime());
		@SuppressWarnings("deprecation")
		int cur = currentTime.getHours()*60+currentTime.getMinutes();
		for(Entry<String, AutomaticEventType> e : nextEvents.entrySet()) {
			int eTimeValue = Integer.parseInt(e.getKey().split(":")[0])*60+Integer.parseInt(e.getKey().split(":")[1]);
			if(eTimeValue < cur) {
				eTimeValue += 24*60;
			}
			if(nextTime == null || (nextTimeValue > eTimeValue)) {
				nextTime = e.getKey();
				nextEvent = e.getValue();
				nextTimeValue = eTimeValue;
			}
		}
		HashMap<String, String> commonPlaceholders = new HashMap<String, String>();
		if(nextTime != null) {
			String[] evtTime = nextTime.split(":");
			int hours = Integer.parseInt(evtTime[0]);
			String eventTime = nextTime == null ? "--:--" : (hours + ":" + evtTime[1]);
			String eventName = nextEvent == null ? "none" : nextEvent.toString();
			commonPlaceholders.put("<" + prefix + "next_event_time>", eventTime);
			commonPlaceholders.put("<" + prefix + "next_event_name>", eventName);
		}
		int staff = 0;
		int queue = 0;
		int fight = 0;
		int ranked = 0;
		int players = 0;
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(PermissionsManager.hasPermission(player, Permission.STAFF)) {
				staff++;
			}
			if(Fight.isInFight(player, plugin)) {
				fight++;
			}
			if(player.hasMetadata(QueueManager.waitingQueue)) {
				queue++;
			}
			if(player.hasMetadata(QueueRunnable.RANKED_QUEUE)) {
				ranked++;
			}
			players++;
		}
		commonPlaceholders.put("<" + prefix + "last_report>", ReportHook.lastReport);
		commonPlaceholders.put("<" + prefix + "players>", players + "");
		commonPlaceholders.put("<" + prefix + "in_fight>", fight + "");
		commonPlaceholders.put("<" + prefix + "in_queue>", queue + "");
		commonPlaceholders.put("<" + prefix + "in_ranked_queue>", ranked + "");
		commonPlaceholders.put("<" + prefix + "in_unranked_queue>", (queue-ranked) + "");
		commonPlaceholders.put("<" + prefix + "online_staff>", staff + "");
		commonPlaceholders.put("<" + prefix + "tps>", TPSUtil.get1MinTPSRounded() + "");
		commonPlaceholders.put("<" + prefix + "max_players>", Bukkit.getMaxPlayers() + "");
		commonPlaceholders.put("<" + prefix + "time>", TimeUtil.getCurrentTime());
		commonPlaceholders.put("<" + prefix + "arenas>", plugin.arenas.size() + "");
		synchronized (PlayerStats.getTop()) {
			for(Entry<String, LinkedHashMap<String, Double>> e : PlayerStats.getTop().entrySet()) {
				int counter = 1;
				for(Entry<String, Double> p : e.getValue().entrySet()) {
					commonPlaceholders.put("<" + prefix + "top_" + e.getKey() + counter + ">", p.getKey());
					commonPlaceholders.put("<" + prefix + "top_" + e.getKey() + counter + "_value>", Integer.toString(p.getValue().intValue()));
					counter++;
				}
			}	
		}
		int free = 0;
		for(Arena a : plugin.arenas) {
			if(!a.isUsing() && !a.needsRollback() && !a.isFFA()) {
				free++;
			}
		}
		commonPlaceholders.put("<" + prefix + "free_arenas>", free + "");
		return commonPlaceholders;
	}

	private static String doPlaceholders(Player p, HashMap<String, String> placeholders, String s) {
		for(Entry<String, String> e : placeholders.entrySet()) {
			if(e.getKey() != null && e.getValue() != null && morePlaceholders(s)) {
				s = StringUtils.replace(s, e.getKey(), e.getValue(), 1);
			}
		}
		return PlaceholderAPIManager.getManager().handlePlaceHolders(p, s);
	}

	private static boolean morePlaceholders(String s) {
		for(char c : s.toCharArray()) {
			if(c == '<') {
				return true;
			}
		}
		return false;
	}
}
