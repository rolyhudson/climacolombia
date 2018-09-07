package climateClusters;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.SparkSession;
import org.joda.time.LocalDate;

import scala.Tuple2;

public class SimpleKMeans {
	
	public SimpleKMeans(String input,String output,SparkSession spark,ClusterParams clusterParams) {
		LocalDate start = clusterParams.getStartDate();
		LocalDate end = clusterParams.getEndDate();
		int seasonStart = clusterParams.getSeasonStartMonth();
		int seasonEnd = clusterParams.getSeasonEndMonth();
		List<String> reqVars = ClusterUtils.convertParams(clusterParams.getVariables());
		List<double[]> bound = clusterParams.getSelectionCoords();
		int numClusters = clusterParams.getNClusters();
		
	    JavaRDD<String> data = spark.read().textFile(input).toJavaRDD();
	    
		    JavaRDD<Record> recorddata = data
	    		.filter(line -> !ClusterUtils.isHeader(line))
	    		.filter(line -> ClusterUtils.inDateRange(line,start,end))
	            .filter(line -> ClusterUtils.inSeasonRange(line,seasonStart,seasonEnd))
	            .filter(line -> ClusterUtils.requiredPoint(line, bound))
	            .map(line -> ClusterUtils.createRecord(line, reqVars));
	    		//.mapToPair(x -> ClusterUtils.getLabeledData(x,reqVars));
		    //need to throw an error if samples are not found
	    for (Record r: recorddata.take(10)) {
	    	System.out.println("loc:"+r.getLocation()+" data:"+r.getVector());
	    	
	    	}
	    //get the vector attribute
	    JavaRDD<Vector> dataPoints = recorddata.map(f->f.getVector());
	    
	    int numIterations = 20;
	    
	    List<Double> scores = new ArrayList<Double>();
	    
	    if(numClusters==0)
	    {
	    	for(int i=2;i<=100;i+=2)
	    	{
	    		KMeansModel clusterEp = KMeans.train(dataPoints.rdd(), i, numIterations);
	    		scores.add(clusterEp.computeCost(dataPoints.rdd()));
	    	}
	    	numClusters = scores.indexOf(Collections.min(scores));
	    }
	    
	    KMeansModel clusters = KMeans.train(dataPoints.rdd(), numClusters, numIterations);
	    
	    //simple string with cluster # and description
	    JavaRDD<String> outputclusters = recorddata.map(f->ClusterUtils.classifyAsString(f,clusters));
	    outputclusters.saveAsTextFile(output);
	    
	    JavaRDD<Record> records = recorddata.map(f->ClusterUtils.classify(f,clusters))
	    		.sortBy(f-> f.getDatetime().getYear(), true, 20);
	    
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
