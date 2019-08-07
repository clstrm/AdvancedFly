package cluster.fly;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.bukkit.Bukkit;

public class Scheduler implements Runnable {

	private long time;
	
	public Scheduler() {
		time = AdvancedFly.f.getData().getLong("scheduler_time");
		Bukkit.getScheduler().scheduleSyncRepeatingTask(AdvancedFly.f, this, 200, 200);
	}
	
	
	@Override
	public void run() {
		if(time == 0) {
			time = System.currentTimeMillis();
			AdvancedFly.f.getData().set("scheduler_time", time);
			AdvancedFly.f.saveData();
		}
		
		if(System.currentTimeMillis() > time) {
			exec();
			calc();
		}
	}


	public void exec() {
		AdvancedFly.f.cleanUp();
	}
	
	private void calc() {
		 LocalDateTime now = LocalDateTime.now();
		 LocalDateTime midnight = now.toLocalDate().atStartOfDay();
		 Date d1 = Date.from(midnight.atZone(ZoneId.systemDefault()).toInstant());
		 time = d1.getTime() + 1000 * 3600 * 24 + 60000;
		 AdvancedFly.f.getData().set("scheduler_time", time);
		 AdvancedFly.f.saveData();
	}
	
	public long getTime() {
		return time;
	}
}
