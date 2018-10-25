package climateClusters;

import java.io.Serializable;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class ClusteringPerformance implements Serializable {
	private int nClusters;
	private double costWSSSE;
	private double silhouette;
	private double dunn;
	private boolean selected;
	private String jobname;
	public ClusteringPerformance(int nclusters,double wsssecost,double BDsil,double BDdunn,boolean isselected,String name) {
		nClusters = nclusters;
		costWSSSE =  wsssecost;
		silhouette = BDsil;
		dunn = BDdunn;
		selected = isselected;
		jobname =name;
	}
	public int getNClusters() {
		return nClusters;
		
	}
	public void setNClusters(int n) {
		nClusters =n;
	}
	public double getCostWSSSE() {
		return costWSSSE;
		
	}
	public void setSilhouette(double s) {
		silhouette =s;
	}
	public double getSilhouette() {
		return silhouette;
		
	}
	public void setDunn(double d) {
		dunn =d;
	}
	public double getDunn() {
		return dunn;
		
	}
	public void setCostWSSSE(double c) {
		costWSSSE =c;
	}
	public boolean getSelected() {
		return selected;
		
	}
	public void setSelected(boolean select) {
		selected = select;
	}
	public String getJobname() {
		return jobname;
	}
	public void setJobName(String name) {
		jobname = name;
	}
	public static int findElbowCluster(List<ClusteringPerformance> points) {
		if(points.size()==1) return points.get(0).getNClusters();
		List<Double> distSq = new ArrayList<Double>();
		ClusteringPerformance p1 = points.get(0);
		ClusteringPerformance p2 = points.get(points.size()-1);
		Vector2D v = new Vector2D(p2.getNClusters()-p1.getNClusters(),p2.getCostWSSSE()-p1.getCostWSSSE());
		Vector2D w;
		//first and last have dist of 0
		for(int i=0;i<points.size();i++)
		{
			ClusteringPerformance p = points.get(i);
			w = new Vector2D(p.getNClusters()-p1.getNClusters(),p.getCostWSSSE()-p1.getCostWSSSE());
			double vsq = v.dotProduct(v);
			double wsq = w.dotProduct(w);
			double proj = w.dotProduct(v);
			double dsq = wsq - proj*proj/vsq;
			distSq.add(dsq);
		}
		int minIndex = distSq.indexOf(Collections.max(distSq));
		int optClusterN = points.get(minIndex).getNClusters();
		return optClusterN;
	}
}
