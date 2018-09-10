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


public class SimpleKMeans {
	
	public SimpleKMeans(String output,SparkSession spark,ClusterParams clusterParams,JavaRDD<Record> recorddata) {

//	    //get the vector attribute
	    JavaRDD<Vector> dataPoints = recorddata.map(f->f.getVectorNorm());

	    int numIterations = 20;
	    
	    int numClusters = clusterParams.getNClusters();
	    List<ClusterPerformance> performance = new ArrayList<ClusterPerformance>();
	   
	    ClusterPerformance cp;
	    if(numClusters==0)
	    {
	    	for(int i=2;i<=10;i+=2)
	    	{
	    		KMeansModel clusterEp = KMeans.train(dataPoints.rdd(), i, numIterations);
	    		cp = new ClusterPerformance();
	    		cp.setCost(clusterEp.computeCost(dataPoints.rdd()));
	    		cp.setNClustsers(i);
	    		performance.add(cp);
	    	}
	    	ClusterPerformance mincp = performance.stream()
	    			.min(Comparator.comparing(ClusterPerformance::getCost))
	    			.orElseThrow(NoSuchElementException::new);
	    	mincp.setSelected(true);
	    	numClusters = mincp.getNClusters();
	    }
	    //re-cluster with auto selected k or with preselected k
	    KMeansModel clusters = KMeans.train(dataPoints.rdd(), numClusters, numIterations);
	    cp = new ClusterPerformance();
    	cp.setCost(clusters.computeCost(dataPoints.rdd()));
		cp.setNClustsers(numClusters);
		performance.add(cp);

    	Dataset<Row> performanceDs = spark.createDataFrame(performance, ClusterPerformance.class);
    	performanceDs.toDF().write().json(output+"/performanceDF");

    	//sorted by year and with cluster id
	    JavaRDD<Record> records = recorddata.map(f->ClusterUtils.classify(f,clusters))
	    		.sortBy(f-> f.getDatetime().getYear(), true, 20);
	    
	    //generate the clustering summary
	    List<ClusterSummary> summary = new ArrayList<ClusterSummary>();
	    Map<Integer,Long> clusterStats = records.map(f->f.getClusternum()).countByValue();
	    Vector[] clusterCenters = clusters.clusterCenters();
	    for(int i=0;i<clusterCenters.length;i++) {
	    	ClusterSummary cs = new ClusterSummary();
	    	cs.setCentroid(clusterCenters[i]);
	    	cs.setClusterId(i);
	    	cs.setCount(clusterStats.get(i));
	    	summary.add(cs);
	    }
	    Dataset<Row> clusteringDs = spark.createDataFrame(summary, ClusterSummary.class);
	    clusteringDs.toDF().write().json(output+"/summaryDF");
	    
	    //split results by year month
	    int maxyear = records.max(new YearComparator()).getDatetime().getYear();
	    int minyear = records.min(new YearComparator()).getDatetime().getYear();
	    for(int y = minyear;y<=maxyear;y++)
	    {
	    	for(int m=1;m<=12;m++)
	    	{
	    		final int yr = y;
	    		final int mon = m;
	    		JavaRDD<String> ymrecords = recorddata.filter(f->ClusterUtils.matchYearMonth(f, yr, mon)).map(f->ClusterUtils.classifyAsString(f,clusters));
	    		ymrecords.saveAsTextFile(output+"/"+y+"/"+m);
	    	}
	    	
	    }
	    for(int i=0;i<numClusters;i++)
	    {
	    	int cnum =i;
	    	JavaRDD<Record> inClusterRecords = records.filter(f->ClusterUtils.matchClusterNum(f,cnum));
	    	//test cluster datapoints against strategies
	    }
	    
	    spark.stop();
	}

}
//// Reduce function adding two integers, will be used to reduce
//Function2<Integer, Integer, Integer> reduceFunc = new Function2<Integer, Integer, Integer>() {
//  @Override public Integer call(Integer i1, Integer i2) {
//      return i1 + i2;
//  }
//};
////spark.createDataset(outputclusters.rdd(), evidence$5)
////simple pair with cluster # as int and string with cluster description
//JavaPairRDD<Integer,String> count = labeldata.mapToPair(f->ClusterUtils.classPoint(f,clusters));
//
//JavaPairRDD<Integer, Iterable<String>> sum = count.groupByKey();
//sum.saveAsTextFile(output+"/clusterGroups");
//try {
//	ClusterUtils.writeMapToTextFile(sum,output+"/clusterCount.txt");
//} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//for (Map.Entry<Integer,Long> entry : sum.entrySet())
//    {
//        System.out.println(entry.getKey() + "/" + entry.getValue());
//    }
