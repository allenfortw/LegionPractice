package tech.hexadevelopment.practice.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EconomyManager {

	private Economy economy;

	public boolean setupEconomy() {
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		this.economy = ((Economy)rsp.getProvider());
		return this.economy != null;
	}
	
	public double getMoney(Player p) {
		if(isEnabled()) {
			return economy.getBalance(p, p.getWorld().getName());
		}
		return 0;
	}
	
	public boolean hasMoney(Player p, double amount) {
		if(isEnabled()) {
			return economy.has(p, p.getWorld().getName(), amount);
		}
		return false;
	}
	
	public boolean give(Player p, double amount) {
		if(isEnabled()) {
			EconomyResponse res = economy.depositPlayer(p, p.getWorld().getName(), amount);
			if(res.transactionSuccess()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean withdraw(Player p, double amount) {
		if(isEnabled()) {
			EconomyResponse res = economy.withdrawPlayer(p, p.getWorld().getName(), amount);
			if(res.transactionSuccess()) {
				return true;
			}
			p.sendMessage(res.errorMessage);
		}
		return false;
	}
	
	public boolean isEnabled() {
		return economy != null;
	}

	public Economy getEconomy() {
		return economy;
	}


}
