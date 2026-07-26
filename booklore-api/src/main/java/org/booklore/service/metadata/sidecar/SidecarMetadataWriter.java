package org.booklore.service.metadata.sidecar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class SidecarMetadataWriter {

    public void deleteSidecarFiles(Path bookPath) {
        if (bookPath == null) {
            return;
        }

        try {
            Path sidecarPath = getSidecarPath(bookPath);
            if (Files.exists(sidecarPath)) {
                Files.delete(sidecarPath);
                log.info("Deleted sidecar file: {}", sidecarPath);
            }

            Path coverPath = getCoverPath(bookPath);
            if (Files.exists(coverPath)) {
                Files.delete(coverPath);
                log.info("Deleted sidecar cover file: {}", coverPath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete sidecar files for {}: {}", bookPath, e.getMessage());
        }
    }

    public void moveSidecarFiles(Path oldBookPath, Path newBookPath) {
        if (oldBookPath == null || newBookPath == null) {
            return;
        }

        try {
            Path oldSidecarPath = getSidecarPath(oldBookPath);
            if (Files.exists(oldSidecarPath)) {
                Path newSidecarPath = getSidecarPath(newBookPath);
                Files.createDirectories(newSidecarPath.getParent());
                Files.move(oldSidecarPath, newSidecarPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Moved sidecar file from {} to {}", oldSidecarPath, newSidecarPath);
            }

            Path oldCoverPath = getCoverPath(oldBookPath);
            if (Files.exists(oldCoverPath)) {
                Path newCoverPath = getCoverPath(newBookPath);
                Files.move(oldCoverPath, newCoverPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Moved sidecar cover from {} to {}", oldCoverPath, newCoverPath);
            }
        } catch (IOException e) {
            log.warn("Failed to move sidecar files from {} to {}: {}", oldBookPath, newBookPath, e.getMessage());
        }
    }

    public Path getSidecarPath(Path bookPath) {
        String fileName = bookPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
        return bookPath.getParent().resolve(baseName + ".metadata.json");
    }

    public Path getCoverPath(Path bookPath) {
        String fileName = bookPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
        return bookPath.getParent().resolve(baseName + ".cover.jpg");
    }

}
