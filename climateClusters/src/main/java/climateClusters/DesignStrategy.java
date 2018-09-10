package climateClusters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DesignStrategy implements Serializable{
	private String name;
	private List<double[]> points = new ArrayList<double[]>();
	
	public void setName(String n) {
		name=n;
	}
	public String getName() {
		return name;
	}
	public void setPoints(List<double[]> pts) {
		points =pts;;
	}
	public List<double[]> getPoints() {
		return points;
	}
}
