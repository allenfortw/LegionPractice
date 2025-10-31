package tech.hexadevelopment.practice.utils;

import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;

public class SimpleLagMeter extends BukkitRunnable {

	private static long l;
	private static long delay;

	public SimpleLagMeter() {
		runTaskTimer(LegionPractice.getInstance(), 20, LegionPractice.performanceMode ? 100 : 20);
		l = System.currentTimeMillis();
	}

	@Override
	public void run() {
		delay = System.currentTimeMillis()-l;
		l = System.currentTimeMillis();
	}

	public static long getDelay() {
		if(LegionPractice.performanceMode) {
			//-10 just for the "margin of error"
			return (delay/5)-10;
		}
		return delay;
	}




}
