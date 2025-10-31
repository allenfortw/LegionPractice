package tech.hexadevelopment.practice.hostedevents.automaticevents;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.hostedevents.brackets.Brackets;
import tech.hexadevelopment.practice.hostedevents.brackets.BracketsCommand;
import tech.hexadevelopment.practice.hostedevents.juggernaut.Juggernaut;
import tech.hexadevelopment.practice.hostedevents.juggernaut.JuggernautCommand;
import tech.hexadevelopment.practice.hostedevents.koth.KOTH;
import tech.hexadevelopment.practice.hostedevents.koth.KOTHCommand;
import tech.hexadevelopment.practice.hostedevents.lms.LMS;
import tech.hexadevelopment.practice.hostedevents.lms.LMSCommand;
import tech.hexadevelopment.practice.hostedevents.sumo.Sumo;
import tech.hexadevelopment.practice.hostedevents.sumo.SumoCommand;
import tech.hexadevelopment.practice.stats.Callback;
import tech.hexadevelopment.practice.utils.ClickableMessage;
import tech.hexadevelopment.practice.LegionPractice;

public class AutomaticEvent {

	private AutomaticEventType type;
	private LegionPractice plugin;
	private String juggernaut;
	private BattleKit kit;
	private boolean started;
	private String host;
	private Callback callback;

	public AutomaticEvent(LegionPractice plugin, String eventName, BattleKit kit) {
		this.plugin = plugin;
		this.kit = kit;
		String e = eventName.toLowerCase();
		if(e.equals("lms")) type = AutomaticEventType.LMS;
		if(e.equals("brackets")) type = AutomaticEventType.BRACKETS;
		if(e.equals("juggernaut")) type = AutomaticEventType.JUGGERNAUT;
		if(e.equals("koth")) type = AutomaticEventType.KOTH;
		if(e.equals("sumo")) type = AutomaticEventType.SUMO;
		if(isValidEvent()) {
			startBroadcast();
		}
	}

	public AutomaticEvent(LegionPractice plugin, String eventName) {
		this.plugin = plugin;
		String e = eventName.toLowerCase();
		if(e.equals("lms")) type = AutomaticEventType.LMS;
		if(e.equals("brackets")) type = AutomaticEventType.BRACKETS;
		if(e.equals("juggernaut")) type = AutomaticEventType.JUGGERNAUT;
		if(e.equals("koth")) type = AutomaticEventType.KOTH;
		if(e.equals("sumo")) type = AutomaticEventType.SUMO;
		if(isValidEvent()) {
			startBroadcast();
		}
	}

	public AutomaticEvent(LegionPractice plugin, String eventName, String juggernaut) {
		this.plugin = plugin;
		this.juggernaut = juggernaut;
		String e = eventName.toLowerCase();
		if(e.equals("lms")) type = AutomaticEventType.LMS;
		if(e.equals("brackets")) type = AutomaticEventType.BRACKETS;
		if(e.equals("juggernaut")) type = AutomaticEventType.JUGGERNAUT;
		if(e.equals("koth")) type = AutomaticEventType.KOTH;
		if(e.equals("sumo")) type = AutomaticEventType.SUMO;
		if(isValidEvent()) {
			startBroadcast();
		}
	}

	private boolean isValidEvent() {
		return type != null && !AutomaticEventTask.onGoing.containsValue(type.toString().toLowerCase());
	}

	public void setHoster(String host) {
		this.host = host;
	}

