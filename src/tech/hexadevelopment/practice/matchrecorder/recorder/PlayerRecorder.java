package tech.hexadevelopment.practice.matchrecorder.recorder;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.matchrecorder.RecordedPlayer;
import tech.hexadevelopment.practice.utils.LocationUtil;
import tech.hexadevelopment.practice.utils.SimpleItem;

public class PlayerRecorder extends BukkitRunnable {


	public static boolean recordVelocities;

	private UUID uuid;
	private String name;
	RecordedPlayer record;
	int tickCounter, maxTime;
	Location relativeLocation;

	public PlayerRecorder(Player recording, Location relativeLocation) {
		this.uuid = recording.getUniqueId();
		this.name = recording.getName();
		this.relativeLocation = relativeLocation.clone();
		maxTime = LegionPractice.getInstance().getConfig().getInt("max-recording-time")*20;
		this.record = new RecordedPlayer(uuid, name);
		PlayerRecorderListener.recording.put(uuid, this);
		runTaskTimer(LegionPractice.getInstance(), 0, 1);
	}

	public RecordedPlayer stopRecording() {
		cancel();
		PlayerRecorderListener.recording.remove(uuid);
		return record;
	}

	@Override
	public void run() {
		try{
			Player recording = Bukkit.getPlayer(uuid);
			tickCounter++;
			if(recording == null || recording.isDead()) {
				if(record.getDeath() == 0) {
					record.setDeath(tickCounter);
				}
			}
			else if(relativeLocation.getWorld() != null && relativeLocation.getChunk().isLoaded() && recording.getLocation().getChunk().isLoaded()){
				record.getLocations().add(LocationUtil.subtractIgnoreWorld(recording.getLocation(), relativeLocation));
				if(recordVelocities) {
					record.getVelocities().add(recording.getVelocity());
				}
				SimpleItem hand = new SimpleItem(recording.getItemInHand());
				if(!getLastItem(record.getItemsInHand()).isSimilar(hand)) {
					record.getItemsInHand().put(tickCounter, hand);
				}
				SimpleItem helmet = new SimpleItem(recording.getInventory().getHelmet());
				if(!getLastItem(record.getHelmets()).isSimilar(helmet) || tickCounter == 0) {
					record.getHelmets().put(tickCounter, helmet);
				}
				SimpleItem chestplate = new SimpleItem(recording.getInventory().getChestplate());
				if(!getLastItem(record.getChestplates()).isSimilar(chestplate) || tickCounter == 0) {
					record.getChestplates().put(tickCounter, chestplate);
				}
				SimpleItem leggings = new SimpleItem(recording.getInventory().getLeggings());
				if(!getLastItem(record.getLeggings()).isSimilar(leggings) || tickCounter == 0) {
					record.getLeggings().put(tickCounter, leggings);
				}
				SimpleItem boots = new SimpleItem(recording.getInventory().getBoots());
				if(!getLastItem(record.getBoots()).isSimilar(boots) || tickCounter == 0) {
					record.getBoots().put(tickCounter, boots);
				}
				boolean lastSneak = getLastBoolean(record.getSneaks(), tickCounter);
				if((recording.isSneaking() && !lastSneak) || (!recording.isSneaking() && lastSneak)) {
					record.getSneaks().put(tickCounter, recording.isSneaking());
				}
				boolean lastSprint = getLastBoolean(record.getSprints(), tickCounter);
				if((recording.isSprinting() && !lastSprint) || (!recording.isSprinting() && lastSprint)) {
					record.getSprints().put(tickCounter, recording.isSprinting());
				}
			}
			if(tickCounter > maxTime) {
				stopRecording();
			}
		}catch(Exception e) {
			stopRecording();
			Bukkit.getLogger().warning("Player recording was stopped because an error occurred!");
			e.printStackTrace();
		}
	}

	private boolean getLastBoolean(HashMap<Integer, Boolean> booleans, int tick) {
		if(!booleans.isEmpty()) {
			for(int i = tick; i > 0; i--) {
				if(booleans.containsKey(i)) {
					return booleans.get(i);
				}
			}
		}
		return false;
	}

	Integer getLastSwung() {
		if(!record.getSwung().isEmpty()) {
			return record.getSwung().get(record.getSwung().size()-1);
		}
		return 0;
	}

	private SimpleItem getLastItem(HashMap<Integer, SimpleItem> hashMap) {
		int counter = 0;
		for(Integer is : hashMap.keySet()) {
			counter++;
			if(counter == hashMap.keySet().size()) {
				return hashMap.get(is);
			}
		}
		return new SimpleItem(Material.AIR, (short) 0);
	}
}