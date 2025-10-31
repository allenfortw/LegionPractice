package tech.hexadevelopment.practice.npc;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.npc.CitizensNPC.Difficulty;
import tech.hexadevelopment.practice.utils.ItemStackUtil;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.PlayerAnimation;
import tech.hexadevelopment.practice.LegionPractice;

public class CombatTask extends BukkitRunnable{

	List<UUID> players;
	Fight fight;
	Difficulty difficulty;
	private CitizensNPC npc;
	private boolean kit, navi, selfHealing;
	private LegionPractice plugin;
	private double attackRange, swingRangeModifier;
	private Player tar = null;

	public CombatTask(CitizensNPC npc, List<UUID> players, Fight fight, Difficulty difficulty) {
		this.fight = fight;
		this.npc = npc;
		this.players = players;
		this.difficulty = difficulty;
		int delay = 2;
		plugin = LegionPractice.getInstance();
		if(!npc.isSpawned()) {
			npc.getNPC().spawn(npc.spawnLocation);
		}
		attackRange = plugin.getConfig().getDouble("bot-attack-range");
		if(difficulty == Difficulty.EASY) {
			attackRange *= 0.8;
			swingRangeModifier = -0.5;
		}
		else if(difficulty == Difficulty.HARD) {
			attackRange *= 2;
			swingRangeModifier = 2;
		}
		else if(difficulty == Difficulty.HACKER) {
			attackRange *= 2.6;
			swingRangeModifier = 3;
			delay = 1;
		}
		runTaskTimer(LegionPractice.getInstance(), plugin.getConfig().getInt("countdown-time")*20, delay);
		if(fight.getKit() != null && fight.getKit().isStickSpawn()) {
			new BukkitRunnable() {

				int count = plugin.getConfig().getInt("countdown-time")*2+2;

				@Override
				public void run() {
					count--;
					if(count == 0) this.cancel();
					try {
						npc.teleport(npc.spawnLocation);
					}catch(Exception e){}
				}
			}.runTaskTimer(plugin, 10, 10);
		}
	}

	@Override
	public void run() {
		if(players == null || players.isEmpty() || npc.isDestroyed()) {
			this.cancel();
			return;
		}
		if(npc.isSpawned() && npc.getBukkitEntity() != null) {
			if(!kit) {
				npc.getNPC().setProtected(false);
				giveKit(fight.getKit());
			}
			if(!navi) {
				navi = true;
				if(difficulty == Difficulty.HARD) {
					npc.getNPC().getNavigator().getLocalParameters().speedModifier(1.33F);
				}
				else if(difficulty == Difficulty.HACKER) {
					npc.getNPC().getNavigator().getLocalParameters().speedModifier(1.66F);
				}
				npc.getNPC().getNavigator().getLocalParameters().attackRange(attackRange);
				//removes bot from teleporting to target locations
				npc.getNPC().getNavigator().getLocalParameters().stuckAction(new StuckAction() {

					@Override
					public boolean run(NPC arg0, Navigator arg1) {
						return false;
					}
				});
			}
			if(!npc.getBukkitEntity().isDead() && npc.getBukkitEntity().getLocation().getBlockY() < 0) {
				npc.getBukkitEntity().setHealth(0);
				return;
			}
			//for some reason even this doesn't fix falling always
			if(npc.getBukkitEntity().getVelocity().getY() < 0.1 && npc.getBukkitEntity().getVelocity().getY() > -0.0784) {
				Vector v = npc.getNPC().getEntity().getVelocity();
				npc.getNPC().getEntity().setVelocity(v.setY(-0.0784));
			}
			if(tar != null && plugin.getSpectatorHandler().isSpectator(tar)) tar = null;
			double distance = tar != null && tar.getWorld().getName().equals(npc.getBukkitEntity().getWorld().getName()) ? tar.getLocation().distanceSquared(npc.getBukkitEntity().getLocation()) : 150*150;
			if(npc.getNPC().getNavigator().getTargetAsLocation() == null || LegionPractice.random.nextInt(10) == 0) {
				for(UUID uuid : players) {
					Player pl = Bukkit.getPlayer(uuid);
					if(pl != null && !plugin.getSpectatorHandler().isSpectator(pl) && pl.getWorld().getName().equals(npc.getBukkitEntity().getWorld().getName())) {
						double dis = npc.getBukkitEntity().getLocation().distanceSquared(pl.getLocation());
						if(dis < distance) {
							tar = pl;
							distance = dis;
						}
					}
				}
			}
			if(tar != null && !selfHealing) {
				if(distance <= attackRange*attackRange*1.5 && LegionPractice.random.nextDouble() > 0.2) {
					npc.getNPC().getNavigator().setTarget((Entity)tar, true);
				}
				else {
					npc.getNPC().getNavigator().setTarget(tar.getLocation());
				}
				npc.getNPC().getNavigator().setPaused(false);
			}
			if(npc.getNPC().getNavigator().getTargetAsLocation() != null) {
				npc.getBukkitEntity().setSprinting(true);
			}
			double x = attackRange+swingRangeModifier+(LegionPractice.random.nextDouble()*3);
			if(distance < x*x && !npc.getNPC().getNavigator().isPaused() && !selfHealing) {
				npc.swingMainArm();
			}
			if(!npc.getBukkitEntity().isDead()) {
				selfHealIfNeeded();
			}
		}
	}