	private void startBroadcast() {
		if(type == AutomaticEventType.LMS) {
			if(LMSCommand.open) return;
			LMSCommand.open = true;
		}
		else if(type == AutomaticEventType.BRACKETS) {
			if(BracketsCommand.brackets != null) return;
			BracketsCommand.brackets = new Brackets(plugin);
		}
		else if(type == AutomaticEventType.SUMO) {
			if(SumoCommand.sumo != null) return;
			SumoCommand.sumo = new Sumo(plugin);
		}
		else if(type == AutomaticEventType.JUGGERNAUT) {
			if(JuggernautCommand.open) return;
			JuggernautCommand.open = true;
		}
		else if(type == AutomaticEventType.KOTH) {
			if(KOTHCommand.open) return;
			KOTHCommand.open = true;
		}
		new BukkitRunnable() {
			String hostName = host == null ? "Console" : host;
			String broadcast = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix") + plugin.getConfig().getString("automatic-events.broadcast." + type.toString().toLowerCase()).replace("<player>", hostName).replace("<host>", hostName));
			int counter = plugin.getConfig().getInt("automatic-events.broadcast.times");
			@Override
			public void run() {
				if(counter <= 0) {
					try{
						new BukkitRunnable() {

							@Override
							public void run() {
								start();
							}
						}.runTask(plugin);
					}catch(Exception e) {
						Bukkit.broadcastMessage(ChatColor.RED + "The event could not be started...");
					}
					this.cancel();
				}
				else if(!isEventRunning()){
					for(Player pl : Bukkit.getOnlinePlayers()) {
						ClickableMessage.sendMessage(pl, broadcast, "/" + type.toString().toLowerCase() + " join");
					}
				}
				counter--;
			}
		}.runTaskTimerAsynchronously(plugin, 0, 20*plugin.getConfig().getInt("automatic-events.broadcast.delay"));
	}

	private boolean isEventRunning() {
		for(PvPEvent e : PvPEvent.getCurrentPvPEvents()) {
			if(type == AutomaticEventType.LMS && e instanceof LMS) {
				return true;
			}
			if(type == AutomaticEventType.BRACKETS && e instanceof Brackets) {
				return true;
			}
			if(type == AutomaticEventType.JUGGERNAUT && e instanceof Juggernaut) {
				return true;
			}
			if(type == AutomaticEventType.KOTH && e instanceof KOTH) {
				return true;
			}
			if(type == AutomaticEventType.SUMO && e instanceof Sumo) {
				return true;
			}
		}
		return false;
	}

	private void start() {
		if(type == AutomaticEventType.LMS) {
			started = LMSCommand.tryToStart(Bukkit.getConsoleSender(), new String[]{
					"start", kit != null ? kit.getName() : plugin.getConfig().getString("automatic-events.lms-kit")
			},  plugin); 
			AutomaticEventTask.onGoing.remove(this);
			LMSCommand.open = false;
		}
		else if(type == AutomaticEventType.BRACKETS) {
			started = 	BracketsCommand.tryToStart(Bukkit.getConsoleSender(), kit != null ? kit.getName() : plugin.getConfig().getString("automatic-events.brackets-kit"), plugin);
			AutomaticEventTask.onGoing.remove(this);
		}
		else if(type == AutomaticEventType.SUMO) {
			started = SumoCommand.tryToStart(Bukkit.getConsoleSender(), kit != null ? kit.getName() : plugin.getConfig().getString("automatic-events.sumo-kit"), plugin);
			AutomaticEventTask.onGoing.remove(this);
		}
		else if(type == AutomaticEventType.JUGGERNAUT){
			started = JuggernautCommand.tryToStart(Bukkit.getConsoleSender(), new String[] {
					"start", juggernaut != null ? juggernaut : "-random", plugin.getConfig().getString("automatic-events.juggernaut-kit"),
							kit != null ? kit.getName() : plugin.getConfig().getString("automatic-events.juggernaut-player-kit")
			}, plugin);
			AutomaticEventTask.onGoing.remove(this);
		}
		else if(type == AutomaticEventType.KOTH) {
			started = KOTHCommand.tryToStart(Bukkit.getConsoleSender(), new String[]{kit != null ? kit.getName() : plugin.getConfig().getString("automatic-events.koth-kit")}, plugin);
			AutomaticEventTask.onGoing.remove(this);
			KOTHCommand.open = LegionPractice.getInstance().getConfig().getBoolean("koth.anytime-join");
		}
		else {
			new BukkitRunnable() {

				@Override
				public void run() {
					synchronized(AutomaticEventTask.onGoing) {
						AutomaticEventTask.onGoing.remove(AutomaticEvent.this);	
					}
				}
			}.runTaskLaterAsynchronously(plugin, 20*60);
		}
		if(callback != null) {
			callback.onSuccess(started ? 1 : -1);
			callback = null;
		}
	}


	public void setCallback(Callback callback) {
		this.callback = callback;
	}

}
