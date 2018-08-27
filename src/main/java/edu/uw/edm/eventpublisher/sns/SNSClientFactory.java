package edu.uw.edm.eventpublisher.sns;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Maxime Deravet Date: 8/23/18
 */
public class SNSClientFactory {

    private final String region;
    private final String accessKeyId;
    private final String secretAccessKey;


    private final AmazonSNSClientBuilder builder;

    public SNSClientFactory(String region, String accessKeyId, String secretAccessKey) {
        this.region = region;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;


        AmazonSNSClientBuilder builder = AmazonSNSClientBuilder
                .standard()
                .withRegion(this.region);

        if (StringUtils.isNotBlank(this.accessKeyId) && StringUtils.isNotBlank(this.secretAccessKey)) {
            builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(this.accessKeyId, this.secretAccessKey)));
        }

        this.builder = builder;
    }

    public AmazonSNS getSNSClient() {
        return this.builder.build();
    }
}
