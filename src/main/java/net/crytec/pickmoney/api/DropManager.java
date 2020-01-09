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

package net.crytec.pickmoney.api;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.Getter;
import net.crytec.libs.commons.utils.UtilMath;
import net.crytec.libs.commons.utils.item.ItemBuilder;
import net.crytec.libs.commons.utils.lang.EnumUtils;
import net.crytec.pickmoney.ConfigOptions;
import net.crytec.pickmoney.PickupMoney;
import net.crytec.pickmoney.utils.ItemPair;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class DropManager {

  public final NamespacedKey key;

  public final RangeMap<Integer, ItemPair> iconRange = TreeRangeMap.create();
  public final HashMap<EntityType, EntityDropData> dropData = Maps.newHashMap();
  private final PickupMoney plugin;

  @Getter
  private int droppedMoneyItems = 0;

  private boolean showDisplayName = true;
  private boolean mergeDrops = true;
  private boolean dropNaturally = true;

  private ItemPair defaultIcon;

  private final Set<String> worlds;

  public DropManager(final PickupMoney instance) {
    key = new NamespacedKey(instance, "pickupmoney");
    this.plugin = instance;
    this.load();
    this.worlds = ImmutableSet.copyOf(ConfigOptions.BLACKLISTED_WORLDS.asStringList());
    this.defaultIcon = new ItemPair(Material.GOLD_INGOT, 0);
  }

  public void load() {

    this.mergeDrops = ConfigOptions.MERGE_ITEMS.asBoolean();
    this.dropNaturally = ConfigOptions.DROP_NATURALLY.asBoolean();
    this.showDisplayName = ConfigOptions.SHOW_DISPLAYNAME.asBoolean();
    this.defaultIcon = new ItemPair(ConfigOptions.DEFAULT_ICON.asString());

    final ConfigurationSection range = plugin.getConfig().getConfigurationSection("icon");

    for (final String key : range.getKeys(false)) {
      final ConfigurationSection entry = range.getConfigurationSection(key);

      final int min = entry.getInt("rangeMin");
      final int max = entry.getInt("rangeMax");

      iconRange.put(Range.closed(min, max), new ItemPair(entry.getString("material")));
    }

    if (ConfigOptions.PLAYER_DROP_ENABLED.asBoolean()) {
      final String[] value = ConfigOptions.PLAYER_DROP_PERCENTAGE.asString().split("-");
      final double min = Double.parseDouble(value[0]);
      double max = min;
      if (value.length == 2) {
        max = Double.parseDouble(value[1]);
      }

      final Range<Double> dropRange = Range.closed(min, max);
      final double chance = ConfigOptions.PLAYER_DROP_CHANCE.asDouble();
      final EntityDropData data = new EntityDropData(EntityType.PLAYER, dropRange, chance);
      dropData.put(EntityType.PLAYER, data);
    }

    final ConfigurationSection entity = plugin.getConfig().getConfigurationSection("entity");

    for (final String key : entity.getKeys(false)) {
      final ConfigurationSection entry = entity.getConfigurationSection(key);

      if (!EnumUtils.isValidEnum(EntityType.class, key)) {
        plugin.getLogger().severe("The given configuration entry for " + key + " is not a valid entitytype");
        continue;
      }

      final EntityType type = EntityType.valueOf(key);
      final String[] value = entry.getString("amount").split("-");

      double min = 0;
      double max = 0;

      try {
        min = Double.parseDouble(value[0]);
        max = min;
      } catch (final NumberFormatException ex) {
        plugin.getLogger().severe("Failed to parse double for entity - " + key);
        continue;
      }

      if (value.length == 2) {
        try {
          max = Double.parseDouble(value[1]);
        } catch (final NumberFormatException ex) {
          plugin.getLogger().severe("Failed to parse double for entity - " + key);
          continue;
        }
      } else {
        max = min;
      }

      final Range<Double> dropRange = Range.closed(min, max);
      final double chance = entry.getDouble("chance");

      if (entry.getBoolean("enabled")) {
        final EntityDropData data = new EntityDropData(type, dropRange, chance);
        dropData.put(type, data);
      }
    }
  }

  public boolean isWorldEnabled(final World world) {
    return !this.worlds.contains(world.getName());
  }

  public ItemStack getDropItem(final double amount) {
    final ItemStack item = new ItemBuilder(this.getIcon(amount)).build();
    final ItemMeta meta = item.getItemMeta();
    meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, amount);
    item.setItemMeta(meta);
    return item;
  }

  public ItemStack getIcon(final double amount) {
    final ItemPair icon = this.iconRange.get((int) amount);
    return (icon != null) ? icon.getIcon() : this.defaultIcon.getIcon();
  }

  public double getDropAmount(final Range<Double> range) {
    final double min = range.lowerEndpoint();
    final double max = range.upperEndpoint();
    if (min == max) {
      return range.lowerEndpoint();
    }

    if (ConfigOptions.DO_DECIMAL_DROPS.asBoolean()) {
      return ThreadLocalRandom.current().nextDouble(min, max);
    } else {
      return ThreadLocalRandom.current().nextInt((int) Math.round(min), (int) Math.round(max));
    }
  }

  public double getRandom(final int decimals) {
    final double random = ThreadLocalRandom.current().nextFloat() * 100;
    final double scale = Math.pow(10, decimals);
    return Math.round(random * scale) / scale;
  }


  public Item dropNaturallyAtLocation(final Location location, final double value) {
    this.droppedMoneyItems++;
    if (this.mergeDrops) {
      return this.merge(location, value);
    } else {
      final Item item;
      if (this.dropNaturally) {
        item = location.getWorld().dropItemNaturally(location, this.getDropItem(value));
      } else {
        item = location.getWorld().dropItem(location, this.getDropItem(value));
      }
      item.addScoreboardTag("pmr");
      this.setDisplay(item, value);
      return item;
    }
  }


  public Item merge(final Location loc, final double start) {
    final List<Item> items = loc.getWorld().getNearbyEntities(loc, 2, 2, 2).stream().filter(ent -> ent instanceof Item)
        .map(e -> (Item) e).filter(i -> i.getScoreboardTags().contains("pmr")).collect(Collectors.toList());

    double mergedAmount = start;

    for (final Item i : items) {
      if (i.getScoreboardTags().contains("ismerging")) {
        continue;
      }
      i.addScoreboardTag("ismerging");
      final ItemMeta meta = i.getItemStack().getItemMeta();
      mergedAmount += meta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
      i.remove();
    }

    final ItemStack newItem = this.getDropItem(mergedAmount);
    final Item newDrop;
    if (this.dropNaturally) {
      newDrop = loc.getWorld().dropItemNaturally(loc, newItem);
    } else {
      newDrop = loc.getWorld().dropItem(loc, newItem);
    }

    newDrop.addScoreboardTag("pmr");
    this.setDisplay(newDrop, mergedAmount);
    return newDrop;
  }

  private void setDisplay(final Item drop, final double mergedAmount) {
    if (this.showDisplayName) {
      drop.setCustomNameVisible(true);
      drop.setCustomName(ChatColor.translateAlternateColorCodes('&', ConfigOptions.DROP_DISPLAYNAME.asString().replace("%value%", String.valueOf(UtilMath.unsafeRound(mergedAmount, 2)))));
    }
  }

}
