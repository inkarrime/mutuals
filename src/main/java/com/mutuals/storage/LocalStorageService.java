package com.mutuals.storage;

import com.mutuals.common.exception.StorageException;
import com.mutuals.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "mutuals.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path root;

    public LocalStorageService(AppProperties properties) {
        this.root = Path.of(properties.storage().localDir()).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, String folder) {
        String extension = ImageValidator.extensionFor(file);
        String fileName = UUID.randomUUID() + "." + extension;
        Path target = root.resolve(folder).resolve(fileName).normalize();
        if (!target.startsWith(root)) {
            throw new StorageException("Invalid storage path");
        }
        try (InputStream input = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new StorageException("Could not store file");
        }
        return "/uploads/" + folder + "/" + fileName;
    }
}
