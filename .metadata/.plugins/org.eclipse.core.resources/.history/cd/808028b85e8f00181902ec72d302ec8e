package org.rolson.emr.emrcycle1;

import java.io.File;
import java.util.Iterator;

import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.devicefarm.model.Upload;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class ClimaBucket {
	public Bucket createBucket(String bucket_name) {
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion("us-west-2")
                .build();
        Bucket b = null;
        if (s3.doesBucketExistV2(bucket_name)) {
            System.out.format("Bucket %s already exists.\n", bucket_name);
            b = getBucket(bucket_name);
        } else {
            try {
                b = s3.createBucket(bucket_name);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }
        return b;
    }
	public Bucket getBucket(String bucket_name) {
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion("us-west-2")
                .build();
        Bucket named_bucket = null;
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucket_name)) {
                named_bucket = b;
            }
        }
        return named_bucket;
    }
	public void listBuckets() {
		final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .build();
		List<Bucket> buckets = s3.listBuckets();
		System.out.println("Your Amazon S3 buckets are:");
		 
		for (Bucket b : buckets) {
		    System.out.println("* " + b.getName());
		}
	}
	public void uploadMultiPart()
	{
		String clientRegion = "us-east-1";
        String bucketName = "rolyhudsontestbucket1";
        String stringObjKeyName = "newfile";
        String fileObjKeyName = "VV.csv";
        String fileName = "D:\\WORK\\piloto\\Climate\\NOAA\\data\\variables\\VV.csv";
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            		.withRegion("us-east-1")
                    .build();
        
            DefaultAWSCredentialsProviderChain credentialProviderChain = new DefaultAWSCredentialsProviderChain();
            TransferManager tm = new TransferManager(credentialProviderChain.getCredentials());
            
            // TransferManager processes all transfers asynchronously,
            // so this call returns immediately.
            com.amazonaws.services.s3.transfer.Upload upload = tm.upload(bucketName, fileObjKeyName, new File(fileName));
            System.out.println("Object upload started");
    
            // Optionally, wait for the upload to finish before continuing.
//            upload.waitForCompletion();
//            System.out.println("Object upload complete");
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
        }
	}
	public void uploadFileToBucket() {
		String clientRegion = "us-east-1";
        String bucketName = "rolyhudsontestbucket1";
        String stringObjKeyName = "newfile";
        String fileObjKeyName = "VV.csv";
        String fileName = "D:\\WORK\\piloto\\Climate\\NOAA\\data\\variables\\VV.csv";

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            		.withRegion("us-east-1")
                    .build();
        
            // Upload a text string as a new object.
            //s3Client.putObject(bucketName, stringObjKeyName, "Uploaded String Object");
            
            // Upload a file as a new object with ContentType and title specified.
            PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, new File(fileName));
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("plain/text");
            metadata.addUserMetadata("x-amz-meta-title", "someTitle");
            request.setMetadata(metadata);
            s3Client.putObject(request);
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
        }
	}
	public void deleteBucket(String bucket_name) {
		final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion("us-west-2")
                .build();
		
		System.out.println("Deleting S3 bucket: " + bucket_name);
        

        try {
            System.out.println(" - removing objects from bucket");
            ObjectListing object_listing = s3.listObjects(bucket_name);
            while (true) {
                for (Iterator<?> iterator =
                        object_listing.getObjectSummaries().iterator();
                        iterator.hasNext();) {
                    S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
                    s3.deleteObject(bucket_name, summary.getKey());
                }

                // more object_listing to retrieve?
                if (object_listing.isTruncated()) {
                    object_listing = s3.listNextBatchOfObjects(object_listing);
                } else {
                    break;
                }
            };

            System.out.println(" - removing versions from bucket");
            VersionListing version_listing = s3.listVersions(
                    new ListVersionsRequest().withBucketName(bucket_name));
            while (true) {
                for (Iterator<?> iterator =
                        version_listing.getVersionSummaries().iterator();
                        iterator.hasNext();) {
                    S3VersionSummary vs = (S3VersionSummary)iterator.next();
                    s3.deleteVersion(
                            bucket_name, vs.getKey(), vs.getVersionId());
                }

                if (version_listing.isTruncated()) {
                    version_listing = s3.listNextBatchOfVersions(
                            version_listing);
                } else {
                    break;
                }
            }

            System.out.println(" OK, bucket ready to delete!");
            s3.deleteBucket(bucket_name);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Done!");
	}
}

