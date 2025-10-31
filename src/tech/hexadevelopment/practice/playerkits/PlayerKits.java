package tech.hexadevelopment.practice.playerkits;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.playerdata.PlayerDataFile;

public class PlayerKits {

	private List<BattleKit> editedKits = new ArrayList<BattleKit>();
	private BattleKit customKit;
	private UUID player;
	
	public PlayerKits(UUID player) {
		this.player = player;	
	}
	
	public BattleKit getEditedKit(BattleKit kit, boolean onlyOwn) {
		for(BattleKit bk : editedKits) {
			if(kit.getName().equals(bk.getName())) {
				return bk;
			}
		}
		return onlyOwn ? null : kit;
	}
	
	public void savePlayerKitsToFile() {
		LegionPractice plugin = LegionPractice.getInstance();
		PlayerDataFile f = new PlayerDataFile(plugin, player + "");
		YamlConfiguration conf = f.getConfig();
		BattleKit kit = getCustomKit();
		if(kit != null) conf.set("custom-kit", kit);
		conf.set("kits", editedKits);
		f.save();
	}

	public boolean removeEditedKit(String serverKit) {
		BattleKit ownKit = null;
		for(BattleKit bk : editedKits) {
			if(serverKit.equals(bk.getName())) {
				ownKit = bk;
			}
		}
		if(ownKit != null) {
			editedKits.remove(ownKit);
			return true;
		}
		return false;
	}
	
	/**
	 * @return the editedKits
	 */
	public List<BattleKit> getEditedKits() {
		return editedKits;
	}

	/**
	 * @param editedKits the editedKits to set
	 */
	public void setEditedKits(List<BattleKit> editedKits) {
		this.editedKits = editedKits;
	}

	/**
	 * @return the customKit
	 */
	public BattleKit getCustomKit() {
		return customKit;
	}

	/**
	 * @param customKit the customKit to set
	 */
	public void setCustomKit(BattleKit customKit) {
		this.customKit = customKit;
	}

	/**
	 * @return the player
	 */
	public UUID getPlayer() {
		return player;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(UUID player) {
		this.player = player;
	}
}
