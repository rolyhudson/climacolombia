package climateClusters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.SparkSession;
import org.joda.time.LocalDate;

import scala.Tuple2;

public class SimpleKMeans {
	
	public SimpleKMeans(String input,String output,SparkSession spark) {
//		LocalDate start = clusterParams.getStartDate();
//		LocalDate end = clusterParams.getEndDate();
//		int seasonStart = clusterParams.getSeasonStartMonth();
//		int seasonEnd = clusterParams.getSeasonEndMonth();
//		List<String> reqVars = ClusterUtils.convertParams(clusterParams.getVariables());
		LocalDate start = new LocalDate(2004,1,1);
		LocalDate end = new LocalDate(2010,1,1);
		int seasonStart =1;
		int seasonEnd =12;
		List<String> reqVars = Arrays.asList("rh", "t", "ws");
	    JavaRDD<String> data = spark.read().textFile(input).toJavaRDD();
		    JavaPairRDD<String,Vector> labeldata = data
	    		.filter(line -> !ClusterUtils.isHeader(line))
	    		.filter(line -> ClusterUtils.inDateRange(line,start,end))
	            .filter(line -> ClusterUtils.inSeasonRange(line,seasonStart,seasonEnd))
	    		.mapToPair(x -> ClusterUtils.getLabeledData(x,reqVars));
		    //need to throw an error if samples are not found
	    for (Tuple2<String,Vector> line: labeldata.take(10)) {
	    	System.out.println("label:"+line._1+" data:"+line._2);
	    	
	    	}
	    JavaRDD<Vector> dataPoints = labeldata.values();
	    int numClusters = 10;
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
	    	    
	    JavaRDD<String> outputclusters = labeldata.map(f->ClusterUtils.classPoint2(f,clusters));
	    outputclusters.saveAsTextFile(output);
	    
//	    JavaPairRDD<Integer,String> count = labeldata.mapToPair(f->ClusterUtils.classPoint(f,clusters));
//	    Map<Integer,Long> sum = count.countByKey();
//	    try {
//			ClusterUtils.writeMapToTextFile(sum,output+"/clusterCount.txt");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    for (Map.Entry<Integer,Long> entry : sum.entrySet())
//		    {
//		        System.out.println(entry.getKey() + "/" + entry.getValue());
//		    }

	    spark.stop();
	}
	
}
