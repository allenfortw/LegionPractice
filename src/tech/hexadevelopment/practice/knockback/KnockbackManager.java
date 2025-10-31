package tech.hexadevelopment.practice.knockback;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashSet;

import org.bukkit.Bukkit;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.utils.VersionChecker;

public class KnockbackManager {

	private HashSet<KnockbackValues> modes = new HashSet<KnockbackValues>();

	private LegionPractice plugin;
	private KnockbackValues defaultValues;
	private KnockbackValues comboValues;
	private boolean onlyCombo;


	public KnockbackManager(LegionPractice plugin) {
		this.plugin = plugin;
		Bukkit.getLogger().info("LegionPractice >> Loading knockback:");
		modes.add(defaultValues = new KnockbackValues(plugin, "default"));
		modes.add(comboValues = new KnockbackValues(plugin, "combo"));
		onlyCombo = plugin.getConfig().getBoolean("knockback.only-combo");
		Bukkit.getLogger().info("only-combo: " + onlyCombo);
		Bukkit.getLogger().info("Loaded default profiles: default, combo");
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
			if(!str2.contains("user:" + plugin.arenaPvP.z() + ":kbperkit=true")) {
				return;
			}
		}catch(Exception e) {}
		for(BattleKit kit : plugin.kits) {
			if(plugin.getConfig().get("knockback." + kit.getName() + ".horizontal") != null) {
				if(!kit.getName().equals("default") && !kit.getName().equals("combo")) {
					modes.add(new KnockbackValues(plugin, kit.getName()));
					Bukkit.getLogger().info("Loaded kit knockback profile: " + kit.getName());
				}
			}
		}
		Bukkit.getLogger().info("LegionPractice >> Knockback loaded");
	}

	public KnockbackValues getKnockback(BattleKit kit) {
		if(kit != null) {
			for(KnockbackValues v : modes) {
				if(v.getIdentifier().equals(kit.getName())) {
					return v;
				}
			}
			return kit.isCombo() ? comboValues : defaultValues;
		}
		return defaultValues;
	}

	public KnockbackValues getKnockback(String s) {
		if(s != null) {
			for(KnockbackValues v : modes) {
				if(v.getIdentifier().equals(s)) {
					return v;
				}
			}
			return s.equals("combo") ? comboValues : defaultValues;
		}
		return defaultValues;
	}
	public void setOnlyCombo(boolean onlyCombo) {
		this.onlyCombo = onlyCombo;
	}

	public boolean isOnlyCombo() {
		return onlyCombo;
	}

	public void save() {
		for(KnockbackValues m : modes) {
			m.save();
		}
		plugin.getConfig().set("knockback.only-combo", onlyCombo);
		plugin.saveConfig();
	}

}
