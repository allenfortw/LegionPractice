package tech.hexadevelopment.practice.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bukkit.Bukkit;
import org.bukkit.Server;

public class TPSUtil {

	private static Object minecraftServer;
	private static Field recentTps;

	public static double get1MinTPS() {
		return getRecentTPS()[0];
	}
	
	public static double get1MinTPSRounded() {
		return round(getRecentTPS()[0], 1);
	}
	
	public static double get1MinTPSRounded(int places) {
		return round(getRecentTPS()[0], places);
	}
	
	public static double[] getRecentTPS() {
		try {
			if (minecraftServer == null) {
				Server server = Bukkit.getServer();
				Field consoleField = server.getClass().getDeclaredField("console");
				consoleField.setAccessible(true);
				minecraftServer = consoleField.get(server);
			}
			if (recentTps == null) {
				recentTps = minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps");
				recentTps.setAccessible(true);
			}
			return (double[]) recentTps.get(minecraftServer);
		} catch (IllegalAccessException | NoSuchFieldException ignored) {
		}
		return new double[] {20, 20, 20};
	}
	
	private static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}
