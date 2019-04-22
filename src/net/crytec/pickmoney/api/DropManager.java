package net.crytec.pickmoney.api;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import io.netty.util.internal.ThreadLocalRandom;
import lombok.Getter;
import net.crytec.api.itemstack.ItemBuilder;
import net.crytec.api.nbt.NBTItem;
import net.crytec.api.util.UtilMath;
import net.crytec.pickmoney.ConfigOptions;
import net.crytec.pickmoney.PickupMoney;
import net.crytec.shaded.org.apache.lang3.EnumUtils;

public class DropManager {

	public static final String NBT_KEY = "pickupmoney";

	public final RangeMap<Integer, Material> iconRange = TreeRangeMap.create();
	public final HashMap<EntityType, EntityDropData> dropData = Maps.newHashMap();
	private final PickupMoney plugin;

	@Getter
	private int droppedMoneyItems = 0;

	private boolean showDisplayName = true;
	private boolean mergeDrops = true;
	private boolean dropNaturally = true;

	private Material defaultMaterial = Material.GOLD_INGOT;

	private final Set<String> worlds;

	public DropManager(PickupMoney instance) {
		this.plugin = instance;
		this.load();
		this.worlds = ImmutableSet.copyOf(ConfigOptions.BLACKLISTED_WORLDS.asStringList());
	}

	public void load() {

		this.mergeDrops = ConfigOptions.MERGE_ITEMS.asBoolean();
		this.dropNaturally = ConfigOptions.DROP_NATURALLY.asBoolean();
		this.showDisplayName = ConfigOptions.SHOW_DISPLAYNAME.asBoolean();
		
		if (EnumUtils.isValidEnum(Material.class, ConfigOptions.DEFAULT_ICON.asString())) {
			this.defaultMaterial = Material.valueOf(ConfigOptions.DEFAULT_ICON.asString());
		}

		ConfigurationSection range = plugin.getConfig().getConfigurationSection("icon");

		for (String key : range.getKeys(false)) {
			ConfigurationSection entry = range.getConfigurationSection(key);

			int min = entry.getInt("rangeMin");
			int max = entry.getInt("rangeMax");
			Material mat = Material.valueOf(entry.getString("material"));

			iconRange.put(Range.closed(min, max), mat);
		}
		
		if (ConfigOptions.PLAYER_DROP_ENABLED.asBoolean()) {
			String[] value = ConfigOptions.PLAYER_DROP_PERCENTAGE.asString().split("-");
			double min = Double.parseDouble(value[0]);
			double max = min;
			if (value.length == 2) {
				max = Double.parseDouble(value[1]);
			}
			
			Range<Double> dropRange = Range.closed(min, max);
			double chance = ConfigOptions.PLAYER_DROP_CHANCE.asDouble();
			EntityDropData data = new EntityDropData(EntityType.PLAYER, dropRange, chance);
			dropData.put(EntityType.PLAYER, data);
		}
		

		ConfigurationSection entity = plugin.getConfig().getConfigurationSection("entity");

		for (String key : entity.getKeys(false)) {
			ConfigurationSection entry = entity.getConfigurationSection(key);
			
			if (!EnumUtils.isValidEnum(EntityType.class, key)) {
				plugin.getLogger().severe("The given configuration entry for " + key + " is not a valid entitytype");
				continue;
			}
			
			EntityType type = EntityType.valueOf(key);
			String[] value = entry.getString("amount").split("-");
			
			double min = 0;
			double max = 0;

			try {
				min = Double.parseDouble(value[0]);max = min;
			} catch (NumberFormatException ex) {
				plugin.getLogger().severe("Failed to parse double for entity - " + key);
				continue;
			}
			
			if (value.length == 2) {
				try {
					max = Double.parseDouble(value[1]);
				} catch (NumberFormatException ex) {
					plugin.getLogger().severe("Failed to parse double for entity - " + key);
					continue;
				}
			} else {
				max = min;
			}

			Range<Double> dropRange = Range.closed(min, max);
			double chance = entry.getDouble("chance");

			if (entry.getBoolean("enabled")) {
				EntityDropData data = new EntityDropData(type, dropRange, chance);
				dropData.put(type, data);
			}
		}
	}

	public boolean isWorldEnabled(World world) {
		return !this.worlds.contains(world.getName());
	}

	public ItemStack getDropItem(double amount) {
		ItemStack item = new ItemBuilder(this.getIcon(amount)).build();
		NBTItem nbt = new NBTItem(item);
		nbt.setDouble(NBT_KEY, amount);
		return nbt.getItem();
	}

	public Material getIcon(double amount) {
		Material material = this.iconRange.get((int) amount);
		return (material != null) ? material : this.defaultMaterial;
	}

	public double getDropAmount(Range<Double> range) {
		double min = range.lowerEndpoint();
		double max = range.upperEndpoint();
		if (min == max) {
			return range.lowerEndpoint();
		}
		
		if (ConfigOptions.DO_DECIMAL_DROPS.asBoolean()) {
			return ThreadLocalRandom.current().nextDouble(min, max);
		} else {
			return ThreadLocalRandom.current().nextInt( (int) Math.round(min), (int) Math.round(max));
		}
	}

	public double getRandom(int decimals) {
		double random = ThreadLocalRandom.current().nextFloat() * 100;
		double scale = Math.pow(10, decimals);
		return Math.round(random * scale) / scale;
	}


	public Item dropNaturallyAtLocation(Location location, double value) {
		this.droppedMoneyItems++;
		if (this.mergeDrops) {
			return this.merge(location, value);
		} else {
			Item item;
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

	
	public Item merge(Location loc, double start) {
		List<Item> items = loc.getWorld().getNearbyEntities(loc, 2, 2, 2).stream().filter(ent -> ent instanceof Item)
				.map(e -> (Item) e).filter(i -> i.getScoreboardTags().contains("pmr")).collect(Collectors.toList());

		double mergedAmount = start;

		for (Item i : items) {
			if (i.getScoreboardTags().contains("ismerging")) continue;
			i.addScoreboardTag("ismerging");
			NBTItem cnbt = new NBTItem(i.getItemStack());
			mergedAmount += cnbt.getDouble(NBT_KEY);
			i.remove();
		}

		ItemStack newItem = this.getDropItem(mergedAmount);
		Item newDrop;
		if (this.dropNaturally) {
			newDrop = loc.getWorld().dropItemNaturally(loc, newItem);
		} else {
			newDrop = loc.getWorld().dropItem(loc, newItem);
		}
		
		newDrop.addScoreboardTag("pmr");
		this.setDisplay(newDrop, mergedAmount);
		return newDrop;
	}

	private void setDisplay(Item drop, double mergedAmount) {
		if (this.showDisplayName) {
			drop.setCustomNameVisible(true);
			drop.setCustomName(ChatColor.translateAlternateColorCodes('&', ConfigOptions.DROP_DISPLAYNAME.asString().replace("%value%", String.valueOf(UtilMath.unsafeRound(mergedAmount, 2)))));
		}
	}

}
