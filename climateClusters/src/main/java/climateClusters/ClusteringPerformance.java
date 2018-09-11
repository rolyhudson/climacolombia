package climateClusters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class ClusteringPerformance implements Serializable {
	private int nClusters;
	private double cost;
	private boolean selected;
	
	public int getNClusters() {
		return nClusters;
		
	}
	public void setNClustsers(int n) {
		nClusters =n;
	}
	public double getCost() {
		return cost;
		
	}
	public void setCost(double c) {
		cost =c;
	}
	public boolean getSelected() {
		return selected;
		
	}
	public void setSelected(boolean select) {
		selected = select;
	}
	public static int findElbowCluster(List<ClusteringPerformance> points) {
		List<Double> distSq = new ArrayList<Double>();
		ClusteringPerformance p1 = points.get(0);
		ClusteringPerformance p2 = points.get(points.size()-1);
		Vector2D v = new Vector2D(p2.getNClusters()-p1.getNClusters(),p2.getCost()-p1.getCost());
		Vector2D w;
		
		for(int i=0;i<points.size();i++)
		{
			ClusteringPerformance p = points.get(i);
			w = new Vector2D(p.getNClusters()-p1.getNClusters(),p.getCost()-p1.getCost());
			double vsq = v.dotProduct(v);
			double wsq = w.dotProduct(w);
			double proj = w.dotProduct(v);
			double dsq = wsq - proj*proj/vsq;
			distSq.add(dsq);
		}
		return 2;
	}
}
