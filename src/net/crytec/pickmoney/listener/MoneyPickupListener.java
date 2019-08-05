package net.crytec.pickmoney.listener;

import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;

import net.crytec.phoenix.api.nbt.NBTItem;
import net.crytec.phoenix.api.utils.UtilMath;
import net.crytec.phoenix.api.utils.UtilPlayer;
import net.crytec.pickmoney.ConfigOptions;
import net.crytec.pickmoney.PickupMoney;
import net.crytec.pickmoney.api.DropManager;
import net.crytec.shaded.org.apache.lang3.EnumUtils;
import net.milkbowl.vault.economy.EconomyResponse;

public class MoneyPickupListener implements Listener {

	private final PickupMoney plugin;
	
	private final Sound sound;
	private float pitch;
	private float volume;
	
	private final boolean showTitle;
	private final boolean showChatMessage;
	private final String title;
	private final String subtitle;

	public MoneyPickupListener(PickupMoney plugin) {
		this.plugin = plugin;
		
		if (EnumUtils.isValidEnum(Sound.class, ConfigOptions.SOUND.asString())) {
			sound = Sound.valueOf(ConfigOptions.SOUND.asString());
		} else {
			sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
		}
		
		this.showChatMessage = ConfigOptions.SHOW_CHAT.asBoolean();
		
		this.pitch = ConfigOptions.SOUND_PITCH.asFloat();
		this.volume = ConfigOptions.SOUND_VOLUME.asFloat();
		
		this.showTitle = ConfigOptions.SHOW_TITLE.asBoolean();
		this.title = ConfigOptions.TITLE_HEADER.asString(true);
		this.subtitle = ConfigOptions.TITLE_SUB.asString(true);
	}
	
	
	@EventHandler
	public void disableSpigotMerge(ItemMergeEvent event) {
		if (event.getEntity() instanceof Item && event.getEntity().getScoreboardTags().contains("pmr")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void denyHopperPickup(InventoryPickupItemEvent event) {
		if (event.getInventory().getType() != InventoryType.HOPPER)
			return;

		NBTItem nbt = new NBTItem(event.getItem().getItemStack());
		if (nbt.hasKey(DropManager.NBT_KEY)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickup(EntityPickupItemEvent event) {
		NBTItem nbt = new NBTItem(event.getItem().getItemStack());
		if (nbt.hasKey(DropManager.NBT_KEY)) {
			event.setCancelled(true);

			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				double amount = nbt.getDouble(DropManager.NBT_KEY);
				UtilPlayer.playSound(player, this.sound, this.volume, this.pitch);

				EconomyResponse response = plugin.getEconomy().depositPlayer(player, amount);
				if (response.transactionSuccess()) {
					String val = String.valueOf(UtilMath.unsafeRound(amount, 2));
					
					if (this.showChatMessage) {
						String message = ConfigOptions.PICKUP_MESSAGE.asString(true).replace("%value%", val);
						player.sendMessage(message);
					}
					
					if (this.showTitle) {
						player.sendTitle(this.title.replace("%value%", val), this.subtitle.replace("%value%", val), 5, 30, 5);
					}
				}

				event.getItem().remove();
				return;
			}
		}
	}
}