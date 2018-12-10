package Coordination;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.transfer.TransferManager;


public class DataManager {
	private AmazonS3 s3client;
	private TransferManager xfm;
	private String bucketName = "clustercolombia";
	private String keyName = "climateData";
	public DataManager()
	{
		setupClient();
	}
	private void setupClient(){
		s3client = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .build();
	}
	private void setupXFManger(){
		DefaultAWSCredentialsProviderChain credentialProviderChain = new DefaultAWSCredentialsProviderChain();
		xfm =  new TransferManager(credentialProviderChain.getCredentials());
	}
	public List<String> listBucketContents()
	{
		List<String> keys = new ArrayList<String>();
		ObjectListing objectListing = s3client.listObjects(bucketName);
		if(!objectListing.isTruncated())
		{
			for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
			    //System.out.print((os.getKey()));
				keys.add(os.getKey());
			}
		}
		else
		{
			while(objectListing.isTruncated())
			{
				for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
				    //System.out.print((os.getKey()));
					keys.add(os.getKey());
				}
				objectListing = s3client.listNextBatchOfObjects(objectListing);
			}
		}
		return keys;
	}
	public List<String> listBucketContentsPrefixedV2(String prefix){
		List<String> keys = new ArrayList<String>();
		System.out.println(("listing objects with prefix: "+prefix));
		
		ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(prefix);
        ListObjectsV2Result result;
		
        do {
            result = s3client.listObjectsV2(req);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                //System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
                keys.add(objectSummary.getKey());
            }
            // If there are more than maxKeys keys in the bucket, get a continuation token
            // and list the next objects.
            String token = result.getNextContinuationToken();
            System.out.println("Next Continuation Token: " + token);
            req.setContinuationToken(token);
        } while (result.isTruncated());
        
		
		return keys;
	}
	public List<String> listBucketContentsPrefixed(String prefix){
		List<String> keys = new ArrayList<String>();
		System.out.println(("listing objects with prefix: "+prefix));
		ListObjectsRequest req = new ListObjectsRequest()
				.withBucketName(bucketName)
				.withPrefix(prefix);
				//.withDelimiter(DELIMITER);
		ObjectListing listing = s3client.listObjects(req);
		if(!listing.isTruncated())
		{
			for (S3ObjectSummary summary: listing.getObjectSummaries()) {
			    
			    keys.add(summary.getKey());
			}
		}
		else
		{
			while(listing.isTruncated())
			{
				for(S3ObjectSummary os : listing.getObjectSummaries()) {
				    
					keys.add(os.getKey());
				}
				listing = s3client.listNextBatchOfObjects(listing);
			}
		}
		return keys;
	}
	public List<String> readFromS3( String key) throws IOException {
		System.out.println(("reading from s3 with key: "+key));
	    S3Object s3object = s3client.getObject(new GetObjectRequest(bucketName, key));
	    System.out.println(s3object.getObjectMetadata().getContentType());
	    System.out.println(s3object.getObjectMetadata().getContentLength());
	    InputStreamReader is = new InputStreamReader(s3object.getObjectContent());
	    BufferedReader reader = new BufferedReader(is);
	    List<String> lines = new ArrayList<String>();
	   
	    String line;
	    
	    while((line = reader.readLine()) != null) {
	      // can copy the content locally as well
	      // using a buffered writer
	    	
	    	lines.add(line);
	      //System.out.println(line);
	    }
	    return lines;
	  }
	public String getString(String key) throws AmazonServiceException
	{
		String contents = "";
		try{
			contents = s3client.getObjectAsString(bucketName, key);
		}
		catch(SdkClientException e) {
	        // Amazon S3 couldn't be contacted for a response, or the client
	        // couldn't parse the response from Amazon S3.
	        e.printStackTrace();
	    }
		return contents;
	}
	public boolean accessObject(String bucket, String key)
	{
		boolean exists = false;
		try
		{
		s3client.getObject(bucket, key);
		exists = true;
		}
		catch(SdkClientException e)
		{
			e.printStackTrace();
		}
		//S3ObjectInputStream inputStream = s3object.getObjectContent();
		return exists;
	}
	public boolean delete(String keyName)
	{
		boolean result = false;
		try {
             s3client.deleteObject(new DeleteObjectRequest(this.bucketName, keyName));
        }
        catch(AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
            result = true;
        }
        catch(SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
		return result;
	}
	public boolean copyMove(String originBucket,String destinationBucket,String originKey, String destinationKey) throws AmazonServiceException
	{
		boolean result = false;
		try{
		CopyObjectResult copyresult = s3client.copyObject(
				originBucket, 
				originKey, 
				destinationBucket, 
				destinationKey
				);
		result = true;
		}
		catch(SdkClientException e) {
	        // Amazon S3 couldn't be contacted for a response, or the client
	        // couldn't parse the response from Amazon S3.
	        e.printStackTrace();
	    }
		return result;
	}
	public boolean uploadStringToFile(String keypath,String text,String targetBucket,String contenttype) throws AmazonServiceException
	{
		boolean result = false;
		this.keyName=keypath;
		InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
		long cLength=0;
		try {
			cLength = IOUtils.toByteArray(stream).length;
			stream.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
		setupXFManger();
		try {
			 ObjectMetadata metadata = new ObjectMetadata();
	            metadata.setContentType(contenttype);
	           metadata.setContentLength(cLength);
				
			PutObjectRequest request = new PutObjectRequest(targetBucket, keypath, stream, metadata);
			PutObjectResult putResult = this.s3client.putObject(request);
			result = true;
		}
		catch(SdkClientException e) {
	        // Amazon S3 couldn't be contacted for a response, or the client
	        // couldn't parse the response from Amazon S3.
	        e.printStackTrace();
	    }
		return result;
	}
	public boolean upload(File f,String keypath)
	{
		this.keyName=keypath;
		
		setupXFManger();
		//see https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-s3-transfermanager.html
		boolean result = false;
		try{
		 // TransferManager processes all transfers asynchronously,
        // so this call returns immediately.
        com.amazonaws.services.s3.transfer.Upload upload = xfm.upload(bucketName, keyName+"/"+f.getName(), f);
        System.out.println("Object upload started");

        // Optionally, wait for the upload to finish before continuing.
        upload.waitForCompletion();
        System.out.println("Object upload complete");
        result = true;
	    }
	    catch(AmazonServiceException e) {
	        // The call was transmitted successfully, but Amazon S3 couldn't process 
	        // it, so it returned an error response.
	        e.printStackTrace();
	    }
	    catch(SdkClientException e) {
	        // Amazon S3 couldn't be contacted for a response, or the client
	        // couldn't parse the response from Amazon S3.
	        e.printStackTrace();
	    } catch (AmazonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	 public String getBucketName(){
		 return bucketName;
	 }
	 public void setBucketName(String bucket){
		 bucketName = bucket;
	 }
	 public String getKeyName(){
		 return keyName;
	 }
	 public void setKeyName(String key){
		 keyName = key;
	 }
}
