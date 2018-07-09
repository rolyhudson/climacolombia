package org.rolson.emr.groupStationByMonthYear;

import java.io.IOException;
import java.time.LocalDate;

import java.time.format.DateTimeFormatter;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;


/**
 * Hello world!
 *
 */
public class App 
{
	private final static IntWritable one = new IntWritable( 1 );
	public static class AMapper extends Mapper<Object, Text, Text, IntWritable> {

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            
        	// Get the value as a String
    		String text = value.toString();
    		String[] pieces = text.split("\\,");
    		//5/4/2008 7:00:00 PM
    		String[] dtpieces = pieces[1].split("\\ ");
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
    		LocalDate ld = LocalDate.parse(dtpieces[0],formatter);
    		// Retrieve the station code first 6 characters
    		
    		// Output the code as the key and 1 as the value
    		
            context.write(new Text(ld.getYear()+"_"+ld.getMonth()), one);
        }
    }

    public static class AReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    	private IntWritable result = new IntWritable();
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
        	int sum = 0;
        	for (IntWritable val : values)
        	{
        	sum += val.get();
        	}
        	result.set(sum);
        	context.write(key, result);
        }
    }

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: <in> <out>");
            System.exit(2);
        }

        Job job = new Job(conf, "Monthly totals");
        job.setJarByClass(App.class);
        job.setMapperClass(AMapper.class);
        job.setReducerClass(AReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
