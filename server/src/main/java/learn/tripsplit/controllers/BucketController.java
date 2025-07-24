package learn.tripsplit.controllers;

import learn.tripsplit.domain.AmazonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/storage")
public class BucketController {

    private final AmazonClient amazonClient;

    @Autowired
    public BucketController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return amazonClient.uploadFile(file);
    }

    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestParam("url") String fileUrl) {
        return amazonClient.deleteFileFromS3Bucket(fileUrl);
    }
}
