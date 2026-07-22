package learn.tripsplit.domain;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class AmazonClient {
    private AmazonS3 s3client;

    @Value("${amazonProperties.accessKey}")
    private String accessKey;

    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    @Value("${amazonProperties.region}")
    private String region;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;

    @Value("${amazonProperties.publicUrl:}")
    private String publicBaseUrl;

    @Value("${amazonProperties.acl:Private}")
    private CannedAccessControlList acl;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        // Endpoint-based config lets this same S3 SDK talk to any S3-compatible store
        // (Cloudflare R2, etc.), not just AWS. R2 requires path-style access.
        s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpointUrl, region))
                .withPathStyleAccessEnabled(true)
                .build();
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = Files.createTempFile("s3-", file.getOriginalFilename()).toFile();
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        convFile.deleteOnExit();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return System.currentTimeMillis() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }

    public String uploadFile(MultipartFile multipartFile) {
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            s3client.putObject(
                    new PutObjectRequest(bucketName, fileName, file)
            );
            return publicUrlFor(fileName);
        } catch (Exception e) {
            throw new RuntimeException("S3 upload error: " + e.getMessage());
        }
    }

    // R2 (and most S3-compatible stores) don't serve objects at the S3 API endpoint,
    // so build the browser-facing URL from the configured public base. Fall back to the
    // S3 API URL when no public base is set (plain AWS).
    private String publicUrlFor(String fileName) {
        if (publicBaseUrl == null || publicBaseUrl.isEmpty()) {
            return s3client.getUrl(bucketName, fileName).toString();
        }
        String base = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;
        return base + "/" + fileName;
    }

    public void deleteFileFromS3Bucket(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3client.deleteObject(bucketName, fileName);
    }
}
