package climateClusters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.SparkSession;

public class ThermalZones implements Serializable{
	private List<DesignStrategy> strategies = new ArrayList<DesignStrategy>();
	public ThermalZones(SparkSession spark,String zonefile) {
		List<String> strategiesString = spark.read().textFile(zonefile).collectAsList();
		for(String s : strategiesString){
			String [] sarray = s.split(",");
			DesignStrategy ds = new DesignStrategy();
			ds.setName(sarray[0]);
			double t=0;
			double rh=0;//remember the units / scaling for humidity ratio
			List<double[]> pts = new ArrayList<double[]>();
			for(int i=1;i<sarray.length;i++) {
				if(i%2==0) {
					rh = Double.parseDouble(sarray[i]);
					double[] coord = {t,rh};
					pts.add(coord);
				}
				else {
					t = Double.parseDouble(sarray[i]);
				}
			}
			ds.setPoints(pts);
		}
	}
	public int[] testInclusion(Record r) {
		int[] inStrategies = {1,6,7};
		
		return inStrategies;
	}
}
