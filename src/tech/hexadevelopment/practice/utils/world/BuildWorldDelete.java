package tech.hexadevelopment.practice.utils.world;

import java.io.File;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.LegionPractice;

public class BuildWorldDelete implements Listener {

	public static HashSet<String> worldsCreated = new HashSet<String>();
	private int counter;

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		counter++;
		if(counter % 10 == 0) {
			new BukkitRunnable() {

				@Override
				public void run() {
					for(World w : Bukkit.getWorlds()) {
						int counter = 0;
						HashSet<Arena> allArs = Arena.getArenasInWorld(w, true);
						for(Arena ar : allArs) {
							if(ar.needsRollback()) {
								counter++;
							}
						}
						if(counter == allArs.size() || worldsCreated.size() > 5) {
							String name = w.getName();
							if(!name.equals(Bukkit.getWorlds().get(0).getName())) {
								if(worldsCreated.contains(name)) {
									if(w.getPlayers().size() == 0) {
										for(Arena ar : Arena.getArenasInWorld(w, true)) {
											if(ar.needsRollback()) {
												ar.removeFromLegionPractice();
											}
										}
										try {
											if(Bukkit.unloadWorld(w, false)) {
												File f = new File(Bukkit.getWorldContainer().getAbsolutePath(), name);
												LegionPractice.getInstance().getWorldStacker().deleteWorld(f);
												worldsCreated.remove(name);
											}
										}catch(Exception e) {
											new BukkitRunnable() {

												@Override
												public void run() {
													if(Bukkit.unloadWorld(w, false)) {
														new BukkitRunnable() {

															@Override
															public void run() {
																File f = new File(Bukkit.getWorldContainer().getAbsolutePath(), name);
																LegionPractice.getInstance().getWorldStacker().deleteWorld(f);
																worldsCreated.remove(name);
															}
														}.runTaskAsynchronously(LegionPractice.getInstance());
													}
												}
											}.runTask(LegionPractice.getInstance());
										}
									}
								}
							}
						}
					}
				}
			}.runTaskAsynchronously(LegionPractice.getInstance());
		}
	}
}
