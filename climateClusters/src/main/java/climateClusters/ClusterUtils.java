package climateClusters;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.joda.time.LocalDate;

import scala.Tuple2;

public class ClusterUtils {
	public static void writeMapToFile(Map map,String out)
	{
		//binary output
		try
        {
               FileOutputStream fos =
                  new FileOutputStream(out);
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
	public static void writeMapToTextFile(Map<Integer, Long> map,String outpath) throws IOException
	{
		FileWriter fstream  = new FileWriter(outpath);
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write("cluster num" + ",count\n");
	    Iterator<Entry<Integer, Long>> it = map.entrySet().iterator();
	    while (it.hasNext() ) {

	        // the key/value pair is stored here in pairs
	        Map.Entry<Integer, Long> pairs = it.next();
	        out.write(pairs.getKey()+","+pairs.getValue() + "\n");
	    }
	    out.close();
	}
	public static String classPoint2(Tuple2<String,Vector> dl,KMeansModel clusters)
	{
		int clusNum = clusters.predict(dl._2);
		String pointinfo = clusNum+","+dl._1+","+dl._2;
		return pointinfo;
	}
	public static Tuple2<Integer,String> classPoint(Tuple2<String,Vector> dl,KMeansModel clusters)
	{
		int clus = clusters.predict(dl._2);
		return new Tuple2<Integer,String>(clus,dl._1);
	}
	public static Tuple2<String,Vector> getLabeledData(String line,List<String> reqVariables)
	{
		String[] sarray = line.split(",");
		Vector data = getValues(line,reqVariables);
		String label = sarray[0]+","+sarray[1];
		return new Tuple2<String,Vector>(label, data);
	}
	public static boolean inSeasonRange(String line,int startmonth,int endmonth)
	{
		String[] sarray = line.split(",");
		int currentMonth = Integer.parseInt(sarray[3])+1;
		if(currentMonth>=startmonth&&currentMonth<=endmonth)return true;
		else return false;
	}
	public static boolean inDateRange(String line,LocalDate startdate,LocalDate enddate)
	{
		String[] sarray = line.split(",");
		int currentYr = Integer.parseInt(sarray[2]);
		int currentMonth = Integer.parseInt(sarray[3])+1;
		LocalDate currentDate = new LocalDate(currentYr, currentMonth, 1); 
		if(currentDate.isAfter(startdate)&&currentDate.isBefore(enddate))
		{
			return true;
		}
		else return false;
	}
	public static boolean isHeader(String line)
	{
		return line.contains("latitude");
	}
	public static List<String> convertParams(List<String> reqVariables)
	{
		List<String> varShort = new ArrayList<String>();
		for(int i=0;i<reqVariables.size();i++)
	      {
	    	  String var = reqVariables.get(i);
	    	
	    	  switch(var)
	    	  {
	    	  case "RADIATION_SOLAR":
	    		  varShort.add("rs");
	    		  break;
	    	  case "WIND_DIRECTION":
	    		  varShort.add("wd");
	    		  break;
	    	  case "CLOUD_COVER":
	    		  varShort.add("cc");
	    		  break;
	    	  case "TEMPERATURE":
	    		  varShort.add("t");
	    		  break;
	    	  case "VAPOUR_PRESSURE":
	    		  varShort.add("vp");
	    		  break;
	    	  case "RELATIVE_HUMIDITY":
	    		  varShort.add("rh");
	    		  break;
	    	  case "TEMP_MIN":
	    		  varShort.add("tmin");
	    		  break;
	    	  case "TEMP_MAX":
	    		  varShort.add("tmax");
	    		  break;
	    	  case "TEMP_RANGE":
	    		  varShort.add("trange");
	    		  break;
	    	  case "PRECIPITATION":
	    		  varShort.add("pr");
	    		  break;
	    	  case "WIND_SPEED":
	    		  varShort.add("ws");
	    		  break;
	    	  case "ALTITUDE":
	    		  varShort.add("alt");
	    		  break;
	    	  case "LATITUDE":
	    		  varShort.add("lat");
	    		  break;
	    	  case "LONGITUDE":
	    		  varShort.add("lon");
	    		  break;
	    	  case "NONE":
	    		  break;
	    	  }
	      }
		
		return varShort;
	}
	public static Vector getValues(String line,List<String> reqVariables)
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
	    	  case "lat":
	    		  values[i] = Double.parseDouble(sarray[0]);
	    		  break;
	    	  case "lon":
	    		  values[i] = Double.parseDouble(sarray[1]);
	    		  break;
	    	  case "alt":
	    		  values[i] = Double.parseDouble(sarray[1]);
	    		  break;
	    	  }
	      }
	      
	      return Vectors.dense(values);
    }
}
