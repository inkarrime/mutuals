package com.mutuals.storage;

import com.mutuals.common.exception.InvalidOperationException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

final class ImageValidator {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private ImageValidator() {
    }

    static String extensionFor(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidOperationException("The uploaded file is empty");
        }
        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) {
            throw new InvalidOperationException("Only JPEG, PNG or WEBP images are allowed");
        }
        return extension;
    }
}
