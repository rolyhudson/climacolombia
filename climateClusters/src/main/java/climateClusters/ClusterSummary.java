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
	private double clusterUTCI;
	private double clusterIdeamCI;
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
	public double getClusterUTCI() {
		return clusterUTCI;
	}
	public void setClusterUTCI(double utci) {
		clusterUTCI = utci;
	}
	public double getClusterIdeamCI() {
		return clusterIdeamCI;
	}
	public void setClusterIdeamCI(double ideamci) {
		clusterIdeamCI = ideamci;
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
