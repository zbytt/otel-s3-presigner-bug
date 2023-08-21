package pl.zt.otels3signersample;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@SpringBootApplication
public class OtelS3SignerSampleApplication {

    static {
        System.setProperty("aws.accessKeyId", "test");
        System.setProperty("aws.secretAccessKey", "test");
        System.setProperty("aws.sessionToken", "test");
    }

    public static void main(String[] args) {
        SpringApplication.run(OtelS3SignerSampleApplication.class, args);
    }

    @Bean
    S3Presigner s3Presigner() {
        return S3Presigner.builder().region(Region.US_EAST_2).build();
    }

    @Bean
    S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of("us-east-2"))
                .endpointOverride(URI.create("http://localhost:5055"))
                .build();
    }

    @Bean
    AmazonS3 amazonS3() {
        return AmazonS3Client.builder()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:5055",
                        "us-east-2"))
                .withPathStyleAccessEnabled(true)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("A", "B")))
                .build();
    }

    @Bean
    @Profile("bug")
    Signer v2Signer(S3Presigner s3Presigner) {
        return () -> s3Presigner
                .presignPutObject(PutObjectPresignRequest.builder()
                        .putObjectRequest(builder -> builder.bucket("test").key("test"))
                        .signatureDuration(Duration.ofDays(1))
                        .build())
                .url()
                .toString();
    }

    @Bean
    @Profile("!bug")
    Signer signerv1(AmazonS3 amazonS3) {
        final Instant ts = Instant.now().plus(Duration.ofHours(1));
        return () -> amazonS3.generatePresignedUrl("test", "test", Date.from(ts)).toString();
    }
}
