import org.apache.spark.sql.SparkSession;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
public class SimpleApp {

	public static void main(String[] args) {
		JavaSparkContext sc = new JavaSparkContext("local[*]", "SimpleApp", 
				System.getenv("SPARK_HOME"), System.getenv("JARS"));
		
		String logFile = "C:/Spark/README.md"; // Should be some file on your system
	    SparkSession spark = SparkSession.builder().appName("Simple Application").getOrCreate();
	    Dataset<String> logData = spark.read().textFile(logFile).cache();

	    long numAs = logData.filter(s -> s.contains("a")).count();
	    long numBs = logData.filter(s -> s.contains("b")).count();

	    System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);

	    spark.stop();

	}

}
