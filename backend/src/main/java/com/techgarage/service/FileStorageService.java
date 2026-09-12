package com.techgarage.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /** Stores the file locally (swap implementation for S3/Cloudinary in production) and returns a public URL path. */
    String store(MultipartFile file);
}
