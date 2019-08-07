package cluster.fly.time;

import java.time.LocalTime;

import cluster.fly.AdvancedFly;
public class FlyTicker {

	private long started, allowed, passed;
	
	public FlyTicker(long started, long allowed, long passed) {
		this.started = started;
		this.allowed = allowed * 1000;
		this.passed = passed * 1000;
	}
	
	
	
	
	
	public boolean expired() {
		return remain() - System.currentTimeMillis() + started < 0;
	}
	
	public String getMessage() {
		return AdvancedFly.f.actionBar.replace("{time}", time());
	}
	
	
	private String time() {
		long r = remain() - System.currentTimeMillis() + started;
		if(r < 0) r = 0;
		LocalTime timeOfDay = LocalTime.ofSecondOfDay(r / 1000);
		return timeOfDay.toString();
	}





	private long remain() {
		return allowed - passed;
	}
	
	public long remainAllowed() {
		return allowed - passed - System.currentTimeMillis() + started;
	}
	
	public long passed() {
		return passed + (System.currentTimeMillis() - started);
	}
}
