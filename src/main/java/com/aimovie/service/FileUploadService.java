package com.aimovie.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadService {

    private final VideoMetadataService videoMetadataService;

    @PostConstruct
    void initialize() {
        cloudImageFolder = normalizeFolder(cloudImageFolder);
        cloudVideoFolder = normalizeFolder(cloudVideoFolder);
    }

    @PreDestroy
    void shutdown() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.max-file-size}")
    private long maxFileSize;

    @Value("${app.allowed.video.formats}")
    private String allowedFormats;

    @Value("${app.upload.image.dir}")
    private String imageUploadDir;

    @Value("${app.upload.image.max-file-size}")
    private long maxImageFileSize;

    @Value("${app.allowed.image.formats}")
    private String allowedImageFormats;

    @Value("${app.storage.cloud.enabled:false}")
    private boolean cloudStorageEnabled;

    @Value("${app.storage.cloud.endpoint:}")
    private String cloudEndpoint;

    @Value("${app.storage.cloud.bucket:}")
    private String cloudBucket;

    @Value("${app.storage.cloud.access-key:}")
    private String cloudAccessKey;

    @Value("${app.storage.cloud.secret-key:}")
    private String cloudSecretKey;

    @Value("${app.storage.cloud.region:auto}")
    private String cloudRegion;

    @Value("${app.storage.cloud.image-folder:images}")
    private String cloudImageFolder;

    @Value("${app.storage.cloud.video-folder:videos}")
    private String cloudVideoFolder;

    @Value("${app.cdn.base-url:}")
    private String cdnBaseUrl;

    private S3Client s3Client;

    public String uploadVideoFile(MultipartFile file) throws IOException {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Video file saved locally: {}", uniqueFilename);

        if (cloudStorageEnabled) {
            try {
                String key = buildCloudKey(cloudVideoFolder, uniqueFilename);
                try (InputStream inputStream = Files.newInputStream(filePath)) {
                    long fileSize = Files.size(filePath);
                    String contentType = file.getContentType();
                    if (contentType == null) {
                        contentType = "video/mp4";
                    }
                    getS3Client().putObject(
                            PutObjectRequest.builder()
                                    .bucket(cloudBucket)
                                    .key(key)
                                    .contentType(contentType)
                                    .build(),
                            RequestBody.fromInputStream(inputStream, fileSize)
                    );
                    log.info("Video uploaded to cloud storage: {}", key);
                } catch (SdkException e) {
                    log.warn("Failed to upload video to cloud storage, keeping local copy: {}", e.getMessage());
                }
            } catch (Exception e) {
                log.warn("Error uploading video to cloud storage: {}", e.getMessage());
            }
        }

        return uniqueFilename;
    }

    public Path getVideoFilePath(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    public boolean fileExists(String filename) {
        return Files.exists(getVideoFilePath(filename));
    }

    public void deleteVideoFile(String filename) throws IOException {
        if (filename == null || filename.isBlank()) {
            return;
        }
        if (cloudStorageEnabled) {
            String key = buildCloudKey(cloudVideoFolder, filename);
            try {
                getS3Client().deleteObject(DeleteObjectRequest.builder()
                        .bucket(cloudBucket)
                        .key(key)
                        .build());
                log.info("Video deleted from cloud storage: {}", key);
            } catch (SdkException e) {
                log.warn("Failed to delete video {} from cloud storage: {}", key, e.getMessage());
            }
            return;
        }
        Path filePath = getVideoFilePath(filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Video file deleted successfully: {}", filename);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + (maxFileSize / (1024 * 1024)) + " MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is invalid");
        }

        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedFormatsList = Arrays.asList(allowedFormats.split(","));
        
        if (!allowedFormatsList.contains(fileExtension)) {
            throw new IllegalArgumentException("File format not allowed. Allowed formats: " + allowedFormats);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public long getFileSize(String filename) throws IOException {
        Path filePath = getVideoFilePath(filename);
        if (Files.exists(filePath)) {
            return Files.size(filePath);
        }
        return 0;
    }

    public VideoUploadResult uploadVideoWithMetadata(MultipartFile file) throws IOException {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Video file saved locally for processing: {}", uniqueFilename);

        if (cloudStorageEnabled) {
            try {
                String key = buildCloudKey(cloudVideoFolder, uniqueFilename);
                try (InputStream inputStream = Files.newInputStream(filePath)) {
                    long fileSize = Files.size(filePath);
                    String contentType = file.getContentType();
                    if (contentType == null) {
                        contentType = "video/mp4";
                    }
                    getS3Client().putObject(
                            PutObjectRequest.builder()
                                    .bucket(cloudBucket)
                                    .key(key)
                                    .contentType(contentType)
                                    .build(),
                            RequestBody.fromInputStream(inputStream, fileSize)
                    );
                    log.info("Video uploaded to cloud storage: {}", key);
                } catch (SdkException e) {
                    log.warn("Failed to upload video to cloud storage, keeping local copy: {}", e.getMessage());
                }
            } catch (Exception e) {
                log.warn("Error uploading video to cloud storage: {}", e.getMessage());
            }
        }

        VideoMetadataService.VideoMetadata metadata = videoMetadataService.extractMetadata(file);

        log.info("Video uploaded with metadata: {}", metadata);

        return new VideoUploadResult(uniqueFilename, originalFilename, metadata);
    }

    public String uploadImageFile(MultipartFile file) throws IOException {
        validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;

        if (cloudStorageEnabled) {
            String key = buildCloudKey(cloudImageFolder, uniqueFilename);
            try (InputStream inputStream = file.getInputStream()) {
                getS3Client().putObject(
                        PutObjectRequest.builder()
                                .bucket(cloudBucket)
                                .key(key)
                                .contentType(file.getContentType())
                                .build(),
                        RequestBody.fromInputStream(inputStream, file.getSize())
                );
                log.info("Image uploaded to cloud storage: {}", key);
            } catch (SdkException e) {
                log.error("Failed to upload image to cloud storage", e);
                throw new IOException("Failed to upload image to cloud storage", e);
            }
        } else {
            Path uploadPath = Paths.get(imageUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Image file uploaded successfully: {}", uniqueFilename);
        }

        return uniqueFilename;
    }

    public Path getImageFilePath(String filename) {
        return Paths.get(imageUploadDir).resolve(filename);
    }

    public boolean imageFileExists(String filename) {
        if (cloudStorageEnabled) {
            log.debug("imageFileExists check skipped in cloud mode for filename={}", filename);
            return false;
        }
        return Files.exists(getImageFilePath(filename));
    }

    public void deleteImageFile(String filename) throws IOException {
        if (filename == null || filename.isBlank()) {
            return;
        }
        if (cloudStorageEnabled) {
            String key = buildCloudKey(cloudImageFolder, filename);
            try {
                getS3Client().deleteObject(DeleteObjectRequest.builder()
                        .bucket(cloudBucket)
                        .key(key)
                        .build());
                log.info("Image deleted from cloud storage: {}", key);
            } catch (SdkException e) {
                log.warn("Failed to delete image {} from cloud storage: {}", key, e.getMessage());
            }
            return;
        }
        Path filePath = getImageFilePath(filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Image file deleted successfully: {}", filename);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxImageFileSize) {
            throw new IllegalArgumentException("Image file size exceeds maximum allowed size of " + (maxImageFileSize / (1024 * 1024)) + " MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is invalid");
        }

        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedImageFormatsList = Arrays.asList(allowedImageFormats.split(","));
        
        if (!allowedImageFormatsList.contains(fileExtension)) {
            throw new IllegalArgumentException("Image format not allowed. Allowed formats: " + allowedImageFormats);
        }
    }

    public static class VideoUploadResult {
        private String filename;
        private String originalFilename;
        private VideoMetadataService.VideoMetadata metadata;

        public VideoUploadResult(String filename, String originalFilename, VideoMetadataService.VideoMetadata metadata) {
            this.filename = filename;
            this.originalFilename = originalFilename;
            this.metadata = metadata;
        }

        // Getters
        public String getFilename() { return filename; }
        public String getOriginalFilename() { return originalFilename; }
        public VideoMetadataService.VideoMetadata getMetadata() { return metadata; }
    }

    public String buildPublicImageUrl(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        if (cloudStorageEnabled) {
            return buildCdnUrl(cloudImageFolder, filename);
        }
        return "/api/images/" + filename;
    }

    public String buildPublicVideoUrl(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        if (cloudStorageEnabled) {
            return buildCdnUrl(cloudVideoFolder, filename);
        }
        return "/api/videos/stream/" + filename;
    }

    public String buildPublicVideoUrl(Long movieId, String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        if (cloudStorageEnabled) {
            String key = buildCloudKey(cloudVideoFolder, movieId + "/" + filename);
            if (cdnBaseUrl == null || cdnBaseUrl.isBlank()) {
                return key;
            }
            String base = cdnBaseUrl.endsWith("/") ? cdnBaseUrl.substring(0, cdnBaseUrl.length() - 1) : cdnBaseUrl;
            return base + "/" + key;
        }
        return "/api/videos/stream/" + movieId + "/" + filename;
    }

    public String uploadVideoFileFromPath(Path filePath, Long movieId, String filename) throws IOException {
        if (cloudStorageEnabled) {
            String key = buildCloudKey(cloudVideoFolder, movieId + "/" + filename);
            try (InputStream inputStream = Files.newInputStream(filePath)) {
                long fileSize = Files.size(filePath);
                String contentType = "video/mp4";
                if (filename.toLowerCase().endsWith(".mkv")) {
                    contentType = "video/x-matroska";
                } else if (filename.toLowerCase().endsWith(".avi")) {
                    contentType = "video/x-msvideo";
                } else if (filename.toLowerCase().endsWith(".mov")) {
                    contentType = "video/quicktime";
                }
                
                getS3Client().putObject(
                        PutObjectRequest.builder()
                                .bucket(cloudBucket)
                                .key(key)
                                .contentType(contentType)
                                .build(),
                        RequestBody.fromInputStream(inputStream, fileSize)
                );
                log.info("Video file uploaded to cloud storage from path: {}", key);
                return filename;
            } catch (SdkException e) {
                log.error("Failed to upload video file to cloud storage", e);
                throw new IOException("Failed to upload video file to cloud storage", e);
            }
        }
        return filename;
    }

    private synchronized S3Client getS3Client() {
        if (!cloudStorageEnabled) {
            return null;
        }
        if (s3Client == null) {
            if (cloudEndpoint == null || cloudEndpoint.isBlank()) {
                throw new IllegalStateException("Cloud storage endpoint is not configured");
            }
            if (cloudBucket == null || cloudBucket.isBlank()) {
                throw new IllegalStateException("Cloud storage bucket is not configured");
            }
            if (cloudAccessKey == null || cloudAccessKey.isBlank() ||
                    cloudSecretKey == null || cloudSecretKey.isBlank()) {
                throw new IllegalStateException("Cloud storage credentials are not configured");
            }

            AwsBasicCredentials credentials = AwsBasicCredentials.create(cloudAccessKey, cloudSecretKey);
            s3Client = S3Client.builder()
                    .endpointOverride(URI.create(cloudEndpoint))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build())
                    .region(Region.of(cloudRegion != null && !cloudRegion.isBlank() ? cloudRegion : "auto"))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        }
        return s3Client;
    }

    private String buildCloudKey(String folder, String filename) {
        if (folder == null || folder.isBlank()) {
            return filename;
        }
        return folder + filename;
    }

    private String buildCdnUrl(String folder, String filename) {
        String key = buildCloudKey(folder, filename);
        if (cdnBaseUrl == null || cdnBaseUrl.isBlank()) {
            return key;
        }
        String base = cdnBaseUrl.endsWith("/") ? cdnBaseUrl.substring(0, cdnBaseUrl.length() - 1) : cdnBaseUrl;
        return base + "/" + key;
    }

    private String normalizeFolder(String folder) {
        if (folder == null) {
            return "";
        }
        String normalized = folder.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.isEmpty() && !normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }
}