	public void giveKit(BattleKit kit) {
		new BukkitRunnable() {

			int counter = 5;

			@Override
			public void run() {
				if(npc.getBukkitEntity() != null) {
					counter--;
					if(counter == 0) this.cancel();
					npc.getBukkitEntity().addPotionEffects(kit.getPotions());
					npc.getBukkitEntity().getInventory().setContents(kit.getInv());
					npc.getBukkitEntity().getInventory().setHelmet(kit.getHelmet());
					npc.getBukkitEntity().getInventory().setChestplate(kit.getChestplate());
					npc.getBukkitEntity().getInventory().setLeggings(kit.getLeggings());
					npc.getBukkitEntity().getInventory().setBoots(kit.getBoots());
					if(kit.isCombo()) {
						npc.getBukkitEntity().setMaximumNoDamageTicks(BattleKit.comboHitDelay);
					}
					npc.getBukkitEntity().setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
					Fight.setCurrentFight(npc.getBukkitEntity(), fight, plugin);
					CombatTask.this.kit = true;
				}
			}
		}.runTaskTimer(LegionPractice.getInstance(), 0, 20);
	}


	public void selfHealIfNeeded() {
		if(selfHealing) return;
		Damageable d = npc.getBukkitEntity();
		if(d.getHealth() <= 13) {
			if(LegionPractice.random.nextBoolean()) {
				if(!splashHeal()) {
					if(!soup()) {
						gapple();
					}
				}
			}
		}
	}

