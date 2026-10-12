package com.vanexa.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, Object> uploadImage(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select an image to upload"
            );
        }
        byte[] fileBytes = file.getBytes();

        String contentType = file.getContentType();

        boolean isPng =
                fileBytes.length >= 8
                        && fileBytes[0] == (byte) 0x89
                        && fileBytes[1] == 0x50
                        && fileBytes[2] == 0x4E
                        && fileBytes[3] == 0x47
                        && fileBytes[4] == 0x0D
                        && fileBytes[5] == 0x0A
                        && fileBytes[6] == 0x1A
                        && fileBytes[7] == 0x0A;

        boolean isOtherImage =
                contentType != null
                        && (
                        contentType.equalsIgnoreCase("image/jpeg")
                                || contentType.equalsIgnoreCase("image/jpg")
                                || contentType.equalsIgnoreCase("image/gif")
                                || contentType.equalsIgnoreCase("image/webp")
                );

        if (!isPng && !isOtherImage) {
            throw new IllegalArgumentException(
                    "Only PNG, JPEG, GIF, and WebP images are allowed"
            );
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                fileBytes,
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "folder", "vanexa-studio/blogs"
                )
        );

        Map<String, Object> response = new HashMap<>();
        response.put("url", result.get("secure_url"));
        response.put("publicId", result.get("public_id"));
        response.put("format", result.get("format"));
        response.put("bytes", result.get("bytes"));

        return response;
    }
}