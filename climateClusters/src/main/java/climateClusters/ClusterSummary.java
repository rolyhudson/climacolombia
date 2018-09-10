package climateClusters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.mllib.linalg.Vector;

public class ClusterSummary implements Serializable{
	private int clusterId;
	private Long count;
	private Vector centroid;
	private List<DesignStrategy> strategies = new ArrayList<DesignStrategy>();
	public void setClusterId(int id) {
		clusterId = id;
	}
	public int getClusterId() {
		return clusterId;
	}
	public void setCount(Long c) {
		count = c;
	}
	public Long getCount() {
		return count;
	}
	public void setCentroid(Vector c) {
		centroid = c;
	}
	public Vector getCentroid() {
		return centroid;
	}
	public void setStrategies(List<DesignStrategy> s) {
		strategies =s;
	}
	public List<DesignStrategy> getStrategies() {
		return strategies;
	}
}
