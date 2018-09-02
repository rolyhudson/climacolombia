package climateClusters;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.SparkSession;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import scala.Tuple2;

public class Clustering {

	public static void main(String[] args) {
		// on EMR use
//		SparkSession spark = SparkSession
//                .builder()
//                .appName("SparkJob")
//                .getOrCreate();
		
		//local debug
		SparkSession spark = SparkSession.builder()
				  .master("local")
				  .appName("SparkJob")
				  .getOrCreate();
		//this is all incoming with args
//		private List<Variables> variables; 
//		private LocalDate start;
//		private LocalDate end;
//		private int seasonStartMonth;
//		private int seasonStartDay;
//		private int seasonEndMonth;
//		private int seasonEndDay;
//		private int dayStartHour;
//		private int dayEndHour;
//		private List<double[]> selectionCoords;
	    List<String> reqVars  = Arrays.asList("t", "ws", "rh");
	    DateTime startdate = new DateTime(2005, 1, 1, 1, 0, 0, 0);
		DateTime enddate = new DateTime(2008, 1, 1, 1,0,0,0);
		int startSeason =1;
		int endSeason=2;
	    
	    JavaRDD<String> data = spark.read().textFile(args[0]).toJavaRDD();
	    
   	    JavaPairRDD<String,Vector> labeldata = data
	    		.filter(line -> !isHeader(line))
	    		.filter(line -> inDateRange(line,startdate,enddate))
                .filter(line -> inSeasonRange(line,startSeason,endSeason))
	    		.mapToPair(x -> getLabeledData(x,reqVars));
	    
	    for (Tuple2<String,Vector> line: labeldata.take(10)) {
	    	System.out.println("label:"+line._1+" data:"+line._2);
	    	}
	    JavaRDD<Vector> dataPoints = labeldata.values();
	    int numClusters = 12;
	    int numIterations = 20;
	    KMeansModel clusters = KMeans.train(dataPoints.rdd(), numClusters, numIterations);
	    
	    JavaRDD<String> outputclusters = labeldata.map(f->classPoint2(f,clusters));
	    outputclusters.saveAsTextFile(args[1]);
//	    Map<Tuple2<Integer, String>, Long> clusterLabel = labeldata.mapToPair(f->classPoint(f,clusters))
//	    		.countByValue();
//	    for (Map.Entry<Tuple2<Integer, String>, Long> entry : clusterLabel.entrySet())
//	    {
//	        System.out.println(entry.getKey() + "/" + entry.getValue());
//	    }
	    
	    spark.stop();
	}
	private static void writeMapToFile(Map map)
	{
		try
        {
               FileOutputStream fos =
                  new FileOutputStream("hashmap.ser");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               oos.writeObject(map);
               oos.close();
               fos.close();
               System.out.printf("Serialized HashMap data is saved in hashmap.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }
	}
	private static String classPoint2(Tuple2<String,Vector> dl,KMeansModel clusters)
	{
		int clusNum = clusters.predict(dl._2);
		String pointinfo = clusNum+","+dl._1+","+dl._2;
		return pointinfo;
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
		String label = sarray[0]+","+sarray[1];
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
