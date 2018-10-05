package climateClusters;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.joda.time.DateTime;

import climateClusters.Record;
import climateClusters.ThermalZones;
import climateClusters.ClusteringPerformance;
import climateClusters.ClusterUtils;
import climateClusters.ClusterSummary;
public class SimpleKMeans {
	private SparkSession spark;
	private Vector[] clusterCenters;
	public SimpleKMeans(String output,SparkSession spk,int numClusters,ThermalZones thermalzones,JavaRDD<Record> recorddata) {
		spark =spk;
//	    //get the vector attribute
		
		JavaRDD<Vector> dataPoints = recorddata.map(f->f.getVectorNorm());
	    //KMEANS specific
	    int numIterations = 20;
	    
	    
	    //numClusters=0;
	    double[] bds;
	    double[] bddunn;
	    double[] costs;
	    dataPoints.cache();
	    int maxclusters=20;
	    int minClusters=2;
	    if(numClusters==0)
	    {

		    maxclusters=20;
		    minClusters=2;
	    }
	    else {
	    	maxclusters=numClusters+1;
		    minClusters=numClusters;
	    }
	    bds = ClusterUtils.BDSilhouette(dataPoints,minClusters,maxclusters,numIterations,spark);
	    bddunn = ClusterUtils.BDDunn(dataPoints,minClusters, maxclusters, numIterations, spark);
	    costs = ClusterUtils.costs(dataPoints, minClusters, maxclusters, numIterations, spark);
	    
	    //store the performance stats
	    List<ClusteringPerformance> performance = new ArrayList<ClusteringPerformance>();
	    ClusteringPerformance cp;
	    for(int i=0;i<bds.length;i++) {
	    	 cp = new ClusteringPerformance(i+minClusters,costs[i],bds[i],bddunn[i],true);
	    	
    		performance.add(cp);
    		
    	}
	    numClusters = ClusteringPerformance.findElbowCluster(performance);
	  //re-cluster with auto selected k or with preselected k
	    KMeansModel clusters = KMeans.train(dataPoints.rdd(), numClusters, numIterations);
	    clusterCenters = clusters.clusterCenters();

    	//sorted by year and with cluster id and associated strategies
	    JavaRDD<Record> records = recorddata.map(f->ClusterUtils.classify(f,clusters))
	    		.map(r->thermalzones.testZones(r))
	    		.sortBy(f-> f.getDatetime().getYear(), true, 20);
	    
	    
	    //get the psuedoF slow
	   //double pf = ClusterUtils.pseudoF(records, clusters);
	    
	    //generate the toplevel reports
	    Dataset<Row> performanceDs = spark.createDataFrame(performance, ClusteringPerformance.class);
    	performanceDs.toDF().write().json(output+"/stats/performanceDF");
	    thermalzones.reportMultiInclusion(records,spark,output+"/stats/strategyStats");
	    double[][] comfortIndices = ComfortIndices.getComfortIndicesClusters(records,clusters);
	    reportClusterSummary(records,output+"/stats/clusterStats",comfortIndices);
	    
	    //split results by year month this could be done from DB in the dashboard
	    int maxyear = records.max(new YearComparator()).getDatetime().getYear();
	    int minyear = records.min(new YearComparator()).getDatetime().getYear();
	    String path="";
	    for(int y = minyear;y<=maxyear;y++)
	    {
	    	for(int m=1;m<=12;m++)
	    	{
	    		final int yr = y;
	    		final int mon = m;
	    		path=output+"/"+y+"/"+m;
	    		JavaRDD<Record> temporalRecords = records.filter(f->ClusterUtils.matchYearMonth(f, yr, mon));
	    		
	    		JavaRDD<String> ymrecords = temporalRecords.map(f->f.toJSONString());
	    		ymrecords.saveAsTextFile(path);
	    		//could add indices per cluster per time step
	    		reportClusterSummary(temporalRecords,path+"/stats/clusterStats",comfortIndices);
	    		thermalzones.reportMultiInclusion(temporalRecords,spark,path+"/stats/strategyStats");
	    		for(int c=0;c<=numClusters;c++) {
	    			final int cnum =c;
	    			JavaRDD<Record> clusterRecords = temporalRecords.filter(f->ClusterUtils.matchCluster(f,cnum));
	    			thermalzones.reportMultiInclusion(clusterRecords,spark,path+"/stats/cluster"+cnum+"Stats");
	    		}
	    	}
	    	
	    }
	    
	    
	    spark.stop();
	}
	private void reportClusterSummary(JavaRDD<Record> records,String output,double[][] comfort) {
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

