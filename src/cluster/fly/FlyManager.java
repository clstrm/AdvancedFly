package cluster.fly;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class FlyManager implements Listener {

	private Map<Player, Fly> storage = new HashMap<>();
	
	public FlyManager() {
		
	}
	
	public boolean isFlying(Player p) {
		return storage.containsKey(p);
	}
	
	public void setFlying(Player p, boolean value) {
		if(value == isFlying(p)) return;
		
		if(value) {
			applyFly(p);
			AdvancedFly.sendMessage(p, AdvancedFly.f.enabled);
		} else {
			depplyFly(p);
			AdvancedFly.sendMessage(p, AdvancedFly.f.disabled);
		}
	}
	
	public void setFlying(Player p, CommandSender init, boolean value) {
		if(value == isFlying(p)) return;
		
		if(value) {
			applyFly(p);
			AdvancedFly.sendMessage(init, AdvancedFly.f.enabledFor.replace("{player}", p.getName()));
			AdvancedFly.sendMessage(p, AdvancedFly.f.enabledBy.replace("{sender}", init.getName()));
		} else {
			depplyFly(p);
			AdvancedFly.sendMessage(init, AdvancedFly.f.disabledFor.replace("{player}", p.getName()));
			AdvancedFly.sendMessage(p, AdvancedFly.f.disabledBy.replace("{sender}", init.getName()));
		}
	}
	
	
	public void applyFly(Player p) {
		p.setAllowFlight(true);
		storage.put(p, new Fly(p));
	}
	public void depplyFly(Player p) {
		p.setAllowFlight(false);
		Fly fly = storage.remove(p);
		if(fly == null) return;
		try {
			if(AdvancedFly.f.noTeleportPermission && p.hasPermission("advancedfly.stay")) return;
		} catch (Exception e) {
			AdvancedFly.f.getLogger().severe(e.getClass().getName() + ": " + e.getMessage());
		}
		fly.teleport();
	}

	public void close() {
		Player[] arr = storage.keySet().toArray(new Player[storage.keySet().size()]);
		for (int i = 0; i < arr.length; i++) {
			depplyFly(arr[i]);
		}
	}
	
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		
		Fly fly = storage.get(p);
		if(fly == null) return;
		
		Location loc = fly.getLocation();
		Location curr = p.getLocation();
		if(!loc.getWorld().equals(curr.getWorld())) return;
		
		if(AdvancedFly.f.maxDistance > 0 && loc.distance(curr) > AdvancedFly.f.maxDistance
				&& !p.hasPermission("advancedfly.overdistance")) {
			fly.teleport();
			AdvancedFly.sendMessage(p, AdvancedFly.f.overDistance);
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		
		Fly fly = storage.get(p);
		if(fly == null) return;
		
		if(fly.isLocked()) return;
		
		if(!p.hasPermission("advancedfly.teleport")) {
			e.setCancelled(true);
			AdvancedFly.sendMessage(p, AdvancedFly.f.cannotTeleport);
		}
	}
	
	
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if(isFlying(p)) depplyFly(p);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Bukkit.getScheduler().runTaskLater(AdvancedFly.f, new Runnable() {
			
			@Override
			public void run() {
				if(p.getAllowFlight()) storage.put(p, new Fly(p));
			}
		}, 5L);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
