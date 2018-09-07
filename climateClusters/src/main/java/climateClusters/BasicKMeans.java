package climateClusters;

import java.util.List;

import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;

import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;

import static org.apache.spark.sql.functions.*;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.callUDF;
import org.joda.time.LocalDate;
import java.sql.Timestamp;

public class BasicKMeans {
	
	final List<double[]> bound;
	public BasicKMeans(String input,String output,SparkSession spark,ClusterParams clusterParams) {
		Timestamp start = new Timestamp(clusterParams.getStartDate().toDateTimeAtCurrentTime().getMillis());
		Timestamp end = new Timestamp(clusterParams.getEndDate().toDateTimeAtCurrentTime().getMillis());
		int seasonStart = clusterParams.getSeasonStartMonth();
		int seasonEnd = clusterParams.getSeasonEndMonth();
		List<String> reqVars = ClusterUtils.convertParams(clusterParams.getVariables());
		this.bound = clusterParams.getSelectionCoords();
		
		Dataset<Row> climateData = spark.read().format("csv")
				
				  .option("sep", ",")
				  .option("inferSchema", "true")
				  .option("header", "true")
				  .load(input);
		climateData.printSchema();
		
		spark.udf().register("pointTest", new UDF1<String,Boolean>() {
			@Override
			public Boolean call(String arg1) throws Exception {
			// TODO Auto-generated method stub
				arg1 = arg1.replace("[", "");
				arg1 = arg1.replace("]", "");
				String[] p = arg1.split("_");
				double[] point = {Double.parseDouble(p[0]),Double.parseDouble(p[1])};
			boolean wanted = ClusterUtils.isPointInPolygon(point,bound);
			return wanted;
			}
			}, DataTypes.BooleanType);
		
		Dataset<Row> climateSubData = climateData.filter(col("date").$greater$eq(start))
				.filter(col("date").$less$eq(end))
				.filter(month(col("date")).$greater$eq(seasonStart))
				.filter(month(col("date")).$less$eq(seasonEnd))
				.filter(callUDF("pointTest",col("point")));
		
		//Dataset<Row> region = climateSubData.withColumn("point", sum())
		climateSubData.write().csv(output+"/filter");	
		
		//climateSubData.show();
	}
	
}


