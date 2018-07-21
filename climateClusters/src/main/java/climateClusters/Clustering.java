package climateClusters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.joda.time.DateTime;

import scala.Tuple2;

public class Clustering {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setMaster("local").setAppName("App");
		JavaSparkContext jsc = new JavaSparkContext(conf);
	    List<String> reqVars  = Arrays.asList("t", "ws", "rh");
	    DateTime startdate = new DateTime(2005, 1, 1, 1, 0, 0, 0);
		DateTime enddate = new DateTime(2008, 1, 1, 1,0,0,0);
		int startSeason =1;
		int endSeason=12;
	    String path = args[0];
	    JavaRDD<String> data = jsc.textFile(path);
	    
//	    JavaRDD<Vector> parsedData = data               // convert list to stream
//                .filter(line -> !isHeader(line)) //filter out header with function
//                .filter(line-> inDateRange(line,startdate,enddate))
//                .filter(line-> inSeasonRange(line,startSeason,endSeason))
//                .map(s->getValues(s,reqVars));
	    
	    JavaPairRDD<String,Vector> labeldata = data
	    		.filter(line -> !isHeader(line))
	    		.filter(line-> inDateRange(line,startdate,enddate))
                .filter(line-> inSeasonRange(line,startSeason,endSeason))
	    		.mapToPair(x -> getLabeledData(x,reqVars));
	    
	    for (Tuple2<String,Vector> line: labeldata.take(10)) {
	    	System.out.println("label:"+line._1+" data:"+line._2);
	    	}
	    JavaRDD<Vector> dataPoints = labeldata.values();
	    int numClusters = 12;
	    int numIterations = 20;
	    KMeansModel clusters = KMeans.train(dataPoints.rdd(), numClusters, numIterations);
	    
	    Map<Tuple2<Integer, String>, Long> clusterLabel = labeldata.mapToPair(f->classPoint(f,clusters))
	    		.countByValue();
	    
	    for (Map.Entry<Tuple2<Integer, String>, Long> entry : clusterLabel.entrySet())
	    {
	        System.out.println(entry.getKey() + "/" + entry.getValue());
	    }
	}
	private static Tuple2<Integer,String> classPoint(Tuple2<String,Vector> dl,KMeansModel clusters)
	{
		int clus = clusters.predict(dl._2);
		return new Tuple2<Integer,String>(clus,dl._1);
	}
	private static Tuple2<String,Vector> getLabeledData(String line,List<String> reqVariables)
	{
		String[] sarray = line.split(",");
		Vector data = getValues(line,reqVariables);
		String label = sarray[0]+"_"+sarray[1];
		return new Tuple2<String,Vector>(label, data);
	}
	private static boolean inSeasonRange(String line,int startmonth,int endmonth)
	{
		String[] sarray = line.split(",");
		int currentMonth = Integer.parseInt(sarray[3])+1;
		if(currentMonth>=startmonth&&currentMonth<=endmonth)return true;
		else return false;
	}
	private static boolean inDateRange(String line,DateTime startdate,DateTime enddate)
	{
		String[] sarray = line.split(",");
		int currentYr = Integer.parseInt(sarray[2]);
		int currentMonth = Integer.parseInt(sarray[3])+1;
		DateTime currentDate =new DateTime(currentYr, currentMonth, 1, 1,0,0);
		if(currentDate.isAfter(startdate)&&currentDate.isBefore(enddate))
		{
			return true;
		}
		else return false;
	}
	private static boolean isHeader(String line)
	{
		return line.contains("latitude");
	}
	
	private static Vector getValues(String line,List<String> reqVariables)
	{
	      String[] sarray = line.split(",");
	      double[] values = new double[reqVariables.size()];
	      for(int i=0;i<reqVariables.size();i++)
	      {
	    	  String var = reqVariables.get(i);
	    	  switch(var)
	    	  {
	    	  case "t":
	    		  values[i] = Double.parseDouble(sarray[4]);
	    		  break;
	    	  case "vp":
	    		  values[i] = Double.parseDouble(sarray[5]);
	    		  break;
	    	  case "rh":
	    		  values[i] = Double.parseDouble(sarray[6]);
	    		  break;
	    	  case "tmin":
	    		  values[i] = Double.parseDouble(sarray[7]);
	    		  break;
	    	  case "tmax":
	    		  values[i] = Double.parseDouble(sarray[8]);
	    		  break;
	    	  case "trange":
	    		  values[i] = Double.parseDouble(sarray[9]);
	    		  break;
	    	  case "pr":
	    		  values[i] = Double.parseDouble(sarray[10]);
	    		  break;
	    	  case "ws":
	    		  values[i] = Double.parseDouble(sarray[11]);
	    		  break;
	    	  }
	      }
	      
	      return Vectors.dense(values);
    }
}
