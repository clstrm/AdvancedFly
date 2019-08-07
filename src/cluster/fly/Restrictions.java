package cluster.fly;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Restrictions implements Listener {

	private boolean disableEntityDamage;
	private boolean disablePvP;
	private boolean disableThrowPotion;
	private boolean disableBowUse;
	private boolean disableProjectile;
	private List<String> disableInterationWithBlocks;
	private List<String> disableInterationWithItemInHand;
	
	private String disableEntityDamage_;
	private String disablePvP_;
	private String disableThrowPotion_;
	private String disableBowUse_;
	private String disableProjectile_;
	private String disableInterationWithBlocks_;
	private String disableInterationWithItemInHand_;
	
	public Restrictions() {
		Bukkit.getPluginManager().registerEvents(this, AdvancedFly.f);
	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(!(e.getDamager() instanceof Player)) return;
		
		Player p = (Player) e.getDamager();
		if(!getAllow(p)) return;
		
		if(disablePvP && e.getEntity() instanceof Player && !PMS.pvp.has(p)) {
			e.setCancelled(true);
			AdvancedFly.sendMessage(p, disablePvP_);
			return;
		}
		
		if(disableEntityDamage && !PMS.damage.has(p)) {
			e.setCancelled(true);
			AdvancedFly.sendMessage(p, disableEntityDamage_);
			return;
		}
	}
	
	
	
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onUse(PlayerInteractEvent e) {
		Action a = e.getAction();
		
		if(a != Action.RIGHT_CLICK_BLOCK && a != Action.RIGHT_CLICK_AIR) return;
		
		Player p = e.getPlayer();
		
		if(!getAllow(p)) return;
		
		if (a == Action.RIGHT_CLICK_BLOCK && !PMS.block.has(p))
		{
			String blockType = e.getClickedBlock().getType().toString();
			
			for (String s : disableInterationWithBlocks) {
				if(s.equalsIgnoreCase(blockType)) {
					e.setCancelled(true);
					AdvancedFly.sendMessage(p, disableInterationWithBlocks_.replace("{block}", s.toLowerCase()));
					return;
				}
			}
		}
		
		if(!checkItem(p, p.getItemInHand(), e) && !AdvancedFly.v1_8) {
			checkItem(p, p.getInventory().getItemInOffHand(), e);
		}
		
	}
	
	private boolean checkItem(Player p, ItemStack it, Cancellable e) {
		if(it == null) return false;
		Material t = it.getType();
		if(t == null) return false;
		
		String type = t.toString();
		
		if(!PMS.hand.has(p)) for (String s : disableInterationWithItemInHand) {
			if(s.equalsIgnoreCase(type)) {
				e.setCancelled(true);
				AdvancedFly.sendMessage(p, disableInterationWithItemInHand_.replace("{item}", s.toLowerCase()));
				updateInventory(p);
				return true;
			}
		}
		
		
		
		
		
		if(disableThrowPotion && t == Material.POTION && !PMS.potion.has(p)) {
			e.setCancelled(true);
			AdvancedFly.sendMessage(p, disableThrowPotion_);
			updateInventory(p);
			return true;
		}
		
		if(disableBowUse && t == Material.BOW && !PMS.bow.has(p)) {
			e.setCancelled(true);
			AdvancedFly.sendMessage(p, disableBowUse_);
			updateInventory(p);
			return true;
		}
		if(disableProjectile && 
				(
					t == Material.EGG || t == Material.SNOWBALL	
				)
				 && !PMS.projectile.has(p)) {
			e.setCancelled(true);
			AdvancedFly.sendMessage(p, disableProjectile_);
			updateInventory(p);
			return true;
		}
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void updateInventory(final Player p) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(AdvancedFly.f, new Runnable() {
			
			@Override
			public void run() {
				p.updateInventory();
			}
		}, 2);
	}


	private boolean getAllow(Player p) {
		if(p.getAllowFlight()) return true;
		return false;
	}
	
	public void load() {
		FileConfiguration c = AdvancedFly.f.getConfig();
		
		disableEntityDamage = c.getBoolean("restrictions.disableEntityDamage");
		disablePvP = c.getBoolean("restrictions.disablePvP");
		disableThrowPotion = c.getBoolean("restrictions.disableThrowPotion");
		disableBowUse = c.getBoolean("restrictions.disableBowUse");
		disableProjectile = c.getBoolean("restrictions.disableProjectile");
		disableInterationWithBlocks = c.getStringList("restrictions.disableInterationWithBlocks");
		if(disableInterationWithBlocks == null) disableInterationWithBlocks = new ArrayList<>();
		disableInterationWithItemInHand = c.getStringList("restrictions.disableInterationWithItemInHand");
		if(disableInterationWithItemInHand == null) disableInterationWithItemInHand = new ArrayList<>();
		
		disableEntityDamage_ = c.getString("messages.disableEntityDamage", "none").replace("&", "§");
		disablePvP_ = c.getString("messages.disablePvP", "none").replace("&", "§");
		disableThrowPotion_ = c.getString("messages.disableThrowPotion", "none").replace("&", "§");
		disableBowUse_ = c.getString("messages.disableBowUse", "none").replace("&", "§");
		disableProjectile_ = c.getString("messages.disableProjectile", "none").replace("&", "§");
		disableInterationWithBlocks_ = c.getString("messages.disableInterationWithBlocks", "none").replace("&", "§");
		disableInterationWithItemInHand_ = c.getString("messages.disableInterationWithItemInHand", "none").replace("&", "§");
	}
}
