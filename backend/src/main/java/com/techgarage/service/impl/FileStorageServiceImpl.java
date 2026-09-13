package com.techgarage.service.impl;

import com.techgarage.exception.BadRequestException;
import com.techgarage.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 25L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "pdf", "txt", "log", "zip");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp", "application/pdf",
            "text/plain", "application/zip", "application/x-zip-compressed"
    );

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File must be 25MB or smaller");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        if (originalName.contains("..")) {
            throw new BadRequestException("Invalid file name");
        }
        String extension = StringUtils.getFilenameExtension(originalName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Unsupported file type. Allowed: PNG, JPG, WEBP, PDF, TXT, LOG, ZIP");
        }
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Unsupported content type");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            String storedName = UUID.randomUUID() + "." + extension.toLowerCase(Locale.ROOT);
            Path target = uploadPath.resolve(storedName).normalize();
            if (!target.getParent().equals(uploadPath)) {
                throw new BadRequestException("Invalid storage path");
            }
            byte[] header = readHeader(file, 16);
            validateSignature(extension.toLowerCase(Locale.ROOT), header, file);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target);
            }
            return "/uploads/" + storedName;
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file");
        }
    }
    private byte[] readHeader(MultipartFile file, int length) throws IOException {
        try (InputStream in = file.getInputStream()) {
            return in.readNBytes(length);
        }
    }

    private void validateSignature(String extension, byte[] header, MultipartFile file) throws IOException {
        boolean valid;
        switch (extension) {
            case "png" -> valid = startsWith(header, new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
            case "jpg", "jpeg" -> valid = startsWith(header, new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF});
            case "webp" -> valid = header.length >= 12 && startsWith(header, new byte[]{0x52,0x49,0x46,0x46})
                    && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50;
            case "pdf" -> valid = startsWith(header, new byte[]{0x25,0x50,0x44,0x46,0x2D});
            case "zip" -> valid = startsWith(header, new byte[]{0x50,0x4B,0x03,0x04}) || startsWith(header, new byte[]{0x50,0x4B,0x05,0x06});
            case "txt", "log" -> valid = !containsBinaryNull(header);
            default -> valid = false;
        }
        if (!valid) throw new BadRequestException("File content does not match its extension");

        if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg")) {
            try (InputStream in = file.getInputStream()) {
                BufferedImage image = ImageIO.read(in);
                if (image == null) throw new BadRequestException("Invalid image file");
            }
        }
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        return value.length >= prefix.length && Arrays.equals(Arrays.copyOf(value, prefix.length), prefix);
    }

    private boolean containsBinaryNull(byte[] value) {
        for (byte b : value) if (b == 0) return true;
        return false;
    }

}
