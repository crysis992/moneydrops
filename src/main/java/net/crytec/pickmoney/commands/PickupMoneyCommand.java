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

package net.crytec.pickmoney.commands;

import net.crytec.libs.commons.utils.UtilMath;
import net.crytec.pickmoney.PickupMoney;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class PickupMoneyCommand implements CommandExecutor {

  private final PickupMoney plugin;

  public PickupMoneyCommand(final PickupMoney plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    if (args[0].equals("reload")) {
      if (!sender.hasPermission("pickupmoney.admin")) {
        sender.sendMessage(ChatColor.RED + "You lack the proper permission to use this command!");
        return true;
      }
      plugin.reloadConfig();
      PickupMoney.getApi().load();
      sender.sendMessage(ChatColor.GREEN + "Configuration successfully reloaded.");
      return true;

    } else if (sender instanceof Player && args[0].equals("drop")) {
      if (!UtilMath.isDouble(args[1])) {
        sender.sendMessage(args[1] + " is not a valid number!");
        return true;
      }
      final Player player = (Player) sender;
      final Item drop = PickupMoney.getApi().dropNaturallyAtLocation(player.getLocation(), Double.parseDouble(args[1]));
      drop.setPickupDelay(100);
      sender.sendMessage(ChatColor.GREEN + "The item has been successfully spawned below you.");
    } else {
      sender.sendMessage(ChatColor.RED + "Please use /moneydrops drop <amount> or /moneydrops reload");
      return true;
    }
    return true;
  }
}