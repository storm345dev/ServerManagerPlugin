package net.stormdev.MTA.SMPlugin.gui;

import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.gui.IconMenu.OptionClickEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AmountInputMenu {

	public AmountInputMenu(
			double currentAmount, ConfirmHandler cHandler) {
		this.menu = new IconMenu(ChatColor.BLUE+"Set Amount", 9, new ClickHandler(this), Core.plugin, true);
		
		this.amount = currentAmount;
		this.cHandler = cHandler;
		
		menu.setOption(0, 
				new ItemStack(Material.BOOK), 
				ChatColor.BLUE+"Cancel", ChatColor.GOLD+"Close the menu");
		// |0|1|2|3|Display(4)|5|6|7|8
		
		menu.setOption(1, new ItemStack(Material.PAPER), ChatColor.BLUE+"<<<", 
				ChatColor.GOLD+"-100.00");
		menu.setOption(2, new ItemStack(Material.PAPER), ChatColor.BLUE+"<<", 
				ChatColor.GOLD+"-10.00");
		menu.setOption(3, new ItemStack(Material.PAPER), ChatColor.BLUE+"<", 
				ChatColor.GOLD+"-0.50");
		menu.setOption(4, new ItemStack(Material.EMERALD), ChatColor.BLUE+"Amount:", 
				ChatColor.GOLD+""+currentAmount);
		menu.setOption(7, new ItemStack(Material.PAPER), ChatColor.BLUE+">>>", 
				ChatColor.GOLD+"+100.00");
		menu.setOption(6, new ItemStack(Material.PAPER), ChatColor.BLUE+">>", 
				ChatColor.GOLD+"+10.00");
		menu.setOption(5, new ItemStack(Material.PAPER), ChatColor.BLUE+">", 
				ChatColor.GOLD+"+0.50");
		menu.setOption(8, new ItemStack(Material.STONE_BUTTON), ChatColor.BLUE+"Confirm", ChatColor.GOLD+"Confirm the amount");
	}
	
	private IconMenu menu = null;
	private double amount = 0;
	private ConfirmHandler cHandler = null;
	
	public double getAmount(){
		return amount;
	}
	
	protected void setAmount(double amount){
		this.amount = amount;
	}
	
	public IconMenu getMenu(){
		return menu;
	}
	
	
	public static class ClickHandler implements IconMenu.OptionClickEventHandler {
		
		private AmountInputMenu ami;
		public ClickHandler(AmountInputMenu menu){
			this.ami = menu;
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void onOptionClick(OptionClickEvent event) {
			event.setWillClose(false);
			Inventory inv = event.getInventory();
			AmountInputMenu menu = ami;
			int pos = event.getPosition();
			Player player = event.getPlayer();
			double increment = 0;
			if(pos == 0){
				event.setWillClose(true);
				event.setWillDestroy(true);
				return;
			}
			else if(pos > 0 && pos < 8 && pos != 4){
				switch(pos){
				case 1:increment = -100;break;
				case 2:increment = -10;break;
				case 3:increment = -0.5;break;
				case 5:increment = 0.5;break;
				case 6:increment = 10;break;
				case 7:increment = 100;break;
				default:increment = 0;break;
				}
			}
			else if(pos == 4){
				//Do nothing
			}
			else{
				event.setWillClose(true);
				event.setWillDestroy(true);
				final double amount = menu.getAmount();
				final Player pl = player;
				final ConfirmHandler ch = menu.cHandler;
				Bukkit.getScheduler().runTaskLater(Core.plugin, new Runnable(){

					@Override
					public void run() {
						ch.onConfirm(amount, pl);
						return;
					}}, 2l);
				
				return;
			}
			
			//Increment the amount
			double amt = menu.getAmount()+increment;
			menu.setAmount(amt);
			ItemStack showcase = new ItemStack(Material.EMERALD);
			showcase = menu.getMenu().setItemNameAndLore(showcase, ChatColor.BLUE+"Amount:", 
					ChatColor.GOLD+""+amt);
			inv.setItem(4, showcase);
			
			player.updateInventory(); //Needed because Minecraft Fail :P
		}
	}
	
	public static interface ConfirmHandler {
		public void onConfirm(double amount, Player player);
	}

}




