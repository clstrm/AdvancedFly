package cluster.fly.time;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import cluster.fly.AdvancedFly;

public class FlyThread implements Runnable {

	private TimedFlyManager fly;
	
	
	public FlyThread(TimedFlyManager fly) {
		this.fly = fly;
	}
	
	@Override
	public void run() {
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if(!AdvancedFly.f.manager.isFlying(p)) continue;
			if(p.hasPermission("advancedfly.fly.overlimit")) {
				fly.players.remove(p);
				continue;
			}
			if(!fly.players.containsKey(p)) fly.players.put(p, fly.createTicker(p));
		}
		
		Map<Player, FlyTicker> remove = new HashMap<>();
		
		for (Entry<Player, FlyTicker> e : fly.players.entrySet()) {
			Player p = e.getKey();
			FlyTicker t = e.getValue();
			
			if(!AdvancedFly.f.manager.isFlying(p) || !p.isOnline()) {
				remove.put(p, t);
				continue;
			}
			
			if(t.expired()) {
				AdvancedFly.f.manager.depplyFly(p);
				remove.put(p, t);
				if(AdvancedFly.nms() != null) AdvancedFly.nms().sendActionBar(p, "");
				AdvancedFly.sendMessage(p, AdvancedFly.f.flyExpired);
				continue;
			}
			
			if(AdvancedFly.nms() != null) AdvancedFly.nms().sendActionBar(p, t.getMessage());
		}
		
		for (Entry<Player, FlyTicker> e : remove.entrySet()) {
			fly.setValue(e.getKey().getName(), e.getValue().passed() / 1000);
			fly.players.remove(e.getKey());
		}
	}
	


}
