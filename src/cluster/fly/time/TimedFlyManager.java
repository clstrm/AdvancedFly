package cluster.fly.time;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import cluster.fly.AdvancedFly;

public class TimedFlyManager {

	private FlyThread thread;
	private int id;
	
	protected Map<Player, FlyTicker> players = new HashMap<>();
	private Map<String, Long> group = new HashMap<>();
	
	public TimedFlyManager() {
		thread = new FlyThread(this);
		reload();
	}
	
	public void start() {
		if(id == 0)
			id = Bukkit.getScheduler().scheduleSyncRepeatingTask(AdvancedFly.f, thread, 20, 20);
	}
	
	public void stop() {
		if(id != 0) {
			id = Bukkit.getScheduler().scheduleSyncRepeatingTask(AdvancedFly.f, thread, 20, 20);
			id = 0;
		}
	}
	
	public void reload() {
		group.clear();
		
		FileConfiguration c = AdvancedFly.f.getConfig();
		ConfigurationSection fly = c.getConfigurationSection("tempFly.groups");
		if(fly == null) return;
		Set<String> keys = fly.getKeys(false);
		if(keys == null) return;
		for (String key : keys) {
			long value = c.getLong("tempFly.groups." + key);
			group.put(key, value);
		}
		
		if(c.getBoolean("tempFly.enable")) {
			start();
		} else {
			stop();
		}
	}
	
	public boolean isRunning() {
		return id != 0;
	}
	
	public long getValue(String key) {
		key = key.toLowerCase();
		return AdvancedFly.f.getData().getLong("playerData." + key + ".fly");
	}
	
	public void setValue(String key, long value) {
		key = key.toLowerCase();
		AdvancedFly.f.getData().set("playerData." + key + ".fly", value);
	    AdvancedFly.f.saveData();
	}

	public FlyTicker createTicker(Player p) {
		long allowed = getAllowed(p);
		long passed = getValue(p.getName());
		return new FlyTicker(System.currentTimeMillis(), allowed, passed);
	}

	private long getAllowed(Player p) {
		long v = 0;
		for (Entry<String, Long> e : group.entrySet()) {
			if(e.getValue() > v && p.hasPermission("advancedfly.fly.time." + e.getKey())) {
				v = e.getValue();
			}
		}
		return v;
	}
	
	
	
}