	private boolean gapple() {
		ItemStack gapple = null;
		for(ItemStack is : npc.getBukkitEntity().getInventory().getContents()) {
			if(is != null && is.getType() == Material.GOLDEN_APPLE) {
				gapple = is.clone();
			}
		}
		if(gapple != null) {
			selfHealing = true;
			ItemStack finalGapple = gapple;
			ItemStack hand = null;
			for(int i = 0; i < 9; i++) {
				if(npc.getBukkitEntity().getInventory().getItem(i) != null &&
						npc.getBukkitEntity().getInventory().getItem(i).equals(gapple)) {
					hand = npc.getBukkitEntity().getInventory().getItem(i);
					npc.getBukkitEntity().getInventory().setHeldItemSlot(i);
					break;
				}
			}
			if(hand == null) {
				npc.getBukkitEntity().getInventory().setHeldItemSlot(1);
				ItemStackUtil.removeItems(npc.getBukkitEntity().getInventory(), finalGapple, 1);
				for(int i = 9; i < 36; i++) {
					if(npc.getBukkitEntity().getInventory().getItem(i) == null 
							|| npc.getBukkitEntity().getInventory().getItem(i).getType() == Material.AIR) {
						npc.getBukkitEntity().getInventory().setItem(i, npc.getBukkitEntity().getItemInHand());
						break;
					}
				}
				npc.getBukkitEntity().setItemInHand(gapple);
			}
			new BukkitRunnable() {

				@Override
				public void run() {
					npc.getBukkitEntity().setItemInHand(finalGapple);
					try {
						Class<?> clz = PlayerAnimation.class;
						clz.getField("START_USE_ITEM");
						try{
							PlayerAnimation.START_USE_ITEM.play(npc.getBukkitEntity());
						}catch(NoSuchFieldError e){}
						clz.getField("EAT_FOOD");
						try{
							PlayerAnimation.EAT_FOOD.play(npc.getBukkitEntity());
						}catch(NoSuchFieldError e){}
					}catch(Exception e) {}
					new BukkitRunnable() {

						@Override
						public void run() {
							if(npc.getBukkitEntity() != null) {
								npc.getNPC().getNavigator().setPaused(true);
								npc.getBukkitEntity().setItemInHand(new ItemStack(Material.AIR));
								finalGapple.setAmount(1);
								if(finalGapple.getDurability() == 0) {
									npc.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*2, 1));
									npc.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*60*2, 1));
								}
								else {
									npc.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*30, 4));
									npc.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*60*2, 1));
									npc.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20*60*5, 0));
									npc.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20*60*5, 0));
								}
								ItemStackUtil.removeItems(npc.getBukkitEntity().getInventory(), finalGapple, finalGapple.getAmount());
								new BukkitRunnable() {

									@Override
									public void run() {
										if(npc.getNPC() != null && npc.isSpawned() && npc.getNPC().getNavigator() != null) {
											npc.getNPC().getNavigator().setPaused(false);
											npc.getBukkitEntity().getInventory().setHeldItemSlot(0);
											selfHealing = false;
										}
									}
								}.runTaskLater(LegionPractice.getInstance(), LegionPractice.random.nextInt(4)+2);
							}
						}
					}.runTaskLater(LegionPractice.getInstance(), 35);
				}
			}.runTaskLater(LegionPractice.getInstance(), LegionPractice.random.nextInt(2)+1);
			return true;
		}
		return false;
	}

	private boolean soup() {
		ItemStack soup = null;
		for(ItemStack is : npc.getBukkitEntity().getInventory().getContents()) {
			if(is != null && is.getType() == Material.MUSHROOM_SOUP) {
				soup = is.clone();
			}
		}
		if(soup != null) {
			selfHealing = true;
			npc.getNPC().getNavigator().setPaused(true);
			ItemStack finalSoup = soup;
			ItemStack hand = null;
			for(int i = 0; i < 9; i++) {
				if(npc.getBukkitEntity().getInventory().getItem(i) != null &&
						npc.getBukkitEntity().getInventory().getItem(i).equals(soup)) {
					hand = npc.getBukkitEntity().getInventory().getItem(i);
					npc.getBukkitEntity().getInventory().setHeldItemSlot(i);
					break;
				}
			}
			if(hand == null) {
				npc.getBukkitEntity().getInventory().setHeldItemSlot(1);
				ItemStackUtil.removeItems(npc.getBukkitEntity().getInventory(), finalSoup, 1);
				for(int i = 9; i < 36; i++) {
					if(npc.getBukkitEntity().getInventory().getItem(i) == null 
							|| npc.getBukkitEntity().getInventory().getItem(i).getType() == Material.AIR) {
						npc.getBukkitEntity().getInventory().setItem(i, npc.getBukkitEntity().getItemInHand());
						break;
					}
				}
				npc.getBukkitEntity().setItemInHand(soup);
			}
			npc.getBukkitEntity().setItemInHand(finalSoup);
			new BukkitRunnable() {

				@Override
				public void run() {
					if(npc == null || npc.getBukkitEntity() == null || npc.getBukkitEntity().isDead() || finalSoup == null) return;
					ItemStackUtil.removeItems(npc.getBukkitEntity().getInventory(), finalSoup, finalSoup.getAmount());
					if(!npc.getBukkitEntity().isDead()) {
						Damageable d = npc.getBukkitEntity();
						d.setHealth(d.getHealth() < 13 ? (d.getHealth()+7) : 20);
						Class<?> clz = PlayerAnimation.class;
						try {
							clz.getField("START_USE_ITEM");
							try{
								PlayerAnimation.START_USE_ITEM.play(npc.getBukkitEntity());
							}catch(NoSuchFieldError e){}
						}
						catch (Exception ex) {}
						npc.getBukkitEntity().setItemInHand(new ItemStack(Material.BOWL));
						new BukkitRunnable() {

							@Override
							public void run() {
								if(npc.getNPC() != null && npc.isSpawned() && npc.getNPC().getNavigator() != null) {
									npc.getNPC().getNavigator().setPaused(false);
									ItemStack is = npc.getBukkitEntity().getItemInHand();
									if(is != null) {
										Item i = npc.getBukkitEntity().getWorld().dropItem(npc.getBukkitEntity().getEyeLocation(), is);
										npc.getBukkitEntity().setItemInHand(new ItemStack(Material.AIR));
										npc.getBukkitEntity().getInventory().setHeldItemSlot(0);
										selfHealing = false;
										Bukkit.getScheduler().scheduleSyncDelayedTask(LegionPractice.getInstance(), new Runnable() {
											public void run() {
												if(i != null) {
													i.remove();
												}
											}
										}, LegionPractice.getInstance().getConfig().getInt("remove-drops")*20);
									}
								}
							}
						}.runTaskLater(LegionPractice.getInstance(), LegionPractice.random.nextInt(1)+1);
					}
				}
			}.runTaskLater(LegionPractice.getInstance(), LegionPractice.random.nextInt(1)+1);
			return true;
		}
		return false;
	}

	private boolean splashHeal() {
		if(!npc.getBukkitEntity().isOnGround() && npc.getBukkitEntity().getLocation().getY()-npc.getBukkitEntity().getLocation().getBlockY() > 0.35) {
			if(LegionPractice.random.nextInt(3) == 0) return false;
		}
		ItemStack pot = null;
		for(ItemStack is : npc.getBukkitEntity().getInventory().getContents()) {
			if(is != null) {
				if((is.getType() == Material.POTION && (is.getDurability() == 16421 || is.getDurability() == 16453))
						|| (is.getDurability() == 438 && plugin.getNMSAccessProvider().laterThan1_8)) {
					pot = is.clone();
					break;
				}
			}
		}
		if(pot != null) {
			selfHealing = true;
			ItemStack finalPot = pot;
			ItemStack hand = null;
			for(int i = 0; i < 9; i++) {
				if(npc.getBukkitEntity().getInventory().getItem(i) != null &&
						npc.getBukkitEntity().getInventory().getItem(i).equals(pot)) {
					hand = npc.getBukkitEntity().getInventory().getItem(i);
					npc.getBukkitEntity().getInventory().setHeldItemSlot(i);
					break;
				}
			}
			if(hand == null) {
				npc.getBukkitEntity().getInventory().setHeldItemSlot(1);
				ItemStackUtil.removeItems(npc.getBukkitEntity().getInventory(), finalPot, 1);
				for(int i = 9; i < 36; i++) {
					if(npc.getBukkitEntity().getInventory().getItem(i) == null 
							|| npc.getBukkitEntity().getInventory().getItem(i).getType() == Material.AIR) {
						npc.getBukkitEntity().getInventory().setItem(i, npc.getBukkitEntity().getItemInHand());
						npc.getBukkitEntity().getInventory().setHeldItemSlot(1);
						break;
					}
				}
				npc.getBukkitEntity().setItemInHand(pot);
			}
			npc.getBukkitEntity().getInventory().setHeldItemSlot(LegionPractice.random.nextInt(8)+1);
			npc.getBukkitEntity().setItemInHand(finalPot);
			Location behind = npc.getBukkitEntity().getLocation().add(npc.getBukkitEntity().getLocation().getDirection().normalize().multiply(-5)).subtract(0, 10, 0);
			npc.getNPC().getNavigator().setTarget(behind);
			new BukkitRunnable() {

				int startCounter = LegionPractice.random.nextInt(5)+5;
				int counter = startCounter;

				@Override
				public void run() {
					if(npc.getNPC() != null && npc.isSpawned() && npc.getNPC().getNavigator() != null) {
						counter--;
						if(counter == 0 || Math.abs(npc.getBukkitEntity().getLocation().getPitch()-90) < 50) {
							this.cancel();
							npc.swingMainArm();
							ThrownPotion thrownPotion = throwPotion(finalPot);
							new BukkitRunnable() {

								@Override
								public void run() {
									if(selfHealing && thrownPotion != null && npc.getNPC() != null && npc.getNPC().isSpawned() && !npc.getBukkitEntity().isDead() && !thrownPotion.isDead()) {
										npc.getNPC().getNavigator().setTarget(thrownPotion.getLocation());
									}
									else this.cancel();
								}
							}.runTaskTimer(plugin, 1, 1);
							npc.getBukkitEntity().setItemInHand(new ItemStack(Material.AIR));
							ItemStackUtil.removeItems(npc.getBukkitEntity().getInventory(), finalPot, 1);
							Damageable d = npc.getBukkitEntity();
							if(d.getHealth() < 12) {
								ItemStack pot = null;
								for(ItemStack is : npc.getBukkitEntity().getInventory().getContents()) {
									if(is != null && (is.getType() == Material.POTION && (is.getDurability() == 16421
											|| is.getDurability() == 16453))) {
										pot = is.clone();
										break;
									}
								}
								if(pot != null) {
									npc.swingMainArm();
									throwPotion(pot);
									ItemStackUtil.removeItems(npc.getBukkitEntity().getInventory(), pot, 1);
								}
							}
							new BukkitRunnable() {

								@Override
								public void run() {
									if(npc.getNPC() != null && npc.isSpawned() && npc.getNPC().getNavigator() != null) {
										npc.getBukkitEntity().getInventory().setHeldItemSlot(0);
										selfHealing = false;
									}
								}
							}.runTaskLater(LegionPractice.getInstance(), LegionPractice.random.nextInt(12)+8);
						}
					}
					else this.cancel();
				}
			}.runTaskTimer(LegionPractice.getInstance(), 1, 1);
			return true;
		}
		return npc.isDestroyed();
	}

	private ThrownPotion throwPotion(ItemStack potion) {
		if(plugin.getConfig().getBoolean("bot-fast-potions")) {
			ThrownPotion thrownPotion = (ThrownPotion) npc.getBukkitEntity().getWorld().spawnEntity(npc.getBukkitEntity().getEyeLocation(), EntityType.SPLASH_POTION);
			thrownPotion.getEffects().addAll(Potion.fromItemStack(potion).getEffects());
			thrownPotion.setItem(potion);
			Vector vec = npc.getBukkitEntity().getLocation().getDirection();
			if(vec.getY() == 0) {
				vec.setY(-LegionPractice.random.nextInt(2)+1+(LegionPractice.random.nextDouble()/10));
			}
			thrownPotion.setVelocity(vec);
			return thrownPotion;
		}
		Potion pot = new Potion(PotionType.INSTANT_HEAL);
		pot.setSplash(true);
		pot.apply(potion);
		ThrownPotion thrownPotion = npc.getBukkitEntity().launchProjectile(ThrownPotion.class);
		thrownPotion.setItem(pot.toItemStack(1));
		thrownPotion.setVelocity(thrownPotion.getVelocity().setY(thrownPotion.getVelocity().getY()*(thrownPotion.getVelocity().getY() > 0 ? 0.6 : 1.4)));
		return thrownPotion;
	}
}
