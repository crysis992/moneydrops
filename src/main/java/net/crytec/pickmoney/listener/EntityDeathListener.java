package net.crytec.pickmoney.listener;

import net.crytec.libs.commons.utils.UtilMath;
import net.crytec.pickmoney.ConfigOptions;
import net.crytec.pickmoney.api.DropManager;
import net.crytec.pickmoney.api.EntityDropData;
import net.crytec.pickmoney.events.EntityDropMoneyEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class EntityDeathListener implements Listener {

  private final DropManager manager;
  private final Economy eco;

  public EntityDeathListener(final DropManager manager, final Economy economy) {
    this.manager = manager;
    this.eco = economy;
  }

  @EventHandler
  public void onEntityKill(final EntityDeathEvent event) {
    if (event.getEntity() instanceof Player) {
      return;
    }

    if (!manager.isWorldEnabled(event.getEntity().getWorld())) {
      return;
    }
    if (!manager.dropData.containsKey(event.getEntity().getType())) {
      return;
    }

    final EntityDropData data = manager.dropData.get(event.getEntity().getType());

    final double chance = manager.getRandom(2);

    if (chance > data.getChance()) {
      return;
    }

    final double money = manager.getDropAmount(data.getRange());

    if (money < ConfigOptions.MINIMUM_TO_DROP.asDouble()) {
      return;
    }

    if (ConfigOptions.REQUIRE_PLAYERKILL.asBoolean() && event.getEntity().getKiller() == null) {
      return;
    }

    final EntityDropMoneyEvent toCall = new EntityDropMoneyEvent(event.getEntity(), money);
    Bukkit.getPluginManager().callEvent(toCall);

    if (toCall.isCancelled()) {
      return;
    }
    manager.dropNaturallyAtLocation(event.getEntity().getLocation(), money);
  }

  @EventHandler
  public void onPlayerDeath(final PlayerDeathEvent event) {
    if (!manager.dropData.containsKey(EntityType.PLAYER)) {
      return;
    }

    if (event.getEntity().hasPermission("pickupmoney.bypass")) {
      return;
    }

    final EntityDropData data = manager.dropData.get(EntityType.PLAYER);
    final double chance = manager.getRandom(2);

    if (chance > data.getChance()) {
      return;
    }
    final double percentage = manager.getDropAmount(data.getRange());
    final double balance = eco.getBalance(event.getEntity());
    if (balance <= 0) {
      return;
    }

    double amount = (balance / 100) * percentage;
    if (amount < ConfigOptions.MINIMUM_TO_DROP.asDouble()) {
      return;
    }

    if (amount > ConfigOptions.PLAYER_DROP_HARDCAP.asDouble()) {
      amount = ConfigOptions.PLAYER_DROP_HARDCAP.asDouble();
    }

    final EntityDropMoneyEvent toCall = new EntityDropMoneyEvent(event.getEntity(), amount);
    Bukkit.getPluginManager().callEvent(toCall);

    if (toCall.isCancelled()) {
      return;
    }

    if (this.eco.withdrawPlayer(event.getEntity(), amount).transactionSuccess()) {
      manager.dropNaturallyAtLocation(event.getEntity().getLocation(), amount);
      event.getEntity().sendMessage(ConfigOptions.LOST_MONEY.asString(true).replace("%value%", String.valueOf(UtilMath.unsafeRound(amount, 2))));
    }
  }

}
