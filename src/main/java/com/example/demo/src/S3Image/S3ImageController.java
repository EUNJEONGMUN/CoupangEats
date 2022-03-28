package com.example.demo.src.S3Image;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/imagetest")
public class S3ImageController {
    private S3Uploader s3Uploader;

    @PostMapping("/images")
    public String upload(@RequestParam("data") MultipartFile multipartFile) throws IOException {
        System.out.println(">>here<<");
        s3Uploader.upload(multipartFile, "static");
        return "test";
    }
}
