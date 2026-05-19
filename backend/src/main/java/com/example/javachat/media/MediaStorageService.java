package com.example.javachat.media;

import com.example.javachat.common.BusinessException;
import com.example.javachat.common.ErrorCode;
import com.example.javachat.media.dto.UploadResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaStorageService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private final Path uploadRoot;

    public MediaStorageService(@Value("${app.upload.base-dir:uploads}") String uploadBaseDir) {
        this.uploadRoot = Path.of(uploadBaseDir).toAbsolutePath().normalize();
    }

    public UploadResponse storeImage(MultipartFile file, String folder) {
        validateImage(file);
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        String fileName = UUID.randomUUID() + extensionFor(contentType);
        Path targetDirectory = uploadRoot.resolve(folder).normalize();
        Path target = targetDirectory.resolve(fileName).normalize();
        if (!target.startsWith(targetDirectory)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件名不合法");
        }
        try {
            Files.createDirectories(targetDirectory);
            file.transferTo(target.toFile());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "文件保存失败");
        }
        return new UploadResponse("/uploads/" + folder + "/" + fileName, contentType, file.getSize());
    }

    private static void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只支持 JPG、PNG、GIF、WebP 图片");
        }
    }

    private static String extensionFor(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
