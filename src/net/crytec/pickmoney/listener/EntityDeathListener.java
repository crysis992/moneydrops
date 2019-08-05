package net.crytec.pickmoney.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.crytec.phoenix.api.utils.UtilMath;
import net.crytec.pickmoney.ConfigOptions;
import net.crytec.pickmoney.api.DropManager;
import net.crytec.pickmoney.api.EntityDropData;
import net.crytec.pickmoney.events.EntityDropMoneyEvent;
import net.milkbowl.vault.economy.Economy;

public class EntityDeathListener implements Listener {

	private final DropManager manager;
	private final Economy eco;

	public EntityDeathListener(DropManager manager, Economy economy) {
		this.manager = manager;
		this.eco = economy;
	}

	@EventHandler
	public void onEntityKill(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player)
			return;

		if (!manager.isWorldEnabled(event.getEntity().getWorld())) return;
		if (!manager.dropData.containsKey(event.getEntity().getType()))
			return;
		
		EntityDropData data = manager.dropData.get(event.getEntity().getType());

		double chance = manager.getRandom(2);

		if (chance > data.getChance())
			return;

		double money = manager.getDropAmount(data.getRange());
		
		if (money < ConfigOptions.MINIMUM_TO_DROP.asDouble()) return;
		
		if (ConfigOptions.REQUIRE_PLAYERKILL.asBoolean() && event.getEntity().getKiller() == null) return;
		
		EntityDropMoneyEvent toCall = new EntityDropMoneyEvent(event.getEntity(), money);
		Bukkit.getPluginManager().callEvent(toCall);

		if (toCall.isCancelled()) {
			return;
		}
		manager.dropNaturallyAtLocation(event.getEntity().getLocation(), money);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!manager.dropData.containsKey(EntityType.PLAYER))
			return;
		
		if (event.getEntity().hasPermission("pickupmoney.bypass")) return;

		EntityDropData data = manager.dropData.get(EntityType.PLAYER);
		double chance = manager.getRandom(2);

		if (chance > data.getChance())
			return;
		double percentage = manager.getDropAmount(data.getRange());
		double balance = eco.getBalance(event.getEntity());
		if (balance <= 0) return;
		
		double amount = (balance / 100) * percentage;
		if (amount < ConfigOptions.MINIMUM_TO_DROP.asDouble()) return;
		
		if (amount > ConfigOptions.PLAYER_DROP_HARDCAP.asDouble()) {
			amount = ConfigOptions.PLAYER_DROP_HARDCAP.asDouble();
		}

		EntityDropMoneyEvent toCall = new EntityDropMoneyEvent(event.getEntity(), amount);
		Bukkit.getPluginManager().callEvent(toCall);

		if (toCall.isCancelled()) {
			return;
		}
		
		if (this.eco.withdrawPlayer(event.getEntity(), amount).transactionSuccess()) {
			manager.dropNaturallyAtLocation(event.getEntity().getLocation(), amount);
			event.getEntity().sendMessage(ConfigOptions.LOST_MONEY.asString(true).replace("%value%",  String.valueOf(UtilMath.unsafeRound(amount, 2))));
		}
	}

}
