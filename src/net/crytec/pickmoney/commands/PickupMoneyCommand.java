package net.crytec.pickmoney.commands;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import net.crytec.acf.BaseCommand;
import net.crytec.acf.CommandHelp;
import net.crytec.acf.CommandIssuer;
import net.crytec.acf.annotation.CommandAlias;
import net.crytec.acf.annotation.CommandPermission;
import net.crytec.acf.annotation.Default;
import net.crytec.acf.annotation.Description;
import net.crytec.acf.annotation.Subcommand;
import net.crytec.acf.annotation.Syntax;
import net.crytec.pickmoney.PickupMoney;

@CommandAlias("pickupmoney|pickmoney")
@CommandPermission("pickupmoney.admin")
public class PickupMoneyCommand extends BaseCommand {
	
	private final PickupMoney plugin;
	
	public PickupMoneyCommand(PickupMoney plugin) {
		this.plugin = plugin;
	}
	
	@Default
	@Syntax("Show Help")
	public void showHelp(CommandIssuer issuer, CommandHelp help) {
		help.showHelp(issuer);
	}
	
	@Subcommand("reload")
	@Description("Reload the configuration.")
	public void voidReload(CommandIssuer issuer) {
		plugin.reloadConfig();
		PickupMoney.getApi().load();
		issuer.sendMessage("§2Configuration sucessfully reloaded.");
	}
	
	@Subcommand("drop")
	@Syntax("<amount>")
	@Description("Drop a set amount of money below you.")
	public void voidReload(Player issuer, double amount) {
		Item drop = PickupMoney.getApi().dropNaturallyAtLocation(issuer.getLocation(), amount);
		drop.setPickupDelay(100);
		issuer.sendMessage("§2The item has been successfully spawned below you.");
	}

}
