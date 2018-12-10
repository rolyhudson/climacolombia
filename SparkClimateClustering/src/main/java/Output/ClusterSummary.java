package Output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import Classification.DesignStrategy;
import Classification.ThermalZones;
import ReadFilter.Record;

public class ClusterSummary implements Serializable{
	private int clusterId;
	private Long count;
	private Vector centroid;
	private List<String> strategies = new ArrayList<String>();
	private double clusterUTCI;
	private double clusterIdeamCI;
	private double clusterTemp;
	private double clusterRh;
	private double clusterMaxTemp;
	private double clusterMaxRh;
	private double clusterMinTemp;
	private double clusterMinRh;
	private double clusterMaxTempRange;
	private double clusterMaxWS;
	private double clusterMinTempRange;
	private double clusterMinWS;
	private String workflowname;
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
	public String getWorkflowname() {
		return workflowname;
	}
	public void setWorkflowname(String name) {
		workflowname = name;
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
	//temps
	public void setClusterTemp(double t) {
		clusterTemp = t;
	}
	public double getClusterTemp() {
		return clusterTemp;
	}
	public void setClusterMaxTemp(double t) {
		clusterMaxTemp = t;
	}
	public double getClusterMaxTemp() {
		return clusterMaxTemp;
	}
	public void setClusterMinTemp(double t) {
		clusterMinTemp = t;
	}
	public double getClusterMinTemp() {
		return clusterMinTemp;
	}
	//rh
	public void setClusterRh(double rh) {
		clusterRh = rh;
	}
	public double getClusterRh() {
		return clusterRh;
	}
	
	public void setClusterMaxRh(double rh) {
		clusterMaxRh = rh;
	}
	public double getClusterMaxRh() {
		return clusterMaxRh;
	}
	public void setClusterMinRh(double rh) {
		clusterMinRh = rh;
	}
	public double getClusterMinRh() {
		return clusterMinRh;
	}
	//wind speed
	public void setClusterMaxWS(double ws) {
		clusterMaxWS = ws;
	}
	public double getClusterMaxWS() {
		return clusterMaxWS;
	}
	public void setClusterMinWS(double ws) {
		clusterMinWS = ws;
	}
	public double getClusterMinWS() {
		return clusterMinWS;
	}
	//temp range
	public void setClusterMaxTempRange(double t) {
		clusterMaxTempRange = t;
	}
	public double getClusterMaxTempRange() {
		return clusterMaxTempRange;
	}
	public void setClusterMinTempRange(double t) {
		clusterMinTempRange = t;
	}
	public double getClusterMinTempRange() {
		return clusterMinTempRange;
	}
	public void setClusterIdeamCI(double ideamci) {
		clusterIdeamCI = ideamci;
	}
	public Vector getCentroid() {
		return centroid;
	}
	public void setStrategies(List<DesignStrategy> s) {
		strategies = new ArrayList<String>();
		for(int i=0;i<s.size();i++) {
			strategies.add(s.get(i).getName());
		}
	}
	public List<String> getStrategies() {
		return strategies;
	}
	public static void reportClusterSummary(JavaRDD<Record> records,String output,double[][] comfort,Vector[] clusterCenters,SparkSession spark,ThermalZones tzs,String name) {
		List<ClusterSummary> summary = new ArrayList<ClusterSummary>();
	    Map<Integer,Long> clusterStats = records.map(f->f.getClusternum()).countByValue();
	    
	    for(int i=0;i<clusterCenters.length;i++) {
	    	ClusterSummary cs = new ClusterSummary();
	    	cs.setWorkflowname(name);
	    	cs.setCentroid(clusterCenters[i]);
	    	cs.setClusterId(i);
	    	cs.setCount(clusterStats.get(i));
	    	//COMFORT array is utci,ideamci,temp,rh,maxtemp,mintemp,maxtemprange,mintemprange,maxrh,minrh,maxws,minws
	    	cs.setClusterUTCI(comfort[i][0]);
	    	cs.setClusterIdeamCI(comfort[i][1]);
	    	cs.setClusterTemp(comfort[i][2]);
	    	cs.setClusterRh(comfort[i][3]);
	    	cs.setClusterMaxTemp(comfort[i][4]);
	    	cs.setClusterMinTemp(comfort[i][5]);
	    	cs.setClusterMaxTempRange(comfort[i][6]);
	    	cs.setClusterMinTempRange(comfort[i][7]);
	    	cs.setClusterMaxRh(comfort[i][8]);
	    	cs.setClusterMinRh(comfort[i][9]);
	    	cs.setClusterMaxWS(comfort[i][10]);
	    	cs.setClusterMinWS(comfort[i][11]);
	    	cs.setStrategies(tzs.testZones(new double[] {comfort[i][2],comfort[i][3]}));
	    	summary.add(cs);
	    }
	    Dataset<Row> clusteringDs = spark.createDataFrame(summary, ClusterSummary.class);
	    clusteringDs.toDF().write().json(output);
		}
}
