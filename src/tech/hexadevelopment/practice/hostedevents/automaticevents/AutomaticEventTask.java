package tech.hexadevelopment.practice.hostedevents.automaticevents;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.utils.TimeUtil;
import tech.hexadevelopment.practice.LegionPractice;

public class AutomaticEventTask extends BukkitRunnable{

	public static HashMap<AutomaticEvent, String> onGoing = new HashMap<AutomaticEvent, String>();

	private LegionPractice plugin;

	public AutomaticEventTask(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		Calendar calendar = TimeUtil.getCurrentCalendarTime();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		for(Entry<String, AutomaticEventType> string : getNextEvents().entrySet()) {
			String[] s = string.getKey().split(":");
			try{
				int confHour = Integer.parseInt(s[0]);
				int confMinute = Integer.parseInt(s[1]);
				if(confHour == hour && confMinute == minute) {
					if(onGoing.containsValue(string.getValue().toString())) return;
					onGoing.put(new AutomaticEvent(plugin, string.getValue().toString()), string.getValue().toString());
				}
			}catch(NumberFormatException | IndexOutOfBoundsException e) {
				Bukkit.getLogger().warning("Could not start automatic event! The format:"
						+ string + " is invalid! Format should be: hours:minutes:event"
						+ "(for example: 15:30:lms)");
			}
		}
	}

	public static HashMap<String, AutomaticEventType> getNextEvents() {
		HashMap<String, AutomaticEventType> events = new HashMap<String, AutomaticEventType>();
		for(String string : LegionPractice.getInstance().getConfig().getStringList("automatic-events.times")) {
			String[] s = string.split(":");
			try{
				int confHour = Integer.parseInt(s[0]);
				int confMinute = Integer.parseInt(s[1]);
				AutomaticEventType event = AutomaticEventType.fromString(s[2]);
				if(event != null) {
					events.put((confHour == 0 ? "00" : confHour) + ":" + (confMinute == 0 ? "00" : confMinute), event);
				}
			}catch(NumberFormatException | IndexOutOfBoundsException e) {
				Bukkit.getLogger().warning("Could not start automatic event! The format:"
						+ string + " is invalid! Format should be: hours:minutes:event"
						+ "(for example: 15:30:lms)");
			}
		}
		return events;
	}

	public static void startTask(LegionPractice plugin) {
		new AutomaticEventTask(plugin).runTaskTimerAsynchronously(plugin, 20*30, LegionPractice.performanceMode ? 20*45 : 20*20);
	}
}
