package climateClusters;

import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.SparkSession;


import climateClusters.ClusterIndices;
import climateClusters.ClusteringPerformance;
public class KMeansPerformance {
	private int numIterations = 20;
	private double[] bds;
	private double[] bddunn;
	private double[] costs;
    int maxclusters=60;
    int minClusters=2;
    int nclusters;
    List<ClusteringPerformance> performance;
    JavaRDD<Vector> dataPoints;
    private SparkSession spark;
    String method;
    
	public KMeansPerformance(SparkSession spk,int numClusters,JavaRDD<Record> recorddata,String method) {
	this.spark =spk;
	this.nclusters = numClusters;	
	dataPoints = recorddata.map(f->f.getVectorNorm());
	dataPoints.cache();
//	if(method.equals("BISECTING_K_MEANS_AND_K_MEANS"))this.method = "BISECTING_K_MEANS";
//	else this.method=method;
	this.method=method;
	runPerformance();
	storePerformance();
	}
	private void runPerformance() {
	 if(nclusters!=0) {
	    	maxclusters=nclusters+1;
		    minClusters=nclusters;
	    }
	    bds = ClusterIndices.BDSilhouette(dataPoints,minClusters,maxclusters,numIterations,spark,this.method);
	    bddunn =ClusterIndices.BDDunn(dataPoints,minClusters, maxclusters, numIterations, spark,this.method);
	    costs = ClusterIndices.costs(dataPoints, minClusters, maxclusters, numIterations, spark,this.method);
	}
	private void storePerformance() {
		 //store the performance stats
	    performance = new ArrayList<ClusteringPerformance>();
	    ClusteringPerformance cp;
	    for(int i=0;i<bds.length;i++) {
	    	cp = new ClusteringPerformance(i+minClusters,costs[i],bds[i],bddunn[i],false);
    		performance.add(cp);
    	}
	    nclusters = ClusteringPerformance.findElbowCluster(performance);
	    Optional<ClusteringPerformance> cpSel = performance.stream().filter(p->p.getNClusters()==nclusters).findFirst();
	    
	    	if(cpSel.isPresent())
			{
	    		ClusteringPerformance w = cpSel.get();
				w.setSelected(true);
			}
			else
			{
				performance.get(0).setSelected(true);
			}
	    
	}
	public int getNclusters() {
		return this.nclusters;
	}
	public List<ClusteringPerformance> getPerformance(){
		return this.performance;
	}
	public double[] getBds() {
		return this.bds;
	}
	public double[] getBddunn() {
		return this.bddunn;
	}
    public double[] getCosts() {
    	return this.costs;
    }
    
}
