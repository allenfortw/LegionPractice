package tech.hexadevelopment.practice.fights.requests;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.duel.Duel;

public class DuelRequest implements Request{
	
	private long startTime;
	private static int seconds = 15;
	private String dueler;
	private String dueled;
	private Duel duel;
	
	
	public DuelRequest(Player dueler, Player dueled, Duel duel) {
		this.startTime = System.currentTimeMillis();
		this.dueler = dueler.getName();
		this.dueled = dueled.getName();
		this.duel = duel;
	}
	
	@Override
	public boolean hasExpired() {
		return startTime/1000+seconds < System.currentTimeMillis()/1000;
	}
	
	@Override
	public Fight getFight() {
		return duel;
	}
	
	public String getDueled() {
		return dueled;
	}
	
	public String getDueler() {
		return dueler;
	}
	
	public Player getPlayerDueled() {
		return Bukkit.getPlayer(getDueled());
	}
	
	public Player getPlayerDueler() {
		return Bukkit.getPlayer(getDueler());
	}
}
