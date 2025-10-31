package tech.hexadevelopment.practice.fights.duel;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyFFA;
import tech.hexadevelopment.practice.fights.party.partyfights.PartySplit;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsBots;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.LegionPracticeAPI;

public class BedWars implements Listener {


	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if(e.getBlock() != null && e.getBlock().getType() == Material.BED_BLOCK) {
			Player p = e.getPlayer();
			Fight fight = LegionPracticeAPI.getFight(p);
			if(fight != null && fight.getKit().isBedwars()) {
				e.setCancelled(true);
				if(fight instanceof PartyFFA) {
					LegionPracticeAPI.forceWin(p);
				}
				else {
					Location own = getOwnSpawn(p, fight);
					Location bed = e.getBlock().getLocation();
					Location opponent = fight.getArena().getLoc1();
					if(own == fight.getArena().getLoc1()) {
						opponent = fight.getArena().getLoc2();
					}
					if(bed.distanceSquared(own) > bed.distanceSquared(opponent)) {
						LegionPracticeAPI.forceWin(p);
					}
				}
			}
		}
	}


	@EventHandler(ignoreCancelled=true)
	public void onDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Damageable)) return;
		double d = ((Damageable) e.getEntity()).getHealth();
		if(e.getFinalDamage() >= d && e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Fight fight = LegionPracticeAPI.getFight(p);
			if(fight != null && !fight.hasEnded() && fight.getKit() != null) {
				if(fight.getKit().isBedwars()) {
					respawn(p);
					e.setCancelled(true);
				}
			}
		}
	}

	public static void respawn(Player p) {
		respawn(p, Fight.getCurrentFight(p, LegionPractice.getInstance()));
	}

	public static void respawn(Player p, Fight fight) {
		if(LegionPractice.getInstance().getSpectatorHandler().isSpectator(p)) return;
		LegionPractice.getInstance().clear(p, false, false);
		LegionPractice.getInstance().getSpectatorHandler().addSpectator(p);
		p.setVelocity(new Vector(0, 2, 0));
		p.setFlying(true);
		if(fight != null && fight.getKit() != null) {
			new BukkitRunnable() {

				@Override
				public void run() {
					if(fight.hasEnded()) {
						LegionPractice.getInstance().getSpectatorHandler().removeSpectator(p, false);
						LegionPractice.getInstance().clear(p, true, true);
					}
					else {
						LegionPractice.getInstance().getSpectatorHandler().removeSpectator(p, false);
						LegionPractice.getInstance().clear(p, false, false);
						p.setFlying(false);
						p.setFallDistance(0);
						Location to = getOwnSpawn(p, fight);
						Block b = to.clone().subtract(0, 1, 0).getBlock();
						if(b.getType() != null && b.getType().isSolid()) {
							to.add(0, 1, 0);
						}
						for(int i = 0; i < 3; i++) {
							if(to.getBlock().getType().isSolid()) {
								to.add(0, 1, 0);
							}
						}
						p.teleport(to);
						fight.getKit().giveKit(p);
						new BukkitRunnable() {

							@Override
							public void run() {
								if(p != null) {
									p.updateInventory();
								}
							}
						}.runTaskLater(LegionPractice.getInstance(), 10);
					}
				}
			}.runTaskLater(LegionPractice.getInstance(), 20*3);
		}
	}

	public static Location getOwnSpawn(Player p, Fight fight) {
		if(fight instanceof Duel) {
			Duel duel = (Duel) fight;
			if(duel.getP1().equals(p.getName())) {
				return fight.getArena().getLoc1();
			}
			return fight.getArena().getLoc2();
		}
		else if(fight instanceof PartySplit) {
			PartySplit split = (PartySplit) fight;
			if(split.getAlive1().contains(p.getName())) {
				return fight.getArena().getLoc1();
			}
			return fight.getArena().getLoc2();
		}
		else if(fight instanceof PartyVsParty) {
			PartyVsParty pvp = (PartyVsParty) fight;
			if(pvp.getPartyAlive1().contains(p.getName())) {
				return fight.getArena().getLoc1();
			}
			return fight.getArena().getLoc2();
		}
		else if(Bukkit.getPlayer(p.getUniqueId()) == null) {
			return fight.getArena().getLoc2();
		}
		else if(fight instanceof BotDuel) {
			return fight.getArena().getLoc1();
		}
		else if(fight instanceof PartyVsBots) {
			return fight.getArena().getLoc1();
		}
		if(LegionPractice.random.nextBoolean()) return fight.getArena().getLoc1();
		return fight.getArena().getLoc2();
	}
}
