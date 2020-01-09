/*
 *
 *  * This file is part of moneydrops, licensed under the MIT License.
 *  *
 *  *  Copyright (c) crysis992 <crysis992@gmail.com>
 *  *  Copyright (c) contributors
 *  *
 *  *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  *  of this software and associated documentation files (the "Software"), to deal
 *  *  in the Software without restriction, including without limitation the rights
 *  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  *  copies of the Software, and to permit persons to whom the Software is
 *  *  furnished to do so, subject to the following conditions:
 *  *
 *  *  The above copyright notice and this permission notice shall be included in all
 *  *  copies or substantial portions of the Software.
 *  *
 *  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  *  SOFTWARE.
 *
 */

package net.crytec.pickmoney;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import net.crytec.pickmoney.Metrics.SingleLineChart;
import net.crytec.pickmoney.api.DropManager;
import net.crytec.pickmoney.commands.PickupMoneyCommand;
import net.crytec.pickmoney.listener.EntityDeathListener;
import net.crytec.pickmoney.listener.MoneyPickupListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class PickupMoney extends JavaPlugin {

  private static DropManager api;
  private Economy economy;

  @Override
  public void onEnable() {
    getDataFolder().mkdirs();
    final File file = new File(this.getDataFolder(), "config.yml");
    if (!file.exists()) {
      this.saveResource("config.yml", true);
    }

    this.initConfig();

    if (!this.setupEconomy() || economy == null) {
      Bukkit.getPluginManager().disablePlugin(this);
      this.getLogger().severe("Failed to initialize PickupMoney - No compatible Economy plugin found.");
      return;
    }

    PickupMoney.api = new DropManager(this);

    Bukkit.getPluginManager().registerEvents(new EntityDeathListener(getApi(), this.economy), this);
    Bukkit.getPluginManager().registerEvents(new MoneyPickupListener(this), this);

    this.getCommand("moneydrop").setExecutor(new PickupMoneyCommand(this));

    final Metrics metrics = new Metrics(this);

    final SingleLineChart chart = new Metrics.SingleLineChart("dropped-money", new Callable<Integer>() {

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
    final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    this.economy = rsp.getProvider();
    return true;
  }

  public void initConfig() {
    try {
      final File lang = new File(this.getDataFolder(), "config.yml");
      if (!lang.exists()) {
        this.getDataFolder().mkdir();
        lang.createNewFile();
        if (lang != null) {
          final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(lang);
          defConfig.save(lang);
          ConfigOptions.setFile(defConfig);
        }
      }

      final YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
      ConfigOptions.setFile(conf);
      int updated = 0;
      for (final ConfigOptions option : ConfigOptions.values()) {
        if (!conf.isSet(option.getPath())) {
          conf.set(option.getPath(), option.getDefault());
          updated++;
        }
      }

      if (updated > 0) {
        conf.save(lang);
      }
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  public static DropManager getApi() {
    return api;
  }

  public Economy getEconomy() {
    return economy;
  }
}
