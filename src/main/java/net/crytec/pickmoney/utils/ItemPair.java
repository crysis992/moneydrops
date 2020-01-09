package net.crytec.pickmoney.utils;

import net.crytec.libs.commons.utils.UtilMath;
import net.crytec.libs.commons.utils.item.ItemBuilder;
import net.crytec.libs.commons.utils.lang.EnumUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemPair {

  private final Material material;
  private final int modeldata;
  private final ItemStack icon;

  public ItemPair(final Material item, final int modeldata) {
    material = item;
    this.modeldata = modeldata;
    final ItemBuilder builder = new ItemBuilder(this.material);
    if (this.modeldata > 0) {
      builder.setModelData(this.modeldata);
    }
    this.icon = builder.build();
  }

  public ItemPair(final String data) {
    if (data.contains(":")) {
      final String[] split = data.split(":");

      if (EnumUtils.isValidEnum(Material.class, split[0])) {
        this.material = Material.valueOf(split[0]);
      } else {
        this.material = Material.BARRIER;
      }

      if (UtilMath.isInt(split[1])) {
        this.modeldata = Integer.parseInt(split[1]);
      } else {
        this.modeldata = 0;
      }
    } else {
      if (EnumUtils.isValidEnum(Material.class, data)) {
        this.material = Material.valueOf(data);
      } else {
        this.material = Material.BARRIER;
      }
      this.modeldata = 0;
    }
    final ItemBuilder builder = new ItemBuilder(this.material);
    if (this.modeldata > 0) {
      builder.setModelData(this.modeldata);
    }
    this.icon = builder.build();
  }

  public ItemStack getIcon() {
    return this.icon.clone();
  }

  public Material getMaterial() {
    return this.material;
  }

  public int getModelData() {
    return this.modeldata;
  }
}
