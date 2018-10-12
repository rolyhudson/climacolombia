package climateClusters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

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
	public static void reportClusterSummary(JavaRDD<Record> records,String output,double[][] comfort,Vector[] clusterCenters,SparkSession spark) {
		List<ClusterSummary> summary = new ArrayList<ClusterSummary>();
	    Map<Integer,Long> clusterStats = records.map(f->f.getClusternum()).countByValue();
	    
	    for(int i=0;i<clusterCenters.length;i++) {
	    	ClusterSummary cs = new ClusterSummary();
	    	cs.setCentroid(clusterCenters[i]);
	    	cs.setClusterId(i);
	    	cs.setCount(clusterStats.get(i));
	    	cs.setClusterUTCI(comfort[i][0]);
	    	cs.setClusterIdeamCI(comfort[i][1]);
	    	summary.add(cs);
	    }
	    Dataset<Row> clusteringDs = spark.createDataFrame(summary, ClusterSummary.class);
	    clusteringDs.toDF().write().json(output);
		}
}
