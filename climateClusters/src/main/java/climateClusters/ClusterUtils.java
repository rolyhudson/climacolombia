package climateClusters;

import java.io.BufferedWriter;


import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.feature.Normalizer;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.joda.time.DateTime;

import climateClusters.Record;
import scala.Tuple2;

public class ClusterUtils implements Serializable {
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
	public static void datasetFromList(String schemaString)
	{
		List<StructField> fields = new ArrayList<>();
		for (String fieldName : schemaString.split(" ")) {
			  StructField field = DataTypes.createStructField(fieldName, DataTypes.StringType, true);
			  fields.add(field);
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
	public static Record classify(Record r,KMeansModel clusters)
	{
		r.setClusternum(clusters.predict(r.getVectorNorm()));
		return r;
	}
	public static Record normaliseVector(Record r)
	{
		Normalizer n = new Normalizer();
		r.setVectorNorm(n.transform(r.getVector()));
		return r;
	}
	
	public static boolean matchYearMonth(Record r,int yr, int m)
	{
		if(r.getDatetime().getYear()==yr && r.getDatetime().getMonthOfYear()==m)
		{
			return true;
		}
		else return false;
	}
	public static boolean matchCluster(Record r,int c)
	{
		if(r.getClusternum()==c)
		{
			return true;
		}
		else return false;
	}
	public static boolean matchClusterNum(Record r,int cnum)
	{
		if(r.getClusternum()==cnum) return true;
		else return false;
	}
	public static String classifyAsString(Record r,KMeansModel clusters)
	{
		r.setClusternum(clusters.predict(r.getVectorNorm()));
		return r.toString();
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
	public static Tuple2<Integer,Record> classRecord(Record r){
		return new Tuple2<Integer,Record>(r.getClusternum(),r);
	}
//	public static Tuple2<String,Vector> getLabeledData(String line,List<String> reqVariables)
//	{
//		String[] sarray = line.split(",");
//		Vector data = getValues(line,reqVariables);
//		String label = sarray[0]+","+sarray[1];
//		return new Tuple2<String,Vector>(label, data);
//	}
	public static boolean inSeasonRange(String line,int startmonth,int endmonth)
	{
		String[] sarray = line.split(",");
		DateTime currentDate =  DateTime.parse(sarray[3]);
		int currentMonth = currentDate.getMonthOfYear();
		if(currentMonth>=startmonth&&currentMonth<=endmonth)return true;
		else return false;
	}
	public static boolean inHourlyRange(String line,int starthour,int endhour)
	{
		String[] sarray = line.split(",");
		DateTime currentDate =  DateTime.parse(sarray[3]);
		int currentHour = currentDate.getHourOfDay();
		if(currentHour>=starthour&&currentHour<=endhour)return true;
		else return false;
	}
	public static Record createRecord(String line,List<String> reqVariables) {
		Record r = new Record();
		String[] sarray = line.split(",");
		double[] p = {Double.parseDouble(sarray[0]),Double.parseDouble(sarray[1])};
		r.setLocation(p);
		r.setElevation(Double.parseDouble(sarray[2]));
		r.setReqVars(reqVariables);
		Vector data = getValues(line,reqVariables);
		DateTime currentDate =  DateTime.parse(sarray[3]);
		r.setDatetime(currentDate);
		r.setVector(data);
		//get all other vars
		List<Double> v = new ArrayList<Double>();
		double[] psychrometricPoint = new double[2];
		for(int i=4;i<sarray.length;i++)
		{
			if(i==4) psychrometricPoint[0] = Double.parseDouble(sarray[i]);
			if(i==6) psychrometricPoint[1] = Double.parseDouble(sarray[i]);
			v.add(Double.parseDouble(sarray[i]));
		}
		Vector allVar = Vectors.dense(v.stream().mapToDouble(Double::doubleValue).toArray());
		r.setVectorAllVar(allVar);
		r.setPsychrometricPoint(psychrometricPoint);
		return r;
	}
	public static boolean inDateRange(String line,DateTime startdate,DateTime enddate)
	{
		String[] sarray = line.split(",");
		
		DateTime currentDate =  DateTime.parse(sarray[3]);
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
	public static boolean requiredPoint(String line,List<double[]> bound) {
		String[] sarray = line.split(",");
		double lat = Double.parseDouble(sarray[0]);
		double lon = Double.parseDouble(sarray[1]);
		double[] pt = {lat,lon};
		return isPointInPolygon(pt, bound);
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
	
	public static boolean isPointInPolygon(double[] point, List<double[]> vs)
    {
        // ray-casting algorithm based on
        // http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html

        double x = point[0], y = point[1];

        boolean inside = false;
        for (int i = 0, j = vs.size() - 1; i < vs.size(); j = i++)
        {
            double xi = vs.get(i)[0], yi = vs.get(i)[1];
            double xj = vs.get(j)[0], yj = vs.get(j)[1];

            boolean intersect = ((yi > y) != (yj > y))
                && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }

        return inside;
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
	    		  values[i] = Double.parseDouble(sarray[2]);
	    		  break; 
	    	  case "ws":
	    		  values[i] = Double.parseDouble(sarray[11]);
	    		  break;
	    	  }
	      }
	      
	      return Vectors.dense(values);
    }
}
