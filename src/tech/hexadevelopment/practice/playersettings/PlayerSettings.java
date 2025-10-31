package tech.hexadevelopment.practice.playersettings;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.events.LanguageSelectEvent;
import tech.hexadevelopment.practice.language.LanguageManager;
import tech.hexadevelopment.practice.playerdata.PlayerDataFile;

public class PlayerSettings {


	private static HashMap<UUID, PlayerSettings> settings = new HashMap<UUID, PlayerSettings>();

	private UUID uuid;
	private String language;
	private boolean scoreboardDisabled, duelRequestsDisabled, adminScoreboard, hideOtherPlayers;

	public PlayerSettings(UUID uuid) {
		this.uuid = uuid;
		settings.put(uuid, this);
		load();
	}

	private void load() {
		PlayerDataFile f = new PlayerDataFile(LegionPractice.getInstance(), uuid + "");
		YamlConfiguration conf = f.getConfig();
		setLanguage(conf.getString("language"), LegionPractice.getInstance(), false);
		scoreboardDisabled = conf.getBoolean("scoreboard-disabled");
		duelRequestsDisabled = conf.getBoolean("duel-requests-disabled");
		adminScoreboard = conf.getBoolean("admin-scoreboard");
		hideOtherPlayers = conf.getBoolean("hide-other-players");
	}

	public void save() {
		PlayerDataFile f = new PlayerDataFile(LegionPractice.getInstance(), uuid + "");
		YamlConfiguration conf = f.getConfig();
		conf.set("language", language);
		conf.set("scoreboard-disabled", scoreboardDisabled);
		conf.set("duel-requests-disabled", duelRequestsDisabled);
		if(adminScoreboard || conf.getBoolean("admin-scoreboard")){
			conf.set("admin-scoreboard", adminScoreboard);
		}
		conf.set("hide-other-players", hideOtherPlayers);
		f.save();
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language, LegionPractice plugin, boolean message) {
		Player p = Bukkit.getPlayer(uuid);
		if(language == null || language.equals("null")) language = LanguageManager.getDefaultLanguage(p);
		String lang = language;
		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				LanguageSelectEvent event = new LanguageSelectEvent(p, lang);
				Bukkit.getPluginManager().callEvent(event);
				if(!event.isCancelled()) {
					PlayerSettings.this.language = event.getLanguage();
					if(message) {
						if(p != null) {
							p.sendMessage(plugin.translateMessage(p, "language-set").replace("<language>", lang));
						}
					}
				}
			}
		});
	}

	/**
	 * @return the scoreboardDisabled
	 */
	public boolean isScoreboardDisabled() {
		return scoreboardDisabled;
	}

	/**
	 * @param scoreboard the scoreboardDisabled to set
	 */
	public void setScoreboardDisabled(boolean scoreboardDisabled) {
		this.scoreboardDisabled = scoreboardDisabled;
	}

	/**
	 * @return the duelRequestsDisabled
	 */
	public boolean isDuelRequestsDisabled() {
		return duelRequestsDisabled;
	}
	
	/**
	 * @return the hideOtherPlayers
	 */
	public boolean isHideOtherPlayers() {
		return hideOtherPlayers;
	}

	/**
	 * @param hideOtherPlayers the hideOtherPlayers to set
	 */
	public void setHideOtherPlayers(boolean hideOtherPlayers) {
		this.hideOtherPlayers = hideOtherPlayers;
	}

	/**
	 * @param duelRequests the duelRequests to set
	 */
	public void setDuelRequestsDisabled(boolean duelRequestsDisabled) {
		this.duelRequestsDisabled = duelRequestsDisabled;
	}

	/**
	 * @return the settings
	 */
	public static HashMap<UUID, PlayerSettings> getSettings() {
		return settings;
	}

	/**
	 * @return the adminScoreboard
	 */
	public boolean isAdminScoreboard() {
		return adminScoreboard;
	}

	/**
	 * @param adminScoreboard the adminScoreboard to set
	 */
	public void setAdminScoreboard(boolean adminScoreboard) {
		this.adminScoreboard = adminScoreboard;
	}

	public UUID getUUID() {
		return uuid;
	}

	public static PlayerSettings getPlayerSettings(UUID uuid) {
		if(settings.containsKey(uuid)) {
			return settings.get(uuid);
		}
		return new PlayerSettings(uuid);
	}

	public static PlayerSettings getPlayerSettings(Player p) {
		return getPlayerSettings(p.getUniqueId());
	}
}
