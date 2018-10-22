package climateClusters;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import scala.Tuple2;
import climateClusters.DesignStrategy;
import climateClusters.ClusterUtils;
import  climateClusters.StrategySummary;
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
			strategies.add(ds);
		}
	}
	public Record testZones(Record r) {
		for(DesignStrategy ds : strategies) {
			if(ClusterUtils.isPointInPolygon(r.getPsychrometricPoint(), ds.getPoints()))
			{
				r.addStrategy(ds.getName());		
			}
			
		}
		return r;
	}
	public List<DesignStrategy> testZones(double[] ppoint) {
		List<DesignStrategy> applicable = new ArrayList<DesignStrategy>();
		for(DesignStrategy ds : strategies) {
			if(ClusterUtils.isPointInPolygon(ppoint, ds.getPoints()))
			{
				applicable.add(ds);		
			}
			
		}
		return applicable;
	}
	public List<String> testZonesNames(double[] ppoint) {
		List<String> applicable = new ArrayList<String>();
		for(DesignStrategy ds : strategies) {
			if(ClusterUtils.isPointInPolygon(ppoint, ds.getPoints()))
			{
				applicable.add(ds.getName());		
			}
			
		}
		return applicable;
	}
	public void reportMultiInclusion(JavaRDD<Record> records,SparkSession spark,String output) {
		long countR = records.count();
		//JavaRDD<Record> recordsStrategy = records.map(f->testZones(f));
		JavaRDD<String> strat = records.flatMap(f->f.getInStrategies().iterator());
		JavaPairRDD<String, Integer> strategyFreq = strat.mapToPair(f->{
			return new Tuple2<String,Integer>( f, 1);
		});
		
		Map<String, Long> strategyCount = strategyFreq.countByKey();
		List<StrategySummary> summary = new ArrayList<StrategySummary>();
		//set the percentages
		for (Map.Entry<String, Long> entry : strategyCount.entrySet()) {
			System.out.println("Item : " + entry.getKey() + " Count : " + entry.getValue().intValue());
			StrategySummary ss = new StrategySummary();
			ss.setName(entry.getKey());
			ss.setCount(entry.getValue().intValue());
			ss.setPercent(entry.getValue().doubleValue()/countR*100.0);
			summary.add(ss);
		}
		Dataset<Row> strategyDF = spark.createDataFrame(summary, StrategySummary.class);
		strategyDF.toDF().write().mode(SaveMode.Overwrite).json(output);
	}
	public void reportInclusion(JavaRDD<Record> records,SparkSession spark,String output) {
		long countR = records.count();
				
		JavaPairRDD<String, Integer> strategyFreq = records.mapToPair(f->{
			//this should return multiple strategies if overlap is permitted
			for(DesignStrategy ds : strategies) {
				if(ClusterUtils.isPointInPolygon(f.getPsychrometricPoint(), ds.getPoints()))
				{
					return new Tuple2<String,Integer>( ds.getName(), 1);		
				}
				
			}
			return new Tuple2<String,Integer>("No strategy found", 1);
		}
		);
		Map<String, Long> strategyCount = strategyFreq.countByKey();
		List<StrategySummary> summary = new ArrayList<StrategySummary>();
		//set the percentages
		for (Map.Entry<String, Long> entry : strategyCount.entrySet()) {
			System.out.println("Item : " + entry.getKey() + " Count : " + entry.getValue().intValue());
			StrategySummary ss = new StrategySummary();
			ss.setName(entry.getKey());
			ss.setCount(entry.getValue().intValue());
			ss.setPercent(entry.getValue().doubleValue()/countR*100.0);
			summary.add(ss);
		}
		Dataset<Row> strategyDF = spark.createDataFrame(summary, StrategySummary.class);
		strategyDF.toDF().write().mode(SaveMode.Overwrite).json(output);
	}
	
}
