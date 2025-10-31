package tech.hexadevelopment.practice.hostedevents;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.hostedevents.brackets.BracketsCommand;
import tech.hexadevelopment.practice.hostedevents.juggernaut.Juggernaut;
import tech.hexadevelopment.practice.hostedevents.juggernaut.JuggernautCommand;
import tech.hexadevelopment.practice.hostedevents.koth.KOTHCommand;
import tech.hexadevelopment.practice.hostedevents.lms.LMSCommand;
import tech.hexadevelopment.practice.hostedevents.sumo.SumoCommand;

/**
 * 
 * @author Toppe5
 * @since 0.1
 */
public interface PvPEvent {

	/**
	 * Method to start the event.
	 */
	public void start();
	
	/**
	 * Method to force stop the event.
	 */
	public void stop();
	
	/**
	 * Check if the event has already started.
	 * @return true if the event has started, false if it hasn't.
	 */
	public boolean hasStarted();
	
	/**
	 * 
	 * @param p
	 * @return
	 */
	public static boolean isInEvent(Player p) {
		if(LMSCommand.lms != null && LMSCommand.lms.getAlive().contains(p.getName())) {
			return true;
		}
		if(p.hasMetadata(LMSCommand.lmsWaiting)) {
			return true;
		}
		if(p.hasMetadata(Juggernaut.inJuggernaut)) return true;
		if(JuggernautCommand.juggernaut != null && JuggernautCommand.juggernaut.getJuggernaut() != null
				&& JuggernautCommand.juggernaut.getJuggernaut().equalsIgnoreCase(p.getName())) return true;
		if(BracketsCommand.brackets != null && BracketsCommand.brackets.getPlayers().containsKey(p.getName())) {
			return true;	
		}
		if(SumoCommand.sumo != null && SumoCommand.sumo.getPlayers().containsKey(p.getName())) {
			return true;	
		}
		if(KOTHCommand.joined.contains(p.getUniqueId())) return true;
		if(KOTHCommand.koth != null && (KOTHCommand.koth.getTeam1().getMembers().contains(p.getUniqueId())
				|| KOTHCommand.koth.getTeam2().getMembers().contains(p.getUniqueId()))) {
			return true;
		}
		return false;
	}
	
	public static String getEventString(Player p) {
		if(LMSCommand.lms != null && LMSCommand.lms.getAlive().contains(p.getName())) {
			return "lms (alive)";
		}
		if(p.hasMetadata(LMSCommand.lmsWaiting)) {
			return "lms (waiting)";
		}
		if(p.hasMetadata(Juggernaut.inJuggernaut)) return "juggernaut (joined)";
		if(JuggernautCommand.juggernaut != null && JuggernautCommand.juggernaut.getJuggernaut() != null
				&& JuggernautCommand.juggernaut.getJuggernaut().equalsIgnoreCase(p.getName())) return "juggernaut (the juggernaut)";
		if(BracketsCommand.brackets != null && BracketsCommand.brackets.getPlayers().containsKey(p.getName())) {
			return "brackets";	
		}
		if(SumoCommand.sumo != null && SumoCommand.sumo.getPlayers().containsKey(p.getName())) {
			return "sumo";	
		}
		if(KOTHCommand.joined.contains(p.getUniqueId())) {
			return "koth (waiting)";
		}
		if(KOTHCommand.koth != null && (KOTHCommand.koth.getTeam1().getMembers().contains(p.getUniqueId())
				|| KOTHCommand.koth.getTeam2().getMembers().contains(p.getUniqueId()))) {
			return "koth";
		}
		return "not in event";
	}
	
	public static PvPEvent getEvent(Player p) {
		if(LMSCommand.lms != null && LMSCommand.lms.getAlive().contains(p.getName())) {
			return LMSCommand.lms;
		}
		if(p.hasMetadata(Juggernaut.inJuggernaut) && JuggernautCommand.juggernaut != null) {
			return JuggernautCommand.juggernaut;
		}
		if(JuggernautCommand.juggernaut != null && JuggernautCommand.juggernaut.getJuggernaut() != null
				&& JuggernautCommand.juggernaut.getJuggernaut().equalsIgnoreCase(p.getName())) {
			return JuggernautCommand.juggernaut;
		}
		if(BracketsCommand.brackets != null && BracketsCommand.brackets.getPlayers().containsKey(p.getName())) {
			return BracketsCommand.brackets;
		}
		if(SumoCommand.sumo != null && SumoCommand.sumo.getPlayers().containsKey(p.getName())) {
			return SumoCommand.sumo;	
		}
		if(KOTHCommand.joined.contains(p.getUniqueId())) {
			return KOTHCommand.koth;
		}
		if(KOTHCommand.koth != null && (KOTHCommand.koth.getTeam1().getMembers().contains(p.getUniqueId())
				|| KOTHCommand.koth.getTeam2().getMembers().contains(p.getUniqueId()))) {
			return KOTHCommand.koth;
		}
		return null;
	}
	
	public static List<PvPEvent> getCurrentPvPEvents() {
		List<PvPEvent> events = new ArrayList<PvPEvent>();
		if(LMSCommand.lms != null && LMSCommand.lms.hasStarted()) {
			events.add(LMSCommand.lms);
		}
		if(JuggernautCommand.juggernaut != null && JuggernautCommand.juggernaut.hasStarted()) {
			events.add(JuggernautCommand.juggernaut);
		}
		if(BracketsCommand.brackets != null && BracketsCommand.brackets.hasStarted()) {
			events.add(BracketsCommand.brackets);
		}
		if(KOTHCommand.koth != null && KOTHCommand.koth.hasStarted()) {
			events.add(KOTHCommand.koth);
		}
		if(SumoCommand.sumo != null && SumoCommand.sumo.hasStarted()) {
			events.add(SumoCommand.sumo);
		}
		return events;
	}
}
