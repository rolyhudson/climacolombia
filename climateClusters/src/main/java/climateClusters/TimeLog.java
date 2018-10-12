package climateClusters;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

public class TimeLog {
	String description;
	int elapsedSeconds;
	public TimeLog(DateTime start, String describe) {
		elapsedSeconds = Seconds.secondsBetween(start, DateTime.now()).getSeconds();
		description = describe;
	}
	public String getDescription() {
		return this.description;
	}
	public int getElapsedSeconds() {
		return this.elapsedSeconds;
	}
	public void setDescription(String d) {
		this.description = d;
	}
	public void setElapsedSeconds(int s) {
		this.elapsedSeconds=s;
	}
}
