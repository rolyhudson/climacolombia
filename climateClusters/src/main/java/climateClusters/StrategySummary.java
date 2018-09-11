package climateClusters;

import java.io.Serializable;

public class StrategySummary implements Serializable {
	private int count;
	private double percent;
	private String name;
	public void setCount(int c) {
		count =c;
	}
	public int getCount() {
		return count;
	}
	public void setPercent(double d) {
		percent = Math.round(d* 100.0) / 100.0;
	}
	public double getPercent() {
		return percent;
	}
	public void setName(String n) {
		name =n;
	}
	public String getName() {
		return name;
	}
}
