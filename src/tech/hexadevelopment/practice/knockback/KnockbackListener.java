package tech.hexadevelopment.practice.knockback;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;

public class KnockbackListener implements Listener {

	private LegionPractice plugin;

	public KnockbackListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onDamage(EntityDamageByEntityEvent e){
		if(e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
			Player entity = (Player)e.getEntity();
			if (entity.getNoDamageTicks() > entity.getMaximumNoDamageTicks() / 2.0D) return;
			BattleKit kit = BattleKit.getCurrentKit(entity);
			if(plugin.getKnockbackManager().isOnlyCombo() && (kit == null || !kit.isCombo())) return;
			KnockbackValues kb = plugin.getKnockbackManager().getKnockback(kit);
			double horizontalMultiplier = entity.getLocation().getY() % 1.0D == 0.0D ? kb.getHorizontalMultiplier() : kb.getAirHorizontalMultiplier();
			double verticalMultiplier = entity.getLocation().getY() % 1.0D == 0.0D ? kb.getVerticalMultiplier() : kb.getAirVerticalMultiplier();
			Player damager = (Player)e.getDamager();
			double sprintMultiplier = damager.isSprinting() ? 0.8D : 0.5D;
			double kbMultiplier = damager.getItemInHand() == null ? 0 : damager.getItemInHand().getEnchantmentLevel(Enchantment.KNOCKBACK) * 0.2D;
			double airMultiplier = entity.isOnGround() ? 1 : 0.5;
			Vector knockback = entity.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();
			knockback.setX((knockback.getX() * sprintMultiplier + kbMultiplier) * horizontalMultiplier);
			knockback.setY(0.35D * airMultiplier * verticalMultiplier);
			knockback.setZ((knockback.getZ() * sprintMultiplier + kbMultiplier) * horizontalMultiplier);
			plugin.getNMSAccessProvider().getAccess().sendKnockback(entity, knockback.getX(), knockback.getY(), knockback.getZ());
		}
	}

	@EventHandler
	public void onKnockback(PlayerVelocityEvent e){
		Player p = e.getPlayer();
		EntityDamageEvent lastDamageCause = p.getLastDamageCause();
		if(lastDamageCause == null || (!(lastDamageCause instanceof EntityDamageByEntityEvent))) return;
		EntityDamageByEntityEvent lastDamageCauseByEntity = (EntityDamageByEntityEvent)lastDamageCause;
		if(lastDamageCauseByEntity.getDamager() instanceof Player) {
			if(plugin.getKnockbackManager().isOnlyCombo()) {
				BattleKit kit = BattleKit.getCurrentKit(p);
				if(kit != null && kit.isCombo()) {
					e.setCancelled(true);
				}
			}
			else e.setCancelled(true);
		}
	}
}
