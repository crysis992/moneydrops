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
