package net.crytec.pickmoney;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import net.crytec.acf.PaperCommandManager;
import net.crytec.phoenix.api.Phoenix;
import net.crytec.pickmoney.Metrics.SingleLineChart;
import net.crytec.pickmoney.api.DropManager;
import net.crytec.pickmoney.commands.PickupMoneyCommand;
import net.crytec.pickmoney.listener.EntityDeathListener;
import net.crytec.pickmoney.listener.MoneyPickupListener;
import net.milkbowl.vault.economy.Economy;

public class PickupMoney extends JavaPlugin {
	
	@Getter
	private static DropManager Api;
	@Getter
	private Economy economy;
	
	
	/*
	 * Todo:
	 * 	- Fishing rewards
	 *  - Mob kills in a row
	 *  - Killed by player / sword condition
	 */
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		
		getDataFolder().mkdirs();
		
		if (!Phoenix.getAPI().requireAPIVersion(this, 500)) {
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		File file = new File(this.getDataFolder(), "config.yml");
		if (!file.exists()) {
			this.saveResource("config.yml", true);
		}
		
		this.initConfig();
		
		if (!this.setupEconomy()) {
			Bukkit.getPluginManager().disablePlugin(this);
			this.getLogger().severe("Failed to initialize PickupMoney - No compatible Economy plugin found.");
			return;
		}
		
		PickupMoney.Api = new DropManager(this);
		
		Bukkit.getPluginManager().registerEvents(new EntityDeathListener(PickupMoney.getApi(), this.economy), this);
		Bukkit.getPluginManager().registerEvents(new MoneyPickupListener(this), this);
		
		
		PaperCommandManager manager = new PaperCommandManager(this);
		manager.registerCommand(new PickupMoneyCommand(this));
		manager.enableUnstableAPI("help");
		
		
		
		Metrics metrics = new Metrics(this);
		
		SingleLineChart chart = new Metrics.SingleLineChart("dropped-money", new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				return getApi().getDroppedMoneyItems();
			}
		});
		metrics.addCustomChart(chart);
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = (Economy) rsp.getProvider();
		return economy != null;
	}
	
	public void initConfig() {
		try {
			File lang = new File(this.getDataFolder(), "config.yml");
			if (!lang.exists()) {
				this.getDataFolder().mkdir();
				lang.createNewFile();
				if (lang != null) {
					YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(lang);
					defConfig.save(lang);
					ConfigOptions.setFile(defConfig);
				}
			}

			YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
			ConfigOptions.setFile(conf);
			int updated = 0;
			for (ConfigOptions option : ConfigOptions.values()) {
				if (!conf.isSet(option.getPath())) {
					conf.set(option.getPath(), option.getDefault());
					updated++;
				}
			}

			if (updated > 0) {
				conf.save(lang);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
