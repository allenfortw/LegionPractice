package tech.hexadevelopment.practice.epearlcooldown;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;

public class PearlListener implements Listener {

	public static String COOLDOWN_META = "LegionPracticeEnderPearlCooldown";

	private LegionPractice plugin;


	public PearlListener(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		for(ItemStack it : e.getPlayer().getInventory().getContents()) {
			if(it != null && it.getType() == Material.ENDER_PEARL) {
				ItemMeta iMeta = it.getItemMeta();
				iMeta.setDisplayName(null);
				it.setItemMeta(iMeta);
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().setLevel(0);
		e.getPlayer().setExp(0);
	}

	@EventHandler
	public void onLaunch(PlayerInteractEvent e){
		Player p = e.getPlayer();
		if(p.hasMetadata(plugin.NO_DAMAGE) && e.getItem() != null && e.getItem().getType().equals(Material.ENDER_PEARL)) {
			e.setCancelled(true);
			return;
		}
		if(e.getItem() != null && e.getItem().getType() == Material.ENDER_PEARL) {
			if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(p.hasMetadata(COOLDOWN_META)) {
					MetadataValue m = plugin.getMetadata(p, COOLDOWN_META);
					if(m != null && m.value() != null) {
						double i = PearlManager.getCooldown()*1000;
						double l = m.asLong();
						double c = System.currentTimeMillis();
						if(l+i > c) {
							e.setCancelled(true);
							double x = (l+i-c)/1000;
							p.sendMessage(plugin.translateMessage(p, "cooldown-message").replace("<time>", PearlManager.getFormat().format(x)));
							return;
						}
					}
				}
				p.setMetadata(COOLDOWN_META, new FixedMetadataValue(plugin, System.currentTimeMillis()));
				new PearlTask(p);
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPickup(PlayerPickupItemEvent e) {
		updateItem(e.getItem().getItemStack(), e.getPlayer());
	}

	@EventHandler
	public void onItemHeld(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		ItemStack item = p.getInventory().getItem(e.getNewSlot());
		if(item != null && item.getType() == Material.ENDER_PEARL) {
			updateItem(item, p);
		}
	}

	private void updateItem(ItemStack item, Player p) {
		if(item.getType() == Material.ENDER_PEARL) {
			if(p.hasMetadata(COOLDOWN_META)) {
				MetadataValue m = plugin.getMetadata(p, COOLDOWN_META);
				if(m != null && m.value() != null) {
					double i = PearlManager.getCooldown()*1000;
					double l = m.asLong();
					double c = System.currentTimeMillis();
					if(l+i > c) {
						double x = (l+i-c)/1000;
						item.setItemMeta(getPearl(item, PearlManager.getFormat().format(x)));
						return;
					}
				}
			}
			ItemMeta im = item.getItemMeta();
			im.setDisplayName(null);
			item.setItemMeta(im);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onClick(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		if(item != null && item.getType() == Material.ENDER_PEARL) {
			ItemMeta iMeta = item.getItemMeta();
			iMeta.setDisplayName(null);
			item.setItemMeta(iMeta);
		}
	}

	private ItemMeta getPearl(ItemStack item, String l) {
		String s = ChatColor.translateAlternateColorCodes('&', PearlManager.getPearlName());
		ItemMeta iMeta = item.getItemMeta();
		iMeta.setDisplayName(s.replace("<time>", l));
		return iMeta;
	}

	private class PearlTask extends BukkitRunnable{

		private Player p;
		private int counter;

		public PearlTask(Player p) {
			this.p = p;
			counter = PearlManager.getInterval();
			if(LegionPractice.ASYNC_EVERYTHING) {
				runTaskTimerAsynchronously(plugin, 0, 1);
			}
			else {
				runTaskTimer(plugin, 0, 1);
			}
		}

		@Override
		public void run() {
			if(p != null) {
				long left = left(p);
				p.setLevel((int) (left/1000));
				p.setExp((float) (left/(PearlManager.getCooldown()*1000)));
				counter--;
				if(counter == 0) {
					counter = PearlManager.getInterval();
					if(p.getItemInHand().getType() == Material.ENDER_PEARL) {
						if(p.hasMetadata(COOLDOWN_META)) {
							MetadataValue m = plugin.getMetadata(p, COOLDOWN_META);
							if(m != null && m.value() != null) {
								if(left > 0) {
									double x = left/1000;
									String format = PearlManager.getFormat().format(x);
									for(ItemStack it : p.getInventory().getContents()) {
										if(it != null && it.getType() == Material.ENDER_PEARL) {
											it.setItemMeta(getPearl(it, format));
										}
									}
								}
								else {
									p.removeMetadata(PearlListener.COOLDOWN_META, plugin);
									for(ItemStack it : p.getInventory().getContents()) {
										if(it != null && it.getType() == Material.ENDER_PEARL) {
											ItemMeta iMeta = it.getItemMeta();
											iMeta.setDisplayName(null);
											it.setItemMeta(iMeta);
										}
									}
									cancel();
								}
							}
							else {
								for(ItemStack it : p.getInventory().getContents()) {
									if(it != null && it.getType() == Material.ENDER_PEARL) {
										ItemMeta iMeta = it.getItemMeta();
										iMeta.setDisplayName(null);
										it.setItemMeta(iMeta);
									}
								}
								cancel();
							}
						}
						else cancel();
					}
				}
			}
			else cancel();
		}

	}

	private long left(Player p) {
		if(p.hasMetadata(COOLDOWN_META)) {
			MetadataValue m = plugin.getMetadata(p, COOLDOWN_META);
			if(m != null && m.value() != null) {
				double i = PearlManager.getCooldown()*1000;
				double l = m.asLong();
				long c = System.currentTimeMillis();
				if(l+i > c) {
					return (long) (l+i-c);
				}
			}
		}
		return 0;
	}
}
