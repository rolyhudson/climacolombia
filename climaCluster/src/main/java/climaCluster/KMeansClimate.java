package climaCluster;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
public class KMeansClimate {
	
	public static void main(String[] args) {
		
		
		SparkConf conf = new SparkConf().setMaster("local").setAppName("App");
	    //val conf = new SparkConf().setMaster("local").setAppName("myApp")
	    JavaSparkContext jsc = new JavaSparkContext(conf);
	    List<String> reqVars  = Arrays.asList("t", "ws", "rh");
		Date startdate = getDate(2005, 1, 1, 1);
		Date enddate = getDate(2008, 1, 1, 1);
		int startSeason =0;
		int endSeason=11;
	    String path = args[0];
	    JavaRDD<String> data = jsc.textFile(path);
	    List<String> head = data.take(10);
	    JavaRDD<Vector> parsedData = data               // convert list to stream
                .filter(line -> !isHeader(line)) //filter out header with function
                .filter(line-> inDateRange(line,startdate,enddate))
                .filter(line-> inSeasonRange(line,startSeason,endSeason))
                .map(s->getValues(s,reqVars));
                //.collect(Collectors.toList());
	    
	    parsedData.cache();
	    for (Vector line: parsedData.take(10)) {
	    	System.out.println(line);
	    	}

		 // Cluster the data into two classes using KMeans
		    int numClusters = 12;
		    int numIterations = 20;
		    KMeansModel clusters = KMeans.train(parsedData.rdd(), numClusters, numIterations);

		    System.out.println("Cluster centers:");
		    for (Vector center: clusters.clusterCenters()) {
		      System.out.println(" " + center);
		    }
		    double cost = clusters.computeCost(parsedData.rdd());
		    System.out.println("Cost: " + cost);

		    // Evaluate clustering by computing Within Set Sum of Squared Errors
		    double WSSSE = clusters.computeCost(parsedData.rdd());
		    System.out.println("Within Set Sum of Squared Errors = " + WSSSE);

		    // Save and load model
		    clusters.save(jsc.sc(), "target/org/apache/spark/JavaKMeansExample/KMeansModel");
		    KMeansModel sameModel = KMeansModel.load(jsc.sc(),
		      "target/org/apache/spark/JavaKMeansExample/KMeansModel");
	    jsc.stop();

	}
	private static boolean inSeasonRange(String line,int startmonth,int endmonth)
	{
		String[] sarray = line.split(",");
		int currentMonth = Integer.parseInt(sarray[3]);
		if(currentMonth>=startmonth&&currentMonth<=endmonth)return true;
		else return false;
	}
	private static boolean inDateRange(String line,Date startdate,Date enddate)
	{
		String[] sarray = line.split(",");
		int currentYr = Integer.parseInt(sarray[2]);
		int currentMonth = Integer.parseInt(sarray[3]);
		Date currentDate = getDate(currentYr, currentMonth, 1, 1);
		if(currentDate.after(startdate)&&currentDate.before(enddate))
		{
			return true;
		}
		else return false;
	}
	private static boolean isHeader(String line)
	{
		return line.contains("latitude");
	}
	public static Date getDate(int year, int month, int day, int hour) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
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
