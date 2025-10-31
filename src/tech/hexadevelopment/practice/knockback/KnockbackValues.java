package tech.hexadevelopment.practice.knockback;

import tech.hexadevelopment.practice.LegionPractice;

public class KnockbackValues {

	private LegionPractice plugin;
	private String identifier;
	private double airHorMultiplier;
	private double airVertMultiplier;
	private double horMultiplier;
	private double vertMultiplier;
	private double airHorMultiplierMax;
	private double airVertMultiplierMax;
	private double horMultiplierMax;
	private double vertMultiplierMax;
	
	
	public KnockbackValues(LegionPractice plugin, String identifier) {
		this.identifier = identifier;
		this.plugin = plugin;
		airHorMultiplier = plugin.getConfig().getDouble("knockback." + identifier + ".air-horizontal");
		airVertMultiplier = plugin.getConfig().getDouble("knockback." + identifier + ".air-vertical");
		horMultiplier = plugin.getConfig().getDouble("knockback." + identifier + ".horizontal");
		vertMultiplier = plugin.getConfig().getDouble("knockback." + identifier + ".vertical");
		airHorMultiplierMax = plugin.getConfig().getDouble("knockback." + identifier + ".air-horizontal-max");
		airVertMultiplierMax = plugin.getConfig().getDouble("knockback." + identifier + ".air-vertical-max");
		horMultiplierMax = plugin.getConfig().getDouble("knockback." + identifier + ".horizontal-max");
		vertMultiplierMax = plugin.getConfig().getDouble("knockback." + identifier + ".vertical-max");
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public double getAirHorizontalMultiplier() {
		return airHorMultiplierMax == 0 ? airHorMultiplier : random(airHorMultiplier, airHorMultiplierMax);
	}

	public double getAirVerticalMultiplier() {
		return airVertMultiplierMax == 0 ? airVertMultiplier : random(airVertMultiplier, airVertMultiplierMax);
	}
	
	public double getHorizontalMultiplier() {
		return horMultiplierMax == 0 ? horMultiplier : random(horMultiplier, horMultiplierMax);
	}

	public double getVerticalMultiplier() {
		return vertMultiplierMax == 0 ? vertMultiplier : random(vertMultiplier, vertMultiplierMax);
	}
	
	public void setAirHorizontalMultiplier(double airHorMultiplier) {
		this.airHorMultiplier = airHorMultiplier;
	}

	public void setAirVerticalMultiplier(double airVertMultiplier) {
		this.airVertMultiplier = airVertMultiplier;
	}

	public void setHorizontalMultiplier(double horMultiplier) {
		this.horMultiplier = horMultiplier;
	}

	public void setVerticalMultiplier(double vertMultiplier) {
		this.vertMultiplier = vertMultiplier;
	}
	
	public void save() {
		plugin.getConfig().set("knockback." + identifier + ".air-horizontal", airHorMultiplier);
		plugin.getConfig().set("knockback." + identifier + ".air-vertical", airVertMultiplier);
		plugin.getConfig().set("knockback." + identifier + ".horizontal", horMultiplier);
		plugin.getConfig().set("knockback." + identifier + ".vertical", vertMultiplier);
		plugin.saveConfig();
	}
	
	private double random(double d1, double d2) {
		return d1 + LegionPractice.random.nextDouble()*(d2-d1);
	}
}
