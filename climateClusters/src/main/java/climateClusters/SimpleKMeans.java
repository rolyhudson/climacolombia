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
	private JavaRDD<Vector> dataPoints;
	private Vector[] clusterCenters;
	public SimpleKMeans(String output,SparkSession spk,int numClusters,ThermalZones thermalzones,JavaRDD<Record> recorddata) {
		spark =spk;
//	    //get the vector attribute
		
		JavaRDD<Vector> dataPoints = recorddata.map(f->f.getVectorNorm());
	    //KMEANS specific
	    int numIterations = 20;
	    
	    List<ClusteringPerformance> performance = new ArrayList<ClusteringPerformance>();
	    ClusteringPerformance cp;
	    if(numClusters==0)
	    {
	    	for(int i=2;i<=10;i+=2)
	    	{
	    		KMeansModel clusterEp = KMeans.train(dataPoints.rdd(), i, numIterations);
	    		cp = new ClusteringPerformance();
	    		cp.setCost(clusterEp.computeCost(dataPoints.rdd()));
	    		cp.setNClustsers(i);
	    		performance.add(cp);
	    	}
	    	
	    	numClusters = ClusteringPerformance.findElbowCluster(performance);
	    }
	    //re-cluster with auto selected k or with preselected k
	    KMeansModel clusters = KMeans.train(dataPoints.rdd(), numClusters, numIterations);
	    clusterCenters = clusters.clusterCenters();
	    cp = new ClusteringPerformance();
    	cp.setCost(clusters.computeCost(dataPoints.rdd()));
		cp.setNClustsers(numClusters);
		cp.setSelected(true);
		performance.add(cp);

    	//sorted by year and with cluster id
	    JavaRDD<Record> records = recorddata.map(f->ClusterUtils.classify(f,clusters))
	    		.sortBy(f-> f.getDatetime().getYear(), true, 20);
	    
	    //generate the toplevel reports
	    Dataset<Row> performanceDs = spark.createDataFrame(performance, ClusteringPerformance.class);
    	performanceDs.toDF().write().json(output+"/stats/performanceDF");
	    thermalzones.reportInclusion(recorddata,spark,output+"/stats/strategyStats");
	    reportClusterSummary(records,output+"/stats/clusterStats");
	    
	    //split results by year month
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
	    		
	    		JavaRDD<String> ymrecords = temporalRecords.map(f->f.toString());
	    		ymrecords.saveAsTextFile(path);
	    		reportClusterSummary(temporalRecords,path+"/stats/clusterStats");
	    		thermalzones.reportInclusion(temporalRecords,spark,path+"/stats/strategyStats");
	    	}
	    	
	    }
	    
	    
	    spark.stop();
	}
	private void reportClusterSummary(JavaRDD<Record> records,String output) {
	List<ClusterSummary> summary = new ArrayList<ClusterSummary>();
    Map<Integer,Long> clusterStats = records.map(f->f.getClusternum()).countByValue();
    
    for(int i=0;i<clusterCenters.length;i++) {
    	ClusterSummary cs = new ClusterSummary();
    	cs.setCentroid(clusterCenters[i]);
    	cs.setClusterId(i);
    	cs.setCount(clusterStats.get(i));
    	summary.add(cs);
    }
    Dataset<Row> clusteringDs = spark.createDataFrame(summary, ClusterSummary.class);
    clusteringDs.toDF().write().json(output);
	}
}

