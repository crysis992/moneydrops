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

package net.crytec.pickmoney.listener;

import net.crytec.libs.commons.utils.UtilMath;
import net.crytec.libs.commons.utils.UtilPlayer;
import net.crytec.libs.commons.utils.lang.EnumUtils;
import net.crytec.pickmoney.ConfigOptions;
import net.crytec.pickmoney.PickupMoney;
import net.crytec.pickmoney.api.DropManager;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MoneyPickupListener implements Listener {

  private final PickupMoney plugin;
  private final DropManager manager;

  private final Sound sound;
  private final float pitch;
  private final float volume;

  private final boolean showTitle;
  private final boolean showChatMessage;
  private final String title;
  private final String subtitle;

  public MoneyPickupListener(final PickupMoney plugin) {
    this.plugin = plugin;
    this.manager = PickupMoney.getApi();

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
  public void disableSpigotMerge(final ItemMergeEvent event) {
    if (event.getEntity().getScoreboardTags().contains("pmr")) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void denyHopperPickup(final InventoryPickupItemEvent event) {
    if (event.getInventory().getType() != InventoryType.HOPPER) {
      return;
    }

    if (event.getItem().getItemStack().getItemMeta().getPersistentDataContainer().has(manager.key, PersistentDataType.DOUBLE)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerPickup(final EntityPickupItemEvent event) {
    final ItemMeta meta = event.getItem().getItemStack().getItemMeta();

    if (!meta.getPersistentDataContainer().has(manager.key, PersistentDataType.DOUBLE)) {
      return;
    }

    event.setCancelled(true);

    if (event.getEntity() instanceof Player) {
      final Player player = (Player) event.getEntity();
      final double amount = meta.getPersistentDataContainer().get(manager.key, PersistentDataType.DOUBLE);
      UtilPlayer.playSound(player, this.sound, this.volume, this.pitch);

      final EconomyResponse response = plugin.getEconomy().depositPlayer(player, amount);

      if (response.transactionSuccess()) {
        final String val = String.valueOf(UtilMath.unsafeRound(amount, 2));

        if (this.showChatMessage) {
          final String message = ConfigOptions.PICKUP_MESSAGE.asString(true).replace("%value%", val);
          player.sendMessage(message);
        }

        if (this.showTitle) {
          player.sendTitle(this.title.replace("%value%", val), this.subtitle.replace("%value%", val), 5, 30, 5);
        }
      }

      event.getItem().remove();
    }
  }
}