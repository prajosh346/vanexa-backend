package com.vanexa.backend.controller;

import com.vanexa.backend.service.ImageUploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    public ImageUploadController(
            ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostMapping(
            value = "/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        Map<String, Object> response =
                imageUploadService.uploadImage(file);

        return ResponseEntity.ok(response);
    }
}