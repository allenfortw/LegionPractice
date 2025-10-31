package tech.hexadevelopment.practice.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.LegionPractice;

public class BowHealth implements Listener{

	private boolean allKits;
	private LegionPractice plugin;
	private List<String> kits = new ArrayList<String>();

	public BowHealth(LegionPractice plugin) {
		this.plugin = plugin;
		String s = plugin.getConfig().getString("bow-health-kits").replace(" ", "");
		for(String k : s.split(",")) {
			if(k.equals("*")) {
				allKits = true;
				kits.clear();
				return;
			}
			kits.add(k.toLowerCase());
		}
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void DamageEvent(EntityDamageByEntityEvent e){
		if(e.getDamager() instanceof Arrow){
			Arrow a = (Arrow)e.getDamager();
			if(a.getShooter() instanceof Player){
				a.getShooter();
				Player p = (Player)a.getShooter();
				Damageable dp = (Damageable)e.getEntity();
				if(dp instanceof Player){
					Player v = (Player)dp;
					double ptviev = dp.getHealth();
					Integer damage = Integer.valueOf((int)e.getFinalDamage());
					Integer realHealth = Integer.valueOf((int)(ptviev - damage.intValue()));
					if(realHealth.intValue() > 0){
						if (p.getPlayer() != v.getPlayer()) {
							if(checkKit(p)) {
								p.sendMessage(plugin.translateMessage(p, "bow-health-message")
										.replace("<player>", v.getName()).replace("<hp>", realHealth.intValue() + "").replace("<hearts>", (realHealth.intValue()/2) + ""));
							}
						}
					}
				}
			}
		}
	}

	private boolean checkKit(Player p) {
		if(allKits) return true;
		BattleKit kit = BattleKit.getCurrentKit(p);
		return kit != null && (kits.contains(kit.getName()) || (kit.getMergedEditor() != null && kits.contains(kit.getMergedEditor())));	
	}
}