import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class Main {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("Line_Count");
        JavaSparkContext ctx = new JavaSparkContext(conf);
        JavaRDD<String> textLoadRDD = ctx.textFile("C:/Spark/README.md");
	System.out.println(textLoadRDD.count());
	
	}

}
