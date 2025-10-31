package tech.hexadevelopment.practice.misc;

import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import tech.hexadevelopment.practice.LegionPractice;

public class SoupListener implements Listener {
	

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent e){
		if(e.getItem() != null && e.getItem().getType() == Material.MUSHROOM_SOUP) {
			if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK
					|| secondHand(e)){
				e.setCancelled(true);
				Player p = e.getPlayer();
				Damageable d = e.getPlayer();
				if (d.getHealth() < d.getMaxHealth() - 7){
					d.setHealth(d.getHealth() + 7);
					p.setItemInHand(new ItemStack(Material.BOWL));
				}
				else if (d.getHealth() < d.getMaxHealth()){
					d.setHealth(d.getMaxHealth());
					p.setItemInHand(new ItemStack(Material.BOWL));
				}
				else if (p.getFoodLevel() < 13){
					p.setFoodLevel(p.getFoodLevel() + 7);
					p.setItemInHand(new ItemStack(Material.BOWL));
					if (p.getSaturation() < 13) {
						p.setSaturation(p.getSaturation() + 7);
					}
					else {
						p.setSaturation(20);
					}
				}
				else if (p.getFoodLevel() < 20){
					p.setFoodLevel(20);
					p.setItemInHand(new ItemStack(Material.BOWL));
					if (p.getSaturation() < 13) {
						p.setSaturation(p.getSaturation() + 7);
					}
					else {
						p.setSaturation(20);
					}
				}
			}
		}
	}

	private boolean secondHand(PlayerInteractEvent e) {
		if(LegionPractice.getInstance().getNMSAccessProvider().laterThan1_8
				&& (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) {
			try {
				Method method = e.getClass().getMethod("getHand");
				Object result = method.invoke(e);
				return result.toString().equals("OFF_HAND");
			}catch(Exception ex) {}
		}
		return false;
	}
}